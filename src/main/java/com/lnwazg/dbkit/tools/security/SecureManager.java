package com.lnwazg.dbkit.tools.security;

import java.sql.SQLException;
import java.util.Date;

import com.lnwazg.dbkit.jdbc.MyJdbc;
import com.lnwazg.dbkit.tools.security.entity.Permission;
import com.lnwazg.dbkit.tools.security.entity.Role;
import com.lnwazg.dbkit.tools.security.entity.RolePermission;
import com.lnwazg.dbkit.tools.security.entity.UserRole;

/**
 * 极简的权限验证框架，却拥有着不输于shiro的使用体验<br>
 * 权限表      权限角色关联表<br>
 * 角色表      角色用户关联表<br>
 * 这是一个shiro的极简的可替代方案
 * @author nan.li
 * @version 2017年5月9日
 */
public class SecureManager
{
    /**
     * 用户表的名称
     */
    private String userTableName;
    
    /**
     * 待使用的jdbc连接池
     */
    private MyJdbc jdbc;
    
    /**
     * 新建表之后是否需要初始化表数据
     */
    boolean initTableData = false;
    
    public SecureManager(String userTableName, MyJdbc jdbc)
    {
        this(userTableName, jdbc, false);
    }
    
    public SecureManager(String userTableName, MyJdbc jdbc, boolean initTableData)
    {
        this.userTableName = userTableName;
        this.jdbc = jdbc;
        this.initTableData = initTableData;
        initTables();
    }
    
    /**
     * 初始化跟权限控制相关的几张表
     * @author nan.li
     */
    private void initTables()
    {
        try
        {
            jdbc.createTable(UserRole.class);
            if (jdbc.createTable(Role.class))
            {
                if (initTableData)
                {
                    //自动建好几个角色，方便使用
                    //这几个角色作为模板来使用，随时可以修改删除
                    jdbc.insert(new Role().setRoleName("超级管理员").setCreateTime(new Date()));
                    jdbc.insert(new Role().setRoleName("管理员").setCreateTime(new Date()));
                    jdbc.insert(new Role().setRoleName("权限管理员").setCreateTime(new Date()));
                    jdbc.insert(new Role().setRoleName("用户").setCreateTime(new Date()));
                }
            }
            jdbc.createTable(RolePermission.class);
            jdbc.createTable(Permission.class);
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }
    
    /**
     * 某个人是否拥有某个角色
     * @author nan.li
     * @param roleName
     */
    public boolean hasRole(String roleName, String userId)
    {
        String sql = "SELECT * FROM S_USER_ROLE a " +
            "LEFT JOIN S_ROLE b ON a.roleId =b.id " +
            "WHERE a.userTableName=? AND a.userId =? AND b.roleName=?";
        try
        {
            return jdbc.listMap(sql, userTableName, userId, roleName).size() > 0;
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean hasRole(String roleName, Integer userId)
    {
        return hasRole(roleName, String.valueOf(userId));
    }
    
    /**
     * 某个人是否有某个权限（严格匹配）
     * @author nan.li
     * @param permissionName
     * @param userId
     */
    public boolean hasPermission(String permissionName, String userId)
    {
        String sql = "SELECT d.permissionName FROM  S_USER_ROLE a " +
            "LEFT JOIN S_ROLE b ON a.roleId =b.id " +
            "LEFT JOIN S_ROLE_PERMISSION c ON b.id =c.roleId " +
            "LEFT JOIN S_PERMISSION d ON c.permissionId = d.id " +
            "WHERE a.userTableName=? AND a.userId = ? AND d.permissionName=?";
        try
        {
            return jdbc.listMap(sql, userTableName, userId, permissionName).size() > 0;
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean hasPermission(String permissionName, Integer userId)
    {
        return hasPermission(permissionName, String.valueOf(userId));
    }
    
    public boolean hasPermissionLike(String permissionNameLikePrefix, Integer userId)
    {
        return hasPermissionLike(permissionNameLikePrefix, String.valueOf(userId));
    }
    
    /**
     * 某个人是否有某个权限（模糊匹配）<br>
     * 若实际拥有的权限是以参数权限开头的，那么返回true;否则返回false
     * @author nan.li
     * @param permissionNameLikePrefix
     * @param userId
     * @return
     */
    public boolean hasPermissionLike(String permissionNameLikePrefix, String userId)
    {
        String sql = "SELECT d.permissionName FROM  S_USER_ROLE a " +
            "LEFT JOIN S_ROLE b ON a.roleId =b.id " +
            "LEFT JOIN S_ROLE_PERMISSION c ON b.id =c.roleId " +
            "LEFT JOIN S_PERMISSION d ON c.permissionId = d.id " +
            "WHERE a.userTableName=? AND a.userId = ? AND d.permissionName like ?";
        try
        {
            return jdbc.listMap(sql, userTableName, userId, permissionNameLikePrefix + "%").size() > 0;
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            return false;
        }
    }
}
