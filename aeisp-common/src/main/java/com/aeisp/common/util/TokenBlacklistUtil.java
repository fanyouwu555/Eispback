package com.aeisp.common.util;

import cn.hutool.crypto.SecureUtil;
import com.aeisp.common.constant.CacheConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Token 黑名单工具类。
 *
 * <p>基于 Redis 实现，用户登出后将 Token 加入黑名单，阻止已失效的 Token 继续使用。
 * 黑名单 Key 使用 Token 的 MD5 哈希，避免长 Key 问题，TTL 与 Token 剩余过期时间一致。</p>
 *
 * @author AEISP Team
 */
@Component
@RequiredArgsConstructor
public class TokenBlacklistUtil {

    private final RedisUtil redisUtil;

    /**
     * 黑名单标识值。
     */
    private static final String BLACKLIST_VALUE = "1";

    /**
     * 将 Token 加入黑名单。
     *
     * @param token          JWT Token
     * @param expirationDate Token 过期时间
     */
    public void addToBlacklist(String token, Date expirationDate) {
        String key = buildKey(token);
        long ttlMillis = expirationDate.getTime() - System.currentTimeMillis();
        if (ttlMillis > 0) {
            redisUtil.set(key, BLACKLIST_VALUE, ttlMillis, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * 检查 Token 是否在黑名单中。
     *
     * @param token JWT Token
     * @return true 表示已被拉黑
     */
    public boolean isBlacklisted(String token) {
        String key = buildKey(token);
        return redisUtil.hasKey(key);
    }

    /**
     * 将用户所有 Token 加入黑名单（用于密码重置后强制登出）。
     *
     * @param userId         用户 ID
     * @param expirationDate Token 过期时间
     */
    public void addUserToBlacklist(Long userId, Date expirationDate) {
        String key = CacheConstants.CACHE_PREFIX + "user:blacklist:" + userId;
        long ttlMillis = expirationDate.getTime() - System.currentTimeMillis();
        if (ttlMillis > 0) {
            redisUtil.set(key, BLACKLIST_VALUE, ttlMillis, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * 检查用户是否已被拉黑（用于密码重置后阻止旧 Token）。
     *
     * @param userId 用户 ID
     * @return true 表示该用户所有 Token 已失效
     */
    public boolean isUserBlacklisted(Long userId) {
        String key = CacheConstants.CACHE_PREFIX + "user:blacklist:" + userId;
        return redisUtil.hasKey(key);
    }

    private String buildKey(String token) {
        return CacheConstants.CACHE_PREFIX + "token:blacklist:" + SecureUtil.md5(token);
    }
}
