package com.aeisp.template.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

/**
 * 创建模板请求。
 *
 * @author AEISP Team
 */
@Data
public class CreateTemplateRequest {

    /**
     * 模板名称。
     */
    @NotBlank(message = "模板名称不能为空")
    private String templateName;

    /**
     * 适用场景。
     */
    @NotBlank(message = "适用场景不能为空")
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
    @NotNull(message = "排序权重不能为空")
    private Integer sortWeight;

    /**
     * 首个版本号。
     */
    @NotBlank(message = "版本号不能为空")
    private String versionNo;

    /**
     * 更新日志。
     */
    private String changelog;

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
     * 上线时间（ISO 格式字符串）。
     */
    private String onlineTime;

    /**
     * 有效截止时间（ISO 格式字符串）。
     */
    private String validTime;

    /**
     * 创作者名称。
     */
    private String creator;

    /**
     * 创作时间（ISO 日期格式）。
     */
    private String produceDate;

    /**
     * 详细描述。
     */
    private String detailDesc;

    /**
     * 难度等级。
     */
    private Integer difficulty;

    /**
     * ZIP 文件。
     */
    @NotNull(message = "ZIP 文件不能为空")
    private MultipartFile zipFile;
}
