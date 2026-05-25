package com.aeisp.template.service.impl;

import com.aeisp.common.exception.BizException;
import com.aeisp.template.code.TemplateErrorCode;
import com.aeisp.template.dto.TplTemplateCategoryDTO;
import com.aeisp.template.dto.TplTemplateCategoryVO;
import com.aeisp.template.entity.TplTemplateCategory;
import com.aeisp.template.mapper.TplTemplateCategoryMapper;
import com.aeisp.template.service.TplTemplateCategoryService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TplTemplateCategoryServiceImpl implements TplTemplateCategoryService {

    private final TplTemplateCategoryMapper mapper;

    @Override
    public List<TplTemplateCategoryVO> getTree() {
        List<TplTemplateCategory> all = mapper.selectList(
                new LambdaQueryWrapper<TplTemplateCategory>()
                        .eq(TplTemplateCategory::getDeleted, 0)
                        .orderByAsc(TplTemplateCategory::getLevel)
                        .orderByAsc(TplTemplateCategory::getSortOrder));
        return buildTree(all);
    }

    @Override
    public TplTemplateCategoryVO getById(Long id) {
        TplTemplateCategory entity = mapper.selectById(id);
        if (entity == null || entity.getDeleted() != 0) {
            throw new BizException(TemplateErrorCode.CATEGORY_NOT_FOUND);
        }
        return toVO(entity);
    }

    @Override
    public Long create(TplTemplateCategoryDTO dto) {
        TplTemplateCategory entity = new TplTemplateCategory();
        BeanUtils.copyProperties(dto, entity);
        if (dto.getParentId() != null && dto.getParentId() > 0) {
            TplTemplateCategory parent = mapper.selectById(dto.getParentId());
            if (parent == null || parent.getDeleted() != 0) {
                throw new BizException(TemplateErrorCode.CATEGORY_PARENT_NOT_FOUND);
            }
            if (parent.getLevel() >= 2) {
                throw new BizException(TemplateErrorCode.CATEGORY_MAX_LEVEL);
            }
            entity.setLevel(parent.getLevel() + 1);
        } else {
            entity.setLevel(0);
        }
        mapper.insert(entity);
        return entity.getId();
    }

    @Override
    public Boolean update(Long id, TplTemplateCategoryDTO dto) {
        TplTemplateCategory entity = mapper.selectById(id);
        if (entity == null) throw new BizException(TemplateErrorCode.CATEGORY_NOT_FOUND);
        if (dto.getName() != null) entity.setName(dto.getName());
        if (dto.getSortOrder() != null) entity.setSortOrder(dto.getSortOrder());
        mapper.updateById(entity);
        return true;
    }

    @Override
    public Boolean delete(Long id) {
        TplTemplateCategory entity = mapper.selectById(id);
        if (entity == null) throw new BizException(TemplateErrorCode.CATEGORY_NOT_FOUND);
        long childCount = mapper.selectCount(
                new LambdaQueryWrapper<TplTemplateCategory>()
                        .eq(TplTemplateCategory::getParentId, id)
                        .eq(TplTemplateCategory::getDeleted, 0));
        if (childCount > 0) throw new BizException(TemplateErrorCode.CATEGORY_HAS_CHILDREN);
        mapper.deleteById(id);
        return true;
    }

    private List<TplTemplateCategoryVO> buildTree(List<TplTemplateCategory> all) {
        List<TplTemplateCategoryVO> roots = new ArrayList<>();
        for (TplTemplateCategory c : all) {
            if (c.getParentId() == null || c.getParentId() == 0) {
                roots.add(buildSubTree(c, all));
            }
        }
        return roots;
    }

    private TplTemplateCategoryVO buildSubTree(TplTemplateCategory node, List<TplTemplateCategory> all) {
        TplTemplateCategoryVO vo = toVO(node);
        vo.setChildren(all.stream()
                .filter(c -> c.getParentId() != null && c.getParentId().equals(node.getId()))
                .map(c -> buildSubTree(c, all))
                .collect(Collectors.toList()));
        return vo;
    }

    private TplTemplateCategoryVO toVO(TplTemplateCategory entity) {
        TplTemplateCategoryVO vo = new TplTemplateCategoryVO();
        BeanUtils.copyProperties(entity, vo);
        vo.setChildren(new ArrayList<>());
        return vo;
    }
}