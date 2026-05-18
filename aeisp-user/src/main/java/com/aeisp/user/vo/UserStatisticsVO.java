package com.aeisp.user.vo;

import lombok.Data;

/**
 * 用户统计 VO。
 *
 * <p>封装平台用户核心统计数据，包括总量、新增及活跃指标。
 * 当前为简化实现，后续可接入真实统计 SQL 或缓存。</p>
 *
 * @author AEISP Team
 */
@Data
public class UserStatisticsVO {

    /**
     * 总用户数。
     */
    private Long totalUsers;

    /**
     * 今日新增用户数。
     */
    private Long newUsersToday;

    /**
     * 本周新增用户数。
     */
    private Long newUsersWeek;

    /**
     * 本月新增用户数。
     */
    private Long newUsersMonth;

    /**
     * 日活跃用户（DAU）。
     */
    private Long dau;

    /**
     * 周活跃用户（WAU）。
     */
    private Long wau;

    /**
     * 月活跃用户（MAU）。
     */
    private Long mau;
}
