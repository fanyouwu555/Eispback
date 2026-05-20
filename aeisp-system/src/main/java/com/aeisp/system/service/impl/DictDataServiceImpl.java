package com.aeisp.system.service.impl;

import com.aeisp.common.constant.CommonConstants;
import com.aeisp.system.entity.SysDictData;
import com.aeisp.system.mapper.SysDictDataMapper;
import com.aeisp.system.service.DictDataService;
import com.aeisp.system.vo.DictDataVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DictDataServiceImpl implements DictDataService {

    private final SysDictDataMapper sysDictDataMapper;

    @Override
    public List<DictDataVO> listByDictCode(String dictCode) {
        LambdaQueryWrapper<SysDictData> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(SysDictData::getDeleted, CommonConstants.DELETED_NO);
        wrapper.eq(SysDictData::getDictCode, dictCode);
        wrapper.orderByAsc(SysDictData::getSortOrder);
        wrapper.orderByAsc(SysDictData::getId);
        return sysDictDataMapper.selectList(wrapper).stream()
                .map(DictDataServiceImpl::convertToVO)
                .toList();
    }

    @Override
    public SysDictData getById(Long id) {
        return sysDictDataMapper.selectById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createData(SysDictData dictData) {
        return sysDictDataMapper.insert(dictData) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateData(SysDictData dictData) {
        return sysDictDataMapper.updateById(dictData) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteData(Long id) {
        return sysDictDataMapper.deleteById(id) > 0;
    }

    @Override
    public List<DictDataVO> listByDictCodeWithPage(String dictCode) {
        return listByDictCode(dictCode);
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