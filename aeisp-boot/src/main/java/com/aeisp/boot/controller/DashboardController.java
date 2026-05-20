package com.aeisp.boot.controller;

import com.aeisp.boot.dto.DashboardSummaryVO;
import com.aeisp.boot.service.DashboardService;
import com.aeisp.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 仪表盘 Controller。
 *
 * <p>聚合用户、资产、项目、模板、AI 五大类数据，提供统一仪表盘接口。</p>
 *
 * @author AEISP Team
 */
@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
@Tag(name = "仪表盘", description = "聚合多模块数据的统一仪表盘接口")
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * 获取仪表盘汇总数据。
     */
    @GetMapping("/summary")
    @Operation(summary = "仪表盘汇总", description = "返回用户/资产/项目/模板/AI 五大类 KPI 指标")
    public Result<DashboardSummaryVO> getSummary() {
        return Result.success(dashboardService.getSummary());
    }

    /**
     * 获取趋势数据。
     *
     * @param category 类别：user/recharge/project/ai
     * @param days     最近天数，默认30
     */
    @GetMapping("/trends")
    @Operation(summary = "趋势数据", description = "按类别返回趋势数据，支持 user/recharge/project/ai")
    public Result<Map<String, Object>> getTrends(
            @RequestParam String category,
            @RequestParam(defaultValue = "30") int days) {
        return Result.success(dashboardService.getTrends(category, days));
    }
}