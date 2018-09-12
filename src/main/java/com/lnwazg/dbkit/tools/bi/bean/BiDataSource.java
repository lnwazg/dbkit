package com.lnwazg.dbkit.tools.bi.bean;

import com.lnwazg.dbkit.jdbc.MyJdbc;

/**
 * 数据源信息
 * @author nan.li
 * @version 2017年12月20日
 */
public class BiDataSource
{
    private String dsName;
    
    String url;
    
    String username;
    
    String password;
    
    private MyJdbc jdbc;
    
    public String getUrl()
    {
        return url;
    }
    
    public BiDataSource setUrl(String url)
    {
        this.url = url;
        return this;
    }
    
    public String getUsername()
    {
        return username;
    }
    
    public BiDataSource setUsername(String username)
    {
        this.username = username;
        return this;
    }
    
    public String getPassword()
    {
        return password;
    }
    
    public BiDataSource setPassword(String password)
    {
        this.password = password;
        return this;
    }
    
    public String getDsName()
    {
        return dsName;
    }
    
    public BiDataSource setDsName(String dsName)
    {
        this.dsName = dsName;
        return this;
    }
    
    public MyJdbc getJdbc()
    {
        return jdbc;
    }
    
    public BiDataSource setJdbc(MyJdbc jdbc)
    {
        this.jdbc = jdbc;
        return this;
    }
}
