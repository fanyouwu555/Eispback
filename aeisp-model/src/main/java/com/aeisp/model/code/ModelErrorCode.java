package com.aeisp.model.code;

import com.aeisp.common.code.ErrorCode;

public enum ModelErrorCode implements ErrorCode {
    MODEL_NOT_FOUND(6001, "模型不存在"),
    SESSION_NOT_FOUND(6002, "会话不存在"),
    MODEL_CALL_NOT_IMPLEMENTED(6003, "模型调用功能待实现");

    private final int code;
    private final String message;

    ModelErrorCode(int code, String message) {
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
