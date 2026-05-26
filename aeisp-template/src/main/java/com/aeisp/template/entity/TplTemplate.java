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
     * 模板唯一编码。
     */
    private String templateCode;

    /**
     * 模板名称。
     */
    private String templateName;

    
    /**
     * 模板简介。
     */
    private String description;

    /**
     * 预览图片 URL（封面图，管理端展示用）。
     */
    private String previewImage;

    /**
     * 缩略图 URL（客户端展示用）。
     */
    private String thumbnail;

    /**
     * 排序权重，越大越靠前。
     */
    private Integer sortWeight;

    /**
     * 状态：1-上架，2-下架。
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

    /**
     * 累计项目数。
     */
    private Integer projectCount;

    /**
     * 优化标记：0-正常，1-需要优化。
     */
    private Integer optimizationFlag;

    /**
     * 存储路径。
     */
    private String storagePath;

    /**
     * 难度系数。
     */
    private Integer difficulty;

    /**
     * 顶级分类 ID。
     */
    private Long topCategoryId;

    /**
     * 一级分类 ID。
     */
    private Long firstCategoryId;

    /**
     * 二级分类 ID。
     */
    private Long secondCategoryId;

    /**
     * 是否付费：0-免费，1-付费。
     */
    private Integer isPaid;

    /**
     * 费用类型。
     */
    private String feeType;

    /**
     * 价格（元）。
     */
    private java.math.BigDecimal price;

    /**
     * 上线时间。
     */
    private java.time.LocalDateTime onlineTime;

    /**
     * 有效截止时间。
     */
    private java.time.LocalDateTime validTime;

    /**
     * 创作者名称。
     */
    private String creator;

    /**
     * 创作时间。
     */
    private java.time.LocalDate produceDate;

    /**
     * 下载次数。
     */
    private Long downloadCount;

    /**
     * 收藏数。
     */
    private Long favoriteCount;

    /**
     * 访问量。
     */
    private Long visitCount;

    /**
     * 详细描述。
     */
    private String detailDesc;

    /**
     * 违规原因。
     */
    private String violationReason;

    /**
     * 创建人 ID。
     */
    private Long createdBy;

    /**
     * 更新人 ID。
     */
    private Long updatedBy;
}
