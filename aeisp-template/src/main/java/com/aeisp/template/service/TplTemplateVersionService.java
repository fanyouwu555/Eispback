package com.aeisp.template.service;

import com.aeisp.template.dto.vo.TplTemplateVersionVO;
import com.aeisp.template.entity.TplTemplateVersion;

import java.util.List;

/**
 * 模板版本 Service 接口。
 *
 * @author AEISP Team
 */
public interface TplTemplateVersionService {

    /**
     * 根据 ID 获取版本。
     *
     * @param versionId 版本 ID
     * @return 版本实体
     */
    TplTemplateVersion getById(Long versionId);

    /**
     * 查询模板的所有版本。
     *
     * @param templateId 模板 ID
     * @return 版本 VO 列表
     */
    List<TplTemplateVersionVO> listVersions(Long templateId);

    /**
     * 删除版本（不能删除当前正在使用的版本）。
     *
     * @param versionId 版本 ID
     * @return 是否成功
     */
    boolean deleteVersion(Long versionId);
}
