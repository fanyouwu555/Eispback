package com.aeisp.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 接口限流注解。
 *
 * <p>基于 Redis 固定窗口计数器实现，支持按 IP、用户或全局维度限流。</p>
 *
 * @author AEISP Team
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {

    /**
     * 限流维度。
     */
    LimitType limitType() default LimitType.IP;

    /**
     * 时间窗口内允许的最大请求数。
     */
    int maxRequests() default 100;

    /**
     * 时间窗口大小（秒）。
     */
    int windowSeconds() default 60;

    /**
     * 限流提示信息。
     */
    String message() default "请求过于频繁，请稍后再试";

    /**
     * 限流维度枚举。
     */
    enum LimitType {
        /**
         * 按客户端 IP 限流。
         */
        IP,
        /**
         * 按登录用户限流。
         */
        USER,
        /**
         * 按接口全局限流。
         */
        GLOBAL
    }
}
