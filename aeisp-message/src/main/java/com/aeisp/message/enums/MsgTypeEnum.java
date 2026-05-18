package com.aeisp.message.enums;

import lombok.Getter;

/**
 * 消息类型枚举。
 *
 * <p>定义平台通知消息的业务分类，用于前端展示不同的消息图标和路由跳转。</p>
 *
 * @author AEISP Team
 */
@Getter
public enum MsgTypeEnum {

    /**
     * 系统公告。
     */
    SYSTEM_NOTICE(1, "系统公告"),

    /**
     * 版本更新。
     */
    VERSION_UPDATE(2, "版本更新"),

    /**
     * 模板更新。
     */
    TEMPLATE_UPDATE(3, "模板更新"),

    /**
     * 任务结果通知。
     */
    TASK_RESULT(4, "任务结果通知"),

    /**
     * 时长预警。
     */
    DURATION_WARNING(5, "时长预警"),

    /**
     * 充值通知。
     */
    RECHARGE_NOTICE(6, "充值通知"),

    /**
     * 违规提醒。
     */
    VIOLATION_REMIND(7, "违规提醒");

    /**
     * 枚举值。
     */
    private final Integer value;

    /**
     * 描述文本。
     */
    private final String label;

    MsgTypeEnum(Integer value, String label) {
        this.value = value;
        this.label = label;
    }

    /**
     * 根据数值获取枚举实例。
     *
     * @param value 枚举值
     * @return 对应的枚举实例，不存在则返回 null
     */
    public static MsgTypeEnum fromValue(Integer value) {
        if (value == null) {
            return null;
        }
        for (MsgTypeEnum e : values()) {
            if (e.value.equals(value)) {
                return e;
            }
        }
        return null;
    }
}
