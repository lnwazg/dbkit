package com.lnwazg.dbkit.proxy;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import com.lnwazg.dbkit.anno.dao.handletype.Delete;
import com.lnwazg.dbkit.anno.dao.handletype.Insert;
import com.lnwazg.dbkit.anno.dao.handletype.Select;
import com.lnwazg.dbkit.anno.dao.handletype.Update;
import com.lnwazg.dbkit.jdbc.MyJdbc;
import com.lnwazg.dbkit.jdbc.impl.ext.SqliteJdbcSupport;
import com.lnwazg.dbkit.utils.DbKit;
import com.lnwazg.dbkit.utils.SqlUtils;
import com.lnwazg.kit.log.Logs;
import com.lnwazg.kit.type.BasicType;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

/**
 * 我的DAO层的动态代理工具<br>
 * 可以根据接口的方法上的注解，自动生成代理的内容<br>
 * 利用cglib，完全可以做到深度定制一个自定义的类啊！堪称神器<br>
 * 此为mixin编程的高级应用！
 * @author nan.li
 * @version 2016年5月23日
 */
public class DaoProxy
{
    /**
     * 代理某个DAO的class，让targetClass拥有参数jdbc对象的能力
     * @author nan.li
     * @param targetClass  被代理的类或者接口
     * @param jdbc 需要执行查询的jdbc对象
     * @return
     */
    public static <T> T proxyDaoInterface(Class<T> targetClass, MyJdbc jdbc)
    {
        if (jdbc == null)
        {
            Logs.e("jdbc param should not be empty!");
            return null;
        }
        if (jdbc instanceof SqliteJdbcSupport)
        {
            Logs.i("当前使用的是SQLite数据库，写同步配置为：" + DbKit.SQLITE_SYNC_WRITE);
            //自适应对SQLite写操作是否加锁
            return proxyDaoInterface(targetClass, jdbc, DbKit.SQLITE_SYNC_WRITE);
        }
        else
        {
            //对写操作不需要加锁
            return proxyDaoInterface(targetClass, jdbc, false);
        }
    }
    
    private static final String ORDER_BY = "OrderBy";
    
    private static final String FIND_BY = "findBy";
    
    private static final String QUERY_BY = "queryBy";
    
    private static final String FIND = "find";
    
    private static final String QUERY = "query";
    
    /**
     * 代理某个DAO的class，让targetClass拥有参数jdbc对象的能力
     * @author nan.li
     * @param targetClass
     * @param jdbc
     * @param syncWrite 是否同步写入操作
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> T proxyDaoInterface(Class<T> targetClass, MyJdbc jdbc, boolean syncWrite)
    {
        if (syncWrite)
        {
            Logs.i("DaoProxy将为被代理类：" + targetClass.getName() + "的方法开启全局写同步...");
        }
        
        MethodInterceptor methodInterceptor = new MethodInterceptor()
        {
            @Override
            public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy)
                throws Throwable
            {
                //代理类里出现的异常不可以掩盖，必须全部抛给上层去处理！
                //因此任何异常都需要抛出给上层。必要的时候甚至可以不抓取任何异常！
                //因为这个代理的方法 throws Throwable
                /**
                 *  什么时候使用事务?
                 *  当你需要一次执行多条SQL语句时，可以使用事务。
                 *  通俗一点说，就是，如果这几条SQL语句全部执行成功，则才对数据库进行一次更新，如果有一条SQL语句执行失败，则这几条SQL语句全部不进行执行，这个时候需要用到事务。
                 */
                //Logs.i(String.format("Begin to invoke proxy method: %s", method.getName()));
                
                Object result = null;
                
                //如果有select操作
                if (method.isAnnotationPresent(Select.class))
                {
                    //获取select的sql语句
                    String sql = method.getAnnotation(Select.class).value();
                    
                    //sql中是否有必要填充入表名(方法名规则查询必备)
                    boolean sqlNeedFillTableName = false;
                    
                    if (StringUtils.isNotEmpty(sql))
                    {
                        //有sql，因此按有SQL的方式进行处理
                        //sql需要再度加工
                        
                        //sql中可能有问号，也可能没问号
                        //有问号，是按参数index传递。没问号的，是拼接sql
                        
                        //是否是预编译的查询
                        boolean preCompiledQuery = (sql.indexOf("?") != -1);
                        if (preCompiledQuery)
                        {
                            //如果是预编译的sql，
                            //例如：select * from AAA where aa=?1 and bb=?2
                            //或者：select * from AAA where aa=? and bb=?
                            //这种查询其实是预编译的查询方式，将会更加安全
                            //那么有必要重新组织参数形式，并且参数数组也要替换
                            //新加入的这两种查询形式，让参数化查询的灵活性得到了空前的提升！
                            Pair<String, Object[]> sqlAndArgs = SqlUtils.injectPreCompiledSql(args, sql);
                            if (sqlAndArgs != null)
                            {
                                sql = sqlAndArgs.getLeft();
                                if (DbKit.DEBUG_MODE)
                                {
                                    Logs.d("DaoProxy 即将执行的sql select语句:" + sql);
                                }
                                args = sqlAndArgs.getRight();
                                if (DbKit.DEBUG_MODE)
                                {
                                    Logs.d("参数化查询调整后的args: " + Arrays.toString(args));
                                }
                            }
                        }
                        else
                        {
                            //select * from AAA where aa=#{aa} and bb=#{bb}
                            sql = SqlUtils.injectAllParams(method, args, sql);
                            if (DbKit.DEBUG_MODE)
                            {
                                Logs.d("DaoProxy 即将执行的sql select语句:" + sql);
                            }
                        }
                    }
                    else
                    {
                        //sql为空，那么将采用方法名规则解析出对应的sql语句
                        //这种按名称解析sql逻辑的方法是一种更强大的高级DAO编程手法！
                        //借鉴了spring-data-jpa的按方法名生成查询逻辑
                        
                        String methodName = method.getName();
                        
                        String methodStart = "";
                        
                        if (StringUtils.startsWith(methodName, QUERY))
                        {
                            //QUERY ALL
                            methodStart = QUERY;
                        }
                        if (StringUtils.startsWith(methodName, FIND))
                        {
                            //QUERY ALL
                            methodStart = FIND;
                        }
                        if (StringUtils.startsWith(methodName, QUERY_BY))
                        {
                            //QUERY BY CONDITION
                            methodStart = QUERY_BY;
                        }
                        else if (StringUtils.startsWith(methodName, FIND_BY))
                        {
                            //QUERY BY CONDITION
                            methodStart = FIND_BY;
                        }
                        else
                        {
                            Logs.e("methodName:" + methodName + " 未按照方法名查询规则命名！因此无法有效地进行查询");
                            return null;
                        }
                        
                        //query
                        //find
                        //queryOrderByIdDesc
                        //queryOrderByIdDescAndNameAsc
                        //queryByNumberAndId
                        //queryByNumberAndIdOrderByIdDescAndNameAsc
                        //queryByNumberAndIdOrderByIdDesc
                        
                        String allPart = methodName.substring(methodStart.length());//参数+排序这两部分
                        //
                        //OrderByIdDesc
                        //OrderByIdDescAndNameAsc
                        //NumberAndId
                        //NumberAndIdOrderByIdDescAndNameAsc
                        
                        String paramPart = "";//参数部分                    //NumberAndId  
                        String orderByPart = null;//排序部分        //IdDescAndNameAsc
                        
                        StringBuilder paramPartSql = new StringBuilder();
                        paramPartSql.append("select * from $tableName where 1=1");
                        
                        StringBuilder orderByPartSql = new StringBuilder();
                        
                        if (StringUtils.isEmpty(allPart))
                        {
                            //do nothing
                            //直接查全表
                        }
                        else
                        {
                            if (allPart.indexOf(ORDER_BY) != -1)
                            {
                                //有orderBy部分
                                orderByPart = allPart.substring(allPart.indexOf(ORDER_BY) + ORDER_BY.length());//IdDescAndNameAsc
                                if (DbKit.DEBUG_MODE)
                                {
                                    Logs.d("orderByPart:" + orderByPart);
                                }
                                if (StringUtils.isEmpty(orderByPart))
                                {
                                    //空，啥也不做
                                }
                                else
                                {
                                    //IdDescAndNameAsc
                                    String[] orderBys = orderByPart.split("And");//["IdDesc","NameAsc"]
                                    if (orderBys != null && orderBys.length > 0)
                                    {
                                        orderByPartSql.append(" order by ");
                                        for (String orderBy : orderBys)
                                        {
                                            //要么以Desc结尾，要么以Asc结尾。若不显式指定，则默认为Asc
                                            if (StringUtils.endsWith(orderBy, "Desc"))
                                            {
                                                String fieldName = orderBy.substring(0, orderBy.lastIndexOf("Desc"));
                                                if (StringUtils.isNotEmpty(fieldName))
                                                {
                                                    orderByPartSql.append(fieldName).append(" Desc,");
                                                }
                                            }
                                            else if (StringUtils.endsWith(orderBy, "Asc"))
                                            {
                                                String fieldName = orderBy.substring(0, orderBy.lastIndexOf("Asc"));
                                                if (StringUtils.isNotEmpty(fieldName))
                                                {
                                                    orderByPartSql.append(fieldName).append(" Asc,");
                                                }
                                            }
                                            else
                                            {
                                                //既不以Desc结尾也不以Asc结尾，则默认为Asc升序排序
                                                String fieldName = orderBy;
                                                if (StringUtils.isNotEmpty(fieldName))
                                                {
                                                    orderByPartSql.append(fieldName).append(" Asc,");
                                                }
                                            }
                                        }
                                        orderByPartSql.deleteCharAt(orderByPartSql.length() - 1);//删掉最后一个orderBy后面的逗号
                                    }
                                    else
                                    {
                                        //空，啥也不做
                                    }
                                }
                                paramPart = allPart.substring(0, allPart.indexOf(ORDER_BY));
                            }
                            else
                            {
                                //无orderBy部分
                                //orderByPartSql依然为初始化状态：空
                                paramPart = allPart;
                            }
                            
                            if (DbKit.DEBUG_MODE)
                            {
                                Logs.d("paramPart:" + paramPart);
                            }
                            
                            if (StringUtils.isEmpty(paramPart))
                            {
                                //do nothing
                                //直接查全表
                            }
                            else
                            {
                                String[] paramNames = paramPart.split("And");//["Number","Id"]
                                if (paramNames != null && paramNames.length > 0)
                                {
                                    for (String paramName : paramNames)
                                    {
                                        if (StringUtils.isNotEmpty(paramName))
                                        {
                                            paramPartSql.append(" and ").append(paramName.toLowerCase()).append("=?");
                                        }
                                    }
                                }
                                else
                                {
                                    Logs.e("分割后无法解析paramNames!");
                                    return null;
                                }
                            }
                        }
                        
                        sql = paramPartSql.append(orderByPartSql).toString();
                        sqlNeedFillTableName = true;
                        
                        if (DbKit.DEBUG_MODE)
                        {
                            Logs.d("按方法名解析出的sql为:" + sql);
                        }
                    }
                    
                    //Type type = method.getGenericReturnType();//获取泛型化的返回类型
                    //ParameterizedType p = (ParameterizedType)type;//如果这是一个参数化的类型，那么就可以强制转化成参数化类型；否则，就不能强制转换成参数类型 
                    //System.out.println(p.getActualTypeArguments()[0].getTypeName());//com.lnwazg.dbkit.vo.CityInfo  获取泛型参数的类型。其实泛型参数就是一个Class
                    //System.out.println(type);//java.util.List<com.lnwazg.dbkit.vo.CityInfo>   一个参数化的类型的实现类
                    //System.out.println(type.getTypeName());//java.util.List<com.lnwazg.dbkit.vo.CityInfo>    类型名称，则是该参数化类型的全名称
                    //System.out.println(type.getClass());//class sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl 实际类型，是一个参数化类型的实现类
                    //Class<?> retType = method.getReturnType(); //获取基本的返回类型（去除泛型信息的）
                    //System.out.println(retType);//interface java.util.List
                    //System.out.println(retType.getTypeName());//java.util.List
                    //System.out.println(retType.getName());//java.util.List
                    
                    /**
                     *   getReturnType()            Class<?>        得到目标方法返回类型对应的Class对象
                     *   getGenericReturnType()     Type            得到目标方法返回类型对应的Type对象
                     */
                    //通用可描述的返回类型
                    Type genericReturnType = method.getGenericReturnType();//获取参数化的返回类型信息（里面含有参数类型信息）
                    
                    // original  type              ->  genericReturnType
                    
                    // List<CustomerUserCard>      ->  CustomerUserCard
                    // List<Map<String, Object>>   ->  Map<String, Object>
                    
                    // Map<String, Object>         ->  String、Object
                    
                    //返回的对象是参数化类型
                    if (genericReturnType instanceof ParameterizedType)
                    {
                        //java.util.List<com.lnwazg.entity.CustomerUserCard>
                        ParameterizedType parameterizedType = (ParameterizedType)genericReturnType;
                        //                        Type aaa = parameterizedType.getRawType();                //java.util.List
                        //                        Type[] bbb = parameterizedType.getActualTypeArguments();  //[class com.lnwazg.entity.CustomerUserCard]
                        //                        Type ccc = parameterizedType.getOwnerType();              //null
                        //                        Class<?> ddd = parameterizedType.getClass();              //sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl
                        //                        String fff = parameterizedType.getTypeName();             //java.util.List<com.lnwazg.entity.CustomerUserCard>
                        
                        //获取裸类型
                        Type rawType = parameterizedType.getRawType();
                        
                        //返回值： 参数化的类型
                        if (rawType == List.class)
                        {
                            // List<CustomerUserCard>      ->  CustomerUserCard
                            // List<Map<String, Object>>   ->  Map<String, Object>
                            
                            //这里已经获得了这个参数化类型的信息
                            //既然是参数化类型，那么这个类型参数也是有可能有多个的，例如SomeType<T,S,R>等等
                            Type firstTypeArgument = parameterizedType.getActualTypeArguments()[0];//那么就尝试获取其实际的类型参数，并获取其第一个类型参数
                            if (firstTypeArgument instanceof ParameterizedType)
                            {
                                //若第一个参数类型也是一个参数化的类型
                                // 例如返回类型为 ：List<Map<String, Object>> 
                                ParameterizedType pt = (ParameterizedType)firstTypeArgument;//Map<String, Object>
                                Type[] ts = pt.getActualTypeArguments();//[String, Object]
                                if (ts.length == 2)
                                {
                                    Type t0 = ts[0];//String
                                    Type t1 = ts[1];//Object
                                    if (t0 == String.class && t1 == Object.class)
                                    {
                                        result = jdbc.listMap(sql, args);
                                    }
                                }
                            }
                            else if (firstTypeArgument instanceof Class<?>)
                            {
                                //第一个类型不是一个参数化类型，那么就是一个实体类的类型
                                //List<CustomerUserCard>      ->  CustomerUserCard
                                //泛型参数肯定是一个具体的Class，所以肯定是Class的实例
                                // 例如返回类型为 ： List<Message> 
                                Class<?> paramTypeClass = (Class<?>)firstTypeArgument;//获取参数类型的实例Class
                                //List<Jyj> jyjs = jdbc.list(new ResultSetObjectFieldNameMapper<Jyj>(Jyj.class), "select * from activity_jyj");
                                //                                result = jdbc.list(new ResultSetToObjectMapper<>(paramTypeClass), sql);
                                if (sqlNeedFillTableName)
                                {
                                    sql = sql.replace("$tableName", paramTypeClass.getSimpleName());
                                    if (DbKit.DEBUG_MODE)
                                    {
                                        Logs.d("方法名规则查询完整的sql语句为:" + sql);
                                    }
                                }
                                
                                result = jdbc.list(paramTypeClass, sql, args);
                            }
                        }
                        else if (rawType == Map.class)
                        {
                            // Map<String, Object>    
                            Type t0 = parameterizedType.getActualTypeArguments()[0];
                            Type t1 = parameterizedType.getActualTypeArguments()[1];
                            if (t0 == String.class && t1 == Object.class)
                            {
                                result = jdbc.findMap(sql, args);
                            }
                        }
                    }
                    else
                    {
                        //返回的对象非参数化类型
                        
                        //若返回的对象是某一个具体裸类型的Class
                        Class<?> clazz = (Class<?>)genericReturnType;
                        
                        //如果是常见的可枚举类型
                        //                        if (rawTypeClass == int.class || rawTypeClass == Integer.class ||
                        //                            rawTypeClass == long.class || rawTypeClass == Long.class ||
                        //                            rawTypeClass == double.class || rawTypeClass == Double.class ||
                        //                            rawTypeClass == float.class || rawTypeClass == Float.class ||
                        //                            rawTypeClass == short.class || rawTypeClass == Short.class ||
                        //                            rawTypeClass == String.class)
                        //                        {
                        //                        }
                        
                        //如果是基本类型的原生类型、基本类型的包装类型、String，那么直接findValue()求值
                        if (BasicType.primitiveSet.contains(clazz) ||
                            BasicType.wrapperSet.contains(clazz) ||
                            clazz == String.class)
                        {
                            //这些一般是求值用的。主要用于count()、sum()等场景
                            Object queryResultObj = jdbc.findValue(sql, args);
                            //若查询结果就是null，那么就直接返回null
                            if (queryResultObj == null)
                            {
                                result = null;
                            }
                            else
                            {
                                //尝试将查询结果先转换为String，这样是为了对返回的对象进行平滑转换，防止出现Integer强转成Long而导致的报错等，最大化地兼容
                                String queryResult = ObjectUtils.toString(queryResultObj);
                                
                                if (clazz == int.class || clazz == Integer.class)
                                {
                                    result = Integer.valueOf(queryResult);
                                }
                                else if (clazz == long.class || clazz == Long.class)
                                {
                                    result = Long.valueOf(queryResult);
                                }
                                else if (clazz == double.class || clazz == Double.class)
                                {
                                    result = Double.valueOf(queryResult);
                                }
                                else if (clazz == float.class || clazz == Float.class)
                                {
                                    result = Float.valueOf(queryResult);
                                }
                                else if (clazz == short.class || clazz == Short.class)
                                {
                                    result = Short.valueOf(queryResult);
                                }
                                //还有char、byte、boolean三种类型没有加判断！
                                else if (clazz == String.class)
                                {
                                    result = queryResult;
                                }
                            }
                        }
                        else
                        {
                            //否则，不可枚举，那么是一个自定义的JavaBean
                            //否则，就是一个具体的JavaBean，将其直接作Bean映射即可
                            //result = jdbc.findOne(new ResultSetToObjectMapper<>(rawTypeClass), sql);
                            if (sqlNeedFillTableName)
                            {
                                sql = sql.replace("$tableName", clazz.getSimpleName());
                                if (DbKit.DEBUG_MODE)
                                {
                                    Logs.d("方法名规则查询完整的sql语句为:" + sql);
                                }
                                
                            }
                            result = jdbc.findOne(clazz, sql, args);
                        }
                    }
                }
                else if (method.isAnnotationPresent(Insert.class))
                {
                    //待实现
                    if (syncWrite)
                    {
                        synchronized (DaoProxy.class)
                        {
                            Logs.i("对SQLite加了@Insert的写操作加同步锁...写操作的方法为:" + method.getName());
                            //result = jdbc.update(sql, args);
                        }
                    }
                    else
                    {
                        //result = jdbc.update(sql, args);
                    }
                }
                else if (method.isAnnotationPresent(Delete.class))
                {
                    //待实现
                    if (syncWrite)
                    {
                        synchronized (DaoProxy.class)
                        {
                            Logs.i("对SQLite加了@Delete的写操作加同步锁...写操作的方法为:" + method.getName());
                            //result = jdbc.update(sql, args);
                        }
                    }
                    else
                    {
                        //result = jdbc.update(sql, args);
                    }
                }
                else if (method.isAnnotationPresent(Update.class))
                {
                    String sql = method.getAnnotation(Update.class).value();//获取select的sql语句
                    sql = SqlUtils.injectAllParams(method, args, sql);
                    if (DbKit.DEBUG_MODE)
                    {
                        Logs.d("DaoProxy 即将执行的sql update语句:" + sql);
                    }
                    if (syncWrite)
                    {
                        synchronized (DaoProxy.class)
                        {
                            Logs.i("对SQLite加了@Update的写操作加同步锁...写操作的方法为:" + method.getName());
                            result = jdbc.update(sql, args);
                        }
                    }
                    else
                    {
                        result = jdbc.update(sql, args);
                    }
                }
                else
                {
                    //非注解类型，则要调用的MyJdbc对象的方法
                    //所以，只要是调用非MyJdbc对象的方法，都应当加上注解！
                    
                    //是否应该对没加注解的DAO（也就是MyJdbc接口的自有方法）加上同步操作
                    boolean shouldSyncNonAnnoMethod = false;
                    //若需同步写
                    if (syncWrite)
                    {
                        //判定当前操作是否是写操作
                        boolean isWriteOperation = judgeWriteOperation(method.getName());
                        shouldSyncNonAnnoMethod = isWriteOperation;
                    }
                    if (shouldSyncNonAnnoMethod)
                    {
                        //若是写操作，那么得加锁访问原方法
                        synchronized (DaoProxy.class)
                        {
                            Logs.i("对SQLite写操作加同步锁...写操作的方法为:" + method.getName());
                            result = method.invoke(jdbc, args);
                        }
                    }
                    else
                    {
                        //不是写操作，那么普通访问原方法
                        result = method.invoke(jdbc, args);
                    }
                }
                return result;
            }
        };
        
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(targetClass);//设置动态代理的父类信息
        // 回调方法
        enhancer.setCallback(methodInterceptor);//设置方法过滤器
        // 创建代理对象
        return (T)enhancer.create();
    }
    
    /**
     * 查询操作的方法名前缀数组
     */
    static String[] selectOpPrefixs = {"checkTableExists", "load", "find", "count", "list", "query", "contains",
        "toString", "getClass", "hashCode", "equals", "clone", "notify", "wait", "finalize"};
    
    /**
     * 判断MyJdbc中的某个方法是否是写入的操作
     * @author nan.li
     * @param name
     * @return
     */
    protected static boolean judgeWriteOperation(String methodName)
    {
        //是否select操作的判定结果
        boolean isSelectOps = false;
        for (String opPrefix : selectOpPrefixs)
        {
            if (methodName.startsWith(opPrefix))
            {
                isSelectOps = true;
                break;
            }
        }
        boolean finalResult = !isSelectOps;
//        Logs.i("judgeWriteOperation methodName=" + methodName + " result=" + finalResult);
        return finalResult;
    }
    
    /**
     * 测试正则匹配
     * @author nan.li
     * @param args
     */
    public static void main(String[] args)
    {
        //String aString = "insert into User(id, password, name) values(#{id}, #{password}, #{name})";
        //String regEx = "<a>([\\s\\S]*?)</a>";//匹配空白与非空白符号
        //String s = "<a>123</a><a>456</a><a>789</a>";
        String regEx = "#\\{([\\s\\S]*?)\\}";//  \\s\\S匹配空白与非空白符号   用正则匹配参数表
        String s = "insert into User(id, password, name) values(#{id}, #{password}, #{name})";
        Pattern pat = Pattern.compile(regEx);
        Matcher mat = pat.matcher(s);
        while (mat.find())
        {
            for (int i = 1; i <= mat.groupCount(); i++)
            {
                System.out.println(mat.group(i));
            }
        }
    }
}
