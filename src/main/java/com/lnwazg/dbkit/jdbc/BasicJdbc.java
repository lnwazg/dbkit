package com.lnwazg.dbkit.jdbc;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.lnwazg.dbkit.order.OrderBy;
import com.lnwazg.dbkit.resolver.ResultSetResolve;

/**
 * JDBC查询，最基础的脚手架，也是最通用、最常用的脚手架<br>
 * API命名原则： 命名考究，按类型划分，简单易记！<br>
 * 不好用的框架、不够智能的框架，终究将会被历史所淘汰掉！<br>
 * 查询单个对象或者单个值的API: 基本都是由load 或者 find开头的<br>
 * @author nan.li
 * @version 2017年5月3日
 */
public interface BasicJdbc
{
    /**
     * 根据一个实体类的Id编码去查询出该实体类的对象，仅返回一条记录<br>
     * 该方法无须指定sql语句即可查询到数据
     * @author nan.li
     * @param clazz
     * @param id
     * @return
     * @throws SQLException
     */
    <T> T load(Class<T> clazz, Object id)
        throws SQLException;
        
    /**
     * 根据sql条件和参数查询单个实体对象
     * @author nan.li
     * @param ro
     * @param sql
     * @param args
     * @return
     * @throws SQLException
     */
    <T> T findOne(ResultSetResolve<T> ro, String sql, Object... args)
        throws SQLException;
        
    /**
     * 根据sql条件和参数查询单个实体对象
     * @author nan.li
     * @param clazz
     * @param sql
     * @param args
     * @return
     * @throws SQLException
     */
    <T> T findOne(Class<T> clazz, String sql, Object... args)
        throws SQLException;
        
    /**
     * 根据主键对象和拥有外键的实体类，查询出一个外键对象
     * @author nan.li
     * @param idObj
     * @param fkClass
     * @return
     * @throws SQLException
     */
    <T> T findForeignOne(Object idObj, Class<T> fkClass)
        throws SQLException;
        
    /**
     * 根据参数查询单个实体对象，无须指定sql即可查询
     * @author nan.li
     * @param clazz
     * @param keyValues
     * @return
     * @throws SQLException
     */
    <T> T findOneNoSql(Class<T> clazz, Object... keyValues)
        throws SQLException;
        
    /**
     * 根据sql条件和参数查询出一个HashMap
     * @param sql sql
     * @param args sql args
     * @return query result
     */
    Map<String, Object> findMap(String sql, Object... args)
        throws SQLException;
        
    /**
     * 根据sql条件和参数查询出一个value值，可能是一个String，也可能是一个int值<br>
     * 一般用在count等查询语句中<br>
     * 该查询的特点是：只返回一行数据，并且该行数据只有一列<br>
     * 1.例如以下sql语句： <code>long count = findValue("select count(1) from users");</code><br>
     * 2.例如以下sql语句： <code>double salaries = findValue("select sum(salary) from users");</code>
     * @param sql sql
     * @param args sql args
     * @param <T> result type
     * @return query result
     */
    <T> T findValue(String sql, Object... args)
        throws SQLException;
        
    /**
     * 兼容性更强的findValue统计语句<br>
     * 因为对于count(1)的结果类型，sqlite是int,mysql是long,oracle是bigDecimal，所以需要一种通用的兼容性写法！
     * @author nan.li
     * @param sql
     * @param args
     * @return
     * @throws SQLException
     */
    long findValueToLong(String sql, Object... args)
        throws SQLException;
        
    /**
     * 兼容性更强的findValue统计语句<br>
     * 因为对于count(1)的结果类型，sqlite是int,mysql是long,oracle是bigDecimal，所以需要一种通用的兼容性写法！
     * @author nan.li
     * @param sql
     * @param args
     * @return
     * @throws SQLException
     */
    int findValueToInt(String sql, Object... args)
        throws SQLException;
        
    /**
     * 计数<br>
     * 是findValueToInt的简写方式！
     * @author nan.li
     * @param sql
     * @param args
     * @return
     * @throws SQLException
     */
    int count(String sql, Object... args)
        throws SQLException;
        
    //查询结果集是一个列表的API=========================================================================
    //基本都是由list开头的
    /**
     * 查询一列值，该列值的类型一般是String或者int<br>
     * 示例代码如下：
     * <code>
     * 		List&lt;String&gt; names = listValue("select name from users");	
     * </code>
     * @param sql sql
     * @param args sql args
     * @param <T> result type
     * @return query result
     */
    <T> List<T> listValue(String sql, Object... args)
        throws SQLException;
        
    /**
     * 根据sql条件和参数查询对象列表<br>
     * 老API，基本不使用
     * @param ro result set operator
     * @param sql sql
     * @param args sql args
     * @param <T> result type
     * @return query result
     */
    @Deprecated
    <T> List<T> list(ResultSetResolve<T> ro, String sql, Object... args)
        throws SQLException;
        
    /**
     * 根据sql条件和参数查询对象列表<br>
     * @author nan.li
     * @param clazz
     * @param sql
     * @param args
     * @return
     * @throws SQLException
     */
    <T> List<T> list(Class<T> clazz, String sql, Object... args)
        throws SQLException;
        
    /**
     * 根据主键对象和拥有外键的实体类，查询出外键对象列表
     * @author nan.li
     * @param idObj
     * @param fkClass
     * @return
     * @throws SQLException
     */
    <T> List<T> listForeignList(Object idObj, Class<T> fkClass)
        throws SQLException;
        
    /**
     * 根据参数查询对象列表，并且支持分页查询
     * @author nan.li
     * @param clazz
     * @param sql
     * @param start
     * @param limit
     * @param args
     * @return
     * @throws SQLException
     */
    <T> List<T> listPaging(Class<T> clazz, String sql, int start, int limit, Object... args)
        throws SQLException;
        
    /**
     * 根据参数查询对象列表，无须指定sql即可查询
     * @author nan.li
     * @param clazz
     * @param sql
     * @param args
     * @return
     * @throws SQLException
     */
    <T> List<T> listNoSql(Class<T> clazz, Object... keyValues)
        throws SQLException;
        
    /**
     * 查询某张表的所有数据，并指定表名称
     * @author nan.li
     * @param clazz
     * @param tableName
     * @return
     * @throws SQLException
     */
    <T> List<T> listAll(Class<T> clazz, String tableName)
        throws SQLException;
        
    /**
     * 查询某张表的所有数据
     * @author nan.li
     * @param clazz
     * @return
     * @throws SQLException
     */
    <T> List<T> listAll(Class<T> clazz)
        throws SQLException;
        
    /**
     * 查询某张表的所有数据，并且支持分页查询
     * @author nan.li
     * @param clazz
     * @param start
     * @param limit
     * @return
     * @throws SQLException
     */
    <T> List<T> listAllPaging(Class<T> clazz, int start, int limit)
        throws SQLException;
        
    /**
     * 根据sql条件和参数查询任意结果集，将结果集映射成List&lt;Map&gt;
     * @param sql sql
     * @param args args
     * @return query result
     */
    List<Map<String, Object>> listMap(String sql, Object... args)
        throws SQLException;
        
    /**
     * 根据sql条件和参数查询任意结果集，将结果集映射成List&lt;Map&gt;，并且支持分页查询
     * @author nan.li
     * @param sql
     * @param start
     * @param limit
     * @param args
     * @return
     * @throws SQLException
     */
    List<Map<String, Object>> listMapPaging(String sql, int start, int limit, Object... args)
        throws SQLException;
        
    /**
     * 根据sql条件和参数查询任意结果集，将结果集映射成List&lt;Map&gt;，并且支持分页查询<br>
     * 老API，已经不推荐再使用
     * @param sql sql
     * @param orders orders
     * @param offset row offset
     * @param limit row limit
     * @param args sql args
     * @return query result
     */
    @Deprecated
    List<Map<String, Object>> listMapPage(String sql, Collection<OrderBy> orders, int offset, int limit, Object... args)
        throws SQLException;
        
    /**
     * 老API，已经不推荐再使用。推荐使用findOne() 查找一个对象  或者findValue() 查询一个结果值
     * @param ro result set operator
     * @param sql sql
     * @param args sql args
     * @param <T> result type
     * @return query result
     * @throws SQLException 
     */
    @Deprecated
    <T> T query(ResultSetResolve<T> ro, String sql, Object... args)
        throws SQLException;
        
    /**
     * 执行任意sql语句
     * @param sql sql
     * @param args sql args
     * @return execute result
     */
    boolean execute(String sql, Object... args)
        throws SQLException;
        
    /**
     * 批量执行任意sql语句
     * @param sql sql
     * @param args args
     * @param batchSize batch size
     * @return effect rows result
     */
    int executeBatch(String sql, int batchSize, Collection<?>... args)
        throws SQLException;
        
    /**
     * 批量执行任意sql语句
     * @param sql sql
     * @param batchSize batch size
     * @param args sql args
     * @return effect rows result
     */
    int executeBatch(String sql, int batchSize, Collection<Collection<?>> args)
        throws SQLException;
        
    /**
     * 插入多行数据
     * @param table table
     * @param cols column names
     * @param args rows data
     * @param batchSize batch size
     * @return effect rows result
     */
    int insert(String table, Collection<String> cols, Collection<Collection<?>> args, int batchSize)
        throws SQLException;
        
    int save(String table, Collection<String> cols, Collection<Collection<?>> args, int batchSize)
        throws SQLException;
        
    /**
     * 插入一行数据
     * @param table table
     * @param data row data
     * @return effect rows result
     */
    int insert(String table, Map<String, Object> columnDataMap)
        throws SQLException;
        
    int save(String table, Map<String, Object> columnDataMap)
        throws SQLException;
        
    /**
     * 更新某张表
     * @param sql sql
     * @param args sql args
     * @return effect rows result
     */
    int update(String sql, final Object... args)
        throws SQLException;
        
    /**
     * 更新某张表
     * @param sql sql
     * @param args sql args
     * @return effect rows result
     */
    int updateByParamArray(String sql, Object[] values)
        throws SQLException;
        
    /**
     * 更新某个实体对象<br>
     * 主要是怕跟update()里面的可变参数冲突<br>
     * 支持多主键对象
     * @author nan.li
     * @param entity
     * @return
     * @throws SQLException
     */
    int updateEntity(Object entity)
        throws SQLException;
        
    /**
     * 某个表的某个字段是否包含某个值
     * @author nan.li
     * @param entity
     * @param field
     * @param value
     * @return
     */
    boolean contains(Class<?> tableClazz, String field, String value)
        throws SQLException;
        
    /**
     * 某个表的某个字段是否近似包含某个值
     * @author nan.li
     * @param entity
     * @param field
     * @param value
     * @return
     */
    boolean containsLike(Class<?> tableClazz, String field, String value)
        throws SQLException;
        
    /**
     * 插入一个实体类到数据库
     * @author nan.li
     * @param entity
     * @return
     * @throws SQLException
     */
    int insert(Object entity)
        throws SQLException;
        
    int save(Object entity)
        throws SQLException;
        
    /**
     * 保存或更新，自适应<br>
     * 若声明了@Id的字段的值非空，则更新；否则，插入新记录
     * @author nan.li
     * @param entity
     * @return
     * @throws SQLException
     */
    int saveOrUpdate(Object entity)
        throws SQLException;
        
    /**
     * 批量插入实体类列表到数据库，使用默认的batchSize
     * @author nan.li
     * @param entities
     * @return
     * @throws SQLException
     */
    int insertBatch(List<?> entities)
        throws SQLException;
        
    int saveBatch(List<?> entities)
        throws SQLException;
        
    /**
     * 批量插入实体类列表到数据库，指定批量提交的batchSize
     * @author nan.li
     * @param entities
     * @param batchSize
     * @return
     * @throws SQLException
     */
    int insertBatch(List<?> entities, int batchSize)
        throws SQLException;
        
    int saveBatch(List<?> entities, int batchSize)
        throws SQLException;
        
    /**
     * 删除一个实体对象<br>
     * 支持多主键对象
     * @author nan.li
     * @param entity
     * @return
     * @throws SQLException
     */
    int delete(Object entity)
        throws SQLException;
        
    /**
     * 根据表类删除某张表
     * @author nan.li
     * @param tableClazz
     * @return
     * @throws SQLException
     */
    boolean dropTable(Class<?> tableClazz)
        throws SQLException;
        
    /**
     * 根据表名称删除某张表
     * @author nan.li
     * @param tableName
     * @return
     * @throws SQLException
     */
    boolean dropTable(String tableName)
        throws SQLException;
}
