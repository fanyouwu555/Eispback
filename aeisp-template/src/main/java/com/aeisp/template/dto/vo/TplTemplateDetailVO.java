package com.aeisp.template.dto.vo;

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
     * 适用场景。
     */
    private String scenario;

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
}
