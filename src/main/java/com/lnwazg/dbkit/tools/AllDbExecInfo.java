package com.lnwazg.dbkit.tools;

import java.sql.Statement;

/**
 * 所有的即将执行的sql的信息
 * @author nan.li
 * @version 2018年5月19日
 */
public class AllDbExecInfo
{
    /**
     * 待执行的sql明细
     */
    private String sqlDetail;
    
    /**
     * 待执行的statement对象
     */
    private Statement statement;
    
    public Statement getStatement()
    {
        return statement;
    }
    
    public AllDbExecInfo setStatement(Statement statement)
    {
        this.statement = statement;
        return this;
    }
    
    public String getSqlDetail()
    {
        return sqlDetail;
    }
    
    public AllDbExecInfo setSqlDetail(String sqlDetail)
    {
        this.sqlDetail = sqlDetail;
        return this;
    }
}
