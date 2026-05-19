package com.aeisp.user.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 更新用户信息请求。
 *
 * @author AEISP Team
 */
@Data
public class UserUpdateRequest {

    /**
     * 用户 ID（由路径参数注入，无需请求体传递）。
     */
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

    /**
     * 用户昵称。
     */
    @Size(max = 50, message = "昵称长度不能超过50位")
    private String nickname;

    /**
     * 头像图片URL。
     */
    private String avatarUrl;

    /**
     * 角色ID列表。
     */
    private List<Long> roleIds;
}
