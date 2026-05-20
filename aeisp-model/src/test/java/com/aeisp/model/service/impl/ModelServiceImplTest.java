package com.aeisp.model.service.impl;

import com.aeisp.common.PageResult;
import com.aeisp.common.exception.BizException;
import com.aeisp.model.dto.ModelDTO;
import com.aeisp.model.dto.ModelQueryRequest;
import com.aeisp.model.dto.ModelTestRequest;
import com.aeisp.model.dto.ModelUsageStatVO;
import com.aeisp.model.dto.ModelVO;
import com.aeisp.model.entity.AiModel;
import com.aeisp.model.mapper.AiModelMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

/**
 * ModelServiceImpl 单元测试。
 */
@ExtendWith(MockitoExtension.class)
class ModelServiceImplTest {

    @Mock
    private AiModelMapper aiModelMapper;

    @InjectMocks
    private ModelServiceImpl modelService;

    @Test
    void testAddModelSuccess() {
        when(aiModelMapper.insert(any(AiModel.class))).thenAnswer(inv -> {
            AiModel m = inv.getArgument(0);
            m.setId(1L);
            return 1;
        });

        ModelDTO dto = new ModelDTO();
        dto.setModelName("GPT-4");
        dto.setModelType("general");
        dto.setApiEndpoint("https://api.example.com");
        dto.setApiKey("sk-test");
        dto.setWeight(10);
        dto.setMaxQps(100);
        dto.setStatus(1);
        dto.setSortOrder(1);

        assertTrue(modelService.addModel(dto));
        verify(aiModelMapper).insert(argThat((AiModel m) ->
                m.getModelName().equals("GPT-4")
                        && m.getUsageCount() == 0L
                        && m.getFailureRate() == 0));
    }

    @Test
    void testUpdateModelSuccess() {
        AiModel existing = new AiModel();
        existing.setId(1L);
        existing.setModelName("Old Name");
        when(aiModelMapper.selectById(1L)).thenReturn(existing);
        when(aiModelMapper.updateById(any(AiModel.class))).thenReturn(1);

        ModelDTO dto = new ModelDTO();
        dto.setModelName("New Name");
        dto.setStatus(1);

        assertTrue(modelService.updateModel(1L, dto));
        verify(aiModelMapper).updateById(argThat((AiModel m) -> m.getModelName().equals("New Name")));
    }

    @Test
    void testUpdateModelNotFound() {
        when(aiModelMapper.selectById(1L)).thenReturn(null);

        ModelDTO dto = new ModelDTO();
        dto.setModelName("New Name");

        BizException ex = assertThrows(BizException.class, () -> modelService.updateModel(1L, dto));
        assertEquals("模型不存在", ex.getMessage());
    }

    @Test
    void testDeleteModelSuccess() {
        AiModel existing = new AiModel();
        existing.setId(1L);
        when(aiModelMapper.selectById(1L)).thenReturn(existing);

        assertTrue(modelService.deleteModel(1L));
        verify(aiModelMapper).deleteById(1L);
    }

    @Test
    void testDeleteModelNotFound() {
        when(aiModelMapper.selectById(1L)).thenReturn(null);

        BizException ex = assertThrows(BizException.class, () -> modelService.deleteModel(1L));
        assertEquals("模型不存在", ex.getMessage());
    }

    @Test
    void testToggleStatusSuccess() {
        AiModel existing = new AiModel();
        existing.setId(1L);
        existing.setStatus(1);
        when(aiModelMapper.selectById(1L)).thenReturn(existing);
        when(aiModelMapper.updateById(any(AiModel.class))).thenReturn(1);

        assertTrue(modelService.toggleStatus(1L, 0));
        verify(aiModelMapper).updateById(argThat((AiModel m) -> m.getStatus() == 0));
    }

    @Test
    void testToggleStatusNotFound() {
        when(aiModelMapper.selectById(1L)).thenReturn(null);
        assertThrows(BizException.class, () -> modelService.toggleStatus(1L, 0));
    }

    @Test
    void testUpdateSortOrderNegative() {
        AiModel existing = new AiModel();
        existing.setId(1L);
        existing.setSortOrder(0);
        when(aiModelMapper.selectById(1L)).thenReturn(existing);
        when(aiModelMapper.updateById(any(AiModel.class))).thenReturn(1);

        assertTrue(modelService.updateSortOrder(1L, -5));
        verify(aiModelMapper).updateById(argThat((AiModel m) -> m.getSortOrder() == -5));
    }

    @Test
    void testUpdateSortOrderNotFound() {
        when(aiModelMapper.selectById(1L)).thenReturn(null);
        assertThrows(BizException.class, () -> modelService.updateSortOrder(1L, 5));
    }

    @Test
    void testUpdateSortOrderSuccess() {
        AiModel existing = new AiModel();
        existing.setId(1L);
        existing.setSortOrder(0);
        when(aiModelMapper.selectById(1L)).thenReturn(existing);
        when(aiModelMapper.updateById(any(AiModel.class))).thenReturn(1);

        assertTrue(modelService.updateSortOrder(1L, 5));
        verify(aiModelMapper).updateById(argThat((AiModel m) -> m.getSortOrder() == 5));
    }

    @Test
    void testGetModelSuccess() {
        AiModel model = new AiModel();
        model.setId(1L);
        model.setModelName("GPT-4");
        when(aiModelMapper.selectById(1L)).thenReturn(model);

        ModelVO vo = modelService.getModel(1L);
        assertNotNull(vo);
        assertEquals("GPT-4", vo.getModelName());
    }

    @Test
    void testGetModelNotFound() {
        when(aiModelMapper.selectById(1L)).thenReturn(null);
        assertNull(modelService.getModel(1L));
    }

    @Test
    void testListModels() {
        AiModel model = new AiModel();
        model.setId(1L);
        model.setModelName("GPT-4");
        Page<AiModel> page = new Page<>(1, 10);
        page.setRecords(List.of(model));
        page.setTotal(1);
        when(aiModelMapper.selectPage(any(Page.class), any())).thenReturn(page);

        ModelQueryRequest request = new ModelQueryRequest();
        request.setPageNum(1L);
        request.setPageSize(10L);
        request.setModelName("GPT");
        PageResult<ModelVO> result = modelService.listModels(request);

        assertEquals(1, result.getTotal());
        assertEquals(1, result.getList().size());
        assertEquals("GPT-4", result.getList().get(0).getModelName());
    }

    @Test
    void testTestModel() {
        ModelTestRequest request = new ModelTestRequest();
        request.setModelId(1L);
        request.setTestInput("hello");
        assertTrue(modelService.testModel(request));
    }

    @Test
    void testGetUsageStats() {
        List<ModelUsageStatVO> stats = modelService.getUsageStats(1L);
        assertTrue(stats.isEmpty());
    }
}
