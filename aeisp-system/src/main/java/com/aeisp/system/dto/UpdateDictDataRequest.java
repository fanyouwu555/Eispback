package com.aeisp.system.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

@Data
public class UpdateDictDataRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String dictCode;

    @Size(max = 100, message = "数据标签长度不能超过 100")
    private String itemLabel;

    @Size(max = 100, message = "数据值长度不能超过 100")
    private String itemValue;

    private Integer sortOrder;

    private Integer status;

    @Size(max = 50, message = "颜色长度不能超过 50")
    private String color;

    private Integer isDefault;
}