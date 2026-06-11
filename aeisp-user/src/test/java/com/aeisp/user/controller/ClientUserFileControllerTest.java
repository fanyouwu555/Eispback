package com.aeisp.user.controller;

import com.aeisp.common.Result;
import com.aeisp.common.security.CustomUserDetails;
import com.aeisp.common.service.ResourceServerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * ClientUserFileController 单元测试。
 *
 * <p>验证上传和下载接口的核心逻辑，不依赖 Spring 容器。</p>
 */
@ExtendWith(MockitoExtension.class)
class ClientUserFileControllerTest {

    @Mock
    private ResourceServerService userResourceServer;

    private ClientUserFileController controller;

    private static final Long TEST_USER_ID = 42L;

    private CustomUserDetails testUser;

    @BeforeEach
    void setUp() {
        controller = new ClientUserFileController(userResourceServer);
        testUser = new CustomUserDetails(
                TEST_USER_ID, "unity_user", "pass", "user", true,
                java.util.List.of(), java.util.List.of());
    }

    // ==================== 上传测试 ====================

    @Test
    void testUploadSuccess() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file", "savegame.dat", "application/octet-stream", "game-data".getBytes());

        when(userResourceServer.uploadFile(eq("42/DataBase/savegame.dat"), any(byte[].class)))
                .thenReturn("http://test/EISP/Users/42/DataBase/savegame.dat");

        Result<String> result = controller.upload(testUser, file, "savegame.dat");

        assertEquals(200, result.getCode());
        assertEquals("http://test/EISP/Users/42/DataBase/savegame.dat", result.getData());
        verify(userResourceServer).uploadFile(eq("42/DataBase/savegame.dat"), any(byte[].class));
    }

    @Test
    void testUploadEmptyFile() {
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file", "savegame.dat", "application/octet-stream", new byte[0]);

        Result<String> result = controller.upload(testUser, emptyFile, "savegame.dat");

        assertEquals(400, result.getCode());
        assertEquals("上传文件不能为空", result.getMessage());
        verifyNoInteractions(userResourceServer);
    }

    @Test
    void testUploadOversizedFile() {
        byte[] oversized = new byte[100 * 1024 * 1024 + 1];
        MockMultipartFile bigFile = new MockMultipartFile(
                "file", "savegame.dat", "application/octet-stream", oversized);

        Result<String> result = controller.upload(testUser, bigFile, "savegame.dat");

        assertEquals(400, result.getCode());
        assertEquals("文件大小不能超过 100MB", result.getMessage());
        verifyNoInteractions(userResourceServer);
    }

    @Test
    void testUploadWithIllegalFileName_dotDot() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "savegame.dat", "application/octet-stream", "data".getBytes());

        Result<String> result = controller.upload(testUser, file, "../etc/passwd");

        assertEquals(400, result.getCode());
        assertEquals("文件名包含非法字符", result.getMessage());
        verifyNoInteractions(userResourceServer);
    }

    @Test
    void testUploadWithIllegalFileName_colon() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "savegame.dat", "application/octet-stream", "data".getBytes());

        Result<String> result = controller.upload(testUser, file, "C:\\windows\\system32");

        assertEquals(400, result.getCode());
        verifyNoInteractions(userResourceServer);
    }

    @Test
    void testUploadWithIllegalFileName_leadingSlash() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "savegame.dat", "application/octet-stream", "data".getBytes());

        Result<String> result = controller.upload(testUser, file, "/etc/passwd");

        assertEquals(400, result.getCode());
        verifyNoInteractions(userResourceServer);
    }

    @Test
    void testUploadWithIllegalFileName_leadingBackslash() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "savegame.dat", "application/octet-stream", "data".getBytes());

        Result<String> result = controller.upload(testUser, file, "\\windows\\system32");

        assertEquals(400, result.getCode());
        verifyNoInteractions(userResourceServer);
    }

    @Test
    void testUploadWithIllegalFileName_blank() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "savegame.dat", "application/octet-stream", "data".getBytes());

        Result<String> result = controller.upload(testUser, file, "   ");

        assertEquals(400, result.getCode());
        verifyNoInteractions(userResourceServer);
    }

    @Test
    void testUploadWithSubDirectory() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file", "backup.dat", "application/octet-stream", "backup-data".getBytes());

        when(userResourceServer.uploadFile(eq("42/DataBase/backup/save.dat"), any(byte[].class)))
                .thenReturn("http://test/EISP/Users/42/DataBase/backup/save.dat");

        Result<String> result = controller.upload(testUser, file, "backup/save.dat");

        assertEquals(200, result.getCode());
        verify(userResourceServer).uploadFile(eq("42/DataBase/backup/save.dat"), any(byte[].class));
    }

    // ==================== 下载测试 ====================

    @Test
    void testDownloadSuccess() {
        when(userResourceServer.readFile("42/DataBase/savegame.dat"))
                .thenReturn("game-data".getBytes());

        ResponseEntity<byte[]> response = controller.download(testUser, "savegame.dat");

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("game-data", new String(response.getBody()));
        assertNotNull(response.getHeaders().getFirst("Content-Disposition"));
    }

    @Test
    void testDownloadFileNotFound() {
        when(userResourceServer.readFile("42/DataBase/missing.dat"))
                .thenReturn(null);

        ResponseEntity<byte[]> response = controller.download(testUser, "missing.dat");

        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    void testDownloadWithIllegalFileName() {
        ResponseEntity<byte[]> response = controller.download(testUser, "../../../etc/passwd");

        assertEquals(400, response.getStatusCode().value());
        verifyNoInteractions(userResourceServer);
    }

    @Test
    void testDownloadWithNullFileName() {
        ResponseEntity<byte[]> response = controller.download(testUser, null);

        assertEquals(400, response.getStatusCode().value());
        verifyNoInteractions(userResourceServer);
    }

    @Test
    void testDownloadWithChineseFileName() {
        String chineseName = "存档文件.dat";
        when(userResourceServer.readFile("42/DataBase/" + chineseName))
                .thenReturn("chinese-data".getBytes());

        ResponseEntity<byte[]> response = controller.download(testUser, chineseName);

        assertEquals(200, response.getStatusCode().value());
        String disposition = response.getHeaders().getFirst("Content-Disposition");
        assertNotNull(disposition);
        assertTrue(disposition.contains("attachment"));
    }

    // ==================== 资源隔离测试 ====================

    @Test
    void testUploadPathUsesUserIdFromJwt() throws IOException {
        CustomUserDetails otherUser = new CustomUserDetails(
                99L, "other_user", "pass", "user", true,
                java.util.List.of(), java.util.List.of());

        MockMultipartFile file = new MockMultipartFile(
                "file", "data.bin", "application/octet-stream", "bytes".getBytes());

        when(userResourceServer.uploadFile(eq("99/DataBase/data.bin"), any(byte[].class)))
                .thenReturn("http://test/EISP/Users/99/DataBase/data.bin");

        Result<String> result = controller.upload(otherUser, file, "data.bin");

        assertEquals(200, result.getCode());
        verify(userResourceServer).uploadFile(eq("99/DataBase/data.bin"), any(byte[].class));
        verify(userResourceServer, never()).uploadFile(contains("42"), any(byte[].class));
    }
}
