package com.aeisp.library.code;

import com.aeisp.common.code.ErrorCode;

public enum LibraryErrorCode implements ErrorCode {

    LIBRARY_NOT_FOUND(5001, "库资源不存在");

    private final int code;
    private final String message;

    LibraryErrorCode(int code, String message) {
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
