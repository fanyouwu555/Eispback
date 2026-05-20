package com.aeisp.template.service;

import com.aeisp.common.PageResult;
import com.aeisp.template.dto.request.CreateTemplateRequest;
import com.aeisp.template.dto.request.TemplateQueryRequest;
import com.aeisp.template.dto.request.UpdateTemplateRequest;
import com.aeisp.template.dto.vo.TplTemplateDetailVO;
import com.aeisp.template.dto.vo.TplTemplateVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 模板主表 Service 接口。
 *
 * @author AEISP Team
 */
public interface TplTemplateService {

    boolean createTemplate(CreateTemplateRequest request);

    boolean updateTemplateInfo(Long templateId, UpdateTemplateRequest request);

    boolean uploadNewVersion(Long templateId, MultipartFile zipFile, String versionNo, String changelog);

    boolean rollbackVersion(Long templateId, Long versionId);

    boolean toggleStatus(Long templateId, Integer status);

    boolean deleteTemplate(Long templateId);

    PageResult<TplTemplateVO> listTemplates(TemplateQueryRequest request);

    TplTemplateDetailVO getDetail(Long templateId);

    List<TplTemplateVO> listOnlineTemplates(String scenario);

    /**
     * 查询上线模板详情（前端使用）。
     *
     * @param templateId 模板 ID
     * @return 模板详情
     * @throws com.aeisp.common.exception.BizException 模板不存在或未上线时抛出
     */
    TplTemplateDetailVO getPublicDetail(Long templateId);

    /**
     * 获取模板使用统计数据。
     *
     * @return 统计结果
     */
    java.util.Map<String, Object> getStatistics();
}
