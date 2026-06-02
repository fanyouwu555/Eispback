package com.aeisp.template.dto.request;

import lombok.Data;

import java.util.List;

/**
 * 设置模板关联库资源请求。
 *
 * @author AEISP Team
 */
@Data
public class SetTemplateLibrariesRequest {

    /**
     * 库资源 ID 列表。
     */
    private List<Long> libraryIds;
}
