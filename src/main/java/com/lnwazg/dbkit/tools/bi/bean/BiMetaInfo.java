package com.lnwazg.dbkit.tools.bi.bean;

import java.util.List;

import com.lnwazg.kit.gson.GsonKit;

/**
 * BI元数据信息
 * @author nan.li
 * @version 2017年12月20日
 */
public class BiMetaInfo
{
    /**
     * 数据源列表
     */
    List<BiDataSource> datasources;
    
    /**
     * 结果集所使用的数据源
     */
    private String resultDsRef;
    
    /**
     * 视图列表
     */
    List<BiView> views;
    
    public List<BiDataSource> getDatasources()
    {
        return datasources;
    }
    
    public BiMetaInfo setDatasources(List<BiDataSource> datasources)
    {
        this.datasources = datasources;
        return this;
    }
    
    public List<BiView> getViews()
    {
        return views;
    }
    
    public BiMetaInfo setViews(List<BiView> views)
    {
        this.views = views;
        return this;
    }
    
    public String getResultDsRef()
    {
        return resultDsRef;
    }
    
    public BiMetaInfo setResultDsRef(String resultDsRef)
    {
        this.resultDsRef = resultDsRef;
        return this;
    }
    
    @Override
    public String toString()
    {
        return GsonKit.prettyGson.toJson(this);
    }
    
}
