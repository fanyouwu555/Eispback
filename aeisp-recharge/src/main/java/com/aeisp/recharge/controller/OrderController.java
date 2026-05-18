package com.aeisp.recharge.controller;

import com.aeisp.common.PageResult;
import com.aeisp.common.Result;
import com.aeisp.recharge.dto.OrderQueryRequest;
import com.aeisp.recharge.dto.OrderVO;
import com.aeisp.recharge.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 充值订单控制器。
 *
 * <p>提供订单创建、支付、退款、取消及分页查询接口。</p>
 *
 * @author AEISP Team
 */
@Tag(name = "充值订单", description = "订单管理")
@RestController
@RequestMapping("/api/v1/recharge/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * 创建订单。
     *
     * @param userId    用户 ID
     * @param packageId 套餐 ID
     * @return 创建的订单
     */
    @Operation(summary = "创建订单")
    @PostMapping
    public Result<OrderVO> createOrder(@RequestParam Long userId, @RequestParam Long packageId) {
        return Result.success(orderService.createOrder(userId, packageId));
    }

    /**
     * 支付订单。
     *
     * @param orderNo 订单号
     * @param payType 支付方式
     * @return 操作结果
     */
    @Operation(summary = "支付订单")
    @PostMapping("/{orderNo}/pay")
    public Result<Boolean> payOrder(@PathVariable String orderNo, @RequestParam String payType) {
        return Result.success(orderService.payOrder(orderNo, payType));
    }

    /**
     * 退款订单。
     *
     * @param orderNo 订单号
     * @param reason  退款原因
     * @return 操作结果
     */
    @Operation(summary = "退款订单")
    @PostMapping("/{orderNo}/refund")
    public Result<Boolean> refundOrder(@PathVariable String orderNo, @RequestParam String reason) {
        return Result.success(orderService.refundOrder(orderNo, reason));
    }

    /**
     * 取消订单。
     *
     * @param orderNo 订单号
     * @return 操作结果
     */
    @Operation(summary = "取消订单")
    @PostMapping("/{orderNo}/cancel")
    public Result<Boolean> cancelOrder(@PathVariable String orderNo) {
        return Result.success(orderService.cancelOrder(orderNo));
    }

    /**
     * 分页查询订单列表。
     *
     * @param request 查询条件
     * @return 分页结果
     */
    @Operation(summary = "订单列表")
    @GetMapping
    public Result<PageResult<OrderVO>> listOrders(OrderQueryRequest request) {
        return Result.success(orderService.listOrders(request));
    }
}
