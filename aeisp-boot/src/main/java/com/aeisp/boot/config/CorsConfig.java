package com.aeisp.boot.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

/**
 * 跨域配置类。
 *
 * <p>允许前端开发环境跨域访问，支持配置的域名来源、所有 HTTP 方法，并允许携带认证头（Authorization/Cookie）。
 * 生产环境务必在 application.yml 中显式配置允许的域名，不允许使用通配符。</p>
 *
 * @author AEISP Team
 */
@Configuration
public class CorsConfig {

    /**
     * 允许的跨域来源列表。
     *
     * <p>默认仅允许本地开发环境。生产环境必须在配置文件中覆盖。</p>
     */
    @Value("${cors.allowed-origins:http://localhost:3000,http://localhost:5173,http://127.0.0.1:3000,http://localhost:5174,http://localhost:5175}")
    private List<String> allowedOrigins;

    /**
     * 配置 CORS 过滤器。
     *
     * @return CorsFilter 实例
     */
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        // 显式配置允许的域名，不允许通配符与 AllowCredentials 同时使用
        allowedOrigins.forEach(config::addAllowedOriginPattern);
        // 允许携带 Cookie/Authorization
        config.setAllowCredentials(true);
        // 允许所有请求头
        config.addAllowedHeader("*");
        // 允许所有 HTTP 方法
        config.addAllowedMethod("*");
        // 预检请求缓存时间（秒）
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}
