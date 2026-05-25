package com.aeisp.template.service;

import com.aeisp.common.PageResult;
import com.aeisp.template.dto.TplTemplateCategoryVO;
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

    /**
     * 上传新版本，可选同步更新模板主表字段（onlineTime/validTime/difficulty/isPaid/feeType/price）。
     *
     * @param templateId 模板 ID
     * @param zipFile     ZIP 文件
     * @param versionNo   版本号
     * @param changelog   更新日志（版本描述）
     * @param onlineTime  上线时间（ISO 格式，空则不更新模板字段）
     * @param validTime   有效截止时间（ISO 格式，空则不更新）
     * @param difficulty  难度等级（null 则不更新）
     * @param isPaid      是否付费（null 则不更新）
     * @param feeType     费用类型（空则不更新）
     * @param price       价格（null 则不更新）
     * @return true 表示成功
     */
    boolean uploadNewVersion(Long templateId, MultipartFile zipFile, String versionNo,
                             String changelog, String onlineTime, String validTime,
                             Integer difficulty, Integer isPaid, String feeType, java.math.BigDecimal price);

    boolean rollbackVersion(Long templateId, Long versionId);

    boolean toggleStatus(Long templateId, Integer status);

    boolean deleteTemplate(Long templateId);

    PageResult<TplTemplateVO> listTemplates(TemplateQueryRequest request);

    TplTemplateDetailVO getDetail(Long templateId);

    List<TplTemplateVO> listOnlineTemplates();

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

    /**
     * 标记模板违规并下架。
     *
     * @param templateId 模板 ID
     * @param reason 违规原因
     * @return 是否成功
     */
    boolean markViolation(Long templateId, String reason);

    /**
     * 模板下载次数 +1。
     *
     * @param templateId 模板 ID
     */
    void incrementDownloadCount(Long templateId);

    /**
     * 查询分类树并挂载上架模板（客户端使用）。
     *
     * @return 分类树，三级分类节点下包含模板列表
     */
    List<TplTemplateCategoryVO> listCategoryTreeWithTemplates();
}
