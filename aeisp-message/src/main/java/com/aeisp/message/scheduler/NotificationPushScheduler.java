package com.aeisp.message.scheduler;

import com.aeisp.message.entity.MsgNotification;
import com.aeisp.message.enums.PushTypeEnum;
import com.aeisp.message.mapper.MsgNotificationMapper;
import com.aeisp.message.service.MsgNotificationService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 消息定时推送扫描任务。
 *
 * <p>每分钟扫描一次状态为"定时发送"且推送时间已到达的消息，自动执行推送。</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationPushScheduler {

    private final MsgNotificationMapper msgNotificationMapper;
    private final MsgNotificationService msgNotificationService;

    /**
     * 每分钟扫描一次待推送的定时消息。
     */
    @Scheduled(cron = "0 * * * * *")
    public void scanScheduledNotifications() {
        LocalDateTime now = LocalDateTime.now();
        LambdaQueryWrapper<MsgNotification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MsgNotification::getStatus, 4) // 定时发送
                .eq(MsgNotification::getPushType, PushTypeEnum.SCHEDULED.getValue())
                .le(MsgNotification::getPushTime, now);

        List<MsgNotification> list = msgNotificationMapper.selectList(wrapper);
        if (list.isEmpty()) {
            return;
        }

        log.info("扫描到 {} 条待推送的定时消息", list.size());
        for (MsgNotification notification : list) {
            try {
                msgNotificationService.pushNotification(notification.getId());
                log.info("定时消息推送成功: id={}", notification.getId());
            } catch (Exception e) {
                log.error("定时消息推送失败: id={}, error={}", notification.getId(), e.getMessage());
            }
        }
    }
}
