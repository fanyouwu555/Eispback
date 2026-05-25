package com.aeisp.project.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateProjectRequest {
    @NotBlank(message = "项目名称不能为空")
    private String projectName;
}