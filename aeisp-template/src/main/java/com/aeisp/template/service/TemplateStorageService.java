package com.aeisp.template.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 模板文件存储服务接口。
 *
 * <p>负责模板 ZIP 文件的存储、解压、读取、删除等文件操作。</p>
 *
 * @author AEISP Team
 */
public interface TemplateStorageService {

    /**
     * 存储 ZIP 文件，返回相对路径。
     *
     * @param templateId 模板 ID
     * @param versionNo  版本号
     * @param file       ZIP 文件
     * @return 存储后的相对路径
     */
    String storeZip(Long templateId, String versionNo, MultipartFile file);

    /**
     * 解压 ZIP 到指定目录。
     *
     * @param zipPath ZIP 文件绝对路径
     * @param destDir 目标目录绝对路径
     */
    void extractZip(String zipPath, String destDir);

    /**
     * 列出模板版本的文件结构（树形）。
     *
     * @param templateId 模板 ID
     * @param versionNo  版本号
     * @return 文件相对路径列表
     */
    List<String> listFiles(Long templateId, String versionNo);

    /**
     * 读取指定文件内容。
     *
     * @param templateId 模板 ID
     * @param versionNo  版本号
     * @param filePath   文件相对路径（在版本目录内）
     * @return 文件字节数组
     */
    byte[] readFile(Long templateId, String versionNo, String filePath);

    /**
     * 删除版本文件（包括 ZIP 和解压后的文件）。
     *
     * @param templateId 模板 ID
     * @param versionNo  版本号
     */
    void deleteVersionFiles(Long templateId, String versionNo);

    /**
     * 删除模板下的所有版本文件。
     *
     * @param templateId 模板 ID
     */
    void deleteTemplateFiles(Long templateId);

    /**
     * 获取 ZIP 文件的绝对路径。
     *
     * @param templateId 模板 ID
     * @param versionNo  版本号
     * @return ZIP 绝对路径
     */
    String getZipAbsolutePath(Long templateId, String versionNo);
}
