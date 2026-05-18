package com.aeisp.user.service.impl;

import com.aeisp.common.exception.BizException;
import com.aeisp.user.entity.UsrUser;
import com.aeisp.user.mapper.UsrUserMapper;
import com.aeisp.user.request.UserBatchCreateRequest;
import com.aeisp.user.request.UserCreateRequest;
import com.aeisp.user.request.UserUpdateRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * UsrUserServiceImpl 单元测试。
 */
@ExtendWith(MockitoExtension.class)
class UsrUserServiceImplTest {

    @Mock
    private UsrUserMapper usrUserMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsrUserServiceImpl usrUserService;

    @BeforeEach
    void setUp() {
        when(passwordEncoder.encode(any())).thenReturn("encoded_password");
    }

    @Test
    void testRegisterSuccess() {
        when(usrUserMapper.selectByUsername("testuser")).thenReturn(null);
        when(usrUserMapper.selectByPhone("13800138000")).thenReturn(null);
        when(usrUserMapper.selectByEmail("test@example.com")).thenReturn(null);
        when(usrUserMapper.insert(any(UsrUser.class))).thenReturn(1);

        UsrUser user = new UsrUser();
        user.setUsername("testuser");
        user.setPhone("13800138000");
        user.setEmail("test@example.com");
        user.setPassword("password123");

        assertTrue(usrUserService.register(user));
        verify(passwordEncoder).encode("password123");
    }

    @Test
    void testRegisterDuplicateUsername() {
        UsrUser existing = new UsrUser();
        existing.setUsername("testuser");
        when(usrUserMapper.selectByUsername("testuser")).thenReturn(existing);

        UsrUser user = new UsrUser();
        user.setUsername("testuser");

        BizException exception = assertThrows(BizException.class, () -> usrUserService.register(user));
        assertEquals("用户名已存在", exception.getMessage());
    }

    @Test
    void testCreateByAdminSuccess() {
        when(usrUserMapper.selectByUsername("adminuser")).thenReturn(null);
        when(usrUserMapper.selectByPhone("13900139000")).thenReturn(null);
        when(usrUserMapper.selectByEmail("admin@example.com")).thenReturn(null);
        when(usrUserMapper.insert(any(UsrUser.class))).thenReturn(1);

        UserCreateRequest request = new UserCreateRequest();
        request.setUsername("adminuser");
        request.setPhone("13900139000");
        request.setEmail("admin@example.com");
        request.setPassword("password123");

        assertTrue(usrUserService.createByAdmin(request));
    }

    @Test
    void testBatchCreateWithEmptyList() {
        UserBatchCreateRequest request = new UserBatchCreateRequest();
        assertFalse(usrUserService.batchCreate(request));
    }

    @Test
    void testBatchCreateSuccess() {
        when(usrUserMapper.batchInsert(anyList())).thenReturn(2);

        UserCreateRequest item1 = new UserCreateRequest();
        item1.setUsername("user1");
        item1.setPhone("13800138001");
        item1.setEmail("user1@example.com");
        item1.setPassword("pass123");

        UserCreateRequest item2 = new UserCreateRequest();
        item2.setUsername("user2");
        item2.setPhone("13800138002");
        item2.setEmail("user2@example.com");
        item2.setPassword("pass456");

        UserBatchCreateRequest request = new UserBatchCreateRequest();
        request.setUsers(java.util.List.of(item1, item2));

        assertTrue(usrUserService.batchCreate(request));
        verify(usrUserMapper).batchInsert(anyList());
        verify(passwordEncoder, times(2)).encode(any());
    }

    @Test
    void testUpdateUserNotFound() {
        when(usrUserMapper.selectById(1L)).thenReturn(null);

        UserUpdateRequest request = new UserUpdateRequest();
        request.setId(1L);

        assertThrows(BizException.class, () -> usrUserService.updateUser(request));
    }

    @Test
    void testResetPassword() {
        when(usrUserMapper.updateById(any(UsrUser.class))).thenReturn(1);

        assertTrue(usrUserService.resetPassword(1L, "newpassword"));
        verify(passwordEncoder).encode("newpassword");
    }
}
