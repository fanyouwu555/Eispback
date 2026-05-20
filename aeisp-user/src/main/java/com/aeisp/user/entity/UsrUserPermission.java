package com.aeisp.user.entity;

import com.aeisp.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("usr_user_permission")
public class UsrUserPermission extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private Long userId;

    private String permKey;

    private String permValue;

    private LocalDateTime effectiveAt;

    private LocalDateTime expireAt;
}