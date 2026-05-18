package com.aeisp.user.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 更新用户信息请求。
 *
 * @author AEISP Team
 */
@Data
public class UserUpdateRequest {

    /**
     * 用户 ID。
     */
    @NotNull(message = "用户 ID 不能为空")
    private Long id;

    /**
     * 手机号。
     */
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    /**
     * 邮箱。
     */
    @Email(message = "邮箱格式不正确")
    private String email;
}
