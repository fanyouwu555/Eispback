package com.aeisp.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 时长变更日志实体。
 *
 * <p>记录每次用户时长增减的详细变更记录，用于审计和对账。</p>
 *
 * @author AEISP Team
 */
@Data
@TableName("usr_duration_change_log")
public class UsrDurationChangeLog {

    private static final long serialVersionUID = 1L;

    /**
     * 主键 ID，自增。
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 关联用户ID。
     */
    private Long userId;

    /**
     * 操作类型：REGISTER_GRANT, RECHARGE, ADMIN_ADD, ADMIN_SUBTRACT, ADMIN_SET, CONSUME, REFUND_DEDUCT。
     */
    private String operationType;

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
     * 关联订单ID，充值类操作时填写。
     */
    private String relatedOrderId;

    /**
     * 操作人ID，系统自动操作时NULL。
     */
    private Long operatorId;

    /**
     * 操作者类型：1-系统自动，2-管理员，3-用户本人。
     */
    private Integer operatorType;

    /**
     * 变更时间。
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
