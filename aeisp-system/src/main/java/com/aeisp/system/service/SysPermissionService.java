package com.aeisp.system.service;

import com.aeisp.system.entity.SysPermission;

import java.util.List;

/**
 * 权限点 Service 接口。
 *
 * @author AEISP Team
 */
public interface SysPermissionService {

    /**
     * 查询所有权限点列表。
     *
     * @return 权限点列表
     */
    List<SysPermission> listAll();

    /**
     * 根据角色 ID 列表查询权限编码列表。
     *
     * @param roleIds 角色 ID 列表
     * @return 权限编码列表
     */
    List<String> getPermissionCodesByRoleIds(List<Long> roleIds);
}
