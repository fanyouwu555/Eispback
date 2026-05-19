package com.aeisp.user.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 重置密码请求。
 *
 * @author AEISP Team
 */
@Data
public class ResetPasswordRequest {

    /**
     * 用户 ID（URL 路径传入，请求体中无需填写）。
     */
    private Long userId;

    /**
     * 新密码（明文，可选，不传则由后台生成）。
     */
    @Size(min = 8, max = 32, message = "密码长度需在 8-32 位之间")
    private String newPassword;

    /**
     * 当前管理员登录密码（二次确认）。
     */
    @NotBlank(message = "管理员密码不能为空")
    private String adminPassword;
}
