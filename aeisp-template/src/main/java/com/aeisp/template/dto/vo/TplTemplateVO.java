package com.aeisp.template.dto.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 模板列表项 VO。
 *
 * @author AEISP Team
 */
@Data
public class TplTemplateVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 模板 ID。
     */
    private Long id;

    /**
     * 模板编码。
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
     * 预览图片 URL。
     */
    private String previewImage;

    /**
     * 排序权重。
     */
    private Integer sortWeight;

    /**
     * 状态：0-下线，1-上线。
     */
    private Integer status;

    /**
     * 使用次数。
     */
    private Long usageCount;

    /**
     * 当前版本号。
     */
    private String currentVersionNo;

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
    private String onlineTime;

    /**
     * 有效截止时间。
     */
    private String validTime;

    /**
     * 创作者名称。
     */
    private String creator;

    /**
     * 创作时间。
     */
    private String produceDate;

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
     * 难度等级。
     */
    private Integer difficulty;

    /**
     * 分类路径，如 "常用模板-小车类-巡线"。
     */
    private String categoryPath;

    /**
     * 资源路径（当前版本的 ZIP 下载 URL）。
     */
    private String resourceUrl;

    /**
     * 更新时间。
     */
    private String updatedAt;

    /**
     * 违规原因。
     */
    private String violationReason;
}
