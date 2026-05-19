package com.aeisp.model.entity;

import com.aeisp.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * AI 大模型配置实体。
 *
 * <p>对应数据库表 {@code ai_model}，存储第三方或自部署 AI 模型的接入配置。</p>
 *
 * @author AEISP Team
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ai_model")
public class AiModel extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 模型名称。
     */
    private String modelName;

    /**
     * 模型类型：general-通用, code_opt-代码优化, bio_adapt-仿生适配。
     */
    private String modelType;

    /**
     * 接口地址。
     */
    private String apiEndpoint;

    /**
     * 访问密钥（AES-256 加密存储）。
     */
    private String apiKey;

    /**
     * 调用权重。
     */
    private Integer weight;

    /**
     * 最大 QPS 限制。
     */
    private Integer maxQps;

    /**
     * 适配场景标签（JSON 数组）。
     */
    private String scenarioTags;

    /**
     * 状态：0-禁用，1-启用。
     */
    private Integer status;

    /**
     * 优先级排序。
     */
    private Integer sortOrder;

    /**
     * 维护时间窗口。
     */
    private String maintainWindow;

    /**
     * 默认参数（JSON）。
     */
    private String defaultParams;

    /**
     * 累计调用次数。
     */
    private Long usageCount;

    /**
     * 失败率（%）。
     */
    private Integer failureRate;
}
