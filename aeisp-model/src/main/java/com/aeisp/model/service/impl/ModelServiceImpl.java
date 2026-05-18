package com.aeisp.model.service.impl;

import com.aeisp.common.PageResult;
import com.aeisp.model.dto.ModelDTO;
import com.aeisp.model.dto.ModelQueryRequest;
import com.aeisp.model.dto.ModelTestRequest;
import com.aeisp.model.dto.ModelUsageStatVO;
import com.aeisp.model.dto.ModelVO;
import com.aeisp.model.service.ModelService;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * 大模型管理服务实现类。
 *
 * <p>待实现真实业务逻辑，当前所有方法抛出 {@link UnsupportedOperationException}。</p>
 *
 * @author AEISP Team
 */
@Service
public class ModelServiceImpl implements ModelService {

    @Override
    public boolean addModel(ModelDTO dto) {
        throw new UnsupportedOperationException("addModel 待实现");
    }

    @Override
    public boolean updateModel(Long id, ModelDTO dto) {
        throw new UnsupportedOperationException("updateModel 待实现");
    }

    @Override
    public boolean deleteModel(Long id) {
        throw new UnsupportedOperationException("deleteModel 待实现");
    }

    @Override
    public boolean toggleStatus(Long id, Integer status) {
        throw new UnsupportedOperationException("toggleStatus 待实现");
    }

    @Override
    public boolean updateSortOrder(Long id, Integer sortOrder) {
        throw new UnsupportedOperationException("updateSortOrder 待实现");
    }

    @Override
    public ModelVO getModel(Long id) {
        throw new UnsupportedOperationException("getModel 待实现");
    }

    @Override
    public PageResult<ModelVO> listModels(ModelQueryRequest request) {
        return PageResult.<ModelVO>builder()
                .list(Collections.emptyList())
                .total(0L)
                .pageNum(request.getPageNum())
                .pageSize(request.getPageSize())
                .totalPages(0L)
                .build();
    }

    @Override
    public boolean testModel(ModelTestRequest request) {
        throw new UnsupportedOperationException("testModel 待实现");
    }

    @Override
    public java.util.List<ModelUsageStatVO> getUsageStats(Long modelId) {
        throw new UnsupportedOperationException("getUsageStats 待实现");
    }
}
