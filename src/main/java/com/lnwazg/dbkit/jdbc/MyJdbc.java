package com.lnwazg.dbkit.jdbc;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 最终使用的Jdbc接口<br>
 * 增加了一些特定数据库的定制操作接口
 * @author Administrator
 * @version 2016年5月12日
 */
public interface MyJdbc extends ConnectionManager
{
    /**
     * 执行建表语句
     * @author nan.li
     * @param tableClazz
     * @return   true新建表并且成功了     false没有新建表
     * @throws SQLException
     */
    boolean createTable(Class<?> tableClazz)
        throws SQLException;
        
    boolean createTable(Object entity)
        throws SQLException;
        
    /**
     * 建表，所有列都是字符串类型
     * @author nan.li
     * @param tableName
     * @param columns
     * @throws SQLException
     */
    boolean createTable(String tableName, List<String> columns)
        throws SQLException;
        
    /**
     * 修改表结构，不影响表数据
     * @author nan.li
     * @param tableClazz
     * @throws SQLException
     */
    void alterTable(Class<?> tableClazz)
        throws SQLException;
        
    int insertCheckTable(Object entity)
        throws SQLException;
        
    /**
     * 检查某个表是否存在
     * @author nan.li
     * @param entity
     * @return
     * @throws SQLException
     */
    boolean checkTableExists(Object entity)
        throws SQLException;
        
    boolean checkTableExists(Class<?> tableClazz)
        throws SQLException;
        
    boolean checkTableExists(String tableName)
        throws SQLException;
        
    boolean truncateTable(Class<?> tableClazz)
        throws SQLException;
        
    boolean truncateTable(String tableName)
        throws SQLException;
        
    /**
     * 插入并为自增长字段赋值
     * @author lnwazg@126.com
     * @param entity
     * @throws SQLException
     */
    void insertAndGetAutoIncr(Object entity)
        throws SQLException;
        
    /**
     * 插入并为自增长字段赋值， 并指定自增长字段的名称
     * @author lnwazg@126.com
     * @param entity
     * @param keyPropertyFieldName
     * @throws SQLException
     */
    void insertAndGetAutoIncr(Object entity, String keyPropertyFieldName)
        throws SQLException;
        
    /**
     * 建表并批量插入数据
     * @author nan.li
     * @param tableName
     * @param columns
     * @param listMap
     * @throws SQLException
     */
    default void createTableAndInsertColumnData(String tableName, List<String> columns, List<Map<String, Object>> listMap)
        throws SQLException
    {
        createTable(tableName, columns);
        System.out.println("开始插入数据集...");
        for (Map<String, Object> pMap : listMap)
        {
            insert(tableName, pMap);
        }
    }
    
    default void createTableAndInsertColumnData(String tableName, List<Map<String, Object>> listMap)
        throws SQLException
    {
        Set<String> set = listMap.get(0).keySet();
        List<String> columns = new ArrayList<>();
        for (String column : set)
        {
            columns.add(column);
        }
        createTableAndInsertColumnData(tableName, columns, listMap);
    }
    
    default void createTableAndRefreshColumnData(String tableName, List<Map<String, Object>> listMap)
        throws SQLException
    {
        dropTable(tableName);
        createTableAndInsertColumnData(tableName, listMap);
    };
    
    /**
     * 建表并批量插入数据，刷新数据内容
     * @author nan.li
     * @param tableName
     * @param columns
     * @param listMap
     * @throws SQLException
     */
    default void createTableAndRefreshColumnData(String tableName, List<String> columns, List<Map<String, Object>> listMap)
        throws SQLException
    {
        dropTable(tableName);
        createTableAndInsertColumnData(tableName, columns, listMap);
    }
}
