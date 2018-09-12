package com.lnwazg.dbkit.tools.security.entity;

import java.util.Date;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.lnwazg.dbkit.anno.entity.Comment;
import com.lnwazg.dbkit.anno.entity.Id;
import com.lnwazg.dbkit.anno.entity.Table;

@Comment("权限角色关联表，一个角色可能对应多个权限")
@Table("S_ROLE_PERMISSION")
public class RolePermission
{
    @Id
    @Comment("角色Id")
    Integer roleId;
    
    @Id
    @Comment("权限Id")
    Integer permissionId;
    
    @Comment("创建时间")
    Date createTime;
    
    public Integer getRoleId()
    {
        return roleId;
    }
    
    public RolePermission setRoleId(Integer roleId)
    {
        this.roleId = roleId;
        return this;
    }
    
    public Integer getPermissionId()
    {
        return permissionId;
    }
    
    public RolePermission setPermissionId(Integer permissionId)
    {
        this.permissionId = permissionId;
        return this;
    }
    
    public Date getCreateTime()
    {
        return createTime;
    }
    
    public RolePermission setCreateTime(Date createTime)
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
