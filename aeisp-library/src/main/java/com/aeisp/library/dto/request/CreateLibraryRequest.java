package com.aeisp.library.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class CreateLibraryRequest {

    @NotBlank(message = "资源名称不能为空")
    private String resourceName;

    private String description;

    @NotBlank(message = "版本号不能为空")
    private String versionNo;

    @NotNull(message = "库文件不能为空")
    private MultipartFile zipFile;

    private String changelog;
}
