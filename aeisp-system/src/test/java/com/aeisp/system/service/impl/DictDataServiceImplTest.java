package com.aeisp.system.service.impl;

import com.aeisp.common.constant.CommonConstants;
import com.aeisp.system.entity.SysDictData;
import com.aeisp.system.mapper.SysDictDataMapper;
import com.aeisp.system.vo.DictDataVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DictDataServiceImplTest {

    @Mock
    private SysDictDataMapper sysDictDataMapper;

    @InjectMocks
    private DictDataServiceImpl dictDataService;

    @Test
    void testListByDictCode() {
        SysDictData data = new SysDictData();
        data.setId(1L);
        data.setDictCode("user_status");
        data.setItemLabel("启用");
        data.setItemValue("1");
        data.setSortOrder(1);
        data.setDeleted(CommonConstants.DELETED_NO);
        when(sysDictDataMapper.selectList(any())).thenReturn(List.of(data));

        List<DictDataVO> result = dictDataService.listByDictCode("user_status");
        assertEquals(1, result.size());
        assertEquals("启用", result.get(0).getItemLabel());
    }

    @Test
    void testListByDictCodeEmpty() {
        when(sysDictDataMapper.selectList(any())).thenReturn(List.of());
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

        assertTrue(dictDataService.createData(data));
    }

    @Test
    void testUpdateData() {
        SysDictData data = new SysDictData();
        data.setId(1L);
        data.setItemLabel("更新后");
        when(sysDictDataMapper.updateById(data)).thenReturn(1);

        assertTrue(dictDataService.updateData(data));
    }

    @Test
    void testDeleteData() {
        when(sysDictDataMapper.deleteById(1L)).thenReturn(1);
        assertTrue(dictDataService.deleteData(1L));
    }
}