package com.aeisp.system.service.impl;

import com.aeisp.common.util.SecurityUtils;
import com.aeisp.system.entity.SysConfigLog;
import com.aeisp.system.mapper.SysConfigLogMapper;
import com.aeisp.system.service.SysConfigLogService;
import com.aeisp.system.vo.SysConfigLogVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class SysConfigLogServiceImpl implements SysConfigLogService {

    private final SysConfigLogMapper sysConfigLogMapper;

    @Override
    public void recordChange(String configType, Long refId, String configKey, String oldValue, String newValue, String environment) {
        SysConfigLog log = new SysConfigLog();
        log.setConfigType(configType);
        log.setRefId(refId);
        log.setConfigKey(configKey);
        log.setOldValue(oldValue);
        log.setNewValue(newValue);
        log.setEnvironment(environment);
        log.setOperatedBy(SecurityUtils.getUserId());
        sysConfigLogMapper.insert(log);
    }

    @Override
    public List<SysConfigLogVO> listByRef(String configType, Long refId) {
        List<SysConfigLog> logs = sysConfigLogMapper.selectByRef(configType, refId);
        return logs.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    private SysConfigLogVO convertToVO(SysConfigLog entity) {
        SysConfigLogVO vo = new SysConfigLogVO();
        vo.setId(entity.getId());
        vo.setConfigType(entity.getConfigType());
        vo.setRefId(entity.getRefId());
        vo.setConfigKey(entity.getConfigKey());
        vo.setOldValue(entity.getOldValue());
        vo.setNewValue(entity.getNewValue());
        vo.setEnvironment(entity.getEnvironment());
        vo.setOperatedBy(entity.getOperatedBy());
        vo.setOperatedAt(entity.getCreatedAt());
        return vo;
    }
}