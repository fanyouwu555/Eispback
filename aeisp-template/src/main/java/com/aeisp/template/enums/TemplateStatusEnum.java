package com.aeisp.template.enums;

import lombok.Getter;

/**
 * 模板状态枚举。
 *
 * @author AEISP Team
 */
@Getter
public enum TemplateStatusEnum {

    /**
     * 正常（草稿/待上架）。
     */
    NORMAL(0, "正常"),

    /**
     * 上架状态。
     */
    ACTIVE(1, "上架"),

    /**
     * 下架状态。
     */
    OFFLINE(2, "下架"),

    /**
     * 违规下架状态。
     */
    VIOLATION(3, "违规");

    /**
     * 状态码。
     */
    private final Integer code;

    /**
     * 状态描述。
     */
    private final String description;

    TemplateStatusEnum(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据状态码获取枚举。
     *
     * @param code 状态码
     * @return 对应的枚举，找不到则返回 null
     */
    public static TemplateStatusEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (TemplateStatusEnum value : values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        return null;
    }
}
