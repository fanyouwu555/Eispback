package com.aeisp.model.entity;

import com.aeisp.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * AI 对话会话实体。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ai_session")
public class AiSession extends BaseEntity {

    private Long userId;
    private Long modelId;
    private String sessionTitle;
    private Integer status;
    private Integer messageCount;
    private Integer totalTokens;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
}