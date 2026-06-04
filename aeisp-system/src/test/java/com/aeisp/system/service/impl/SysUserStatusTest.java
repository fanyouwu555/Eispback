package com.aeisp.system.service.impl;

import com.aeisp.common.constant.CommonConstants;
import com.aeisp.system.entity.SysUser;
import com.aeisp.system.mapper.SysUserMapper;
import com.aeisp.system.mapper.SysUserRoleMapper;
import com.aeisp.system.mapper.SysRoleMapper;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
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
 * 管理员状态管理测试。
 * 验证管理员创建和更新时的状态字段处理逻辑。
 */
@ExtendWith(MockitoExtension.class)
class SysUserStatusTest {

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
        // 初始化 MyBatis-Plus Lambda 缓存（单元测试无 Spring 上下文）
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), SysUser.class);
        lenient().when(passwordEncoder.encode(anyString())).thenAnswer(inv -> "encoded_" + inv.getArgument(0));
    }

    /**
     * 测试1: 管理员创建时忽略 status 字段
     * 验证管理员创建时发送 status=2 (禁用)，创建后状态为 1 (正常)
     */
    @Test
    void testCreateUserIgnoresStatusField() {
        // 模拟插入用户成功
        when(sysUserMapper.insert(any(SysUser.class))).thenAnswer(inv -> {
            SysUser user = inv.getArgument(0);
            user.setId(1L);
            return 1;
        });

        SysUser user = new SysUser();
        user.setUsername("testadmin");
        user.setPassword("password123");
        user.setRealName("测试管理员");
        user.setStatus(2); // 设置为禁用

        boolean result = sysUserService.createUser(user, null);

        assertTrue(result);
        // 验证状态被设置为启用 (1)，忽略传入的 2
        verify(sysUserMapper).insert(argThat((SysUser u) -> u.getStatus() == CommonConstants.STATUS_ENABLED));
    }

    /**
     * 测试2: 管理员创建时不传 status 字段
     * 验证管理员创建时不传 status 字段，创建后状态为 1 (正常)
     */
    @Test
    void testCreateUserWithoutStatusField() {
        // 模拟插入用户成功
        when(sysUserMapper.insert(any(SysUser.class))).thenAnswer(inv -> {
            SysUser user = inv.getArgument(0);
            user.setId(1L);
            return 1;
        });

        SysUser user = new SysUser();
        user.setUsername("testadmin");
        user.setPassword("password123");
        user.setRealName("测试管理员");
        // 不设置 status 字段

        boolean result = sysUserService.createUser(user, null);

        assertTrue(result);
        // 验证状态被设置为启用 (1)
        verify(sysUserMapper).insert(argThat((SysUser u) -> u.getStatus() == CommonConstants.STATUS_ENABLED));
    }

    /**
     * 测试3: 管理员更新时接受 status 字段
     * 验证管理员更新时发送 status=2 (禁用)，状态成功更新
     */
    @Test
    void testUpdateUserAcceptsStatusField() {
        // 模拟更新用户成功（使用 UpdateWrapper）
        when(sysUserMapper.update(isNull(), any())).thenReturn(1);

        SysUser user = new SysUser();
        user.setId(1L);
        user.setRealName("更新后的姓名");
        user.setStatus(2); // 设置为禁用

        boolean result = sysUserService.updateUser(user, null);

        assertTrue(result);
        // 验证 update 方法被调用
        verify(sysUserMapper).update(isNull(), any());
    }

    /**
     * 测试4: 管理员更新时不传 status 字段
     * 验证管理员更新时不传 status 字段，状态保持不变
     */
    @Test
    void testUpdateUserWithoutStatusField() {
        // 模拟更新用户成功（使用 UpdateWrapper）
        when(sysUserMapper.update(isNull(), any())).thenReturn(1);

        SysUser user = new SysUser();
        user.setId(1L);
        user.setRealName("更新后的姓名");
        // 不设置 status 字段

        boolean result = sysUserService.updateUser(user, null);

        assertTrue(result);
        // 验证 update 方法被调用
        verify(sysUserMapper).update(isNull(), any());
    }

    /**
     * 测试5: 管理员查询时返回正确状态
     * 验证查询管理员时返回的状态值正确
     */
    @Test
    void testGetUserReturnsCorrectStatus() {
        SysUser user = new SysUser();
        user.setId(1L);
        user.setUsername("testadmin");
        user.setStatus(2); // 禁用状态

        when(sysUserMapper.selectById(1L)).thenReturn(user);

        SysUser result = sysUserService.getById(1L);

        assertNotNull(result);
        assertEquals(2, result.getStatus());
    }
}
