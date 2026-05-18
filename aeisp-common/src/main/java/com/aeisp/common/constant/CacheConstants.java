package com.aeisp.common.constant;

/**
 * 缓存 Key 前缀常量类。
 *
 * <p>统一管理 Redis 缓存的 Key 前缀，避免缓存键冲突，便于维护和清理。</p>
 *
 * @author AEISP Team
 */
public final class CacheConstants {

    private CacheConstants() {
        // 工具类禁止实例化
    }

    /**
     * 缓存命名空间前缀。
     */
    public static final String CACHE_PREFIX = "aeisp:";

    /**
     * 验证码缓存前缀。
     */
    public static final String CACHE_CAPTCHA = CACHE_PREFIX + "captcha:";

    /**
     * 用户 Token 缓存前缀。
     */
    public static final String CACHE_USER_TOKEN = CACHE_PREFIX + "user:token:";

    /**
     * 用户信息缓存前缀。
     */
    public static final String CACHE_USER_INFO = CACHE_PREFIX + "user:info:";

    /**
     * 用户权限缓存前缀。
     */
    public static final String CACHE_USER_PERMS = CACHE_PREFIX + "user:perms:";

    /**
     * 字典数据缓存前缀。
     */
    public static final String CACHE_DICT = CACHE_PREFIX + "dict:";

    /**
     * 系统配置缓存前缀。
     */
    public static final String CACHE_CONFIG = CACHE_PREFIX + "config:";

    /**
     * 登录失败次数缓存前缀。
     */
    public static final String CACHE_LOGIN_FAIL = CACHE_PREFIX + "login:fail:";

    /**
     * 登录锁定缓存前缀。
     */
    public static final String CACHE_LOGIN_LOCK = CACHE_PREFIX + "login:lock:";
}
