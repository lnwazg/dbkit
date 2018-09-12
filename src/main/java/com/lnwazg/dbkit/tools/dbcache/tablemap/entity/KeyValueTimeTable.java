package com.lnwazg.dbkit.tools.dbcache.tablemap.entity;

import java.util.Date;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.lnwazg.dbkit.anno.entity.AutoIncrement;
import com.lnwazg.dbkit.anno.entity.Comment;
import com.lnwazg.dbkit.anno.entity.Id;
import com.lnwazg.dbkit.anno.entity.Varchar;

/**
 * 键值对跟时间戳的表<br>
 * 将session保存到数据库里，可以一劳永逸地解决session重启失效的问题！
 * @author nan.li
 * @version 2017年4月8日
 */
public class KeyValueTimeTable
{
    @Id
    @AutoIncrement
    @Comment("主键")
    Integer id;
    
    @Varchar(100)
    @Comment("键")
    String strKey;
    
    @Comment("值")
    String strValue;
    
    @Comment("创建时间")
    Date createTime;
    
    public Integer getId()
    {
        return id;
    }
    
    public void setId(Integer id)
    {
        this.id = id;
    }
    
    public String getStrKey()
    {
        return strKey;
    }
    
    public void setStrKey(String strKey)
    {
        this.strKey = strKey;
    }
    
    public String getStrValue()
    {
        return strValue;
    }
    
    public void setStrValue(String strValue)
    {
        this.strValue = strValue;
    }
    
    public Date getCreateTime()
    {
        return createTime;
    }
    
    public void setCreateTime(Date createTime)
    {
        this.createTime = createTime;
    }
    
    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.JSON_STYLE);
    }
    
}
