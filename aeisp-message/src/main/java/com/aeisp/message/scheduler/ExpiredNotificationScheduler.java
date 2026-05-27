package com.aeisp.message.scheduler;

import com.aeisp.message.mapper.MsgNotificationMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 公告自动过期扫描任务。
 *
 * <p>每分钟扫描一次已发送（status=2）但已超过 expire_time 的公告，
 * 自动将其状态更新为 6（已过期）。</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExpiredNotificationScheduler {

    private final MsgNotificationMapper msgNotificationMapper;

    /**
     * 每分钟执行一次，将过期公告标记为"已过期"。
     */
    @Scheduled(cron = "0 * * * * *")
    public void scanExpiredNotifications() {
        LocalDateTime now = LocalDateTime.now();
        int updated = msgNotificationMapper.markExpired(now);
        if (updated > 0) {
            log.info("公告过期扫描: {} 条公告已标记为已过期", updated);
        }
    }
}