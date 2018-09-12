package com.lnwazg.dbkit.tools.bi.bean;

import java.util.List;

/**
 * 视图
 * @author nan.li
 * @version 2017年12月20日
 */
public class BiView
{
    String viewName;
    
    String refDsName;
    
    String sql;
    
    String columnSplitter;
    
    /**
     * 具体的操作类型
     */
    private String action;
    
    /**
     * 要操作的表列表，多个之间以逗号分隔
     */
    private String tables;
    
    List<String> columns;
    
    public String getColumnSplitter()
    {
        return columnSplitter;
    }
    
    public BiView setColumnSplitter(String columnSplitter)
    {
        this.columnSplitter = columnSplitter;
        return this;
    }
    
    public List<String> getColumns()
    {
        return columns;
    }
    
    public BiView setColumns(List<String> columns)
    {
        this.columns = columns;
        return this;
    }
    
    public String getViewName()
    {
        return viewName;
    }
    
    public BiView setViewName(String viewName)
    {
        this.viewName = viewName;
        return this;
    }
    
    public String getRefDsName()
    {
        return refDsName;
    }
    
    public BiView setRefDsName(String refDsName)
    {
        this.refDsName = refDsName;
        return this;
    }
    
    public String getSql()
    {
        return sql;
    }
    
    public BiView setSql(String sql)
    {
        this.sql = sql;
        return this;
    }
    
    public String getAction()
    {
        return action;
    }
    
    public BiView setAction(String action)
    {
        this.action = action;
        return this;
    }

    public String getTables()
    {
        return tables;
    }

    public BiView setTables(String tables)
    {
        this.tables = tables;
        return this;
    }
    
}
