package com.aeisp.project.dto.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class ClientProjectVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long projectId;
    private String projectName;
    private Long templateId;
    private String resourceUrl;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}