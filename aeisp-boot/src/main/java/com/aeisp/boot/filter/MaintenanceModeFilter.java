package com.aeisp.boot.filter;

import com.aeisp.common.Result;
import com.aeisp.system.service.SysFeatureSwitchService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 全站维护模式过滤器。
 *
 * <p>当维护模式开启时，阻止所有客户端/前台 API 请求，仅保留管理员后台接口可用。
 * 使用 {@link OncePerRequestFilter} 确保每个请求仅执行一次。</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MaintenanceModeFilter extends OncePerRequestFilter {

    private final SysFeatureSwitchService featureSwitchService;
    private final ObjectMapper objectMapper;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    /**
     * 维护模式下允许访问的路径（白名单）。
     */
    private static final String[] WHITE_LIST = {
            // 认证接口（登录/刷新/登出，管理员需要能登录）
            "/api/v1/auth/**",
            // 平台公开信息
            "/api/v1/platform/info",
            // Swagger / API 文档
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/swagger-ui.html",
            // 系统配置接口（管理员需要能关闭维护模式）
            "/api/v1/system/features/**",
            "/api/v1/system/configs/**",
            // 静态资源
            "/",
            "/index.html",
            "/assets/**",
            "/favicon.ico",
            "/error"
    };

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {
        String uri = request.getRequestURI();

        // 白名单直接放行
        if (isWhiteListed(uri)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 检查维护模式
        if (!featureSwitchService.isEnabled("maintenance")) {
            // 维护模式未开启
            filterChain.doFilter(request, response);
            return;
        }

        // 维护模式已开启，阻止请求
        log.warn("维护模式已开启，阻止请求: {} {}", request.getMethod(), uri);
        response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        response.setContentType("application/json;charset=UTF-8");
        Result<Void> result = Result.error(503, "系统维护中，请稍后再试");
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }

    private boolean isWhiteListed(String uri) {
        for (String pattern : WHITE_LIST) {
            if (pathMatcher.match(pattern, uri)) {
                return true;
            }
        }
        return false;
    }
}
