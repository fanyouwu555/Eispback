package com.aeisp.common.aspect;

import com.aeisp.common.annotation.RateLimit;
import com.aeisp.common.constant.CacheConstants;
import com.aeisp.common.constant.ResultCode;
import com.aeisp.common.exception.BizException;
import com.aeisp.common.util.RedisUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.concurrent.TimeUnit;

/**
 * 接口限流 AOP 切面。
 *
 * <p>基于 Redis 固定窗口计数器实现，拦截标记了 {@link RateLimit} 的方法，
 * 在请求数超过阈值时抛出 {@link BizException}（错误码 429）。</p>
 *
 * @author AEISP Team
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class RateLimitAspect {

    private final RedisUtil redisUtil;

    @Pointcut("@annotation(com.aeisp.common.annotation.RateLimit)")
    public void rateLimitPointcut() {
    }

    @Before("rateLimitPointcut() && @annotation(rateLimit)")
    public void before(RateLimit rateLimit) {
        String key = buildKey(rateLimit);
        long count = incrementAndGet(key, rateLimit.windowSeconds());
        if (count > rateLimit.maxRequests()) {
            log.warn("接口限流触发, key={}, count={}", key, count);
            throw new BizException(ResultCode.TOO_MANY_REQUESTS, rateLimit.message());
        }
    }

    /**
     * Redis 计数器自增，首次设置过期时间。
     */
    private long incrementAndGet(String key, int windowSeconds) {
        String redisKey = CacheConstants.CACHE_PREFIX + "rate_limit:" + key;
        Long count = redisUtil.increment(redisKey);
        if (count != null && count == 1) {
            // 首次设置过期时间
            redisUtil.expire(redisKey, windowSeconds, TimeUnit.SECONDS);
        }
        return count != null ? count : 1;
    }

    private String buildKey(RateLimit rateLimit) {
        ServletRequestAttributes attributes = (ServletRequestAttributes)
                RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes != null ? attributes.getRequest() : null;
        String uri = request != null ? request.getRequestURI() : "unknown";

        switch (rateLimit.limitType()) {
            case IP -> {
                String ip = getClientIp(request);
                return "ip:" + ip + ":" + uri;
            }
            case USER -> {
                String userId = getCurrentUserId();
                return "user:" + userId + ":" + uri;
            }
            case GLOBAL -> {
                return "global:" + uri;
            }
            default -> {
                return "default:" + uri;
            }
        }
    }

    private String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() != null) {
            Object principal = authentication.getPrincipal();
            try {
                java.lang.reflect.Method method = principal.getClass().getMethod("getUserId");
                Object userId = method.invoke(principal);
                return userId != null ? userId.toString() : "anonymous";
            } catch (Exception e) {
                return principal.toString();
            }
        }
        return "anonymous";
    }

    private String getClientIp(HttpServletRequest request) {
        if (request == null) {
            return "unknown";
        }
        String ip = request.getHeader("X-Forwarded-For");
        if (!hasText(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (!hasText(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (!hasText(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    private boolean hasText(String str) {
        return str != null && !str.isBlank();
    }
}
