package com.lnwazg.dbkit.tools.graphtool.vo;

public class TableData
{
    String tableName;
    
    String tableComment;
    
    public String getTableName()
    {
        return tableName;
    }
    
    public TableData setTableName(String tableName)
    {
        this.tableName = tableName;
        return this;
    }
    
    public String getTableComment()
    {
        return tableComment;
    }
    
    public TableData setTableComment(String tableComment)
    {
        this.tableComment = tableComment;
        return this;
    }
    
    @Override
    public String toString()
    {
        return "TableData [tableName=" + tableName + ", tableComment=" + tableComment + "]";
    }
}
