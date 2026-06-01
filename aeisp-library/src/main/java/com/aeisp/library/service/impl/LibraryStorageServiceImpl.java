package com.aeisp.library.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ZipUtil;
import com.aeisp.common.service.ResourceServerService;
import com.aeisp.library.service.LibraryStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
@Service
public class LibraryStorageServiceImpl implements LibraryStorageService {

    @Value("${library.upload-path:./uploads/library/}")
    private String basePath;

    private final ResourceServerService resourceServerService;

    public LibraryStorageServiceImpl(@Qualifier("libraryResourceServer") ResourceServerService resourceServerService) {
        this.resourceServerService = resourceServerService;
    }

    private String getBasePath() {
        return basePath;
    }

    @Override
    public String storeZip(Long resourceId, String versionNo, MultipartFile file) {
        String relativeDir = resourceId + "/" + versionNo + "/";
        String absoluteDir = FileUtil.normalize(getBasePath() + "/" + relativeDir);
        FileUtil.mkdir(absoluteDir);

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            originalFilename = "library.zip";
        }
        String targetPath = new File(absoluteDir, originalFilename).getAbsolutePath();
        try (InputStream is = file.getInputStream()) {
            FileUtil.writeFromStream(is, targetPath);
        } catch (IOException e) {
            log.error("存储 ZIP 文件失败: resourceId={}, versionNo={}", resourceId, versionNo, e);
            throw new RuntimeException("存储 ZIP 文件失败", e);
        }
        return relativeDir + originalFilename;
    }

    @Override
    public void extractZip(String zipPath, String destDir) {
        File zipFile = new File(zipPath);
        if (!zipFile.exists()) {
            log.warn("ZIP 文件不存在，跳过解压: {}", zipPath);
            return;
        }
        FileUtil.mkdir(destDir);
        ZipUtil.unzip(zipPath, destDir);
        log.info("解压 ZIP 完成: {} -> {}", zipPath, destDir);
    }

    private static boolean isPathSafe(String path) {
        if (path == null || path.isBlank()) {
            return false;
        }
        return !path.contains("..") && !path.contains(":") && !path.startsWith("/");
    }

    private boolean isWithinBaseDir(String resolvedPath) {
        try {
            Path base = Paths.get(FileUtil.normalize(getBasePath())).toAbsolutePath().normalize();
            Path target = Paths.get(resolvedPath).toAbsolutePath().normalize();
            return target.startsWith(base);
        } catch (Exception e) {
            log.warn("路径解析失败: {}", resolvedPath, e);
            return false;
        }
    }

    @Override
    public List<String> listFiles(Long resourceId, String versionNo) {
        if (!isPathSafe(versionNo)) {
            log.warn("非法版本号: {}", versionNo);
            throw new SecurityException("非法版本号");
        }
        String baseDir = resourceServerService.getUploadPath();
        String versionDir = FileUtil.normalize(baseDir + "/" + resourceId + "/" + versionNo);
        File dir = new File(versionDir);
        if (!dir.exists() || !dir.isDirectory()) {
            return new ArrayList<>();
        }

        List<String> result = new ArrayList<>();
        Path base = Paths.get(versionDir);
        try (Stream<Path> walk = Files.walk(base)) {
            walk.filter(Files::isRegularFile)
                    .forEach(path -> result.add(base.relativize(path).toString().replace("\\", "/")));
        } catch (IOException e) {
            log.error("列出文件失败: resourceId={}, versionNo={}", resourceId, versionNo, e);
            throw new RuntimeException("列出文件失败", e);
        }
        return result;
    }

    @Override
    public byte[] readFile(Long resourceId, String versionNo, String filePath) {
        if (!isPathSafe(versionNo) || !isPathSafe(filePath)) {
            log.warn("非法版本号或文件路径: versionNo={}, filePath={}", versionNo, filePath);
            throw new SecurityException("非法文件路径");
        }
        String baseDir = resourceServerService.getUploadPath();
        String absolutePath = FileUtil.normalize(baseDir + "/" + resourceId + "/" + versionNo + "/" + filePath);
        if (!isWithinBaseDir(absolutePath)) {
            log.warn("路径超出允许范围: {}", absolutePath);
            throw new SecurityException("非法文件路径");
        }
        File file = new File(absolutePath);
        if (!file.exists() || !file.isFile()) {
            log.warn("文件不存在: {}", absolutePath);
            return null;
        }
        try {
            return Files.readAllBytes(file.toPath());
        } catch (IOException e) {
            log.error("读取文件失败: {}", absolutePath, e);
            throw new RuntimeException("读取文件失败", e);
        }
    }

    @Override
    public void deleteVersionFiles(Long resourceId, String versionNo) {
        String versionDir = FileUtil.normalize(getBasePath() + "/" + resourceId + "/" + versionNo);
        if (!isWithinBaseDir(versionDir)) {
            log.warn("路径超出允许范围: {}", versionDir);
            throw new SecurityException("非法文件路径");
        }
        File dir = new File(versionDir);
        if (dir.exists()) {
            FileUtil.del(dir);
            log.info("删除版本文件完成: {}", versionDir);
        }
    }

    @Override
    public void deleteResourceFiles(Long resourceId) {
        String resourceDir = FileUtil.normalize(getBasePath() + "/" + resourceId);
        if (!isWithinBaseDir(resourceDir)) {
            log.warn("路径超出允许范围: {}", resourceDir);
            throw new SecurityException("非法文件路径");
        }
        File dir = new File(resourceDir);
        if (dir.exists()) {
            FileUtil.del(dir);
            log.info("删除库资源文件完成: {}", resourceDir);
        }
    }

    @Override
    public String getZipAbsolutePath(Long resourceId, String versionNo) {
        String versionDir = FileUtil.normalize(getBasePath() + "/" + resourceId + "/" + versionNo);
        if (!isWithinBaseDir(versionDir)) {
            log.warn("路径超出允许范围: {}", versionDir);
            throw new SecurityException("非法文件路径");
        }
        File dir = new File(versionDir);
        if (!dir.exists() || !dir.isDirectory()) {
            return null;
        }
        File[] files = dir.listFiles((d, name) -> name.toLowerCase().endsWith(".zip"));
        if (files == null || files.length == 0) {
            return null;
        }
        return files[0].getAbsolutePath();
    }
}
