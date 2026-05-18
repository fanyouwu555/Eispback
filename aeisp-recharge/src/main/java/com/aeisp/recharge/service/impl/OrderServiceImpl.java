package com.aeisp.recharge.service.impl;

import com.aeisp.common.PageResult;
import com.aeisp.recharge.dto.OrderQueryRequest;
import com.aeisp.recharge.dto.OrderVO;
import com.aeisp.recharge.service.OrderService;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * 充值订单服务实现类。
 *
 * <p>待实现真实业务逻辑，当前所有方法抛出 {@link UnsupportedOperationException}。</p>
 *
 * @author AEISP Team
 */
@Service
public class OrderServiceImpl implements OrderService {

    @Override
    public OrderVO createOrder(Long userId, Long packageId) {
        throw new UnsupportedOperationException("createOrder 待实现");
    }

    @Override
    public boolean payOrder(String orderNo, String payType) {
        throw new UnsupportedOperationException("payOrder 待实现");
    }

    @Override
    public boolean refundOrder(String orderNo, String reason) {
        throw new UnsupportedOperationException("refundOrder 待实现");
    }

    @Override
    public boolean cancelOrder(String orderNo) {
        throw new UnsupportedOperationException("cancelOrder 待实现");
    }

    @Override
    public PageResult<OrderVO> listOrders(OrderQueryRequest request) {
        return PageResult.<OrderVO>builder()
                .list(Collections.emptyList())
                .total(0L)
                .pageNum(request.getPageNum())
                .pageSize(request.getPageSize())
                .totalPages(0L)
                .build();
    }
}
