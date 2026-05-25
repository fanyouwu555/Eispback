package com.aeisp.common.constant;

/**
 * 统一响应状态码常量。
 *
 * <p>定义 REST API 返回的常用 HTTP 风格状态码，避免在代码中直接硬编码数字。</p>
 *
 * @author AEISP Team
 */
public final class ResultCode {

    private ResultCode() {
        // 工具类禁止实例化
    }

    /**
     * 操作成功。
     */
    public static final int SUCCESS = 200;

    /**
     * 请求参数错误。
     * @deprecated 请使用 {@link com.aeisp.common.code.CommonErrorCode}
     */
    @Deprecated
    public static final int BAD_REQUEST = 400;

    /**
     * 未授权（未登录或 Token 无效）。
     * @deprecated 请使用 {@link com.aeisp.common.code.CommonErrorCode}
     */
    @Deprecated
    public static final int UNAUTHORIZED = 401;

    /**
     * 禁止访问（权限不足）。
     * @deprecated 请使用 {@link com.aeisp.common.code.CommonErrorCode}
     */
    @Deprecated
    public static final int FORBIDDEN = 403;

    /**
     * 资源不存在。
     * @deprecated 请使用 {@link com.aeisp.common.code.CommonErrorCode}
     */
    @Deprecated
    public static final int NOT_FOUND = 404;

    /**
     * 请求方法不支持。
     * @deprecated 请使用 {@link com.aeisp.common.code.CommonErrorCode}
     */
    @Deprecated
    public static final int METHOD_NOT_ALLOWED = 405;

    /**
     * 请求过于频繁（限流）。
     * @deprecated 请使用 {@link com.aeisp.common.code.CommonErrorCode}
     */
    @Deprecated
    public static final int TOO_MANY_REQUESTS = 429;

    /**
     * 系统内部错误。
     */
    public static final int INTERNAL_ERROR = 500;
}
