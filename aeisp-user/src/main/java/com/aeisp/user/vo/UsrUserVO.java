package com.aeisp.user.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

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
     * 账号状态：0-禁用，1-正常，2-冻结。
     */
    private Integer status;

    /**
     * 剩余使用时长（分钟）。
     */
    private Long remainingDuration;

    /**
     * 充值总额（分）。
     */
    private Long totalRecharge;

    /**
     * 最近一次登录时间。
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastLoginTime;

    /**
     * 注册 IP。
     */
    private String registerIp;

    /**
     * 注册设备。
     */
    private String registerDevice;

    /**
     * 注册时间。
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime registerTime;

    /**
     * 角色编码。
     */
    private String roleCode;

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
