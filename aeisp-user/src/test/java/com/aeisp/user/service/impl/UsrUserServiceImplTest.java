package com.aeisp.user.service.impl;

import com.aeisp.common.PageResult;
import com.aeisp.common.constant.CommonConstants;
import com.aeisp.common.exception.BizException;
import com.aeisp.system.entity.SysRole;
import com.aeisp.system.mapper.SysRoleMapper;
import com.aeisp.user.entity.*;
import com.aeisp.user.mapper.*;
import com.aeisp.user.request.*;
import com.aeisp.user.vo.UsrUserVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * UsrUserServiceImpl 单元测试。
 */
@ExtendWith(MockitoExtension.class)
class UsrUserServiceImplTest {

    @Mock
    private UsrUserMapper usrUserMapper;
    @Mock
    private UsrUserRoleMapper usrUserRoleMapper;
    @Mock
    private UsrUserDurationMapper usrUserDurationMapper;
    @Mock
    private UsrUserBalanceMapper usrUserBalanceMapper;
    @Mock
    private UsrDurationChangeLogMapper usrDurationChangeLogMapper;
    @Mock
    private SysRoleMapper sysRoleMapper;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsrUserServiceImpl usrUserService;

    @Test
    void testRegisterSuccess() {
        when(usrUserMapper.selectByUsername("testuser")).thenReturn(null);
        when(usrUserMapper.selectByPhone("13800138000")).thenReturn(null);
        when(usrUserMapper.selectByEmail("test@example.com")).thenReturn(null);
        when(usrUserMapper.insert(any(UsrUser.class))).thenAnswer(inv -> {
            UsrUser u = inv.getArgument(0);
            u.setId(1L);
            return 1;
        });
        when(passwordEncoder.encode("password123")).thenReturn("encoded_password");

        UsrUser user = new UsrUser();
        user.setUsername("testuser");
        user.setPhone("13800138000");
        user.setEmail("test@example.com");
        user.setPassword("password123");

        assertTrue(usrUserService.register(user));
        verify(passwordEncoder).encode("password123");
        verify(usrUserDurationMapper).insert(isA(UsrUserDuration.class));
        verify(usrUserBalanceMapper).insert(isA(UsrUserBalance.class));
    }

    @Test
    void testRegisterDuplicateUsername() {
        when(usrUserMapper.selectByUsername("testuser")).thenReturn(new UsrUser());

        UsrUser user = new UsrUser();
        user.setUsername("testuser");

        BizException ex = assertThrows(BizException.class, () -> usrUserService.register(user));
        assertEquals("用户名已存在", ex.getMessage());
    }

    @Test
    void testCreateByAdminSuccess() {
        when(usrUserMapper.selectByUsername("adminuser")).thenReturn(null);
        when(usrUserMapper.selectByPhone("13900139000")).thenReturn(null);
        when(usrUserMapper.selectByEmail("admin@example.com")).thenReturn(null);
        when(usrUserMapper.insert(any(UsrUser.class))).thenAnswer(inv -> {
            UsrUser u = inv.getArgument(0);
            u.setId(1L);
            return 1;
        });
        when(passwordEncoder.encode("password123")).thenReturn("encoded_password");

        UserCreateRequest request = new UserCreateRequest();
        request.setUsername("adminuser");
        request.setPhone("13900139000");
        request.setEmail("admin@example.com");
        request.setPassword("password123");
        request.setRemainingMinutes(100);

        assertTrue(usrUserService.createByAdmin(request));
        verify(usrUserDurationMapper).insert(argThat((UsrUserDuration d) -> d.getRemainingMinutes() == 100));
    }

    @Test
    void testCreateByAdminWithRoles() {
        when(usrUserMapper.selectByUsername("adminuser")).thenReturn(null);
        when(usrUserMapper.selectByPhone("13900139000")).thenReturn(null);
        when(usrUserMapper.selectByEmail("admin@example.com")).thenReturn(null);
        when(usrUserMapper.insert(any(UsrUser.class))).thenAnswer(inv -> {
            UsrUser u = inv.getArgument(0);
            u.setId(1L);
            return 1;
        });
        when(usrUserRoleMapper.batchInsert(anyList())).thenReturn(2);
        when(passwordEncoder.encode("password123")).thenReturn("encoded_password");

        UserCreateRequest request = new UserCreateRequest();
        request.setUsername("adminuser");
        request.setPhone("13900139000");
        request.setEmail("admin@example.com");
        request.setPassword("password123");
        request.setRoleIds(List.of(1L, 2L));

        assertTrue(usrUserService.createByAdmin(request));
        verify(usrUserRoleMapper).batchInsert(anyList());
    }

    @Test
    void testBatchCreateWithEmptyList() {
        UserBatchCreateRequest request = new UserBatchCreateRequest();
        assertFalse(usrUserService.batchCreate(request));
    }

    @Test
    void testBatchCreateSuccess() {
        when(usrUserMapper.batchInsert(anyList())).thenAnswer(inv -> {
            List<UsrUser> list = inv.getArgument(0);
            list.get(0).setId(1L);
            list.get(1).setId(2L);
            return 2;
        });
        when(passwordEncoder.encode(any())).thenReturn("encoded_password");

        UserCreateRequest item1 = new UserCreateRequest();
        item1.setUsername("user1");
        item1.setPhone("13800138001");
        item1.setPassword("pass123");

        UserCreateRequest item2 = new UserCreateRequest();
        item2.setUsername("user2");
        item2.setPhone("13800138002");
        item2.setPassword("pass456");

        UserBatchCreateRequest request = new UserBatchCreateRequest();
        request.setUsers(List.of(item1, item2));

        assertTrue(usrUserService.batchCreate(request));
        verify(usrUserMapper).batchInsert(anyList());
        verify(passwordEncoder, times(2)).encode(any());
        verify(usrUserDurationMapper, times(2)).insert(isA(UsrUserDuration.class));
    }

    @Test
    void testUpdateUserNotFound() {
        when(usrUserMapper.selectById(1L)).thenReturn(null);

        UserUpdateRequest request = new UserUpdateRequest();
        request.setId(1L);

        assertThrows(BizException.class, () -> usrUserService.updateUser(request));
    }

    @Test
    void testUpdateUserSuccess() {
        UsrUser existing = new UsrUser();
        existing.setId(1L);
        existing.setPhone("13800138000");
        existing.setEmail("old@example.com");
        when(usrUserMapper.selectById(1L)).thenReturn(existing);
        when(usrUserMapper.updateById(any(UsrUser.class))).thenReturn(1);

        UserUpdateRequest request = new UserUpdateRequest();
        request.setId(1L);
        request.setPhone("13800138000");
        request.setEmail("new@example.com");
        request.setNickname("NewName");

        assertTrue(usrUserService.updateUser(request));
        verify(usrUserMapper).updateById(any(UsrUser.class));
    }

    @Test
    void testUpdateUserWithRoles() {
        UsrUser existing = new UsrUser();
        existing.setId(1L);
        existing.setPhone("13800138000");
        when(usrUserMapper.selectById(1L)).thenReturn(existing);
        when(usrUserMapper.updateById(any(UsrUser.class))).thenReturn(1);
        when(usrUserRoleMapper.delete(any())).thenReturn(1);
        when(usrUserRoleMapper.batchInsert(anyList())).thenReturn(2);

        UserUpdateRequest request = new UserUpdateRequest();
        request.setId(1L);
        request.setPhone("13800138000");
        request.setRoleIds(List.of(1L, 2L));

        assertTrue(usrUserService.updateUser(request));
        verify(usrUserRoleMapper).delete(any());
        verify(usrUserRoleMapper).batchInsert(anyList());
    }

    @Test
    void testUpdateStatus() {
        when(usrUserMapper.updateById(any(UsrUser.class))).thenReturn(1);
        assertTrue(usrUserService.updateStatus(1L, CommonConstants.USER_STATUS_DISABLED));
    }

    @Test
    void testResetPassword() {
        when(usrUserMapper.updateById(any(UsrUser.class))).thenReturn(1);
        when(passwordEncoder.encode("newpassword")).thenReturn("encoded_newpassword");

        assertTrue(usrUserService.resetPassword(1L, "newpassword"));
        verify(passwordEncoder).encode("newpassword");
    }

    @Test
    void testAdjustDurationSuccess() {
        UsrUserDuration duration = new UsrUserDuration();
        duration.setId(1L);
        duration.setUserId(1L);
        duration.setRemainingMinutes(100);
        duration.setTotalGrantedMinutes(200);
        duration.setTotalConsumedMinutes(100);
        when(usrUserDurationMapper.selectByUserId(1L)).thenReturn(duration);
        when(usrDurationChangeLogMapper.insert(any(UsrDurationChangeLog.class))).thenReturn(1);

        AdjustDurationRequest request = new AdjustDurationRequest();
        request.setUserId(1L);
        request.setDeltaMinutes(50);
        request.setReason("测试增加");

        assertTrue(usrUserService.adjustDuration(request));
        verify(usrUserDurationMapper).updateById(isA(UsrUserDuration.class));
        verify(usrDurationChangeLogMapper).insert(argThat((UsrDurationChangeLog l) ->
                l.getChangeMinutes() == 50 && "ADMIN_ADD".equals(l.getOperationType())));
    }

    @Test
    void testAdjustDurationDeduct() {
        UsrUserDuration duration = new UsrUserDuration();
        duration.setId(1L);
        duration.setUserId(1L);
        duration.setRemainingMinutes(100);
        duration.setTotalGrantedMinutes(200);
        duration.setTotalConsumedMinutes(100);
        when(usrUserDurationMapper.selectByUserId(1L)).thenReturn(duration);
        when(usrDurationChangeLogMapper.insert(any(UsrDurationChangeLog.class))).thenReturn(1);

        AdjustDurationRequest request = new AdjustDurationRequest();
        request.setUserId(1L);
        request.setDeltaMinutes(-30);
        request.setReason("测试扣减");

        assertTrue(usrUserService.adjustDuration(request));
        verify(usrUserDurationMapper).updateById(isA(UsrUserDuration.class));
        verify(usrDurationChangeLogMapper).insert(argThat((UsrDurationChangeLog l) ->
                l.getChangeMinutes() == -30 && "ADMIN_SUBTRACT".equals(l.getOperationType())));
    }

    @Test
    void testAdjustDurationInsufficient() {
        UsrUserDuration duration = new UsrUserDuration();
        duration.setId(1L);
        duration.setUserId(1L);
        duration.setRemainingMinutes(10);
        when(usrUserDurationMapper.selectByUserId(1L)).thenReturn(duration);

        AdjustDurationRequest request = new AdjustDurationRequest();
        request.setUserId(1L);
        request.setDeltaMinutes(-20);

        BizException ex = assertThrows(BizException.class, () -> usrUserService.adjustDuration(request));
        assertTrue(ex.getMessage().contains("剩余时长不足"));
    }

    @Test
    void testAdjustDurationNotFound() {
        when(usrUserDurationMapper.selectByUserId(1L)).thenReturn(null);

        AdjustDurationRequest request = new AdjustDurationRequest();
        request.setUserId(1L);
        request.setDeltaMinutes(10);

        BizException ex = assertThrows(BizException.class, () -> usrUserService.adjustDuration(request));
        assertEquals("用户时长记录不存在", ex.getMessage());
    }

    @Test
    void testListUsers() {
        UsrUser user = new UsrUser();
        user.setId(1L);
        user.setUsername("test");
        user.setDeleted(CommonConstants.DELETED_NO);
        Page<UsrUser> page = new Page<>(1, 10);
        page.setRecords(List.of(user));
        page.setTotal(1);
        when(usrUserMapper.selectPage(any(Page.class), any())).thenReturn(page);

        UsrUserDuration duration = new UsrUserDuration();
        duration.setUserId(1L);
        duration.setRemainingMinutes(60);
        when(usrUserDurationMapper.selectList(any())).thenReturn(List.of(duration));

        UsrUserBalance balance = new UsrUserBalance();
        balance.setUserId(1L);
        balance.setBalanceCents(1000);
        when(usrUserBalanceMapper.selectList(any())).thenReturn(List.of(balance));

        when(usrUserRoleMapper.selectList(any())).thenReturn(List.of());

        UserQueryRequest request = new UserQueryRequest();
        request.setPageNum(1L);
        request.setPageSize(10L);
        PageResult<UsrUserVO> result = usrUserService.listUsers(request);

        assertEquals(1, result.getTotal());
        assertEquals(1, result.getList().size());
        assertEquals("test", result.getList().get(0).getUsername());
        assertEquals(60, result.getList().get(0).getRemainingMinutes());
        assertEquals(1000, result.getList().get(0).getBalanceCents());
    }

    @Test
    void testGetUserDetail() {
        UsrUser user = new UsrUser();
        user.setId(1L);
        user.setUsername("test");
        user.setDeleted(CommonConstants.DELETED_NO);
        when(usrUserMapper.selectById(1L)).thenReturn(user);

        UsrUserDuration duration = new UsrUserDuration();
        duration.setUserId(1L);
        duration.setRemainingMinutes(60);
        when(usrUserDurationMapper.selectByUserId(1L)).thenReturn(duration);

        UsrUserBalance balance = new UsrUserBalance();
        balance.setUserId(1L);
        balance.setBalanceCents(1000);
        when(usrUserBalanceMapper.selectByUserId(1L)).thenReturn(balance);

        when(usrUserRoleMapper.selectRoleIdsByUserId(1L)).thenReturn(List.of(1L));
        SysRole role = new SysRole();
        role.setId(1L);
        role.setRoleCode("USER");
        when(sysRoleMapper.selectBatchIds(anyList())).thenReturn(List.of(role));

        UsrUserVO vo = usrUserService.getUserDetail(1L);
        assertNotNull(vo);
        assertEquals("test", vo.getUsername());
        assertEquals(60, vo.getRemainingMinutes());
        assertEquals(List.of("USER"), vo.getRoleCodes());
    }

    @Test
    void testGetUserDetailNotFound() {
        when(usrUserMapper.selectById(1L)).thenReturn(null);
        assertNull(usrUserService.getUserDetail(1L));
    }
}
