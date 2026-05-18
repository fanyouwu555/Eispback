package com.aeisp.recharge.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 时长统计视图对象。
 *
 * <p>用于展示用户时长的汇总统计信息。</p>
 *
 * @author AEISP Team
 */
@Data
public class DurationStatVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户 ID。
     */
    private Long userId;

    /**
     * 总充值时长（分钟）。
     */
    private Long totalRecharged;

    /**
     * 总消耗时长（分钟）。
     */
    private Long totalConsumed;

    /**
     * 剩余时长（分钟）。
     */
    private Long remainingDuration;

    /**
     * 本月消耗时长（分钟）。
     */
    private Long currentMonthConsumed;

    /**
     * 今日消耗时长（分钟）。
     */
    private Long todayConsumed;
}
