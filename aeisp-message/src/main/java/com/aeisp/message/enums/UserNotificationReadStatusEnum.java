package com.aeisp.message.enums;

import lombok.Getter;

/**
 * 用户通知阅读状态枚举。
 *
 * <p>定义用户对单条通知的阅读状态。</p>
 *
 * @author AEISP Team
 */
@Getter
public enum UserNotificationReadStatusEnum {

    /**
     * 未读。
     */
    UNREAD(1, "未读"),

    /**
     * 已读。
     */
    READ(2, "已读"),

    /**
     * 已撤回。
     */
    REVOKED(3, "已撤回");

    /**
     * 枚举值。
     */
    private final Integer value;

    /**
     * 描述文本。
     */
    private final String label;

    UserNotificationReadStatusEnum(Integer value, String label) {
        this.value = value;
        this.label = label;
    }

    /**
     * 根据数值获取枚举实例。
     *
     * @param value 枚举值
     * @return 对应的枚举实例，不存在则返回 null
     */
    public static UserNotificationReadStatusEnum fromValue(Integer value) {
        if (value == null) {
            return null;
        }
        for (UserNotificationReadStatusEnum e : values()) {
            if (e.value.equals(value)) {
                return e;
            }
        }
        return null;
    }
}
