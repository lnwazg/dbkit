package com.lnwazg.dbkit.tools.db.collection.entity;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.lnwazg.dbkit.anno.entity.AutoIncrement;
import com.lnwazg.dbkit.anno.entity.Comment;
import com.lnwazg.dbkit.anno.entity.Id;

/**
 * 将HashMap的数据存入数据库
 * @author nan.li
 * @version 2017年8月4日
 */
public class DbCollectionTable
{
    @Id
    @AutoIncrement
    @Comment("主键")
    Integer id;
    
    @Comment("值")
    String strValue;
    
    public Integer getId()
    {
        return id;
    }
    
    public void setId(Integer id)
    {
        this.id = id;
    }
    
    public String getStrValue()
    {
        return strValue;
    }
    
    public void setStrValue(String strValue)
    {
        this.strValue = strValue;
    }
    
    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.JSON_STYLE);
    }
}
