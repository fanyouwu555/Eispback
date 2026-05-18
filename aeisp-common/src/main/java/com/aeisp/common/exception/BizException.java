package com.aeisp.common.exception;

/**
 * 业务异常。
 *
 * <p>用于封装业务逻辑层面的可预期异常，如参数校验失败、业务规则冲突等。
 * 通常对应 HTTP 状态码 400 或自定义业务错误码。</p>
 *
 * @author AEISP Team
 */
public class BizException extends BaseException {

    private static final long serialVersionUID = 1L;

    /**
     * 默认业务错误码。
     */
    private static final int DEFAULT_BIZ_CODE = 400;

    /**
     * 构造业务异常。
     *
     * @param message 错误信息
     */
    public BizException(String message) {
        super(DEFAULT_BIZ_CODE, message);
    }

    /**
     * 构造业务异常。
     *
     * @param code    错误码
     * @param message 错误信息
     */
    public BizException(Integer code, String message) {
        super(code, message);
    }

    /**
     * 构造业务异常（携带 cause）。
     *
     * @param message 错误信息
     * @param cause   原始异常
     */
    public BizException(String message, Throwable cause) {
        super(DEFAULT_BIZ_CODE, message, cause);
    }

    /**
     * 构造业务异常（携带 cause）。
     *
     * @param code    错误码
     * @param message 错误信息
     * @param cause   原始异常
     */
    public BizException(Integer code, String message, Throwable cause) {
        super(code, message, cause);
    }
}
