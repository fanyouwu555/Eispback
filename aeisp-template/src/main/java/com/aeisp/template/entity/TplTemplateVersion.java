package com.aeisp.template.entity;

import com.aeisp.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 模板版本表实体类。
 *
 * <p>对应数据库表 {@code tpl_template_version}，存储模板各版本的文件路径、配置内容及更新日志。</p>
 *
 * @author AEISP Team
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tpl_template_version")
public class TplTemplateVersion extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 关联模板 ID。
     */
    private Long templateId;

    /**
     * 版本号，如 "1.0.0"。
     */
    private String versionNo;

    /**
     * ZIP 文件存储路径（相对路径）。
     */
    private String filePath;

    /**
     * 存储 URL。
     */
    private String storageUrl;

    /**
     * 文件大小（字节）。
     */
    private Long fileSize;

    /**
     * 文件哈希值。
     */
    private String fileHash;

    /**
     * 是否重大更新：0-否，1-是。
     */
    private Integer isMajorUpdate;

    /**
     * 模板配置文件内容（JSON）。
     */
    private String configContent;

    /**
     * 更新日志。
     */
    private String changelog;
}
