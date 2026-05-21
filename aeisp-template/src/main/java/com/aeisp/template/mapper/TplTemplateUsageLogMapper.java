package com.aeisp.template.mapper;

import com.aeisp.template.entity.TplTemplateUsageLog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 模板使用记录 Mapper 接口。
 *
 * @author AEISP Team
 */
public interface TplTemplateUsageLogMapper extends BaseMapper<TplTemplateUsageLog> {

    @Select("SELECT * FROM usr_template_usage_log WHERE user_id = #{userId} ORDER BY created_at DESC")
    List<TplTemplateUsageLog> selectByUserId(@Param("userId") Long userId);
}
