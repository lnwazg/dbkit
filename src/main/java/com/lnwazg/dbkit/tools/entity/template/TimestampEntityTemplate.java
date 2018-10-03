package com.lnwazg.dbkit.tools.entity.template;

import java.util.Date;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.lnwazg.dbkit.anno.entity.Comment;
import com.lnwazg.dbkit.anno.field.SubClassFieldsFirst;

@Comment("时间戳模板表")
@SubClassFieldsFirst
public class TimestampEntityTemplate
{
    @Comment("创建时间")
    Date createTime;
    
    @Comment("上次更新时间")
    Date updateTime;
    
    public TimestampEntityTemplate()
    {
        createTime = new Date();
        updateTime = new Date();
    }
    
    public Date getCreateTime()
    {
        return createTime;
    }
    
    public TimestampEntityTemplate setCreateTime(Date createTime)
    {
        this.createTime = createTime;
        return this;
    }
    
    public Date getUpdateTime()
    {
        return updateTime;
    }
    
    public TimestampEntityTemplate setUpdateTime(Date updateTime)
    {
        this.updateTime = updateTime;
        return this;
    }
    
    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.JSON_STYLE);
    }
}
