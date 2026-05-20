package com.aeisp.boot.service;

import com.aeisp.boot.dto.DashboardSummaryVO;

import java.util.Map;

/**
 * 仪表盘服务接口。
 *
 * @author AEISP Team
 */
public interface DashboardService {

    /**
     * 获取仪表盘汇总数据（5 大类指标）。
     */
    DashboardSummaryVO getSummary();

    /**
     * 获取趋势数据。
     *
     * @param category 类别：user/recharge/project/ai
     * @param days     天数
     */
    Map<String, Object> getTrends(String category, int days);
}