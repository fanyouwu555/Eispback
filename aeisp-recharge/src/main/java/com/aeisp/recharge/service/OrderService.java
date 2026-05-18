package com.aeisp.recharge.service;

import com.aeisp.common.PageResult;
import com.aeisp.recharge.dto.OrderQueryRequest;
import com.aeisp.recharge.dto.OrderVO;

/**
 * 充值订单服务接口。
 *
 * <p>提供订单创建、支付、退款、取消及分页查询能力。</p>
 *
 * @author AEISP Team
 */
public interface OrderService {

    /**
     * 创建订单。
     *
     * @param userId    用户 ID
     * @param packageId 套餐 ID
     * @return 订单视图对象
     */
    OrderVO createOrder(Long userId, Long packageId);

    /**
     * 支付订单。
     *
     * @param orderNo 订单号
     * @param payType 支付方式
     * @return 是否成功
     */
    boolean payOrder(String orderNo, String payType);

    /**
     * 退款订单。
     *
     * @param orderNo 订单号
     * @param reason  退款原因
     * @return 是否成功
     */
    boolean refundOrder(String orderNo, String reason);

    /**
     * 取消订单。
     *
     * @param orderNo 订单号
     * @return 是否成功
     */
    boolean cancelOrder(String orderNo);

    /**
     * 分页查询订单列表。
     *
     * @param request 查询条件
     * @return 分页结果
     */
    PageResult<OrderVO> listOrders(OrderQueryRequest request);
}
