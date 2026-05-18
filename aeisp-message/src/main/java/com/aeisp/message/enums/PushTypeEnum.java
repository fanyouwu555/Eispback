package com.aeisp.message.enums;

import lombok.Getter;

/**
 * 推送方式枚举。
 *
 * <p>定义消息通知的触发时机，支持立即推送和定时推送两种模式。</p>
 *
 * @author AEISP Team
 */
@Getter
public enum PushTypeEnum {

    /**
     * 立即推送。
     */
    IMMEDIATE(1, "立即推送"),

    /**
     * 定时推送。
     */
    SCHEDULED(2, "定时推送");

    /**
     * 枚举值。
     */
    private final Integer value;

    /**
     * 描述文本。
     */
    private final String label;

    PushTypeEnum(Integer value, String label) {
        this.value = value;
        this.label = label;
    }

    /**
     * 根据数值获取枚举实例。
     *
     * @param value 枚举值
     * @return 对应的枚举实例，不存在则返回 null
     */
    public static PushTypeEnum fromValue(Integer value) {
        if (value == null) {
            return null;
        }
        for (PushTypeEnum e : values()) {
            if (e.value.equals(value)) {
                return e;
            }
        }
        return null;
    }
}
