package com.aeisp.library.code;

import com.aeisp.common.code.ErrorCode;

public enum LibraryErrorCode implements ErrorCode {

    LIBRARY_NOT_FOUND(5001, "库资源不存在"),
    LIBRARY_CODE_EXISTS(5002, "资源编码已存在"),
    VERSION_NOT_FOUND(5011, "版本不存在"),
    VERSION_NOT_BELONG(5012, "版本不属于该库资源"),
    VERSION_EXISTS(5013, "版本号已存在"),
    LIBRARY_NOT_PUBLISHED(5021, "库资源未上架"),
    FILE_UPLOAD_FAILED(5031, "文件上传失败"),
    FILE_HASH_MISMATCH(5032, "文件哈希校验失败");

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
