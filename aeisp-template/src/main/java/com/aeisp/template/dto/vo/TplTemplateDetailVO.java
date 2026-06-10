package com.aeisp.template.dto.vo;

import com.aeisp.common.dto.FileNodeVO;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 模板详情 VO。
 *
 * @author AEISP Team
 */
@Data
public class TplTemplateDetailVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 模板 ID。
     */
    private Long id;

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
     * 缩略图 URL。
     */
    private String thumbnail;

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
     * 当前版本信息。
     */
    private TplTemplateVersionVO currentVersion;

    /**
     * 历史版本列表。
     */
    private List<TplTemplateVersionVO> historyVersions;

    /**
     * 文件结构。
     */
    private List<FileNodeVO> fileTree;

    private Long topCategoryId;
    private Long firstCategoryId;
    private Long secondCategoryId;
    private Integer isPaid;
    private String feeType;
    private java.math.BigDecimal price;
    private String onlineTime;
    private String validTime;
    private String creator;
    private String produceDate;
    private Long downloadCount;
    private Long favoriteCount;
    private Long visitCount;
    private String detailDesc;

    /**
     * 难度等级。
     */
    private Integer difficulty;

    private String violationReason;
}
