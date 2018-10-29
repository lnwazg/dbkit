package com.lnwazg.dbkit.tools.dbcache.tablemap;

import java.sql.SQLException;

import com.lnwazg.dbkit.jdbc.MyJdbc;
import com.lnwazg.dbkit.tools.dbcache.tablemap.entity.DbConfig;
import com.lnwazg.kit.converter.abst.AbstractDbConverter;
import com.lnwazg.kit.singleton.B;

/**
 * 数据库的配置类<br>
 * 使用前需要保证MyJdbc已经被优先注入，然后就可以注册DBConfigHelper了<br>
 *   MyJdbc jdbc = DbKit.getJdbc(Constants.DB_CONFIG_FILE_NAME);//获取jdbc对象实例<br>
     B.s(MyJdbc.class, jdbc);//注册jdbc对象实例<br>
     B.s(DBConfigHelper.class);//注册DBConfigHelper<br>
   在任意类中使用DBConfigHelper的方法：<br>
   先声明，再使用即可：DBConfigHelper dbConfigHelper = B.g(DBConfigHelper.class);<br>     
 * @author nan.li
 * @version 2017年7月22日
 */
public class DBConfigHelper extends AbstractDbConverter
{
    /**
     * token-登录信息表
     */
    DbKeyValueTime<String> dbConfigKeyValueTimeMap = new DbKeyValueTime<String>(B.get(MyJdbc.class), DbConfig.class, String.class);
    
    @Override
    public Object convertValue(String key)
        throws SQLException
    {
        return dbConfigKeyValueTimeMap.get(key);
    }
    
    /**
     * 放入键值对
     * @author nan.li
     * @param key
     * @param value
     * @throws SQLException 
     */
    public void put(String key, String value)
        throws SQLException
    {
        dbConfigKeyValueTimeMap.put(key, value);
    }
    
    /**
     * 设置键值对
     * @author nan.li
     * @param key
     * @param value
     * @throws SQLException 
     */
    public void set(String key, String value)
        throws SQLException
    {
        put(key, value);
    }
}
