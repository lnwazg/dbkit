package com.lnwazg.dbkit.tools.dbcache.tablemap.entity;

import com.lnwazg.dbkit.anno.entity.Comment;
import com.lnwazg.dbkit.anno.entity.Varchar;

@Comment("数据库配置信息表")
public class DbConfig extends KeyValueTimeTable
{
    @Comment("备注信息")
    @Varchar(100)
    private String remark;
    
    public String getRemark()
    {
        return remark;
    }
    
    public void setRemark(String remark)
    {
        this.remark = remark;
    }
}
