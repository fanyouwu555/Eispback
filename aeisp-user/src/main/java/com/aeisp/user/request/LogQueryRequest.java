package com.aeisp.user.request;

import com.aeisp.common.constant.CommonConstants;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * 登录日志分页查询请求。
 *
 * @author AEISP Team
 */
@Data
public class LogQueryRequest {

    /**
     * 页码。
     */
    private Long pageNum = CommonConstants.DEFAULT_PAGE_NUM;

    /**
     * 每页大小。
     */
    private Long pageSize = CommonConstants.DEFAULT_PAGE_SIZE;

    /**
     * 用户 ID。
     */
    private Long userId;

    /**
     * 登录账号（模糊查询）。
     */
    private String loginAccount;

    /**
     * 登录类型：1-密码登录，2-验证码登录，3-Token刷新。
     */
    private Integer loginType;

    /**
     * 登录结果：1-成功，2-密码错误，3-账号不存在，4-账号禁用，5-账号冻结，6-账号锁定，7-验证码错误，8-Token过期。
     */
    private Integer loginResult;

    /**
     * 登录时间起始。
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAtStart;

    /**
     * 登录时间截止。
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAtEnd;
}
