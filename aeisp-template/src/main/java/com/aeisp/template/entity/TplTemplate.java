package com.aeisp.template.entity;

import com.aeisp.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 模板主表实体类。
 *
 * <p>对应数据库表 {@code tpl_template}，存储编程模板的基础信息、状态、排序及当前版本关联。</p>
 *
 * @author AEISP Team
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tpl_template")
public class TplTemplate extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 模板名称。
     */
    private String templateName;

    /**
     * 适用场景：teaching-教学, competition-竞赛, practice-实战。
     */
    private String scenario;

    /**
     * 模板简介。
     */
    private String description;

    /**
     * 预览图片 URL（相对路径）。
     */
    private String previewImage;

    /**
     * 排序权重，越大越靠前。
     */
    private Integer sortWeight;

    /**
     * 状态：0-下线，1-上线。
     */
    private Integer status;

    /**
     * 当前版本 ID（关联 tpl_template_version）。
     */
    private Long currentVersionId;

    /**
     * 使用次数（新建项目数）。
     */
    private Long usageCount;
}
