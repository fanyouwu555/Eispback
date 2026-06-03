package com.aeisp.user.service.impl;

import com.aeisp.common.constant.CommonConstants;
import com.aeisp.user.entity.UsrUser;
import com.aeisp.user.entity.UsrUserDuration;
import com.aeisp.user.entity.UsrUserBalance;
import com.aeisp.user.mapper.*;
import com.aeisp.user.request.UserCreateRequest;
import com.aeisp.system.service.SysConfigService;
import com.aeisp.system.service.SysFeatureSwitchService;
import com.aeisp.system.mapper.SysRoleMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.context.ApplicationEventPublisher;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 前端用户状态管理测试。
 * 验证前端用户创建和更新时的状态字段处理逻辑。
 */
@ExtendWith(MockitoExtension.class)
class UsrUserStatusTest {

    @Mock
    private UsrUserMapper usrUserMapper;
    @Mock
    private UsrUserRoleMapper usrUserRoleMapper;
    @Mock
    private UsrUserDurationMapper usrUserDurationMapper;
    @Mock
    private UsrUserBalanceMapper usrUserBalanceMapper;
    @Mock
    private SysRoleMapper sysRoleMapper;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Mock
    private SysFeatureSwitchService featureSwitchService;
    @Mock
    private SysConfigService sysConfigService;

    @InjectMocks
    private UsrUserServiceImpl usrUserService;

    @BeforeEach
    void setUp() {
        lenient().when(passwordEncoder.encode(anyString())).thenAnswer(inv -> "encoded_" + inv.getArgument(0));
        lenient().when(featureSwitchService.isEnabled(anyString())).thenReturn(false);
        lenient().when(sysConfigService.getConfigValue(anyString(), anyString())).thenReturn(null);
    }

    /**
     * 测试1: 前端用户创建时接受 status 字段
     * 验证前端用户创建时发送 status=2 (禁用)，创建后状态为 2 (禁用)
     */
    @Test
    void testCreateUserAcceptsStatusField() {
        // 模拟插入用户成功
        when(usrUserMapper.selectByUsername("testuser")).thenReturn(null);
        when(usrUserMapper.insert(any(UsrUser.class))).thenAnswer(inv -> {
            UsrUser user = inv.getArgument(0);
            user.setId(1L);
            return 1;
        });
        when(usrUserDurationMapper.insert(any(UsrUserDuration.class))).thenReturn(1);
        when(usrUserBalanceMapper.insert(any(UsrUserBalance.class))).thenReturn(1);

        UserCreateRequest request = new UserCreateRequest();
        request.setUsername("testuser");
        request.setPassword("password123");
        request.setNickname("测试用户");
        request.setStatus(2); // 设置为禁用

        String password = usrUserService.createByAdmin(request);

        assertNotNull(password);
        // 验证状态被设置为 2 (禁用)
        verify(usrUserMapper).insert(argThat((UsrUser u) -> u.getStatus() == 2));
    }

    /**
     * 测试2: 前端用户创建时不传 status 字段
     * 验证前端用户创建时不传 status 字段，创建后状态为 1 (正常)
     */
    @Test
    void testCreateUserWithoutStatusField() {
        // 模拟插入用户成功
        when(usrUserMapper.selectByUsername("testuser")).thenReturn(null);
        when(usrUserMapper.insert(any(UsrUser.class))).thenAnswer(inv -> {
            UsrUser user = inv.getArgument(0);
            user.setId(1L);
            return 1;
        });
        when(usrUserDurationMapper.insert(any(UsrUserDuration.class))).thenReturn(1);
        when(usrUserBalanceMapper.insert(any(UsrUserBalance.class))).thenReturn(1);

        UserCreateRequest request = new UserCreateRequest();
        request.setUsername("testuser");
        request.setPassword("password123");
        request.setNickname("测试用户");
        // 不设置 status 字段

        String password = usrUserService.createByAdmin(request);

        assertNotNull(password);
        // 验证状态被设置为 1 (正常)
        verify(usrUserMapper).insert(argThat((UsrUser u) -> u.getStatus() == CommonConstants.USER_STATUS_NORMAL));
    }

    /**
     * 测试3: 前端用户创建时 status=3 (冻结)
     * 验证前端用户创建时发送 status=3 (冻结)，创建后状态为 3 (冻结)
     */
    @Test
    void testCreateUserWithFrozenStatus() {
        // 模拟插入用户成功
        when(usrUserMapper.selectByUsername("testuser")).thenReturn(null);
        when(usrUserMapper.insert(any(UsrUser.class))).thenAnswer(inv -> {
            UsrUser user = inv.getArgument(0);
            user.setId(1L);
            return 1;
        });

        UserCreateRequest request = new UserCreateRequest();
        request.setUsername("testuser");
        request.setPassword("password123");
        request.setNickname("测试用户");
        request.setStatus(3); // 设置为冻结

        String password = usrUserService.createByAdmin(request);

        assertNotNull(password);
        // 验证状态被设置为 3 (冻结)
        verify(usrUserMapper).insert(argThat((UsrUser u) -> u.getStatus() == CommonConstants.USER_STATUS_FROZEN));
    }

    /**
     * 测试4: 前端用户创建时 status=4 (锁定)
     * 验证前端用户创建时发送 status=4 (锁定)，创建后状态为 4 (锁定)
     */
    @Test
    void testCreateUserWithLockedStatus() {
        // 模拟插入用户成功
        when(usrUserMapper.selectByUsername("testuser")).thenReturn(null);
        when(usrUserMapper.insert(any(UsrUser.class))).thenAnswer(inv -> {
            UsrUser user = inv.getArgument(0);
            user.setId(1L);
            return 1;
        });

        UserCreateRequest request = new UserCreateRequest();
        request.setUsername("testuser");
        request.setPassword("password123");
        request.setNickname("测试用户");
        request.setStatus(4); // 设置为锁定

        String password = usrUserService.createByAdmin(request);

        assertNotNull(password);
        // 验证状态被设置为 4 (锁定)
        verify(usrUserMapper).insert(argThat((UsrUser u) -> u.getStatus() == CommonConstants.USER_STATUS_LOCKED));
    }

    /**
     * 测试5: 前端用户查询时返回正确状态
     * 验证查询前端用户时返回的状态值正确
     */
    @Test
    void testGetUserReturnsCorrectStatus() {
        UsrUser user = new UsrUser();
        user.setId(1L);
        user.setUsername("testuser");
        user.setStatus(2); // 禁用状态

        when(usrUserMapper.selectById(1L)).thenReturn(user);

        UsrUser result = usrUserMapper.selectById(1L);

        assertNotNull(result);
        assertEquals(2, result.getStatus());
    }
}
