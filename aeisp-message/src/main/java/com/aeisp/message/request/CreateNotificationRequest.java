package com.aeisp.message.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 创建消息通知请求。
 *
 * <p>用于后台管理端创建新的消息通知，支持立即推送和定时推送两种模式。</p>
 *
 * @author AEISP Team
 */
@Data
public class CreateNotificationRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 消息标题。
     */
    @NotBlank(message = "消息标题不能为空")
    private String title;

    /**
     * 消息内容（富文本）。
     */
    @NotBlank(message = "消息内容不能为空")
    private String content;

    /**
     * 消息类型枚举值。
     *
     * @see com.aeisp.message.enums.MsgTypeEnum
     */
    @NotNull(message = "消息类型不能为空")
    private Integer msgType;

    /**
     * 推送范围：1-全员，2-指定用户，3-指定角色。
     *
     * @see com.aeisp.message.enums.PushScopeEnum
     */
    @NotNull(message = "推送范围不能为空")
    private Integer pushScope;

    /**
     * 推送目标（用户 ID 列表或角色 ID 列表，JSON 数组）。
     * <p>当 pushScope 为 ALL 时可为空。</p>
     */
    private String pushTarget;

    /**
     * 推送方式：1-立即，2-定时。
     *
     * @see com.aeisp.message.enums.PushTypeEnum
     */
    @NotNull(message = "推送方式不能为空")
    private Integer pushType;

    /**
     * 定时推送时间。
     * <p>当 pushType 为 SCHEDULED 时必填。</p>
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime pushTime;

    /**
     * 过期时间。
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expireTime;
}
