package com.aeisp.user.service.impl;

import com.aeisp.common.exception.BizException;
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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.context.ApplicationEventPublisher;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 前端用户密码验证测试。
 * 验证默认密码 123456 跳过密码验证的逻辑。
 */
@ExtendWith(MockitoExtension.class)
class UsrUserPasswordValidationTest {

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
     * 测试: 输入默认密码 123456 应该跳过验证
     * 验证输入 "123456" 时不会提示密码长度错误
     */
    @Test
    void testPassword123456SkipsValidation() {
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
        request.setPassword("123456"); // 输入默认密码
        request.setNickname("测试用户");

        // 不应该抛出异常
        String password = usrUserService.createByAdmin(request);

        assertNotNull(password);
        assertEquals("123456", password);
    }

    /**
     * 测试: 输入短密码应该提示错误
     * 验证输入 "123" 时会提示密码长度错误
     */
    @Test
    void testShortPasswordThrowsError() {
        // 模拟插入用户成功
        when(usrUserMapper.selectByUsername("testuser")).thenReturn(null);

        UserCreateRequest request = new UserCreateRequest();
        request.setUsername("testuser");
        request.setPassword("123"); // 输入短密码
        request.setNickname("测试用户");

        // 应该抛出异常
        BizException exception = assertThrows(BizException.class, () -> usrUserService.createByAdmin(request));
        assertTrue(exception.getMessage().contains("密码长度"));
    }

    /**
     * 测试: 空密码使用默认密码 123456
     * 验证空密码时使用默认密码并跳过验证
     */
    @Test
    void testEmptyPasswordUsesDefault() {
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
        request.setPassword(null); // 空密码
        request.setNickname("测试用户");

        // 不应该抛出异常
        String password = usrUserService.createByAdmin(request);

        assertNotNull(password);
        assertEquals("123456", password);
    }
}
