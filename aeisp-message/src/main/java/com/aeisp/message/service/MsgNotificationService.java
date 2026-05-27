package com.aeisp.message.service;

import com.aeisp.common.PageResult;
import com.aeisp.message.request.CreateNotificationRequest;
import com.aeisp.message.request.UpdateNotificationRequest;
import com.aeisp.message.request.NotificationQueryRequest;
import com.aeisp.message.vo.MsgNotificationDetailVO;
import com.aeisp.message.vo.MsgNotificationVO;
import com.aeisp.message.vo.ClientAnnouncementVO;

import java.util.List;

/**
 * 消息通知 Service 接口。
 *
 * <p>提供消息通知的创建、推送、撤回、归档、置顶及查询等核心业务能力。</p>
 *
 * @author AEISP Team
 */
public interface MsgNotificationService {

    /**
     * 创建消息通知（草稿状态）。
     *
     * @param request 创建请求
     * @return 是否创建成功
     */
    boolean createNotification(CreateNotificationRequest request);

    /**
     * 更新消息通知（仅草稿状态可编辑）。
     *
     * @param id      消息通知 ID
     * @param request 更新请求
     * @return 是否更新成功
     */
    boolean updateNotification(Long id, UpdateNotificationRequest request);

    /**
     * 执行消息推送。
     *
     * <p>根据消息的推送范围生成对应的 {@code msg_user_notification} 记录，
     * 并更新消息状态为已推送。</p>
     *
     * @param notificationId 消息通知 ID
     * @return 是否推送成功
     */
    boolean pushNotification(Long notificationId);

    /**
     * 撤回消息通知。
     *
     * <p>仅允许撤回已推送且未过期的消息，撤回后用户端不再展示。</p>
     *
     * @param notificationId 消息通知 ID
     * @return 是否撤回成功
     */
    boolean revokeNotification(Long notificationId);

    /**
     * 归档消息通知。
     *
     * <p>将消息状态变更为已归档，归档后的消息仅保留记录，不再主动推送。</p>
     *
     * @param notificationId 消息通知 ID
     * @return 是否归档成功
     */
    boolean archiveNotification(Long notificationId);

    /**
     * 置顶或取消置顶消息。
     *
     * @param notificationId 消息通知 ID
     * @param isTop          是否置顶：0-正常，1-置顶
     * @return 是否操作成功
     */
    boolean toggleTop(Long notificationId, Integer isTop);

    /**
     * 分页查询消息通知列表（后台管理）。
     *
     * @param request 查询条件
     * @return 分页结果
     */
    PageResult<MsgNotificationVO> listNotifications(NotificationQueryRequest request);

    /**
     * 获取消息通知详情（后台管理）。
     *
     * <p>包含完整内容、推送目标用户列表及已读/未读统计。</p>
     *
     * @param notificationId 消息通知 ID
     * @return 消息详情
     */
    MsgNotificationDetailVO getDetail(Long notificationId);

    /**
     * 获取客户端公告列表（已发送且未过期）。
     *
     * @return 公告列表
     */
    List<ClientAnnouncementVO> listClientAnnouncements();
}
