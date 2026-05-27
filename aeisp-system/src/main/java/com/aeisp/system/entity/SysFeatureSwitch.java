package com.aeisp.system.entity;

import com.aeisp.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_feature_switch")
public class SysFeatureSwitch extends BaseEntity {
    private String featureKey;
    private String featureName;
    private String category;
    private Boolean enabled;
    private String description;
    private Integer sortOrder;
}