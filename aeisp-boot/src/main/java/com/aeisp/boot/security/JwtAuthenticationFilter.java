package com.aeisp.boot.security;

import com.aeisp.common.Result;
import com.aeisp.common.constant.ResultCode;
import com.aeisp.common.util.JwtUtil;
import com.aeisp.common.util.TokenBlacklistUtil;
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
                    writeUnauthorized(response, "Token 已失效，请重新登录");
                    return;
                }
                Claims claims = jwtUtil.parseToken(token);
                String type = claims.get("type", String.class);
                // 只允许 Access Token 用于接口认证
                if (!"access".equals(type)) {
                    writeUnauthorized(response, "无效的 Token 类型");
                    return;
                }
                String username = claims.get("username", String.class);
                if (StringUtils.hasText(username) && SecurityContextHolder.getContext().getAuthentication() == null) {
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
                writeUnauthorized(response, "未登录或 Token 已过期");
                return;
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
    private void writeUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        Result<Void> result = Result.error(ResultCode.UNAUTHORIZED, message);
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }
}
