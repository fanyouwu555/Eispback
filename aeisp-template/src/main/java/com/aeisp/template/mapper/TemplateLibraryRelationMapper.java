package com.aeisp.template.mapper;

import com.aeisp.template.entity.TemplateLibraryRelation;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface TemplateLibraryRelationMapper extends BaseMapper<TemplateLibraryRelation> {

    /**
     * 查询模板关联的有效库资源 ID（排除已逻辑删除的库资源）。
     *
     * @param templateId 模板 ID
     * @return 库资源 ID 列表
     */
    @Select("SELECT library_id FROM tpl_template_library " +
            "WHERE template_id = #{templateId} " +
            "AND library_id IN (SELECT id FROM lib_resource WHERE deleted = 0)")
    List<Long> selectValidLibraryIds(@Param("templateId") Long templateId);
}
