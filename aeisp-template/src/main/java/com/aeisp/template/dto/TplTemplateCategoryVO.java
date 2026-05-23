package com.aeisp.template.dto;

import com.aeisp.template.dto.vo.TplTemplateVO;
import lombok.Data;

import java.util.List;

@Data
public class TplTemplateCategoryVO {
    private Long id;
    private String name;
    private Long parentId;
    private Integer level;
    private Integer sortOrder;
    private List<TplTemplateCategoryVO> children;
    private List<TplTemplateVO> templates;
}