package com.aeisp.common.security;

import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

/**
 * 自定义 UserDetails 实现类。
 *
 * <p>包装系统管理员（SysUser）和前端用户（UsrUser）的统一认证信息，
 * 支持角色和权限的 Spring Security 授权校验。</p>
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
     * API 访问密钥。
     */
    private final String apiKey;

    /**
     * 租户 ID。
     */
    private final String tenantId;

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
        this(userId, username, password, userType, enabled, roles, permissions, null, null);
    }

    /**
     * 构造方法（含 apiKey/tenantId）。
     *
     * @param userId      用户 ID
     * @param username    用户名
     * @param password    密码
     * @param userType    用户类型
     * @param enabled     是否启用
     * @param roles       角色列表
     * @param permissions 权限列表
     * @param apiKey      API 访问密钥
     * @param tenantId    租户 ID
     */
    public CustomUserDetails(Long userId, String username, String password,
                             String userType, boolean enabled,
                             List<String> roles, List<String> permissions,
                             String apiKey, String tenantId) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.userType = userType;
        this.enabled = enabled;
        this.roles = roles != null ? roles : List.of();
        this.permissions = permissions != null ? permissions : List.of();
        this.apiKey = apiKey;
        this.tenantId = tenantId;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<SimpleGrantedAuthority> roleAuthorities = roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .toList();
        List<SimpleGrantedAuthority> permissionAuthorities = permissions.stream()
                .map(SimpleGrantedAuthority::new)
                .toList();
        return Stream.concat(roleAuthorities.stream(), permissionAuthorities.stream())
                .toList();
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