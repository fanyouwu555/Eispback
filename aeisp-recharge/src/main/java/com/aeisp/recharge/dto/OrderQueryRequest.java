package com.aeisp.recharge.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 订单分页查询请求。
 *
 * <p>用于订单列表的条件筛选和分页参数传递。</p>
 *
 * @author AEISP Team
 */
@Data
public class OrderQueryRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户 ID。
     */
    private Long userId;

    /**
     * 订单号（精确查询）。
     */
    private String orderNo;

    /**
     * 状态：0-待支付，1-已支付，2-已退款，3-已取消。
     */
    private Integer status;

    /**
     * 当前页码。
     */
    private Long pageNum = 1L;

    /**
     * 每页大小。
     */
    private Long pageSize = 10L;
}
