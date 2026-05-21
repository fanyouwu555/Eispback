package com.aeisp.system.entity;

import com.aeisp.common.constant.CommonConstants;
import com.aeisp.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 后台管理员账号实体。
 *
 * <p>对应数据库表 {@code sys_user}，存储系统后台管理员的基本信息。</p>
 *
 * @author AEISP Team
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
public class SysUser extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 登录用户名，全局唯一。
     */
    private String username;

    /**
     * 登录密码，采用 BCrypt 加密存储。
     */
    private String password;

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
     * 账号状态：{@link CommonConstants#STATUS_DISABLED} 禁用，{@link CommonConstants#STATUS_ENABLED} 正常。
     */
    private Integer status;

    /**
     * 最后登录 IP。
     */
    private String lastLoginIp;

    /**
     * 最后登录时间。
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastLoginAt;
}
