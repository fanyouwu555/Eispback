package com.aeisp.message.service.impl;

import com.aeisp.common.PageResult;
import com.aeisp.message.entity.MsgNotification;
import com.aeisp.message.entity.MsgUserNotification;
import com.aeisp.message.enums.MsgTypeEnum;
import com.aeisp.message.mapper.MsgNotificationMapper;
import com.aeisp.message.mapper.MsgUserNotificationMapper;
import com.aeisp.message.request.NotificationQueryRequest;
import com.aeisp.message.service.MsgUserNotificationService;
import com.aeisp.message.vo.UserNotificationVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户消息关联 Service 实现类。
 *
 * <p>实现用户视角的消息查询、已读标记及未读统计等业务逻辑。</p>
 *
 * @author AEISP Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MsgUserNotificationServiceImpl extends ServiceImpl<MsgUserNotificationMapper, MsgUserNotification>
        implements MsgUserNotificationService {

    private final MsgUserNotificationMapper msgUserNotificationMapper;
    private final MsgNotificationMapper msgNotificationMapper;

    @Override
    public PageResult<UserNotificationVO> listUserNotifications(Long userId, NotificationQueryRequest request) {
        Page<MsgUserNotification> page = new Page<>(request.getPageNum(), request.getPageSize());
        LambdaQueryWrapper<MsgUserNotification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MsgUserNotification::getUserId, userId);
        // 消息铃铛默认展示最近 90 天内的消息记录
        wrapper.ge(MsgUserNotification::getCreatedAt, LocalDateTime.now().minusDays(90));
        wrapper.orderByDesc(MsgUserNotification::getCreatedAt);
        IPage<MsgUserNotification> resultPage = msgUserNotificationMapper.selectPage(page, wrapper);

        List<UserNotificationVO> voList = resultPage.getRecords().stream()
                .map(this::convertToVO)
                .toList();
        return PageResult.of(resultPage, voList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean markAsRead(Long userId, Long notificationId) {
        int rows = msgUserNotificationMapper.markAsRead(userId, notificationId);
        if (rows > 0) {
            updateReadCount(notificationId);
        }
        return rows > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean markAllAsRead(Long userId) {
        int rows = msgUserNotificationMapper.markAllAsRead(userId);
        return rows >= 0;
    }

    @Override
    public Long getUnreadCount(Long userId) {
        Long count = msgNotificationMapper.selectUnreadCountByUserId(userId);
        return count != null ? count : 0L;
    }

    /**
     * 更新消息的已读人数统计。
     *
     * @param notificationId 消息通知 ID
     */
    private void updateReadCount(Long notificationId) {
        try {
            Long readCount = msgUserNotificationMapper.selectCount(
                    new LambdaQueryWrapper<MsgUserNotification>()
                            .eq(MsgUserNotification::getNotificationId, notificationId)
                            .eq(MsgUserNotification::getReadStatus, 2));
            MsgNotification notification = new MsgNotification();
            notification.setId(notificationId);
            notification.setReadCount(readCount);
            msgNotificationMapper.updateById(notification);
        } catch (Exception e) {
            log.warn("更新消息已读人数失败, notificationId={}", notificationId, e);
        }
    }

    /**
     * 转换为用户消息 VO。
     *
     * @param userNotification 用户消息关联实体
     * @return VO
     */
    private UserNotificationVO convertToVO(MsgUserNotification userNotification) {
        UserNotificationVO vo = new UserNotificationVO();
        vo.setId(userNotification.getId());
        vo.setNotificationId(userNotification.getNotificationId());
        vo.setReadStatus(userNotification.getReadStatus());
        vo.setReadAt(userNotification.getReadAt());
        vo.setCreatedAt(userNotification.getCreatedAt());

        MsgNotification notification = msgNotificationMapper.selectById(userNotification.getNotificationId());
        if (notification != null) {
            vo.setTitle(notification.getTitle());
            vo.setContentSummary(truncateContent(notification.getContent()));
            vo.setMsgType(notification.getMsgType());
            vo.setMsgTypeLabel(getMsgTypeLabel(notification.getMsgType()));
        }
        return vo;
    }

    /**
     * 截断内容为摘要。
     *
     * @param content 原始内容
     * @return 摘要文本
     */
    private static String truncateContent(String content) {
        if (content == null) {
            return "";
        }
        int maxLength = 100;
        if (content.length() <= maxLength) {
            return content;
        }
        return content.substring(0, maxLength) + "...";
    }

    private static String getMsgTypeLabel(Integer value) {
        MsgTypeEnum e = MsgTypeEnum.fromValue(value);
        return e != null ? e.getLabel() : "";
    }
}
