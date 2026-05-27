package com.aeisp.system.entity;

import com.aeisp.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_config_log")
public class SysConfigLog extends BaseEntity {
    private String configType;
    private Long refId;
    private String configKey;
    private String oldValue;
    private String newValue;
    private String environment;
    private Long operatedBy;
}