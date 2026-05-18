package com.aeisp.message.mapper;

import com.aeisp.message.entity.MsgUserNotification;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户消息关联 Mapper 接口。
 *
 * <p>提供 {@code msg_user_notification} 表的 CRUD 操作及批量处理方法。</p>
 *
 * @author AEISP Team
 */
public interface MsgUserNotificationMapper extends BaseMapper<MsgUserNotification> {

    /**
     * 批量插入用户消息关联记录。
     *
     * @param list 用户消息关联列表
     * @return 插入记录数
     */
    int batchInsert(@Param("list") List<MsgUserNotification> list);

    /**
     * 标记单条消息为已读。
     *
     * @param userId         用户 ID
     * @param notificationId 消息通知 ID
     * @return 影响行数
     */
    int markAsRead(@Param("userId") Long userId, @Param("notificationId") Long notificationId);

    /**
     * 标记用户所有消息为已读。
     *
     * @param userId 用户 ID
     * @return 影响行数
     */
    int markAllAsRead(@Param("userId") Long userId);
}
