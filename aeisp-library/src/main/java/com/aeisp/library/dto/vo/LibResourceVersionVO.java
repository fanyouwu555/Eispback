package com.aeisp.library.dto.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LibResourceVersionVO {

    private Long id;
    private String versionNo;
    private Long fileSize;
    private String fileHash;
    private String changelog;
    private LocalDateTime createdAt;
    private Boolean isCurrent;

    /**
     * 存储 URL（运行时动态拼接的完整下载地址）。
     */
    private String storageUrl;
}
