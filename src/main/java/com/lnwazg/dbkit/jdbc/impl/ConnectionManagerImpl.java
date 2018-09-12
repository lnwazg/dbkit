package com.lnwazg.dbkit.jdbc.impl;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import com.lnwazg.dbkit.jdbc.ConnectionManager;
import com.lnwazg.dbkit.order.OrderBy;
import com.lnwazg.dbkit.proxy.ServiceProxy;
import com.lnwazg.dbkit.resolver.ResultSetResolve;
import com.lnwazg.dbkit.resolver.TableDataResolver;
import com.lnwazg.dbkit.resolver.impl.TableDataResolverImpl;
import com.lnwazg.dbkit.utils.DbKit;
import com.lnwazg.dbkit.vo.BasicJdbcSupportConnectionObject;
import com.lnwazg.kit.log.Logs;

/**
 * 连接管理器
 * @author nan.li
 * @version 2017年5月7日
 */
public class ConnectionManagerImpl extends BasicJdbcSupport implements ConnectionManager
{
    /**
     * 数据源<br>
     * 生产Connection对象的工厂
     */
    private DataSource dataSource;
    
    /**
     * 数据库schema名称
     */
    private String schemaName;
    
    @Override
    public String getSchemaName()
    {
        return schemaName;
    }
    
    public ConnectionManagerImpl setSchemaName(String schemaName)
    {
        this.schemaName = schemaName;
        return this;
    }
    
    /**
     * 表数据解析器
     */
    private TableDataResolver customResolver;
    
    public ConnectionManagerImpl(DataSource dataSource, String schemaName)
    {
        this(dataSource);
        setSchemaName(schemaName);
    }
    
    public ConnectionManagerImpl(DataSource dataSource)
    {
        this(dataSource, new TableDataResolverImpl());
    }
    
    public ConnectionManagerImpl(DataSource dataSource, TableDataResolver customResolver)
    {
        if (dataSource == null)
        {
            throw new IllegalArgumentException("Data Source Could Not Be Null");
        }
        this.dataSource = dataSource;
        if (customResolver == null)
        {
            customResolver = new TableDataResolverImpl();
        }
        this.customResolver = customResolver;
    }
    
    public DataSource getDataSource()
    {
        return dataSource;
    }
    
    public Connection getNewConnection()
        throws SQLException
    {
        return dataSource.getConnection();
    }
    
    /**
     * 智能获取连接<br>
     * 如果线程本地对象里面有连接，那么从线程本地对象里获取；否则，获取新的<br>
     * 这一切都是为了AOP事务管理服务
     * @author lnwazg@126.com
     * @return
     * @throws SQLException 
     */
    private Connection getSmartConnection()
        throws SQLException
    {
        Connection connection = ServiceProxy.currentThreadConnectionMap.get();
        if (connection != null)
        {
            if (DbKit.DEBUG_MODE)
            {
                Logs.d("getConnection()   线程本地对象里面有连接，因此不新建，直接使用之！");
            }
            return connection;
        }
        else
        {
            if (DbKit.DEBUG_MODE)
            {
                Logs.d("getConnection()   获取新连接！");
            }
            return getNewConnection();
        }
    }
    
    /**
     * 智能关闭连接<br>
     * 如果线程本地对象里面有连接，那么本地什么都不做，会由框架自动关闭；否则，主动关闭之<br>
     * 这一切都是为了AOP事务管理服务
     * @author lnwazg@126.com
     * @param conn
     */
    private void closeSmartConnection(Connection conn)
    {
        Connection connection = ServiceProxy.currentThreadConnectionMap.get();
        if (connection != null)
        {
            if (DbKit.DEBUG_MODE)
            {
                Logs.d("close()   线程本地对象里面有连接，因此什么都不做，交由框架去关闭连接！");
            }
        }
        else
        {
            if (DbKit.DEBUG_MODE)
            {
                Logs.d("close()   主动关闭连接！");
            }
            if (conn != null)
            {
                try
                {
                    if (DbKit.DEBUG_MODE)
                    {
                        Logs.d("Close Database Connection [" + conn + "].");
                    }
                    conn.close();
                }
                catch (SQLException e)
                {
                    Logs.e("Close Connection Error Caused.", e);
                }
            }
        }
    }
    
    public <T> T execute(BasicJdbcSupportConnectionObject<T> co)
        throws SQLException
    {
        Connection conn = null;
        try
        {
            conn = getSmartConnection();
            co.connection = conn;
            co.tableDataResolver = customResolver;
            return co.run();
        }
        finally
        {
            closeSmartConnection(conn);
        }
    }
    
    public <T> T executeTransaction(BasicJdbcSupportConnectionObject<T> co)
        throws SQLException
    {
        Connection conn = null;
        try
        {
            conn = getSmartConnection();
            //By default, new connections are in auto-commit mode. 
            conn.setAutoCommit(false);//关闭事务的自动提交。因为默认获得的连接就是自动提交的
            co.connection = conn;
            co.tableDataResolver = customResolver;
            T t = co.run();
            conn.commit();
            return t;
        }
        catch (Exception e)
        {
            //错误主要定义在co.run()方法内部
            //co.run()方法抛出任何异常，程序都要回滚掉
            Logs.e("Execute Transaction Error Caused.", e);
            try
            {
                Logs.i("Rollback Transaction.");
                conn.rollback();
            }
            catch (SQLException se)
            {
                Logs.e("Execute Transaction Rollback Error Caused.", e);
            }
            throw e;
        }
        finally
        {
            closeSmartConnection(conn);
        }
    }
    
    public <T> T findValue(final String sql, final Object... values)
        throws SQLException
    {
        return execute(new BasicJdbcSupportConnectionObject<T>()
        {
            @Override
            public T run()
                throws SQLException
            {
                return findValue(sql, values);
            }
        });
    }
    
    public <T> List<T> listValue(final String sql, final Object... values)
        throws SQLException
    {
        return execute(new BasicJdbcSupportConnectionObject<List<T>>()
        {
            @Override
            public List<T> run()
                throws SQLException
            {
                return listValue(sql, values);
            }
        });
    }
    
    public <T> T query(final ResultSetResolve<T> rsr, final String strSQL, final Object... values)
        throws SQLException
    {
        return execute(new BasicJdbcSupportConnectionObject<T>()
        {
            @Override
            public T run()
                throws SQLException
            {
                return query(rsr, strSQL, values);
            }
        });
    }
    
    public boolean execute(final String sql, final Object... args)
        throws SQLException
    {
        return execute(new BasicJdbcSupportConnectionObject<Boolean>()
        {
            @Override
            public Boolean run()
                throws SQLException
            {
                return execute(sql, args);
            }
        });
    }
    
    public int executeBatch(final String sql, final int batchSize, final Collection<?>... args)
        throws SQLException
    {
        return executeTransaction(new BasicJdbcSupportConnectionObject<Integer>()
        {
            @Override
            public Integer run()
                throws SQLException
            {
                return executeBatch(sql, batchSize, args);
            }
        });
    }
    
    public int executeBatch(String sql, int batchSize, Collection<Collection<?>> args)
        throws SQLException
    {
        return executeBatch(sql, batchSize, args.toArray(new Collection<?>[0]));
    }
    
    public int insert(final String table, final Collection<String> cols, final Collection<Collection<?>> args, final int batchSize)
        throws SQLException
    {
        return execute(new BasicJdbcSupportConnectionObject<Integer>()
        {
            @Override
            public Integer run()
                throws SQLException
            {
                return insert(table, cols, args, batchSize);
            }
        });
    }
    
    public int insert(final String table, final Map<String, Object> data)
        throws SQLException
    {
        return execute(new BasicJdbcSupportConnectionObject<Integer>()
        {
            @Override
            public Integer run()
                throws SQLException
            {
                return insert(table, data);
            }
        });
    }
    
    public int update(final String sql, final Object... args)
        throws SQLException
    {
        return execute(new BasicJdbcSupportConnectionObject<Integer>()
        {
            @Override
            public Integer run()
                throws SQLException
            {
                return update(sql, args);
            }
        });
    }
    
    public int updateByParamArray(final String sql, Object[] values)
        throws SQLException
    {
        return execute(new BasicJdbcSupportConnectionObject<Integer>()
        {
            @Override
            public Integer run()
                throws SQLException
            {
                return updateByParamArray(sql, values);
            }
        });
    }
    
    public List<Map<String, Object>> listMapPage(final String sql, final Collection<OrderBy> orders, final int start, final int limit, final Object... args)
        throws SQLException
    {
        return execute(new BasicJdbcSupportConnectionObject<List<Map<String, Object>>>()
        {
            @Override
            public List<Map<String, Object>> run()
                throws SQLException
            {
                return listMapPage(sql, orders, start, limit, args);
            }
        });
    }
    
    public List<Map<String, Object>> listMap(final String sql, final Object... args)
        throws SQLException
    {
        return execute(new BasicJdbcSupportConnectionObject<List<Map<String, Object>>>()
        {
            @Override
            public List<Map<String, Object>> run()
                throws SQLException
            {
                return listMap(sql, args);
            }
        });
    }
    
    public Map<String, Object> findMap(final String sql, final Object... args)
        throws SQLException
    {
        return execute(new BasicJdbcSupportConnectionObject<Map<String, Object>>()
        {
            @Override
            public Map<String, Object> run()
                throws SQLException
            {
                return findMap(sql, args);
            }
        });
    }
    
    public <T> T findOne(final ResultSetResolve<T> ro, final String sql, final Object... args)
        throws SQLException
    {
        return execute(new BasicJdbcSupportConnectionObject<T>()
        {
            @Override
            public T run()
                throws SQLException
            {
                return findOne(ro, sql, args);
            }
        });
    }
    
    public <T> List<T> list(final ResultSetResolve<T> ro, final String sql, final Object... args)
        throws SQLException
    {
        return execute(new BasicJdbcSupportConnectionObject<List<T>>()
        {
            @Override
            public List<T> run()
                throws SQLException
            {
                return list(ro, sql, args);
            }
        });
    }
    
}
