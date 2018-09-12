package com.lnwazg.dbkit.tools.security.entity;

import java.util.Date;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.lnwazg.dbkit.anno.entity.AutoIncrement;
import com.lnwazg.dbkit.anno.entity.Comment;
import com.lnwazg.dbkit.anno.entity.Id;
import com.lnwazg.dbkit.anno.entity.Table;
import com.lnwazg.dbkit.anno.entity.Varchar;

@Comment("权限表")
@Table("S_PERMISSION")
public class Permission
{
    @Id
    @AutoIncrement
    @Comment("主键")
    Integer id;
    
    @Varchar(500)
    @Comment("权限名称")
    String permissionName;
    
    @Comment("创建时间")
    Date createTime;
    
    public Integer getId()
    {
        return id;
    }
    
    public Permission setId(Integer id)
    {
        this.id = id;
        return this;
    }
    
    public String getPermissionName()
    {
        return permissionName;
    }
    
    public Permission setPermissionName(String permissionName)
    {
        this.permissionName = permissionName;
        return this;
    }
    
    public Date getCreateTime()
    {
        return createTime;
    }
    
    public Permission setCreateTime(Date createTime)
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
