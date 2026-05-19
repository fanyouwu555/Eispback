package com.aeisp.recharge.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 余额变更日志实体。
 *
 * <p>记录每次用户余额的变更明细，用于审计和对账。
 * 不继承 {@link com.aeisp.common.entity.BaseEntity}，使用独立轻量结构。</p>
 *
 * @author AEISP Team
 */
@Data
@TableName("usr_balance_change_log")
public class UsrBalanceChangeLog {

    private static final long serialVersionUID = 1L;

    /**
     * 主键 ID，自增。
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户 ID。
     */
    private Long userId;

    /**
     * 变更类型：1-充值，2-消费，3-退款，4-管理员调整。
     */
    private Integer changeType;

    /**
     * 变更金额（分），正数增加，负数扣减。
     */
    private Integer changeCents;

    /**
     * 变更前余额（分）。
     */
    private Integer previousBalance;

    /**
     * 变更后余额（分）。
     */
    private Integer currentBalance;

    /**
     * 关联订单 ID。
     */
    private Long relatedOrderId;

    /**
     * 变更原因。
     */
    private String reason;

    /**
     * 操作人 ID。
     */
    private Long operatorId;

    /**
     * 操作者类型：1-系统自动，2-管理员，3-用户本人。
     */
    private Integer operatorType;

    /**
     * 创建时间。
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
