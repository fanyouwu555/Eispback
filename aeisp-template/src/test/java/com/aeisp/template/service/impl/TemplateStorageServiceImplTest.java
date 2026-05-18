package com.aeisp.template.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Method;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TemplateStorageServiceImpl 单元测试。
 */
class TemplateStorageServiceImplTest {

    private TemplateStorageServiceImpl storageService;

    @BeforeEach
    void setUp() throws Exception {
        storageService = new TemplateStorageServiceImpl();
    }

    @Test
    void testIsPathSafeWithValidPath() throws Exception {
        Method method = TemplateStorageServiceImpl.class.getDeclaredMethod("isPathSafe", String.class);
        method.setAccessible(true);

        assertTrue((Boolean) method.invoke(storageService, "v1.0.0"));
        assertTrue((Boolean) method.invoke(storageService, "path/to/file.txt"));
        assertTrue((Boolean) method.invoke(storageService, "file-name_123"));
    }

    @Test
    void testIsPathSafeWithPathTraversal() throws Exception {
        Method method = TemplateStorageServiceImpl.class.getDeclaredMethod("isPathSafe", String.class);
        method.setAccessible(true);

        assertFalse((Boolean) method.invoke(storageService, "../../../etc/passwd"));
        assertFalse((Boolean) method.invoke(storageService, "..\\windows\\system32"));
        assertFalse((Boolean) method.invoke(storageService, "/absolute/path"));
        assertFalse((Boolean) method.invoke(storageService, "C:\\windows"));
    }

    @Test
    void testIsPathSafeWithNullOrBlank() throws Exception {
        Method method = TemplateStorageServiceImpl.class.getDeclaredMethod("isPathSafe", String.class);
        method.setAccessible(true);

        assertFalse((Boolean) method.invoke(storageService, (String) null));
        assertFalse((Boolean) method.invoke(storageService, ""));
        assertFalse((Boolean) method.invoke(storageService, "   "));
    }

    @Test
    void testStoreAndReadFile(@TempDir Path tempDir) throws Exception {
        java.lang.reflect.Field basePathField = TemplateStorageServiceImpl.class.getDeclaredField("basePath");
        basePathField.setAccessible(true);
        basePathField.set(storageService, tempDir.toString() + "/");

        MultipartFile file = new MockMultipartFile(
                "file", "template.zip", "application/zip", "fake-zip-content".getBytes());

        String storedPath = storageService.storeZip(1L, "v1.0.0", file);
        assertNotNull(storedPath);

        byte[] content = storageService.readFile(1L, "v1.0.0", "template.zip");
        assertNotNull(content);
        assertEquals("fake-zip-content", new String(content));
    }

    @Test
    void testReadFileWithIllegalPath() {
        assertThrows(SecurityException.class, () -> storageService.readFile(1L, "v1.0.0", "../../../etc/passwd"));
        assertThrows(SecurityException.class, () -> storageService.readFile(1L, "../../../etc", "passwd"));
    }
}
