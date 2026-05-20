package com.aeisp.message.enums;

import lombok.Getter;

/**
 * 通知状态枚举。
 *
 * <p>定义消息通知的生命周期状态。</p>
 *
 * @author AEISP Team
 */
@Getter
public enum NotificationStatusEnum {

    /**
     * 草稿。
     */
    DRAFT(1, "草稿"),

    /**
     * 已发送。
     */
    SENT(2, "已发送"),

    /**
     * 已撤回。
     */
    REVOKED(3, "已撤回"),

    /**
     * 定时发送。
     */
    SCHEDULED(4, "定时发送"),

    /**
     * 已归档。
     */
    ARCHIVED(5, "已归档");

    /**
     * 枚举值。
     */
    private final Integer value;

    /**
     * 描述文本。
     */
    private final String label;

    NotificationStatusEnum(Integer value, String label) {
        this.value = value;
        this.label = label;
    }

    /**
     * 根据数值获取枚举实例。
     *
     * @param value 枚举值
     * @return 对应的枚举实例，不存在则返回 null
     */
    public static NotificationStatusEnum fromValue(Integer value) {
        if (value == null) {
            return null;
        }
        for (NotificationStatusEnum e : values()) {
            if (e.value.equals(value)) {
                return e;
            }
        }
        return null;
    }
}
