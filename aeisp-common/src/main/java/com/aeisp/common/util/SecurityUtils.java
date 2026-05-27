package com.aeisp.common.util;

import com.aeisp.common.security.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 安全工具类。
 *
 * <p>提供从 Spring Security 上下文中获取当前登录用户信息的静态方法。</p>
 *
 * @author AEISP Team
 */
public class SecurityUtils {

    /**
     * 获取当前登录用户的 ID。
     *
     * @return 用户 ID，如果未登录则返回 {@code null}
     */
    public static Long getUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof CustomUserDetails userDetails) {
            return userDetails.getUserId();
        }
        return null;
    }

    /**
     * 获取当前登录用户的用户名。
     *
     * @return 用户名，如果未登录则返回 {@code null}
     */
    public static String getUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof CustomUserDetails userDetails) {
            return userDetails.getUsername();
        }
        return null;
    }
}