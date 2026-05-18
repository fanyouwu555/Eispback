package com.aeisp.template.enums;

import lombok.Getter;

/**
 * 模板适用场景枚举。
 *
 * @author AEISP Team
 */
@Getter
public enum TemplateScenarioEnum {

    /**
     * 教学场景。
     */
    TEACHING("teaching", "教学"),

    /**
     * 竞赛场景。
     */
    COMPETITION("competition", "竞赛"),

    /**
     * 实战场景。
     */
    PRACTICE("practice", "实战");

    /**
     * 场景编码。
     */
    private final String code;

    /**
     * 场景描述。
     */
    private final String description;

    TemplateScenarioEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据编码获取枚举。
     *
     * @param code 场景编码
     * @return 对应的枚举，找不到则返回 null
     */
    public static TemplateScenarioEnum fromCode(String code) {
        for (TemplateScenarioEnum value : values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        return null;
    }
}
