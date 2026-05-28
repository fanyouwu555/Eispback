package com.aeisp.system.service;

import com.aeisp.system.vo.SysFeatureSwitchVO;

import java.util.List;

public interface SysFeatureSwitchService {
    List<SysFeatureSwitchVO> listAll();
    void toggle(Long id);
    void toggleMaintenance(Boolean enabled);

    /**
     * 检查指定功能开关是否启用。
     *
     * @param featureKey 开关键名
     * @return 开关存在且启用返回 {@code true}；开关不存在默认返回 {@code true}（避免误关闭）
     */
    boolean isEnabled(String featureKey);
}