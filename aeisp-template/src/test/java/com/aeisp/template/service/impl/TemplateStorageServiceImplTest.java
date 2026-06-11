package com.aeisp.template.service.impl;

import com.aeisp.common.service.ResourceServerService;
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
    private String mockUploadPath;

    @BeforeEach
    void setUp() throws Exception {
        mockUploadPath = System.getProperty("java.io.tmpdir");
        ResourceServerService mockResourceServerService = new ResourceServerService() {
            @Override public String uploadFile(String relativePath, byte[] data) { return null; }
            @Override public String uploadFile(String relativePath, java.io.File sourceFile) { return null; }
            @Override public java.util.List<String> uploadExtractedFiles(Long templateId, String versionNo, java.io.File extractDir) { return java.util.List.of(); }
            @Override public void deleteFile(String relativePath) { }
            @Override public void deleteVersionFiles(Long templateId, String versionNo) { }
            @Override public String getUrl(String relativePath) { return "http://localhost/" + relativePath; }
            @Override public String getBaseUrl() { return "http://localhost/"; }
            @Override public String getUploadPath() { return mockUploadPath; }
            @Override public boolean fileExists(String relativePath) { return false; }
            @Override public void deleteDirectory(String relativePath) { }
            @Override public byte[] readFile(String relativePath) { return null; }
        };
        storageService = new TemplateStorageServiceImpl(mockResourceServerService);
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
        String basePath = tempDir.toString() + "/";

        // 让 resourceServerService.getUploadPath() 和 basePath 使用相同路径
        mockUploadPath = basePath;

        java.lang.reflect.Field basePathField = TemplateStorageServiceImpl.class.getDeclaredField("basePath");
        basePathField.setAccessible(true);
        basePathField.set(storageService, basePath);

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
