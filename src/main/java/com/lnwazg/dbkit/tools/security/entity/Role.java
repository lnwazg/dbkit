package com.lnwazg.dbkit.tools.security.entity;

import java.util.Date;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.lnwazg.dbkit.anno.entity.AutoIncrement;
import com.lnwazg.dbkit.anno.entity.Comment;
import com.lnwazg.dbkit.anno.entity.Id;
import com.lnwazg.dbkit.anno.entity.Table;
import com.lnwazg.dbkit.anno.entity.Varchar;

@Comment("角色表")
@Table("S_ROLE")
public class Role
{
    @Id
    @AutoIncrement
    @Comment("主键")
    Integer id;
    
    @Varchar(500)
    @Comment("角色名称")
    String roleName;
    
    @Comment("创建时间")
    Date createTime;
    
    public Integer getId()
    {
        return id;
    }
    
    public Role setId(Integer id)
    {
        this.id = id;
        return this;
    }
    
    public String getRoleName()
    {
        return roleName;
    }
    
    public Role setRoleName(String roleName)
    {
        this.roleName = roleName;
        return this;
    }
    
    public Date getCreateTime()
    {
        return createTime;
    }
    
    public Role setCreateTime(Date createTime)
    {
        this.createTime = createTime;
        return this;
    }
    
    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.JSON_STYLE);
    }
}
