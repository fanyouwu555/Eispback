package com.aeisp.boot.security;

import com.aeisp.common.constant.CommonConstants;
import com.aeisp.common.security.CustomUserDetails;
import com.aeisp.system.entity.SysUser;
import com.aeisp.system.mapper.SysPermissionMapper;
import com.aeisp.system.mapper.SysRoleMapper;
import com.aeisp.system.mapper.SysUserMapper;
import com.aeisp.system.mapper.SysUserRoleMapper;
import com.aeisp.user.entity.UsrUser;
import com.aeisp.user.mapper.UsrUserMapper;
import com.aeisp.user.mapper.UsrUserRoleMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * 自定义 UserDetailsService 实现类。
 *
 * <p>支持两套账号体系登录：
 * <ul>
 *   <li>先查询 {@code sys_user}（后台管理员），存在则按 admin 类型返回。</li>
 *   <li>再查询 {@code usr_user}（前端用户），存在则按 user 类型返回。</li>
 * </ul>
 * 查询不到则抛出 {@link UsernameNotFoundException}。</p>
 *
 * @author AEISP Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final SysUserMapper sysUserMapper;
    private final UsrUserMapper usrUserMapper;
    private final SysRoleMapper sysRoleMapper;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final UsrUserRoleMapper usrUserRoleMapper;
    private final SysPermissionMapper sysPermissionMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 1. 先查询后台管理员
        SysUser sysUser = sysUserMapper.selectByUsername(username);
        if (sysUser != null) {
            List<String> roles = sysRoleMapper.selectRoleCodesByUserId(sysUser.getId(), CommonConstants.STATUS_ENABLED);
            List<Long> roleIds = sysUserRoleMapper.selectList(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.aeisp.system.entity.SysUserRole>()
                            .eq(com.aeisp.system.entity.SysUserRole::getUserId, sysUser.getId())
            ).stream().map(com.aeisp.system.entity.SysUserRole::getRoleId).toList();
            List<String> permissions = roleIds.isEmpty()
                    ? Collections.emptyList()
                    : sysPermissionMapper.selectPermissionCodesByRoleIds(roleIds);
            boolean enabled = CommonConstants.STATUS_ENABLED == sysUser.getStatus();
            return new CustomUserDetails(
                    sysUser.getId(),
                    sysUser.getUsername(),
                    sysUser.getPassword(),
                    "admin",
                    enabled,
                    roles,
                    permissions
            );
        }

        // 2. 再查询前端用户
        UsrUser usrUser = usrUserMapper.selectByUsername(username);
        if (usrUser != null) {
            boolean enabled = CommonConstants.STATUS_ENABLED == usrUser.getStatus();
            List<String> roles = sysRoleMapper.selectRoleCodesByFrontendUserId(usrUser.getId(), CommonConstants.STATUS_ENABLED);
            if (roles == null || roles.isEmpty()) {
                roles = List.of(CommonConstants.DEFAULT_FRONTEND_ROLE_CODE);
            }
            List<Long> roleIds = usrUserRoleMapper.selectRoleIdsByUserId(usrUser.getId());
            List<String> permissions = roleIds.isEmpty()
                    ? Collections.emptyList()
                    : sysPermissionMapper.selectPermissionCodesByRoleIds(roleIds);
            return new CustomUserDetails(
                    usrUser.getId(),
                    usrUser.getUsername(),
                    usrUser.getPassword(),
                    "user",
                    enabled,
                    roles,
                    permissions
            );
        }

        log.warn("用户不存在: {}", username);
        throw new UsernameNotFoundException("用户不存在: " + username);
    }

    /**
     * 根据用户 ID 加载用户信息。
     *
     * @param userId 用户 ID
     * @return CustomUserDetails
     * @throws UsernameNotFoundException 用户不存在时抛出
     */
    public UserDetails loadUserByUserId(Long userId) throws UsernameNotFoundException {
        SysUser sysUser = sysUserMapper.selectById(userId);
        if (sysUser != null) {
            List<String> roles = sysRoleMapper.selectRoleCodesByUserId(sysUser.getId(), CommonConstants.STATUS_ENABLED);
            List<Long> roleIds = sysUserRoleMapper.selectList(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.aeisp.system.entity.SysUserRole>()
                            .eq(com.aeisp.system.entity.SysUserRole::getUserId, sysUser.getId())
            ).stream().map(com.aeisp.system.entity.SysUserRole::getRoleId).toList();
            List<String> permissions = roleIds.isEmpty()
                    ? Collections.emptyList()
                    : sysPermissionMapper.selectPermissionCodesByRoleIds(roleIds);
            boolean enabled = CommonConstants.STATUS_ENABLED == sysUser.getStatus();
            return new CustomUserDetails(
                    sysUser.getId(),
                    sysUser.getUsername(),
                    sysUser.getPassword(),
                    "admin",
                    enabled,
                    roles,
                    permissions
            );
        }

        UsrUser usrUser = usrUserMapper.selectById(userId);
        if (usrUser != null) {
            boolean enabled = CommonConstants.STATUS_ENABLED == usrUser.getStatus();
            List<String> roles = sysRoleMapper.selectRoleCodesByFrontendUserId(usrUser.getId(), CommonConstants.STATUS_ENABLED);
            if (roles == null || roles.isEmpty()) {
                roles = List.of(CommonConstants.DEFAULT_FRONTEND_ROLE_CODE);
            }
            List<Long> roleIds = usrUserRoleMapper.selectRoleIdsByUserId(usrUser.getId());
            List<String> permissions = roleIds.isEmpty()
                    ? Collections.emptyList()
                    : sysPermissionMapper.selectPermissionCodesByRoleIds(roleIds);
            return new CustomUserDetails(
                    usrUser.getId(),
                    usrUser.getUsername(),
                    usrUser.getPassword(),
                    "user",
                    enabled,
                    roles,
                    permissions
            );
        }

        log.warn("用户不存在，userId: {}", userId);
        throw new UsernameNotFoundException("用户不存在: " + userId);
    }
}
