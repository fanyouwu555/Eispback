package com.aeisp.system.service.impl;

import com.aeisp.common.constant.CommonConstants;
import com.aeisp.system.entity.SysConfig;
import com.aeisp.system.mapper.SysConfigMapper;
import com.aeisp.system.service.SysConfigLogService;
import com.aeisp.system.service.SysConfigService;
import com.aeisp.system.vo.SysConfigVO;
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
class SysConfigServiceImplTest {

    private SysConfigMapper sysConfigMapper = mock(SysConfigMapper.class);

    @Mock
    private SysConfigLogService sysConfigLogService;

    @InjectMocks
    private SysConfigServiceImpl sysConfigService;

    private SysConfig testConfig;

    @BeforeEach
    void setUp() {
        testConfig = new SysConfig();
        testConfig.setId(1L);
        testConfig.setConfigKey("system.name");
        testConfig.setConfigValue("Test System");
        testConfig.setDescription("系统名称");
        testConfig.setEnvironment("all");
        testConfig.setIsEditable(1);
        testConfig.setCategory("platform");
        testConfig.setFieldType("text");
    }

    @Test
    void testGetConfigValue_Exists() {
        when(sysConfigMapper.selectByConfigKey("system.name", "all"))
                .thenReturn(testConfig);

        String value = sysConfigService.getConfigValue("system.name", "all");

        assertEquals("Test System", value);
        verify(sysConfigMapper).selectByConfigKey("system.name", "all");
    }

    @Test
    void testGetConfigValue_NotExists() {
        when(sysConfigMapper.selectByConfigKey("nonexistent", "all"))
                .thenReturn(null);

        String value = sysConfigService.getConfigValue("nonexistent", "all");

        assertNull(value);
        verify(sysConfigMapper).selectByConfigKey("nonexistent", "all");
    }

    @Test
    void testUpdateConfig_Success() {
        when(sysConfigMapper.selectByConfigKey("system.name", "all"))
                .thenReturn(testConfig);
        doReturn(1).when(sysConfigMapper).updateById(any(SysConfig.class));

        boolean result = sysConfigService.updateConfig("system.name", "Updated System", "all");

        assertTrue(result);
        assertEquals("Updated System", testConfig.getConfigValue());
        verify(sysConfigMapper).updateById(any(SysConfig.class));
        verify(sysConfigLogService).recordChange(
                eq("config"),
                eq(1L),
                eq("system.name"),
                eq("Test System"),
                eq("Updated System"),
                eq("all")
        );
    }

    @Test
    void testUpdateConfig_NotExists() {
        when(sysConfigMapper.selectByConfigKey("nonexistent", "all"))
                .thenReturn(null);

        boolean result = sysConfigService.updateConfig("nonexistent", "value", "all");

        assertFalse(result);
        verify(sysConfigMapper, never()).updateById(any(SysConfig.class));
        verify(sysConfigLogService, never()).recordChange(any(), any(), any(), any(), any(), any());
    }

    @Test
    void testListAll() {
        SysConfig config1 = new SysConfig();
        config1.setId(1L);
        config1.setConfigKey("system.name");
        config1.setConfigValue("System Name");
        config1.setDeleted(CommonConstants.DELETED_NO);

        SysConfig config2 = new SysConfig();
        config2.setId(2L);
        config2.setConfigKey("system.version");
        config2.setConfigValue("1.0.0");
        config2.setDeleted(CommonConstants.DELETED_NO);

        when(sysConfigMapper.selectList(any()))
                .thenReturn(List.of(config1, config2));

        List<SysConfigVO> result = sysConfigService.listAll();

        assertEquals(2, result.size());
        assertEquals("system.name", result.get(0).getConfigKey());
        assertEquals("system.version", result.get(1).getConfigKey());
        verify(sysConfigMapper).selectList(any());
    }

    @Test
    void testListAll_Empty() {
        when(sysConfigMapper.selectList(any()))
                .thenReturn(List.of());

        List<SysConfigVO> result = sysConfigService.listAll();

        assertTrue(result.isEmpty());
    }

    @Test
    void testListByCategory() {
        SysConfig config1 = new SysConfig();
        config1.setId(1L);
        config1.setConfigKey("storage.project");
        config1.setConfigValue("/projects/");
        config1.setCategory("storage");
        config1.setDeleted(CommonConstants.DELETED_NO);

        SysConfig config2 = new SysConfig();
        config2.setId(2L);
        config2.setConfigKey("storage.template");
        config2.setConfigValue("/templates/");
        config2.setCategory("storage");
        config2.setDeleted(CommonConstants.DELETED_NO);

        when(sysConfigMapper.selectByCategory("storage"))
                .thenReturn(List.of(config1, config2));

        List<SysConfigVO> result = sysConfigService.listByCategory("storage");

        assertEquals(2, result.size());
        assertEquals("storage", result.get(0).getCategory());
        assertEquals("storage", result.get(1).getCategory());
        verify(sysConfigMapper).selectByCategory("storage");
    }

    @Test
    void testListByCategory_Empty() {
        when(sysConfigMapper.selectByCategory("nonexistent"))
                .thenReturn(List.of());

        List<SysConfigVO> result = sysConfigService.listByCategory("nonexistent");

        assertTrue(result.isEmpty());
    }

    @Test
    void testUpdateConfig_WithEnvironmentSpecific() {
        SysConfig devConfig = new SysConfig();
        devConfig.setId(2L);
        devConfig.setConfigKey("official_website_url");
        devConfig.setConfigValue("https://dev.example.com");
        devConfig.setEnvironment("dev");
        devConfig.setDeleted(CommonConstants.DELETED_NO);

        when(sysConfigMapper.selectByConfigKey("official_website_url", "dev"))
                .thenReturn(devConfig);
        doReturn(1).when(sysConfigMapper).updateById(any(SysConfig.class));

        boolean result = sysConfigService.updateConfig(
                "official_website_url",
                "https://new-dev.example.com",
                "dev"
        );

        assertTrue(result);
        assertEquals("https://new-dev.example.com", devConfig.getConfigValue());
        verify(sysConfigLogService).recordChange(
                eq("config"),
                eq(2L),
                eq("official_website_url"),
                eq("https://dev.example.com"),
                eq("https://new-dev.example.com"),
                eq("dev")
        );
    }

    @Test
    void testGetConfigValue_WithEnvironment() {
        SysConfig devConfig = new SysConfig();
        devConfig.setId(1L);
        devConfig.setConfigKey("official_website_url");
        devConfig.setConfigValue("https://dev.example.com");
        devConfig.setEnvironment("dev");

        when(sysConfigMapper.selectByConfigKey("official_website_url", "dev"))
                .thenReturn(devConfig);

        String value = sysConfigService.getConfigValue("official_website_url", "dev");

        assertEquals("https://dev.example.com", value);
    }
}
