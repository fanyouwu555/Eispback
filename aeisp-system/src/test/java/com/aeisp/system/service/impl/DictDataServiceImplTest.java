package com.aeisp.system.service.impl;

import com.aeisp.common.constant.CacheConstants;
import com.aeisp.common.constant.CommonConstants;
import com.aeisp.system.entity.SysDictData;
import com.aeisp.system.mapper.SysDictDataMapper;
import com.aeisp.system.vo.DictDataVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DictDataServiceImplTest {

    @Mock
    private SysDictDataMapper sysDictDataMapper;
    @Mock
    private StringRedisTemplate stringRedisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;
    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private DictDataServiceImpl dictDataService;

    @Test
    void testListByDictCodeCacheMiss() {
        SysDictData data = new SysDictData();
        data.setId(1L);
        data.setDictCode("user_status");
        data.setItemLabel("启用");
        data.setItemValue("1");
        data.setSortOrder(1);
        data.setDeleted(CommonConstants.DELETED_NO);
        when(sysDictDataMapper.selectList(any())).thenReturn(List.of(data));
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(null);

        List<DictDataVO> result = dictDataService.listByDictCode("user_status");
        assertEquals(1, result.size());
        assertEquals("启用", result.get(0).getItemLabel());
    }

    @Test
    void testListByDictCodeEmpty() {
        when(sysDictDataMapper.selectList(any())).thenReturn(List.of());
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(null);

        List<DictDataVO> result = dictDataService.listByDictCode("empty");
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetById() {
        SysDictData data = new SysDictData();
        data.setId(1L);
        data.setItemLabel("禁用");
        when(sysDictDataMapper.selectById(1L)).thenReturn(data);

        SysDictData result = dictDataService.getById(1L);
        assertNotNull(result);
        assertEquals("禁用", result.getItemLabel());
    }

    @Test
    void testCreateData() {
        SysDictData data = new SysDictData();
        data.setDictCode("user_status");
        data.setItemLabel("测试");
        data.setItemValue("99");
        when(sysDictDataMapper.insert(data)).thenReturn(1);
        when(stringRedisTemplate.delete(anyString())).thenReturn(true);

        assertTrue(dictDataService.createData(data));
        verify(stringRedisTemplate).delete(CacheConstants.CACHE_DICT + "data:user_status");
    }

    @Test
    void testUpdateData() {
        SysDictData existing = new SysDictData();
        existing.setId(1L);
        existing.setDictCode("user_status");
        when(sysDictDataMapper.selectById(1L)).thenReturn(existing);
        when(sysDictDataMapper.updateById(any(SysDictData.class))).thenReturn(1);
        when(stringRedisTemplate.delete(anyString())).thenReturn(true);

        SysDictData data = new SysDictData();
        data.setId(1L);
        data.setItemLabel("更新后");
        assertTrue(dictDataService.updateData(data));
        verify(stringRedisTemplate).delete(CacheConstants.CACHE_DICT + "data:user_status");
    }

    @Test
    void testDeleteData() {
        SysDictData existing = new SysDictData();
        existing.setId(1L);
        existing.setDictCode("user_status");
        when(sysDictDataMapper.selectById(1L)).thenReturn(existing);
        when(sysDictDataMapper.deleteById(1L)).thenReturn(1);
        when(stringRedisTemplate.delete(anyString())).thenReturn(true);

        assertTrue(dictDataService.deleteData(1L));
        verify(stringRedisTemplate).delete(CacheConstants.CACHE_DICT + "data:user_status");
    }
}
