package com.aeisp.user.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 后台创建用户请求。
 *
 * @author AEISP Team
 */
@Data
public class UserCreateRequest {

    /**
     * 用户名。
     */
    @NotBlank(message = "用户名不能为空")
    @Size(min = 4, max = 20, message = "用户名长度需在 4-20 位之间")
    @Pattern(regexp = "^[a-zA-Z][a-zA-Z0-9_]{3,19}$", message = "用户名只允许字母、数字、下划线，首位必须为字母，长度4-20位")
    private String username;

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

    /**
     * 用户昵称。
     */
    @Size(max = 50, message = "昵称长度不能超过50位")
    private String nickname;

    /**
     * 初始密码（不传则默认 123456）。
     */
    private String password;

    /**
     * 账号状态：1-正常，2-禁用，3-冻结。
     */
    private Integer status;

    /**
     * API 访问密钥。
     */
    @NotBlank(message = "APIKEY不能为空")
    @Size(max = 128, message = "APIKEY长度不能超过128位")
    @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "APIKEY只能包含字母、数字、下划线、横线")
    private String apiKey;

    /**
     * 租户 ID。
     */
    @NotBlank(message = "租户ID不能为空")
    @Size(max = 64, message = "租户ID长度不能超过64位")
    @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "租户ID只能包含字母、数字、下划线、横线")
    private String tenantId;

    /**
     * 初始剩余时长（分钟）。
     */
    private Integer remainingMinutes;

    /**
     * 角色ID列表。
     */
    private List<Long> roleIds;

    /**
     * 是否比赛用户：0-否，1-是。
     */
    private Integer isCompetition;
}
