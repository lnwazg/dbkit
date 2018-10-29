package com.lnwazg.dbkit.tools.dbcache.tablemap;

import java.sql.SQLException;

import com.lnwazg.dbkit.jdbc.MyJdbc;
import com.lnwazg.dbkit.tools.dbcache.tablemap.entity.DbConfig;
import com.lnwazg.kit.converter.abst.AbstractDbConverter;
import com.lnwazg.kit.gson.GsonKit;
import com.lnwazg.kit.log.Logs;

/**
 * 数据库的配置类
 * @author nan.li
 * @version 2017年7月22日
 */
public class DBConfigHelper extends AbstractDbConverter
{
    DbKeyValueTime<String> dbConfigKeyValueTimeMap;
    
    public DBConfigHelper(MyJdbc myJdbc)
    {
        dbConfigKeyValueTimeMap = new DbKeyValueTime<String>(myJdbc, DbConfig.class, String.class);
    }
    
    @Override
    public Object convertValue(String key)
    {
        //异常在内部捕捉，降低外层使用难度，像使用普通数据一样操作DB数据
        try
        {
            return dbConfigKeyValueTimeMap.get(key);
        }
        catch (SQLException e)
        {
            Logs.e(e);
        }
        return null;
    }
    
    public void put(String key, Object value)
    {
        //异常在内部捕捉，降低外层使用难度，像使用普通数据一样操作DB数据
        String valueStr = GsonKit.gson.toJson(value);
        try
        {
            dbConfigKeyValueTimeMap.put(key, valueStr);
        }
        catch (SQLException e)
        {
            Logs.e(e);
        }
    }
    
    public void set(String key, Object value)
    {
        put(key, value);
    }
    
    public void remove(String key)
    {
        try
        {
            dbConfigKeyValueTimeMap.del(key);
        }
        catch (SQLException e)
        {
            Logs.e(e);
        }
    }
    
    public void del(String key)
    {
        remove(key);
    }
}
