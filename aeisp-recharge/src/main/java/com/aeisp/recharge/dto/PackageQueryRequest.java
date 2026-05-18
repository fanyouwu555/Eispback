package com.aeisp.recharge.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 套餐分页查询请求。
 *
 * <p>用于套餐列表的条件筛选和分页参数传递。</p>
 *
 * @author AEISP Team
 */
@Data
public class PackageQueryRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 套餐名称（模糊查询）。
     */
    private String packageName;

    /**
     * 状态：0-下架，1-上架。
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
