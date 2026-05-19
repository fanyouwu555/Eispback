package com.aeisp.message.enums;

import lombok.Getter;

/**
 * 通知优先级枚举。
 *
 * <p>定义消息通知的优先级等级，用于前端展示不同的样式和排序权重。</p>
 *
 * @author AEISP Team
 */
@Getter
public enum NotificationPriorityEnum {

    /**
     * 低优先级。
     */
    LOW(1, "低"),

    /**
     * 中优先级。
     */
    MEDIUM(2, "中"),

    /**
     * 高优先级。
     */
    HIGH(3, "高"),

    /**
     * 紧急优先级。
     */
    URGENT(4, "紧急");

    /**
     * 枚举值。
     */
    private final Integer value;

    /**
     * 描述文本。
     */
    private final String label;

    NotificationPriorityEnum(Integer value, String label) {
        this.value = value;
        this.label = label;
    }

    /**
     * 根据数值获取枚举实例。
     *
     * @param value 枚举值
     * @return 对应的枚举实例，不存在则返回 null
     */
    public static NotificationPriorityEnum fromValue(Integer value) {
        if (value == null) {
            return null;
        }
        for (NotificationPriorityEnum e : values()) {
            if (e.value.equals(value)) {
                return e;
            }
        }
        return null;
    }
}
