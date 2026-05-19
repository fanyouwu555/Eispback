package com.aeisp.template.mapper;

import com.aeisp.template.entity.TplTemplatePreviewImage;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 模板预览图片 Mapper 接口。
 *
 * @author AEISP Team
 */
public interface TplTemplatePreviewImageMapper extends BaseMapper<TplTemplatePreviewImage> {

    /**
     * 根据模板 ID 查询预览图片列表。
     *
     * @param templateId 模板 ID
     * @return 预览图片列表
     */
    List<TplTemplatePreviewImage> selectByTemplateId(@Param("templateId") Long templateId);
}
