package com.aeisp.recharge.controller;

import com.aeisp.common.PageResult;
import com.aeisp.common.Result;
import com.aeisp.recharge.dto.OrderQueryRequest;
import com.aeisp.recharge.dto.OrderVO;
import com.aeisp.recharge.service.OrderService;
import com.aeisp.system.annotation.OperationLog;
import com.aeisp.system.entity.SysUserBehaviorLog;
import com.aeisp.system.mapper.SysUserBehaviorLogMapper;
import com.aeisp.system.service.SysFeatureSwitchService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.aeisp.common.code.CommonErrorCode;
import com.aeisp.common.exception.BizException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 充值订单控制器。
 *
 * <p>提供订单创建、支付、退款、取消及分页查询接口。</p>
 *
 * @author AEISP Team
 */
@Slf4j
@Tag(name = "充值订单", description = "订单管理")
@RestController
@RequestMapping("/api/v1/recharge/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final PasswordEncoder passwordEncoder;
    private final SysUserBehaviorLogMapper behaviorLogMapper;
    private final ObjectMapper objectMapper;
    private final SysFeatureSwitchService featureSwitchService;

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
        if (!featureSwitchService.isEnabled("recharge")) {
            return Result.error(503, "充值功能已关闭");
        }
        OrderVO order = orderService.createOrder(userId, packageId);
        saveBehaviorLog(userId, "RECHARGE", Map.of("orderNo", order.getOrderNo(), "amount", order.getAmount()));
        return Result.success(order);
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
        if (!featureSwitchService.isEnabled("order.pay")) {
            return Result.error(503, "订单支付功能已关闭");
        }
        return Result.success(orderService.payOrder(orderNo, payType));
    }

    /**
     * 退款订单。
     *
     * @param orderNo 订单号
     * @param reason  退款原因
     * @return 操作结果
     */
    @PreAuthorize("hasAuthority('order:refund')")
    @OperationLog(module = "订单管理", operation = "退款", sensitivity = 2)
    @Operation(summary = "退款订单")
    @PostMapping("/{orderNo}/refund")
    public Result<Boolean> refundOrder(@PathVariable String orderNo,
                                       @RequestParam String reason,
                                       @RequestParam String adminPassword) {
        verifyAdminPassword(adminPassword);
        return Result.success(orderService.refundOrder(orderNo, reason));
    }

    private void verifyAdminPassword(String adminPassword) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof UserDetails details)) {
            throw new BizException(CommonErrorCode.ACCESS_DENIED, "无法获取当前管理员信息");
        }
        if (!passwordEncoder.matches(adminPassword, details.getPassword())) {
            throw new BizException(CommonErrorCode.ACCESS_DENIED, "管理员密码错误");
        }
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
    @PreAuthorize("hasAuthority('order:manage')")
    @Operation(summary = "订单列表")
    @GetMapping
    public Result<PageResult<OrderVO>> listOrders(OrderQueryRequest request) {
        return Result.success(orderService.listOrders(request));
    }

    private void saveBehaviorLog(Long userId, String behaviorType, Map<String, Object> detail) {
        try {
            SysUserBehaviorLog behaviorLog = new SysUserBehaviorLog();
            behaviorLog.setUserId(userId);
            behaviorLog.setBehaviorType(behaviorType);
            behaviorLog.setBehaviorDetail(objectMapper.writeValueAsString(detail));
            behaviorLog.setCreatedAt(LocalDateTime.now());
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                behaviorLog.setIpAddress(attrs.getRequest().getRemoteAddr());
            }
            behaviorLogMapper.insert(behaviorLog);
        } catch (Exception e) {
            log.warn("记录用户行为日志失败: {}", e.getMessage());
        }
    }
}
