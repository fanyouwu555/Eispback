package com.aeisp.system.service;

import com.aeisp.system.entity.SysRole;

import java.util.List;

/**
 * 角色 Service 接口。
 *
 * @author AEISP Team
 */
public interface SysRoleService {

    /**
     * 查询所有角色列表。
     *
     * @return 角色列表
     */
    List<SysRole> listAll();

    /**
     * 根据用户 ID 查询角色编码列表。
     *
     * @param userId 用户 ID
     * @return 角色编码列表
     */
    List<String> getRoleCodesByUserId(Long userId);

    /**
     * 创建角色并分配权限。
     *
     * @param role          角色实体
     * @param permissionIds 权限 ID 列表
     * @return {@code true} 创建成功
     */
    boolean createRole(SysRole role, List<Long> permissionIds);

    /**
     * 更新角色及权限分配。
     *
     * @param role          角色实体
     * @param permissionIds 权限 ID 列表
     * @return {@code true} 更新成功
     */
    boolean updateRole(SysRole role, List<Long> permissionIds);

    /**
     * 删除角色。
     *
     * @param roleId 角色 ID
     * @return {@code true} 删除成功
     */
    boolean deleteRole(Long roleId);
}
