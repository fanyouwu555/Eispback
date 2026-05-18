package com.aeisp.recharge.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 充值订单视图对象。
 *
 * <p>用于订单详情和列表查询的返回展示。</p>
 *
 * @author AEISP Team
 */
@Data
public class OrderVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 订单 ID。
     */
    private Long id;

    /**
     * 用户 ID。
     */
    private Long userId;

    /**
     * 订单号。
     */
    private String orderNo;

    /**
     * 充值金额（分）。
     */
    private Integer amount;

    /**
     * 到账时长（小时）。
     */
    private Long durationHours;

    /**
     * 支付方式。
     */
    private String payType;

    /**
     * 状态：0-待支付，1-已支付，2-已退款，3-已取消。
     */
    private Integer status;

    /**
     * 下单时间。
     */
    private LocalDateTime orderTime;

    /**
     * 支付时间。
     */
    private LocalDateTime payTime;

    /**
     * 退款原因。
     */
    private String refundReason;
}
