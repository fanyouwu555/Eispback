package com.aeisp.user.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 时长变更日志 VO。
 *
 * @author AEISP Team
 */
@Data
public class UsrDurationChangeLogVO {

    /**
     * 日志 ID。
     */
    private Long id;

    /**
     * 用户 ID。
     */
    private Long userId;

    /**
     * 操作类型编码。
     */
    private String operationType;

    /**
     * 操作类型中文标签。
     */
    private String operationTypeLabel;

    /**
     * 变更时长（分钟），正数表示增加，负数表示扣减。
     */
    private Integer changeMinutes;

    /**
     * 变更前剩余时长。
     */
    private Integer previousRemaining;

    /**
     * 变更后剩余时长。
     */
    private Integer currentRemaining;

    /**
     * 变更原因说明。
     */
    private String reason;

    /**
     * 关联订单ID。
     */
    private String relatedOrderId;

    /**
     * 操作人ID。
     */
    private Long operatorId;

    /**
     * 操作者类型：1-系统自动，2-管理员，3-用户本人。
     */
    private Integer operatorType;

    /**
     * 操作者类型标签。
     */
    private String operatorTypeLabel;

    /**
     * 变更时间。
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
