package com.aeisp.user.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户登录日志 VO。
 *
 * @author AEISP Team
 */
@Data
public class UsrLoginLogVO {

    /**
     * 日志 ID。
     */
    private Long id;

    /**
     * 用户 ID。
     */
    private Long userId;

    /**
     * 用户名。
     */
    private String username;

    /**
     * 登录时间。
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime loginTime;

    /**
     * 登录 IP。
     */
    private String ip;

    /**
     * 登录设备。
     */
    private String device;

    /**
     * 登录结果：0-失败，1-成功。
     */
    private Integer result;

    /**
     * 错误信息。
     */
    private String errorMsg;
}
