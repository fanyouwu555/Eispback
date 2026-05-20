package com.aeisp.system.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 操作日志注解。
 *
 * <p>标记在 Controller 方法上，自动拦截并记录操作日志。
 * 支持自定义模块名称、操作类型及是否记录请求参数/响应数据。</p>
 *
 * @author AEISP Team
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OperationLog {

    /**
     * 操作模块，如 {@code 用户管理}、{@code 角色管理}。
     */
    String module();

    /**
     * 操作类型，如 {@code 新增}、{@code 修改}、{@code 删除}、{@code 查询}。
     */
    String operation();

    /**
     * 是否记录请求参数。
     */
    boolean recordParams() default true;

    /**
     * 是否记录响应数据。
     */
    boolean recordResponse() default false;

    /**
     * 敏感度：1=普通，2=敏感（涉及资金/权限变更）
     */
    int sensitivity() default 1;
}
