package com.aeisp.template.service;

import com.aeisp.template.dto.TplTemplateCategoryDTO;
import com.aeisp.template.dto.TplTemplateCategoryVO;

import java.util.List;

public interface TplTemplateCategoryService {
    List<TplTemplateCategoryVO> getTree();
    TplTemplateCategoryVO getById(Long id);
    Long create(TplTemplateCategoryDTO dto);
    Boolean update(Long id, TplTemplateCategoryDTO dto);
    Boolean delete(Long id);
}