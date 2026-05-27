package com.aeisp.system.mapper;

import com.aeisp.system.entity.SysConfigLog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface SysConfigLogMapper extends BaseMapper<SysConfigLog> {

    @Select("SELECT * FROM sys_config_log WHERE config_type = #{configType} AND ref_id = #{refId} AND deleted = 0 ORDER BY created_at DESC")
    List<SysConfigLog> selectByRef(@Param("configType") String configType, @Param("refId") Long refId);
}