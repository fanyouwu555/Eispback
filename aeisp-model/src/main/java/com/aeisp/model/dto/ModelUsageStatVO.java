package com.aeisp.model.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 模型用量统计视图对象。
 *
 * <p>用于展示单个模型的调用量、成功率、Token 消耗及分位响应时长。</p>
 *
 * @author AEISP Team
 */
@Data
public class ModelUsageStatVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 模型 ID。
     */
    private Long modelId;

    /**
     * 模型名称。
     */
    private String modelName;

    /**
     * 调用次数。
     */
    private Long callCount;

    /**
     * 成功次数。
     */
    private Long successCount;

    /**
     * 失败次数。
     */
    private Long failCount;

    /**
     * Token 消耗总量。
     */
    private Long tokenTotal;

    /**
     * 平均响应时长（毫秒）。
     */
    private Double avgResponseTime;

    /**
     * P50 响应时长（毫秒）。
     */
    private Double p50;

    /**
     * P95 响应时长（毫秒）。
     */
    private Double p95;

    /**
     * P99 响应时长（毫秒）。
     */
    private Double p99;
}
