package com.aeisp.system.service.impl;

import com.aeisp.system.entity.SysUser;
import com.aeisp.system.mapper.SysRoleMapper;
import com.aeisp.system.mapper.SysUserMapper;
import com.aeisp.system.mapper.SysUserRoleMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 密码策略测试。
 * 验证管理员和前端用户的密码处理逻辑一致性。
 */
@ExtendWith(MockitoExtension.class)
class PasswordStrategyTest {

    @Mock
    private SysUserMapper sysUserMapper;
    @Mock
    private SysUserRoleMapper sysUserRoleMapper;
    @Mock
    private SysRoleMapper sysRoleMapper;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private SysUserServiceImpl sysUserService;

    @BeforeEach
    void setUp() {
        lenient().when(passwordEncoder.encode(anyString())).thenAnswer(inv -> "encoded_" + inv.getArgument(0));
    }

    /**
     * 测试1: 管理员创建时密码为空使用默认密码 "123456"
     * 验证管理员创建时密码为空，使用默认密码 "123456"
     */
    @Test
    void testAdminCreateUserDefaultPassword() {
        // 模拟插入用户成功
        when(sysUserMapper.insert(any(SysUser.class))).thenAnswer(inv -> {
            SysUser user = inv.getArgument(0);
            user.setId(1L);
            return 1;
        });

        SysUser user = new SysUser();
        user.setUsername("testadmin");
        user.setPassword(null); // 密码为空
        user.setRealName("测试管理员");

        boolean result = sysUserService.createUser(user, null);

        assertTrue(result);
        // 验证使用默认密码 "123456" 进行加密
        verify(passwordEncoder).encode("123456");
    }

    /**
     * 测试2: 管理员创建时密码为空字符串使用默认密码 "123456"
     * 验证管理员创建时密码为空字符串，使用默认密码 "123456"
     */
    @Test
    void testAdminCreateUserEmptyPassword() {
        // 模拟插入用户成功
        when(sysUserMapper.insert(any(SysUser.class))).thenAnswer(inv -> {
            SysUser user = inv.getArgument(0);
            user.setId(1L);
            return 1;
        });

        SysUser user = new SysUser();
        user.setUsername("testadmin");
        user.setPassword(""); // 密码为空字符串
        user.setRealName("测试管理员");

        boolean result = sysUserService.createUser(user, null);

        assertTrue(result);
        // 验证使用默认密码 "123456" 进行加密
        verify(passwordEncoder).encode("123456");
    }

    /**
     * 测试3: 管理员创建时密码为 null 使用默认密码 "123456"
     * 验证管理员创建时密码为 null，使用默认密码 "123456"
     */
    @Test
    void testAdminCreateUserNullPassword() {
        // 模拟插入用户成功
        when(sysUserMapper.insert(any(SysUser.class))).thenAnswer(inv -> {
            SysUser user = inv.getArgument(0);
            user.setId(1L);
            return 1;
        });

        SysUser user = new SysUser();
        user.setUsername("testadmin");
        user.setPassword(null); // 密码为 null
        user.setRealName("测试管理员");

        boolean result = sysUserService.createUser(user, null);

        assertTrue(result);
        // 验证使用默认密码 "123456" 进行加密
        verify(passwordEncoder).encode("123456");
    }

    /**
     * 测试4: 管理员创建时提供自定义密码
     * 验证管理员创建时提供自定义密码，使用自定义密码
     */
    @Test
    void testAdminCreateUserCustomPassword() {
        // 模拟插入用户成功
        when(sysUserMapper.insert(any(SysUser.class))).thenAnswer(inv -> {
            SysUser user = inv.getArgument(0);
            user.setId(1L);
            return 1;
        });

        SysUser user = new SysUser();
        user.setUsername("testadmin");
        user.setPassword("mypassword123"); // 自定义密码
        user.setRealName("测试管理员");

        boolean result = sysUserService.createUser(user, null);

        assertTrue(result);
        // 验证使用自定义密码进行加密
        verify(passwordEncoder).encode("mypassword123");
    }

    /**
     * 测试5: 管理员更新时密码为空不修改密码
     * 验证管理员更新时密码为空，不修改密码
     */
    @Test
    void testAdminUpdateUserEmptyPassword() {
        // 模拟更新用户成功
        when(sysUserMapper.updateById(any(SysUser.class))).thenReturn(1);

        SysUser user = new SysUser();
        user.setId(1L);
        user.setPassword(""); // 密码为空
        user.setRealName("更新后的姓名");

        boolean result = sysUserService.updateUser(user, null);

        assertTrue(result);
        // 验证不调用密码加密方法
        verify(passwordEncoder, never()).encode(anyString());
    }

    /**
     * 测试6: 管理员更新时密码为 null 不修改密码
     * 验证管理员更新时密码为 null，不修改密码
     */
    @Test
    void testAdminUpdateUserNullPassword() {
        // 模拟更新用户成功
        when(sysUserMapper.updateById(any(SysUser.class))).thenReturn(1);

        SysUser user = new SysUser();
        user.setId(1L);
        user.setPassword(null); // 密码为 null
        user.setRealName("更新后的姓名");

        boolean result = sysUserService.updateUser(user, null);

        assertTrue(result);
        // 验证不调用密码加密方法
        verify(passwordEncoder, never()).encode(anyString());
    }

    /**
     * 测试7: 管理员更新时提供新密码
     * 验证管理员更新时提供新密码，使用新密码
     */
    @Test
    void testAdminUpdateUserNewPassword() {
        // 模拟更新用户成功
        when(sysUserMapper.updateById(any(SysUser.class))).thenReturn(1);

        SysUser user = new SysUser();
        user.setId(1L);
        user.setPassword("newpassword123"); // 新密码
        user.setRealName("更新后的姓名");

        boolean result = sysUserService.updateUser(user, null);

        assertTrue(result);
        // 验证使用新密码进行加密
        verify(passwordEncoder).encode("newpassword123");
    }
}
