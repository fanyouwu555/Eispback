package com.aeisp.message.code;

import com.aeisp.common.code.ErrorCode;

public enum MessageErrorCode implements ErrorCode {
    NOTIFICATION_NOT_FOUND(5001, "消息不存在"),
    NOTIFICATION_ONLY_DRAFT_CAN_PUSH(5002, "只有草稿状态的消息才能推送"),
    NOTIFICATION_PUSH_SCOPE_INVALID(5003, "推送范围无效"),
    NOTIFICATION_PUSH_TARGET_EMPTY(5004, "推送目标用户为空"),
    NOTIFICATION_ONLY_SENT_CAN_REVOKE(5005, "只有已发送的消息才能撤回");

    private final int code;
    private final String message;

    MessageErrorCode(int code, String message) {
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
