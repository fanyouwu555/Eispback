package com.aeisp.message.service;

import com.aeisp.common.PageResult;
import com.aeisp.message.request.NotificationQueryRequest;
import com.aeisp.message.vo.UserNotificationVO;

/**
 * 用户消息关联 Service 接口。
 *
 * <p>提供用户视角的消息查询、已读标记及未读统计等能力。</p>
 *
 * @author AEISP Team
 */
public interface MsgUserNotificationService {

    /**
     * 分页查询当前用户的消息列表。
     *
     * @param userId  用户 ID
     * @param request 查询条件
     * @return 分页结果
     */
    PageResult<UserNotificationVO> listUserNotifications(Long userId, NotificationQueryRequest request);

    /**
     * 标记单条消息为已读。
     *
     * @param userId         用户 ID
     * @param notificationId 消息通知 ID
     * @return 是否操作成功
     */
    boolean markAsRead(Long userId, Long notificationId);

    /**
     * 标记用户所有消息为已读。
     *
     * @param userId 用户 ID
     * @return 是否操作成功
     */
    boolean markAllAsRead(Long userId);

    /**
     * 获取用户未读消息数量。
     *
     * @param userId 用户 ID
     * @return 未读消息数
     */
    Long getUnreadCount(Long userId);
}
