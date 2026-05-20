package com.aeisp.model.dto;

import lombok.Data;

/**
 * AI 会话分页查询请求。
 */
@Data
public class AiSessionQueryRequest {
    private Long userId;
    private Long modelId;
    private Integer status;
    private String keyword;
    private Long pageNum = 1L;
    private Long pageSize = 10L;
}