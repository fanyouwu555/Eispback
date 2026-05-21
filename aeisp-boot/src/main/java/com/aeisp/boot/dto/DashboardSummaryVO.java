package com.aeisp.boot.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 仪表盘汇总数据 VO。
 *
 * <p>聚合用户、资产、项目、模板、AI 五大类指标，支持按时间范围切换。</p>
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
        /** 总注册用户数 */
        private Long totalUsers;
        /** 新增用户（按所选时间范围） */
        private Long newUsers;
        /** 活跃用户数（按所选时间范围） */
        private Long activeUsers;
        /** 封禁用户数 */
        private Long bannedCount;
    }

    @Data
    public static class AssetStats implements Serializable {
        private static final long serialVersionUID = 1L;
        /** 平台总充值金额（分） */
        private Long totalRechargeBalance;
        /** 充值金额（分，按所选时间范围） */
        private Long rechargeAmount;
        /** 待结算金额（分） */
        private Long pendingSettlement;
        /** 用户平均余额（分） */
        private Long avgBalance;
    }

    @Data
    public static class ProjectStats implements Serializable {
        private static final long serialVersionUID = 1L;
        /** 平台全部项目总数 */
        private Long totalProjects;
        /** 新建项目数（按所选时间范围） */
        private Long newProjects;
        /** 星标热门项目数 */
        private Long benchmarkProjects;
    }

    @Data
    public static class TemplateStats implements Serializable {
        private static final long serialVersionUID = 1L;
        /** 总模板数量 */
        private Long totalTemplates;
        /** 模板上架数量 */
        private Long onlineCount;
        /** 今日新增模板 */
        private Long todayNewTemplates;
        /** 热门模板数 */
        private Integer hotTemplateCount;
    }

    @Data
    public static class AiStats implements Serializable {
        private static final long serialVersionUID = 1L;
        /** AI对话次数（按所选时间范围） */
        private Long callCount;
        /** 累计会话量 */
        private Long totalSessions;
        /** AI接口调用成功率 */
        private String successRate;
    }
}