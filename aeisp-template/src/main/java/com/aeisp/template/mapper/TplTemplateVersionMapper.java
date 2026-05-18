package com.aeisp.template.mapper;

import com.aeisp.template.entity.TplTemplateVersion;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 模板版本表 Mapper 接口。
 *
 * @author AEISP Team
 */
public interface TplTemplateVersionMapper extends BaseMapper<TplTemplateVersion> {

    /**
     * 查询模板的所有版本。
     *
     * @param templateId 模板 ID
     * @return 版本列表
     */
    List<TplTemplateVersion> selectByTemplateId(@Param("templateId") Long templateId);
}
