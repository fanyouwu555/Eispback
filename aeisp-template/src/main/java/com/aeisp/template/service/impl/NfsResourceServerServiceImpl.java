package com.aeisp.template.service.impl;

import cn.hutool.core.io.FileUtil;
import com.aeisp.template.service.ResourceServerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * NFS 资源服务器实现。
 *
 * <p>NFS 挂载到本地路径后，通过本地文件写入 + URL 拼接实现资源上传。
 * 适用于阿里云 OSS 上线前的临时方案。</p>
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "resource-server.type", havingValue = "nfs", matchIfMissing = true)
public class NfsResourceServerServiceImpl implements ResourceServerService {

    @Value("${resource-server.nfs.upload-path}")
    private String uploadPath;

    @Value("${resource-server.nfs.base-url}")
    private String baseUrl;

    @Override
    public String uploadFile(String relativePath, byte[] data) {
        if (!isPathSafe(relativePath)) {
            log.warn("非法文件路径: {}", relativePath);
            throw new SecurityException("非法文件路径");
        }
        String normalizedPath = normalizePath(relativePath);
        String absolutePath = FileUtil.normalize(uploadPath + "/" + normalizedPath);
        if (!isWithinBaseDir(absolutePath)) {
            log.warn("路径超出允许范围: {}", absolutePath);
            throw new SecurityException("非法文件路径");
        }
        // 确保父目录存在
        FileUtil.mkdir(new File(absolutePath).getParent());
        FileUtil.writeBytes(data, absolutePath);
        log.debug("NFS 上传文件完成: {}", absolutePath);
        return getUrl(normalizedPath);
    }

    @Override
    public List<String> uploadExtractedFiles(Long templateId, String versionNo, File extractDir) {
        List<String> uploadedPaths = new ArrayList<>();
        if (extractDir == null || !extractDir.exists() || !extractDir.isDirectory()) {
            log.warn("解压目录不存在，跳过批量上传: {}", extractDir);
            return uploadedPaths;
        }

        if (templateId == null || versionNo == null || versionNo.isBlank()) {
            log.warn("templateId 或 versionNo 为空，跳过批量上传");
            return uploadedPaths;
        }

        Path baseDir = extractDir.toPath().toAbsolutePath().normalize();
        String versionDir = templateId + "/" + versionNo + "/";

        try (Stream<Path> walk = Files.walk(baseDir)) {
            walk.filter(Files::isRegularFile)
                    .forEach(filePath -> {
                        String subPath = baseDir.relativize(filePath).toString().replace("\\", "/");
                        String relativePath = versionDir + subPath;
                        try {
                            byte[] bytes = Files.readAllBytes(filePath);
                            uploadFile(relativePath, bytes);
                            uploadedPaths.add(relativePath);
                        } catch (IOException e) {
                            log.error("上传提取文件失败: {}", relativePath, e);
                        }
                    });
        } catch (IOException e) {
            log.error("遍历解压目录失败: {}", extractDir, e);
        }

        log.info("NFS 批量上传完成: {} 个文件", uploadedPaths.size());
        return uploadedPaths;
    }

    @Override
    public void deleteFile(String relativePath) {
        if (!isPathSafe(relativePath)) {
            log.warn("非法文件路径: {}", relativePath);
            throw new SecurityException("非法文件路径");
        }
        String normalizedPath = normalizePath(relativePath);
        String absolutePath = FileUtil.normalize(uploadPath + "/" + normalizedPath);
        if (!isWithinBaseDir(absolutePath)) {
            log.warn("路径超出允许范围: {}", absolutePath);
            throw new SecurityException("非法文件路径");
        }
        File file = new File(absolutePath);
        if (file.exists()) {
            FileUtil.del(file);
            log.debug("NFS 删除文件完成: {}", absolutePath);
        }
    }

    @Override
    public void deleteVersionFiles(Long templateId, String versionNo) {
        if (templateId == null || versionNo == null || versionNo.isBlank()) {
            log.warn("templateId 或 versionNo 为空，跳过删除版本文件");
            return;
        }
        String versionDir = templateId + "/" + versionNo;
        String absolutePath = FileUtil.normalize(uploadPath + "/" + versionDir);
        if (!isWithinBaseDir(absolutePath)) {
            log.warn("路径超出允许范围: {}", absolutePath);
            throw new SecurityException("非法文件路径");
        }
        File dir = new File(absolutePath);
        if (dir.exists()) {
            FileUtil.del(dir);
            log.info("NFS 删除版本目录完成: {}", absolutePath);
        }
    }

    @Override
    public String getUrl(String relativePath) {
        if (baseUrl == null || baseUrl.isBlank()) {
            log.warn("baseUrl 未配置，无法生成完整 URL");
            return null;
        }
        if (relativePath == null || relativePath.isBlank()) {
            return null;
        }
        String normalized = relativePath.startsWith("/") ? relativePath.substring(1) : relativePath;
        return baseUrl + normalized;
    }

    @Override
    public String getBaseUrl() {
        return baseUrl;
    }

    @Override
    public boolean fileExists(String relativePath) {
        if (!isPathSafe(relativePath)) {
            log.warn("非法文件路径: {}", relativePath);
            throw new SecurityException("非法文件路径");
        }
        String normalizedPath = normalizePath(relativePath);
        String absolutePath = FileUtil.normalize(uploadPath + "/" + normalizedPath);
        if (!isWithinBaseDir(absolutePath)) {
            log.warn("路径超出允许范围: {}", absolutePath);
            throw new SecurityException("非法文件路径");
        }
        return new File(absolutePath).exists();
    }

    /**
     * 校验路径是否安全，防止路径遍历攻击。
     *
     * <p>通过将目标路径解析为绝对路径后，检查其是否仍然位于允许的基目录下，
     * 可有效防御 {@code ../}、绝对路径、空字节注入等多种路径遍历手段。</p>
     *
     * @param path 待校验的路径片段
     * @return true 表示安全
     */
    private static boolean isPathSafe(String path) {
        if (path == null || path.isBlank()) {
            return false;
        }
        return !path.contains("..") && !path.contains(":") && !path.startsWith("/");
    }

    /**
     * 校验最终解析后的文件路径是否位于允许的基目录范围内。
     *
     * @param resolvedPath 解析后的绝对路径
     * @return true 表示在允许范围内
     */
    private boolean isWithinBaseDir(String resolvedPath) {
        try {
            Path base = Paths.get(FileUtil.normalize(uploadPath)).toAbsolutePath().normalize();
            Path target = Paths.get(resolvedPath).toAbsolutePath().normalize();
            return target.startsWith(base);
        } catch (Exception e) {
            log.warn("路径解析失败: {}", resolvedPath, e);
            return false;
        }
    }

    /**
     * 规范化路径，移除前导斜杠。
     */
    private static String normalizePath(String relativePath) {
        if (relativePath == null || relativePath.isBlank()) {
            return "";
        }
        String normalized = relativePath.replace("\\", "/");
        return normalized.startsWith("/") ? normalized.substring(1) : normalized;
    }
}