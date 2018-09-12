package com.lnwazg.dbkit.jdbc;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import com.lnwazg.dbkit.vo.BasicJdbcSupportConnectionObject;

/**
 * 连接管理器。<br>
 * 所有连接的新生与销毁都在这里进行
 * @author nan.li
 * @version 2017年5月7日
 */
public interface ConnectionManager extends BasicJdbc
{
    /**
     * 获取数据源信息
     * @author lnwazg@126.com
     * @return
     */
    DataSource getDataSource();
    
    /**
     * 获得数据库名称
     * @author nan.li
     * @return
     */
    String getSchemaName();
    
    /**
     * 获取一个来自数据源的新连接对象
     * @author lnwazg@126.com
     * @return
     * @throws SQLException 
     */
    public Connection getNewConnection()
        throws SQLException;
        
    /**
     * 执行操作，无事务
     * @author nan.li
     * @param co
     * @return
     * @throws SQLException
     */
    <T> T execute(BasicJdbcSupportConnectionObject<T> co)
        throws SQLException;
        
    /**
     * 执行操作，有事务
     * @author nan.li
     * @param co
     * @return
     * @throws SQLException
     */
    <T> T executeTransaction(BasicJdbcSupportConnectionObject<T> co)
        throws SQLException;
        
}
