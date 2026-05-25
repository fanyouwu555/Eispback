package com.aeisp.project.code;

import com.aeisp.common.code.ErrorCode;

public enum ProjectErrorCode implements ErrorCode {
    PROJECT_NOT_FOUND(8001, "项目不存在"),
    PROJECT_ALREADY_ARCHIVED(8002, "项目已归档，无需重复操作");

    private final int code;
    private final String message;

    ProjectErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
