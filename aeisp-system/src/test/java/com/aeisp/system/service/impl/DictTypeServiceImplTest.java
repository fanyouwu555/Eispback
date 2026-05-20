package com.aeisp.system.service.impl;

import com.aeisp.common.PageResult;
import com.aeisp.common.constant.CommonConstants;
import com.aeisp.system.dto.DictTypeQueryRequest;
import com.aeisp.system.entity.SysDictData;
import com.aeisp.system.entity.SysDictType;
import com.aeisp.system.mapper.SysDictDataMapper;
import com.aeisp.system.mapper.SysDictTypeMapper;
import com.aeisp.system.vo.DictTypeVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
class DictTypeServiceImplTest {

    @Mock
    private SysDictTypeMapper sysDictTypeMapper;
    @Mock
    private SysDictDataMapper sysDictDataMapper;

    @InjectMocks
    private DictTypeServiceImpl dictTypeService;

    @Test
    void testListByPage() {
        SysDictType type = new SysDictType();
        type.setId(1L);
        type.setDictName("用户状态");
        type.setDictCode("user_status");
        type.setStatus(1);
        Page<SysDictType> page = new Page<>(1, 10);
        page.setRecords(List.of(type));
        page.setTotal(1);
        when(sysDictTypeMapper.selectPage(any(Page.class), any())).thenReturn(page);

        DictTypeQueryRequest request = new DictTypeQueryRequest();
        request.setPageNum(1L);
        request.setPageSize(10L);
        PageResult<DictTypeVO> result = dictTypeService.listByPage(request);

        assertEquals(1, result.getTotal());
        assertEquals("user_status", result.getList().get(0).getDictCode());
    }

    @Test
    void testListAll() {
        SysDictType type = new SysDictType();
        type.setId(1L);
        type.setDictName("性别");
        type.setDictCode("gender");
        type.setDeleted(CommonConstants.DELETED_NO);
        when(sysDictTypeMapper.selectList(any())).thenReturn(List.of(type));

        List<DictTypeVO> result = dictTypeService.listAll();
        assertEquals(1, result.size());
        assertEquals("gender", result.get(0).getDictCode());
    }

    @Test
    void testGetById() {
        SysDictType type = new SysDictType();
        type.setId(1L);
        type.setDictName("通知类型");
        when(sysDictTypeMapper.selectById(1L)).thenReturn(type);

        SysDictType result = dictTypeService.getById(1L);
        assertNotNull(result);
        assertEquals("通知类型", result.getDictName());
    }

    @Test
    void testGetByDictCode() {
        SysDictType type = new SysDictType();
        type.setDictCode("notice_type");
        when(sysDictTypeMapper.selectByDictCode("notice_type")).thenReturn(type);

        SysDictType result = dictTypeService.getByDictCode("notice_type");
        assertNotNull(result);
        assertEquals("notice_type", result.getDictCode());
    }

    @Test
    void testCreateType() {
        SysDictType type = new SysDictType();
        type.setDictName("新字典");
        type.setDictCode("new_dict");
        when(sysDictTypeMapper.insert(type)).thenReturn(1);

        assertTrue(dictTypeService.createType(type));
    }

    @Test
    void testUpdateType() {
        SysDictType type = new SysDictType();
        type.setId(1L);
        type.setDictName("更新后的名称");
        when(sysDictTypeMapper.updateById(type)).thenReturn(1);

        assertTrue(dictTypeService.updateType(type));
    }

    @Test
    void testDeleteTypeNonSystem() {
        SysDictType type = new SysDictType();
        type.setId(1L);
        type.setIsSystem(0);
        when(sysDictTypeMapper.selectById(1L)).thenReturn(type);
        when(sysDictTypeMapper.deleteById(1L)).thenReturn(1);

        assertTrue(dictTypeService.deleteType(1L));
    }

    @Test
    void testDeleteTypeSystem() {
        SysDictType type = new SysDictType();
        type.setId(1L);
        type.setIsSystem(1);
        when(sysDictTypeMapper.selectById(1L)).thenReturn(type);

        assertFalse(dictTypeService.deleteType(1L));
        verify(sysDictTypeMapper, never()).deleteById(any());
    }

    @Test
    void testGetDictData() {
        SysDictData data = new SysDictData();
        data.setId(1L);
        data.setDictCode("user_status");
        data.setItemLabel("正常");
        data.setItemValue("1");
        data.setStatus(1);
        data.setDeleted(CommonConstants.DELETED_NO);
        when(sysDictDataMapper.selectByDictCode("user_status")).thenReturn(List.of(data));

        var result = dictTypeService.getDictData("user_status");
        assertEquals(1, result.size());
        assertEquals("正常", result.get(0).getItemLabel());
    }
}