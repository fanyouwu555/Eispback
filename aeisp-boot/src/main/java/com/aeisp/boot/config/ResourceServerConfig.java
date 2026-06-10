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
 * <p>三个实例：</p>
 * <ul>
 *   <li><b>templateResourceServer</b> (@Primary) — 模板资源，路径 key = storage.template</li>
 *   <li><b>userResourceServer</b> — 用户项目资源，路径 key = storage.project</li>
 *   <li><b>libraryResourceServer</b> — 库资源，路径 key = storage.library.path</li>
 * </ul>
 *
 * <p>改造后：各实例只存相对路径，完整 URL 由 {@link NfsResourceServerServiceImpl#getUrl(String)}
 * 运行时动态拼接（base-address + url-prefix + relative-path）。</p>
 */
@Configuration
public class ResourceServerConfig {

    @Primary
    @Bean("templateResourceServer")
    @ConditionalOnProperty(name = "resource-server.type", havingValue = "nfs", matchIfMissing = true)
    public ResourceServerService templateResourceServer(
            @Value("${resource-server.base-address}") String baseAddress,
            SysConfigService sysConfigService,
            @Value("${resource-server.nfs.upload-path}") String defaultUploadPath,
            @Value("${resource-server.nfs.url-prefix}") String urlPrefix) {
        String uploadPath = sysConfigService.getConfigValue("storage.template", "all");
        if (!StringUtils.hasText(uploadPath)) {
            uploadPath = defaultUploadPath;
        }
        return new NfsResourceServerServiceImpl(uploadPath, baseAddress, urlPrefix);
    }

    @Bean("userResourceServer")
    @ConditionalOnProperty(name = "resource-server.type", havingValue = "nfs", matchIfMissing = true)
    public ResourceServerService userResourceServer(
            @Value("${resource-server.base-address}") String baseAddress,
            SysConfigService sysConfigService,
            @Value("${resource-server.users.upload-path}") String defaultUploadPath,
            @Value("${resource-server.users.url-prefix}") String urlPrefix) {
        String uploadPath = sysConfigService.getConfigValue("storage.project", "all");
        if (!StringUtils.hasText(uploadPath)) {
            uploadPath = defaultUploadPath;
        }
        return new NfsResourceServerServiceImpl(uploadPath, baseAddress, urlPrefix);
    }

    @Bean("libraryResourceServer")
    @ConditionalOnProperty(name = "resource-server.type", havingValue = "nfs", matchIfMissing = true)
    public ResourceServerService libraryResourceServer(
            @Value("${resource-server.base-address}") String baseAddress,
            SysConfigService sysConfigService,
            @Value("${resource-server.library.upload-path:./uploads/library/}") String defaultUploadPath,
            @Value("${resource-server.library.url-prefix:library/}") String defaultUrlPrefix) {
        String uploadPath = sysConfigService.getConfigValue("storage.library.path", "all");
        if (!StringUtils.hasText(uploadPath)) {
            uploadPath = defaultUploadPath;
        }
        // sys_config 中存储的也是相对路径（路径前缀），不再存完整 URL
        String libraryUrlPrefix = sysConfigService.getConfigValue("storage.library.baseUrl", "all");
        if (!StringUtils.hasText(libraryUrlPrefix)) {
            libraryUrlPrefix = defaultUrlPrefix;
        } else {
            // 兼容旧数据：如果 sys_config 中存的是完整 URL，截断为相对路径
            libraryUrlPrefix = extractPathPrefix(libraryUrlPrefix);
        }
        return new NfsResourceServerServiceImpl(uploadPath, baseAddress, libraryUrlPrefix);
    }

    /**
     * 从可能包含协议和域名的字符串中提取纯路径前缀。
     *
     * <p>兼容旧配置中存完整 URL 的情况，如：
     * {@code http://192.168.50.215:4050/EISP/Resource/Library/} → {@code EISP/Resource/Library/}</p>
     */
    private static String extractPathPrefix(String value) {
        if (value == null || value.isBlank()) {
            return value;
        }
        if (value.startsWith("http://") || value.startsWith("https://")) {
            // 跳过协议和主机部分
            int hostEnd = value.indexOf("/", value.indexOf("://") + 3);
            if (hostEnd > 0) {
                String path = value.substring(hostEnd + 1);
                return path.endsWith("/") ? path : path + "/";
            }
        }
        return value.endsWith("/") ? value : value + "/";
    }
}
