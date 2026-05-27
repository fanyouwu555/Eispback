package com.aeisp.message.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 消息通知列表项 VO。
 *
 * <p>用于后台管理端展示消息列表，包含标题、类型、推送范围、状态等核心摘要信息。</p>
 *
 * @author AEISP Team
 */
@Data
public class MsgNotificationVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 消息 ID。
     */
    private Long id;

    /**
     * 消息标题。
     */
    private String title;

    /**
     * 消息类型枚举值。
     */
    private Integer msgType;

    /**
     * 消息类型文本。
     */
    private String msgTypeLabel;

    /**
     * 推送范围枚举值。
     */
    private Integer pushScope;

    /**
     * 推送范围文本。
     */
    private String pushScopeLabel;

    /**
     * 状态：0-草稿，1-已推送，2-已撤回，3-已归档。
     */
    private Integer status;

    /**
     * 状态文本。
     */
    private String statusLabel;

    /**
     * 推送方式：1-立即，2-定时。
     */
    private Integer pushType;

    /**
     * 推送时间。
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime pushTime;

    /**
     * 已读人数。
     */
    private Long readCount;

    /**
     * 推送总人数。
     */
    private Long totalCount;

    /**
     * 是否置顶：0-正常，1-置顶。
     */
    private Integer isTop;

    /**
     * 消息摘要/简介。
     */
    private String summary;

    /**
     * 发布人用户名。
     */
    private String publisherName;

    /**
     * 创建时间。
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
