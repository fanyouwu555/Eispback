package com.aeisp.boot.security;

import com.aeisp.boot.code.BootErrorCode;
import com.aeisp.boot.filter.UnityAccessLogFilter;
import com.aeisp.common.Result;
import com.aeisp.common.code.CommonErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security 配置类。
 *
 * <p>配置 JWT 无状态认证、权限控制、公开接口放行、以及异常处理。
 * 禁用 CSRF 和 Session，适用于前后端分离架构。</p>
 *
 * <h3>API 路径约定</h3>
 * <pre>
 *   /api/v1/auth/**           — 认证相关（登录/注册/刷新），无需认证
 *   /api/v1/client/**         — 客户端 API（Unity/其他），需 JWT 认证，不检查 @PreAuthorize
 *   /api/v1/templates/public/** — 公开模板数据，无需认证
 *   /api/v1/template-categories/** — 模板分类，公开接口无需认证，管理接口需 admin 权限
 *   /api/v1/**（其他）         — 管理端 API，需 JWT 认证 + @PreAuthorize 细粒度权限
 * </pre>
 *
 * <h3>客户端 API 授权策略</h3>
 * <p>客户端 API（/api/v1/client/*）采用 <b>身份授权</b> 而非 <b>角色授权</b>：
 * <ul>
 *   <li>只验证 JWT 有效性（已登录）</li>
 *   <li>不检查 {@code @PreAuthorize} 细粒度权限</li>
 *   <li>授权逻辑由 Controller/Service 自行处理（如按 userId 过滤资源归属）</li>
 *   <li>如需客户端分级（免费版/付费版），在 Service 层使用用户属性判断，而非 RBAC</li>
 * </ul>
 *
 * @author AEISP Team
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UserDetailsService userDetailsService;
    private final ObjectMapper objectMapper;

    /**
     * Unity 客户端访问日志过滤器。
     */
    @Bean
    public UnityAccessLogFilter unityAccessLogFilter() {
        return new UnityAccessLogFilter(objectMapper);
    }

    /**
     * 白名单路径：无需认证即可访问。
     *
     * <p>仅保留认证入口、API 文档和静态资源。业务数据接口（模板/分类等）
     * 均需要登录后才能访问，即使是客户端也需先获取 JWT。</p>
     */
    private static final String[] WHITE_LIST = {
            // ===== 认证接口 =====
            "/api/v1/auth/**",                  // 登录/注册/刷新 token

            // ===== API 文档 =====
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/swagger-ui.html",

            // ===== 系统默认 =====
            "/error",

            // ===== 静态资源 =====
            "/",
            "/index.html",
            "/assets/**",
            "/favicon.ico"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 禁用 CSRF（前后端分离）
                .csrf(AbstractHttpConfigurer::disable)
                // 无状态 Session
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 请求授权配置
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(WHITE_LIST).permitAll()
                        .requestMatchers(HttpMethod.OPTIONS).permitAll()
                        .anyRequest().authenticated()
                )
                // 添加 JWT 过滤器
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                // 添加 Unity 访问日志过滤器（在 JWT 之后，可获取用户信息）
                .addFilterAfter(unityAccessLogFilter(), JwtAuthenticationFilter.class)
                // 异常处理
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(authenticationEntryPoint())
                        .accessDeniedHandler(accessDeniedHandler())
                );

        return http.build();
    }

    /**
     * 配置认证管理器。
     *
     * @return AuthenticationManager 实例
     */
    @Bean
    public AuthenticationManager authenticationManager() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return new ProviderManager(provider);
    }

    /**
     * 配置密码加密器。
     *
     * @return BCryptPasswordEncoder 实例
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 未认证处理器：返回 401 JSON。
     *
     * @return AuthenticationEntryPoint 实例
     */
    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, authException) -> {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            Result<Void> result = Result.error(BootErrorCode.TOKEN_MISSING);
            response.getWriter().write(objectMapper.writeValueAsString(result));
        };
    }

    /**
     * 无权限处理器：返回 403 JSON。
     *
     * @return AccessDeniedHandler 实例
     */
    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json;charset=UTF-8");
            Result<Void> result = Result.error(CommonErrorCode.ACCESS_DENIED);
            response.getWriter().write(objectMapper.writeValueAsString(result));
        };
    }
}
