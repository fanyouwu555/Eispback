package com.aeisp.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 前端用户角色关联实体。
 *
 * <p>维护前端用户与角色之间的多对多关联关系。</p>
 *
 * @author AEISP Team
 */
@Data
@TableName("usr_user_role")
public class UsrUserRole {

    private static final long serialVersionUID = 1L;

    /**
     * 主键 ID，自增。
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户 ID，外键关联 usr_user.id。
     */
    private Long userId;

    /**
     * 角色 ID，外键关联 sys_role.id。
     */
    private Long roleId;

    /**
     * 授权人管理员ID。
     */
    private Long grantedBy;

    /**
     * 授权时间。
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime grantedAt;

    /**
     * 创建时间。
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
