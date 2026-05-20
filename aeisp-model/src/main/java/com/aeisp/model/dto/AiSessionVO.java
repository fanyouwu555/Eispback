package com.aeisp.model.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * AI 会话视图对象。
 */
@Data
public class AiSessionVO {
    private Long id;
    private Long userId;
    private Long modelId;
    private String modelName;
    private String sessionTitle;
    private Integer status;
    private String statusLabel;
    private Integer messageCount;
    private Integer totalTokens;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    private LocalDateTime createdAt;
}