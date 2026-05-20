package com.aeisp.template.entity;

import com.aeisp.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tpl_template_category")
public class TplTemplateCategory extends BaseEntity {

    private String name;
    private Long parentId;
    private Integer level;
    private Integer sortOrder;
}