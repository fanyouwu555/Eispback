package com.aeisp.common;

import com.aeisp.common.constant.ResultCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 统一响应包装类。
 *
 * <p>用于封装所有 REST API 的返回结果，包含状态码、提示信息、业务数据和时间戳。</p>
 *
 * @param <T> 业务数据类型
 * @author AEISP Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 状态码，200 表示成功。
     */
    private Integer code;

    /**
     * 提示信息。
     */
    private String message;

    /**
     * 业务数据。
     */
    private T data;

    /**
     * 时间戳（毫秒）。
     */
    private Long timestamp;

    /**
     * 构造一个成功响应（无数据）。
     *
     * @param <T> 数据类型
     * @return 成功响应对象
     */
    public static <T> Result<T> success() {
        return Result.<T>builder()
                .code(ResultCode.SUCCESS)
                .message("操作成功")
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 构造一个成功响应（携带数据）。
     *
     * @param data 业务数据
     * @param <T>  数据类型
     * @return 成功响应对象
     */
    public static <T> Result<T> success(T data) {
        return Result.<T>builder()
                .code(ResultCode.SUCCESS)
                .message("操作成功")
                .data(data)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 构造一个成功响应（携带数据和自定义消息）。
     *
     * @param data    业务数据
     * @param message 提示信息
     * @param <T>     数据类型
     * @return 成功响应对象
     */
    public static <T> Result<T> success(T data, String message) {
        return Result.<T>builder()
                .code(ResultCode.SUCCESS)
                .message(message)
                .data(data)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 构造一个错误响应。
     *
     * @param code    错误码
     * @param message 错误信息
     * @param <T>     数据类型
     * @return 错误响应对象
     */
    public static <T> Result<T> error(int code, String message) {
        return Result.<T>builder()
                .code(code)
                .message(message)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 构造一个错误响应（携带数据）。
     *
     * @param code    错误码
     * @param message 错误信息
     * @param data    业务数据
     * @param <T>     数据类型
     * @return 错误响应对象
     */
    public static <T> Result<T> error(int code, String message, T data) {
        return Result.<T>builder()
                .code(code)
                .message(message)
                .data(data)
                .timestamp(System.currentTimeMillis())
                .build();
    }
}
