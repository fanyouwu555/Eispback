package com.aeisp.recharge.entity;

import com.aeisp.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 时长套餐实体。
 *
 * <p>对应数据库表 {@code duration_package}，存储前端展示的充值套餐信息。</p>
 *
 * @author AEISP Team
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("duration_package")
public class DurationPackage extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 套餐名称。
     */
    private String packageName;

    /**
     * 价格（分）。
     */
    private Integer priceCents;

    /**
     * 对应时长（分钟）。
     */
    private Long durationMinutes;

    /**
     * 有效期（天）。
     */
    private Integer validDays;

    /**
     * 优惠活动描述。
     */
    private String promotion;

    /**
     * 状态：1-上架，2-下架。
     */
    private Integer status;

    /**
     * 排序值。
     */
    private Integer sortOrder;
}
