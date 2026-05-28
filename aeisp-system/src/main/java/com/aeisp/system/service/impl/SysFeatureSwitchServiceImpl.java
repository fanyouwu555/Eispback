package com.aeisp.system.service.impl;

import com.aeisp.common.exception.BizException;
import com.aeisp.system.entity.SysFeatureSwitch;
import com.aeisp.system.mapper.SysFeatureSwitchMapper;
import com.aeisp.system.service.SysConfigLogService;
import com.aeisp.system.service.SysFeatureSwitchService;
import com.aeisp.system.vo.SysFeatureSwitchVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class SysFeatureSwitchServiceImpl implements SysFeatureSwitchService {

    private final SysFeatureSwitchMapper sysFeatureSwitchMapper;
    private final SysConfigLogService sysConfigLogService;
    private final ConcurrentHashMap<String, Boolean> switchCache = new ConcurrentHashMap<>();

    @Override
    public boolean isEnabled(String featureKey) {
        return switchCache.computeIfAbsent(featureKey, key -> {
            LambdaQueryWrapper<SysFeatureSwitch> wrapper = Wrappers.lambdaQuery(SysFeatureSwitch.class)
                    .eq(SysFeatureSwitch::getFeatureKey, key)
                    .eq(SysFeatureSwitch::getDeleted, 0);
            SysFeatureSwitch entity = sysFeatureSwitchMapper.selectOne(wrapper);
            return entity == null || Boolean.TRUE.equals(entity.getEnabled());
        });
    }

    @Override
    public List<SysFeatureSwitchVO> listAll() {
        LambdaQueryWrapper<SysFeatureSwitch> wrapper = Wrappers.lambdaQuery(SysFeatureSwitch.class)
                .eq(SysFeatureSwitch::getDeleted, 0)
                .orderByAsc(SysFeatureSwitch::getSortOrder);
        return sysFeatureSwitchMapper.selectList(wrapper)
                .stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    public void toggle(Long id) {
        SysFeatureSwitch entity = sysFeatureSwitchMapper.selectById(id);
        if (entity == null || entity.getDeleted() == 1) {
            throw new BizException("功能开关不存在");
        }
        boolean oldValue = entity.getEnabled();
        entity.setEnabled(!oldValue);
        sysFeatureSwitchMapper.updateById(entity);
        switchCache.remove(entity.getFeatureKey());
        sysConfigLogService.recordChange("feature", id, entity.getFeatureKey(),
                String.valueOf(oldValue), String.valueOf(!oldValue), "all");
    }

    @Override
    public void toggleMaintenance(Boolean enabled) {
        LambdaQueryWrapper<SysFeatureSwitch> maintenanceQuery = Wrappers.lambdaQuery(SysFeatureSwitch.class)
                .eq(SysFeatureSwitch::getFeatureKey, "maintenance")
                .eq(SysFeatureSwitch::getDeleted, 0);
        SysFeatureSwitch maintenance = sysFeatureSwitchMapper.selectOne(maintenanceQuery);
        if (maintenance == null) return;

        if (enabled) {
            // Turn off all business switches
            LambdaQueryWrapper<SysFeatureSwitch> businessQuery = Wrappers.lambdaQuery(SysFeatureSwitch.class)
                    .eq(SysFeatureSwitch::getCategory, "business")
                    .eq(SysFeatureSwitch::getDeleted, 0);
            List<SysFeatureSwitch> businessSwitches = sysFeatureSwitchMapper.selectList(businessQuery);

            for (SysFeatureSwitch sw : businessSwitches) {
                boolean oldVal = sw.getEnabled();
                if (oldVal) {
                    sw.setEnabled(false);
                    sysFeatureSwitchMapper.updateById(sw);
                    sysConfigLogService.recordChange("feature", sw.getId(), sw.getFeatureKey(),
                            String.valueOf(oldVal), "false", "all");
                }
            }
            // Enable maintenance
            maintenance.setEnabled(true);
            sysFeatureSwitchMapper.updateById(maintenance);
            switchCache.clear();
            sysConfigLogService.recordChange("feature", maintenance.getId(), "maintenance",
                    "false", "true", "all");
        } else {
            // Restore all business switches
            LambdaQueryWrapper<SysFeatureSwitch> businessQuery = Wrappers.lambdaQuery(SysFeatureSwitch.class)
                    .eq(SysFeatureSwitch::getCategory, "business")
                    .eq(SysFeatureSwitch::getDeleted, 0);
            List<SysFeatureSwitch> businessSwitches = sysFeatureSwitchMapper.selectList(businessQuery);

            for (SysFeatureSwitch sw : businessSwitches) {
                boolean oldVal = sw.getEnabled();
                if (!oldVal) {
                    sw.setEnabled(true);
                    sysFeatureSwitchMapper.updateById(sw);
                    sysConfigLogService.recordChange("feature", sw.getId(), sw.getFeatureKey(),
                            String.valueOf(oldVal), "true", "all");
                }
            }
            // Disable maintenance
            maintenance.setEnabled(false);
            sysFeatureSwitchMapper.updateById(maintenance);
            switchCache.clear();
            sysConfigLogService.recordChange("feature", maintenance.getId(), "maintenance",
                    "true", "false", "all");
        }
    }

    private SysFeatureSwitchVO convertToVO(SysFeatureSwitch entity) {
        SysFeatureSwitchVO vo = new SysFeatureSwitchVO();
        vo.setId(entity.getId());
        vo.setFeatureKey(entity.getFeatureKey());
        vo.setFeatureName(entity.getFeatureName());
        vo.setCategory(entity.getCategory());
        vo.setEnabled(entity.getEnabled());
        vo.setDescription(entity.getDescription());
        vo.setSortOrder(entity.getSortOrder());
        vo.setCreatedAt(entity.getCreatedAt());
        vo.setUpdatedAt(entity.getUpdatedAt());
        return vo;
    }
}