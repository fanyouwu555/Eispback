package com.aeisp.system.entity;

import com.aeisp.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_dict_data")
public class SysDictData extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private String dictCode;

    private String itemLabel;

    private String itemValue;

    private Integer sortOrder;

    private Integer status;

    private String color;

    private Integer isDefault;
}