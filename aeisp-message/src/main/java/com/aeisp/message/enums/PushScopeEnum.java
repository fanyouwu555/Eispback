package com.aeisp.message.enums;

import lombok.Getter;

/**
 * 推送范围枚举。
 *
 * <p>定义消息通知的推送目标范围，控制消息下发给哪些用户群体。</p>
 *
 * @author AEISP Team
 */
@Getter
public enum PushScopeEnum {

    /**
     * 全员推送。
     */
    ALL(1, "全员推送"),

    /**
     * 指定用户推送。
     */
    SPECIFIC_USERS(2, "指定用户"),

    /**
     * 指定角色推送。
     */
    SPECIFIC_ROLES(3, "指定角色");

    /**
     * 枚举值。
     */
    private final Integer value;

    /**
     * 描述文本。
     */
    private final String label;

    PushScopeEnum(Integer value, String label) {
        this.value = value;
        this.label = label;
    }

    /**
     * 根据数值获取枚举实例。
     *
     * @param value 枚举值
     * @return 对应的枚举实例，不存在则返回 null
     */
    public static PushScopeEnum fromValue(Integer value) {
        if (value == null) {
            return null;
        }
        for (PushScopeEnum e : values()) {
            if (e.value.equals(value)) {
                return e;
            }
        }
        return null;
    }
}
