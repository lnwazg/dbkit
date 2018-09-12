package com.lnwazg.dbkit.vo;

import java.util.Collection;

/**
 * 批量提交的对象
 * @author nan.li
 * @version 2016年5月13日
 */
public class BatchObj
{
    /**
     * 批量提交的sql列表
     */
    Collection<String> cols;
    
    /**
     * 批量提交的参数表（二维表）
     */
    Collection<Collection<?>> args;
    
    public BatchObj(Collection<String> cols, Collection<Collection<?>> args)
    {
        super();
        this.cols = cols;
        this.args = args;
    }
    
    public Collection<String> getCols()
    {
        return cols;
    }
    
    public void setCols(Collection<String> cols)
    {
        this.cols = cols;
    }
    
    public Collection<Collection<?>> getArgs()
    {
        return args;
    }
    
    public void setArgs(Collection<Collection<?>> args)
    {
        this.args = args;
    }
}
