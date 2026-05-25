package com.aeisp.recharge.service.impl;

import com.aeisp.common.PageResult;
import com.aeisp.common.exception.BizException;
import com.aeisp.recharge.code.RechargeErrorCode;
import com.aeisp.recharge.dto.OrderQueryRequest;
import com.aeisp.recharge.dto.OrderVO;
import com.aeisp.recharge.entity.DurationPackage;
import com.aeisp.recharge.entity.RechargeOrder;
import com.aeisp.recharge.mapper.DurationPackageMapper;
import com.aeisp.recharge.mapper.RechargeOrderMapper;
import com.aeisp.recharge.service.OrderService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 充值订单服务实现类。
 *
 * @author AEISP Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final RechargeOrderMapper orderMapper;
    private final DurationPackageMapper packageMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderVO createOrder(Long userId, Long packageId) {
        DurationPackage pkg = packageMapper.selectById(packageId);
        if (pkg == null) {
            throw new BizException(RechargeErrorCode.PACKAGE_NOT_FOUND);
        }

        RechargeOrder order = new RechargeOrder();
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setPackageId(packageId);
        order.setAmountCents(pkg.getPriceCents());
        order.setDurationMinutes(pkg.getDurationMinutes());
        order.setStatus(1); // 待支付
        orderMapper.insert(order);

        return convertToVO(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean payOrder(String orderNo, String payType) {
        RechargeOrder order = orderMapper.selectByOrderNo(orderNo);
        if (order == null) {
            throw new BizException(RechargeErrorCode.ORDER_NOT_FOUND);
        }
        if (!Integer.valueOf(1).equals(order.getStatus())) {
            throw new BizException(RechargeErrorCode.ORDER_STATUS_PAY_INVALID);
        }
        order.setStatus(2); // 已支付
        order.setPayType(payType);
        order.setPayTime(LocalDateTime.now());
        return orderMapper.updateById(order) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean refundOrder(String orderNo, String reason) {
        RechargeOrder order = orderMapper.selectByOrderNo(orderNo);
        if (order == null) {
            throw new BizException(RechargeErrorCode.ORDER_NOT_FOUND);
        }
        if (!Integer.valueOf(2).equals(order.getStatus())) {
            throw new BizException(RechargeErrorCode.ORDER_STATUS_REFUND_INVALID);
        }
        order.setStatus(3); // 已退款
        order.setRefundReason(reason);
        return orderMapper.updateById(order) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelOrder(String orderNo) {
        RechargeOrder order = orderMapper.selectByOrderNo(orderNo);
        if (order == null) {
            throw new BizException(RechargeErrorCode.ORDER_NOT_FOUND);
        }
        if (!Integer.valueOf(1).equals(order.getStatus())) {
            throw new BizException(RechargeErrorCode.ORDER_STATUS_CANCEL_INVALID);
        }
        order.setStatus(4); // 已取消
        return orderMapper.updateById(order) > 0;
    }

    @Override
    public PageResult<OrderVO> listOrders(OrderQueryRequest request) {
        Page<RechargeOrder> page = new Page<>(request.getPageNum(), request.getPageSize());
        Integer dbStatus = request.getStatus() != null ? request.getStatus() + 1 : null;
        LambdaQueryWrapper<RechargeOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(request.getUserId() != null, RechargeOrder::getUserId, request.getUserId())
                .eq(StringUtils.hasText(request.getOrderNo()), RechargeOrder::getOrderNo, request.getOrderNo())
                .eq(request.getStatus() != null, RechargeOrder::getStatus, dbStatus)
                .orderByDesc(RechargeOrder::getCreatedAt);
        Page<RechargeOrder> resultPage = orderMapper.selectPage(page, wrapper);

        List<OrderVO> voList = new ArrayList<>();
        for (RechargeOrder o : resultPage.getRecords()) {
            voList.add(convertToVO(o));
        }
        return PageResult.of(resultPage, voList);
    }

    private String generateOrderNo() {
        return "RO" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + (System.currentTimeMillis() % 10000);
    }

    private OrderVO convertToVO(RechargeOrder order) {
        OrderVO vo = new OrderVO();
        BeanUtils.copyProperties(order, vo);
        vo.setAmount(order.getAmountCents());
        vo.setDurationHours(order.getDurationMinutes() != null ? order.getDurationMinutes() / 60 : null);
        vo.setOrderTime(order.getCreatedAt());
        // Entity status: 1=待支付,2=已支付,3=已退款,4=已取消
        // VO status: 0=待支付,1=已支付,2=已退款,3=已取消
        if (order.getStatus() != null) {
            vo.setStatus(order.getStatus() - 1);
        }
        return vo;
    }
}
