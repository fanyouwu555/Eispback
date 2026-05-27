package com.aeisp.system.mapper;

import com.aeisp.system.entity.SysConfig;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 系统配置 Mapper 接口。
 *
 * @author AEISP Team
 */
public interface SysConfigMapper extends BaseMapper<SysConfig> {

    /**
     * 根据配置键查询配置值。
     *
     * @param configKey 配置键
     * @return 配置实体，不存在则返回 {@code null}
     */
    @Select("SELECT * FROM sys_config WHERE config_key = #{configKey} AND deleted = 0 AND (environment = #{environment} OR environment = 'all') ORDER BY environment DESC LIMIT 1")
    SysConfig selectByConfigKey(@Param("configKey") String configKey, @Param("environment") String environment);

    /**
     * 根据分类查询配置列表。
     *
     * @param category 配置分类
     * @return 配置列表
     */
    @Select("SELECT * FROM sys_config WHERE category = #{category} AND deleted = 0 ORDER BY config_key ASC")
    List<SysConfig> selectByCategory(@Param("category") String category);
}
