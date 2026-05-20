package com.aeisp.system.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class DictDataQueryRequest extends PageParam {

    private static final long serialVersionUID = 1L;

    private String dictCode;

    private Integer status;
}