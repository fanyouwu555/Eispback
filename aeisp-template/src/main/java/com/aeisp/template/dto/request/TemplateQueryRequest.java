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
     * 适用场景。
     */
    private String scenario;

    /**
     * 状态：0-下线，1-上线。
     */
    private Integer status;
}
