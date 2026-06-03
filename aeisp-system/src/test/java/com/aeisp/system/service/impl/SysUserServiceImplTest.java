package com.aeisp.system.service.impl;

import com.aeisp.common.constant.CommonConstants;
import com.aeisp.system.entity.SysUser;
import com.aeisp.system.entity.SysUserRole;
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

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * SysUserServiceImpl 单元测试。
 */
@ExtendWith(MockitoExtension.class)
class SysUserServiceImplTest {

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
        // 设置默认行为
        lenient().when(passwordEncoder.encode(anyString())).thenAnswer(inv -> "encoded_" + inv.getArgument(0));
    }

    @Test
    void testCreateUserSuccess() {
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
        user.setEmail("test@example.com");
        user.setPhone("13800138000");

        boolean result = sysUserService.createUser(user, List.of(1L, 2L));

        assertTrue(result);
        verify(sysUserMapper).insert(any(SysUser.class));
        verify(sysUserRoleMapper).batchInsert(eq(1L), eq(List.of(1L, 2L)));
        assertEquals(CommonConstants.STATUS_ENABLED, user.getStatus());
        assertEquals("encoded_password123", user.getPassword());
    }

    @Test
    void testCreateUserWithoutPassword() {
        // 测试不传密码的情况，默认使用 123456
        when(sysUserMapper.insert(any(SysUser.class))).thenAnswer(inv -> {
            SysUser user = inv.getArgument(0);
            user.setId(1L);
            return 1;
        });

        SysUser user = new SysUser();
        user.setUsername("testadmin");
        user.setRealName("测试管理员");

        boolean result = sysUserService.createUser(user, null);

        assertTrue(result);
        verify(sysUserMapper).insert(any(SysUser.class));
        // 密码为空，应该使用默认密码 123456
        verify(passwordEncoder).encode("123456");
    }

    @Test
    void testCreateUserWithEmptyPassword() {
        // 测试密码为空字符串的情况，默认使用 123456
        when(sysUserMapper.insert(any(SysUser.class))).thenAnswer(inv -> {
            SysUser user = inv.getArgument(0);
            user.setId(1L);
            return 1;
        });

        SysUser user = new SysUser();
        user.setUsername("testadmin");
        user.setPassword("");
        user.setRealName("测试管理员");

        boolean result = sysUserService.createUser(user, null);

        assertTrue(result);
        verify(sysUserMapper).insert(any(SysUser.class));
        // 密码为空字符串，应该使用默认密码 123456
        verify(passwordEncoder).encode("123456");
    }

    @Test
    void testCreateUserWithoutRoles() {
        // 测试不分配角色的情况
        when(sysUserMapper.insert(any(SysUser.class))).thenAnswer(inv -> {
            SysUser user = inv.getArgument(0);
            user.setId(1L);
            return 1;
        });

        SysUser user = new SysUser();
        user.setUsername("testadmin");
        user.setPassword("password123");
        user.setRealName("测试管理员");

        boolean result = sysUserService.createUser(user, null);

        assertTrue(result);
        verify(sysUserMapper).insert(any(SysUser.class));
        verify(sysUserRoleMapper, never()).batchInsert(anyLong(), anyList());
    }

    @Test
    void testCreateUserWithEmptyRoles() {
        // 测试分配空角色列表的情况
        when(sysUserMapper.insert(any(SysUser.class))).thenAnswer(inv -> {
            SysUser user = inv.getArgument(0);
            user.setId(1L);
            return 1;
        });

        SysUser user = new SysUser();
        user.setUsername("testadmin");
        user.setPassword("password123");
        user.setRealName("测试管理员");

        boolean result = sysUserService.createUser(user, List.of());

        assertTrue(result);
        verify(sysUserMapper).insert(any(SysUser.class));
        verify(sysUserRoleMapper, never()).batchInsert(anyLong(), anyList());
    }

    @Test
    void testCreateUserFailure() {
        // 测试插入用户失败的情况
        when(sysUserMapper.insert(any(SysUser.class))).thenReturn(0);

        SysUser user = new SysUser();
        user.setUsername("testadmin");
        user.setPassword("password123");
        user.setRealName("测试管理员");

        boolean result = sysUserService.createUser(user, null);

        assertFalse(result);
        verify(sysUserMapper).insert(any(SysUser.class));
    }

    @Test
    void testCreateUserSetsStatusEnabled() {
        // 测试创建用户时状态设置为启用
        when(sysUserMapper.insert(any(SysUser.class))).thenAnswer(inv -> {
            SysUser user = inv.getArgument(0);
            user.setId(1L);
            return 1;
        });

        SysUser user = new SysUser();
        user.setUsername("testadmin");
        user.setPassword("password123");
        user.setRealName("测试管理员");
        user.setStatus(CommonConstants.STATUS_DISABLED); // 设置为禁用

        sysUserService.createUser(user, null);

        // 验证状态被设置为启用
        verify(sysUserMapper).insert(argThat((SysUser u) -> u.getStatus() == CommonConstants.STATUS_ENABLED));
    }

    @Test
    void testUpdateUserSuccess() {
        // 模拟更新用户成功
        when(sysUserMapper.updateById(any(SysUser.class))).thenReturn(1);

        SysUser user = new SysUser();
        user.setId(1L);
        user.setRealName("更新后的姓名");
        user.setEmail("updated@example.com");
        user.setPhone("13900139000");
        user.setStatus(CommonConstants.STATUS_DISABLED);

        boolean result = sysUserService.updateUser(user, List.of(1L));

        assertTrue(result);
        verify(sysUserMapper).updateById(any(SysUser.class));
        verify(sysUserRoleMapper).deleteByUserId(1L);
        verify(sysUserRoleMapper).batchInsert(1L, List.of(1L));
    }

    @Test
    void testUpdateUserWithPassword() {
        // 测试更新用户时包含密码
        when(sysUserMapper.updateById(any(SysUser.class))).thenReturn(1);

        SysUser user = new SysUser();
        user.setId(1L);
        user.setPassword("newpassword123");
        user.setRealName("更新后的姓名");

        boolean result = sysUserService.updateUser(user, null);

        assertTrue(result);
        verify(sysUserMapper).updateById(any(SysUser.class));
        verify(passwordEncoder).encode("newpassword123");
    }

    @Test
    void testUpdateUserWithoutPassword() {
        // 测试更新用户时不包含密码
        when(sysUserMapper.updateById(any(SysUser.class))).thenReturn(1);

        SysUser user = new SysUser();
        user.setId(1L);
        user.setRealName("更新后的姓名");

        boolean result = sysUserService.updateUser(user, null);

        assertTrue(result);
        verify(sysUserMapper).updateById(any(SysUser.class));
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void testDeleteUserSuccess() {
        // 模拟删除用户成功
        when(sysUserMapper.deleteById(1L)).thenReturn(1);

        boolean result = sysUserService.deleteUser(1L);

        assertTrue(result);
        verify(sysUserMapper).deleteById(1L);
        verify(sysUserRoleMapper).deleteByUserId(1L);
    }

    @Test
    void testDeleteUserFailure() {
        // 模拟删除用户失败
        when(sysUserMapper.deleteById(1L)).thenReturn(0);

        boolean result = sysUserService.deleteUser(1L);

        assertFalse(result);
        verify(sysUserMapper).deleteById(1L);
    }

    @Test
    void testDeleteLastSuperAdminThrowsException() {
        // 测试：不能删除最后一个超级管理员
        // 模拟用户有超级管理员角色
        when(sysUserRoleMapper.selectList(any())).thenAnswer(inv -> {
            SysUserRole userRole = new SysUserRole();
            userRole.setUserId(1L);
            userRole.setRoleId(1L); // SUPER_ADMIN_ROLE_ID
            return List.of(userRole);
        });
        // 模拟只有一个超级管理员
        when(sysUserRoleMapper.countUsersByRoleId(1L)).thenReturn(1L);

        // 应该抛出异常
        assertThrows(Exception.class, () -> sysUserService.deleteUser(1L));
        verify(sysUserMapper, never()).deleteById(anyLong());
    }

    @Test
    void testDeleteNonSuperAdminSuccess() {
        // 测试：可以删除非超级管理员用户
        // 模拟用户没有超级管理员角色
        when(sysUserRoleMapper.selectList(any())).thenReturn(List.of());

        when(sysUserMapper.deleteById(1L)).thenReturn(1);

        boolean result = sysUserService.deleteUser(1L);

        assertTrue(result);
        verify(sysUserMapper).deleteById(1L);
        verify(sysUserRoleMapper).deleteByUserId(1L);
    }

    @Test
    void testDeleteSuperAdminNotLastSuccess() {
        // 测试：可以删除有超级管理员角色但不是最后一个的用户
        // 模拟用户有超级管理员角色
        when(sysUserRoleMapper.selectList(any())).thenAnswer(inv -> {
            SysUserRole userRole = new SysUserRole();
            userRole.setUserId(1L);
            userRole.setRoleId(1L); // SUPER_ADMIN_ROLE_ID
            return List.of(userRole);
        });
        // 模拟有多个超级管理员（不是最后一个）
        when(sysUserRoleMapper.countUsersByRoleId(1L)).thenReturn(2L);
        when(sysUserMapper.deleteById(1L)).thenReturn(1);

        boolean result = sysUserService.deleteUser(1L);

        assertTrue(result);
        verify(sysUserMapper).deleteById(1L);
        verify(sysUserRoleMapper).deleteByUserId(1L);
    }
}
