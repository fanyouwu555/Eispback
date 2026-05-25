package com.aeisp.template.dto.request;

import lombok.Data;

/**
 * 模板分页查询请求。
 *
 * @author AEISP Team
 */
@Data
public class TemplateQueryRequest {

    /**
     * 当前页码，默认 1。
     */
    private Long pageNum = 1L;

    /**
     * 每页大小，默认 10。
     */
    private Long pageSize = 10L;

    /**
     * 模板名称（模糊查询）。
     */
    private String templateName;

    /**
     * 模板编码（精确匹配）。
     */
    private String templateCode;

    /**
     * 适用场景。
     */
    private String scenario;

    /**
     * 状态：0-正常, 1-上架, 2-下架, 3-违规。
     */
    private Integer status;

    /**
     * 顶级分类 ID。
     */
    private Long topCategoryId;

    /**
     * 一级分类 ID。
     */
    private Long firstCategoryId;

    /**
     * 二级分类 ID。
     */
    private Long secondCategoryId;

    /**
     * 是否付费：0-免费，1-付费。
     */
    private Integer isPaid;
}
