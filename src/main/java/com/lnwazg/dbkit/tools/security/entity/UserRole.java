package com.lnwazg.dbkit.tools.security.entity;

import java.util.Date;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.lnwazg.dbkit.anno.entity.Comment;
import com.lnwazg.dbkit.anno.entity.Id;
import com.lnwazg.dbkit.anno.entity.Table;
import com.lnwazg.dbkit.anno.entity.Varchar;

@Comment("用户角色关联表，一个用户可能对应多个角色")
@Table("S_USER_ROLE")
public class UserRole
{
    @Id
    @Varchar(50)
    @Comment("用户表的名称")
    String userTableName;
    
    @Id
    @Comment("用户Id")
    Integer userId;
    
    @Id
    @Comment("角色Id")
    Integer roleId;
    
    @Comment("创建时间")
    Date createTime;
    
    public Integer getRoleId()
    {
        return roleId;
    }
    
    public UserRole setRoleId(Integer roleId)
    {
        this.roleId = roleId;
        return this;
    }
    
    public Date getCreateTime()
    {
        return createTime;
    }
    
    public UserRole setCreateTime(Date createTime)
    {
        this.createTime = createTime;
        return this;
    }
    
    public String getUserTableName()
    {
        return userTableName;
    }
    
    public UserRole setUserTableName(String userTableName)
    {
        this.userTableName = userTableName;
        return this;
    }
    
    public Integer getUserId()
    {
        return userId;
    }
    
    public UserRole setUserId(Integer userId)
    {
        this.userId = userId;
        return this;
    }
    
    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.JSON_STYLE);
    }
}
