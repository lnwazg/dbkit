package com.lnwazg.dbkit.jdbc.impl.ext;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;

import com.lnwazg.dbkit.anno.ddl.AlterTable;
import com.lnwazg.dbkit.anno.ddl.AlterTableEnum;
import com.lnwazg.dbkit.anno.entity.AutoIncrement;
import com.lnwazg.dbkit.anno.entity.Comment;
import com.lnwazg.dbkit.anno.entity.DefaultValue;
import com.lnwazg.dbkit.anno.entity.Id;
import com.lnwazg.dbkit.anno.entity.Index;
import com.lnwazg.dbkit.anno.entity.NotNull;
import com.lnwazg.dbkit.anno.entity.Varchar;
import com.lnwazg.dbkit.jdbc.MyJdbc;
import com.lnwazg.dbkit.jdbc.impl.ConnectionManagerImpl;
import com.lnwazg.dbkit.utils.DbKit;
import com.lnwazg.dbkit.utils.TableUtils;
import com.lnwazg.kit.list.Lists;
import com.lnwazg.kit.log.Logs;
import com.lnwazg.kit.reflect.ClassKit;

/**
 * Mysql Jdbc 的实现<br>
 * 在线参考使用手册：https://git.oschina.net/chyxion/newbie-jdbc
 * @author Administrator
 * @version 2016年5月12日
 */
public class MysqlJdbcSupport extends ConnectionManagerImpl implements MyJdbc
{
    public MysqlJdbcSupport(DataSource dataSource)
    {
        super(dataSource);
    }
    
    public MysqlJdbcSupport(DataSource dataSource, String schemaName)
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
    public void alterTable(Class<?> tableClazz)
        throws SQLException
    {
        //扫描该Class的各个字段，如果有@AlterTable注解，就去执行alter table语句  
        
        //获取表名称
        String tableName = TableUtils.getTableName(tableClazz);
        
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
        
        for (Field field : fields)
        {
            field.setAccessible(true);
            String fieldName = field.getName();
            //考虑到联合主键的情况
            boolean autoIncrement = field.isAnnotationPresent(AutoIncrement.class);
            boolean notNull = field.isAnnotationPresent(NotNull.class);
            boolean comment = field.isAnnotationPresent(Comment.class);
            boolean varchar = field.isAnnotationPresent(Varchar.class);
            boolean defaultValue = field.isAnnotationPresent(DefaultValue.class);
            boolean alterTable = field.isAnnotationPresent(AlterTable.class);//该字段是否有修改表结构的指令
            
            String type = "";
            if (field.getType() == int.class || field.getType() == Integer.class)
            {
                type = "int";
            }
            else if (field.getType() == String.class)
            {
                type = "longtext";
                if (varchar)
                {
                    //                        type =  VARCHAR(100) 
                    type = String.format("varchar(%d)", field.getAnnotation(Varchar.class).value());
                }
            }
            else if (field.getType() == byte[].class)
            {
                type = "blob";
            }
            else if (field.getType() == Date.class)
            {
                type = "datetime";
            }
            else
            {
                type = "longtext";
            }
            
            //如果有@AlterTable注解，就去执行alter table语句  
            
            //ALTER TABLE Persons ADD COLUMN Birthday date
            //ALTER TABLE Persons ALTER COLUMN Birthday year
            //ALTER TABLE Persons DROP COLUMN Birthday
            
            if (alterTable)
            {
                //操作措施 
                //ADD
                //DROP
                //MODIFY
                String handle = field.getAnnotation(AlterTable.class).value().name();
                
                //default is ADD and MODIFY
                String sql = String.format("ALTER TABLE `%s` %s COLUMN `%s` %s%s%s%s%s",
                    tableName,
                    handle,
                    fieldName,
                    type,
                    (notNull ? " NOT NULL" : ""),
                    (defaultValue ? String.format(" default '%s'", field.getAnnotation(DefaultValue.class).value()) : ""),
                    (autoIncrement ? " AUTO_INCREMENT" : ""),
                    (comment ? String.format(" COMMENT '%s'", field.getAnnotation(Comment.class).value()) : ""));
                
                //DROP比较特殊一些
                if (AlterTableEnum.DROP.name().equals(handle))
                {
                    sql = String.format("ALTER TABLE `%s` %s COLUMN `%s`", tableName, handle, fieldName);
                }
                
                System.out.println(String.format("即将执行DML语句：%s", sql));
                execute(sql);
            }
        }
    }
    
    @Override
    public boolean createTable(Class<?> tableClazz)
        throws SQLException
    {
        boolean done = checkTableExists(tableClazz);//建表任务是否已经完成
        if (!done)
        {
            //如果还没有完成
            
            //获取表名称
            String tableName = TableUtils.getTableName(tableClazz);
            //遍历字段，根据字段去建表
            Field[] fields = ClassKit.getAllDeclaredFieldsParentClassFirst(tableClazz);
            List<String> fieldSentences = new ArrayList<>();//字段语句列表
            List<String> indexStrList = new ArrayList<>();
            //复合索引的字段名列表
            List<String> multipleIndexFieldNames = new ArrayList<>();
            List<Boolean> multipleIndexFieldIsLongtext = new ArrayList<>();
            boolean tableComment = tableClazz.isAnnotationPresent(Comment.class);
            
            //主键字段列表
            List<String> primaryKeyFields = new ArrayList<>();
            Integer autoIncrementStartValue = null;//自增长的默认值
            
            for (Field field : fields)
            {
                field.setAccessible(true);
                String fieldName = field.getName();
                //考虑到联合主键的情况
                boolean primaryKey = field.isAnnotationPresent(Id.class);
                boolean autoIncrement = field.isAnnotationPresent(AutoIncrement.class);
                boolean notNull = field.isAnnotationPresent(NotNull.class);
                boolean comment = field.isAnnotationPresent(Comment.class);
                boolean varchar = field.isAnnotationPresent(Varchar.class);
                boolean defaultValue = field.isAnnotationPresent(DefaultValue.class);
                
                String type = "";
                if (field.getType() == int.class || field.getType() == Integer.class)
                {
                    type = "int";
                }
                else if (field.getType() == String.class)
                {
                    type = "longtext";
                    if (varchar)
                    {
                        //                        type =  VARCHAR(100) 
                        type = String.format("varchar(%d)", field.getAnnotation(Varchar.class).value());
                    }
                }
                else if (field.getType() == byte[].class)
                {
                    type = "blob";
                }
                else if (field.getType() == Date.class)
                {
                    type = "datetime";
                }
                else
                {
                    type = "longtext";
                }
                fieldSentences.add(String.format("`%s` %s%s%s%s%s",
                    fieldName,
                    type,
                    (notNull ? " NOT NULL" : ""),
                    //                    (primaryKey ? " PRIMARY KEY" : ""), //主键挪到底下统一声明
                    (defaultValue ? String.format(" default '%s'", field.getAnnotation(DefaultValue.class).value()) : ""),
                    (autoIncrement ? " AUTO_INCREMENT" : ""),
                    (comment ? String.format(" COMMENT '%s'", field.getAnnotation(Comment.class).value()) : "")));
                boolean index = field.isAnnotationPresent(Index.class);//是否是索引
                if (index)
                {
                    Index indexAnno = field.getAnnotation(Index.class);
                    boolean multiple = indexAnno.multiple();
                    if (!multiple)
                    {
                        //        CREATE FULLTEXT INDEX idx_node ON message(content)
                        //        CREATE FULLTEXT INDEX idx_node2 ON message(content,node)
                        //        CREATE FULLTEXT INDEX idx_node22 ON message(content,node,sent) 
                        //        CREATE          INDEX idx_node222 ON message(createtime) 
                        indexStrList.add(String.format("CREATE %s INDEX %s_%s_idx ON %s(%s)",
                            ("longtext".equals(type) ? "FULLTEXT" : ""),
                            tableName,
                            fieldName,
                            tableName,
                            fieldName));
                    }
                    else
                    {
                        multipleIndexFieldNames.add(fieldName);
                        multipleIndexFieldIsLongtext.add("longtext".equals(type) ? true : false);
                    }
                }
                if (primaryKey)
                {
                    primaryKeyFields.add(String.format("`%s`", fieldName));
                }
                if (autoIncrement)
                {
                    autoIncrementStartValue = field.getAnnotation(AutoIncrement.class).startValue();
                }
            }
            //将字段语句列表拼接成一个完整的语句
            String fieldsJoinStr = StringUtils.join(fieldSentences, ",");
            //拼接主键声明字段列表
            if (Lists.isNotEmpty(primaryKeyFields))
            {
                fieldsJoinStr = String.format("%s, PRIMARY KEY (%s)", fieldsJoinStr, StringUtils.join(primaryKeyFields, ","));
            }
            // COMMENT='商品表'
            String sql = String.format("CREATE TABLE IF NOT EXISTS `%s` (%s) ENGINE=INNODB %s DEFAULT CHARSET=utf8%s",
                tableName,
                fieldsJoinStr,
                (autoIncrementStartValue == null ? "" : String.format("AUTO_INCREMENT=%d", autoIncrementStartValue)),
                (tableComment ? String.format(" COMMENT '%s'", tableClazz.getAnnotation(Comment.class).value()) : ""));
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
            
            //如果是复合索引
            if (Lists.isNotEmpty(multipleIndexFieldNames))
            {
                boolean allTheSame = true;//一开始，假设所有的布尔值都相等
                boolean b0 = multipleIndexFieldIsLongtext.get(0);
                for (Boolean b : multipleIndexFieldIsLongtext)
                {
                    if (b != b0)
                    {
                        allTheSame = false;
                    }
                }
                if (allTheSame)
                {
                    //此处的复合索引，可能是FULLTEXT类型（假如字段是longtext类型的话），也可能不是
                    String indexStr = String.format("CREATE %s INDEX %s_multi_idx ON %s(%s)",
                        (b0 ? "FULLTEXT" : ""),
                        tableName,
                        tableName,
                        StringUtils.join(multipleIndexFieldNames, ","));
                    System.out.println(String.format("即将执行复合索引语句：%s", indexStr));
                    execute(indexStr);
                }
                else
                {
                    Logs.e(String.format("标记了复合索引@Index的多个字段的类型不完全一致，因此无法创建复合索引！"));
                }
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
                fieldSentences.add(String.format("`%s` longtext", columnName));
            }
            
            //将字段语句列表拼接成一个完整的语句
            String fieldsJoinStr = StringUtils.join(fieldSentences, ",");
            // COMMENT='商品表'
            String sql = String.format("CREATE TABLE IF NOT EXISTS `%s` (%s) ENGINE=INNODB DEFAULT CHARSET=utf8",
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
        String sql =
            String.format("SELECT COUNT(1) count FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA='%s' AND TABLE_NAME='%s'", DbKit.SCHEMA_NAME, tableName);
        return count(sql) > 0;
    }
    
    @Override
    public boolean checkTableExists(Class<?> tableClazz)
        throws SQLException
    {
        //获取表名称
        String tableName = TableUtils.getTableName(tableClazz);
        String sql =
            String.format("SELECT COUNT(1) count FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA='%s' AND TABLE_NAME='%s'", DbKit.SCHEMA_NAME, tableName);
        return count(sql) > 0;
    }
    
    @Override
    public boolean checkTableExists(String tableName)
        throws SQLException
    {
        String sql =
            String.format("SELECT COUNT(1) count FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA='%s' AND TABLE_NAME='%s'", DbKit.SCHEMA_NAME, tableName);
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
        String sql = String.format("TRUNCATE TABLE %s", tableName);
        //        System.out.println(String.format("即将执行：%s", sql));
        return execute(sql);
    }
    
    @Override
    public void insertAndGetAutoIncr(Object entity)
        throws SQLException
    {
        insertAndGetAutoIncr(entity, TableUtils.getIdColumnName(entity));
    }
    
    @Override
    public void insertAndGetAutoIncr(Object entity, String keyPropertyFieldName)
        throws SQLException
    {
        insert(entity);
        //查询上次插入的那个Id的值
        String id = ((Object)findValue("SELECT LAST_INSERT_ID()")).toString();
        try
        {
            Class<?> clazz = entity.getClass().getDeclaredField(keyPropertyFieldName).getType();
            if (clazz == int.class || clazz == Integer.class)
            {
                ClassKit.setField(entity, keyPropertyFieldName, Integer.valueOf(id));
            }
            else if (clazz == long.class || clazz == Long.class)
            {
                ClassKit.setField(entity, keyPropertyFieldName, Long.valueOf(id));
            }
            else
            {
                throw new UnsupportedOperationException(String.format("不支持的主键自增长类型：%s", clazz.getName()));
            }
        }
        catch (NoSuchFieldException | SecurityException e)
        {
            e.printStackTrace();
        }
    }
}
