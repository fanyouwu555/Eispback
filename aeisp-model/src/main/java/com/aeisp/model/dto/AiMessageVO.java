package com.aeisp.model.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * AI 消息视图对象。
 */
@Data
public class AiMessageVO {
    private Long id;
    private Long sessionId;
    private String role;
    private String content;
    private Integer tokensUsed;
    private Long modelId;
    private LocalDateTime createdAt;
}