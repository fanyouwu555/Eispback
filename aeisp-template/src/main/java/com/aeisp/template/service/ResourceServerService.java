package com.aeisp.template.service;

import java.io.File;
import java.util.List;

/**
 * 资源服务器服务接口。
 *
 * <p>负责将模板资源（ZIP、提取文件等）上传到远程资源服务器（NFS/OSS），
 * 并提供 URL 访问能力。NFS 和 OSS 通过不同的实现类切换。</p>
 *
 * @author AEISP Team
 */
public interface ResourceServerService {

    /**
     * 上传单个文件到资源服务器。
     *
     * @param relativePath 相对路径，如 "1/v1.0.0/template.zip"
     * @param data         文件字节数组
     * @return 完整的可访问 URL
     */
    String uploadFile(String relativePath, byte[] data);

    /**
     * 批量上传解压后的所有文件到资源服务器。
     * 遍历 extractDir 下的所有文件，保持子目录结构。
     *
     * @param templateId 模板 ID
     * @param versionNo  版本号
     * @param extractDir 本地解压目录
     * @return 上传的所有文件相对路径列表
     */
    List<String> uploadExtractedFiles(Long templateId, String versionNo, File extractDir);

    /**
     * 删除资源服务器上的单个文件。
     *
     * @param relativePath 相对路径
     */
    void deleteFile(String relativePath);

    /**
     * 删除资源服务器上整个版本目录。
     *
     * @param templateId 模板 ID
     * @param versionNo  版本号
     */
    void deleteVersionFiles(Long templateId, String versionNo);

    /**
     * 根据相对路径获取完整的可访问 URL。
     *
     * @param relativePath 相对路径
     * @return 完整的 URL
     */
    String getUrl(String relativePath);

    /**
     * 获取资源服务器的基础 URL。
     *
     * @return 基础 URL
     */
    String getBaseUrl();

    /**
     * 判断资源服务器上文件是否存在。
     *
     * @param relativePath 相对路径
     * @return true 如果存在
     */
    boolean fileExists(String relativePath);
}