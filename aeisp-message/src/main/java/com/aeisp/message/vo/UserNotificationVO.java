package com.aeisp.message.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户视角消息列表项 VO。
 *
 * <p>用于前端消息铃铛展示用户收到的消息，包含标题、内容摘要、类型、已读状态等。</p>
 *
 * @author AEISP Team
 */
@Data
public class UserNotificationVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户消息关联 ID。
     */
    private Long id;

    /**
     * 消息通知 ID。
     */
    private Long notificationId;

    /**
     * 消息标题。
     */
    private String title;

    /**
     * 内容摘要（前 100 字符）。
     */
    private String contentSummary;

    /**
     * 消息类型枚举值。
     */
    private Integer msgType;

    /**
     * 消息类型文本。
     */
    private String msgTypeLabel;

    /**
     * 阅读状态：1-未读，2-已读，3-已撤回。
     */
    private Integer readStatus;

    /**
     * 阅读时间。
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime readAt;

    /**
     * 消息发送时间。
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
