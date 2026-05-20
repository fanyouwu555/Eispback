package com.aeisp.template.dto;

import lombok.Data;

@Data
public class TplTemplateCategoryDTO {
    private String name;
    private Long parentId;
    private Integer level;
    private Integer sortOrder;
}