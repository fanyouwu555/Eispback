package com.aeisp.system.service.impl;

import com.aeisp.common.PageResult;
import com.aeisp.common.constant.CacheConstants;
import com.aeisp.common.constant.CommonConstants;
import com.aeisp.system.dto.DictTypeQueryRequest;
import com.aeisp.system.entity.SysDictData;
import com.aeisp.system.entity.SysDictType;
import com.aeisp.system.mapper.SysDictDataMapper;
import com.aeisp.system.mapper.SysDictTypeMapper;
import com.aeisp.system.service.DictTypeService;
import com.aeisp.system.vo.DictDataVO;
import com.aeisp.system.vo.DictTypeVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DictTypeServiceImpl implements DictTypeService {

    private final SysDictTypeMapper sysDictTypeMapper;
    private final SysDictDataMapper sysDictDataMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    private static final Duration CACHE_TTL = Duration.ofMinutes(30);

    @Override
    public PageResult<DictTypeVO> listByPage(DictTypeQueryRequest request) {
        LambdaQueryWrapper<SysDictType> wrapper = buildWrapper(request);
        Page<SysDictType> page = new Page<>(request.getPageNum(), request.getPageSize());
        Page<SysDictType> resultPage = sysDictTypeMapper.selectPage(page, wrapper);
        List<DictTypeVO> voList = resultPage.getRecords().stream()
                .map(DictTypeServiceImpl::convertToVO)
                .toList();
        return PageResult.of(resultPage, voList);
    }

    @Override
    public List<DictTypeVO> listAll() {
        LambdaQueryWrapper<SysDictType> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(SysDictType::getDeleted, CommonConstants.DELETED_NO);
        wrapper.orderByAsc(SysDictType::getDictCode);
        return sysDictTypeMapper.selectList(wrapper).stream()
                .map(DictTypeServiceImpl::convertToVO)
                .toList();
    }

    @Override
    public SysDictType getById(Long id) {
        return sysDictTypeMapper.selectById(id);
    }

    @Override
    public SysDictType getByDictCode(String dictCode) {
        return sysDictTypeMapper.selectByDictCode(dictCode);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createType(SysDictType dictType) {
        return sysDictTypeMapper.insert(dictType) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateType(SysDictType dictType) {
        boolean success = sysDictTypeMapper.updateById(dictType) > 0;
        if (success && dictType.getDictCode() != null) {
            evictTypeCache(dictType.getDictCode());
        }
        return success;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteType(Long id) {
        SysDictType type = sysDictTypeMapper.selectById(id);
        if (type != null && type.getIsSystem() != null && type.getIsSystem() == 1) {
            return false;
        }
        boolean success = sysDictTypeMapper.deleteById(id) > 0;
        if (success && type != null && type.getDictCode() != null) {
            evictTypeCache(type.getDictCode());
            evictDataCache(type.getDictCode());
        }
        return success;
    }

    @Override
    public List<DictDataVO> getDictData(String dictCode) {
        String key = CacheConstants.CACHE_DICT + "data:" + dictCode;
        String cached = stringRedisTemplate.opsForValue().get(key);
        if (cached != null) {
            return fromJson(cached);
        }
        List<SysDictData> list = sysDictDataMapper.selectByDictCode(dictCode);
        List<DictDataVO> result = list.stream()
                .map(DictTypeServiceImpl::convertDataToVO)
                .toList();
        stringRedisTemplate.opsForValue().set(key, toJson(result), CACHE_TTL);
        return result;
    }

    void evictTypeCache(String dictCode) {
        stringRedisTemplate.delete(CacheConstants.CACHE_DICT + "type:" + dictCode);
    }

    void evictDataCache(String dictCode) {
        stringRedisTemplate.delete(CacheConstants.CACHE_DICT + "data:" + dictCode);
    }

    @SneakyThrows
    private String toJson(Object obj) {
        return objectMapper.writeValueAsString(obj);
    }

    @SneakyThrows
    private List<DictDataVO> fromJson(String json) {
        return objectMapper.readValue(json, new TypeReference<List<DictDataVO>>() {});
    }

    private LambdaQueryWrapper<SysDictType> buildWrapper(DictTypeQueryRequest request) {
        LambdaQueryWrapper<SysDictType> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(SysDictType::getDeleted, CommonConstants.DELETED_NO);
        if (request.getDictName() != null && !request.getDictName().isEmpty()) {
            wrapper.like(SysDictType::getDictName, request.getDictName());
        }
        if (request.getDictCode() != null && !request.getDictCode().isEmpty()) {
            wrapper.like(SysDictType::getDictCode, request.getDictCode());
        }
        if (request.getStatus() != null) {
            wrapper.eq(SysDictType::getStatus, request.getStatus());
        }
        wrapper.orderByAsc(SysDictType::getDictCode);
        return wrapper;
    }

    static DictTypeVO convertToVO(SysDictType type) {
        DictTypeVO vo = new DictTypeVO();
        vo.setId(type.getId());
        vo.setDictName(type.getDictName());
        vo.setDictCode(type.getDictCode());
        vo.setDescription(type.getDescription());
        vo.setStatus(type.getStatus());
        vo.setIsSystem(type.getIsSystem());
        vo.setCreatedAt(type.getCreatedAt());
        vo.setUpdatedAt(type.getUpdatedAt());
        return vo;
    }

    static DictDataVO convertDataToVO(SysDictData data) {
        DictDataVO vo = new DictDataVO();
        vo.setId(data.getId());
        vo.setDictCode(data.getDictCode());
        vo.setItemLabel(data.getItemLabel());
        vo.setItemValue(data.getItemValue());
        vo.setSortOrder(data.getSortOrder());
        vo.setStatus(data.getStatus());
        vo.setColor(data.getColor());
        vo.setIsDefault(data.getIsDefault());
        vo.setCreatedAt(data.getCreatedAt());
        vo.setUpdatedAt(data.getUpdatedAt());
        return vo;
    }
}