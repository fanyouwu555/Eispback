package com.aeisp.boot.service.impl;

import com.aeisp.boot.dto.DashboardSummaryVO;
import com.aeisp.boot.service.DashboardService;
import com.aeisp.model.mapper.AiSessionMapper;
import com.aeisp.model.mapper.ModelCallLogMapper;
import com.aeisp.project.mapper.PrjProjectMapper;
import com.aeisp.recharge.mapper.RechargeOrderMapper;
import com.aeisp.recharge.mapper.UsrBalanceChangeLogMapper;
import com.aeisp.template.mapper.TplTemplateMapper;
import com.aeisp.user.entity.UsrUser;
import com.aeisp.user.mapper.UsrLoginLogMapper;
import com.aeisp.user.mapper.UsrUserBalanceMapper;
import com.aeisp.user.mapper.UsrUserMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 仪表盘服务实现。
 *
 * <p>聚合多个模块的统计数据。</p>
 *
 * @author AEISP Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final UsrUserMapper usrUserMapper;
    private final UsrLoginLogMapper usrLoginLogMapper;
    private final UsrUserBalanceMapper usrUserBalanceMapper;
    private final RechargeOrderMapper rechargeOrderMapper;
    private final UsrBalanceChangeLogMapper usrBalanceChangeLogMapper;
    private final ModelCallLogMapper modelCallLogMapper;
    private final AiSessionMapper aiSessionMapper;
    private final PrjProjectMapper prjProjectMapper;
    private final TplTemplateMapper tplTemplateMapper;

    /**
     * 根据 range 参数计算时间边界。
     *
     * @param range 时间范围：today/yesterday/week/month/total
     * @param now   当前时间
     * @return [start, end] 数组，total 模式下均为 null
     */
    private LocalDateTime[] computeTimeRange(String range, LocalDateTime now) {
        return switch (range) {
            case "today" -> new LocalDateTime[]{now.with(LocalTime.MIN), now};
            case "yesterday" -> new LocalDateTime[]{
                    now.minusDays(1).with(LocalTime.MIN),
                    now.minusDays(1).with(LocalTime.MAX)};
            case "week" -> new LocalDateTime[]{
                    now.with(DayOfWeek.MONDAY).with(LocalTime.MIN), now};
            case "month" -> new LocalDateTime[]{
                    now.withDayOfMonth(1).with(LocalTime.MIN), now};
            default -> new LocalDateTime[]{null, null};
        };
    }

    @Override
    public DashboardSummaryVO getSummary(String range) {
        DashboardSummaryVO vo = new DashboardSummaryVO();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime[] timeRange = computeTimeRange(range, now);
        LocalDateTime start = timeRange[0];
        LocalDateTime end = timeRange[1];
        boolean hasRange = start != null;

        // ===== User =====
        DashboardSummaryVO.UserStats userStats = new DashboardSummaryVO.UserStats();
        userStats.setTotalUsers(usrUserMapper.selectCount(Wrappers.lambdaQuery()));
        userStats.setNewUsers(hasRange ? usrUserMapper.selectCount(
                Wrappers.<UsrUser>lambdaQuery()
                        .ge(UsrUser::getRegisterTime, start)
                        .le(UsrUser::getRegisterTime, end)) : usrUserMapper.selectCount(Wrappers.lambdaQuery()));
        userStats.setActiveUsers(hasRange ? usrLoginLogMapper.countActiveUsers(start, end) : usrLoginLogMapper.countAllActiveUsers());
        userStats.setBannedCount(usrUserMapper.selectCount(
                Wrappers.<UsrUser>lambdaQuery().eq(UsrUser::getStatus, 2)));
        vo.setUser(userStats);

        // ===== Asset =====
        DashboardSummaryVO.AssetStats assetStats = new DashboardSummaryVO.AssetStats();
        Long totalRecharge = usrUserBalanceMapper.selectTotalRechargeSum();
        assetStats.setTotalRechargeBalance(totalRecharge != null ? totalRecharge : 0L);
        assetStats.setRechargeAmount(hasRange ? rechargeOrderMapper.selectDailyRechargeSum(start, end) : rechargeOrderMapper.selectTotalRechargeSum());
        Long totalBalance = usrUserBalanceMapper.selectTotalBalanceSum();
        assetStats.setPendingSettlement(totalBalance != null ? totalBalance : 0L);
        Long avgBalance = usrUserBalanceMapper.selectAvgBalance();
        assetStats.setAvgBalance(avgBalance != null ? avgBalance : 0L);
        vo.setAsset(assetStats);

        // ===== Project =====
        DashboardSummaryVO.ProjectStats projectStats = new DashboardSummaryVO.ProjectStats();
        projectStats.setTotalProjects(prjProjectMapper.selectCount(null));
        projectStats.setNewProjects(hasRange ? prjProjectMapper.selectCount(
                Wrappers.<com.aeisp.project.entity.PrjProject>lambdaQuery()
                        .ge(com.aeisp.project.entity.PrjProject::getCreatedAt, start)
                        .le(com.aeisp.project.entity.PrjProject::getCreatedAt, end)) : 0L);
        Long benchmark = prjProjectMapper.countBenchmarkProjects();
        projectStats.setBenchmarkProjects(benchmark != null ? benchmark : 0L);
        vo.setProject(projectStats);

        // ===== Template =====
        DashboardSummaryVO.TemplateStats templateStats = new DashboardSummaryVO.TemplateStats();
        templateStats.setTotalTemplates(tplTemplateMapper.selectCount(null));
        templateStats.setTodayNewTemplates(hasRange ? tplTemplateMapper.selectCount(
                Wrappers.<com.aeisp.template.entity.TplTemplate>lambdaQuery()
                        .ge(com.aeisp.template.entity.TplTemplate::getCreatedAt, start)
                        .le(com.aeisp.template.entity.TplTemplate::getCreatedAt, end)) : tplTemplateMapper.selectCount(null));
        templateStats.setOnlineCount(tplTemplateMapper.selectCount(
                Wrappers.<com.aeisp.template.entity.TplTemplate>lambdaQuery()
                        .eq(com.aeisp.template.entity.TplTemplate::getStatus, 1)));
        List<Map<String, Object>> hotTemplates = tplTemplateMapper.selectTopHotTemplates(10);
        templateStats.setHotTemplateCount(hotTemplates != null ? hotTemplates.size() : 0);
        vo.setTemplate(templateStats);

        // ===== AI =====
        DashboardSummaryVO.AiStats aiStats = new DashboardSummaryVO.AiStats();
        Long totalCalls = modelCallLogMapper.countTotalCalls();
        aiStats.setCallCount(hasRange ? modelCallLogMapper.selectCount(
                Wrappers.<com.aeisp.model.entity.ModelCallLog>lambdaQuery()
                        .ge(com.aeisp.model.entity.ModelCallLog::getCreatedAt, start)
                        .le(com.aeisp.model.entity.ModelCallLog::getCreatedAt, end)) : (totalCalls != null ? totalCalls : 0L));
        Long totalSessions = aiSessionMapper.selectCount(null);
        aiStats.setTotalSessions(totalSessions != null ? totalSessions : 0L);
        Long successCalls = modelCallLogMapper.countSuccessCalls();
        Long effectiveTotal = totalCalls != null ? totalCalls : 0L;
        if (effectiveTotal > 0 && successCalls != null) {
            double rate = successCalls.doubleValue() / effectiveTotal * 100;
            aiStats.setSuccessRate(String.format("%.1f%%", rate));
        } else {
            aiStats.setSuccessRate("0.0%");
        }
        vo.setAi(aiStats);

        return vo;
    }

    @Override
    public Map<String, Object> getTrends(String category, int days) {
        LocalDateTime now = LocalDateTime.now();

        switch (category) {
            case "user":
                return getUserTrend(now, days);
            case "recharge":
                return getRechargeTrend(now, days);
            case "project":
                return getProjectTrend(now, days);
            case "ai":
                return getAiTrend(now, days);
            default:
                return Map.of();
        }
    }

    private Map<String, Object> getUserTrend(LocalDateTime now, int days) {
        List<String> dates = new ArrayList<>();
        List<Long> newUsers = new ArrayList<>();
        List<Long> activeUsers = new ArrayList<>();

        for (int i = days - 1; i >= 0; i--) {
            LocalDateTime dayStart = now.minusDays(i).with(LocalTime.MIN);
            LocalDateTime dayEnd = now.minusDays(i).with(LocalTime.MAX);
            dates.add(dayStart.toLocalDate().toString());

            long newCount = usrUserMapper.selectCount(
                    Wrappers.<UsrUser>lambdaQuery()
                            .ge(UsrUser::getRegisterTime, dayStart)
                            .le(UsrUser::getRegisterTime, dayEnd));
            newUsers.add(newCount);

            Long activeCount = usrLoginLogMapper.countActiveUsers(dayStart, dayEnd);
            activeUsers.add(activeCount != null ? activeCount : 0L);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("dates", dates);
        result.put("newUsers", newUsers);
        result.put("activeUsers", activeUsers);
        return result;
    }

    private Map<String, Object> getRechargeTrend(LocalDateTime now, int days) {

        // Daily recharge amounts
        Map<LocalDate, Long> rechargeMap = new HashMap<>();
        for (int i = 0; i < days; i++) {
            rechargeMap.put(now.minusDays(i).toLocalDate(), 0L);
        }
        // Fill from DB query — iterate per day for simplicity
        for (int i = days - 1; i >= 0; i--) {
            LocalDateTime dayStart = now.minusDays(i).with(LocalTime.MIN);
            LocalDateTime dayEnd = now.minusDays(i).with(LocalTime.MAX);
            Long amount = rechargeOrderMapper.selectDailyRechargeSum(dayStart, dayEnd);
            rechargeMap.put(dayStart.toLocalDate(), amount != null ? amount : 0L);
        }

        // Daily consumption from balance change log
        Map<LocalDate, Long> consumeMap = new HashMap<>();
        for (int i = 0; i < days; i++) {
            consumeMap.put(now.minusDays(i).toLocalDate(), 0L);
        }
        for (int i = days - 1; i >= 0; i--) {
            LocalDateTime dayStart = now.minusDays(i).with(LocalTime.MIN);
            LocalDateTime dayEnd = now.minusDays(i).with(LocalTime.MAX);
            Long amount = usrBalanceChangeLogMapper.selectDailyConsumeSum(dayStart, dayEnd);
            consumeMap.put(dayStart.toLocalDate(), amount != null ? amount : 0L);
        }

        List<String> dates = rechargeMap.keySet().stream()
                .sorted().map(LocalDate::toString).collect(Collectors.toList());
        List<Long> rechargeAmounts = dates.stream().map(d -> rechargeMap.get(LocalDate.parse(d)))
                .collect(Collectors.toList());
        List<Long> consumeAmounts = dates.stream().map(d -> consumeMap.get(LocalDate.parse(d)))
                .collect(Collectors.toList());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("dates", dates);
        result.put("rechargeAmounts", rechargeAmounts);
        result.put("consumeAmounts", consumeAmounts);
        return result;
    }

    private Map<String, Object> getProjectTrend(LocalDateTime now, int days) {
        LocalDateTime start = now.minusDays(days - 1).with(LocalTime.MIN);
        LocalDateTime end = now.with(LocalTime.MAX);

        List<Map<String, Object>> dbStats = prjProjectMapper.selectDailyProjectStats(start, end);
        Map<LocalDate, Long> createdMap = new HashMap<>();
        for (Map<String, Object> row : dbStats) {
            Object dateObj = row.get("date");
            Object countObj = row.get("createdCount");
            if (dateObj != null) {
                createdMap.put(LocalDate.parse(dateObj.toString()),
                        countObj != null ? ((Number) countObj).longValue() : 0L);
            }
        }

        List<String> dates = new ArrayList<>();
        List<Long> created = new ArrayList<>();
        for (int i = days - 1; i >= 0; i--) {
            LocalDate d = now.minusDays(i).toLocalDate();
            dates.add(d.toString());
            created.add(createdMap.getOrDefault(d, 0L));
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("dates", dates);
        result.put("created", created);
        return result;
    }

    private Map<String, Object> getAiTrend(LocalDateTime now, int days) {
        LocalDateTime start = now.minusDays(days - 1).with(LocalTime.MIN);
        LocalDateTime end = now.with(LocalTime.MAX);

        List<Map<String, Object>> dbStats = modelCallLogMapper.selectDailyCallStats(start, end);
        Map<LocalDate, Long> callMap = new HashMap<>();
        Map<LocalDate, Long> tokenMap = new HashMap<>();
        for (Map<String, Object> row : dbStats) {
            Object dateObj = row.get("date");
            Object callObj = row.get("callCount");
            Object tokenObj = row.get("tokenCount");
            if (dateObj != null) {
                LocalDate d = LocalDate.parse(dateObj.toString());
                callMap.put(d, callObj != null ? ((Number) callObj).longValue() : 0L);
                tokenMap.put(d, tokenObj != null ? ((Number) tokenObj).longValue() : 0L);
            }
        }

        List<String> dates = new ArrayList<>();
        List<Long> callCounts = new ArrayList<>();
        List<Long> tokenCounts = new ArrayList<>();
        for (int i = days - 1; i >= 0; i--) {
            LocalDate d = now.minusDays(i).toLocalDate();
            dates.add(d.toString());
            callCounts.add(callMap.getOrDefault(d, 0L));
            tokenCounts.add(tokenMap.getOrDefault(d, 0L));
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("dates", dates);
        result.put("callCounts", callCounts);
        result.put("tokenCounts", tokenCounts);
        return result;
    }
}