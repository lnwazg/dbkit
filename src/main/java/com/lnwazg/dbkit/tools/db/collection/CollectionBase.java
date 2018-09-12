package com.lnwazg.dbkit.tools.db.collection;

import java.sql.SQLException;

import org.apache.commons.lang.ObjectUtils;

import com.lnwazg.dbkit.jdbc.MyJdbc;
import com.lnwazg.kit.gson.GsonKit;

/**
 * 基础抽象父类
 * @author nan.li
 * @version 2017年8月5日
 */
public abstract class CollectionBase
{
    protected MyJdbc myJdbc;
    
    protected String tableName;
    
    /**
     * 将key和value的object对象转换成存储的字符串
     * @author nan.li
     * @param object
     * @return
     */
    protected String parseObject2StoreString(Object object)
    {
        if (object instanceof String)
        {
            return (String)object;
        }
        return GsonKit.parseObject2String(object);
    }
    
    /**
     * 将存储的字符串转换成对象
     * @author nan.li
     * @param json
     * @param classOfT
     * @return
     */
    protected <T> T parseStoreString2Object(String json, Class<T> classOfT)
    {
        return GsonKit.parseString2Object(json, classOfT);
    }
    
    public int size()
    {
        try
        {
            return Integer.valueOf(ObjectUtils.toString(myJdbc.findValue(String.format("select count(1) from %s where 1=1", tableName))));
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return 0;
    }
    
    public boolean isEmpty()
    {
        return size() == 0;
    }
    
    public void clear()
    {
        try
        {
            myJdbc.execute(String.format("delete from %s", tableName));
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }
}
