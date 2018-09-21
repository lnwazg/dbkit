package com.lnwazg.dbkit.jdbc.impl;

import java.lang.reflect.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.ObjectUtils;
import org.slf4j.helpers.MessageFormatter;

import com.lnwazg.dbkit.jdbc.BasicJdbc;
import com.lnwazg.dbkit.order.OrderBy;
import com.lnwazg.dbkit.pagination.PaginationProcessor;
import com.lnwazg.dbkit.resolver.ResultSetResolve;
import com.lnwazg.dbkit.resolver.TableDataResolver;
import com.lnwazg.dbkit.resolver.impl.ResultSetToObjectMapper;
import com.lnwazg.dbkit.tools.AllDbExecInfo;
import com.lnwazg.dbkit.tools.sqlmonitor.SQLMonitor;
import com.lnwazg.dbkit.utils.DbKit;
import com.lnwazg.dbkit.utils.StringUtils;
import com.lnwazg.dbkit.utils.TableUtils;
import com.lnwazg.dbkit.vo.BatchObj;
import com.lnwazg.dbkit.vo.SqlAndArgs;
import com.lnwazg.dbkit.vo.StatementObject;
import com.lnwazg.kit.list.Lists;
import com.lnwazg.kit.log.Logs;

/**
 * 基本API的实现
 * @author nan.li
 * @version 2017年5月3日
 */
public class BasicJdbcSupport implements BasicJdbc
{
    /**
     * 数据库连接对象<br>
     * BasicJdbcSupport实例里面的所有操作数据库的方法都依赖该conn成员对象<br>
     * 该对象由NewbieJdbcSupport管理，当需要新查询的时候，NewbieJdbcSupport获取一个连接对象，并注入到BasicJdbcSupport对象里
     */
    public Connection connection;
    
    /**
     * 自定义的数据库与实体类映射器对象<br>
     * 通常该对象为空，因为dbkit约定大于配置，直接用反射技术来作结果集数据映射（也就是说：直接使用Java字段名作为数据库表字段名，且两者完全保持一致）
     */
    public TableDataResolver tableDataResolver;
    
    public BasicJdbcSupport()
    {
    }
    
    public BasicJdbcSupport(Connection conn, TableDataResolver tableDataResolver)
    {
        this.connection = conn;
        this.tableDataResolver = tableDataResolver;
    }
    
    public <T> T load(Class<T> clazz, Object id)
        throws SQLException
    {
        return findOne(clazz,
            String.format("select * from %s where %s=%s", TableUtils.getTableName(clazz), TableUtils.getTableId(clazz), ObjectUtils.toString(id)));
    }
    
    public <T> T findOne(final ResultSetResolve<T> ro, final String sql, Object... values)
        throws SQLException
    {
        return query(new ResultSetResolve<T>()
        {
            public T exec(ResultSet rs)
                throws SQLException
            {
                T result = null;
                if (rs.next())
                {
                    result = ro.exec(rs);
                }
                if (rs.next())
                {
                    throw new IllegalStateException(
                        fmt("Find One By SQL [{}] Expected One Result (Or NULL) To Be Returned, " + "But Found More Than One", sql));
                }
                return result;
            }
        }, sql, values);
    }
    
    @Override
    public <T> T findOne(Class<T> clazz, String sql, Object... args)
        throws SQLException
    {
        return findOne(new ResultSetToObjectMapper<T>(clazz), sql, args);
    }
    
    @Override
    public <T> T findForeignOne(Object idObj, Class<T> fkClass)
        throws SQLException
    {
        Object idValue = TableUtils.getIdColumnValue(idObj);
        if (idValue == null)
        {
            throw new SQLException("参数" + idObj + "的id字段不能为空！");
        }
        String fkColumnName = TableUtils.getFKColumnName(fkClass);
        if (org.apache.commons.lang3.StringUtils.isEmpty(fkColumnName))
        {
            throw new SQLException("参数" + fkClass + "中未定义外键列，因此无法关联查询！请指定一个用@FK注解的字段！");
        }
        return findOneNoSql(fkClass, fkColumnName, idValue);
    }
    
    @Override
    public <T> T findOneNoSql(Class<T> clazz, Object... keyValues)
        throws SQLException
    {
        if (keyValues.length % 2 != 0)
        {
            throw new SQLException("keyValues参数个数必须是2的整数倍！");
        }
        String[] keys = new String[keyValues.length / 2];//0、1、2、3、...
        Object[] values = new String[keyValues.length / 2];
        if (keys.length > 0)
        {
            for (int i = 0; i < keys.length; i++)
            {
                keys[i] = ObjectUtils.toString(keyValues[i * 2]);
                values[i] = keyValues[i * 2 + 1];
            }
        }
        String basicSql = String.format("select * from %s where 1=1", TableUtils.getTableName(clazz));
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append(basicSql);
        if (keys.length > 0)
        {
            for (String key : keys)
            {
                sqlBuilder.append(String.format(" and %s=?", key));
            }
        }
        return findOne(clazz, sqlBuilder.toString(), values);
    }
    
    public Map<String, Object> findMap(String sql, Object... values)
        throws SQLException
    {
        return findOne(new ResultSetResolve<Map<String, Object>>()
        {
            public Map<String, Object> exec(ResultSet rs)
                throws SQLException
            {
                return readMap(rs);
            }
        }, sql, values);
    }
    
    public <T> T findValue(final String sql, Object... values)
        throws SQLException
    {
        return findOne(new ResultSetResolve<T>()
        {
            @SuppressWarnings("unchecked")
            public T exec(ResultSet rs)
                throws SQLException
            {
                if (rs.getMetaData().getColumnCount() > 1)
                {
                    throw new IllegalStateException(fmt("Find Value By SQL [{}] Expected One Column To Be Returned, " + "But Found More Than One", sql));
                }
                return (T)tableDataResolver.readValue(rs, 1);
            }
        }, sql, values);
    }
    
    public long findValueToLong(String sql, Object... args)
        throws SQLException
    {
        return Long.valueOf(ObjectUtils.toString(findValue(sql, args)));
    }
    
    public int findValueToInt(String sql, Object... args)
        throws SQLException
    {
        return Integer.valueOf(ObjectUtils.toString(findValue(sql, args)));
    }
    
    public int count(String sql, Object... args)
        throws SQLException
    {
        return findValueToInt(sql, args);
    }
    
    public <T> List<T> listValue(final String sql, Object... values)
        throws SQLException
    {
        return list(new ResultSetResolve<T>()
        {
            @SuppressWarnings("unchecked")
            public T exec(ResultSet rs)
                throws SQLException
            {
                if (rs.getMetaData().getColumnCount() > 1)
                {
                    //查询单列的值的列表，因此限定了只能返回一列值
                    throw new IllegalStateException(fmt("List Values By SQL [{}] Expected One Column To Be Returned, " + "But Found More Than One", sql));
                }
                return (T)tableDataResolver.readValue(rs, 1);
            }
        }, sql, values);
    }
    
    @Override
    public <T> List<T> listForeignList(Object idObj, Class<T> fkClass)
        throws SQLException
    {
        Object idValue = TableUtils.getIdColumnValue(idObj);
        if (idValue == null)
        {
            throw new SQLException("参数" + idObj + "的id字段不能为空！");
        }
        String fkColumnName = TableUtils.getFKColumnName(fkClass);
        if (org.apache.commons.lang3.StringUtils.isEmpty(fkColumnName))
        {
            throw new SQLException("参数" + fkClass + "中未定义外键列，因此无法关联查询！请指定一个用@FK注解的字段！");
        }
        return listNoSql(fkClass, fkColumnName, idValue);
    }
    
    public <T> T query(final ResultSetResolve<T> ro, final String sql, final Object... args)
        throws SQLException
    {
        return exec(new StatementObject<T>()
        {
            public AllDbExecInfo build()
                throws SQLException
            {
                //                Logs.d("执行查询:" + sql);
                return prepareStatement(sql, Arrays.asList(args));
            }
            
            public T exec(Statement statement)
                throws SQLException
            {
                ResultSet rs = null;
                try
                {
                    rs = ps(statement).executeQuery();
                    return ro.exec(rs);
                }
                finally
                {
                    if (rs != null)
                    {
                        try
                        {
                            rs.close();
                        }
                        catch (SQLException e)
                        {
                            Logs.e("Result Set Close Error Cause", e);
                        }
                    }
                }
            }
        });
    }
    
    public boolean execute(final String sql, final Object... args)
        throws SQLException
    {
        return exec(new StatementObject<Boolean>()
        {
            public AllDbExecInfo build()
                throws SQLException
            {
                return prepareStatement(sql, Arrays.asList(args));
            }
            
            public Boolean exec(Statement statement)
                throws SQLException
            {
                return ps(statement).execute();
            }
        });
    }
    
    public int executeBatch(final String sql, final int batchSize, final Collection<?>... args)
        throws SQLException
    {
        return exec(new StatementObject<Integer>()
        {
            public AllDbExecInfo build()
                throws SQLException
            {
                return prepareStatement(sql, Arrays.asList(args));
            }
            
            public Integer exec(Statement statement)
                throws SQLException
            {
                //关闭自动提交
                //By default, new connections are in auto-commit mode. 
                connection.setAutoCommit(false);//关闭事务的自动提交。默认获得的连接就是自动提交的
                int result = 0;
                int bs = (batchSize > 0 ? batchSize : DbKit.DEFAULT_BATCH_SIZE);
                int totalNum = args.length;
                for (int i = 0; i < totalNum; i++)
                {
                    Collection<?> list = args[i];
                    setValues(ps(statement), list);
                    ps(statement).addBatch();//只有批量提交，没有批量删除操作！因为PreparedStatement只提供了addBatch()方法！
                    //每满足一次批量数，就批量提交一次
                    if ((i + 1) % bs == 0)
                    {
                        result += sum(ps(statement).executeBatch());
                        connection.commit();
                        ps(statement).clearBatch();
                    }
                }
                //如果有零头，那么将零头也批量提交掉
                if (totalNum % bs != 0)
                {
                    result += sum(ps(statement).executeBatch());
                    connection.commit();
                    ps(statement).clearBatch();
                }
                return result;
            }
        });
    }
    
    public int executeBatch(final String sql, final int batchSize, final Collection<Collection<?>> args)
        throws SQLException
    {
        return executeBatch(sql, batchSize, args.toArray(new Collection<?>[0]));
    }
    
    public int insert(String table, Collection<String> cols, Collection<Collection<?>> values, int batchSize)
        throws SQLException
    {
        return executeBatch(generateInsertSQL(table, cols), batchSize, values.toArray(new Collection<?>[0]));
    }
    
    public int insert(String table, Map<String, Object> columnDataMap)
        throws SQLException
    {
        List<String> columnNames = new ArrayList<String>(columnDataMap.keySet());
        List<Object> columnValues = new LinkedList<Object>();
        for (String columnName : columnNames)
        {
            columnValues.add(columnDataMap.get(columnName));
        }
        String sql = generateInsertSQL(table, columnNames);
        return update(sql, columnValues);
    }
    
    public int update(final String sql, final Object... values)
        throws SQLException
    {
        Logs.d("sql=" + sql + " values=" + Arrays.asList(values));
        return exec(new StatementObject<Integer>()
        {
            public AllDbExecInfo build()
                throws SQLException
            {
                return prepareStatement(sql, Arrays.asList(values));
            }
            
            public Integer exec(Statement statement)
                throws SQLException
            {
                return ps(statement).executeUpdate();
            }
        });
    }
    
    public int updateByParamArray(final String sql, Object[] values)
        throws SQLException
    {
        return exec(new StatementObject<Integer>()
        {
            public AllDbExecInfo build()
                throws SQLException
            {
                return prepareStatement(sql, Arrays.asList(values));
            }
            
            public Integer exec(Statement statement)
                throws SQLException
            {
                return ps(statement).executeUpdate();
            }
        });
    }
    
    @Override
    public int updateEntity(Object entity)
        throws SQLException
    {
        String sql = String.format("update %s set %s where %s",
            TableUtils.getTableName(entity),
            TableUtils.getUpdateJoinSqlForPreparedStatement(entity),
            TableUtils.getIdParamsValuesFragment(entity));
        return updateByParamArray(sql, TableUtils.getUpdateParamsForPreparedStatement(entity));
    }
    
    @Override
    public int delete(Object entity)
        throws SQLException
    {
        String sql = String.format("delete from %s where %s",
            TableUtils.getTableName(entity),
            TableUtils.getIdParamsValuesFragment(entity));
        return update(sql);
    }
    
    public List<Map<String, Object>> listMap(final String sql, final Object... values)
        throws SQLException
    {
        return list(new ResultSetResolve<Map<String, Object>>()
        {
            public Map<String, Object> exec(ResultSet rs)
                throws SQLException
            {
                return readMap(rs);
            }
        }, sql, values);
    }
    
    @Override
    public List<Map<String, Object>> listMapPaging(String sql, int start, int limit, Object... values)
        throws SQLException
    {
        return listMap(TableUtils.getPagingSql(sql, start, limit), values);
    }
    
    public List<Map<String, Object>> listMapPage(String sql, Collection<OrderBy> orders, int start, int limit, Object... args)
        throws SQLException
    {
        SqlAndArgs sa = tableDataResolver.getPaginationProcessor(connection).process(orders, start, limit, sql, Arrays.asList(args));
        return query(new ResultSetResolve<List<Map<String, Object>>>()
        {
            public List<Map<String, Object>> exec(ResultSet rs)
                throws SQLException
            {
                return readMapList(rs);
            }
        }, sa.getSql(), sa.getArgs());
    }
    
    public <T> List<T> list(final ResultSetResolve<T> ro, String sql, Object... args)
        throws SQLException
    {
        return query(new ResultSetResolve<List<T>>()
        {
            public List<T> exec(ResultSet rs)
                throws SQLException
            {
                List<T> result = new LinkedList<T>();
                while (rs.next())
                {
                    result.add(ro.exec(rs));
                }
                return result;
            }
        }, sql, args);
    }
    
    @Override
    public <T> List<T> list(Class<T> clazz, String sql, Object... args)
        throws SQLException
    {
        return list(new ResultSetToObjectMapper<>(clazz), sql, args);
    }
    
    @Override
    public <T> List<T> listPaging(Class<T> clazz, String sql, int start, int limit, Object... args)
        throws SQLException
    {
        return list(clazz, TableUtils.getPagingSql(sql, start, limit), args);
    }
    
    @Override
    public <T> List<T> listAll(Class<T> clazz)
        throws SQLException
    {
        return list(clazz, String.format("select * from %s", TableUtils.getTableName(clazz)));
    }
    
    @Override
    public <T> List<T> listNoSql(Class<T> clazz, Object... keyValues)
        throws SQLException
    {
        if (keyValues.length % 2 != 0)
        {
            throw new SQLException("keyValues参数个数必须是2的整数倍！");
        }
        String[] keys = new String[keyValues.length / 2];//0、1、2、3、...
        Object[] values = new String[keyValues.length / 2];
        if (keys.length > 0)
        {
            for (int i = 0; i < keys.length; i++)
            {
                keys[i] = ObjectUtils.toString(keyValues[i * 2]);
                values[i] = keyValues[i * 2 + 1];
            }
        }
        String basicSql = String.format("select * from %s where 1=1", TableUtils.getTableName(clazz));
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append(basicSql);
        if (keys.length > 0)
        {
            for (String key : keys)
            {
                sqlBuilder.append(String.format(" and %s=?", key));
            }
        }
        return list(new ResultSetToObjectMapper<>(clazz), sqlBuilder.toString(), (Object[])values);
    }
    
    @Override
    public <T> List<T> listAll(Class<T> clazz, String tableName)
        throws SQLException
    {
        return list(clazz, String.format("select * from %s", tableName));
    }
    
    @Override
    public <T> List<T> listAllPaging(Class<T> clazz, int start, int limit)
        throws SQLException
    {
        return list(clazz, TableUtils.getPagingSql(String.format("select * from %s", TableUtils.getTableName(clazz)), start, limit));
    }
    
    private <T> T exec(StatementObject<T> so)
        throws SQLException
    {
        Statement statement = null;
        try
        {
            long t1 = 0, t2 = 0;
            String sqlDetail = "";
            if (DbKit.SQL_MONITOR)
            {
                t1 = System.currentTimeMillis();
            }
            AllDbExecInfo allDbExecInfo = so.build();
            statement = allDbExecInfo.getStatement();
            if (DbKit.SQL_MONITOR)
            {
                sqlDetail = allDbExecInfo.getSqlDetail();
            }
            T t = so.exec(statement);
            if (DbKit.SQL_MONITOR)
            {
                t2 = System.currentTimeMillis();
                long cost = t2 - t1;
                if (DbKit.DEBUG_MODE)
                {
                    Logs.d("Cost " + cost + " ms" + ", sqlDetail=" + sqlDetail);
                }
                SQLMonitor.record(sqlDetail, cost);
            }
            return t;
        }
        finally
        {
            if (statement != null)
            {
                try
                {
                    //Logs.d("Close Statement [" + statement + "].");
                    statement.close();
                }
                catch (SQLException e)
                {
                    Logs.e("Close Statement Error Cause.", e);
                }
            }
        }
    }
    
    private PreparedStatement ps(Statement statement)
    {
        return (PreparedStatement)statement;
    }
    
    private Map<String, Object> readMap(ResultSet rs)
        throws SQLException
    {
        //获取结果集的列数据。这样就可以不因为value为null而丢失对应列的键值对
        ResultSetMetaData metaData = rs.getMetaData();
        int numColumn = metaData.getColumnCount();
        //按照原有的列顺序进行返回
        Map<String, Object> mapRtn = new LinkedHashMap<String, Object>();
        for (int i = 1; i <= numColumn; ++i)
        {
            String colName = metaData.getColumnLabel(i);
            // ignore row number
            if (!PaginationProcessor.COLUMN_ROW_NUMBER.equalsIgnoreCase(colName))
            {
                mapRtn.put(colName, tableDataResolver.readValue(rs, i));
            }
        }
        return mapRtn;
    }
    
    private List<Map<String, Object>> readMapList(ResultSet rs)
    {
        List<Map<String, Object>> mapList = new LinkedList<Map<String, Object>>();
        try
        {
            while (rs.next())
            {
                mapList.add(readMap(rs));
            }
        }
        catch (SQLException e)
        {
            throw new IllegalStateException(e);
        }
        return mapList;
    }
    
    private PreparedStatement setValues(PreparedStatement ps, Collection<?> values)
        throws SQLException
    {
        if (values != null && !values.isEmpty())
        {
            int i = 0;
            for (Object value : values)
            {
                tableDataResolver.setParam(ps, ++i, value);
            }
        }
        return ps;
    }
    
    /**
     * @param sql delete from users where id = :id
     * @param mapArgs {id: 2008110101}
     * @param outArgs [] -> [2008110101]
     * @return delete from users where id = ?
     */
    private String buildSql(String sql, Map<String, ?> mapArgs, List<Object> outArgs)
    {
        String rtnSQL;
        if (!mapArgs.isEmpty())
        {
            StringBuffer newSql = new StringBuffer();
            // matches such as :user_id, :id
            Matcher matcher = Pattern.compile(":\\w+").matcher(sql);
            while (matcher.find())
            {
                List<Object> args = new LinkedList<Object>();
                // replace named arg ':user_id' to arg holder '?'
                matcher.appendReplacement(newSql, genArgHolder(mapArgs.get(matcher.group().substring(1)), args));
                outArgs.addAll(args);
            }
            matcher.appendTail(newSql);
            rtnSQL = newSql.toString();
        }
        else
        {
            rtnSQL = sql;
        }
        return rtnSQL;
    }
    
    /**
     * @param sql delete from users where id in (?)
     * @param args
     * @param outArgs
     * @return
     */
    private String buildSql(String sql, Collection<?> args, List<Object> outArgs)
    {
        StringBuilder newSql = new StringBuilder();
        // avoid last ? could not be split
        String[] sqlSplitted = (sql + " ").split("\\?");
        // no ? and values is empty
        if (sqlSplitted.length == 1 && args.isEmpty())
        {
            newSql.append(sql);
        }
        // expand n values for 1 '?'
        else if (sqlSplitted.length == 2 && !args.isEmpty())
        {
            newSql.append(sqlSplitted[0]);
            newSql.append(genArgHolder(args, outArgs));
            newSql.append(sqlSplitted[1]);
        }
        // size(?) == size(values)
        else if (sqlSplitted.length == args.size() + 1)
        {
            int i = 0;
            for (Object v : args)
            {
                List<Object> valuesExpanded = new LinkedList<Object>();
                newSql.append(sqlSplitted[i++]);
                newSql.append(genArgHolder(v, valuesExpanded));
                outArgs.addAll(valuesExpanded);
            }
            newSql.append(sqlSplitted[sqlSplitted.length - 1]);
        }
        // error
        else
        {
            throw new IllegalStateException(fmt("SQL [{}] Does Not Match Args [{}]", sql, args));
        }
        return newSql.toString();
    }
    
    @SuppressWarnings("unchecked")
    private AllDbExecInfo prepareStatement(String sql, Collection<?> args)
        throws SQLException
    {
        String newSql;
        List<Object> newArgs = new LinkedList<Object>();
        if (args.size() == 1)
        {
            Object objArgs = args.iterator().next();
            if (objArgs != null && objArgs.getClass().isArray())
            {
                List<Object> vh = new LinkedList<Object>();
                for (int i = 0; i < Array.getLength(objArgs); ++i)
                {
                    vh.add(Array.get(objArgs, i));
                }
                newSql = buildSql(sql, vh, newArgs);
            }
            else if (objArgs instanceof Collection<?>)
            {
                newSql = buildSql(sql, (Collection<?>)objArgs, newArgs);
            }
            else if (objArgs instanceof Map<?, ?>)
            {
                newSql = buildSql(sql, (Map<String, ?>)objArgs, newArgs);
            }
            else
            {
                newSql = sql;
                newArgs.add(objArgs);
            }
        }
        else if (args.size() > 1)
        {
            newSql = buildSql(sql, args, newArgs);
        }
        else
        {
            newSql = sql;
        }
        PreparedStatement ps = connection.prepareStatement(newSql);
        //sql参数有内容，并且有参数的时候，才进行设值
        if (!newArgs.isEmpty() && newSql.indexOf("?") != -1)
        {
            setValues(ps, newArgs);
        }
        return new AllDbExecInfo().setSqlDetail(buildSqlAndParam(newSql, newArgs)).setStatement(ps);
    }
    
    /**
     * 拼接一条待展示分析的sql
     * @author nan.li
     * @param newSql
     * @param newArgs
     * @return
     */
    private String buildSqlAndParam(String newSql, List<Object> newArgs)
    {
        if (Lists.isEmpty(newArgs))
        {
            return newSql;
        }
        return String.format(newSql.replaceAll("\\?", "'%s'"), newArgs.toArray(new Object[newArgs.size()]));
    }
    
    public static void main(String[] args)
    {
        String aa = "select value from Word where name=?";
        System.out.println(aa);
        aa = aa.replaceAll("\\?", "%s");
        System.out.println(aa);
    }
    
    /**
     * generate arg holder
     * @param arg if v is collection, generate '?, ?...' else ?
     * @param argsOut args out
     * @return sql ?, ?
     */
    private String genArgHolder(Object arg, List<Object> argsOut)
    {
        String sqlRtn = null;
        if (arg != null && arg.getClass().isArray())
        {
            List<String> vh = new LinkedList<String>();
            for (int i = 0; i < Array.getLength(arg); ++i)
            {
                argsOut.add(Array.get(arg, i));
                vh.add("?");
            }
            sqlRtn = StringUtils.join(vh, ", ");
        }
        else if (arg instanceof Collection<?>)
        {
            Collection<?> listValues = (Collection<?>)arg;
            argsOut.addAll(listValues);
            String[] vh = new String[listValues.size()];
            Arrays.fill(vh, "?");
            sqlRtn = StringUtils.join(Arrays.asList(vh), ", ");
        }
        else
        {
            argsOut.add(arg);
            sqlRtn = "?";
        }
        return sqlRtn;
    }
    
    /**
     * 生成插入的sql语句
     * @author nan.li
     * @param table
     * @param columnNames
     * @return
     */
    protected String generateInsertSQL(String table, Collection<String> columnNames)
    {
        List<String> params = new ArrayList<>();
        for (int i = 0; i < columnNames.size(); i++)
        {
            params.add("?");
        }
        return new StringBuffer("insert into ").append(table)
            .append(" (")
            .append(StringUtils.join(columnNames, ", "))
            .append(") values (")
            .append(StringUtils.join(params, ", "))
            .append(")")
            .toString();
    }
    
    private int sum(int[] v)
    {
        int result = 0;
        for (int i : v)
        {
            result += i;
        }
        return result;
    }
    
    private String fmt(String msg, Object... args)
    {
        return MessageFormatter.format(msg, args).getMessage();
    }
    
    @Override
    public boolean contains(Class<?> tableClazz, String field, String value)
        throws SQLException
    {
        return list(tableClazz, String.format("select * from %s where %s='%s'", TableUtils.getTableName(tableClazz), field, value)).size() > 0;
    }
    
    @Override
    public boolean containsLike(Class<?> tableClazz, String field, String value)
        throws SQLException
    {
        return list(tableClazz, String.format("select * from %s where %s like '%%%s%%'", TableUtils.getTableName(tableClazz), field, value)).size() > 0;
    }
    
    @Override
    public int insert(Object entity)
        throws SQLException
    {
        Class<?> entityClass = entity.getClass();
        String table = TableUtils.getTableName(entityClass);
        Map<String, Object> columnDataMap = TableUtils.obj2Map(entity);
        return insert(table, columnDataMap);
    }
    
    @Override
    public int insertBatch(List<?> entities)
        throws SQLException
    {
        return insertBatch(entities, DbKit.DEFAULT_BATCH_SIZE);
    }
    
    @Override
    public int insertBatch(List<?> entities, int batchSize)
        throws SQLException
    {
        long begin = System.currentTimeMillis();
        BatchObj batchObj = TableUtils.getBatchObj(entities);
        int result = insert(TableUtils.getTableName(entities.get(0).getClass()), batchObj.getCols(), batchObj.getArgs(), batchSize);
        long end = System.currentTimeMillis();
        Logs.i(String.format("Batch Insert cost %d millseconds!", (end - begin)));
        return result;
    }
    
    @Override
    public boolean dropTable(Class<?> tableClazz)
        throws SQLException
    {
        String sql = String.format("DROP TABLE IF EXISTS %s", TableUtils.getTableName(tableClazz));
        return execute(sql);
    }
    
    @Override
    public boolean dropTable(String tableName)
        throws SQLException
    {
        String sql = String.format("DROP TABLE IF EXISTS %s", tableName);
        System.out.println(String.format("即将执行：%s", sql));
        return execute(sql);
    }
    
}
