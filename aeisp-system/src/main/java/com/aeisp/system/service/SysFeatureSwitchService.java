package com.aeisp.system.service;

import com.aeisp.system.vo.SysFeatureSwitchVO;

import java.util.List;

public interface SysFeatureSwitchService {
    List<SysFeatureSwitchVO> listAll();
    void toggle(Long id);
    void toggleMaintenance(Boolean enabled);
}