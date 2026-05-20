package com.aeisp.project.dto.request;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ProjectQueryRequest {
    private Long pageNum = 1L;
    private Long pageSize = 10L;
    private String keyword;
    private Long userId;
    private Integer status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}