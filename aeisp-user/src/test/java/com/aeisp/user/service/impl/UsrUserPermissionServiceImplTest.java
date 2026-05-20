package com.aeisp.user.service.impl;

import com.aeisp.user.entity.UsrUserPermission;
import com.aeisp.user.mapper.UsrUserPermissionMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsrUserPermissionServiceImplTest {

    @Mock
    private UsrUserPermissionMapper usrUserPermissionMapper;

    @InjectMocks
    private UsrUserPermissionServiceImpl usrUserPermissionService;

    @Test
    void testGetByUserIdReturnsPermissions() {
        UsrUserPermission perm = new UsrUserPermission();
        perm.setId(1L);
        perm.setUserId(1L);
        perm.setPermKey("ai_dialog_enabled");
        perm.setPermValue("true");
        when(usrUserPermissionMapper.selectByUserId(1L)).thenReturn(List.of(perm));

        List<UsrUserPermission> result = usrUserPermissionService.getByUserId(1L);
        assertEquals(1, result.size());
        assertEquals("ai_dialog_enabled", result.get(0).getPermKey());
        assertEquals("true", result.get(0).getPermValue());
    }

    @Test
    void testGetByUserIdReturnsEmptyWhenNone() {
        when(usrUserPermissionMapper.selectByUserId(1L)).thenReturn(List.of());
        List<UsrUserPermission> result = usrUserPermissionService.getByUserId(1L);
        assertTrue(result.isEmpty());
    }

    @Test
    void testUpdatePermissionsReplacesAll() {
        UsrUserPermission p1 = new UsrUserPermission();
        p1.setPermKey("ai_dialog_enabled");
        p1.setPermValue("false");

        UsrUserPermission p2 = new UsrUserPermission();
        p2.setPermKey("max_projects");
        p2.setPermValue("20");

        when(usrUserPermissionMapper.delete(any())).thenReturn(2);
        when(usrUserPermissionMapper.insert(isA(UsrUserPermission.class))).thenReturn(1);

        boolean result = usrUserPermissionService.updatePermissions(1L, List.of(p1, p2));
        assertTrue(result);
        verify(usrUserPermissionMapper).delete(any());
        verify(usrUserPermissionMapper, times(2)).insert(isA(UsrUserPermission.class));
    }

    @Test
    void testUpdatePermissionsWithEmptyListClearsAll() {
        when(usrUserPermissionMapper.delete(any())).thenReturn(0);

        boolean result = usrUserPermissionService.updatePermissions(1L, List.of());
        assertTrue(result);
        verify(usrUserPermissionMapper).delete(any());
        verify(usrUserPermissionMapper, never()).insert(isA(UsrUserPermission.class));
    }

    @Test
    void testGetByUserIdAndKeyReturnsCorrectPermission() {
        UsrUserPermission perm = new UsrUserPermission();
        perm.setUserId(1L);
        perm.setPermKey("file_upload_enabled");
        perm.setPermValue("false");
        when(usrUserPermissionMapper.selectByUserIdAndKey(1L, "file_upload_enabled")).thenReturn(perm);

        UsrUserPermission result = usrUserPermissionService.getByUserIdAndKey(1L, "file_upload_enabled");
        assertNotNull(result);
        assertEquals("false", result.getPermValue());
    }

    @Test
    void testGetByUserIdAndKeyReturnsNullWhenNotFound() {
        when(usrUserPermissionMapper.selectByUserIdAndKey(1L, "nonexistent")).thenReturn(null);
        assertNull(usrUserPermissionService.getByUserIdAndKey(1L, "nonexistent"));
    }
}