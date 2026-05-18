package com.aeisp.boot.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * SpringDoc OpenAPI 配置类。
 *
 * <p>配置 Swagger UI 文档信息、Bearer Token 安全认证方案及开发环境服务器地址。</p>
 *
 * @author AEISP Team
 */
@Configuration
public class OpenApiConfig {

    /**
     * 安全方案名称。
     */
    private static final String SECURITY_SCHEME_NAME = "Bearer Authentication";

    /**
     * 配置 OpenAPI 文档 Bean。
     *
     * @return OpenAPI 实例
     */
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("AEISP Server API")
                        .version("1.0.0")
                        .description("AEISP 服务端 REST API 文档，支持管理员和前端用户两套账号体系。"))
                .servers(List.of(new Server().url("http://localhost:8080").description("本地开发环境")))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME,
                                new SecurityScheme()
                                        .name(SECURITY_SCHEME_NAME)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")));
    }
}
