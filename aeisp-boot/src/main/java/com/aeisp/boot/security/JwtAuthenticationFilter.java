package com.aeisp.boot.security;

import com.aeisp.common.Result;
import com.aeisp.common.constant.CommonConstants;
import com.aeisp.boot.code.BootErrorCode;
import com.aeisp.common.util.JwtUtil;
import com.aeisp.common.util.TokenBlacklistUtil;
import com.aeisp.user.entity.UsrUser;
import com.aeisp.user.mapper.UsrUserMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT 认证过滤器。
 *
 * <p>从请求头 {@code Authorization: Bearer <token>} 中提取 JWT Token，
 * 解析并校验后构建 {@link UsernamePasswordAuthenticationToken} 放入 SecurityContext。
 * 若 Token 无效或过期，直接返回 401 JSON 响应，避免进入全局异常处理器。</p>
 *
 * @author AEISP Team
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    private final ObjectMapper objectMapper;
    private final TokenBlacklistUtil tokenBlacklistUtil;
    private final UsrUserMapper usrUserMapper;

    /**
     * Authorization 请求头名称。
     */
    private static final String AUTHORIZATION_HEADER = "Authorization";

    /**
     * Bearer Token 前缀。
     */
    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = resolveToken(request);

        if (StringUtils.hasText(token)) {
            try {
                // 检查 Token 是否在黑名单中
                if (tokenBlacklistUtil.isBlacklisted(token)) {
                    writeUnauthorized(response, BootErrorCode.TOKEN_INVALID);
                    return;
                }
                Claims claims = jwtUtil.parseToken(token);
                String type = claims.get("type", String.class);
                // 只允许 Access Token 用于接口认证
                if (!"access".equals(type)) {
                    writeUnauthorized(response, BootErrorCode.TOKEN_TYPE_INVALID);
                    return;
                }
                // 检查用户是否已被全局拉黑（密码重置后强制登出）
                String userIdStr = claims.get("userId", String.class);
                if (StringUtils.hasText(userIdStr)) {
                    try {
                        Long userId = Long.parseLong(userIdStr);
                        if (tokenBlacklistUtil.isUserBlacklisted(userId)) {
                            writeUnauthorized(response, BootErrorCode.ACCOUNT_RESET);
                            return;
                        }
                    } catch (NumberFormatException ignored) {
                    }
                }
                String username = claims.get("username", String.class);
                if (StringUtils.hasText(username) && SecurityContextHolder.getContext().getAuthentication() == null) {
                    // 检查用户状态（仅前端用户），非正常状态拒绝访问
                    if (StringUtils.hasText(userIdStr)) {
                        try {
                            Long uid = Long.parseLong(userIdStr);
                            UsrUser usrUser = usrUserMapper.selectById(uid);
                            if (usrUser != null && usrUser.getStatus() != CommonConstants.USER_STATUS_NORMAL) {
                                String label = switch (usrUser.getStatus()) {
                                    case 2 -> "禁用";
                                    case 3 -> "冻结";
                                    case 4 -> "锁定";
                                    default -> "异常";
                                };
                                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                                response.setContentType("application/json;charset=UTF-8");
                                Result<Void> result = Result.error(BootErrorCode.ACCOUNT_DISABLED.getCode(), "账号已被" + label);
                                response.getWriter().write(objectMapper.writeValueAsString(result));
                                return;
                            }
                        } catch (NumberFormatException ignored) {
                        }
                    }
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(
                            new org.springframework.security.web.authentication.WebAuthenticationDetailsSource()
                                    .buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (Exception e) {
                log.warn("JWT 认证失败: {}", e.getMessage());
                // 清除认证上下文，继续过滤器链，由 Spring Security 的 authorizeHttpRequests 决定是否放行
                // 白名单路径（如 /api/v1/auth/**）应被 permitAll() 放行，不受 Token 无效影响
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 从请求头中解析 Token。
     *
     * @param request HTTP 请求
     * @return Token 字符串，不存在则返回 null
     */
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }
        return null;
    }

    /**
     * 写入 401 未认证响应。
     *
     * @param response HTTP 响应
     * @param message  错误信息
     * @throws IOException IO 异常
     */
    private void writeUnauthorized(HttpServletResponse response, BootErrorCode errorCode) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        Result<Void> result = Result.error(errorCode);
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }
}
