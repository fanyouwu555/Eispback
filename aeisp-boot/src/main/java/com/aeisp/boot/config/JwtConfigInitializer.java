package com.aeisp.boot.config;

import com.aeisp.common.util.JwtUtil;
import com.aeisp.system.service.SysConfigService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * JWT 配置初始化器。
 *
 * <p>应用启动时从 sys_config 读取超时配置，覆盖 application.yml 中的默认值。</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtConfigInitializer {

    private final JwtUtil jwtUtil;
    private final SysConfigService sysConfigService;

    /**
     * 默认 Access Token 过期时间（毫秒）：2 小时 = 120 分钟。
     */
    private static final long DEFAULT_ACCESS_MINUTES = 120;

    @PostConstruct
    public void init() {
        String sessionTimeout = sysConfigService.getConfigValue("timeout.session", "all");
        long minutes = parseMinutes(sessionTimeout);
        long millis = minutes * 60 * 1000;
        jwtUtil.setAccessExpiration(millis);
        log.info("JWT Access Token 过期时间已设置为 {} 分钟（{} 毫秒）", minutes, millis);
    }

    private long parseMinutes(String value) {
        if (!StringUtils.hasText(value)) {
            return DEFAULT_ACCESS_MINUTES;
        }
        try {
            long minutes = Long.parseLong(value.trim());
            return minutes > 0 ? minutes : DEFAULT_ACCESS_MINUTES;
        } catch (NumberFormatException e) {
            log.warn("timeout.session 配置值 '{}' 无法解析为数字，使用默认值 {} 分钟", value, DEFAULT_ACCESS_MINUTES);
            return DEFAULT_ACCESS_MINUTES;
        }
    }
}
