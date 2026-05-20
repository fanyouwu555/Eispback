package com.aeisp.boot.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 仪表盘汇总数据 VO。
 *
 * <p>聚合用户、资产、项目、模板、AI 五大类指标。</p>
 *
 * @author AEISP Team
 */
@Data
public class DashboardSummaryVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private UserStats user;
    private AssetStats asset;
    private ProjectStats project;
    private TemplateStats template;
    private AiStats ai;

    @Data
    public static class UserStats implements Serializable {
        private static final long serialVersionUID = 1L;
        private Long totalUsers;
        private Long newUsersToday;
        private Long newUsersWeek;
        private Long newUsersMonth;
        private Long mau;
        private Long totalActiveUsers;
    }

    @Data
    public static class AssetStats implements Serializable {
        private static final long serialVersionUID = 1L;
        private Long totalRechargeBalance;
        private Long dailyRechargeAmount;
        private Long totalConsumedAmount;
        private Long avgBalance;
        private Long totalBalance;
    }

    @Data
    public static class ProjectStats implements Serializable {
        private static final long serialVersionUID = 1L;
        private Long totalProjects;
        private Long dailyNewProjects;
        private Long monthlyNewProjects;
        private Long benchmarkProjects;
    }

    @Data
    public static class TemplateStats implements Serializable {
        private static final long serialVersionUID = 1L;
        private Long totalTemplates;
        private Long todayNewTemplates;
        private Long onlineCount;
        private List<Map<String, Object>> hotTemplates;
    }

    @Data
    public static class AiStats implements Serializable {
        private static final long serialVersionUID = 1L;
        private Long totalCalls;
        private Long totalSessions;
        private String successRate;
    }
}