package com.aeisp.system.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class DictDataVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String dictCode;

    private String itemLabel;

    private String itemValue;

    private Integer sortOrder;

    private Integer status;

    private String color;

    private Integer isDefault;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}