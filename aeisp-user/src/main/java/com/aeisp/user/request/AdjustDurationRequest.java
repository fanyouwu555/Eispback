package com.aeisp.user.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 手动调整用户剩余时长请求。
 *
 * @author AEISP Team
 */
@Data
public class AdjustDurationRequest {

    /**
     * 用户 ID（URL 路径传入，请求体中无需填写）。
     */
    private Long userId;

    /**
     * 调整类型：1-增加，2-扣减，3-设定。
     */
    @NotNull(message = "调整类型不能为空")
    private Integer adjustType;

    /**
     * 调整量（分钟）。
     * <p>增加/扣减时为 delta，设定时为目标值。</p>
     */
    @NotNull(message = "调整时长不能为空")
    private Integer deltaMinutes;

    /**
     * 调整原因（5-200字符）。
     */
    @NotBlank(message = "调整原因不能为空")
    @Size(min = 5, max = 200, message = "调整原因长度需在 5-200 字符之间")
    private String reason;

    /**
     * 当前管理员登录密码（二次确认）。
     */
    @NotBlank(message = "管理员密码不能为空")
    private String adminPassword;
}
