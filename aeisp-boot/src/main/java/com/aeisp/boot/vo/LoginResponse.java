package com.aeisp.boot.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录响应数据。
 *
 * @author AEISP Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    /**
     * Access Token。
     */
    private String accessToken;

    /**
     * Refresh Token。
     */
    private String refreshToken;

    /**
     * Token 类型，固定为 Bearer。
     */
    private String tokenType;

    /**
     * Access Token 过期时间（秒）。
     */
    private Long expiresIn;

    /**
     * 用户信息。
     */
    private UserInfoVO userInfo;
}
