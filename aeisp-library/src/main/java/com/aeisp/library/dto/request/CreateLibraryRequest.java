package com.aeisp.library.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateLibraryRequest {

    @NotBlank(message = "资源名称不能为空")
    private String resourceName;

    private String description;
}
