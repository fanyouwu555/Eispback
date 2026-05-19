package com.aeisp.message.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户消息关联表实体。
 *
 * <p>对应数据库表 {@code msg_user_notification}，维护用户与消息的一对多关系，
 * 记录每个用户的消息已读状态。该实体不继承 {@link com.aeisp.common.entity.BaseEntity}，
 * 因为数据量较大且不需要逻辑删除和审计字段。</p>
 *
 * @author AEISP Team
 */
@Data
@TableName("msg_user_notification")
public class MsgUserNotification implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键 ID，自增策略。
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户 ID。
     */
    private Long userId;

    /**
     * 消息通知 ID。
     */
    private Long notificationId;

    /**
     * 阅读状态：1-未读，2-已读，3-已撤回。
     *
     * @see com.aeisp.message.enums.UserNotificationReadStatusEnum
     */
    private Integer readStatus;

    /**
     * 已接收渠道（JSON 数组：site/email/sms/push）。
     */
    private String channelReceived;

    /**
     * 阅读时间。
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime readAt;

    /**
     * 创建时间（消息推送时间）。
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
