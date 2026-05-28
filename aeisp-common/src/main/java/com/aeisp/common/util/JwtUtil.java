package com.aeisp.common.util;

import com.aeisp.common.exception.BizException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JWT 工具类。
 *
 * <p>提供 Access Token 和 Refresh Token 的生成、解析与校验功能。
 * 基于 JJWT 0.12.x 实现，密钥和过期时间支持通过配置文件注入。</p>
 *
 * @author AEISP Team
 */
@Slf4j
@Component
public class JwtUtil {

    /**
     * 默认 Access Token 过期时间（毫秒）：2 小时。
     */
    private static final long DEFAULT_ACCESS_EXPIRATION = 2 * 60 * 60 * 1000L;

    /**
     * 默认 Refresh Token 过期时间（毫秒）：7 天。
     */
    private static final long DEFAULT_REFRESH_EXPIRATION = 7 * 24 * 60 * 60 * 1000L;

    @Value("${jwt.secret:}")
    private String configuredSecret;

    @Value("${jwt.access-expiration:}")
    private Long configuredAccessExpiration;

    @Value("${jwt.refresh-expiration:}")
    private Long configuredRefreshExpiration;

    private SecretKey secretKey;
    private long accessExpiration;
    private long refreshExpiration;

    public void setAccessExpiration(long accessExpiration) {
        this.accessExpiration = accessExpiration;
    }

    public void setRefreshExpiration(long refreshExpiration) {
        this.refreshExpiration = refreshExpiration;
    }

    /**
     * 初始化密钥和过期时间。
     *
     * <p>密钥必须通过配置文件显式配置，不存在默认密钥，防止因未修改配置导致的安全隐患。</p>
     */
    @PostConstruct
    public void init() {
        if (!StringUtils.hasText(configuredSecret)) {
            throw new IllegalStateException("JWT 密钥未配置，请在 application.yml 中设置 jwt.secret（Base64 编码，至少 256 位）");
        }
        this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(configuredSecret));
        this.accessExpiration = (configuredAccessExpiration == null || configuredAccessExpiration <= 0)
                ? DEFAULT_ACCESS_EXPIRATION : configuredAccessExpiration;
        this.refreshExpiration = (configuredRefreshExpiration == null || configuredRefreshExpiration <= 0)
                ? DEFAULT_REFRESH_EXPIRATION : configuredRefreshExpiration;
    }

    /**
     * 生成 Access Token。
     *
     * @param userId   用户 ID
     * @param username 用户名
     * @param roles    角色列表
     * @return JWT Token 字符串
     */
    public String generateToken(String userId, String username, List<String> roles) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("roles", roles);
        claims.put("type", "access");
        return buildToken(claims, accessExpiration);
    }

    /**
     * 生成 Refresh Token。
     *
     * @param userId 用户 ID
     * @return JWT Refresh Token 字符串
     */
    public String generateRefreshToken(String userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("type", "refresh");
        return buildToken(claims, refreshExpiration);
    }

    /**
     * 解析 Token 获取 Claims。
     *
     * @param token JWT Token
     * @return Claims 对象
     * @throws BizException 当 Token 无效或过期时抛出
     */
    public Claims parseToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            log.warn("Token 已过期");
            throw new BizException("登录已过期，请重新登录");
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Token 解析失败: {}", e.getMessage());
            throw new BizException("无效的 Token");
        }
    }

    /**
     * 校验 Token 是否有效。
     *
     * @param token JWT Token
     * @return true 表示有效
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("Token 已过期");
            return false;
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Token 校验失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 获取 Access Token 的过期时间（毫秒）。
     *
     * @return 过期时间毫秒值
     */
    public long getAccessExpiration() {
        return accessExpiration;
    }

    /**
     * 获取 Refresh Token 的过期时间（毫秒）。
     *
     * @return 过期时间毫秒值
     */
    public long getRefreshExpiration() {
        return refreshExpiration;
    }

    /**
     * 构建 Token。
     *
     * @param claims     自定义声明
     * @param expiration 过期时间（毫秒）
     * @return JWT 字符串
     */
    private String buildToken(Map<String, Object> claims, long expiration) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);
        return Jwts.builder()
                .claims(claims)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(secretKey)
                .compact();
    }
}
