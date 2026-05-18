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
     * ZIP 文件。
     */
    @NotNull(message = "ZIP 文件不能为空")
    private MultipartFile zipFile;
}
