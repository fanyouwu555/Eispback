package com.aeisp.library.entity;

import com.aeisp.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 库资源版本表实体类。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("lib_resource_version")
public class LibResourceVersion extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private Long resourceId;
    private String versionNo;
    private String filePath;
    private String storageUrl;
    private Long fileSize;
    private String fileHash;
    private String changelog;
}
