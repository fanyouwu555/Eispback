package com.aeisp.message.mapper;

import com.aeisp.message.entity.MsgNotification;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 消息通知 Mapper 接口。
 *
 * <p>提供 {@code msg_notification} 表的 CRUD 操作及自定义查询方法。</p>
 *
 * @author AEISP Team
 */
public interface MsgNotificationMapper extends BaseMapper<MsgNotification> {

    /**
     * 查询用户的未读消息列表。
     *
     * <p>通过关联 {@code msg_user_notification} 表，筛选出指定用户未读的消息记录。</p>
     *
     * @param userId 用户 ID
     * @return 未读消息列表
     */
    List<MsgNotification> selectUnreadByUserId(@Param("userId") Long userId);

    /**
     * 查询用户未读消息数量。
     *
     * @param userId 用户 ID
     * @return 未读消息数
     */
    Long selectUnreadCountByUserId(@Param("userId") Long userId);

    /**
     * 查询已到推送时间的定时消息。
     *
     * <p>筛选条件：push_type=2（定时推送）、status=0（草稿）、push_time <= 当前时间。</p>
     *
     * @return 待推送的消息列表
     */
    List<MsgNotification> selectScheduledNotificationsToPush();
}
