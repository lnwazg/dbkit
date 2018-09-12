package com.lnwazg.dbkit.tools.graphtool.vo;

public class ColumnData
{
    String tableName;
    
    String columnName;
    
    String dataType;
    
    String columnComment;
    
    public String getTableName()
    {
        return tableName;
    }
    
    public ColumnData setTableName(String tableName)
    {
        this.tableName = tableName;
        return this;
    }
    
    public String getColumnName()
    {
        return columnName;
    }
    
    public ColumnData setColumnName(String columnName)
    {
        this.columnName = columnName;
        return this;
    }
    
    public String getDataType()
    {
        return dataType;
    }
    
    public ColumnData setDataType(String dataType)
    {
        this.dataType = dataType;
        return this;
    }
    
    public String getColumnComment()
    {
        return columnComment;
    }
    
    public ColumnData setColumnComment(String columnComment)
    {
        this.columnComment = columnComment;
        return this;
    }
    
    @Override
    public String toString()
    {
        return "ColumnData [tableName=" + tableName + ", columnName=" + columnName + ", dataType=" + dataType + ", columnComment=" + columnComment + "]";
    }
}
