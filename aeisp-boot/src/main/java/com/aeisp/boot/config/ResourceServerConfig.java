package com.aeisp.boot.config;

import com.aeisp.common.service.ResourceServerService;
import com.aeisp.template.service.impl.NfsResourceServerServiceImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * 资源服务器配置。
 *
 * <p>根据 resource-server.type 配置创建对应的资源服务器实例。
 * 当前仅支持 NFS，未来可扩展 OSS。</p>
 *
 * <p>两个实例：</p>
 * <ul>
 *   <li><b>templateResourceServer</b> (@Primary) — 模板资源，路径 resource-server.nfs.*</li>
 *   <li><b>userResourceServer</b> — 用户项目资源，路径 resource-server.users.*</li>
 * </ul>
 */
@Configuration
public class ResourceServerConfig {

    @Primary
    @Bean("templateResourceServer")
    @ConditionalOnProperty(name = "resource-server.type", havingValue = "nfs", matchIfMissing = true)
    public ResourceServerService templateResourceServer(
            @Value("${resource-server.nfs.upload-path}") String uploadPath,
            @Value("${resource-server.nfs.base-url}") String baseUrl) {
        return new NfsResourceServerServiceImpl(uploadPath, baseUrl);
    }

    @Bean("userResourceServer")
    @ConditionalOnProperty(name = "resource-server.type", havingValue = "nfs", matchIfMissing = true)
    public ResourceServerService userResourceServer(
            @Value("${resource-server.users.upload-path}") String uploadPath,
            @Value("${resource-server.users.base-url}") String baseUrl) {
        return new NfsResourceServerServiceImpl(uploadPath, baseUrl);
    }
}