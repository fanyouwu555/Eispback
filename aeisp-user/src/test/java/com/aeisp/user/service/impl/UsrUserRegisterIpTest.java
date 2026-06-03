package com.aeisp.user.service.impl;

import com.aeisp.system.service.SysConfigService;
import com.aeisp.system.service.SysFeatureSwitchService;
import com.aeisp.system.mapper.SysRoleMapper;
import com.aeisp.user.entity.UsrUser;
import com.aeisp.user.entity.UsrUserBalance;
import com.aeisp.user.entity.UsrUserDuration;
import com.aeisp.user.mapper.*;
import com.aeisp.user.request.UserCreateRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.context.ApplicationEventPublisher;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 前端用户注册IP测试。
 * 验证管理员创建用户时 register_ip 字段正确设置。
 */
@ExtendWith(MockitoExtension.class)
class UsrUserRegisterIpTest {

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
     * 测试: 管理员创建用户时 register_ip 字段设置为 "system"
     * 验证创建用户时 register_ip 字段正确设置
     */
    @Test
    void testCreateUserSetsRegisterIpToSystem() {
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

        String password = usrUserService.createByAdmin(request);

        assertNotNull(password);

        // 验证 register_ip 字段被设置为 "system"
        ArgumentCaptor<UsrUser> userCaptor = ArgumentCaptor.forClass(UsrUser.class);
        verify(usrUserMapper).insert(userCaptor.capture());

        UsrUser capturedUser = userCaptor.getValue();
        assertEquals("system", capturedUser.getRegisterIp());
    }

    /**
     * 测试: 管理员创建用户时 register_ip 字段不为 null
     * 验证创建用户时 register_ip 字段不为空
     */
    @Test
    void testCreateUserRegisterIpIsNotNull() {
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

        String password = usrUserService.createByAdmin(request);

        assertNotNull(password);

        // 验证 register_ip 字段不为 null
        ArgumentCaptor<UsrUser> userCaptor = ArgumentCaptor.forClass(UsrUser.class);
        verify(usrUserMapper).insert(userCaptor.capture());

        UsrUser capturedUser = userCaptor.getValue();
        assertNotNull(capturedUser.getRegisterIp());
    }
}
