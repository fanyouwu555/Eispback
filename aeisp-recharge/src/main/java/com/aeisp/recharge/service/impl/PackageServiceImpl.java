package com.aeisp.recharge.service.impl;

import com.aeisp.common.PageResult;
import com.aeisp.common.exception.BizException;
import com.aeisp.recharge.dto.PackageDTO;
import com.aeisp.recharge.dto.PackageQueryRequest;
import com.aeisp.recharge.dto.PackageVO;
import com.aeisp.recharge.entity.DurationPackage;
import com.aeisp.recharge.mapper.DurationPackageMapper;
import com.aeisp.recharge.service.PackageService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 时长套餐服务实现类。
 *
 * @author AEISP Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PackageServiceImpl implements PackageService {

    private final DurationPackageMapper packageMapper;

    @Override
    public boolean createPackage(PackageDTO dto) {
        DurationPackage entity = new DurationPackage();
        entity.setPackageName(dto.getPackageName());
        entity.setPriceCents(dto.getPrice());
        entity.setDurationMinutes(dto.getDurationHours() != null ? dto.getDurationHours() * 60 : null);
        entity.setValidDays(dto.getValidDays());
        entity.setPromotion(dto.getPromotion());
        entity.setStatus(dto.getStatus());
        entity.setSortOrder(dto.getSortOrder());
        packageMapper.insert(entity);
        return true;
    }

    @Override
    public boolean updatePackage(Long id, PackageDTO dto) {
        DurationPackage entity = packageMapper.selectById(id);
        if (entity == null) {
            throw new BizException("套餐不存在");
        }
        entity.setPackageName(dto.getPackageName());
        entity.setPriceCents(dto.getPrice());
        entity.setDurationMinutes(dto.getDurationHours() != null ? dto.getDurationHours() * 60 : null);
        entity.setValidDays(dto.getValidDays());
        entity.setPromotion(dto.getPromotion());
        entity.setStatus(dto.getStatus());
        entity.setSortOrder(dto.getSortOrder());
        return packageMapper.updateById(entity) > 0;
    }

    @Override
    public boolean deletePackage(Long id) {
        DurationPackage entity = packageMapper.selectById(id);
        if (entity == null) {
            throw new BizException("套餐不存在");
        }
        packageMapper.deleteById(id);
        return true;
    }

    @Override
    public List<PackageVO> listActivePackages() {
        List<DurationPackage> list = packageMapper.selectActiveList();
        List<PackageVO> voList = new ArrayList<>();
        for (DurationPackage p : list) {
            voList.add(convertToVO(p));
        }
        return voList;
    }

    @Override
    public PageResult<PackageVO> listPackages(PackageQueryRequest request) {
        Page<DurationPackage> page = new Page<>(request.getPageNum(), request.getPageSize());
        LambdaQueryWrapper<DurationPackage> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(request.getPackageName()), DurationPackage::getPackageName, request.getPackageName())
                .eq(request.getStatus() != null, DurationPackage::getStatus, request.getStatus())
                .orderByDesc(DurationPackage::getSortOrder)
                .orderByDesc(DurationPackage::getCreatedAt);
        Page<DurationPackage> resultPage = packageMapper.selectPage(page, wrapper);

        List<PackageVO> voList = new ArrayList<>();
        for (DurationPackage p : resultPage.getRecords()) {
            voList.add(convertToVO(p));
        }
        return PageResult.of(resultPage, voList);
    }

    private PackageVO convertToVO(DurationPackage entity) {
        PackageVO vo = new PackageVO();
        BeanUtils.copyProperties(entity, vo);
        vo.setPrice(entity.getPriceCents());
        vo.setDurationHours(entity.getDurationMinutes() != null ? entity.getDurationMinutes() / 60 : null);
        return vo;
    }
}
