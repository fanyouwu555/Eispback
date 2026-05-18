package com.aeisp.common.util;

import com.aeisp.common.exception.BizException;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JwtUtil 单元测试。
 */
class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() throws Exception {
        jwtUtil = new JwtUtil();
        // 触发 @PostConstruct
        jwtUtil.init();
    }

    @Test
    void testGenerateAndParseToken() {
        String token = jwtUtil.generateToken("1", "admin", List.of("SUPER_ADMIN"));
        assertNotNull(token);
        assertTrue(token.length() > 0);

        Claims claims = jwtUtil.parseToken(token);
        assertEquals("1", claims.get("userId", String.class));
        assertEquals("admin", claims.get("username", String.class));
        assertEquals("access", claims.get("type", String.class));
    }

    @Test
    void testGenerateAndParseRefreshToken() {
        String token = jwtUtil.generateRefreshToken("1");
        assertNotNull(token);

        Claims claims = jwtUtil.parseToken(token);
        assertEquals("1", claims.get("userId", String.class));
        assertEquals("refresh", claims.get("type", String.class));
    }

    @Test
    void testValidateTokenSuccess() {
        String token = jwtUtil.generateToken("1", "admin", List.of("ADMIN"));
        assertTrue(jwtUtil.validateToken(token));
    }

    @Test
    void testValidateTokenExpired() throws Exception {
        // 通过反射将过期时间设为 1 毫秒，然后等待过期
        Field accessExpField = JwtUtil.class.getDeclaredField("accessExpiration");
        accessExpField.setAccessible(true);
        accessExpField.setLong(jwtUtil, 1L);

        String token = jwtUtil.generateToken("1", "admin", List.of("ADMIN"));
        Thread.sleep(10);

        assertFalse(jwtUtil.validateToken(token));
        assertThrows(BizException.class, () -> jwtUtil.parseToken(token));
    }

    @Test
    void testParseInvalidToken() {
        assertThrows(BizException.class, () -> jwtUtil.parseToken("invalid-token"));
    }

    @Test
    void testDefaultExpirationValues() {
        assertEquals(2 * 60 * 60 * 1000L, jwtUtil.getAccessExpiration());
        assertEquals(7 * 24 * 60 * 60 * 1000L, jwtUtil.getRefreshExpiration());
    }
}
