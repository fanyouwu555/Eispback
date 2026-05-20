package com.aeisp.recharge.service.impl;

import com.aeisp.common.PageResult;
import com.aeisp.common.exception.BizException;
import com.aeisp.recharge.dto.OrderQueryRequest;
import com.aeisp.recharge.dto.OrderVO;
import com.aeisp.recharge.entity.DurationPackage;
import com.aeisp.recharge.entity.RechargeOrder;
import com.aeisp.recharge.mapper.DurationPackageMapper;
import com.aeisp.recharge.mapper.RechargeOrderMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

/**
 * OrderServiceImpl 单元测试。
 */
@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private RechargeOrderMapper orderMapper;

    @Mock
    private DurationPackageMapper packageMapper;

    @InjectMocks
    private OrderServiceImpl orderService;

    @Test
    void testCreateOrderSuccess() {
        DurationPackage pkg = new DurationPackage();
        pkg.setId(1L);
        pkg.setPriceCents(9900);
        pkg.setDurationMinutes(6000L);
        when(packageMapper.selectById(1L)).thenReturn(pkg);
        when(orderMapper.insert(any(RechargeOrder.class))).thenAnswer(inv -> {
            RechargeOrder o = inv.getArgument(0);
            o.setId(1L);
            return 1;
        });

        OrderVO vo = orderService.createOrder(1L, 1L);
        assertNotNull(vo);
        assertEquals(1L, vo.getUserId());
        assertEquals(9900, vo.getAmount());
        assertEquals(100L, vo.getDurationHours());
        assertEquals(0, vo.getStatus()); // entity 1 -> vo 0
        assertNotNull(vo.getOrderNo());
        assertTrue(vo.getOrderNo().startsWith("RO"));
    }

    @Test
    void testCreateOrderPackageNotFound() {
        when(packageMapper.selectById(1L)).thenReturn(null);
        BizException ex = assertThrows(BizException.class, () -> orderService.createOrder(1L, 1L));
        assertEquals("套餐不存在", ex.getMessage());
    }

    @Test
    void testPayOrderSuccess() {
        RechargeOrder order = new RechargeOrder();
        order.setId(1L);
        order.setOrderNo("RO20260101120000001");
        order.setStatus(1);
        when(orderMapper.selectByOrderNo("RO20260101120000001")).thenReturn(order);
        when(orderMapper.updateById(any(RechargeOrder.class))).thenReturn(1);

        assertTrue(orderService.payOrder("RO20260101120000001", "alipay"));
        verify(orderMapper).updateById(argThat((RechargeOrder o) -> o.getStatus() == 2 && "alipay".equals(o.getPayType())));
    }

    @Test
    void testPayOrderNotFound() {
        when(orderMapper.selectByOrderNo("NO")).thenReturn(null);
        BizException ex = assertThrows(BizException.class, () -> orderService.payOrder("NO", "alipay"));
        assertEquals("订单不存在", ex.getMessage());
    }

    @Test
    void testPayOrderWrongStatus() {
        RechargeOrder order = new RechargeOrder();
        order.setStatus(2);
        when(orderMapper.selectByOrderNo("RO")).thenReturn(order);
        BizException ex = assertThrows(BizException.class, () -> orderService.payOrder("RO", "alipay"));
        assertEquals("订单状态不正确，无法支付", ex.getMessage());
    }

    @Test
    void testRefundOrderSuccess() {
        RechargeOrder order = new RechargeOrder();
        order.setId(1L);
        order.setOrderNo("RO");
        order.setStatus(2);
        when(orderMapper.selectByOrderNo("RO")).thenReturn(order);
        when(orderMapper.updateById(any(RechargeOrder.class))).thenReturn(1);

        assertTrue(orderService.refundOrder("RO", "test refund"));
        verify(orderMapper).updateById(argThat((RechargeOrder o) -> o.getStatus() == 3));
    }

    @Test
    void testRefundOrderWrongStatus() {
        RechargeOrder order = new RechargeOrder();
        order.setStatus(1);
        when(orderMapper.selectByOrderNo("RO")).thenReturn(order);
        BizException ex = assertThrows(BizException.class, () -> orderService.refundOrder("RO", "reason"));
        assertEquals("订单状态不正确，无法退款", ex.getMessage());
    }

    @Test
    void testCancelOrderSuccess() {
        RechargeOrder order = new RechargeOrder();
        order.setId(1L);
        order.setOrderNo("RO");
        order.setStatus(1);
        when(orderMapper.selectByOrderNo("RO")).thenReturn(order);
        when(orderMapper.updateById(any(RechargeOrder.class))).thenReturn(1);

        assertTrue(orderService.cancelOrder("RO"));
        verify(orderMapper).updateById(argThat((RechargeOrder o) -> o.getStatus() == 4));
    }

    @Test
    void testCancelOrderWrongStatus() {
        RechargeOrder order = new RechargeOrder();
        order.setStatus(2);
        when(orderMapper.selectByOrderNo("RO")).thenReturn(order);
        BizException ex = assertThrows(BizException.class, () -> orderService.cancelOrder("RO"));
        assertEquals("订单状态不正确，无法取消", ex.getMessage());
    }

    @Test
    void testCancelAlreadyCanceledOrder() {
        RechargeOrder order = new RechargeOrder();
        order.setStatus(4);
        when(orderMapper.selectByOrderNo("RO")).thenReturn(order);
        BizException ex = assertThrows(BizException.class, () -> orderService.cancelOrder("RO"));
        assertEquals("订单状态不正确，无法取消", ex.getMessage());
    }

    @Test
    void testPayAlreadyPaidOrder() {
        RechargeOrder order = new RechargeOrder();
        order.setStatus(2);
        when(orderMapper.selectByOrderNo("RO")).thenReturn(order);
        BizException ex = assertThrows(BizException.class, () -> orderService.payOrder("RO", "alipay"));
        assertEquals("订单状态不正确，无法支付", ex.getMessage());
    }

    @Test
    void testRefundAlreadyRefundedOrder() {
        RechargeOrder order = new RechargeOrder();
        order.setStatus(3);
        when(orderMapper.selectByOrderNo("RO")).thenReturn(order);
        BizException ex = assertThrows(BizException.class, () -> orderService.refundOrder("RO", "reason"));
        assertEquals("订单状态不正确，无法退款", ex.getMessage());
    }

    @Test
    void testListOrders() {
        RechargeOrder order = new RechargeOrder();
        order.setId(1L);
        order.setOrderNo("RO001");
        order.setStatus(2);
        order.setAmountCents(9900);
        Page<RechargeOrder> page = new Page<>(1, 10);
        page.setRecords(List.of(order));
        page.setTotal(1);
        when(orderMapper.selectPage(any(Page.class), any())).thenReturn(page);

        OrderQueryRequest request = new OrderQueryRequest();
        request.setPageNum(1L);
        request.setPageSize(10L);
        request.setStatus(1); // vo status 1 = entity status 2
        PageResult<OrderVO> result = orderService.listOrders(request);

        assertEquals(1, result.getTotal());
        assertEquals(1, result.getList().get(0).getStatus()); // entity 2 -> vo 1
    }
}
