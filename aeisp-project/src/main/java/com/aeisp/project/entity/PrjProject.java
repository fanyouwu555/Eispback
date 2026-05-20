package com.aeisp.project.entity;

import com.aeisp.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("prj_project")
public class PrjProject extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private String projectName;
    private String description;
    private Long userId;
    private Long templateId;
    private Long templateVersionId;
    private Integer status;
    private Integer isPinned;
    private Long runTimeSeconds;
    private String projectConfig;
    private String remark;
    private LocalDateTime archivedAt;
}