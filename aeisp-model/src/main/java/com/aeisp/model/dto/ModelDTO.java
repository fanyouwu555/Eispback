package com.aeisp.model.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 大模型数据传输对象。
 *
 * <p>用于模型新增和编辑时的参数传递。</p>
 *
 * @author AEISP Team
 */
@Data
public class ModelDTO implements Serializable {

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
     * 访问密钥。
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
}
