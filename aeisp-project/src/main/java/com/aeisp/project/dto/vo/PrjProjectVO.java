package com.aeisp.project.dto.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class PrjProjectVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String projectName;
    private String description;
    private Long userId;
    private String username;
    private Long templateId;
    private String templateName;
    private Integer status;
    private String statusLabel;
    private Integer isPinned;
    private Long runTimeSeconds;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime archivedAt;
}