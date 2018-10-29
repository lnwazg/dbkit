package com.lnwazg.dbkit.utils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import com.lnwazg.dbkit.anno.entity.AutoIncrement;
import com.lnwazg.dbkit.anno.entity.FK;
import com.lnwazg.dbkit.anno.entity.Id;
import com.lnwazg.dbkit.anno.entity.Table;
import com.lnwazg.dbkit.anno.field.ParentClassFieldsFirst;
import com.lnwazg.dbkit.anno.field.SubClassFieldsFirst;
import com.lnwazg.dbkit.vo.BatchObj;
import com.lnwazg.kit.reflect.ClassKit;

/**
 * sql参数处理工具类
 * @author nan.li
 * @version 2016年5月20日
 */
@SuppressWarnings("deprecation")
public class TableUtils
{
    /**
     * 根据表格的映射类，去获得表名
     * @author Administrator
     * @param tableClazz
     * @return
     */
    public static String getTableName(Class<?> tableClazz)
    {
        String tableName = tableClazz.getSimpleName();
        //将表名称跟类名称保持一致，而非改成全大写的形式
        //反正表名称不区分大小写，所以写入的时候区分大小写反而增加了可读性
        if (tableClazz.isAnnotationPresent(Table.class))
        {
            Table table = tableClazz.getAnnotation(Table.class);
            String tableValue = table.value();
            if (StringUtils.isNotEmpty(tableValue))
            {
                tableName = tableValue;
            }
        }
        return tableName;
    }
    
    /**
     * 根据表格的映射类，去获得表名
     * @author nan.li
     * @param entity
     * @return
     */
    public static String getTableName(Object entity)
    {
        return getTableName(entity.getClass());
    }
    
    /**
     * 将实体类对象转换成map
     * @author Administrator
     * @param entity
     * @return
     */
    public static Map<String, Object> obj2Map(Object entity)
    {
        Map<String, Object> columnDataMap = new LinkedHashMap<>();
        Field[] fields = ClassKit.getAllDeclaredFieldsParentClassFirst(entity);
        for (Field field : fields)
        {
            field.setAccessible(true);
            try
            {
                columnDataMap.put(field.getName(), field.get(entity));
            }
            catch (IllegalArgumentException e)
            {
                e.printStackTrace();
            }
            catch (IllegalAccessException e)
            {
                e.printStackTrace();
            }
        }
        return columnDataMap;
    }
    
    /**
     * 获取所有字段的map
     * @author nan.li
     * @param entity
     * @return
     */
    public static Map<String, Object> getAllFieldsMap(Object entity)
    {
        return obj2Map(entity);
    }
    
    /**
     * 获得Id字段的map，通用的方法
     * @author nan.li
     * @param entity
     * @return
     */
    public static Map<String, Object> getIdFieldMap(Object entity)
    {
        Map<String, Object> map = new LinkedHashMap<>();
        //        Field[] fields = entity.getClass().getDeclaredFields();
        Field[] fields = ClassKit.getAllDeclaredFieldsParentClassFirst(entity);
        for (Field field : fields)
        {
            if (field.isAnnotationPresent(Id.class))
            {
                field.setAccessible(true);
                try
                {
                    map.put(field.getName(), field.get(entity));
                }
                catch (IllegalArgumentException | IllegalAccessException e)
                {
                    e.printStackTrace();
                }
            }
        }
        return map;
    }
    
    /**
     * 获取Id参数和值的字符串<br>
     * 例如: 1=1 and id = 123 and id2 = 456
     * @author nan.li
     * @param entity
     * @return
     */
    public static String getIdParamsValuesFragment(Object entity)
    {
        StringBuilder sBuilder = new StringBuilder("1=1");
        Map<String, Object> map = getIdFieldMap(entity);
        for (String key : map.keySet())
        {
            sBuilder.append(String.format(" and %s = '%s'", key, map.get(key)));
        }
        return sBuilder.toString();
    }
    
    /**
     * 获取非Id字段的map
     * @author nan.li
     * @param entity
     * @return
     */
    public static Map<String, Object> getNonIdFieldsMap(Object entity)
    {
        Map<String, Object> map = new LinkedHashMap<>();
        //        Field[] fields = entity.getClass().getDeclaredFields();
        Field[] fields = ClassKit.getAllDeclaredFieldsParentClassFirst(entity);
        for (Field field : fields)
        {
            if (!field.isAnnotationPresent(Id.class))
            {
                field.setAccessible(true);
                try
                {
                    map.put(field.getName(), field.get(entity));
                }
                catch (IllegalArgumentException | IllegalAccessException e)
                {
                    e.printStackTrace();
                }
            }
        }
        return map;
    }
    
    /**
     * 获取更新语句中，以逗号相连接的sql语句
     * @author nan.li
     * @param entity
     * @return
     */
    public static String getUpdateJoinSql(Object entity)
    {
        Map<String, Object> map = getNonIdFieldsMap(entity);
        List<String> sentences = new ArrayList<>();
        for (Map.Entry<String, Object> entry : map.entrySet())
        {
            sentences.add(String.format("%s='%s'", entry.getKey(), ObjectUtils.toString(entry.getValue())));
        }
        return StringUtils.join(sentences, ",");
    }
    
    /**
     * 根据实体对象，获取更新语句的拼接参数sql
     * @author nan.li
     * @param entity
     * @return
     */
    public static String getUpdateJoinSqlForPreparedStatement(Object entity)
    {
        Map<String, Object> map = getNonIdFieldsMap(entity);
        List<String> sentences = new ArrayList<>();
        for (Map.Entry<String, Object> entry : map.entrySet())
        {
            sentences.add(String.format("%s=?", entry.getKey()));
        }
        return StringUtils.join(sentences, ",");
    }
    
    /**
     * 根据实体对象，获取更新语句的参数数组
     * @author nan.li
     * @param entity
     * @return
     */
    public static Object[] getUpdateParamsForPreparedStatement(Object entity)
    {
        Map<String, Object> map = getNonIdFieldsMap(entity);
        Object[] ret = new Object[map.size()];
        int i = 0;
        for (Map.Entry<String, Object> entry : map.entrySet())
        {
            ret[i++] = entry.getValue();
        }
        return ret;
    }
    
    /**
    * 获取批量提交的对象
    * @author nan.li
    * @param entities
    * @return
    */
    public static BatchObj getBatchObj(List<?> entities)
    {
        //      Collection<String> cols = Arrays.asList("id", "name", "gender", "date_created");
        //      Collection<Collection<?>> args = Arrays.<Collection<?>> asList(Arrays.<Object> asList("104", "Xuir", "F", new Date()),
        //          Arrays.<Object> asList("105", "Sorina Nyco", "F", new Date()),
        //          Arrays.<Object> asList("106", "Gemily", "F", new Date()),
        //          Arrays.<Object> asList("107", "Luffy", "M", new Date()),
        //          Arrays.<Object> asList("108", "Zoro", "M", new Date()),
        //          Arrays.<Object> asList("109", "Bruck", "M", new Date()));
        Object entity = entities.get(0);
        Collection<String> cols = ClassKit.getFields(entity);
        Collection<Collection<?>> args = ClassKit.getBatchArgs(cols, entities);
        return new BatchObj(cols, args);
    }
    
    /**
     * 获取主键列的名称，假设主键只有一列<br>
     * 该方法作用与getIdColumnName()完全相同
     * @param entity
     * @return
     */
    public static String getTableId(Object entity)
    {
        return getTableId(entity.getClass());
    }
    
    /**
     * 获取主键列的名称，假设主键只有一列
     * @author nan.li
     * @param tableClazz
     * @return
     */
    public static String getTableId(Class<?> tableClazz)
    {
        return getIdColumnName(tableClazz);
    }
    
    /**
     * 获取主键列的名称，假设主键只有一列
     * @author nan.li
     * @param entity
     * @return
     */
    public static String getIdColumnName(Object entity)
    {
        return getIdColumnName(entity.getClass());
    }
    
    /**
     * 获取主键列的名称，假设主键只有一列
     * @author Administrator
     * @param tableClazz
     * @return
     */
    public static String getIdColumnName(Class<?> tableClazz)
    {
        String ret = "id";//默认值就叫做id
        Field[] fields = ClassKit.getAllDeclaredFieldsParentClassFirst(tableClazz);
        for (Field field : fields)
        {
            if (field.isAnnotationPresent(Id.class))
            {
                ret = field.getName();
                break;
            }
        }
        return ret;
    }
    
    /**
     * 获取主键列列表
     * @author nan.li
     * @param entity
     * @return
     */
    public static List<String> getIdColumnNames(Object entity)
    {
        return getIdColumnNames(entity.getClass());
    }
    
    /**
     * 获取主键列列表
     * @author nan.li
     * @param tableClazz
     * @return
     */
    public static List<String> getIdColumnNames(Class<?> tableClazz)
    {
        List<String> list = new ArrayList<>();
        Field[] fields = ClassKit.getAllDeclaredFieldsParentClassFirst(tableClazz);
        for (Field field : fields)
        {
            if (field.isAnnotationPresent(Id.class))
            {
                list.add(field.getName());
            }
        }
        return list;
    }
    
    /**
     * 获取自增长字段名
     * @author nan.li
     * @param entity
     * @return
     */
    public static String getAutoIncrementColumnName(Object entity)
    {
        return getAutoIncrementColumnName(entity.getClass());
    }
    
    /**
     * 获取自增长字段名
     * @author nan.li
     * @param tableClazz
     * @return
     */
    public static String getAutoIncrementColumnName(Class<?> tableClazz)
    {
        Field[] fields = ClassKit.getAllDeclaredFieldsParentClassFirst(tableClazz);
        for (Field field : fields)
        {
            if (field.isAnnotationPresent(AutoIncrement.class))
            {
                return field.getName();
            }
        }
        return null;
    }
    
    public static List<String> getAutoIncrementColumnNames(Object entity)
    {
        return getAutoIncrementColumnNames(entity.getClass());
    }
    
    /**
     * 获取自增长字段名
     * @author nan.li
     * @param tableClazz
     * @return
     */
    public static List<String> getAutoIncrementColumnNames(Class<?> tableClazz)
    {
        List<String> list = new ArrayList<>();
        Field[] fields = ClassKit.getAllDeclaredFieldsParentClassFirst(tableClazz);
        for (Field field : fields)
        {
            if (field.isAnnotationPresent(AutoIncrement.class))
            {
                list.add(field.getName());
            }
        }
        return list;
    }
    
    /**
     * 获取外键列的名称
     * @author nan.li
     * @param entity
     * @return
     */
    public static String getFKColumnName(Object entity)
    {
        return getFKColumnName(entity.getClass());
    }
    
    /**
     * 获取外键列的名称
     * @author nan.li
     * @param fkClass
     * @return
     */
    public static String getFKColumnName(Class<?> tableClazz)
    {
        Field[] fields = ClassKit.getAllDeclaredFieldsParentClassFirst(tableClazz);
        for (Field field : fields)
        {
            if (field.isAnnotationPresent(FK.class))
            {
                return field.getName();
            }
        }
        return null;
    }
    
    public static List<String> getFKColumnNames(Object entity)
    {
        return getFKColumnNames(entity.getClass());
    }
    
    public static List<String> getFKColumnNames(Class<?> tableClazz)
    {
        List<String> list = new ArrayList<>();
        Field[] fields = ClassKit.getAllDeclaredFieldsParentClassFirst(tableClazz);
        for (Field field : fields)
        {
            if (field.isAnnotationPresent(FK.class))
            {
                list.add(field.getName());
            }
        }
        return list;
    }
    
    /**
     * 获取日期类型的字段列表(Oracle专用)
     * @author nan.li
     * @param tableClazz
     * @return
     */
    public static List<String> getDateTypeFields(Class<?> tableClazz)
    {
        List<String> list = new ArrayList<>();
        Field[] fields = ClassKit.getAllDeclaredFieldsParentClassFirst(tableClazz);
        for (Field field : fields)
        {
            if (field.getType() == Date.class)
            {
                list.add(field.getName());
            }
        }
        return list;
    }
    
    /**
     * 获取主键列的值，假设主键只有一列
     * @author Administrator
     * @param entity
     * @return
     */
    public static Object getIdColumnValue(Object entity)
    {
        Object ret = "";
        //        Field[] fields = entity.getClass().getDeclaredFields();
        Field[] fields = ClassKit.getAllDeclaredFieldsParentClassFirst(entity);
        for (Field field : fields)
        {
            field.setAccessible(true);
            if (field.isAnnotationPresent(Id.class))
            {
                try
                {
                    ret = field.get(entity);
                }
                catch (IllegalArgumentException e)
                {
                    e.printStackTrace();
                }
                catch (IllegalAccessException e)
                {
                    e.printStackTrace();
                }
                break;
            }
        }
        return ret;
    }
    
    /**
     * 拼接并获取分页查询的sql<br>
     * 目前仅考虑了mysql、sqlite系列的语法，并不支持Oracle！
     * @author nan.li
     * @param sql
     * @param start
     * @param limit
     * @return
     */
    public static String getPagingSql(String sql, int start, int limit)
    {
        return String.format("select * from (%s) _innerT limit %d,%d", sql, start, limit);
    }
    
    /**
     * 是否子类的字段的顺序优先<br>
     * 这个属性决定了最终生成的表结构的字段顺序
     * @param tableClazz
     * @return
     */
    public static boolean isSubClassFieldsFirst(Class<?> tableClazz)
    {
        boolean isSubClassFieldsFirst = true;
        //遍历字段，根据字段去建表
        Class<?> clazz = ClassKit.getTopParentClass(tableClazz);
        if (clazz.isAnnotationPresent(SubClassFieldsFirst.class))
        {
            //子类优先
            isSubClassFieldsFirst = true;
        }
        else if (clazz.isAnnotationPresent(ParentClassFieldsFirst.class))
        {
            //父类优先
            isSubClassFieldsFirst = false;
        }
        else
        {
            isSubClassFieldsFirst = true;
        }
        return isSubClassFieldsFirst;
    }
}
