package com.aeisp.boot.config;

import com.aeisp.common.service.ResourceServerService;
import com.aeisp.system.service.SysConfigService;
import com.aeisp.template.service.impl.NfsResourceServerServiceImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.util.StringUtils;

/**
 * 资源服务器配置。
 *
 * <p>根据 resource-server.type 配置创建对应的资源服务器实例。
 * 当前仅支持 NFS，未来可扩展 OSS。</p>
 *
 * <p>存储路径优先从 sys_config 读取，未配置时回退到 application.yml 默认值。</p>
 * <p>两个实例：</p>
 * <ul>
 *   <li><b>templateResourceServer</b> (@Primary) — 模板资源，路径 key = storage.template</li>
 *   <li><b>userResourceServer</b> — 用户项目资源，路径 key = storage.project</li>
 * </ul>
 */
@Configuration
public class ResourceServerConfig {

    @Primary
    @Bean("templateResourceServer")
    @ConditionalOnProperty(name = "resource-server.type", havingValue = "nfs", matchIfMissing = true)
    public ResourceServerService templateResourceServer(
            SysConfigService sysConfigService,
            @Value("${resource-server.nfs.upload-path}") String defaultUploadPath,
            @Value("${resource-server.nfs.base-url}") String baseUrl) {
        String uploadPath = sysConfigService.getConfigValue("storage.template", "all");
        if (!StringUtils.hasText(uploadPath)) {
            uploadPath = defaultUploadPath;
        }
        return new NfsResourceServerServiceImpl(uploadPath, baseUrl);
    }

    @Bean("userResourceServer")
    @ConditionalOnProperty(name = "resource-server.type", havingValue = "nfs", matchIfMissing = true)
    public ResourceServerService userResourceServer(
            SysConfigService sysConfigService,
            @Value("${resource-server.users.upload-path}") String defaultUploadPath,
            @Value("${resource-server.users.base-url}") String baseUrl) {
        String uploadPath = sysConfigService.getConfigValue("storage.project", "all");
        if (!StringUtils.hasText(uploadPath)) {
            uploadPath = defaultUploadPath;
        }
        return new NfsResourceServerServiceImpl(uploadPath, baseUrl);
    }
}