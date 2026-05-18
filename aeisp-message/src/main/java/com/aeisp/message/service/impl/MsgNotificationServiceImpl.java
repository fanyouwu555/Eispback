package com.aeisp.message.service.impl;

import com.aeisp.common.PageResult;
import com.aeisp.common.constant.CommonConstants;
import com.aeisp.common.exception.BizException;
import com.aeisp.message.dto.PushTargetDTO;
import com.aeisp.message.entity.MsgNotification;
import com.aeisp.message.entity.MsgUserNotification;
import com.aeisp.message.enums.MsgTypeEnum;
import com.aeisp.message.enums.PushScopeEnum;
import com.aeisp.message.enums.PushTypeEnum;
import com.aeisp.message.mapper.MsgNotificationMapper;
import com.aeisp.message.mapper.MsgUserNotificationMapper;
import com.aeisp.message.request.CreateNotificationRequest;
import com.aeisp.message.request.NotificationQueryRequest;
import com.aeisp.message.service.MsgNotificationService;
import com.aeisp.message.vo.MsgNotificationDetailVO;
import com.aeisp.message.vo.MsgNotificationVO;
import com.aeisp.message.vo.PushTargetUserVO;
import com.aeisp.system.entity.SysUserRole;
import com.aeisp.system.mapper.SysUserRoleMapper;
import com.aeisp.user.entity.UsrUser;
import com.aeisp.user.mapper.UsrUserMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 消息通知 Service 实现类。
 *
 * <p>实现消息通知的创建、推送、撤回、归档、置顶及查询等业务逻辑。</p>
 *
 * @author AEISP Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MsgNotificationServiceImpl extends ServiceImpl<MsgNotificationMapper, MsgNotification>
        implements MsgNotificationService {

    private final MsgNotificationMapper msgNotificationMapper;
    private final MsgUserNotificationMapper msgUserNotificationMapper;
    private final UsrUserMapper usrUserMapper;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final ObjectMapper objectMapper;

    /**
     * 状态常量。
     */
    private static final int STATUS_DRAFT = 0;
    private static final int STATUS_PUSHED = 1;
    private static final int STATUS_REVOKED = 2;
    private static final int STATUS_ARCHIVED = 3;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createNotification(CreateNotificationRequest request) {
        MsgNotification notification = new MsgNotification();
        notification.setTitle(request.getTitle());
        notification.setContent(request.getContent());
        notification.setMsgType(request.getMsgType());
        notification.setPushScope(request.getPushScope());
        notification.setPushTarget(request.getPushTarget());
        notification.setPushType(request.getPushType());
        notification.setPushTime(request.getPushTime());
        notification.setExpireTime(request.getExpireTime());
        notification.setStatus(STATUS_DRAFT);
        notification.setIsTop(CommonConstants.STATUS_DISABLED);
        notification.setReadCount(0L);
        notification.setTotalCount(0L);
        return save(notification);
    }

    /**
     * 分批插入的批次大小。
     */
    private static final int BATCH_SIZE = 1000;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean pushNotification(Long notificationId) {
        MsgNotification notification = getById(notificationId);
        if (notification == null) {
            throw new BizException("消息不存在");
        }
        if (!Objects.equals(notification.getStatus(), STATUS_DRAFT)) {
            throw new BizException("只有草稿状态的消息才能推送");
        }

        PushScopeEnum scope = PushScopeEnum.fromValue(notification.getPushScope());
        if (scope == null) {
            throw new BizException("推送范围无效");
        }

        LocalDateTime now = LocalDateTime.now();
        long totalCount;

        // 全员推送采用分页分批处理，避免一次性加载大量用户导致内存溢出
        if (scope == PushScopeEnum.ALL) {
            totalCount = pushToAllUsers(notificationId, now);
        } else {
            List<Long> targetUserIds = resolveTargetUsers(notification);
            if (CollectionUtils.isEmpty(targetUserIds)) {
                throw new BizException("推送目标用户为空");
            }
            List<MsgUserNotification> userNotifications = targetUserIds.stream()
                    .distinct()
                    .map(userId -> buildUserNotification(userId, notificationId, now))
                    .collect(Collectors.toList());
            if (!userNotifications.isEmpty()) {
                msgUserNotificationMapper.batchInsert(userNotifications);
            }
            totalCount = userNotifications.size();
        }

        notification.setStatus(STATUS_PUSHED);
        notification.setTotalCount(totalCount);
        if (Objects.equals(notification.getPushType(), PushTypeEnum.IMMEDIATE.getValue())) {
            notification.setPushTime(now);
        }
        return updateById(notification);
    }

    /**
     * 分页全员推送，每批 {@link #BATCH_SIZE} 条。
     *
     * @param notificationId 消息 ID
     * @param now            当前时间
     * @return 推送用户总数
     */
    private long pushToAllUsers(Long notificationId, LocalDateTime now) {
        long totalCount = 0;
        long currentPage = 1;
        while (true) {
            Page<UsrUser> page = new Page<>(currentPage, BATCH_SIZE);
            Page<UsrUser> result = usrUserMapper.selectPage(page,
                    new LambdaQueryWrapper<UsrUser>()
                            .eq(UsrUser::getStatus, CommonConstants.STATUS_ENABLED)
                            .select(UsrUser::getId));
            List<UsrUser> records = result.getRecords();
            if (records.isEmpty()) {
                break;
            }
            List<MsgUserNotification> batch = records.stream()
                    .map(u -> buildUserNotification(u.getId(), notificationId, now))
                    .collect(Collectors.toList());
            if (!batch.isEmpty()) {
                msgUserNotificationMapper.batchInsert(batch);
            }
            totalCount += batch.size();
            if (currentPage >= result.getPages()) {
                break;
            }
            currentPage++;
        }
        return totalCount;
    }

    /**
     * 构建用户消息关联实体。
     */
    private MsgUserNotification buildUserNotification(Long userId, Long notificationId, LocalDateTime now) {
        MsgUserNotification un = new MsgUserNotification();
        un.setUserId(userId);
        un.setNotificationId(notificationId);
        un.setIsRead(CommonConstants.STATUS_DISABLED);
        un.setCreatedAt(now);
        return un;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean revokeNotification(Long notificationId) {
        MsgNotification notification = getById(notificationId);
        if (notification == null) {
            throw new BizException("消息不存在");
        }
        if (!Objects.equals(notification.getStatus(), STATUS_PUSHED)) {
            throw new BizException("只有已推送的消息才能撤回");
        }
        notification.setStatus(STATUS_REVOKED);
        boolean updated = updateById(notification);
        // 删除用户关联记录，确保用户端不再展示
        if (updated) {
            msgUserNotificationMapper.delete(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<MsgUserNotification>()
                            .eq(MsgUserNotification::getNotificationId, notificationId));
        }
        return updated;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean archiveNotification(Long notificationId) {
        MsgNotification notification = getById(notificationId);
        if (notification == null) {
            throw new BizException("消息不存在");
        }
        notification.setStatus(STATUS_ARCHIVED);
        return updateById(notification);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean toggleTop(Long notificationId, Integer isTop) {
        MsgNotification notification = getById(notificationId);
        if (notification == null) {
            throw new BizException("消息不存在");
        }
        notification.setIsTop(isTop);
        return updateById(notification);
    }

    @Override
    public PageResult<MsgNotificationVO> listNotifications(NotificationQueryRequest request) {
        Page<MsgNotification> page = new Page<>(request.getPageNum(), request.getPageSize());
        LambdaQueryWrapper<MsgNotification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Objects.nonNull(request.getMsgType()), MsgNotification::getMsgType, request.getMsgType());
        wrapper.eq(Objects.nonNull(request.getStatus()), MsgNotification::getStatus, request.getStatus());
        wrapper.ge(Objects.nonNull(request.getStartTime()), MsgNotification::getCreatedAt, request.getStartTime());
        wrapper.le(Objects.nonNull(request.getEndTime()), MsgNotification::getCreatedAt, request.getEndTime());
        wrapper.orderByDesc(MsgNotification::getIsTop);
        wrapper.orderByDesc(MsgNotification::getCreatedAt);
        IPage<MsgNotification> resultPage = msgNotificationMapper.selectPage(page, wrapper);

        List<MsgNotificationVO> voList = resultPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        return PageResult.of(resultPage, voList);
    }

    @Override
    public MsgNotificationDetailVO getDetail(Long notificationId) {
        MsgNotification notification = getById(notificationId);
        if (notification == null) {
            throw new BizException("消息不存在");
        }
        MsgNotificationDetailVO vo = new MsgNotificationDetailVO();
        vo.setId(notification.getId());
        vo.setTitle(notification.getTitle());
        vo.setContent(notification.getContent());
        vo.setMsgType(notification.getMsgType());
        vo.setMsgTypeLabel(getMsgTypeLabel(notification.getMsgType()));
        vo.setPushScope(notification.getPushScope());
        vo.setPushScopeLabel(getPushScopeLabel(notification.getPushScope()));
        vo.setPushTarget(notification.getPushTarget());
        vo.setPushType(notification.getPushType());
        vo.setPushTypeLabel(getPushTypeLabel(notification.getPushType()));
        vo.setPushTime(notification.getPushTime());
        vo.setExpireTime(notification.getExpireTime());
        vo.setStatus(notification.getStatus());
        vo.setStatusLabel(getStatusLabel(notification.getStatus()));
        vo.setIsTop(notification.getIsTop());
        vo.setReadCount(notification.getReadCount());
        vo.setTotalCount(notification.getTotalCount());
        vo.setCreatedAt(notification.getCreatedAt());
        vo.setUpdatedAt(notification.getUpdatedAt());

        List<PushTargetUserVO> targetUsers = new ArrayList<>();
        if (Objects.equals(notification.getStatus(), STATUS_PUSHED)
                || Objects.equals(notification.getStatus(), STATUS_REVOKED)
                || Objects.equals(notification.getStatus(), STATUS_ARCHIVED)) {
            List<MsgUserNotification> userNotifications = msgUserNotificationMapper.selectList(
                    new LambdaQueryWrapper<MsgUserNotification>()
                            .eq(MsgUserNotification::getNotificationId, notificationId));
            for (MsgUserNotification un : userNotifications) {
                PushTargetUserVO targetUser = new PushTargetUserVO();
                targetUser.setUserId(un.getUserId());
                UsrUser user = usrUserMapper.selectById(un.getUserId());
                targetUser.setUsername(user != null ? user.getUsername() : "");
                targetUser.setIsRead(un.getIsRead());
                targetUsers.add(targetUser);
            }
        }
        vo.setTargetUsers(targetUsers);
        return vo;
    }

    /**
     * 解析推送目标用户列表。
     *
     * @param notification 消息通知实体
     * @return 目标用户 ID 列表
     */
    private List<Long> resolveTargetUsers(MsgNotification notification) {
        PushScopeEnum scope = PushScopeEnum.fromValue(notification.getPushScope());
        if (scope == null) {
            return Collections.emptyList();
        }
        switch (scope) {
            case ALL:
                return usrUserMapper.selectList(new LambdaQueryWrapper<UsrUser>()
                                .eq(UsrUser::getStatus, CommonConstants.STATUS_ENABLED))
                        .stream()
                        .map(UsrUser::getId)
                        .collect(Collectors.toList());
            case SPECIFIC_USERS:
                return parsePushTargetIds(notification.getPushTarget());
            case SPECIFIC_ROLES:
                List<Long> roleIds = parsePushTargetIds(notification.getPushTarget());
                if (CollectionUtils.isEmpty(roleIds)) {
                    return Collections.emptyList();
                }
                return sysUserRoleMapper.selectList(
                                new LambdaQueryWrapper<SysUserRole>()
                                        .in(SysUserRole::getRoleId, roleIds))
                        .stream()
                        .map(SysUserRole::getUserId)
                        .distinct()
                        .collect(Collectors.toList());
            default:
                return Collections.emptyList();
        }
    }

    /**
     * 解析 pushTarget JSON 字符串中的 ID 列表。
     *
     * @param pushTarget JSON 数组字符串
     * @return ID 列表
     */
    private List<Long> parsePushTargetIds(String pushTarget) {
        if (!StringUtils.hasText(pushTarget)) {
            return Collections.emptyList();
        }
        try {
            PushTargetDTO dto = objectMapper.readValue(pushTarget, PushTargetDTO.class);
            return dto.getIds() != null ? dto.getIds() : Collections.emptyList();
        } catch (Exception e) {
            log.warn("解析 pushTarget 失败: {}", pushTarget, e);
            return Collections.emptyList();
        }
    }

    /**
     * 转换为列表 VO。
     *
     * @param notification 实体
     * @return VO
     */
    private MsgNotificationVO convertToVO(MsgNotification notification) {
        MsgNotificationVO vo = new MsgNotificationVO();
        vo.setId(notification.getId());
        vo.setTitle(notification.getTitle());
        vo.setMsgType(notification.getMsgType());
        vo.setMsgTypeLabel(getMsgTypeLabel(notification.getMsgType()));
        vo.setPushScope(notification.getPushScope());
        vo.setPushScopeLabel(getPushScopeLabel(notification.getPushScope()));
        vo.setStatus(notification.getStatus());
        vo.setStatusLabel(getStatusLabel(notification.getStatus()));
        vo.setPushType(notification.getPushType());
        vo.setPushTime(notification.getPushTime());
        vo.setReadCount(notification.getReadCount());
        vo.setTotalCount(notification.getTotalCount());
        vo.setIsTop(notification.getIsTop());
        vo.setCreatedAt(notification.getCreatedAt());
        return vo;
    }

    private String getMsgTypeLabel(Integer value) {
        MsgTypeEnum e = MsgTypeEnum.fromValue(value);
        return e != null ? e.getLabel() : "";
    }

    private String getPushScopeLabel(Integer value) {
        PushScopeEnum e = PushScopeEnum.fromValue(value);
        return e != null ? e.getLabel() : "";
    }

    private String getPushTypeLabel(Integer value) {
        PushTypeEnum e = PushTypeEnum.fromValue(value);
        return e != null ? e.getLabel() : "";
    }

    private String getStatusLabel(Integer status) {
        return switch (status) {
            case STATUS_DRAFT -> "草稿";
            case STATUS_PUSHED -> "已推送";
            case STATUS_REVOKED -> "已撤回";
            case STATUS_ARCHIVED -> "已归档";
            default -> "";
        };
    }
}
