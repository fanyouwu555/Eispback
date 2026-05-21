package com.aeisp.system.service.impl;

import com.aeisp.common.constant.CacheConstants;
import com.aeisp.common.constant.CommonConstants;
import com.aeisp.system.entity.SysDictData;
import com.aeisp.system.mapper.SysDictDataMapper;
import com.aeisp.system.service.DictDataService;
import com.aeisp.system.vo.DictDataVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
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
public class DictDataServiceImpl implements DictDataService {

    private final SysDictDataMapper sysDictDataMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    private static final Duration CACHE_TTL = Duration.ofMinutes(30);

    @Override
    public List<DictDataVO> listByDictCode(String dictCode) {
        String key = CacheConstants.CACHE_DICT + "data:" + dictCode;
        String cached = stringRedisTemplate.opsForValue().get(key);
        if (cached != null) {
            return fromJson(cached);
        }
        LambdaQueryWrapper<SysDictData> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(SysDictData::getDeleted, CommonConstants.DELETED_NO);
        wrapper.eq(SysDictData::getDictCode, dictCode);
        wrapper.orderByAsc(SysDictData::getSortOrder);
        wrapper.orderByAsc(SysDictData::getId);
        List<DictDataVO> result = sysDictDataMapper.selectList(wrapper).stream()
                .map(DictDataServiceImpl::convertToVO)
                .toList();
        stringRedisTemplate.opsForValue().set(key, toJson(result), CACHE_TTL);
        return result;
    }

    @Override
    public SysDictData getById(Long id) {
        return sysDictDataMapper.selectById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createData(SysDictData dictData) {
        boolean success = sysDictDataMapper.insert(dictData) > 0;
        if (success && dictData.getDictCode() != null) {
            evictDataCache(dictData.getDictCode());
        }
        return success;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateData(SysDictData dictData) {
        SysDictData existing = sysDictDataMapper.selectById(dictData.getId());
        boolean success = sysDictDataMapper.updateById(dictData) > 0;
        if (success) {
            String code = dictData.getDictCode() != null ? dictData.getDictCode() : (existing != null ? existing.getDictCode() : null);
            if (code != null) {
                evictDataCache(code);
            }
        }
        return success;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteData(Long id) {
        SysDictData existing = sysDictDataMapper.selectById(id);
        boolean success = sysDictDataMapper.deleteById(id) > 0;
        if (success && existing != null && existing.getDictCode() != null) {
            evictDataCache(existing.getDictCode());
        }
        return success;
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

    static DictDataVO convertToVO(SysDictData data) {
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
