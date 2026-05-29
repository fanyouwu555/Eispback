package com.aeisp.library.service;

import com.aeisp.common.PageResult;
import com.aeisp.common.exception.BizException;
import com.aeisp.common.service.ResourceServerService;
import com.aeisp.library.code.LibraryErrorCode;
import com.aeisp.library.dto.request.CreateLibraryRequest;
import com.aeisp.library.dto.request.LibraryQueryRequest;
import com.aeisp.library.dto.request.UpdateLibraryRequest;
import com.aeisp.library.dto.vo.LibResourceDetailVO;
import com.aeisp.library.dto.vo.LibResourceVO;
import com.aeisp.library.entity.LibResource;
import com.aeisp.library.entity.LibResourceVersion;
import com.aeisp.library.mapper.LibResourceMapper;
import com.aeisp.library.mapper.LibResourceVersionMapper;
import com.aeisp.library.service.impl.LibraryResourceServiceImpl;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LibraryResourceServiceImplTest {

    @Mock
    private LibResourceMapper resourceMapper;

    @Mock
    private LibResourceVersionMapper versionMapper;

    @Mock
    private LibraryStorageService libraryStorageService;

    @Mock
    private ResourceServerService resourceServerService;

    @InjectMocks
    private LibraryResourceServiceImpl libraryResourceService;

    @Test
    void testUpdateLibraryInfo_NotFound() {
        when(resourceMapper.selectById(1L)).thenReturn(null);

        UpdateLibraryRequest request = new UpdateLibraryRequest();
        request.setResourceName("Test");

        assertThrows(BizException.class, () -> libraryResourceService.updateLibraryInfo(1L, request));
    }

    @Test
    void testToggleStatus() {
        LibResource resource = new LibResource();
        resource.setId(1L);
        resource.setStatus(0);

        when(resourceMapper.selectById(1L)).thenReturn(resource);
        when(resourceMapper.updateById(any(LibResource.class))).thenReturn(1);

        boolean result = libraryResourceService.toggleStatus(1L, 1);

        assertTrue(result);
        assertEquals(1, resource.getStatus());
    }

    @Test
    void testRollbackVersion_NotFound() {
        when(resourceMapper.selectById(1L)).thenReturn(null);

        assertThrows(BizException.class, () -> libraryResourceService.rollbackVersion(1L, 1L));
    }

    @Test
    void testRollbackVersion_VersionNotBelong() {
        LibResource resource = new LibResource();
        resource.setId(1L);

        LibResourceVersion version = new LibResourceVersion();
        version.setId(2L);
        version.setResourceId(3L); // different resource

        when(resourceMapper.selectById(1L)).thenReturn(resource);
        when(versionMapper.selectById(2L)).thenReturn(version);

        assertThrows(BizException.class, () -> libraryResourceService.rollbackVersion(1L, 2L));
    }

    @Test
    void testListLibraries() {
        LibraryQueryRequest request = new LibraryQueryRequest();
        request.setResourceName("Test");
        request.setStatus(1);

        Page<LibResource> page = new Page<>();
        page.setRecords(List.of());
        page.setTotal(0);
        page.setCurrent(1);
        page.setSize(10);

        when(resourceMapper.selectPage(any(), any())).thenReturn(page);

        PageResult<LibResourceVO> result = libraryResourceService.listLibraries(request);

        assertNotNull(result);
        assertEquals(0, result.getTotal());
    }

    @Test
    void testIncrementDownloadCount() {
        LibResource resource = new LibResource();
        resource.setId(1L);
        resource.setDownloadCount(5L);

        when(resourceMapper.selectById(1L)).thenReturn(resource);
        when(resourceMapper.updateById(any(LibResource.class))).thenReturn(1);

        libraryResourceService.incrementDownloadCount(1L);

        assertEquals(6L, resource.getDownloadCount());
    }
}
