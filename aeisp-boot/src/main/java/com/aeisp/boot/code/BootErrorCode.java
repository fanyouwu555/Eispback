package com.aeisp.boot.code;

import com.aeisp.common.code.ErrorCode;

public enum BootErrorCode implements ErrorCode {
    USER_NOT_FOUND(1001, "账号不存在"),
    PASSWORD_MISMATCH(1002, "密码错误"),
    ACCOUNT_DISABLED(1003, "账号已被禁用"),
    ACCOUNT_FROZEN(1004, "账号已被冻结"),
    ACCOUNT_LOCKED(1005, "账号已被锁定"),
    ACCOUNT_RESET(1006, "账号密码已重置，请重新登录"),
    LOGIN_LOCKED_BY_RETRY(1007, "密码错误次数过多，账号已锁定"),
    ACCOUNT_LOGIN_ELSEWHERE(1008, "账号已在其他设备登录"),
    TOKEN_EXPIRED(1011, "登录已过期，请重新登录"),
    TOKEN_INVALID(1012, "Token 已失效，请重新登录"),
    TOKEN_TYPE_INVALID(1013, "无效的 Token 类型"),
    TOKEN_MISSING(1014, "未登录或 Token 已过期"),
    AUTH_FAILED(1050, "认证失败");

    private final int code;
    private final String message;

    BootErrorCode(int code, String message) {
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
