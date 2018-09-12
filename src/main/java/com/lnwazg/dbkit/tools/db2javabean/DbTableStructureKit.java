package com.lnwazg.dbkit.tools.db2javabean;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang3.CharEncoding;
import org.apache.commons.lang3.StringUtils;

import com.lnwazg.dbkit.jdbc.MyJdbc;
import com.lnwazg.dbkit.tools.graphtool.bean.GField;
import com.lnwazg.dbkit.tools.graphtool.bean.GTable;
import com.lnwazg.dbkit.tools.graphtool.vo.ColumnData;
import com.lnwazg.dbkit.tools.graphtool.vo.TableData;
import com.lnwazg.dbkit.utils.DbKit;
import com.lnwazg.kit.freemarker.FreeMkKit;
import com.lnwazg.kit.log.Logs;
import com.lnwazg.kit.map.Maps;

import freemarker.template.Configuration;
import freemarker.template.Template;

/**
 * 数据库->EntityBean生成器<br>
 * 数据库表结构工具类
 * @author nan.li
 * @version 2017年5月7日
 */
public class DbTableStructureKit
{
    /**
     * 获取表结构的元数据列表<br>
     * 这种方法目前仅支持mysql
     * @author nan.li
     * @param jdbc
     * @return
     * @throws SQLException
     */
    public static List<GTable> getTableList(MyJdbc jdbc)
        throws SQLException
    {
        List<ColumnData> columnDatas = jdbc.list(ColumnData.class,
            String.format(
                "SELECT table_name tableName,column_name columnName, CONCAT(IFNULL(data_type,''),'(',IFNULL(CHARACTER_MAXIMUM_LENGTH,''),')') dataType,column_comment columnComment FROM information_schema.columns WHERE table_schema ='%s'",
                DbKit.SCHEMA_NAME));
        //表主键列表
        List<Map<String, Object>> tableKeyFieldList = jdbc.listMap(String.format("SELECT table_name,column_name FROM information_schema.`statistics` WHERE table_schema='%s' AND index_name='PRIMARY'",
            DbKit.SCHEMA_NAME));
        Map<String, List<GField>> map = new TreeMap<>();
        for (ColumnData columnData : columnDatas)
        {
            String tableName = columnData.getTableName();
            GField gField =
                new GField().setFieldName(columnData.getColumnName()).setFieldType(columnData.getDataType()).setComment(columnData.getColumnComment());
                
            for (Map<String, Object> m : tableKeyFieldList)
            {
                //若表名和主键名一致
                if (tableName.equals(m.get("table_name").toString()) && gField.getFieldName().equals(m.get("column_name").toString()))
                {
                    gField.setPk(true);
                }
            }
            if (!map.containsKey(tableName))
            {
                map.put(tableName, new ArrayList<>());
            }
            map.get(tableName).add(gField);
        }
        
        List<GTable> tableList = new ArrayList<>();
        List<TableData> tables = jdbc.list(TableData.class,
            String.format("SELECT table_name tableName, table_comment  tableComment FROM information_schema.`TABLES` WHERE table_schema='%s'",
                DbKit.SCHEMA_NAME));
        Map<String, String> tableCommentMap = new HashMap<>();
        for (TableData tableData : tables)
        {
            String tableComment = tableData.getTableComment();
            if (StringUtils.isEmpty(tableComment))
            {
                tableComment = "";
            }
            else
            {
                //comment截取掉无关的数据
                int index = tableComment.indexOf(";");
                if (index != -1)
                {
                    tableComment = tableComment.substring(0, index).trim();
                }
                else
                {
                    //没查找到，那么就不应该展示内容
                    if (tableComment.startsWith("InnoDB"))
                    {
                        tableComment = "";
                    }
                }
            }
            tableCommentMap.put(tableData.getTableName(), tableComment);
        }
        for (String tableName : map.keySet())
        {
            tableList.add(new GTable().setTableName(tableName).setTableComment(tableCommentMap.get(tableName)).setFields(map.get(tableName)));
        }
        return tableList;
    }
    
    /**
     * 根据表结构反向生成Bean
     * @author nan.li
     * @param codePath
     * @param packageName
     * @param configPath
     */
    public static void generateBeanFromDb(String codePath, String packageName, String configPath)
    {
        MyJdbc jdbc = DbKit.getJdbc(configPath);
        generateBeanFromDb(codePath, packageName, jdbc);
    }
    
    /**
     * 根据表结构反向生成EntityBean
     * @author nan.li
     * @param codePath
     * @param packageName
     * @param jdbc
     */
    public static void generateBeanFromDb(String codePath, String packageName, MyJdbc jdbc)
    {
        try
        {
            File targetPathFile = new File(codePath);
            if (!targetPathFile.exists())
            {
                targetPathFile.mkdirs();
            }
            List<GTable> tableList = DbTableStructureKit.getTableList(jdbc);
            for (GTable gTable : tableList)
            {
                generateBeanByTable(codePath, packageName, gTable);
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }
    
    /**
     * 根据表结构描述去生成对应的JavaBean
     * @author nan.li
     * @param codePath
     * @param packageName
     * @param gTable
     */
    private static void generateBeanByTable(String codePath, String packageName, GTable gTable)
    {
        try
        {
            //创建一个合适的Configration对象  
            Configuration configuration = FreeMkKit.getConfigurationByClass(DbTableStructureKit.class, "");
            Template template = configuration.getTemplate("template.txt");
            
            String tableName = gTable.getTableName();//business
            String className = StringUtils.capitalize(tableName);//Business
            String tableAnno = String.format("@Table(\"%s\")", tableName);
            String comment = StringUtils.isNotEmpty(gTable.getTableComment()) ? String.format("@Comment(\"%s\")", StringUtils.trim(gTable.getTableComment().replace("\n", "").replace("\r", ""))) : "";//商户表
            StringBuilder fieldsBuilder = new StringBuilder();
            StringBuilder getSetBuilder = new StringBuilder();
            for (GField field : gTable.getFields())
            {
                String fieldName = field.getFieldName();//id
                String fieldType = field.getFieldType();//int
                String fieldComment = StringUtils.isNotEmpty(field.getComment()) ? StringUtils.trim(field.getComment().replace("\n", "").replace("\r", "")) : "";//主键
                String javaType = getJavaType(fieldType);//Integer
                String fieldNameCap = StringUtils.capitalize(fieldName);//Id
                
                //    @Id
                //    @Comment("主键")
                //    Integer id;
                if (field.isPk())
                {
                    fieldsBuilder.append("\t").append("@Id").append("\r\n");
                }
                if (StringUtils.isNotEmpty(fieldComment))
                {
                    fieldsBuilder.append("\t").append("@Comment(\"").append(fieldComment).append("\")").append("\r\n");
                }
                fieldsBuilder.append("\t").append(javaType).append(" ").append(fieldName).append(";").append("\r\n");
                fieldsBuilder.append("\r\n");
                
                //                public Integer getId()
                //                {
                //                    return id;
                //                }
                //                
                //                public Business setId(Integer id)
                //                {
                //                    this.id = id;
                //                    return this;
                //                }
                getSetBuilder.append("\t").append("public ").append(javaType).append(" get").append(fieldNameCap).append("()").append("\r\n");
                getSetBuilder.append("\t").append("{").append("\r\n");
                getSetBuilder.append("\t").append("\t").append("return ").append(fieldName).append(";").append("\r\n");
                getSetBuilder.append("\t").append("}").append("\r\n");
                getSetBuilder.append("\r\n");
                getSetBuilder.append("\t").append("public ").append(className).append(" set").append(fieldNameCap).append("(").append(javaType).append(" ").append(fieldName).append(")").append("\r\n");
                getSetBuilder.append("\t").append("{").append("\r\n");
                getSetBuilder.append("\t").append("\t").append("this.").append(fieldName).append(" = ").append(fieldName).append(";").append("\r\n");
                getSetBuilder.append("\t").append("\t").append("return this;").append("\r\n");
                getSetBuilder.append("\t").append("}").append("\r\n");
                getSetBuilder.append("\r\n");
            }
            
            String fields = fieldsBuilder.toString();
            String getSet = getSetBuilder.toString();
            
            template.process(Maps.asMap("package",
                packageName,
                "ClassName",
                className,
                "tableAnno",
                tableAnno,
                "comment",
                comment,
                "fields",
                fields,
                "getSet",
                getSet),
                new OutputStreamWriter(new FileOutputStream(new File(codePath, String.format("%s.java", className))), CharEncoding.UTF_8));
                
            Logs.i(String.format("在 %s 目录生成实体类  %s 成功！", codePath, className));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
    /**
     * 根据数据库类型去获取对应的Java类型
     * @author nan.li
     * @param fieldType
     * @return
     */
    private static String getJavaType(String fieldType)
    {
        if (fieldType.startsWith("varchar") || fieldType.startsWith("longtext") || fieldType.startsWith("char") || fieldType.startsWith("mediumtext"))
        {
            return "String";
        }
        else if (fieldType.startsWith("int"))
        {
            return "Integer";
        }
        else if (fieldType.startsWith("date") || fieldType.startsWith("timestamp"))
        {
            return "Date";
        }
        else if (fieldType.startsWith("decimal"))
        {
            return "Integer";
        }
        else if (fieldType.startsWith("blob"))
        {
            return "byte[]";
        }
        else
        {
            Logs.i("fieldType: " + fieldType);
        }
        return null;
    }
    
}
