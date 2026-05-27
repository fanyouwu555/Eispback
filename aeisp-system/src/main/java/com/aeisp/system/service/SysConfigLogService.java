package com.aeisp.system.service;

import com.aeisp.system.vo.SysConfigLogVO;

import java.util.List;

public interface SysConfigLogService {
    void recordChange(String configType, Long refId, String configKey, String oldValue, String newValue, String environment);
    List<SysConfigLogVO> listByRef(String configType, Long refId);
}