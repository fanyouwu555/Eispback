package com.aeisp.common.exception;

import lombok.Getter;

/**
 * 抽象异常基类。
 *
 * <p>所有业务异常和系统异常的根类，继承自 {@link RuntimeException}，
 * 包含错误码和错误信息，便于统一异常处理和日志记录。</p>
 *
 * @author AEISP Team
 */
@Getter
public abstract class BaseException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * 错误码。
     */
    private final Integer code;

    /**
     * 构造异常。
     *
     * @param code    错误码
     * @param message 错误信息
     */
    protected BaseException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * 构造异常（携带 cause）。
     *
     * @param code    错误码
     * @param message 错误信息
     * @param cause   原始异常
     */
    protected BaseException(Integer code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }
}
