package com.aeisp.system.mapper;

import com.aeisp.system.entity.SysPermission;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 权限点 Mapper 接口。
 *
 * @author AEISP Team
 */
public interface SysPermissionMapper extends BaseMapper<SysPermission> {

    /**
     * 根据角色 ID 列表查询权限编码列表。
     *
     * @param roleIds 角色 ID 列表
     * @return 权限编码列表
     */
    @Select("<script>" +
            "SELECT DISTINCT p.permission_code FROM sys_permission p " +
            "INNER JOIN sys_role_permission rp ON p.id = rp.permission_id " +
            "WHERE rp.role_id IN " +
            "<foreach collection='roleIds' item='roleId' open='(' separator=',' close=')'>" +
            "#{roleId}" +
            "</foreach>" +
            "</script>")
    List<String> selectPermissionCodesByRoleIds(@Param("roleIds") List<Long> roleIds);

    /**
     * 根据角色编码查询权限编码列表。
     *
     * @param roleCode 角色编码
     * @return 权限编码列表
     */
    @Select("SELECT DISTINCT p.permission_code FROM sys_permission p " +
            "INNER JOIN sys_role_permission rp ON p.id = rp.permission_id " +
            "INNER JOIN sys_role r ON r.id = rp.role_id " +
            "WHERE r.role_code = #{roleCode}")
    List<String> selectPermissionCodesByRoleCode(@Param("roleCode") String roleCode);
}
