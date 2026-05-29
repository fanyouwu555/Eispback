package com.aeisp.library.mapper;

import com.aeisp.library.entity.LibResource;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface LibResourceMapper extends BaseMapper<LibResource> {

    @Select("SELECT MAX(resource_code) FROM lib_resource WHERE resource_code LIKE CONCAT(#{prefix}, '%') AND deleted = 0")
    String selectMaxResourceCode(@Param("prefix") String prefix);
}
