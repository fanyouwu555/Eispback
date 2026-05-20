package com.aeisp.system.mapper;

import com.aeisp.system.entity.SysDictType;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;

public interface SysDictTypeMapper extends BaseMapper<SysDictType> {

    @Select("SELECT * FROM sys_dict_type WHERE dict_code = #{dictCode} AND deleted = 0 LIMIT 1")
    SysDictType selectByDictCode(String dictCode);
}