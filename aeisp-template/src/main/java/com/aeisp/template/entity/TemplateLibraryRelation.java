package com.aeisp.template.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("tpl_template_library")
public class TemplateLibraryRelation {

    private Long id;
    private Long templateId;
    private Long libraryId;
}
