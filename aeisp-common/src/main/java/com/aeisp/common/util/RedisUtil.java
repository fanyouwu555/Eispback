package com.aeisp.common.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Redis 工具类。
 *
 * <p>基于 {@link StringRedisTemplate} 封装常用 Redis 操作，
 * 支持字符串和对象的存取，对象默认使用 JSON 序列化。</p>
 *
 * @author AEISP Team
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisUtil {

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    /**
     * 键值自增 1。
     *
     * @param key 键
     * @return 自增后的值
     */
    public Long increment(String key) {
        return stringRedisTemplate.opsForValue().increment(key);
    }

    /**
     * 获取键的剩余过期时间（秒）。
     *
     * @param key 键
     * @return 剩余秒数，-1 表示永不过期，-2 表示键不存在
     */
    public Long getExpire(String key) {
        return stringRedisTemplate.getExpire(key, TimeUnit.SECONDS);
    }

    /**
     * 存储字符串值，并设置过期时间。
     *
     * @param key     键
     * @param value   值
     * @param timeout 过期时间
     * @param unit    时间单位
     */
    public void set(String key, String value, long timeout, TimeUnit unit) {
        stringRedisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    /**
     * 获取字符串值。
     *
     * @param key 键
     * @return 值，不存在时返回 null
     */
    public String get(String key) {
        return stringRedisTemplate.opsForValue().get(key);
    }

    /**
     * 删除键。
     *
     * @param key 键
     * @return true 表示删除成功
     */
    public boolean delete(String key) {
        return Boolean.TRUE.equals(stringRedisTemplate.delete(key));
    }

    /**
     * 设置键的过期时间。
     *
     * @param key     键
     * @param timeout 过期时间
     * @param unit    时间单位
     * @return true 表示设置成功
     */
    public boolean expire(String key, long timeout, TimeUnit unit) {
        return Boolean.TRUE.equals(stringRedisTemplate.expire(key, timeout, unit));
    }

    /**
     * 判断键是否存在。
     *
     * @param key 键
     * @return true 表示存在
     */
    public boolean hasKey(String key) {
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(key));
    }

    /**
     * 存储对象（JSON 序列化），并设置过期时间。
     *
     * @param key     键
     * @param value   对象值
     * @param timeout 过期时间
     * @param unit    时间单位
     * @param <T>     对象类型
     */
    public <T> void setObject(String key, T value, long timeout, TimeUnit unit) {
        try {
            String json = objectMapper.writeValueAsString(value);
            stringRedisTemplate.opsForValue().set(key, json, timeout, unit);
        } catch (JsonProcessingException e) {
            log.error("Redis 对象序列化失败: key={}, error={}", key, e.getMessage(), e);
            throw new RuntimeException("Redis 序列化失败", e);
        }
    }

    /**
     * 获取对象（JSON 反序列化）。
     *
     * @param key   键
     * @param clazz 目标类型
     * @param <T>   对象类型
     * @return 对象，不存在时返回 null
     */
    public <T> T getObject(String key, Class<T> clazz) {
        String json = stringRedisTemplate.opsForValue().get(key);
        if (json == null) {
            return null;
        }
        try {
            return objectMapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            log.error("Redis 对象反序列化失败: key={}, error={}", key, e.getMessage(), e);
            throw new RuntimeException("Redis 反序列化失败", e);
        }
    }
}
