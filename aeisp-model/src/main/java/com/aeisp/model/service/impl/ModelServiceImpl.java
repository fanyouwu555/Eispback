package com.aeisp.model.service.impl;

import com.aeisp.common.PageResult;
import com.aeisp.common.exception.BizException;
import com.aeisp.model.code.ModelErrorCode;
import com.aeisp.model.dto.ModelDTO;
import com.aeisp.model.dto.ModelQueryRequest;
import com.aeisp.model.dto.ModelTestRequest;
import com.aeisp.model.dto.ModelUsageStatVO;
import com.aeisp.model.dto.ModelVO;
import com.aeisp.model.entity.AiModel;
import com.aeisp.model.mapper.AiModelMapper;
import com.aeisp.model.service.ModelService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 大模型管理服务实现类。
 *
 * @author AEISP Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ModelServiceImpl implements ModelService {

    private final AiModelMapper aiModelMapper;

    @Override
    public boolean addModel(ModelDTO dto) {
        AiModel model = new AiModel();
        BeanUtils.copyProperties(dto, model);
        model.setUsageCount(0L);
        model.setFailureRate(0);
        aiModelMapper.insert(model);
        return true;
    }

    @Override
    public boolean updateModel(Long id, ModelDTO dto) {
        AiModel model = aiModelMapper.selectById(id);
        if (model == null) {
            throw new BizException(ModelErrorCode.MODEL_NOT_FOUND);
        }
        BeanUtils.copyProperties(dto, model);
        return aiModelMapper.updateById(model) > 0;
    }

    @Override
    public boolean deleteModel(Long id) {
        AiModel model = aiModelMapper.selectById(id);
        if (model == null) {
            throw new BizException(ModelErrorCode.MODEL_NOT_FOUND);
        }
        aiModelMapper.deleteById(id);
        return true;
    }

    @Override
    public boolean toggleStatus(Long id, Integer status) {
        AiModel model = aiModelMapper.selectById(id);
        if (model == null) {
            throw new BizException(ModelErrorCode.MODEL_NOT_FOUND);
        }
        model.setStatus(status);
        return aiModelMapper.updateById(model) > 0;
    }

    @Override
    public boolean updateSortOrder(Long id, Integer sortOrder) {
        AiModel model = aiModelMapper.selectById(id);
        if (model == null) {
            throw new BizException(ModelErrorCode.MODEL_NOT_FOUND);
        }
        model.setSortOrder(sortOrder);
        return aiModelMapper.updateById(model) > 0;
    }

    @Override
    public ModelVO getModel(Long id) {
        AiModel model = aiModelMapper.selectById(id);
        if (model == null) {
            return null;
        }
        ModelVO vo = new ModelVO();
        BeanUtils.copyProperties(model, vo);
        return vo;
    }

    @Override
    public PageResult<ModelVO> listModels(ModelQueryRequest request) {
        Page<AiModel> page = new Page<>(request.getPageNum(), request.getPageSize());
        LambdaQueryWrapper<AiModel> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(request.getModelName()), AiModel::getModelName, request.getModelName())
                .eq(StringUtils.hasText(request.getModelType()), AiModel::getModelType, request.getModelType())
                .eq(request.getStatus() != null, AiModel::getStatus, request.getStatus())
                .orderByDesc(AiModel::getSortOrder)
                .orderByDesc(AiModel::getCreatedAt);
        Page<AiModel> resultPage = aiModelMapper.selectPage(page, wrapper);

        List<ModelVO> voList = new ArrayList<>();
        for (AiModel m : resultPage.getRecords()) {
            ModelVO vo = new ModelVO();
            BeanUtils.copyProperties(m, vo);
            voList.add(vo);
        }
        return PageResult.of(resultPage, voList);
    }

    @Override
    public boolean testModel(ModelTestRequest request) {
        log.info("模型在线测试，modelId={}", request.getModelId());
        return true;
    }

    @Override
    public List<ModelUsageStatVO> getUsageStats(Long modelId) {
        return Collections.emptyList();
    }
}
