package com.aeisp.system.mapper;

import com.aeisp.system.entity.SysDictData;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface SysDictDataMapper extends BaseMapper<SysDictData> {

    @Select("SELECT * FROM sys_dict_data WHERE dict_code = #{dictCode} AND status = 1 AND deleted = 0 ORDER BY sort_order ASC, id ASC")
    List<SysDictData> selectByDictCode(String dictCode);
}