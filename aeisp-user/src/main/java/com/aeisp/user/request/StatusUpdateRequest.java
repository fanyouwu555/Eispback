package com.aeisp.user.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 修改用户状态请求。
 *
 * @author AEISP Team
 */
@Data
public class StatusUpdateRequest {

    /**
     * 目标状态：1-正常，2-禁用，3-冻结，4-锁定。
     */
    @NotNull(message = "状态不能为空")
    private Integer status;

    /**
     * 状态变更原因。
     */
    private String reason;

    /**
     * 当前管理员登录密码（二次确认）。
     */
    @NotBlank(message = "管理员密码不能为空")
    private String adminPassword;
}
