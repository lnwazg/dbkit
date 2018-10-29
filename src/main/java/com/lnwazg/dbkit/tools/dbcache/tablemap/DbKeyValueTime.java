package com.lnwazg.dbkit.tools.dbcache.tablemap;

import java.sql.SQLException;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;

import com.lnwazg.dbkit.jdbc.MyJdbc;
import com.lnwazg.dbkit.tools.dbcache.tablemap.entity.KeyValueTimeTable;
import com.lnwazg.dbkit.utils.TableUtils;
import com.lnwazg.kit.gson.GsonKit;
import com.lnwazg.kit.reflect.ClassKit;

/**
 * 数据库Map，将数据库表当做FileCache来使用<br>
 * V为valueType
 * V为存放的value的class type<br>
 * @author nan.li
 * @version 2017年4月8日
 */
public class DbKeyValueTime<V>
{
    /**
     * 具体的数据库表对应的Java类
     */
    private Class<? extends KeyValueTimeTable> tableClazz;
    
    /**
     * myJdbc的引用
     */
    private MyJdbc myJdbc;
    
    /**
     * 存入的值的类型
     */
    private Class<V> valueClazz;
    
    /**
     * 构造函数 
     * @param myJdbc
     * @param clazz
     * @param valueClazz 此处传入value的class并保存下来，就可以在后续不用再传value的class
     */
    public DbKeyValueTime(MyJdbc myJdbc, Class<? extends KeyValueTimeTable> clazz, Class<V> valueClazz)
    {
        this.myJdbc = myJdbc;
        this.tableClazz = clazz;
        this.valueClazz = valueClazz;
        try
        {
            //（如果表不存在，则）自动建表
            myJdbc.createTable(tableClazz);
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }
    
    /**
     * 将任意对象存到表里面<br>
     * 对象将自动被序列化为Json形式
     * @author nan.li
     * @param key
     * @param value
     * @throws SQLException 
     */
    public void put(String key, V value)
        throws SQLException
    {
        //统一转为json形式储存，这样就可以无差别操作
        String valueStr = GsonKit.gson.toJson(value);
        KeyValueTimeTable keyValueTimeTable = myJdbc.findOneNoSql(tableClazz, "strKey", key);
        if (keyValueTimeTable == null)
        {
            //该键值对不存在，则直接插入即可
            keyValueTimeTable = ClassKit.newInstance(tableClazz);
            keyValueTimeTable.setStrKey(key);
            keyValueTimeTable.setStrValue(valueStr);
            keyValueTimeTable.setUpdateTime(new Date());
            myJdbc.insert(keyValueTimeTable);
        }
        else
        {
            //该键值对已经存在，则更新之
            keyValueTimeTable.setStrValue(valueStr);
            keyValueTimeTable.setUpdateTime(new Date());
            myJdbc.updateEntity(keyValueTimeTable);
        }
    }
    
    /**
     * 从表里面取出数据，并且可以指定缓存失效时间。取出的数据自动转换为初始化DbMap时指定的泛型类型<br>
     * 实质上是将这个数据表当作缓存表来使用，所以可以指定缓存的失效时间<br>
     * @author nan.li
     * @param key
     * @param failTime
     * @param timeUnit
     * @return
     * @throws SQLException
     */
    public V get(String key, int failTime, TimeUnit timeUnit)
        throws SQLException
    {
        // 如果缓存表中压根就没有，那么忽略之
        KeyValueTimeTable keyValueTimeTable = myJdbc.findOneNoSql(tableClazz, "strKey", key);
        if (null != keyValueTimeTable)
        {
            // 当前时间
            long now = System.currentTimeMillis();
            // 放入的时间
            long putTime = keyValueTimeTable.getUpdateTime().getTime();
            // 时间差
            long deltaTimeInMills = now - putTime; // 时间差
            // 超过如下毫秒数则认为缓存已经过期
            long failTimeInMills = 0;
            switch (timeUnit)
            {
                case DAYS:
                    failTimeInMills = failTime * 24 * 60 * 60 * 1000;
                    break;
                case HOURS:
                    failTimeInMills = failTime * 60 * 60 * 1000;
                    break;
                case MINUTES:
                    failTimeInMills = failTime * 60 * 1000;
                    break;
                case SECONDS:
                    failTimeInMills = failTime * 1000;
                    break;
                default:
                    break;
            }
            if (deltaTimeInMills > failTimeInMills)
            {
                return null;
            }
            else
            {
                String value = keyValueTimeTable.getStrValue();
                if (StringUtils.isNotEmpty(value))
                {
                    return GsonKit.gson.fromJson(value, valueClazz);
                }
            }
        }
        return null;
    }
    
    /**
     * 从表里面取出数据，取出的数据自动转换为初始化DbMap时指定的泛型类型
     * @author nan.li
     * @param key
     * @return
     * @throws SQLException
     */
    public V get(String key)
        throws SQLException
    {
        KeyValueTimeTable keyValueTimeTable = myJdbc.findOneNoSql(tableClazz, "strKey", key);
        if (null != keyValueTimeTable)
        {
            String value = keyValueTimeTable.getStrValue();
            if (StringUtils.isNotEmpty(value))
            {
                return GsonKit.gson.fromJson(value, valueClazz);
            }
        }
        return null;
    }
    
    /**
     * 从数据库缓存表中删除某个键值对
     * @author nan.li
     * @param key
     * @throws SQLException
     */
    public void del(String key)
        throws SQLException
    {
        myJdbc.update(String.format("delete from %s where strKey='%s'", TableUtils.getTableName(tableClazz), key));
    }
}
