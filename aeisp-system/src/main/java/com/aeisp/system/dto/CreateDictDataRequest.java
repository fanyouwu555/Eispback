package com.aeisp.system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

@Data
public class CreateDictDataRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "字典标识不能为空")
    @Size(max = 100, message = "字典标识长度不能超过 100")
    private String dictCode;

    @NotBlank(message = "数据标签不能为空")
    @Size(max = 100, message = "数据标签长度不能超过 100")
    private String itemLabel;

    @NotBlank(message = "数据值不能为空")
    @Size(max = 100, message = "数据值长度不能超过 100")
    private String itemValue;

    private Integer sortOrder;

    private Integer status;

    @Size(max = 50, message = "颜色长度不能超过 50")
    private String color;

    private Integer isDefault;
}