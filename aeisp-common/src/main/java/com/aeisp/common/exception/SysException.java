package com.aeisp.common.exception;

/**
 * 系统异常。
 *
 * <p>用于封装系统层面的不可预期异常，如数据库连接失败、第三方服务不可用、
 * 空指针异常等。通常对应 HTTP 状态码 500。</p>
 *
 * @author AEISP Team
 */
public class SysException extends BaseException {

    private static final long serialVersionUID = 1L;

    /**
     * 默认系统错误码。
     */
    private static final int DEFAULT_SYS_CODE = 500;

    /**
     * 构造系统异常。
     *
     * @param message 错误信息
     */
    public SysException(String message) {
        super(DEFAULT_SYS_CODE, message);
    }

    /**
     * 构造系统异常。
     *
     * @param code    错误码
     * @param message 错误信息
     */
    public SysException(Integer code, String message) {
        super(code, message);
    }

    /**
     * 构造系统异常（携带 cause）。
     *
     * @param message 错误信息
     * @param cause   原始异常
     */
    public SysException(String message, Throwable cause) {
        super(DEFAULT_SYS_CODE, message, cause);
    }

    /**
     * 构造系统异常（携带 cause）。
     *
     * @param code    错误码
     * @param message 错误信息
     * @param cause   原始异常
     */
    public SysException(Integer code, String message, Throwable cause) {
        super(code, message, cause);
    }
}
