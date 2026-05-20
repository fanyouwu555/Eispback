package com.aeisp.system.service.impl;

import com.aeisp.common.PageResult;
import com.aeisp.common.constant.CommonConstants;
import com.aeisp.common.exception.BizException;
import com.aeisp.system.dto.UserQueryRequest;
import com.aeisp.system.entity.SysUser;
import com.aeisp.system.mapper.SysUserMapper;
import com.aeisp.system.mapper.SysUserRoleMapper;
import com.aeisp.system.service.SysUserService;
import com.aeisp.system.vo.SysUserVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 后台管理员账号 Service 实现类。
 *
 * @author AEISP Team
 */
@Service
@RequiredArgsConstructor
public class SysUserServiceImpl implements SysUserService {

    private static final Long SUPER_ADMIN_ROLE_ID = 1L;

    private final SysUserMapper sysUserMapper;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public SysUser getByUsername(String username) {
        return sysUserMapper.selectByUsername(username);
    }

    @Override
    public SysUser getById(Long userId) {
        return sysUserMapper.selectById(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createUser(SysUser user, List<Long> roleIds) {
        // 密码加密
        if (StringUtils.hasText(user.getPassword())) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        user.setStatus(CommonConstants.STATUS_ENABLED);
        int rows = sysUserMapper.insert(user);
        if (rows <= 0) {
            return false;
        }
        // 分配角色
        if (!CollectionUtils.isEmpty(roleIds)) {
            sysUserRoleMapper.batchInsert(user.getId(), roleIds);
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateUser(SysUser user, List<Long> roleIds) {
        // 密码加密（如果传了密码）
        if (StringUtils.hasText(user.getPassword())) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        } else {
            user.setPassword(null);
        }
        // 防止降级最后一个超级管理员
        if (roleIds != null && !roleIds.contains(SUPER_ADMIN_ROLE_ID)) {
            if (hasSuperAdminRole(user.getId()) && isLastSuperAdmin(user.getId())) {
                throw new BizException("系统中至少保留一个超级管理员，无法降级");
            }
        }
        int rows = sysUserMapper.updateById(user);
        if (rows <= 0) {
            return false;
        }
        // 重新分配角色
        if (roleIds != null) {
            sysUserRoleMapper.deleteByUserId(user.getId());
            if (!CollectionUtils.isEmpty(roleIds)) {
                sysUserRoleMapper.batchInsert(user.getId(), roleIds);
            }
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteUser(Long userId) {
        // 防止删除最后一个超级管理员
        if (hasSuperAdminRole(userId) && isLastSuperAdmin(userId)) {
            throw new BizException("系统中至少保留一个超级管理员，无法删除");
        }
        int rows = sysUserMapper.deleteById(userId);
        if (rows > 0) {
            sysUserRoleMapper.deleteByUserId(userId);
        }
        return rows > 0;
    }

    private boolean hasSuperAdminRole(Long userId) {
        List<com.aeisp.system.entity.SysUserRole> userRoles = sysUserRoleMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.aeisp.system.entity.SysUserRole>()
                        .eq(com.aeisp.system.entity.SysUserRole::getUserId, userId));
        return userRoles.stream().anyMatch(ur -> SUPER_ADMIN_ROLE_ID.equals(ur.getRoleId()));
    }

    private boolean isLastSuperAdmin(Long userId) {
        long count = sysUserRoleMapper.countUsersByRoleId(SUPER_ADMIN_ROLE_ID);
        return count <= 1;
    }

    @Override
    public PageResult<SysUserVO> listUsers(UserQueryRequest request) {
        LambdaQueryWrapper<SysUser> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(SysUser::getDeleted, CommonConstants.DELETED_NO);
        if (StringUtils.hasText(request.getUsername())) {
            wrapper.like(SysUser::getUsername, request.getUsername());
        }
        if (StringUtils.hasText(request.getRealName())) {
            wrapper.like(SysUser::getRealName, request.getRealName());
        }
        if (request.getStatus() != null) {
            wrapper.eq(SysUser::getStatus, request.getStatus());
        }
        wrapper.orderByDesc(SysUser::getCreatedAt);

        Page<SysUser> page = new Page<>(request.getPageNum(), request.getPageSize());
        Page<SysUser> resultPage = sysUserMapper.selectPage(page, wrapper);

        List<SysUserVO> voList = resultPage.getRecords().stream()
                .map(SysUserServiceImpl::convertToVO)
                .toList();
        return PageResult.of(resultPage, voList);
    }

    private static SysUserVO convertToVO(SysUser user) {
        SysUserVO vo = new SysUserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setRealName(user.getRealName());
        vo.setEmail(user.getEmail());
        vo.setPhone(user.getPhone());
        vo.setStatus(user.getStatus());
        vo.setCreatedAt(user.getCreatedAt());
        vo.setUpdatedAt(user.getUpdatedAt());
        return vo;
    }
}
