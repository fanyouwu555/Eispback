package com.aeisp.message.entity;

import com.aeisp.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 消息通知主表实体。
 *
 * <p>对应数据库表 {@code msg_notification}，存储平台通知消息的元数据，
 * 包括标题、内容、推送范围、推送方式、状态等核心信息。</p>
 *
 * @author AEISP Team
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("msg_notification")
public class MsgNotification extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 消息标题。
     */
    private String title;

    /**
     * 消息内容（富文本）。
     */
    private String content;

    /**
     * 消息类型枚举值。
     *
     * @see com.aeisp.message.enums.MsgTypeEnum
     */
    private Integer msgType;

    /**
     * 推送范围：1-全员，2-指定用户，3-指定角色。
     *
     * @see com.aeisp.message.enums.PushScopeEnum
     */
    private Integer pushScope;

    /**
     * 推送目标（用户 ID 列表或角色 ID 列表，JSON 数组格式）。
     */
    private String pushTarget;

    /**
     * 推送方式：1-立即，2-定时。
     *
     * @see com.aeisp.message.enums.PushTypeEnum
     */
    private Integer pushType;

    /**
     * 定时推送时间。
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime pushTime;

    /**
     * 过期时间。
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expireTime;

    /**
     * 状态：1-草稿，2-已发送，3-已撤回，4-定时发送。
     *
     * @see com.aeisp.message.enums.NotificationStatusEnum
     */
    private Integer status;

    /**
     * 是否置顶：0-正常，1-置顶。
     */
    private Integer isTop;

    /**
     * 已读人数。
     */
    private Long readCount;

    /**
     * 推送总人数。
     */
    private Long totalCount;

    /**
     * 优先级：1-低，2-中，3-高，4-紧急。
     *
     * @see com.aeisp.message.enums.NotificationPriorityEnum
     */
    private Integer priority;

    /**
     * 推送渠道（JSON 数组：site/email/sms/push）。
     */
    private String channels;

    /**
     * 目标用户 ID 列表（JSON 数组）。
     */
    private String targetUserIds;

    /**
     * 目标分组 ID 列表（JSON 数组）。
     */
    private String targetGroupIds;

    /**
     * 是否需要阅读确认：0-否，1-是。
     */
    private Integer needReadConfirmation;

    /**
     * 目标人数。
     */
    private Integer targetCount;

    /**
     * 发送成功人数。
     */
    private Integer successCount;

    /**
     * 发送失败人数。
     */
    private Integer failedCount;

    /**
     * 发送时间。
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime sentAt;

    /**
     * 撤回时间。
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime revokedAt;
}
