package com.aeisp.system.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 创建用户请求参数。
 *
 * @author AEISP Team
 */
@Data
public class CreateUserRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 登录用户名。
     */
    @NotBlank(message = "用户名不能为空")
    @Size(max = 50, message = "用户名长度不能超过 50")
    private String username;

    /**
     * 登录密码。
     */
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 100, message = "密码长度必须在 6-100 之间")
    private String password;

    /**
     * 真实姓名。
     */
    @Size(max = 50, message = "真实姓名长度不能超过 50")
    private String realName;

    /**
     * 电子邮箱。
     */
    @Email(message = "邮箱格式不正确")
    @Size(max = 100, message = "邮箱长度不能超过 100")
    private String email;

    /**
     * 手机号码。
     */
    @Size(max = 20, message = "手机号长度不能超过 20")
    private String phone;

    /**
     * 角色 ID 列表。
     */
    private List<Long> roleIds;
}
