package com.aeisp.user.controller;

import com.aeisp.common.Result;
import com.aeisp.common.security.CustomUserDetails;
import com.aeisp.common.service.ResourceServerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 客户端用户文件 Controller。
 *
 * <p>Unity 客户端专用接口，提供用户 DataBase 目录下的文件上传与下载。
 * 文件存储路径固定为 {@code {userId}/DataBase/{fileName}}，文件名由客户端决定。</p>
 *
 * <p>遵循客户端 API 规范：</p>
 * <ul>
 *   <li>路径前缀 {@code /api/v1/client/*}</li>
 *   <li>不使用 {@code @PreAuthorize}，身份授权而非角色授权</li>
 *   <li>资源隔离：每个用户只能操作自己的 DataBase 目录</li>
 * </ul>
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/client/user-files")
@Tag(name = "客户端用户文件", description = "Unity 客户端用户 DataBase 目录文件上传与下载")
public class ClientUserFileController {

    private static final long MAX_FILE_SIZE = 100 * 1024 * 1024; // 100MB
    private static final String BASE_DIR = "DataBase";

    private final ResourceServerService userResourceServer;

    public ClientUserFileController(@Qualifier("userResourceServer") ResourceServerService userResourceServer) {
        this.userResourceServer = userResourceServer;
    }

    /**
     * 上传文件到用户 DataBase 目录。
     *
     * @param user     当前登录用户
     * @param file     文件内容
     * @param fileName 文件名（由客户端决定，如 savegame.dat）
     * @return 完整的可访问 URL
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "上传用户文件", description = "上传文件到用户 DataBase 目录，文件名由客户端指定")
    public Result<String> upload(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestParam("file") MultipartFile file,
            @RequestParam("fileName") String fileName) {

        if (file.isEmpty()) {
            return Result.error(400, "上传文件不能为空");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            return Result.error(400, "文件大小不能超过 100MB");
        }
        if (!isFileNameValid(fileName)) {
            return Result.error(400, "文件名包含非法字符");
        }

        String relativePath = buildRelativePath(user.getUserId(), fileName);
        try {
            String url = userResourceServer.uploadFile(relativePath, file.getBytes());
            log.info("用户文件上传成功: userId={}, fileName={}, url={}", user.getUserId(), fileName, url);
            return Result.success(url);
        } catch (IOException e) {
            log.error("用户文件上传失败: userId={}, fileName={}", user.getUserId(), fileName, e);
            return Result.error(500, "文件上传失败");
        }
    }

    /**
     * 从用户 DataBase 目录下载文件。
     *
     * @param user     当前登录用户
     * @param fileName 文件名
     * @return 文件字节流
     */
    @GetMapping("/download")
    @Operation(summary = "下载用户文件", description = "从用户 DataBase 目录下载指定文件")
    public ResponseEntity<byte[]> download(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestParam("fileName") String fileName) {

        if (!isFileNameValid(fileName)) {
            return ResponseEntity.badRequest().build();
        }

        String relativePath = buildRelativePath(user.getUserId(), fileName);
        byte[] data = userResourceServer.readFile(relativePath);
        if (data == null) {
            return ResponseEntity.notFound().build();
        }

        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replace("+", "%20");
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + encodedFileName + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(data.length)
                .body(data);
    }

    /**
     * 构建相对路径：{userId}/DataBase/{fileName}。
     */
    private static String buildRelativePath(Long userId, String fileName) {
        return userId + "/" + BASE_DIR + "/" + fileName;
    }

    /**
     * 校验文件名是否安全。
     *
     * <p>禁止路径遍历（..）、绝对路径（:、/ 开头）、反斜杠开头。</p>
     */
    private static boolean isFileNameValid(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return false;
        }
        return !fileName.contains("..")
                && !fileName.contains(":")
                && !fileName.startsWith("/")
                && !fileName.startsWith("\\");
    }
}
