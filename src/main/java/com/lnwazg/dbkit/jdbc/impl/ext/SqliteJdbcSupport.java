package com.lnwazg.dbkit.jdbc.impl.ext;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;

import com.lnwazg.dbkit.anno.entity.AutoIncrement;
import com.lnwazg.dbkit.anno.entity.Id;
import com.lnwazg.dbkit.anno.entity.Index;
import com.lnwazg.dbkit.jdbc.MyJdbc;
import com.lnwazg.dbkit.jdbc.impl.ConnectionManagerImpl;
import com.lnwazg.dbkit.utils.TableUtils;
import com.lnwazg.kit.list.Lists;
import com.lnwazg.kit.log.Logs;
import com.lnwazg.kit.reflect.ClassKit;

/**
 * Sqlite Jdbc的实现
 * @author Administrator
 * @version 2016年5月12日
 */
public class SqliteJdbcSupport extends ConnectionManagerImpl implements MyJdbc
{
    public SqliteJdbcSupport(DataSource dataSource)
    {
        super(dataSource);
    }
    
    public SqliteJdbcSupport(DataSource dataSource, String schemaName)
    {
        super(dataSource, schemaName);
    }
    
    @Override
    public boolean createTable(Object entity)
        throws SQLException
    {
        return createTable(entity.getClass());
    }
    
    @Override
    public boolean createTable(Class<?> tableClazz)
        throws SQLException
    {
        boolean done = checkTableExists(tableClazz);//建表任务是否已经完成
        if (!done)
        {
            //获取表名称
            String tableName = TableUtils.getTableName(tableClazz);
            
            //遍历字段，根据字段去建表
            //获取字段列表
            Field[] fields = null;
            boolean subClassFieldsFirst = TableUtils.isSubClassFieldsFirst(tableClazz);
            if (subClassFieldsFirst)
            {
                Logs.i("子类字段顺序优先");
                fields = ClassKit.getAllDeclaredFieldsSubClassFirst(tableClazz);
            }
            else
            {
                Logs.i("父类字段顺序优先");
                fields = ClassKit.getAllDeclaredFieldsParentClassFirst(tableClazz);
            }
            
            List<String> fieldSentences = new ArrayList<>();//字段语句列表
            List<String> indexStrList = new ArrayList<>();
            List<String> multipleIndexFieldNames = new ArrayList<>();
            for (Field field : fields)
            {
                field.setAccessible(true);
                String fieldName = field.getName();
                boolean primaryKey = field.isAnnotationPresent(Id.class);
                boolean autoIncrement = field.isAnnotationPresent(AutoIncrement.class);
                boolean index = field.isAnnotationPresent(Index.class);//是否是索引
                if (index)
                {
                    Index indexAnno = field.getAnnotation(Index.class);
                    boolean multiple = indexAnno.multiple();
                    if (!multiple)
                    {
                        //CREATE INDEX node_idx ON MESSAGE(node);
                        //CREATE INDEX testtable_idx ON testtable(first_col);
                        //CREATE INDEX testtable_idx2 ON testtable(first_col ASC,second_col DESC);
                        indexStrList.add(String.format("CREATE INDEX [%s_%s_idx] ON [%s]([%s])", tableName, fieldName, tableName, fieldName));
                    }
                    else
                    {
                        multipleIndexFieldNames.add(fieldName);
                    }
                }
                String type = "";
                if (field.getType() == int.class || field.getType() == Integer.class)
                {
                    type = "integer";
                }
                else if (field.getType() == String.class)
                {
                    type = "text";
                }
                else if (field.getType() == byte[].class)
                {
                    type = "blob";
                }
                else if (field.getType() == Date.class)
                {
                    type = "timestamp default (datetime('now', 'localtime'))";
                }
                else
                {
                    type = "text";
                }
                fieldSentences.add(String.format("%s %s%s%s", fieldName, type, (primaryKey ? " primary key" : ""), (autoIncrement ? " autoincrement" : "")));
            }
            String fieldsJoinStr = StringUtils.join(fieldSentences, ",");//将字段语句列表拼接成一个完整的语句
            
            //如果还没有完成
            String sql = String.format("CREATE TABLE IF NOT EXISTS %s (%s)", tableName, fieldsJoinStr);
            System.out.println(String.format("即将执行：%s", sql));
            execute(sql);
            
            //插入相应的索引列表
            if (Lists.isNotEmpty(indexStrList))
            {
                for (String indexStr : indexStrList)
                {
                    System.out.println(String.format("即将执行索引语句：%s", indexStr));
                    execute(indexStr);
                }
            }
            if (Lists.isNotEmpty(multipleIndexFieldNames))
            {
                String indexStr = String.format("CREATE INDEX [%s_multi_idx] ON %s(%s)", tableName, tableName, StringUtils.join(multipleIndexFieldNames, ","));
                System.out.println(String.format("即将执行复合索引语句：%s", indexStr));
                execute(indexStr);
            }
            return true;
        }
        else
        {
            return false;
        }
    }
    
    @Override
    public boolean createTable(String tableName, List<String> columns)
        throws SQLException
    {
        boolean done = checkTableExists(tableName);//建表任务是否已经完成
        if (!done)
        {
            //如果还没有完成
            //获取表名称
            List<String> fieldSentences = new ArrayList<>();//字段语句列表
            for (String columnName : columns)
            {
                fieldSentences.add(String.format("%s text", columnName));
            }
            
            //将字段语句列表拼接成一个完整的语句
            String fieldsJoinStr = StringUtils.join(fieldSentences, ",");
            // COMMENT='商品表'
            String sql = String.format("CREATE TABLE IF NOT EXISTS %s (%s)",
                tableName,
                fieldsJoinStr);
            System.out.println(String.format("即将执行：%s", sql));
            execute(sql);
            return true;
        }
        return false;
    }
    
    @Override
    public int insertCheckTable(Object entity)
        throws SQLException
    {
        if (!checkTableExists(entity))
        {
            createTable(entity);
        }
        return insert(entity);
    }
    
    @Override
    public boolean checkTableExists(Object entity)
        throws SQLException
    {
        //获取表名称
        String tableName = TableUtils.getTableName(entity);
        String sql = String.format("SELECT COUNT(1) count FROM sqlite_master where type='table' and name='%s'", tableName);
        return count(sql) > 0;
    }
    
    public boolean checkTableExists(Class<?> tableClazz)
        throws SQLException
    {
        //获取表名称
        String tableName = TableUtils.getTableName(tableClazz);
        String sql = String.format("SELECT COUNT(1) count FROM sqlite_master where type='table' and name='%s'", tableName);
        return count(sql) > 0;
    }
    
    @Override
    public boolean checkTableExists(String tableName)
        throws SQLException
    {
        String sql = String.format("SELECT COUNT(1) count FROM sqlite_master where type='table' and name='%s'", tableName);
        return count(sql) > 0;
    }
    
    @Override
    public boolean truncateTable(Class<?> tableClazz)
        throws SQLException
    {
        return truncateTable(TableUtils.getTableName(tableClazz));
    }
    
    @Override
    public boolean truncateTable(String tableName)
        throws SQLException
    {
        String sql = String.format("DELETE FROM %s", tableName);
        //        System.out.println(String.format("即将执行：%s", sql));
        boolean result = execute(sql);
        
        sql = String.format("DELETE FROM sqlite_sequence WHERE name = '%s'", tableName);
        //        System.out.println(String.format("即将执行：%s", sql));
        execute(sql);
        return result;
    }
    
    @Override
    public void insertAndGetAutoIncr(Object entity)
        throws SQLException
    {
        throw new UnsupportedOperationException("该方法尚不支持sqlite数据库！Not implemented yet");
    }
    
    @Override
    public void insertAndGetAutoIncr(Object entity, String keyPropertyFieldName)
        throws SQLException
    {
        throw new UnsupportedOperationException("该方法尚不支持sqlite数据库！Not implemented yet");
    }
    
    @Override
    public void alterTable(Class<?> tableClazz)
        throws SQLException
    {
        throw new UnsupportedOperationException("该方法尚不支持sqlite数据库！Not implemented yet");
    }
    
}
