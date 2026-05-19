package com.aeisp.message.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 通知发送日志实体。
 *
 * <p>对应数据库表 {@code msg_notification_send_log}，记录每条通知按渠道维度的发送明细，
 * 包括发送目标、渠道、状态、响应结果等。该实体不继承 {@link com.aeisp.common.entity.BaseEntity}，
 * 因为数据量较大且不需要逻辑删除和审计字段。</p>
 *
 * @author AEISP Team
 */
@Data
@TableName("msg_notification_send_log")
public class MsgNotificationSendLog implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键 ID，自增策略。
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 消息通知 ID。
     */
    private Long notificationId;

    /**
     * 目标用户 ID。
     */
    private Long userId;

    /**
     * 发送渠道：site-站内，email-邮件，sms-短信，push-推送。
     */
    private String channel;

    /**
     * 发送状态：1-成功，2-失败，3-待发送。
     */
    private Integer status;

    /**
     * 发送请求内容（JSON）。
     */
    private String requestContent;

    /**
     * 发送响应内容（JSON）。
     */
    private String responseContent;

    /**
     * 错误信息。
     */
    private String errorMsg;

    /**
     * 发送时间。
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime sentAt;

    /**
     * 创建时间。
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
