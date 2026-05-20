package com.aeisp.system.service.impl;

import com.aeisp.common.constant.CommonConstants;
import com.aeisp.system.entity.SysConfig;
import com.aeisp.system.mapper.SysConfigMapper;
import com.aeisp.system.service.SysConfigService;
import com.aeisp.system.vo.SysConfigVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 系统配置 Service 实现类。
 *
 * @author AEISP Team
 */
@Service
@RequiredArgsConstructor
public class SysConfigServiceImpl implements SysConfigService {

    private final SysConfigMapper sysConfigMapper;

    @Override
    public String getConfigValue(String key, String environment) {
        SysConfig config = sysConfigMapper.selectByConfigKey(key, environment);
        return config != null ? config.getConfigValue() : null;
    }

    @Override
    public boolean updateConfig(String key, String value, String environment) {
        SysConfig config = sysConfigMapper.selectByConfigKey(key, environment);
        if (config == null) {
            return false;
        }
        config.setConfigValue(value);
        return sysConfigMapper.updateById(config) > 0;
    }

    @Override
    public List<SysConfigVO> listAll() {
        LambdaQueryWrapper<SysConfig> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(SysConfig::getDeleted, CommonConstants.DELETED_NO);
        wrapper.orderByAsc(SysConfig::getConfigKey);
        List<SysConfig> list = sysConfigMapper.selectList(wrapper);
        return list.stream()
                .map(SysConfigServiceImpl::convertToVO)
                .toList();
    }

    private static SysConfigVO convertToVO(SysConfig config) {
        SysConfigVO vo = new SysConfigVO();
        vo.setId(config.getId());
        vo.setConfigKey(config.getConfigKey());
        vo.setConfigValue(config.getConfigValue());
        vo.setDescription(config.getDescription());
        vo.setCreatedAt(config.getCreatedAt());
        vo.setUpdatedAt(config.getUpdatedAt());
        return vo;
    }
}
