package com.aeisp.system.service.impl;

import com.aeisp.common.exception.BizException;
import com.aeisp.system.entity.SysFeatureSwitch;
import com.aeisp.system.mapper.SysFeatureSwitchMapper;
import com.aeisp.system.service.SysConfigLogService;
import com.aeisp.system.service.SysFeatureSwitchService;
import com.aeisp.system.vo.SysFeatureSwitchVO;
import org.junit.jupiter.api.BeforeEach;
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
class SysFeatureSwitchServiceImplTest {

    private SysFeatureSwitchMapper sysFeatureSwitchMapper = mock(SysFeatureSwitchMapper.class);

    @Mock
    private SysConfigLogService sysConfigLogService;

    @InjectMocks
    private SysFeatureSwitchServiceImpl sysFeatureSwitchService;

    private SysFeatureSwitch testSwitch;

    @BeforeEach
    void setUp() {
        testSwitch = new SysFeatureSwitch();
        testSwitch.setId(1L);
        testSwitch.setFeatureKey("user.register");
        testSwitch.setFeatureName("用户注册");
        testSwitch.setCategory("business");
        testSwitch.setEnabled(true);
        testSwitch.setDescription("开启/关闭用户注册功能");
        testSwitch.setSortOrder(1);
        testSwitch.setDeleted(0);
    }

    @Test
    void testIsEnabled_True() {
        when(sysFeatureSwitchMapper.selectOne(any())).thenReturn(testSwitch);

        boolean result = sysFeatureSwitchService.isEnabled("user.register");

        assertTrue(result);
        verify(sysFeatureSwitchMapper).selectOne(any());
    }

    @Test
    void testIsEnabled_False() {
        testSwitch.setEnabled(false);
        when(sysFeatureSwitchMapper.selectOne(any())).thenReturn(testSwitch);

        boolean result = sysFeatureSwitchService.isEnabled("user.register");

        assertFalse(result);
    }

    @Test
    void testIsEnabled_NotExists() {
        when(sysFeatureSwitchMapper.selectOne(any())).thenReturn(null);

        boolean result = sysFeatureSwitchService.isEnabled("nonexistent");

        assertTrue(result); // Default to true when not exists
    }

    @Test
    void testIsEnabled_Cached() {
        when(sysFeatureSwitchMapper.selectOne(any())).thenReturn(testSwitch);

        // First call - should query database
        boolean result1 = sysFeatureSwitchService.isEnabled("user.register");
        assertTrue(result1);

        // Second call - should use cache
        boolean result2 = sysFeatureSwitchService.isEnabled("user.register");
        assertTrue(result2);

        // Verify database was only queried once
        verify(sysFeatureSwitchMapper, times(1)).selectOne(any());
    }

    @Test
    void testListAll() {
        SysFeatureSwitch switch1 = new SysFeatureSwitch();
        switch1.setId(1L);
        switch1.setFeatureKey("user.register");
        switch1.setFeatureName("用户注册");
        switch1.setCategory("business");
        switch1.setEnabled(true);
        switch1.setSortOrder(1);
        switch1.setDeleted(0);

        SysFeatureSwitch switch2 = new SysFeatureSwitch();
        switch2.setId(2L);
        switch2.setFeatureKey("user.login");
        switch2.setFeatureName("账号登录");
        switch2.setCategory("business");
        switch2.setEnabled(true);
        switch2.setSortOrder(2);
        switch2.setDeleted(0);

        when(sysFeatureSwitchMapper.selectList(any()))
                .thenReturn(List.of(switch1, switch2));

        List<SysFeatureSwitchVO> result = sysFeatureSwitchService.listAll();

        assertEquals(2, result.size());
        assertEquals("user.register", result.get(0).getFeatureKey());
        assertEquals("user.login", result.get(1).getFeatureKey());
        verify(sysFeatureSwitchMapper).selectList(any());
    }

    @Test
    void testListAll_Empty() {
        when(sysFeatureSwitchMapper.selectList(any()))
                .thenReturn(List.of());

        List<SysFeatureSwitchVO> result = sysFeatureSwitchService.listAll();

        assertTrue(result.isEmpty());
    }

    @Test
    void testToggle_EnableToDisable() {
        testSwitch.setEnabled(true);
        when(sysFeatureSwitchMapper.selectById(1L)).thenReturn(testSwitch);
        when(sysFeatureSwitchMapper.updateById(any(SysFeatureSwitch.class))).thenReturn(1);

        sysFeatureSwitchService.toggle(1L);

        assertFalse(testSwitch.getEnabled());
        verify(sysFeatureSwitchMapper).updateById(any(SysFeatureSwitch.class));
        verify(sysConfigLogService).recordChange(
                eq("feature"),
                eq(1L),
                eq("user.register"),
                eq("true"),
                eq("false"),
                eq("all")
        );
    }

    @Test
    void testToggle_DisableToEnable() {
        testSwitch.setEnabled(false);
        when(sysFeatureSwitchMapper.selectById(1L)).thenReturn(testSwitch);
        when(sysFeatureSwitchMapper.updateById(any(SysFeatureSwitch.class))).thenReturn(1);

        sysFeatureSwitchService.toggle(1L);

        assertTrue(testSwitch.getEnabled());
        verify(sysFeatureSwitchMapper).updateById(any(SysFeatureSwitch.class));
        verify(sysConfigLogService).recordChange(
                eq("feature"),
                eq(1L),
                eq("user.register"),
                eq("false"),
                eq("true"),
                eq("all")
        );
    }

    @Test
    void testToggle_NotExists() {
        when(sysFeatureSwitchMapper.selectById(999L)).thenReturn(null);

        BizException exception = assertThrows(BizException.class,
                () -> sysFeatureSwitchService.toggle(999L));

        assertEquals("功能开关不存在", exception.getMessage());
        verify(sysFeatureSwitchMapper, never()).updateById(any(SysFeatureSwitch.class));
    }

    @Test
    void testToggle_DeletedSwitch() {
        testSwitch.setDeleted(1);
        when(sysFeatureSwitchMapper.selectById(1L)).thenReturn(testSwitch);

        BizException exception = assertThrows(BizException.class,
                () -> sysFeatureSwitchService.toggle(1L));

        assertEquals("功能开关不存在", exception.getMessage());
        verify(sysFeatureSwitchMapper, never()).updateById(any(SysFeatureSwitch.class));
    }

    @Test
    void testToggleMaintenance_Enable() {
        SysFeatureSwitch maintenanceSwitch = new SysFeatureSwitch();
        maintenanceSwitch.setId(18L);
        maintenanceSwitch.setFeatureKey("maintenance");
        maintenanceSwitch.setEnabled(false);
        maintenanceSwitch.setDeleted(0);

        SysFeatureSwitch businessSwitch1 = new SysFeatureSwitch();
        businessSwitch1.setId(1L);
        businessSwitch1.setFeatureKey("user.register");
        businessSwitch1.setCategory("business");
        businessSwitch1.setEnabled(true);
        businessSwitch1.setDeleted(0);

        SysFeatureSwitch businessSwitch2 = new SysFeatureSwitch();
        businessSwitch2.setId(2L);
        businessSwitch2.setFeatureKey("user.login");
        businessSwitch2.setCategory("business");
        businessSwitch2.setEnabled(true);
        businessSwitch2.setDeleted(0);

        when(sysFeatureSwitchMapper.selectOne(any()))
                .thenReturn(maintenanceSwitch);
        when(sysFeatureSwitchMapper.selectList(any()))
                .thenReturn(List.of(businessSwitch1, businessSwitch2));
        when(sysFeatureSwitchMapper.updateById(any(SysFeatureSwitch.class))).thenReturn(1);

        sysFeatureSwitchService.toggleMaintenance(true);

        assertTrue(maintenanceSwitch.getEnabled());
        assertFalse(businessSwitch1.getEnabled());
        assertFalse(businessSwitch2.getEnabled());
        verify(sysFeatureSwitchMapper, times(3)).updateById(any(SysFeatureSwitch.class));
        verify(sysConfigLogService, times(3)).recordChange(any(), any(), any(), any(), any(), any());
    }

    @Test
    void testToggleMaintenance_Disable() {
        SysFeatureSwitch maintenanceSwitch = new SysFeatureSwitch();
        maintenanceSwitch.setId(18L);
        maintenanceSwitch.setFeatureKey("maintenance");
        maintenanceSwitch.setEnabled(true);
        maintenanceSwitch.setDeleted(0);

        SysFeatureSwitch businessSwitch1 = new SysFeatureSwitch();
        businessSwitch1.setId(1L);
        businessSwitch1.setFeatureKey("user.register");
        businessSwitch1.setCategory("business");
        businessSwitch1.setEnabled(false);
        businessSwitch1.setDeleted(0);

        SysFeatureSwitch businessSwitch2 = new SysFeatureSwitch();
        businessSwitch2.setId(2L);
        businessSwitch2.setFeatureKey("user.login");
        businessSwitch2.setCategory("business");
        businessSwitch2.setEnabled(false);
        businessSwitch2.setDeleted(0);

        when(sysFeatureSwitchMapper.selectOne(any()))
                .thenReturn(maintenanceSwitch);
        when(sysFeatureSwitchMapper.selectList(any()))
                .thenReturn(List.of(businessSwitch1, businessSwitch2));
        when(sysFeatureSwitchMapper.updateById(any(SysFeatureSwitch.class))).thenReturn(1);

        sysFeatureSwitchService.toggleMaintenance(false);

        assertFalse(maintenanceSwitch.getEnabled());
        assertTrue(businessSwitch1.getEnabled());
        assertTrue(businessSwitch2.getEnabled());
        verify(sysFeatureSwitchMapper, times(3)).updateById(any(SysFeatureSwitch.class));
        verify(sysConfigLogService, times(3)).recordChange(any(), any(), any(), any(), any(), any());
    }

    @Test
    void testToggleMaintenance_MaintenanceSwitchNotFound() {
        when(sysFeatureSwitchMapper.selectOne(any()))
                .thenReturn(null);

        sysFeatureSwitchService.toggleMaintenance(true);

        verify(sysFeatureSwitchMapper, never()).updateById(any(SysFeatureSwitch.class));
        verify(sysConfigLogService, never()).recordChange(any(), any(), any(), any(), any(), any());
    }

    @Test
    void testToggleMaintenance_NoBusinessSwitchesToToggle() {
        SysFeatureSwitch maintenanceSwitch = new SysFeatureSwitch();
        maintenanceSwitch.setId(18L);
        maintenanceSwitch.setFeatureKey("maintenance");
        maintenanceSwitch.setEnabled(false);
        maintenanceSwitch.setDeleted(0);

        // Business switches are already in the desired state
        SysFeatureSwitch businessSwitch1 = new SysFeatureSwitch();
        businessSwitch1.setId(1L);
        businessSwitch1.setFeatureKey("user.register");
        businessSwitch1.setCategory("business");
        businessSwitch1.setEnabled(false); // Already disabled
        businessSwitch1.setDeleted(0);

        when(sysFeatureSwitchMapper.selectOne(any()))
                .thenReturn(maintenanceSwitch);
        when(sysFeatureSwitchMapper.selectList(any()))
                .thenReturn(List.of(businessSwitch1));
        when(sysFeatureSwitchMapper.updateById(any(SysFeatureSwitch.class))).thenReturn(1);

        sysFeatureSwitchService.toggleMaintenance(true);

        assertTrue(maintenanceSwitch.getEnabled());
        // businessSwitch1 should remain disabled (no change)
        verify(sysFeatureSwitchMapper, times(1)).updateById(any(SysFeatureSwitch.class));
        verify(sysConfigLogService, times(1)).recordChange(any(), any(), any(), any(), any(), any());
    }

    @Test
    void testIsEnabled_CacheClearOnToggle() {
        // First call - populate cache
        when(sysFeatureSwitchMapper.selectOne(any()))
                .thenReturn(testSwitch);
        sysFeatureSwitchService.isEnabled("user.register");

        // Toggle should clear cache
        when(sysFeatureSwitchMapper.selectById(1L)).thenReturn(testSwitch);
        doReturn(1).when(sysFeatureSwitchMapper).updateById(any(SysFeatureSwitch.class));
        sysFeatureSwitchService.toggle(1L);

        // Second call after toggle - should query database again
        testSwitch.setEnabled(false);
        when(sysFeatureSwitchMapper.selectOne(any()))
                .thenReturn(testSwitch);
        boolean result = sysFeatureSwitchService.isEnabled("user.register");

        assertFalse(result);
        // Should have queried database twice (once for initial cache, once after toggle)
        verify(sysFeatureSwitchMapper, times(2)).selectOne(any());
    }
}
