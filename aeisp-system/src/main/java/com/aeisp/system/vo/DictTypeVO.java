package com.aeisp.system.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class DictTypeVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String dictName;

    private String dictCode;

    private String description;

    private Integer status;

    private Integer isSystem;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}