package com.aeisp.system.service.impl;

import com.aeisp.common.constant.CommonConstants;
import com.aeisp.system.entity.SysRole;
import com.aeisp.system.entity.SysRolePermission;
import com.aeisp.system.mapper.SysRoleMapper;
import com.aeisp.system.mapper.SysRolePermissionMapper;
import com.aeisp.system.service.SysRoleService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * 角色 Service 实现类。
 *
 * @author AEISP Team
 */
@Service
@RequiredArgsConstructor
public class SysRoleServiceImpl implements SysRoleService {

    private final SysRoleMapper sysRoleMapper;
    private final SysRolePermissionMapper sysRolePermissionMapper;

    @Override
    public List<SysRole> listAll() {
        LambdaQueryWrapper<SysRole> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(SysRole::getDeleted, CommonConstants.DELETED_NO);
        wrapper.eq(SysRole::getStatus, CommonConstants.STATUS_ENABLED);
        wrapper.orderByAsc(SysRole::getId);
        return sysRoleMapper.selectList(wrapper);
    }

    @Override
    public List<String> getRoleCodesByUserId(Long userId) {
        return sysRoleMapper.selectRoleCodesByUserId(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createRole(SysRole role, List<Long> permissionIds) {
        role.setStatus(CommonConstants.STATUS_ENABLED);
        int rows = sysRoleMapper.insert(role);
        if (rows <= 0) {
            return false;
        }
        if (!CollectionUtils.isEmpty(permissionIds)) {
            sysRolePermissionMapper.batchInsert(role.getId(), permissionIds);
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateRole(SysRole role, List<Long> permissionIds) {
        int rows = sysRoleMapper.updateById(role);
        if (rows <= 0) {
            return false;
        }
        if (permissionIds != null) {
            sysRolePermissionMapper.deleteByRoleId(role.getId());
            if (!CollectionUtils.isEmpty(permissionIds)) {
                sysRolePermissionMapper.batchInsert(role.getId(), permissionIds);
            }
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteRole(Long roleId) {
        sysRolePermissionMapper.deleteByRoleId(roleId);
        return sysRoleMapper.deleteById(roleId) > 0;
    }
}
