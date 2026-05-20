package com.aeisp.user.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 前端用户详情 VO。
 *
 * @author AEISP Team
 */
@Data
public class UsrUserVO {

    /**
     * 用户 ID。
     */
    private Long id;

    /**
     * 用户名。
     */
    private String username;

    /**
     * 手机号。
     */
    private String phone;

    /**
     * 邮箱。
     */
    private String email;

    /**
     * 用户昵称。
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
     * 账号锁定到期时间。
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lockedUntil;

    /**
     * 下次登录是否需要修改密码：0-否，1-是。
     */
    private Integer needChangePassword;

    /**
     * 连续登录失败次数。
     */
    private Integer failedLoginAttempts;

    /**
     * 最后登录时间。
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastLoginTime;

    /**
     * 最后登录IP地址。
     */
    private String lastLoginIp;

    /**
     * 注册时IP地址。
     */
    private String registerIp;

    /**
     * 注册设备信息JSON。
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
     * 角色编码列表。
     */
    private List<String> roleCodes;

    /**
     * 剩余可用时长（分钟）。
     */
    private Integer remainingMinutes;

    /**
     * 当前余额（分）。
     */
    private Integer balanceCents;

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
}
