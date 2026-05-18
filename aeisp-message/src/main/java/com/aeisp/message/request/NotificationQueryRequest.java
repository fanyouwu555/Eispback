package com.aeisp.message.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 消息通知查询请求。
 *
 * <p>用于后台管理端和用户端分页查询消息列表，支持按类型、状态、时间范围筛选。</p>
 *
 * @author AEISP Team
 */
@Data
public class NotificationQueryRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 当前页码，默认 1。
     */
    private Long pageNum = 1L;

    /**
     * 每页大小，默认 10。
     */
    private Long pageSize = 10L;

    /**
     * 消息类型枚举值。
     */
    private Integer msgType;

    /**
     * 状态：0-草稿，1-已推送，2-已撤回，3-已归档。
     */
    private Integer status;

    /**
     * 开始时间。
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    /**
     * 结束时间。
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;
}
