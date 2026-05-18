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
 * <p>存储平台前端用户的账号信息、状态、剩余时长及充值记录等核心数据。
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
     * 用户名，全局唯一，用于登录。
     */
    private String username;

    /**
     * 手机号，全局唯一。
     */
    private String phone;

    /**
     * 邮箱，全局唯一。
     */
    private String email;

    /**
     * 登录密码，采用 BCrypt 加密存储。
     */
    private String password;

    /**
     * 账号状态：0-禁用，1-正常，2-冻结。
     *
     * @see com.aeisp.common.constant.CommonConstants
     */
    private Integer status;

    /**
     * 剩余使用时长（分钟）。
     */
    private Long remainingDuration;

    /**
     * 充值总额（单位：分）。
     */
    private Long totalRecharge;

    /**
     * 最近一次登录时间。
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastLoginTime;

    /**
     * 注册 IP 地址。
     */
    private String registerIp;

    /**
     * 注册设备信息。
     */
    private String registerDevice;

    /**
     * 注册时间。
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime registerTime;

    /**
     * 角色编码，关联 sys_role.role_code。
     */
    private String roleCode;
}
