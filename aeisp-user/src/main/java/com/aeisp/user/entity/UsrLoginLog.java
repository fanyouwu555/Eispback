package com.aeisp.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户登录日志实体。
 *
 * <p>记录所有用户的登录行为，用于安全审计和活跃度统计。
 * 不继承 {@link com.aeisp.common.entity.BaseEntity}，使用独立轻量结构。</p>
 *
 * @author AEISP Team
 */
@Data
@TableName("usr_login_log")
public class UsrLoginLog {

    private static final long serialVersionUID = 1L;

    /**
     * 主键 ID，自增。
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID，登录失败时可能为NULL。
     */
    private Long userId;

    /**
     * 登录时使用的账号（用户名/手机号/邮箱）。
     */
    private String loginAccount;

    /**
     * 登录类型：1-密码登录，2-验证码登录，3-Token刷新。
     */
    private Integer loginType;

    /**
     * 登录结果：1-成功，2-密码错误，3-账号不存在，4-账号禁用，5-账号冻结，6-账号锁定，7-验证码错误，8-Token过期。
     */
    private Integer loginResult;

    /**
     * 登录IP。
     */
    private String ipAddress;

    /**
     * 设备类型：desktop/mobile/tablet/unknown。
     */
    private String deviceType;

    /**
     * 操作系统信息。
     */
    private String osInfo;

    /**
     * 浏览器信息。
     */
    private String browserInfo;

    /**
     * 设备唯一标识。
     */
    private String deviceId;

    /**
     * 登录时间。
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
