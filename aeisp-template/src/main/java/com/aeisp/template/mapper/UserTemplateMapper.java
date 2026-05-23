package com.aeisp.template.mapper;

import com.aeisp.template.entity.UserTemplate;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 用户模板权限 Mapper。
 */
public interface UserTemplateMapper extends BaseMapper<UserTemplate> {

    /**
     * 查询用户对指定模板的有效权限记录。
     */
    @Select("SELECT * FROM usr_user_template WHERE user_id = #{userId} AND template_id = #{templateId} " +
            "AND (expire_at IS NULL OR expire_at > NOW()) LIMIT 1")
    UserTemplate selectValidByUserAndTemplate(@Param("userId") Long userId, @Param("templateId") Long templateId);
}
