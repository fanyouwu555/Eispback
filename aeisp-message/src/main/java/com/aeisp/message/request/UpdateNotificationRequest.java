package com.aeisp.message.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 更新消息通知请求（仅草稿状态可编辑）。
 *
 * @author AEISP Team
 */
@Data
public class UpdateNotificationRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "消息标题不能为空")
    private String title;

    /**
     * 消息摘要/简介。
     */
    private String summary;

    @NotBlank(message = "消息内容不能为空")
    private String content;

    @NotNull(message = "消息类型不能为空")
    private Integer msgType;

    @NotNull(message = "推送范围不能为空")
    private Integer pushScope;

    private String pushTarget;

    @NotNull(message = "推送方式不能为空")
    private Integer pushType;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime pushTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expireTime;
}