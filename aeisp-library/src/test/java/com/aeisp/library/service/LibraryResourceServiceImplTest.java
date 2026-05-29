package com.aeisp.library.service;

import com.aeisp.common.exception.BizException;
import com.aeisp.library.code.LibraryErrorCode;
import com.aeisp.library.entity.LibResource;
import com.aeisp.library.mapper.LibResourceMapper;
import com.aeisp.library.mapper.LibResourceVersionMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class LibraryResourceServiceImplTest {

    @Autowired
    private LibResourceMapper resourceMapper;

    @Autowired
    private LibResourceVersionMapper versionMapper;

    @Test
    void testResourceCodeGeneration() {
        LibResource resource = new LibResource();
        resource.setResourceCode("LIB202605290001");
        resource.setResourceName("测试库资源");
        resource.setStatus(0);
        resourceMapper.insert(resource);
        assertNotNull(resource.getId());

        String maxCode = resourceMapper.selectMaxResourceCode("LIB20260529");
        assertEquals("LIB202605290001", maxCode);
    }

    @Test
    void testLibraryNotFound() {
        assertThrows(BizException.class, () -> {
            throw new BizException(LibraryErrorCode.LIBRARY_NOT_FOUND);
        });
    }
}
