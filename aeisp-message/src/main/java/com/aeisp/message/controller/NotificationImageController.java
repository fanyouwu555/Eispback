package com.aeisp.message.controller;

import com.aeisp.common.Result;
import com.aeisp.system.service.SysConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * 公告图片上传 Controller。
 *
 * <p>上传图片到通知专用目录，返回可访问的 URL。
 * 上传路径优先从 sys_config (storage.upload) 读取，未配置时回退到 application.yml 默认值。</p>
 */
@Slf4j
@Tag(name = "公告图片上传", description = "系统公告图片上传接口")
@RestController
@RequestMapping("/api/v1/messages")
@RequiredArgsConstructor
public class NotificationImageController {

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final Set<String> ALLOWED_EXTENSIONS = new HashSet<>();

    static {
        ALLOWED_EXTENSIONS.add(".jpg");
        ALLOWED_EXTENSIONS.add(".jpeg");
        ALLOWED_EXTENSIONS.add(".png");
        ALLOWED_EXTENSIONS.add(".gif");
        ALLOWED_EXTENSIONS.add(".webp");
    }

    private final SysConfigService sysConfigService;

    @Value("${resource-server.notification.upload-path}")
    private String defaultUploadPath;

    @Value("${resource-server.notification.base-url}")
    private String baseUrl;

    private String getUploadPath() {
        String path = sysConfigService.getConfigValue("storage.upload", "all");
        return StringUtils.hasText(path) ? path : defaultUploadPath;
    }

    @PreAuthorize("hasAuthority('notification:create')")
    @Operation(summary = "上传公告图片", description = "上传图片到公告目录，返回图片 URL")
    @PostMapping(value = "/upload-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<String> uploadImage(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error(400, "上传文件不能为空");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            return Result.error(400, "文件大小不能超过 5MB");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            return Result.error(400, "文件名不能为空");
        }

        String ext = "";
        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex > 0) {
            ext = originalFilename.substring(dotIndex).toLowerCase();
        }
        if (!ALLOWED_EXTENSIONS.contains(ext)) {
            return Result.error(400, "仅支持 jpg/png/gif/webp 格式图片");
        }

        String newFilename = UUID.randomUUID().toString() + ext;
        String uploadPath = getUploadPath();
        try {
            File dir = new File(uploadPath);
            if (!dir.exists()) {
                Files.createDirectories(dir.toPath());
            }
            Path targetPath = Paths.get(uploadPath, newFilename);
            Files.copy(file.getInputStream(), targetPath);
            String url = baseUrl + newFilename;
            log.info("公告图片上传成功: {}", url);
            return Result.success(url);
        } catch (IOException e) {
            log.error("公告图片上传失败", e);
            return Result.error(500, "图片上传失败");
        }
    }
}