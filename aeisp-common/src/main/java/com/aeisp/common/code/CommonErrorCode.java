package com.aeisp.common.code;

public enum CommonErrorCode implements ErrorCode {
    PARAM_MISSING(9001, "请求参数缺失"),
    PARAM_VALIDATION_FAILED(9002, "参数校验失败"),
    PARAM_BIND_FAILED(9003, "参数绑定失败"),
    RESOURCE_NOT_FOUND(9004, "请求资源不存在"),
    METHOD_NOT_ALLOWED(9005, "请求方法不支持"),
    ACCESS_DENIED(9006, "权限不足，无法访问该资源"),
    RATE_LIMITED(9007, "请求过于频繁"),
    SYSTEM_ERROR(9998, "系统繁忙，请稍后重试"),
    UNKNOWN_ERROR(9999, "未知错误");

    private final int code;
    private final String message;

    CommonErrorCode(int code, String message) {
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
