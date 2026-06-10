package com.aeisp.library.entity;

import com.aeisp.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 库资源实体类。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("lib_resource")
public class LibResource extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private String resourceName;
    private String description;
}
