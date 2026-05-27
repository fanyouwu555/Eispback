package com.aeisp.system.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SysConfigLogVO {
    private Long id;
    private String configType;
    private Long refId;
    private String configKey;
    private String oldValue;
    private String newValue;
    private String environment;
    private Long operatedBy;
    private String operatorName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime operatedAt;
}