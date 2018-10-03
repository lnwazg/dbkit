package com.lnwazg.dbkit.resolver.impl;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.lnwazg.dbkit.resolver.ResultSetResolve;
import com.lnwazg.dbkit.utils.DbKit;
import com.lnwazg.dbkit.utils.DbType;
import com.lnwazg.dbkit.utils.SqlUtils;
import com.lnwazg.kit.date.DateUtils;
import com.lnwazg.kit.reflect.ClassKit;

/**
 * 将ResultSet解析为一个对象<br>
 * 根据字段名称映射的映射工具<br>
 * 一个典型的值映射型对象<br>
 * 传入一个对象的类，该类的字段和数据库的内容完全一一对应<br>
 * 之所以要传入一个class，是因为泛型无法直接实例化，所以要借助传入的类型来接力！
 * @author nan.li
 * @version 2016年4月25日
 */
public class ResultSetToObjectMapper<T> implements ResultSetResolve<T>
{
    Class<T> tClass;
    
    public ResultSetToObjectMapper(Class<T> clazz)
    {
        this.tClass = clazz;
    }
    
    /**
     * 取值的方法，与之相对应的，是设值的方法
     * {@inheritDoc}
     * @see com.lnwazg.dbkit.resolver.impl.TableDataResolverImpl
     */
    @Override
    public T exec(ResultSet rs)
        throws SQLException
    {
        List<String> colNames = SqlUtils.getResultSetColNames(rs);//查询结果集中的列
        try
        {
            T t = tClass.newInstance();
            Field[] fields = ClassKit.getAllDeclaredFieldsParentClassFirst(tClass);
            for (Field field : fields)
            {
                field.setAccessible(true);
                String fieldName = field.getName();//字段名称，对应着数据库表的列名称
                //若当前数据库是oracle，那么返回的colNames全部是大写，必须进行兼容处理
                if (DbKit.dbType == DbType.oracle)
                {
                    fieldName = fieldName.toUpperCase();
                }
                //首先需要判断结果集中是否含有该列，若有该列，才进行设置
                if (!colNames.contains(fieldName))
                {
                    //如果该名称不在结果集内，则不映射
                    continue;
                }
                if (field.getType() == int.class || field.getType() == Integer.class)
                {
                    //the column value; if the value is SQL NULL, the value returned is 0
                    //如果数据库中该字段为null，那么查出来的会是0
                    field.set(t, rs.getInt(fieldName));
                }
                else if (field.getType() == long.class || field.getType() == Long.class)
                {
                    field.set(t, rs.getLong(fieldName));
                }
                else if (field.getType() == byte.class || field.getType() == Byte.class)
                {
                    field.set(t, rs.getByte(fieldName));
                }
                else if (field.getType() == short.class || field.getType() == Short.class)
                {
                    field.set(t, rs.getShort(fieldName));
                }
                else if (field.getType() == float.class || field.getType() == Float.class)
                {
                    field.set(t, rs.getFloat(fieldName));
                }
                else if (field.getType() == double.class || field.getType() == Double.class)
                {
                    field.set(t, rs.getDouble(fieldName));
                }
                else if (field.getType() == boolean.class || field.getType() == Boolean.class)
                {
                    field.set(t, rs.getBoolean(fieldName));
                }
                else if (field.getType() == byte[].class || field.getType() == Byte[].class)
                {
                    field.set(t, rs.getBytes(fieldName));
                }
                else if (field.getType() == BigDecimal.class)
                {
                    field.set(t, rs.getBigDecimal(fieldName));
                }
                else if (field.getType() == String.class)
                {
                    field.set(t, rs.getString(fieldName));
                }
                else if (field.getType() == Date.class)
                {
                    //作为字符串进行转换
                    String value = rs.getString(fieldName);
                    if (StringUtils.isNoneBlank(value))
                    {
                        field.set(t, DateUtils.parseStr2DateTime(value, DateUtils.DEFAULT_DATE_TIME_FORMAT_PATTERN));
                    }
                }
                else if (field.getType() == java.sql.Date.class)
                {
                    //只有日期信息
                    field.set(t, rs.getDate(fieldName));
                }
                else if (field.getType() == java.sql.Time.class)
                {
                    //只有时间信息
                    field.set(t, rs.getTime(fieldName));
                }
                else if (field.getType() == Timestamp.class)
                {
                    //包含了日期和时间信息
                    field.set(t, rs.getTimestamp(fieldName));
                }
                else
                {
                    //如果任何类型都未能匹配到，则用默认的类型去获取并设置到field内
                    field.set(t, rs.getString(fieldName));
                }
            }
            return t;
        }
        catch (InstantiationException e)
        {
            e.printStackTrace();
        }
        catch (IllegalAccessException e)
        {
            e.printStackTrace();
        }
        return null;
    }
}
