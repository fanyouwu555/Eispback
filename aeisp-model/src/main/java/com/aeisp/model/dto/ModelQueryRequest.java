package com.aeisp.model.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 模型分页查询请求。
 *
 * <p>用于模型列表的条件筛选和分页参数传递。</p>
 *
 * @author AEISP Team
 */
@Data
public class ModelQueryRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 模型名称（模糊查询）。
     */
    private String modelName;

    /**
     * 模型类型。
     */
    private String modelType;

    /**
     * 状态：0-禁用，1-启用。
     */
    private Integer status;

    /**
     * 当前页码。
     */
    private Long pageNum = 1L;

    /**
     * 每页大小。
     */
    private Long pageSize = 10L;
}
