package com.aeisp.user.code;

import com.aeisp.common.code.ErrorCode;

public enum UserErrorCode implements ErrorCode {
    USER_NOT_FOUND(3001, "用户不存在"),
    USERNAME_EXISTS(3002, "用户名已注册"),
    PHONE_EXISTS(3003, "手机号已注册"),
    EMAIL_EXISTS(3004, "邮箱已注册"),
    VERIFICATION_CODE_ERROR(3005, "验证码错误"),
    VERIFICATION_CODE_EXPIRED(3006, "验证码已过期"),
    USER_STATUS_ABNORMAL(3007, "用户状态异常"),
    FIRST_RECHARGE_REQUIRED(3008, "您尚未完成首次充值，请先充值"),
    COMPETITION_ACCOUNT(3009, "比赛专用账号，正在跳转比赛环境");

    private final int code;
    private final String message;

    UserErrorCode(int code, String message) {
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
