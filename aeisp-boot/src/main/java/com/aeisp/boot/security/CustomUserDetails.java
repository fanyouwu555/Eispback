package com.aeisp.boot.security;

import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 自定义 UserDetails 实现类。
 *
 * <p>包装系统管理员（SysUser）和前端用户（UsrUser）的统一认证信息，
 * 支持角色和权限的 Spring Security 授权校验。</p>
 *
 * @author AEISP Team
 */
@Data
public class CustomUserDetails implements UserDetails {

    private static final long serialVersionUID = 1L;

    /**
     * 用户 ID。
     */
    private final Long userId;

    /**
     * 登录用户名。
     */
    private final String username;

    /**
     * 登录密码。
     */
    private final String password;

    /**
     * 用户类型：{@code admin} 表示后台管理员，{@code user} 表示前端用户。
     */
    private final String userType;

    /**
     * 账号状态：true 表示启用。
     */
    private final boolean enabled;

    /**
     * 角色编码列表。
     */
    private final List<String> roles;

    /**
     * 权限编码列表。
     */
    private final List<String> permissions;

    /**
     * 构造方法。
     *
     * @param userId      用户 ID
     * @param username    用户名
     * @param password    密码
     * @param userType    用户类型
     * @param enabled     是否启用
     * @param roles       角色列表
     * @param permissions 权限列表
     */
    public CustomUserDetails(Long userId, String username, String password,
                             String userType, boolean enabled,
                             List<String> roles, List<String> permissions) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.userType = userType;
        this.enabled = enabled;
        this.roles = roles != null ? roles : List.of();
        this.permissions = permissions != null ? permissions : List.of();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 将角色和权限都转换为 SimpleGrantedAuthority
        List<SimpleGrantedAuthority> roleAuthorities = roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toList());
        List<SimpleGrantedAuthority> permissionAuthorities = permissions.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
        return Stream.concat(roleAuthorities.stream(), permissionAuthorities.stream())
                .collect(Collectors.toList());
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
