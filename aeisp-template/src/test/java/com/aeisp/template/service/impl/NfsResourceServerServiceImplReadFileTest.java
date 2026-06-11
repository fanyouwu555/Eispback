package com.aeisp.template.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * NfsResourceServerServiceImpl readFile 方法单元测试。
 *
 * <p>使用临时目录隔离测试，验证文件读取、路径安全和边界场景。</p>
 */
class NfsResourceServerServiceImplReadFileTest {

    private NfsResourceServerServiceImpl service;

    @BeforeEach
    void setUp(@TempDir Path tempDir) {
        String uploadPath = tempDir.toString();
        service = new NfsResourceServerServiceImpl(
                uploadPath,
                "http://localhost:4050",
                "EISP/Users/");
    }

    @Test
    void testReadFileSuccess(@TempDir Path tempDir) throws IOException {
        // 准备：在临时目录下创建测试文件
        Path filePath = tempDir.resolve("42/DataBase/savegame.dat");
        Files.createDirectories(filePath.getParent());
        Files.writeString(filePath, "game-save-data");

        String uploadPath = tempDir.toString();
        service = new NfsResourceServerServiceImpl(
                uploadPath,
                "http://localhost:4050",
                "EISP/Users/");

        byte[] result = service.readFile("42/DataBase/savegame.dat");

        assertNotNull(result);
        assertEquals("game-save-data", new String(result));
    }

    @Test
    void testReadFileNotExists(@TempDir Path tempDir) {
        String uploadPath = tempDir.toString();
        service = new NfsResourceServerServiceImpl(
                uploadPath,
                "http://localhost:4050",
                "EISP/Users/");

        byte[] result = service.readFile("42/DataBase/nonexistent.dat");

        assertNull(result);
    }

    @Test
    void testReadFileWithPathTraversal(@TempDir Path tempDir) {
        String uploadPath = tempDir.toString();
        service = new NfsResourceServerServiceImpl(
                uploadPath,
                "http://localhost:4050",
                "EISP/Users/");

        assertThrows(SecurityException.class,
                () -> service.readFile("42/DataBase/../../../etc/passwd"));
    }

    @Test
    void testReadFileWithAbsolutePath(@TempDir Path tempDir) {
        String uploadPath = tempDir.toString();
        service = new NfsResourceServerServiceImpl(
                uploadPath,
                "http://localhost:4050",
                "EISP/Users/");

        assertThrows(SecurityException.class,
                () -> service.readFile("C:/windows/system32/notepad.exe"));
    }

    @Test
    void testReadFileWithLeadingSlash(@TempDir Path tempDir) {
        String uploadPath = tempDir.toString();
        service = new NfsResourceServerServiceImpl(
                uploadPath,
                "http://localhost:4050",
                "EISP/Users/");

        assertThrows(SecurityException.class,
                () -> service.readFile("/etc/passwd"));
    }

    @Test
    void testReadFileWithNullPath(@TempDir Path tempDir) {
        String uploadPath = tempDir.toString();
        service = new NfsResourceServerServiceImpl(
                uploadPath,
                "http://localhost:4050",
                "EISP/Users/");

        assertThrows(SecurityException.class,
                () -> service.readFile(null));
    }

    @Test
    void testReadFileWithBlankPath(@TempDir Path tempDir) {
        String uploadPath = tempDir.toString();
        service = new NfsResourceServerServiceImpl(
                uploadPath,
                "http://localhost:4050",
                "EISP/Users/");

        assertThrows(SecurityException.class,
                () -> service.readFile("  "));
    }

    @Test
    void testReadFileDirectoryEscape(@TempDir Path tempDir) throws IOException {
        // 在 uploadPath 同级目录创建一个文件，尝试通过 ../ 读取
        Path siblingFile = tempDir.resolveSibling("secret.txt");
        Files.writeString(siblingFile, "secret");

        String uploadPath = tempDir.toString();
        service = new NfsResourceServerServiceImpl(
                uploadPath,
                "http://localhost:4050",
                "EISP/Users/");

        // 尝试通过 ../ 逃出 uploadPath 目录
        String relativePath = "42/DataBase/../../secret.txt";
        assertThrows(SecurityException.class,
                () -> service.readFile(relativePath));

        // 清理
        Files.deleteIfExists(siblingFile);
    }

    @Test
    void testReadFileBinaryContent(@TempDir Path tempDir) throws IOException {
        Path filePath = tempDir.resolve("42/DataBase/binary.bin");
        Files.createDirectories(filePath.getParent());
        byte[] binaryData = new byte[]{0x00, 0x01, 0x02, (byte) 0xFF, (byte) 0xFE};
        Files.write(filePath, binaryData);

        String uploadPath = tempDir.toString();
        service = new NfsResourceServerServiceImpl(
                uploadPath,
                "http://localhost:4050",
                "EISP/Users/");

        byte[] result = service.readFile("42/DataBase/binary.bin");

        assertNotNull(result);
        assertArrayEquals(binaryData, result);
    }

    @Test
    void testReadFileEmptyFile(@TempDir Path tempDir) throws IOException {
        Path filePath = tempDir.resolve("42/DataBase/empty.dat");
        Files.createDirectories(filePath.getParent());
        Files.createFile(filePath);

        String uploadPath = tempDir.toString();
        service = new NfsResourceServerServiceImpl(
                uploadPath,
                "http://localhost:4050",
                "EISP/Users/");

        byte[] result = service.readFile("42/DataBase/empty.dat");

        assertNotNull(result);
        assertEquals(0, result.length);
    }

    @Test
    void testReadFileWithSubDirectory(@TempDir Path tempDir) throws IOException {
        Path filePath = tempDir.resolve("42/DataBase/backup/save.dat");
        Files.createDirectories(filePath.getParent());
        Files.writeString(filePath, "nested-data");

        String uploadPath = tempDir.toString();
        service = new NfsResourceServerServiceImpl(
                uploadPath,
                "http://localhost:4050",
                "EISP/Users/");

        byte[] result = service.readFile("42/DataBase/backup/save.dat");

        assertNotNull(result);
        assertEquals("nested-data", new String(result));
    }

    @Test
    void testReadFileWhenPathIsDirectory(@TempDir Path tempDir) {
        Path dirPath = tempDir.resolve("42/DataBase");
        File dir = dirPath.toFile();
        dir.mkdirs();

        String uploadPath = tempDir.toString();
        service = new NfsResourceServerServiceImpl(
                uploadPath,
                "http://localhost:4050",
                "EISP/Users/");

        // 尝试读取目录，应该返回 null（因为不是文件）
        byte[] result = service.readFile("42/DataBase");
        assertNull(result);
    }
}
