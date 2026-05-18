package com.aeisp.template.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ZipUtil;
import com.aeisp.template.service.TemplateStorageService;
import lombok.extern.slf4j.Slf4j;
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

/**
 * 模板文件存储服务实现类。
 *
 * <p>基于本地磁盘实现模板 ZIP 文件的存储、解压、读取与删除。
 * 文件目录结构：{@code {basePath}/{templateId}/{versionNo}/}</p>
 *
 * @author AEISP Team
 */
@Slf4j
@Service
public class TemplateStorageServiceImpl implements TemplateStorageService {

    @Value("${template.upload-path:./uploads/templates/}")
    private String basePath;

    @Override
    public String storeZip(Long templateId, String versionNo, MultipartFile file) {
        String relativeDir = templateId + "/" + versionNo + "/";
        String absoluteDir = FileUtil.normalize(basePath + "/" + relativeDir);
        FileUtil.mkdir(absoluteDir);

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            originalFilename = "template.zip";
        }
        String targetPath = absoluteDir + originalFilename;

        try (InputStream is = file.getInputStream()) {
            FileUtil.writeFromStream(is, targetPath);
        } catch (IOException e) {
            log.error("存储 ZIP 文件失败: templateId={}, versionNo={}", templateId, versionNo, e);
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

    /**
     * 校验路径是否安全，防止路径遍历攻击。
     *
     * <p>通过将目标路径解析为绝对路径后，检查其是否仍然位于允许的基目录下，
     * 可有效防御 {@code ../}、绝对路径、空字节注入等多种路径遍历手段。</p>
     *
     * @param path 待校验的路径片段
     * @return true 表示安全
     */
    private boolean isPathSafe(String path) {
        if (path == null || path.isBlank()) {
            return false;
        }
        // 禁止包含 .. 或绝对路径
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
            Path base = Paths.get(FileUtil.normalize(basePath)).toAbsolutePath().normalize();
            Path target = Paths.get(resolvedPath).toAbsolutePath().normalize();
            return target.startsWith(base);
        } catch (Exception e) {
            log.warn("路径解析失败: {}", resolvedPath, e);
            return false;
        }
    }

    @Override
    public List<String> listFiles(Long templateId, String versionNo) {
        if (!isPathSafe(versionNo)) {
            log.warn("非法版本号: {}", versionNo);
            throw new SecurityException("非法版本号");
        }
        String versionDir = FileUtil.normalize(basePath + "/" + templateId + "/" + versionNo);
        if (!isWithinBaseDir(versionDir)) {
            log.warn("路径超出允许范围: {}", versionDir);
            throw new SecurityException("非法文件路径");
        }
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
            log.error("列出文件失败: templateId={}, versionNo={}", templateId, versionNo, e);
            throw new RuntimeException("列出文件失败", e);
        }
        return result;
    }

    @Override
    public byte[] readFile(Long templateId, String versionNo, String filePath) {
        if (!isPathSafe(versionNo) || !isPathSafe(filePath)) {
            log.warn("非法版本号或文件路径: versionNo={}, filePath={}", versionNo, filePath);
            throw new SecurityException("非法文件路径");
        }
        String absolutePath = FileUtil.normalize(basePath + "/" + templateId + "/" + versionNo + "/" + filePath);
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
    public void deleteVersionFiles(Long templateId, String versionNo) {
        String versionDir = FileUtil.normalize(basePath + "/" + templateId + "/" + versionNo);
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
    public void deleteTemplateFiles(Long templateId) {
        String templateDir = FileUtil.normalize(basePath + "/" + templateId);
        if (!isWithinBaseDir(templateDir)) {
            log.warn("路径超出允许范围: {}", templateDir);
            throw new SecurityException("非法文件路径");
        }
        File dir = new File(templateDir);
        if (dir.exists()) {
            FileUtil.del(dir);
            log.info("删除模板文件完成: {}", templateDir);
        }
    }

    @Override
    public String getZipAbsolutePath(Long templateId, String versionNo) {
        String versionDir = FileUtil.normalize(basePath + "/" + templateId + "/" + versionNo);
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
