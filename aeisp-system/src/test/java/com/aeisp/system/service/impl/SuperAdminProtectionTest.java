package com.aeisp.system.service.impl;

import com.aeisp.common.exception.BizException;
import com.aeisp.system.code.SystemErrorCode;
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
 * 超级管理员保护机制测试。
 * 验证不能删除最后一个超级管理员，不能降级最后一个超级管理员。
 */
@ExtendWith(MockitoExtension.class)
class SuperAdminProtectionTest {

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
     * 测试1: 删除最后一个超级管理员抛出异常
     * 验证删除最后一个超级管理员时抛出 USER_CANNOT_DELETE_LAST_SUPER 异常
     */
    @Test
    void testDeleteLastSuperAdminThrowsException() {
        // 模拟用户有超级管理员角色
        when(sysUserRoleMapper.selectList(any())).thenAnswer(inv -> {
            SysUserRole userRole = new SysUserRole();
            userRole.setUserId(1L);
            userRole.setRoleId(1L); // SUPER_ADMIN_ROLE_ID
            return List.of(userRole);
        });

        // 模拟只有一个超级管理员
        when(sysUserRoleMapper.countUsersByRoleId(1L)).thenReturn(1L);

        // 验证抛出异常
        BizException exception = assertThrows(BizException.class, () -> sysUserService.deleteUser(1L));
        assertEquals(SystemErrorCode.USER_CANNOT_DELETE_LAST_SUPER.getCode(), exception.getCode());
        verify(sysUserMapper, never()).deleteById(anyLong());
    }

    /**
     * 测试2: 删除非最后一个超级管理员成功
     * 验证删除非最后一个超级管理员时成功
     */
    @Test
    void testDeleteNonLastSuperAdminSuccess() {
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

    /**
     * 测试3: 删除非超级管理员成功
     * 验证删除非超级管理员时成功
     */
    @Test
    void testDeleteNonSuperAdminSuccess() {
        // 模拟用户没有超级管理员角色
        when(sysUserRoleMapper.selectList(any())).thenReturn(List.of());

        when(sysUserMapper.deleteById(1L)).thenReturn(1);

        boolean result = sysUserService.deleteUser(1L);

        assertTrue(result);
        verify(sysUserMapper).deleteById(1L);
        verify(sysUserRoleMapper).deleteByUserId(1L);
    }

    /**
     * 测试4: 降级最后一个超级管理员抛出异常
     * 验证降级最后一个超级管理员时抛出 USER_CANNOT_DEMOTE_SUPER 异常
     */
    @Test
    void testDemoteLastSuperAdminThrowsException() {
        // 模拟用户有超级管理员角色
        when(sysUserRoleMapper.selectList(any())).thenAnswer(inv -> {
            SysUserRole userRole = new SysUserRole();
            userRole.setUserId(1L);
            userRole.setRoleId(1L); // SUPER_ADMIN_ROLE_ID
            return List.of(userRole);
        });

        // 模拟只有一个超级管理员
        when(sysUserRoleMapper.countUsersByRoleId(1L)).thenReturn(1L);

        SysUser user = new SysUser();
        user.setId(1L);
        user.setRealName("更新后的姓名");

        // 尝试更新用户，移除超级管理员角色（roleIds 为空或不包含 1L）
        List<Long> newRoleIds = List.of(2L, 3L); // 不包含超级管理员角色

        // 验证抛出异常
        BizException exception = assertThrows(BizException.class, () -> sysUserService.updateUser(user, newRoleIds));
        assertEquals(SystemErrorCode.USER_CANNOT_DEMOTE_SUPER.getCode(), exception.getCode());
        verify(sysUserMapper, never()).updateById(any(SysUser.class));
    }

    /**
     * 测试5: 降级非最后一个超级管理员成功
     * 验证降级非最后一个超级管理员时成功
     */
    @Test
    void testDemoteNonLastSuperAdminSuccess() {
        // 模拟用户有超级管理员角色
        when(sysUserRoleMapper.selectList(any())).thenAnswer(inv -> {
            SysUserRole userRole = new SysUserRole();
            userRole.setUserId(1L);
            userRole.setRoleId(1L); // SUPER_ADMIN_ROLE_ID
            return List.of(userRole);
        });

        // 模拟有多个超级管理员（不是最后一个）
        when(sysUserRoleMapper.countUsersByRoleId(1L)).thenReturn(2L);
        when(sysUserMapper.updateById(any(SysUser.class))).thenReturn(1);

        SysUser user = new SysUser();
        user.setId(1L);
        user.setRealName("更新后的姓名");

        // 尝试更新用户，移除超级管理员角色
        List<Long> newRoleIds = List.of(2L, 3L); // 不包含超级管理员角色

        boolean result = sysUserService.updateUser(user, newRoleIds);

        assertTrue(result);
        verify(sysUserMapper).updateById(any(SysUser.class));
        verify(sysUserRoleMapper).deleteByUserId(1L);
        verify(sysUserRoleMapper).batchInsert(1L, newRoleIds);
    }

    /**
     * 测试6: 保留超级管理员角色更新成功
     * 验证保留超级管理员角色更新时成功
     */
    @Test
    void testUpdateUserKeepSuperAdminRoleSuccess() {
        // 当新角色列表包含超级管理员角色时，不会触发保护逻辑
        // 因此不需要模拟 selectList 和 countUsersByRoleId
        when(sysUserMapper.updateById(any(SysUser.class))).thenReturn(1);

        SysUser user = new SysUser();
        user.setId(1L);
        user.setRealName("更新后的姓名");

        // 尝试更新用户，保留超级管理员角色
        List<Long> newRoleIds = List.of(1L, 2L); // 包含超级管理员角色

        boolean result = sysUserService.updateUser(user, newRoleIds);

        assertTrue(result);
        verify(sysUserMapper).updateById(any(SysUser.class));
        verify(sysUserRoleMapper).deleteByUserId(1L);
        verify(sysUserRoleMapper).batchInsert(1L, newRoleIds);
    }

    /**
     * 测试7: 更新非超级管理员用户成功
     * 验证更新非超级管理员用户时成功
     */
    @Test
    void testUpdateNonSuperAdminSuccess() {
        // 模拟用户没有超级管理员角色
        when(sysUserRoleMapper.selectList(any())).thenReturn(List.of());

        when(sysUserMapper.updateById(any(SysUser.class))).thenReturn(1);

        SysUser user = new SysUser();
        user.setId(1L);
        user.setRealName("更新后的姓名");

        // 尝试更新用户，分配新角色
        List<Long> newRoleIds = List.of(2L, 3L);

        boolean result = sysUserService.updateUser(user, newRoleIds);

        assertTrue(result);
        verify(sysUserMapper).updateById(any(SysUser.class));
        verify(sysUserRoleMapper).deleteByUserId(1L);
        verify(sysUserRoleMapper).batchInsert(1L, newRoleIds);
    }
}
