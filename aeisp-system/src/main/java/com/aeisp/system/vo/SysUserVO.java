package com.aeisp.system.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 后台管理员账号 VO。
 *
 * <p>用于返回用户基本信息及关联的角色列表。</p>
 *
 * @author AEISP Team
 */
@Data
public class SysUserVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户 ID。
     */
    private Long id;

    /**
     * 登录用户名。
     */
    private String username;

    /**
     * 真实姓名。
     */
    private String realName;

    /**
     * 电子邮箱。
     */
    private String email;

    /**
     * 手机号码。
     */
    private String phone;

    /**
     * 账号状态：0-禁用，1-正常。
     */
    private Integer status;

    /**
     * 创建时间。
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    /**
     * 更新时间。
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    /**
     * 角色列表。
     */
    private List<SysRoleVO> roles;
}
