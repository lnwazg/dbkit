package com.lnwazg.dbkit.vo;

import java.util.Collection;

/**
 * SQL语句以及参数表
 * @author nan.li
 * @version 2017年5月7日
 */
public class SqlAndArgs
{
    private String sql;
    
    private Collection<?> args;
    
    public SqlAndArgs(String sql, Collection<?> args)
    {
        this.sql = sql;
        this.args = args;
    }
    
    public String getSql()
    {
        return sql;
    }
    
    public void setSql(String sql)
    {
        this.sql = sql;
    }
    
    public Collection<?> getArgs()
    {
        return args;
    }
    
    public void setArgs(Collection<?> args)
    {
        this.args = args;
    }
}
