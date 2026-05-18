package com.aeisp.boot.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 登录请求参数。
 *
 * @author AEISP Team
 */
@Data
public class LoginRequest {

    /**
     * 用户名。
     */
    @NotBlank(message = "用户名不能为空")
    private String username;

    /**
     * 密码。
     */
    @NotBlank(message = "密码不能为空")
    private String password;
}
