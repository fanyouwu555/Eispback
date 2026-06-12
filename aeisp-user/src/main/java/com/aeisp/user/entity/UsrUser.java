package com.aeisp.user.entity;

import com.aeisp.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 前端注册用户主表实体。
 *
 * <p>存储平台前端用户的账号信息、状态、登录及注册信息等核心数据。
 * 时长与余额已拆分为独立表 {@link UsrUserDuration} 和 {@link UsrUserBalance}。
 * 与 {@code aeisp-system} 的 {@code SysUser}（后台管理员）为两套独立的账号体系。</p>
 *
 * @author AEISP Team
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("usr_user")
public class UsrUser extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 用户名，全局唯一，只允许字母、数字、下划线，首位必须为字母。
     */
    private String username;

    /**
     * 登录密码，采用 BCrypt 加密存储。
     */
    private String password;

    /**
     * 手机号，全局唯一，11位数字。
     */
    private String phone;

    /**
     * 邮箱，全局唯一。
     */
    private String email;

    /**
     * 用户昵称，展示用。
     */
    private String nickname;

    /**
     * 头像图片URL。
     */
    private String avatarUrl;

    /**
     * 账号状态：1-正常，2-禁用，3-冻结，4-锁定。
     */
    private Integer status;

    /**
     * 状态变更原因说明。
     */
    private String statusReason;

    /**
     * 账号锁定到期时间，仅在锁定状态时有效。
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lockedUntil;

    /**
     * 下次登录是否需要修改密码：0-否，1-是。
     */
    private Integer needChangePassword;

    /**
     * 连续登录失败次数，登录成功后清零。
     */
    private Integer failedLoginAttempts;

    /**
     * 累计登录次数。
     */
    private Integer loginCount;

    /**
     * 是否异地登录：0-否，1-是。
     */
    private Integer abnormalLogin;

    /**
     * 最后登录时间。
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastLoginTime;

    /**
     * 最后登录IP地址（支持IPv6）。
     */
    private String lastLoginIp;

    /**
     * 注册时IP地址。
     */
    private String registerIp;

    /**
     * 注册设备信息JSON，包含设备类型、操作系统、浏览器、设备ID。
     */
    private String registerDeviceInfo;

    /**
     * 注册时间。
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime registerTime;

    /**
     * 注册时使用的邀请码。
     */
    private String invitationCodeUsed;

    /**
     * 是否比赛用户：0-否，1-是。
     */
    private Integer isCompetition;

    /**
     * API 访问密钥。
     */
    private String apiKey;

    /**
     * 租户 ID。
     */
    private String tenantId;
}
