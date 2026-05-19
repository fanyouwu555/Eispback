package com.aeisp.template.entity;

import com.aeisp.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 模板预览图片实体类。
 *
 * <p>对应数据库表 {@code tpl_template_preview_image}，存储模板的预览图片列表。</p>
 *
 * @author AEISP Team
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tpl_template_preview_image")
public class TplTemplatePreviewImage extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 关联模板 ID。
     */
    private Long templateId;

    /**
     * 预览图片 URL。
     */
    private String imageUrl;

    /**
     * 排序顺序。
     */
    private Integer sortOrder;
}
