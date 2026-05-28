package com.aeisp.recharge.scheduler;

import com.aeisp.recharge.entity.RechargeOrder;
import com.aeisp.recharge.mapper.RechargeOrderMapper;
import com.aeisp.system.service.SysConfigService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单超时自动取消定时任务。
 *
 * <p>每 5 分钟扫描一次待支付订单，将超过 {@code timeout.order} 配置时间的订单自动取消。</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderTimeoutScheduler {

    private final RechargeOrderMapper orderMapper;
    private final SysConfigService sysConfigService;

    private static final int DEFAULT_TIMEOUT_MINUTES = 30;

    /**
     * 每 5 分钟执行一次订单超时扫描。
     */
    @Scheduled(cron = "0 */5 * * * *")
    @Transactional(rollbackFor = Exception.class)
    public void cancelTimeoutOrders() {
        int timeoutMinutes = resolveTimeoutMinutes();
        LocalDateTime deadline = LocalDateTime.now().minusMinutes(timeoutMinutes);

        LambdaQueryWrapper<RechargeOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RechargeOrder::getStatus, 1)
                .lt(RechargeOrder::getCreatedAt, deadline);

        List<RechargeOrder> timeoutOrders = orderMapper.selectList(wrapper);
        if (timeoutOrders.isEmpty()) {
            return;
        }

        for (RechargeOrder order : timeoutOrders) {
            order.setStatus(4);
            orderMapper.updateById(order);
            log.info("订单超时自动取消: orderNo={}, createdAt={}, timeout={}分钟",
                    order.getOrderNo(), order.getCreatedAt(), timeoutMinutes);
        }

        log.info("订单超时扫描完成: {} 条订单已自动取消", timeoutOrders.size());
    }

    private int resolveTimeoutMinutes() {
        String timeoutStr = sysConfigService.getConfigValue("timeout.order", "all");
        if (!StringUtils.hasText(timeoutStr)) {
            return DEFAULT_TIMEOUT_MINUTES;
        }
        try {
            int minutes = Integer.parseInt(timeoutStr.trim());
            return minutes > 0 ? minutes : DEFAULT_TIMEOUT_MINUTES;
        } catch (NumberFormatException e) {
            log.warn("timeout.order 配置值 '{}' 无法解析，使用默认值 {} 分钟", timeoutStr, DEFAULT_TIMEOUT_MINUTES);
            return DEFAULT_TIMEOUT_MINUTES;
        }
    }
}
