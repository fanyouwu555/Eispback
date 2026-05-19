package com.aeisp.message.service.impl;

import com.aeisp.common.PageResult;
import com.aeisp.common.exception.BizException;
import com.aeisp.message.entity.MsgNotification;
import com.aeisp.message.entity.MsgUserNotification;
import com.aeisp.message.enums.PushScopeEnum;
import com.aeisp.message.enums.PushTypeEnum;
import com.aeisp.message.mapper.MsgNotificationMapper;
import com.aeisp.message.mapper.MsgUserNotificationMapper;
import com.aeisp.message.request.CreateNotificationRequest;
import com.aeisp.message.request.NotificationQueryRequest;
import com.aeisp.message.vo.MsgNotificationDetailVO;
import com.aeisp.message.vo.MsgNotificationVO;
import com.aeisp.system.mapper.SysUserRoleMapper;
import com.aeisp.user.entity.UsrUser;
import com.aeisp.user.mapper.UsrUserMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * MsgNotificationServiceImpl 单元测试。
 */
@ExtendWith(MockitoExtension.class)
class MsgNotificationServiceImplTest {

    @Mock
    private MsgNotificationMapper msgNotificationMapper;
    @Mock
    private MsgUserNotificationMapper msgUserNotificationMapper;
    @Mock
    private UsrUserMapper usrUserMapper;
    @Mock
    private SysUserRoleMapper sysUserRoleMapper;
    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @Spy
    @InjectMocks
    private MsgNotificationServiceImpl msgNotificationService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(msgNotificationService, "baseMapper", msgNotificationMapper);
    }

    @Test
    void testCreateNotification() {
        doReturn(true).when(msgNotificationService).save(any(MsgNotification.class));

        CreateNotificationRequest request = new CreateNotificationRequest();
        request.setTitle("测试标题");
        request.setContent("测试内容");
        request.setMsgType(1);
        request.setPushScope(PushScopeEnum.ALL.getValue());
        request.setPushType(PushTypeEnum.IMMEDIATE.getValue());

        boolean result = msgNotificationService.createNotification(request);
        assertTrue(result);
        verify(msgNotificationService).save(argThat(n ->
                "测试标题".equals(n.getTitle()) && n.getStatus() == 1));
    }

    @Test
    void testPushNotificationNotFound() {
        doReturn(null).when(msgNotificationService).getById(1L);
        assertThrows(BizException.class, () -> msgNotificationService.pushNotification(1L));
    }

    @Test
    void testPushNotificationNotDraft() {
        MsgNotification notification = new MsgNotification();
        notification.setId(1L);
        notification.setStatus(2); // 已发送
        doReturn(notification).when(msgNotificationService).getById(1L);

        assertThrows(BizException.class, () -> msgNotificationService.pushNotification(1L));
    }

    @Test
    void testPushNotificationToSpecificUsers() {
        MsgNotification notification = new MsgNotification();
        notification.setId(1L);
        notification.setStatus(1); // 草稿
        notification.setPushScope(PushScopeEnum.SPECIFIC_USERS.getValue());
        notification.setPushType(PushTypeEnum.IMMEDIATE.getValue());
        notification.setPushTarget("{\"ids\":[1,2,3]}");
        doReturn(notification).when(msgNotificationService).getById(1L);
        when(msgUserNotificationMapper.batchInsert(anyList())).thenReturn(3);
        doReturn(true).when(msgNotificationService).updateById(any(MsgNotification.class));

        boolean result = msgNotificationService.pushNotification(1L);
        assertTrue(result);
        verify(msgUserNotificationMapper).batchInsert(argThat(list -> list.size() == 3));
    }

    @Test
    void testRevokeNotification() {
        MsgNotification notification = new MsgNotification();
        notification.setId(1L);
        notification.setStatus(2); // 已发送
        doReturn(notification).when(msgNotificationService).getById(1L);
        doReturn(true).when(msgNotificationService).updateById(any(MsgNotification.class));
        when(msgUserNotificationMapper.update(any(), any())).thenReturn(1);

        boolean result = msgNotificationService.revokeNotification(1L);
        assertTrue(result);
        verify(msgNotificationService).updateById(argThat(n -> n.getStatus() == 3));
    }

    @Test
    void testToggleTop() {
        MsgNotification notification = new MsgNotification();
        notification.setId(1L);
        doReturn(notification).when(msgNotificationService).getById(1L);
        doReturn(true).when(msgNotificationService).updateById(any(MsgNotification.class));

        assertTrue(msgNotificationService.toggleTop(1L, 1));
        assertEquals(1, notification.getIsTop());
    }

    @Test
    void testListNotifications() {
        MsgNotification notification = new MsgNotification();
        notification.setId(1L);
        notification.setTitle("标题");
        notification.setMsgType(1);
        notification.setPushScope(1);
        notification.setStatus(2);
        notification.setPushType(1);
        notification.setReadCount(5L);
        notification.setTotalCount(10L);
        notification.setIsTop(0);
        notification.setCreatedAt(LocalDateTime.now());

        Page<MsgNotification> page = new Page<>(1, 10);
        page.setRecords(List.of(notification));
        page.setTotal(1);
        when(msgNotificationMapper.selectPage(any(Page.class), any())).thenReturn(page);

        NotificationQueryRequest request = new NotificationQueryRequest();
        request.setPageNum(1L);
        request.setPageSize(10L);
        request.setStatus(2);

        PageResult<MsgNotificationVO> result = msgNotificationService.listNotifications(request);
        assertEquals(1, result.getTotal());
        assertEquals("标题", result.getList().get(0).getTitle());
    }

    @Test
    void testGetDetail() {
        MsgNotification notification = new MsgNotification();
        notification.setId(1L);
        notification.setTitle("标题");
        notification.setContent("内容");
        notification.setMsgType(1);
        notification.setPushScope(1);
        notification.setPushType(1);
        notification.setStatus(2);
        notification.setReadCount(1L);
        notification.setTotalCount(2L);
        notification.setCreatedAt(LocalDateTime.now());

        doReturn(notification).when(msgNotificationService).getById(1L);
        when(msgUserNotificationMapper.selectList(any())).thenReturn(List.of());

        MsgNotificationDetailVO vo = msgNotificationService.getDetail(1L);
        assertNotNull(vo);
        assertEquals("标题", vo.getTitle());
        assertEquals("已发送", vo.getStatusLabel());
    }

    @Test
    void testGetDetailWithTargets() {
        MsgNotification notification = new MsgNotification();
        notification.setId(1L);
        notification.setStatus(2);
        doReturn(notification).when(msgNotificationService).getById(1L);

        MsgUserNotification un = new MsgUserNotification();
        un.setUserId(1L);
        un.setReadStatus(1);
        when(msgUserNotificationMapper.selectList(any())).thenReturn(List.of(un));

        UsrUser user = new UsrUser();
        user.setId(1L);
        user.setUsername("test");
        when(usrUserMapper.selectById(1L)).thenReturn(user);

        MsgNotificationDetailVO vo = msgNotificationService.getDetail(1L);
        assertEquals(1, vo.getTargetUsers().size());
        assertEquals("test", vo.getTargetUsers().get(0).getUsername());
    }
}
