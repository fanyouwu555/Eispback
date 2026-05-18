package com.aeisp.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户登录日志实体。
 *
 * <p>记录前端用户的每次登录行为，包括登录时间、IP、设备、结果及失败原因。
 * 不继承 {@link com.aeisp.common.entity.BaseEntity}，使用独立轻量结构。</p>
 *
 * @author AEISP Team
 */
@Data
@TableName("usr_login_log")
public class UsrLoginLog {

    private static final long serialVersionUID = 1L;

    /**
     * 主键 ID，自增。
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 关联用户 ID。
     */
    private Long userId;

    /**
     * 登录用户名（冗余，方便查询）。
     */
    private String username;

    /**
     * 登录时间。
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime loginTime;

    /**
     * 登录 IP 地址。
     */
    private String ip;

    /**
     * 登录设备信息。
     */
    private String device;

    /**
     * 登录结果：0-失败，1-成功。
     */
    private Integer result;

    /**
     * 失败时的错误信息。
     */
    private String errorMsg;
}
