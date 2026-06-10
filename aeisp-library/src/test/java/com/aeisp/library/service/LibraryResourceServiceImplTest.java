package com.aeisp.library.service;

import com.aeisp.common.PageResult;
import com.aeisp.common.exception.BizException;
import com.aeisp.library.code.LibraryErrorCode;
import com.aeisp.library.dto.request.CreateLibraryRequest;
import com.aeisp.library.dto.request.LibraryQueryRequest;
import com.aeisp.library.dto.request.UpdateLibraryRequest;
import com.aeisp.library.dto.vo.LibResourceDetailVO;
import com.aeisp.library.dto.vo.LibResourceVO;
import com.aeisp.library.entity.LibResource;
import com.aeisp.library.mapper.LibResourceMapper;
import com.aeisp.library.service.impl.LibraryResourceServiceImpl;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
class LibraryResourceServiceImplTest {

    @Mock
    private LibResourceMapper resourceMapper;

    @InjectMocks
    private LibraryResourceServiceImpl libraryResourceService;

    @Test
    void testCreateLibrary() {
        CreateLibraryRequest request = new CreateLibraryRequest();
        request.setResourceName("numpy-1.26.4");
        request.setDescription("Python scientific computing library");

        when(resourceMapper.insert(any(LibResource.class))).thenAnswer(inv -> {
            LibResource r = inv.getArgument(0);
            r.setId(1L);
            return 1;
        });

        boolean result = libraryResourceService.createLibrary(request);
        assertTrue(result);
    }

    @Test
    void testUpdateLibraryInfo_Success() {
        LibResource resource = new LibResource();
        resource.setId(1L);
        resource.setResourceName("old-name");

        when(resourceMapper.selectById(1L)).thenReturn(resource);
        when(resourceMapper.updateById(any(LibResource.class))).thenReturn(1);

        UpdateLibraryRequest request = new UpdateLibraryRequest();
        request.setResourceName("numpy-1.26.5");
        request.setDescription("updated desc");

        boolean result = libraryResourceService.updateLibraryInfo(1L, request);
        assertTrue(result);
        assertEquals("numpy-1.26.5", resource.getResourceName());
    }

    @Test
    void testUpdateLibraryInfo_NotFound() {
        when(resourceMapper.selectById(1L)).thenReturn(null);

        UpdateLibraryRequest request = new UpdateLibraryRequest();
        request.setResourceName("Test");

        BizException ex = assertThrows(BizException.class,
                () -> libraryResourceService.updateLibraryInfo(1L, request));
        assertEquals(LibraryErrorCode.LIBRARY_NOT_FOUND.getCode(), ex.getCode());
    }

    @Test
    void testDeleteLibrary() {
        LibResource resource = new LibResource();
        resource.setId(1L);

        when(resourceMapper.selectById(1L)).thenReturn(resource);
        when(resourceMapper.deleteById(1L)).thenReturn(1);

        boolean result = libraryResourceService.deleteLibrary(1L);
        assertTrue(result);
        verify(resourceMapper).deleteById(1L);
    }

    @Test
    void testListLibraries() {
        LibraryQueryRequest request = new LibraryQueryRequest();
        request.setResourceName("numpy");

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
    void testGetDetail() {
        LibResource resource = new LibResource();
        resource.setId(1L);
        resource.setResourceName("numpy-1.26.4");
        resource.setDescription("Python scientific computing library");

        when(resourceMapper.selectById(1L)).thenReturn(resource);

        LibResourceDetailVO detail = libraryResourceService.getDetail(1L);
        assertNotNull(detail);
        assertEquals("numpy-1.26.4", detail.getResourceName());
    }

    @Test
    void testListOnlineLibraries() {
        LibResource r1 = new LibResource();
        r1.setId(1L);
        r1.setResourceName("numpy-1.26.4");

        when(resourceMapper.selectList(any())).thenReturn(List.of(r1));

        List<LibResourceVO> result = libraryResourceService.listOnlineLibraries();
        assertEquals(1, result.size());
        assertEquals("numpy-1.26.4", result.get(0).getResourceName());
    }
}
