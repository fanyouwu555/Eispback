package com.aeisp.user.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 用户自主注册请求。
 *
 * @author AEISP Team
 */
@Data
public class UserRegisterRequest {

    /**
     * 用户名。
     */
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 32, message = "用户名长度需在 3-32 位之间")
    private String username;

    /**
     * 手机号。
     */
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    /**
     * 邮箱。
     */
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    /**
     * 密码。
     */
    @NotBlank(message = "密码不能为空")
    @Size(min = 8, max = 32, message = "密码长度需在 8-32 位之间")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d@$!%*?&]{8,}$",
            message = "密码必须包含字母和数字，且至少 8 位")
    private String password;

    /**
     * 确认密码。
     */
    @NotBlank(message = "确认密码不能为空")
    private String confirmPassword;

    /**
     * 是否同意用户协议。
     */
    @NotBlank(message = "请同意用户协议")
    private String agreement;

    /**
     * 验证码标识。
     */
    @NotBlank(message = "验证码标识不能为空")
    private String captchaKey;

    /**
     * 验证码。
     */
    @NotBlank(message = "验证码不能为空")
    private String captchaCode;
}
