package com.aeisp.system.service;

import com.aeisp.system.vo.SysConfigVO;

import java.util.List;

/**
 * 系统配置 Service 接口。
 *
 * @author AEISP Team
 */
public interface SysConfigService {

    /**
     * 根据配置键获取配置值。
     *
     * @param key 配置键
     * @return 配置值，不存在则返回 {@code null}
     */
    String getConfigValue(String key);

    /**
     * 更新配置值。
     *
     * @param key   配置键
     * @param value 配置值
     * @return {@code true} 更新成功
     */
    boolean updateConfig(String key, String value);

    /**
     * 查询所有配置列表。
     *
     * @return 配置 VO 列表
     */
    List<SysConfigVO> listAll();
}
