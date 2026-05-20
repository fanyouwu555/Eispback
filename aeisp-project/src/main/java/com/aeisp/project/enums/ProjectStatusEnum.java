package com.aeisp.project.enums;

import lombok.Getter;

@Getter
public enum ProjectStatusEnum {
    IN_PROGRESS(0, "进行中"),
    COMPLETED(1, "已完成"),
    ARCHIVED(2, "已归档");

    private final Integer code;
    private final String description;

    ProjectStatusEnum(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    public static ProjectStatusEnum fromCode(Integer code) {
        if (code == null) return null;
        for (ProjectStatusEnum value : values()) {
            if (value.code.equals(code)) return value;
        }
        return null;
    }
}