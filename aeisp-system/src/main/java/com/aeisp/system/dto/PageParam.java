package com.aeisp.system.dto;

import com.aeisp.common.constant.CommonConstants;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.io.Serializable;

/**
 * 分页查询基础参数。
 *
 * <p>所有分页查询请求 DTO 应继承此类以获得统一的分页参数校验能力。</p>
 *
 * @author AEISP Team
 */
@Data
public class PageParam implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 当前页码，默认 1。
     */
    @Min(value = 1, message = "页码必须大于等于 1")
    private Long pageNum = CommonConstants.DEFAULT_PAGE_NUM;

    /**
     * 每页大小，默认 20。
     */
    @Min(value = 1, message = "每页大小必须大于等于 1")
    @Max(value = 500, message = "每页大小不能超过 500")
    private Long pageSize = CommonConstants.DEFAULT_PAGE_SIZE;
}
