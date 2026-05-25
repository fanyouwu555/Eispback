package com.aeisp.template.service.impl;

import com.aeisp.common.PageResult;
import com.aeisp.common.exception.BizException;
import com.aeisp.template.dto.request.CreateTemplateRequest;
import com.aeisp.template.dto.request.TemplateQueryRequest;
import com.aeisp.template.dto.request.UpdateTemplateRequest;
import com.aeisp.template.dto.vo.TplTemplateDetailVO;
import com.aeisp.template.dto.vo.TplTemplateVO;
import com.aeisp.template.entity.TplTemplate;
import com.aeisp.template.entity.TplTemplateVersion;
import com.aeisp.template.enums.TemplateStatusEnum;
import com.aeisp.template.mapper.TplTemplateMapper;
import com.aeisp.template.mapper.TplTemplateVersionMapper;
import com.aeisp.common.service.ResourceServerService;
import com.aeisp.template.service.TemplateStorageService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * TplTemplateServiceImpl 单元测试。
 */
@ExtendWith(MockitoExtension.class)
class TplTemplateServiceImplTest {

    @Mock
    private TplTemplateMapper templateMapper;
    @Mock
    private TplTemplateVersionMapper versionMapper;
    @Mock
    private TemplateStorageService templateStorageService;
    @Mock
    private ResourceServerService resourceServerService;

    @InjectMocks
    private TplTemplateServiceImpl tplTemplateService;

    @Test
    void testCreateTemplate() throws IOException {
        Path tempDir = Files.createTempDirectory("tpl-test-");
        Path tempFile = tempDir.resolve("template.zip");
        Files.write(tempFile, "dummy zip content".getBytes());

        try {
            when(templateMapper.insert(any(TplTemplate.class))).thenAnswer(inv -> {
                TplTemplate t = inv.getArgument(0);
                t.setId(1L);
                return 1;
            });
            when(templateStorageService.storeZip(anyLong(), anyString(), any())).thenReturn("templates/1/v1.0.0/template.zip");
            when(templateStorageService.getZipAbsolutePath(anyLong(), anyString())).thenReturn(tempFile.toString());
            doNothing().when(templateStorageService).extractZip(anyString(), anyString());
            when(resourceServerService.uploadExtractedFiles(anyLong(), anyString(), any())).thenReturn(List.of());
            when(versionMapper.insert(any(TplTemplateVersion.class))).thenAnswer(inv -> {
                TplTemplateVersion v = inv.getArgument(0);
                v.setId(10L);
                return 1;
            });
            when(templateMapper.selectMaxTemplateCode(anyString())).thenReturn(null);
            when(templateMapper.updateById(any(TplTemplate.class))).thenReturn(1);

            CreateTemplateRequest request = new CreateTemplateRequest();
            request.setTemplateName("测试模板");
            request.setVersionNo("v1.0.0");
            request.setDifficulty(3);
            request.setZipFile(new MockMultipartFile("file", "test.zip", "application/zip", "content".getBytes()));

            assertTrue(tplTemplateService.createTemplate(request));
            // 验证模板编码自动生成
            verify(templateMapper).selectMaxTemplateCode(anyString());
            verify(templateMapper).insert(any(TplTemplate.class));
            verify(versionMapper).insert(any(TplTemplateVersion.class));
        } finally {
            Files.deleteIfExists(tempFile);
            Files.deleteIfExists(tempDir);
        }
    }

    @Test
    void testUpdateTemplateInfoNotFound() {
        when(templateMapper.selectById(1L)).thenReturn(null);
        assertThrows(BizException.class, () -> tplTemplateService.updateTemplateInfo(1L, new UpdateTemplateRequest()));
    }

    @Test
    void testUpdateTemplateInfoSuccess() {
        TplTemplate template = new TplTemplate();
        template.setId(1L);
        when(templateMapper.selectById(1L)).thenReturn(template);
        when(templateMapper.updateById(any(TplTemplate.class))).thenReturn(1);

        UpdateTemplateRequest request = new UpdateTemplateRequest();
        request.setTemplateName("新名称");
        request.setDifficulty(3);

        assertTrue(tplTemplateService.updateTemplateInfo(1L, request));
        assertEquals("新名称", template.getTemplateName());
    }

    @Test
    void testRollbackVersion() {
        TplTemplate template = new TplTemplate();
        template.setId(1L);
        template.setCurrentVersionId(10L);
        when(templateMapper.selectById(1L)).thenReturn(template);

        TplTemplateVersion version = new TplTemplateVersion();
        version.setId(20L);
        version.setTemplateId(1L);
        when(versionMapper.selectById(20L)).thenReturn(version);
        when(templateMapper.updateById(any(TplTemplate.class))).thenReturn(1);

        assertTrue(tplTemplateService.rollbackVersion(1L, 20L));
        assertEquals(20L, template.getCurrentVersionId());
    }

    @Test
    void testRollbackVersionNotFound() {
        when(templateMapper.selectById(1L)).thenReturn(null);
        assertThrows(BizException.class, () -> tplTemplateService.rollbackVersion(1L, 20L));
    }

    @Test
    void testToggleStatus() {
        TplTemplate template = new TplTemplate();
        template.setId(1L);
        when(templateMapper.selectById(1L)).thenReturn(template);
        when(templateMapper.updateById(any(TplTemplate.class))).thenReturn(1);

        assertTrue(tplTemplateService.toggleStatus(1L, TemplateStatusEnum.ACTIVE.getCode()));
        assertEquals(TemplateStatusEnum.ACTIVE.getCode(), template.getStatus());
    }

    @Test
    void testDeleteTemplate() {
        TplTemplate template = new TplTemplate();
        template.setId(1L);
        when(templateMapper.selectById(1L)).thenReturn(template);
        when(templateMapper.deleteById(1L)).thenReturn(1);
        when(versionMapper.delete(any())).thenReturn(2);
        when(versionMapper.selectList(any())).thenReturn(List.of());
        doNothing().when(templateStorageService).deleteTemplateFiles(1L);

        assertTrue(tplTemplateService.deleteTemplate(1L));
        verify(templateMapper).deleteById(1L);
        verify(templateStorageService).deleteTemplateFiles(1L);
    }

    @Test
    void testListTemplates() {
        TplTemplate template = new TplTemplate();
        template.setId(1L);
        template.setTemplateName("测试模板");
        template.setStatus(TemplateStatusEnum.ACTIVE.getCode());
        template.setCurrentVersionId(10L);

        Page<TplTemplate> page = new Page<>(1, 10);
        page.setRecords(List.of(template));
        page.setTotal(1);
        when(templateMapper.selectPage(any(Page.class), any())).thenReturn(page);

        TplTemplateVersion version = new TplTemplateVersion();
        version.setId(10L);
        version.setVersionNo("v1.0.0");
        when(versionMapper.selectById(10L)).thenReturn(version);

        TemplateQueryRequest request = new TemplateQueryRequest();
        request.setPageNum(1L);
        request.setPageSize(10L);
        request.setStatus(TemplateStatusEnum.ACTIVE.getCode());

        PageResult<TplTemplateVO> result = tplTemplateService.listTemplates(request);
        assertEquals(1, result.getTotal());
        assertEquals("测试模板", result.getList().get(0).getTemplateName());
        assertEquals("v1.0.0", result.getList().get(0).getCurrentVersionNo());
    }

    @Test
    void testGetPublicDetailOffline() {
        TplTemplate template = new TplTemplate();
        template.setId(1L);
        template.setStatus(TemplateStatusEnum.OFFLINE.getCode());
        when(templateMapper.selectById(1L)).thenReturn(template);

        assertThrows(BizException.class, () -> tplTemplateService.getPublicDetail(1L));
    }

    @Test
    void testGetPublicDetailSuccess() {
        TplTemplate template = new TplTemplate();
        template.setId(1L);
        template.setStatus(TemplateStatusEnum.ACTIVE.getCode());
        template.setCurrentVersionId(10L);
        when(templateMapper.selectById(1L)).thenReturn(template);

        TplTemplateVersion version = new TplTemplateVersion();
        version.setId(10L);
        version.setTemplateId(1L);
        version.setVersionNo("v1.0.0");
        when(versionMapper.selectById(10L)).thenReturn(version);
        when(versionMapper.selectByTemplateId(1L)).thenReturn(List.of());
        when(templateStorageService.listFiles(1L, "v1.0.0")).thenReturn(List.of());
        when(resourceServerService.getBaseUrl()).thenReturn("http://localhost/EISP/Resource/Template/");

        TplTemplateDetailVO vo = tplTemplateService.getPublicDetail(1L);
        assertNotNull(vo);
        assertEquals(TemplateStatusEnum.ACTIVE.getCode(), template.getStatus());
    }

    @Test
    void testUploadNewVersionWithExtraFields() throws IOException {
        Path tempDir = Files.createTempDirectory("tpl-test-ext-");
        Path tempFile = tempDir.resolve("template.zip");
        Files.write(tempFile, "dummy zip content".getBytes());

        try {
            TplTemplate template = new TplTemplate();
            template.setId(1L);
            template.setCurrentVersionId(10L);
            when(templateMapper.selectById(1L)).thenReturn(template);
            when(templateStorageService.storeZip(anyLong(), anyString(), any())).thenReturn("templates/1/v1.1.0/template.zip");
            when(templateStorageService.getZipAbsolutePath(anyLong(), anyString())).thenReturn(tempFile.toString());
            doNothing().when(templateStorageService).extractZip(anyString(), anyString());
            when(resourceServerService.uploadExtractedFiles(anyLong(), anyString(), any())).thenReturn(List.of());
            when(versionMapper.insert(any(TplTemplateVersion.class))).thenAnswer(inv -> {
                TplTemplateVersion v = inv.getArgument(0);
                v.setId(20L);
                return 1;
            });
            when(templateMapper.updateById(any(TplTemplate.class))).thenReturn(1);

            MockMultipartFile mockFile = new MockMultipartFile("file", "test.zip", "application/zip", "content".getBytes());
            assertTrue(tplTemplateService.uploadNewVersion(1L, mockFile, "v1.1.0", "bug fixes",
                    "2026-06-01T00:00:00", "2026-12-31T23:59:59", 4, 1, "onetime", new java.math.BigDecimal("99.00")));

            // 验证模板字段被更新（needUpdate=true 触发额外 updateById + currentVersionId 更新 = 2次）
            verify(templateMapper, times(2)).updateById(any(TplTemplate.class));
        } finally {
            Files.deleteIfExists(tempFile);
            Files.deleteIfExists(tempDir);
        }
    }

    @Test
    void testListOnlineTemplates() {
        TplTemplate template = new TplTemplate();
        template.setId(1L);
        template.setTemplateName("在线模板");
        template.setStatus(TemplateStatusEnum.ACTIVE.getCode());
        template.setCurrentVersionId(10L);
        when(templateMapper.selectOnlineList()).thenReturn(List.of(template));

        TplTemplateVersion version = new TplTemplateVersion();
        version.setId(10L);
        version.setVersionNo("v1.0.0");
        when(versionMapper.selectById(10L)).thenReturn(version);

        List<TplTemplateVO> result = tplTemplateService.listOnlineTemplates();
        assertEquals(1, result.size());
        assertEquals("在线模板", result.get(0).getTemplateName());
    }
}
