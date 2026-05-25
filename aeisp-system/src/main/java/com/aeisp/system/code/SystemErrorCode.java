package com.aeisp.system.code;

import com.aeisp.common.code.ErrorCode;

public enum SystemErrorCode implements ErrorCode {
    USER_NOT_FOUND(2001, "用户不存在"),
    USER_CANNOT_DEMOTE_SUPER(2002, "系统中至少保留一个超级管理员，无法降级"),
    USER_CANNOT_DELETE_LAST_SUPER(2003, "系统中至少保留一个超级管理员，无法删除"),
    ROLE_NOT_FOUND(2011, "角色不存在"),
    MENU_NOT_FOUND(2012, "菜单不存在"),
    DICT_TYPE_NOT_FOUND(2021, "字典类型不存在"),
    DICT_DATA_NOT_FOUND(2022, "字典数据不存在"),
    CONFIG_NOT_FOUND(2031, "配置项不存在");

    private final int code;
    private final String message;

    SystemErrorCode(int code, String message) {
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
