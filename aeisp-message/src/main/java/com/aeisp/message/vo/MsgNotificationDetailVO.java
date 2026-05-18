package com.aeisp.message.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 消息通知详情 VO。
 *
 * <p>用于后台管理端展示消息详情，包含完整内容、推送目标用户列表及已读/未读统计。</p>
 *
 * @author AEISP Team
 */
@Data
public class MsgNotificationDetailVO implements Serializable {

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
     * 消息内容（富文本）。
     */
    private String content;

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
     * 推送目标（用户 ID 列表或角色 ID 列表，JSON 数组）。
     */
    private String pushTarget;

    /**
     * 推送方式：1-立即，2-定时。
     */
    private Integer pushType;

    /**
     * 推送方式文本。
     */
    private String pushTypeLabel;

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
     * 状态：0-草稿，1-已推送，2-已撤回，3-已归档。
     */
    private Integer status;

    /**
     * 状态文本。
     */
    private String statusLabel;

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
     * 推送目标用户列表（仅指定用户/角色时有效）。
     */
    private List<PushTargetUserVO> targetUsers;

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
