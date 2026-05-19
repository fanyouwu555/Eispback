package com.aeisp.user.request;

import com.aeisp.common.constant.CommonConstants;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户分页查询请求。
 *
 * @author AEISP Team
 */
@Data
public class UserQueryRequest {

    /**
     * 页码。
     */
    private Long pageNum = CommonConstants.DEFAULT_PAGE_NUM;

    /**
     * 每页大小。
     */
    private Long pageSize = CommonConstants.DEFAULT_PAGE_SIZE;

    /**
     * 用户名（模糊查询）。
     */
    private String username;

    /**
     * 手机号（模糊查询）。
     */
    private String phone;

    /**
     * 账号状态。
     */
    private Integer status;

    /**
     * 注册时间起始。
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime registerTimeStart;

    /**
     * 注册时间截止。
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime registerTimeEnd;

    /**
     * 角色 ID 列表（IN 查询）。
     */
    private List<Long> roleIds;

    /**
     * 剩余时长下限（分钟，含）。
     */
    private Integer remainingMinutesMin;

    /**
     * 剩余时长上限（分钟，含）。
     */
    private Integer remainingMinutesMax;
}
