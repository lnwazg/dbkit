package com.lnwazg.dbkit.jdbc.impl.ext;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
import com.lnwazg.dbkit.utils.TableUtils;
import com.lnwazg.kit.list.Lists;
import com.lnwazg.kit.log.Logs;
import com.lnwazg.kit.reflect.ClassKit;

/**
 * Oracle Jdbc的实现
 */
public class OracleJdbcSupport extends ConnectionManagerImpl implements MyJdbc
{
    public OracleJdbcSupport(DataSource dataSource)
    {
        super(dataSource);
    }
    
    public OracleJdbcSupport(DataSource dataSource, String schemaName)
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
                type = "number";
            }
            else if (field.getType() == String.class)
            {
                type = "varchar2(4000)";
                if (varchar)
                {
                    type = String.format("varchar2(%d)", field.getAnnotation(Varchar.class).value());
                }
            }
            else if (field.getType() == byte[].class)
            {
                type = "blob";
            }
            else if (field.getType() == Date.class)
            {
                type = "date";
            }
            else
            {
                type = "varchar2(4000)";
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
                String sql = String.format("ALTER TABLE %s %s COLUMN %s %s%s%s",
                    tableName,
                    handle,
                    fieldName,
                    type,
                    (notNull ? " NOT NULL" : ""),
                    (defaultValue ? String.format(" default '%s'", field.getAnnotation(DefaultValue.class).value()) : ""));
                
                //DROP比较特殊一些
                //如果是DROP操作，那么需要特殊处理
                if (AlterTableEnum.DROP.name().equals(handle))
                {
                    sql = String.format("ALTER TABLE %s %s COLUMN %s", tableName, handle, fieldName);
                }
                System.out.println(String.format("即将执行DML语句：%s", sql));
                execute(sql);
                
                //注释
                if (comment)
                {
                    String commentSql = String.format("comment on column %s.%s is '%s'", tableName, fieldName, field.getAnnotation(Comment.class).value());
                    System.out.println(String.format("即将执行字段注释语句：%s", commentSql));
                    execute(commentSql);
                }
                
                //创建序列
                if (autoIncrement)
                {
                    //首先检查对应的序列是否存在
                    // select count(1) from user_sequences where sequence_name = 'SEQ_DBHASHMAPTABLE_ID'
                    String querySeqSql = String.format("select count(1) from user_sequences where sequence_name = '%s'", String.format("SEQ_%s_%s", tableName, fieldName).toUpperCase());
                    System.out.println(String.format("即将执行检查序列是否存在语句：%s", querySeqSql));
                    if (count(querySeqSql) == 0)
                    {
                        String seqSql = String.format("create sequence SEQ_%s_%s", tableName, fieldName);
                        System.out.println(String.format("即将执行创建序列语句：%s", seqSql));
                        execute(seqSql);
                    }
                    else
                    {
                        System.out.println("序列已存在，忽略新建！");
                    }
                }
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
            Map<String, String> fieldCommentsMap = new LinkedHashMap<>();
            Integer autoIncrementStartValue = null;//自增长的默认值
            List<String> autoIncrementFields = new ArrayList<>();
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
                    type = "number";
                }
                else if (field.getType() == String.class)
                {
                    type = "varchar2(4000)";
                    if (varchar)
                    {
                        type = String.format("varchar2(%d)", field.getAnnotation(Varchar.class).value());
                    }
                }
                else if (field.getType() == byte[].class)
                {
                    type = "blob";
                }
                else if (field.getType() == Date.class)
                {
                    type = "date";
                }
                else
                {
                    type = "varchar2(4000)";
                }
                fieldSentences.add(String.format("%s %s%s%s",
                    fieldName,
                    type,
                    (notNull ? " NOT NULL" : ""),
                    (defaultValue ? String.format(" default '%s'", field.getAnnotation(DefaultValue.class).value()) : "")));
                
                boolean index = field.isAnnotationPresent(Index.class);//是否是索引
                if (index)
                {
                    Index indexAnno = field.getAnnotation(Index.class);
                    boolean multiple = indexAnno.multiple();
                    if (!multiple)
                    {
                        indexStrList.add(String.format("CREATE INDEX %s_%s_idx ON %s(%s)",
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
                    primaryKeyFields.add(String.format("%s", fieldName));
                }
                if (comment)
                {
                    fieldCommentsMap.put(fieldName, field.getAnnotation(Comment.class).value());
                }
                if (autoIncrement)
                {
                    autoIncrementStartValue = field.getAnnotation(AutoIncrement.class).startValue();
                    autoIncrementFields.add(fieldName);
                }
            }
            //将字段语句列表拼接成一个完整的语句
            String fieldsJoinStr = StringUtils.join(fieldSentences, ",");
            
            String sql = String.format("CREATE TABLE %s (%s)", tableName, fieldsJoinStr);
            System.out.println(String.format("即将执行：%s", sql));
            execute(sql);
            
            //创建主键
            if (Lists.isNotEmpty(primaryKeyFields))
            {
                //单主键
                //alter table course add constraint pk_scott_id primary key (id);
                //复合主键
                //alter table course add constraint pk_scott_sno_name primary key (sno,name);
                String pkComma = StringUtils.join(primaryKeyFields, ",");
                String pkUnder = StringUtils.join(primaryKeyFields, "_");
                if (pkUnder.length() > 15)
                {
                    //采用简写模式拼装
                    List<String> simplified = new ArrayList<>();
                    for (String primaryKeyField : primaryKeyFields)
                    {
                        simplified.add(primaryKeyField.substring(0, 2));
                    }
                    pkUnder = StringUtils.join(simplified, "_");
                }
                String pkSql = String.format("alter table %s add constraint PK_%s_%s primary key (%s)", tableName, tableName, pkUnder, pkComma);
                System.out.println(String.format("即将执行建主键语句：%s", pkSql));
                execute(pkSql);
            }
            
            //表和字段加注释
            if (tableComment)
            {
                //comment on table TEST is '测试表';
                String tableCommentContent = tableClazz.getAnnotation(Comment.class).value();
                String commentSql = String.format("comment on table %s is '%s'", tableName, tableCommentContent);
                System.out.println(String.format("即将执行表注释语句：%s", commentSql));
                execute(commentSql);
            }
            if (fieldCommentsMap.size() > 0)
            {
                //comment on column TEST.ID is '主键';
                //comment on column TEST.NAME is '名称';
                for (String fieldName : fieldCommentsMap.keySet())
                {
                    String commentSql = String.format("comment on column %s.%s is '%s'", tableName, fieldName, fieldCommentsMap.get(fieldName));
                    System.out.println(String.format("即将执行字段注释语句：%s", commentSql));
                    execute(commentSql);
                }
            }
            //为自增长字段建立序列
            //create sequence SEQ_DEPT_ID
            if (autoIncrementFields.size() > 0)
            {
                for (String aiFieldName : autoIncrementFields)
                {
                    //首先检查对应的序列是否存在
                    // select count(1) from user_sequences where sequence_name = 'SEQ_DBHASHMAPTABLE_ID'
                    String querySeqSql = String.format("select count(1) from user_sequences where sequence_name = '%s'", String.format("SEQ_%s_%s", tableName, aiFieldName).toUpperCase());
                    System.out.println(String.format("即将执行检查序列是否存在语句：%s", querySeqSql));
                    if (count(querySeqSql) == 0)
                    {
                        String seqSql = String.format("create sequence SEQ_%s_%s", tableName, aiFieldName);
                        System.out.println(String.format("即将执行创建序列语句：%s", seqSql));
                        execute(seqSql);
                    }
                    else
                    {
                        System.out.println("序列已存在，忽略新建！");
                    }
                }
            }
            
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
        return false;
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
                fieldSentences.add(String.format("%s varchar2(4000)", columnName));
            }
            
            //将字段语句列表拼接成一个完整的语句
            String fieldsJoinStr = StringUtils.join(fieldSentences, ",");
            // COMMENT='商品表'
            String sql = String.format("CREATE TABLE %s (%s)",
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
            String.format("select count(1) from user_tables where table_name = '%s'", tableName.toUpperCase());
        return count(sql) > 0;
    }
    
    @Override
    public boolean checkTableExists(Class<?> tableClazz)
        throws SQLException
    {
        //获取表名称
        String tableName = TableUtils.getTableName(tableClazz);
        String sql =
            String.format("select count(1) from user_tables where table_name = '%s'", tableName.toUpperCase());
        return count(sql) > 0;
    }
    
    @Override
    public boolean checkTableExists(String tableName)
        throws SQLException
    {
        String sql =
            String.format("select count(1) from user_tables where table_name = '%s'", tableName.toUpperCase());
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
        System.out.println(String.format("即将执行：%s", sql));
        return execute(sql);
    }
    
    @Override
    public void insertAndGetAutoIncr(Object entity)
        throws SQLException
    {
        insertAndGetAutoIncr(entity, TableUtils.getIdColumnName(entity));
    }
    
    /**
     * 对于oracle要重写insert
     */
    public int insert(Object entity)
        throws SQLException
    {
        Class<?> entityClass = entity.getClass();
        String table = TableUtils.getTableName(entityClass);
        Map<String, Object> columnDataMap = TableUtils.obj2Map(entity);
        List<String> dateTypeFields = TableUtils.getDateTypeFields(entityClass);
        //对于oracle，需要增加定制：从序列取值
        List<String> autoIncrementColumns = TableUtils.getAutoIncrementColumnNames(entityClass);
        if (Lists.isNotEmpty(autoIncrementColumns))
        {
            for (String aiColumn : autoIncrementColumns)
            {
                String getSeqValueSql = String.format("select SEQ_%s_%s.nextval from dual", table.toUpperCase(), aiColumn.toUpperCase());
                System.out.println(String.format("即将执行查序列语句：%s", getSeqValueSql));
                int seqValue = count(getSeqValueSql);
                System.out.println(String.format("查序列结果为：%s", seqValue));
                columnDataMap.put(aiColumn, seqValue);
            }
        }
        return insert(table, columnDataMap, dateTypeFields);
    }
    
    public int insert(String table, Map<String, Object> columnDataMap, List<String> dateTypeFields)
        throws SQLException
    {
        List<String> columnNames = new ArrayList<String>(columnDataMap.keySet());
        List<Object> columnValues = new LinkedList<Object>();
        for (String columnName : columnNames)
        {
            columnValues.add(columnDataMap.get(columnName));
        }
        String sql = generateInsertSQL(table, columnNames, dateTypeFields);
        return update(sql, columnValues);
    }
    
    /**
     * 为Oracle定制日期类型的操作
     * @author nan.li
     * @param table
     * @param columnNames
     * @param dateTypeFields
     * @return
     */
    public String generateInsertSQL(String table, List<String> columnNames, List<String> dateTypeFields)
    {
        List<String> params = new ArrayList<>();
        for (int i = 0; i < columnNames.size(); i++)
        {
            if (dateTypeFields.contains(columnNames.get(i)))
            {
                params.add("to_date(?,'YYYY-MM-DD HH24:MI:SS')");
            }
            else
            {
                params.add("?");
            }
        }
        return new StringBuffer("insert into ").append(table)
            .append(" (")
            .append(StringUtils.join(columnNames, ", "))
            .append(") values (")
            .append(StringUtils.join(params, ", "))
            .append(")")
            .toString();
    }
    
    @Override
    public void insertAndGetAutoIncr(Object entity, String keyPropertyFieldName)
        throws SQLException
    {
        insert(entity);
        //查询上次插入的那个Id的值
        Class<?> entityClass = entity.getClass();
        String table = TableUtils.getTableName(entityClass);
        String id = ((Object)findValue(String.format("select SEQ_%s_%s.currval from dual", table.toUpperCase(), keyPropertyFieldName.toUpperCase()))).toString();
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
