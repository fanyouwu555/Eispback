package com.aeisp.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * AI 对话消息实体。
 */
@Data
@TableName("ai_message")
public class AiMessage {

    private Long id;
    private Long sessionId;
    private String role;
    private String content;
    private Integer tokensUsed;
    private Long modelId;
    private LocalDateTime createdAt;
}