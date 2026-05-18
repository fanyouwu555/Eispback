package com.aeisp.user.request;

import com.aeisp.common.constant.CommonConstants;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

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
     * 用户名（模糊查询）。
     */
    private String username;

    /**
     * 登录结果：0-失败，1-成功。
     */
    private Integer result;

    /**
     * 登录时间起始。
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime loginTimeStart;

    /**
     * 登录时间截止。
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime loginTimeEnd;
}
