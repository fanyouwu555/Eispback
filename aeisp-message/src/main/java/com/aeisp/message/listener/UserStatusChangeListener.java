package com.aeisp.message.listener;

import com.aeisp.common.event.UserStatusChangeEvent;
import com.aeisp.message.entity.MsgNotification;
import com.aeisp.message.enums.MsgTypeEnum;
import com.aeisp.message.enums.PushScopeEnum;
import com.aeisp.message.enums.PushTypeEnum;
import com.aeisp.message.mapper.MsgNotificationMapper;
import com.aeisp.message.service.MsgNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户状态变更事件监听器。
 *
 * <p>监听 {@link UserStatusChangeEvent}，向被操作用户发送站内消息通知。</p>
 *
 * @author AEISP Team
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserStatusChangeListener {

    private final MsgNotificationMapper msgNotificationMapper;
    private final MsgNotificationService msgNotificationService;

    @EventListener
    @Transactional(rollbackFor = Exception.class)
    public void onUserStatusChange(UserStatusChangeEvent event) {
        String statusLabel = switch (event.getNewStatus()) {
            case 2 -> "禁用";
            case 3 -> "冻结";
            case 4 -> "锁定";
            default -> "正常";
        };

        StringBuilder content = new StringBuilder();
        content.append("您的账号状态已变更为：").append(statusLabel).append("。");
        if (event.getReason() != null && !event.getReason().isEmpty()) {
            content.append("原因：").append(event.getReason()).append("。");
        }

        MsgNotification notification = new MsgNotification();
        notification.setTitle("账号状态变更通知");
        notification.setContent(content.toString());
        notification.setMsgType(MsgTypeEnum.SYSTEM_NOTICE.getValue());
        notification.setPushScope(PushScopeEnum.SPECIFIC_USERS.getValue());
        notification.setPushTarget("[" + event.getUserId() + "]");
        notification.setPushType(PushTypeEnum.IMMEDIATE.getValue());
        notification.setStatus(1); // 草稿
        notification.setIsTop(0);
        notification.setReadCount(0L);
        notification.setTotalCount(0L);
        notification.setDeleted(0);
        msgNotificationMapper.insert(notification);

        try {
            msgNotificationService.pushNotification(notification.getId());
        } catch (Exception e) {
            log.warn("状态变更通知推送失败，userId={}，msgId={}，原因：{}",
                    event.getUserId(), notification.getId(), e.getMessage());
        }
    }
}
