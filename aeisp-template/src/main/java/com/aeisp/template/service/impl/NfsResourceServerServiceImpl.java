package com.aeisp.template.service.impl;

import cn.hutool.core.io.FileUtil;
import com.aeisp.common.service.ResourceServerService;
import lombok.extern.slf4j.Slf4j;

import jakarta.annotation.PostConstruct;
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
 *
 * <p>改造后：数据库只存相对路径，{@link #getUrl(String)} 运行时动态拼接完整 URL，
 * 基础地址变更时无需修改历史数据。</p>
 */
@Slf4j
public class NfsResourceServerServiceImpl implements ResourceServerService {

    private final String uploadPath;
    private final String baseAddress;
    private final String urlPrefix;

    public NfsResourceServerServiceImpl(String uploadPath, String baseAddress, String urlPrefix) {
        this.uploadPath = uploadPath;
        this.baseAddress = baseAddress;
        this.urlPrefix = urlPrefix;
    }

    @PostConstruct
    public void init() {
        log.info("NfsResourceServerServiceImpl initialized: uploadPath={}, baseAddress={}, urlPrefix={}",
                uploadPath, baseAddress, urlPrefix);
    }

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
    public String uploadFile(String relativePath, File sourceFile) {
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
        FileUtil.copy(sourceFile.getAbsolutePath(), absolutePath, true);
        log.info("NFS 流式上传文件完成: {} -> {}", sourceFile.getAbsolutePath(), absolutePath);
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
                            uploadFile(relativePath, filePath.toFile());
                            uploadedPaths.add(relativePath);
                        } catch (Exception e) {
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
    public void deleteDirectory(String relativePath) {
        if (!isPathSafe(relativePath)) {
            log.warn("非法目录路径: {}", relativePath);
            throw new SecurityException("非法目录路径");
        }
        String normalizedPath = normalizePath(relativePath);
        String absolutePath = FileUtil.normalize(uploadPath + "/" + normalizedPath);
        if (!isWithinBaseDir(absolutePath)) {
            log.warn("路径超出允许范围: {}", absolutePath);
            throw new SecurityException("非法目录路径");
        }
        File dir = new File(absolutePath);
        if (dir.exists()) {
            FileUtil.del(dir);
            log.debug("NFS 删除目录完成: {}", absolutePath);
        }
    }

    /**
     * 根据相对路径拼接完整的可访问 URL。
     *
     * <p>运行时动态拼接：baseAddress + urlPrefix + relativePath，
     * 基础地址（IP:端口）变化时无需修改数据库中的相对路径。</p>
     *
     * @param relativePath 相对路径，如 "1/v1.0.0/template.zip"
     * @return 完整的可访问 URL
     */
    @Override
    public String getUrl(String relativePath) {
        if (relativePath == null || relativePath.isBlank()) {
            return null;
        }
        if (baseAddress == null || baseAddress.isBlank()) {
            log.warn("baseAddress 未配置，无法生成完整 URL");
            return null;
        }
        if (urlPrefix == null || urlPrefix.isBlank()) {
            log.warn("urlPrefix 未配置，无法生成完整 URL");
            return null;
        }
        String normalized = relativePath.startsWith("/") ? relativePath.substring(1) : relativePath;
        String prefix = urlPrefix.endsWith("/") ? urlPrefix : urlPrefix + "/";
        String address = baseAddress.endsWith("/") ? baseAddress.substring(0, baseAddress.length() - 1) : baseAddress;
        return address + "/" + prefix + normalized;
    }

    @Override
    public String getBaseUrl() {
        if (baseAddress == null || baseAddress.isBlank() || urlPrefix == null || urlPrefix.isBlank()) {
            return null;
        }
        String prefix = urlPrefix.endsWith("/") ? urlPrefix : urlPrefix + "/";
        String address = baseAddress.endsWith("/") ? baseAddress.substring(0, baseAddress.length() - 1) : baseAddress;
        return address + "/" + prefix;
    }

    @Override
    public String getUploadPath() {
        return uploadPath;
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
     * 从完整 URL 中提取相对路径。
     *
     * <p>用于兼容管理员在前端手动输入的完整 URL，保存时自动截断为相对路径。</p>
     *
     * @param fullUrl 完整 URL，如 http://192.168.50.215:4050/EISP/Users/18/42/project.zip
     * @return 相对路径，如 18/42/project.zip；如果已经是相对路径则原样返回
     */
    public String extractRelativePath(String fullUrl) {
        if (fullUrl == null || fullUrl.isBlank()) {
            return fullUrl;
        }
        // 如果已经是相对路径（不以 http 开头），直接返回
        if (!fullUrl.startsWith("http://") && !fullUrl.startsWith("https://")) {
            return fullUrl;
        }
        String base = getBaseUrl();
        if (base != null && fullUrl.startsWith(base)) {
            return fullUrl.substring(base.length());
        }
        // 兜底：尝试按 URL 结构截断
        int lastSlash = fullUrl.indexOf("/", fullUrl.indexOf("://") + 3);
        if (lastSlash > 0) {
            // 跳过 IP:port 后的第一段路径（如 /EISP/Users/）
            String afterHost = fullUrl.substring(lastSlash + 1);
            int prefixEnd = afterHost.indexOf("/");
            if (prefixEnd > 0) {
                String afterPrefix = afterHost.substring(prefixEnd + 1);
                int secondPrefixEnd = afterPrefix.indexOf("/");
                if (secondPrefixEnd > 0) {
                    return afterPrefix.substring(secondPrefixEnd + 1);
                }
            }
        }
        // 无法识别，原样返回（避免数据丢失）
        log.warn("无法从 URL 中提取相对路径，原样返回: {}", fullUrl);
        return fullUrl;
    }

    /**
     * 校验路径是否安全，防止路径遍历攻击。
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
