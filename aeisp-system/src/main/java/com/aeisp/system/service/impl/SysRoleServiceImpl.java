package com.aeisp.system.service.impl;

import com.aeisp.common.constant.CommonConstants;
import com.aeisp.common.exception.BizException;
import com.aeisp.system.entity.SysRole;
import com.aeisp.system.mapper.SysRoleMapper;
import com.aeisp.system.mapper.SysRolePermissionMapper;
import com.aeisp.system.mapper.SysUserRoleMapper;
import com.aeisp.system.service.SysRoleService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

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
    private final SysUserRoleMapper sysUserRoleMapper;

    @Override
    public List<SysRole> listAll(String roleName) {
        LambdaQueryWrapper<SysRole> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(SysRole::getDeleted, CommonConstants.DELETED_NO);
        if (StringUtils.hasText(roleName)) {
            wrapper.like(SysRole::getRoleName, roleName);
        }
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
        SysRole existing = sysRoleMapper.selectById(role.getId());
        if (existing == null) {
            throw new BizException("角色不存在");
        }
        if (Integer.valueOf(1).equals(existing.getIsSystem())) {
            // 系统内置角色禁止修改编码和内置标识
            if (role.getRoleCode() != null && !role.getRoleCode().equals(existing.getRoleCode())) {
                throw new BizException("系统内置角色的编码不允许修改");
            }
            if (role.getIsSystem() != null && !Integer.valueOf(1).equals(role.getIsSystem())) {
                throw new BizException("系统内置角色不允许改为自定义角色");
            }
        }
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
        SysRole role = sysRoleMapper.selectById(roleId);
        if (role == null) {
            throw new BizException("角色不存在");
        }
        if (Integer.valueOf(1).equals(role.getIsSystem())) {
            throw new BizException("系统内置角色不可删除");
        }
        // 检查是否有管理员绑定该角色
        long userCount = sysUserRoleMapper.countUsersByRoleId(roleId);
        if (userCount > 0) {
            throw new BizException("该角色已绑定 " + userCount + " 个管理员，请先解除绑定后再删除");
        }
        sysRolePermissionMapper.deleteByRoleId(roleId);
        return sysRoleMapper.deleteById(roleId) > 0;
    }
}
