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
     * 用户 ID。
     */
    @NotNull(message = "用户 ID 不能为空")
    private Long userId;

    /**
     * 调整量（分钟），正数为增加，负数为扣除。
     */
    @NotNull(message = "调整时长不能为空")
    private Integer deltaMinutes;

    /**
     * 调整原因（5-200字符）。
     */
    @NotBlank(message = "调整原因不能为空")
    @Size(min = 5, max = 200, message = "调整原因长度需在 5-200 字符之间")
    private String reason;
}
