package com.aeisp.project.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateProjectRequest {
    @NotNull(message = "模板ID不能为空")
    private Long templateId;

    private String projectName;
}