package com.aeisp.template.dto.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 模板版本信息 VO。
 *
 * @author AEISP Team
 */
@Data
public class TplTemplateVersionVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 版本 ID。
     */
    private Long id;

    /**
     * 版本号。
     */
    private String versionNo;

    /**
     * 更新日志。
     */
    private String changelog;

    /**
     * 文件大小（字节）。
     */
    private Long fileSize;

    /**
     * 文件哈希值（SHA-256）。
     */
    private String fileHash;

    /**
     * 创建时间。
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    /**
     * ZIP 文件的资源服务器下载地址。
     */
    private String storageUrl;

    /**
     * 该版本资源的基础 URL，Unity 客户端用于拼接具体资源路径按需加载。
     */
    private String resourceBaseUrl;
}
