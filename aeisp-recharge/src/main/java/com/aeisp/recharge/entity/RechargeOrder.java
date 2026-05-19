package com.aeisp.recharge.entity;

import com.aeisp.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 充值订单实体。
 *
 * <p>对应数据库表 {@code recharge_order}，存储用户充值订单的完整生命周期。</p>
 *
 * @author AEISP Team
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("recharge_order")
public class RechargeOrder extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 订单号，全局唯一。
     */
    private String orderNo;

    /**
     * 用户 ID。
     */
    private Long userId;

    /**
     * 关联套餐 ID。
     */
    private Long packageId;

    /**
     * 充值金额（分）。
     */
    private Integer amountCents;

    /**
     * 到账时长（分钟）。
     */
    private Long durationMinutes;

    /**
     * 支付方式。
     */
    private String payType;

    /**
     * 状态：1-待支付，2-已支付，3-已退款，4-已取消。
     */
    private Integer status;

    /**
     * 支付时间。
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime payTime;

    /**
     * 退款原因。
     */
    private String refundReason;
}
