package com.aeisp.recharge.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 时长套餐数据传输对象。
 *
 * <p>用于套餐新增和编辑时的参数传递。</p>
 *
 * @author AEISP Team
 */
@Data
public class PackageDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 套餐名称。
     */
    private String packageName;

    /**
     * 价格（分）。
     */
    private Integer price;

    /**
     * 对应时长（小时）。
     */
    private Long durationHours;

    /**
     * 有效期（天）。
     */
    private Integer validDays;

    /**
     * 优惠活动描述。
     */
    private String promotion;

    /**
     * 状态：0-下架，1-上架。
     */
    private Integer status;

    /**
     * 排序值。
     */
    private Integer sortOrder;
}
