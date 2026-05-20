package com.aeisp.system.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

@Data
public class UpdateDictTypeRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    @Size(max = 100, message = "字典名称长度不能超过 100")
    private String dictName;

    @Size(max = 100, message = "字典标识长度不能超过 100")
    private String dictCode;

    @Size(max = 500, message = "备注长度不能超过 500")
    private String description;

    private Integer status;

    private Integer isSystem;
}