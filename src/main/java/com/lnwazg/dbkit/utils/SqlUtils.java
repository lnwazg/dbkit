package com.lnwazg.dbkit.utils;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;

import com.lnwazg.dbkit.anno.dao.Param;
import com.lnwazg.dbkit.pagination.PaginationProcessor;
import com.lnwazg.kit.list.Lists;
import com.lnwazg.kit.validate.Validates;

/**
 * Sql工具类
 * @author nan.li
 * @version 2016年5月24日
 */
public class SqlUtils
{
    /**
     * 获取结果集中的列名称列表
     * @author nan.li
     * @param rs
     * @return
     */
    public static List<String> getResultSetColNames(ResultSet rs)
    {
        List<String> colNames = new ArrayList<>();
        try
        {
            ResultSetMetaData metaData = rs.getMetaData();
            int numColumn = metaData.getColumnCount();
            for (int i = 1; i <= numColumn; ++i)
            {
                String colName = metaData.getColumnLabel(i);//列名称
                // ignore row number
                //如果不是行号，则是一个正常的行，才将其加入到列汇总里面
                if (!PaginationProcessor.COLUMN_ROW_NUMBER.equalsIgnoreCase(colName))
                {
                    colNames.add(colName);
                }
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return colNames;
    }
    
    /**
     * 注入所有的参数<br>
     * 根据注解注入，以及根据map的内容进行注入
     * @author nan.li
     * @param method
     * @param args  telephone、age
     * @param sql   select * from User where telephone=#{telephone} and age=${age}
     * @return
     */
    @SuppressWarnings("unchecked")
    public static String injectAllParams(Method method, Object[] args, String sql)
        throws SQLException
    {
        //获取参数表
        Parameter[] parameters = method.getParameters();
        //取出参数的值，注入sql语句，并重新拼接sql
        if (parameters.length > 0)
        {
            //这两种方式是互斥的！
            //Map方式与传参数的方式不可同时出现，以减少DAO层接口的复杂性！
            
            //1. 对无注解的并且参数是Map<String, Object>的这种情况，进行map式的替换
            //@Select("select * from city_info where city_id=#{city_id}")
            //CityInfo queryOneCity(Map<String, Object> paramMap);
            //假如只有一个参数，并且该参数是一个map，并且参数上没有写注解。则可以符合条件：
            if (parameters.length == 1 && parameters[0].getType() == Map.class)
            {
                Pair<List<String>, List<String>> pair = getMybatisLikeParams(sql);
                Map<String, Object> paramMap = (Map<String, Object>)args[0];
                sql = injectMapParams(sql, pair, paramMap);
            }
            else
            {
                //2.进行注解式的替换
                //@Select("select * from city_info where city_id=#{city_id} and ${sql}")
                //CityInfo queryOneCity(@Param("city_id") String city_id, @Param("sql") String sql);
                for (int i = 0; i < parameters.length; i++)
                {
                    Parameter parameter = parameters[i];
                    if (parameter.isAnnotationPresent(Param.class))
                    {
                        //参数替换
                        Param param = parameter.getAnnotation(Param.class);//取出参数名称上面的注解
                        //假如abc的值为123
                        //如果是#{abc}，则替换成'123'        （加了引号的）
                        //如果是${abc}，则替换成123
                        
                        //参数表达式的名称
                        String elName = param.value();//city_id、sql
                        if (StringUtils.isEmpty(elName))
                        {
                            throw new SQLException(String.format("DAO方法：%s按照值传递时，args[%d]的@Param注解值不能为空字符串！", method.getName(), i));
                        }
                        String elValue = String.valueOf(args[i]);
                        
                        //sql注入检测不能误伤${xxx}这样的表达式
                        
                        //#开头的表达式
                        String elNameExpNumberSymbol = String.format("#{%s}", elName);//#{city_id}
                        //$开头的表达式
                        String elNameExpDollarSymbol = String.format("${%s}", elName);//${sql}
                        
                        if (sql.indexOf(elNameExpNumberSymbol) != -1)
                        {
                            //若能查找到#开头的表达式
                            //那么需要对其做sql注入检测
                            if (!checkSqlInject(elValue))
                            {
                                throw new SQLException(String.format("检测到sql注入！非法参数值为:%s", elValue));
                            }
                            //处理#{city_id}替换
                            sql = StringUtils.replace(sql, elNameExpNumberSymbol, String.format("'%s'", elValue));//参数一般是#{xxx}这个样子的，因此将其直接替换成实际的参数值args[i]即可
                        }
                        else if (sql.indexOf(elNameExpDollarSymbol) != -1)
                        {
                            //若能查找到$开头的表达式
                            //那么说明是sql拼接，此时就不应该做sql注入检测了！
                            
                            //处理${sql}替换
                            sql = StringUtils.replace(sql, elNameExpDollarSymbol, elValue);//参数一般是${xxx}这个样子的
                        }
                        else
                        {
                            //实际的sql语句中并没有用到该参数，因此忽略即可！
                        }
                    }
                    else
                    {
                        throw new SQLException(String.format("DAO方法：%s按照值传递时，未指定@Param注解", method.getName()));
                    }
                }
            }
        }
        return sql;
    }
    
    /**
     * 注入参数化的Sql<br>
     * 例如：select * from AAA where aa=?1 and bb=?2，那么此时参数数组需要重新组织
     * 但是也有可能直接就是select * from AAA where aa=? and bb=?，那么此时参数数组直接使用即可
     * @author nan.li
     * @param method  
     * @param args  重新组织后的参数表
     * @param sql   select * from AAA where aa=? and bb=?
     * @return
     * @throws SQLException 
     */
    public static Pair<String, Object[]> injectPreCompiledSql(Object[] args, String sql)
        throws SQLException
    {
        //一言不合先做注入检测
        for (int i = 0; i < args.length; i++)
        {
            String elValue = String.valueOf(args[i]);
            if (!checkSqlInject(elValue))
            {
                throw new SQLException(String.format("检测到sql注入！非法参数值为:%s", elValue));
            }
        }
        
        //重新组织后的参数表
        List<Object> reOrganizedparams = new ArrayList<>();
        
        //匹配 ?1 这样的字符串，取出具体的数字
        Pattern pattern = Pattern.compile("\\?(\\d)");
        Matcher matcher = pattern.matcher(sql);
        while (matcher.find())
        {
            for (int i = 1; i <= matcher.groupCount(); i++)
            {
                String number = matcher.group(i);//1
                if (Validates.isInteger(number))
                {
                    //加入参数
                    reOrganizedparams.add(args[Integer.valueOf(number) - 1]);//往前取一位
                }
                else
                {
                    break;
                }
            }
        }
        
        //如果reOrganizedparams为空，那么参数直接是原始形式：select * from AAA where aa=? and bb=?，那么此时参数数组直接使用即可
        if (Lists.isEmpty(reOrganizedparams))
        {
            //这种形式，严格依赖参数传递的顺序，需要用户自己做顺序保证
            return new ImmutablePair<String, Object[]>(sql, args);
        }
        else
        {
            sql = sql.replaceAll("\\?\\d", "\\?");
            return new ImmutablePair<String, Object[]>(sql, reOrganizedparams.toArray());
        }
    }
    
    /**
     * 获取类似mybatis类型的参数表<br>
     * 左边是#开头的表达式，右边是$开头的表达式
     * @author nan.li
     * @param sql
     * @return
     */
    public static Pair<List<String>, List<String>> getMybatisLikeParams(String sql)
    {
        MutablePair<List<String>, List<String>> pair = new MutablePair<>();
        
        //#开头的列表
        List<String> numberELList = new ArrayList<>();
        //#开头的正则
        String regEx = "#\\{([\\s\\S]*?)\\}";//  \\s\\S匹配空白与非空白符号   用正则匹配参数表  匹配： #{xxx} 参数
        //        String s = "insert into User(id, password, name) values(#{id}, #{password}, #{name})";
        Pattern pat = Pattern.compile(regEx);
        Matcher mat = pat.matcher(sql);
        while (mat.find())
        {
            for (int i = 1; i <= mat.groupCount(); i++)
            {
                numberELList.add(mat.group(i));
            }
        }
        pair.setLeft(numberELList);
        
        //$开头的列表
        List<String> dollarELList = new ArrayList<>();
        //$开头的正则
        regEx = "\\$\\{([\\s\\S]*?)\\}";//  \\s\\S匹配空白与非空白符号   用正则匹配参数表  匹配： #{xxx} 参数
        //        String s = "insert into User(id, password, name) values(#{id}, #{password}, #{name})";
        pat = Pattern.compile(regEx);
        mat = pat.matcher(sql);
        while (mat.find())
        {
            for (int i = 1; i <= mat.groupCount(); i++)
            {
                dollarELList.add(mat.group(i));
            }
        }
        pair.setRight(dollarELList);
        return pair;
    }
    
    /**
     * 注入map里面的参数表<br>
     * 逐个注入即可
     * @author nan.li
     * @param sql
     * @param params
     * @param paramMap
     * @return
     */
    public static String injectMapParams(String sql, Pair<List<String>, List<String>> pair, Map<String, Object> paramMap)
        throws SQLException
    {
        List<String> numberELList = pair.getLeft();
        List<String> dollarELList = pair.getRight();
        
        for (String elName : numberELList)
        {
            //从参数表中取出待替换的参数的值
            String elValue = ObjectUtils.toString(paramMap.get(elName));
            //#开头的表达式，需要做sql注入检测！
            if (!checkSqlInject(elValue))
            {
                throw new SQLException(String.format("检测到sql注入！非法参数值为:%s", elValue));
            }
            sql = StringUtils.replace(sql, String.format("#{%s}", elName), String.format("'%s'", elValue));
        }
        
        for (String elName : dollarELList)
        {
            //从参数表中取出待替换的参数的值
            String elValue = ObjectUtils.toString(paramMap.get(elName));
            //因为是$开头的表达式，因此是字符串拼接，不要检查sql注入！
            sql = StringUtils.replace(sql, String.format("${%s}", elName), elValue);
        }
        
        return sql;
    }
    
    /**
     * 确认是非法字符的组合，一律拦截并查杀掉<br>
     * 并不会过于严厉，以防止误杀<br>
     * 对${xxx}这种纯sql替换友好，并不会杀掉形如 ‘in('aaa','bbb','ccc')’这样的语句
     */
    static String[] invalidTokens = {";", "--", " 1=1", "' OR 1=1 --", "' or 1=1 --", "or 1=1", "OR 1=1", " union ", " UNION ", "truncate table",
        "TRUNCATE TABLE", "drop table", "DROP TABLE", "delete from", "DELETE FROM"};
        
    /**
     * sql注入的解决  14' or 1=1 or userId='14
     * 在网页中无法注入，因为键值对传递的时候这个sql参数早就被url参数分解掉
     * 主动调用的时候也会被正常拦截掉！
     * 这样就保证了足够的安全！
     * http://www.microtek.com.cn/happystudy/happystudy_info.php?idnow=4 union select 1,database(),version(),4,5,6,7,8
     */
    
    /**
     * 检测sql注入
     * @author lnwazg@126.com
     * @param value
     * @return 检测通过返回true，否则返回false
     */
    private static boolean checkSqlInject(String value)
    {
        //1. 如果含有以下非法字符，那么就是非法的
        for (String token : invalidTokens)
        {
            if (value.indexOf(token) != -1)
            {
                return false;
            }
        }
        //2.其他类型的，待扩充
        return true;
    }
    
}
