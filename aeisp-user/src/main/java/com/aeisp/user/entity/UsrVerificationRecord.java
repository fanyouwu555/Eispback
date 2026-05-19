package com.aeisp.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 验证码发送记录实体。
 *
 * <p>记录所有验证码的发送历史，用于防刷分析和安全审计。</p>
 *
 * @author AEISP Team
 */
@Data
@TableName("usr_verification_record")
public class UsrVerificationRecord {

    private static final long serialVersionUID = 1L;

    /**
     * 主键 ID，自增。
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 发送目标（手机号或邮箱）。
     */
    private String target;

    /**
     * 类型：1-短信，2-邮件。
     */
    private Integer sendType;

    /**
     * 场景：register/reset_password/bind/login。
     */
    private String scene;

    /**
     * 发送的验证码内容（明文存储，短期有效）。
     */
    private String verificationCode;

    /**
     * 验证结果：1-验证成功，2-验证失败，NULL-未验证。
     */
    private Integer verifyResult;

    /**
     * 请求IP。
     */
    private String ipAddress;

    /**
     * 验证码过期时间。
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expiredAt;

    /**
     * 发送时间。
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
