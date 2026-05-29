package com.aeisp.library.dto.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LibResourceVO {

    private Long id;
    private String resourceCode;
    private String resourceName;
    private String description;
    private Integer status;
    private String statusLabel;
    private String currentVersionNo;
    private Long fileSize;
    private String fileSizeLabel;
    private Long downloadCount;
    private LocalDateTime createdAt;
}
