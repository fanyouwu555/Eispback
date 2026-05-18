package com.aeisp.message.task;

import com.aeisp.message.entity.MsgNotification;
import com.aeisp.message.mapper.MsgNotificationMapper;
import com.aeisp.message.service.MsgNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 定时消息推送任务。
 *
 * <p>每分钟轮询一次，查找已到推送时间的定时消息并自动执行推送。</p>
 *
 * @author AEISP Team
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ScheduledNotificationTask {

    private final MsgNotificationMapper msgNotificationMapper;
    private final MsgNotificationService msgNotificationService;

    /**
     * 每分钟执行一次，检查并推送已到期的定时消息。
     */
    @Scheduled(cron = "0 */1 * * * *")
    public void pushScheduledNotifications() {
        List<MsgNotification> notifications = msgNotificationMapper.selectScheduledNotificationsToPush();
        if (notifications.isEmpty()) {
            return;
        }
        log.info("发现 {} 条待推送的定时消息", notifications.size());
        for (MsgNotification notification : notifications) {
            try {
                msgNotificationService.pushNotification(notification.getId());
                log.info("定时消息推送成功, notificationId={}", notification.getId());
            } catch (Exception e) {
                log.error("定时消息推送失败, notificationId={}", notification.getId(), e);
            }
        }
    }
}
