package com.aeisp.model.service;

import com.aeisp.common.PageResult;
import com.aeisp.model.dto.ModelDTO;
import com.aeisp.model.dto.ModelQueryRequest;
import com.aeisp.model.dto.ModelTestRequest;
import com.aeisp.model.dto.ModelUsageStatVO;
import com.aeisp.model.dto.ModelVO;

import java.util.List;

/**
 * 大模型管理服务接口。
 *
 * <p>提供模型的增删改查、状态切换、排序调整、在线测试及用量统计能力。</p>
 *
 * @author AEISP Team
 */
public interface ModelService {

    /**
     * 新增模型。
     *
     * @param dto 模型数据
     * @return 是否成功
     */
    boolean addModel(ModelDTO dto);

    /**
     * 编辑模型。
     *
     * @param id  模型 ID
     * @param dto 模型数据
     * @return 是否成功
     */
    boolean updateModel(Long id, ModelDTO dto);

    /**
     * 删除模型。
     *
     * @param id 模型 ID
     * @return 是否成功
     */
    boolean deleteModel(Long id);

    /**
     * 切换模型启用/禁用状态。
     *
     * @param id     模型 ID
     * @param status 目标状态：0-禁用，1-启用
     * @return 是否成功
     */
    boolean toggleStatus(Long id, Integer status);

    /**
     * 更新模型排序。
     *
     * @param id        模型 ID
     * @param sortOrder 排序值
     * @return 是否成功
     */
    boolean updateSortOrder(Long id, Integer sortOrder);

    /**
     * 获取模型详情。
     *
     * @param id 模型 ID
     * @return 模型视图对象
     */
    ModelVO getModel(Long id);

    /**
     * 分页查询模型列表。
     *
     * @param request 查询条件
     * @return 分页结果
     */
    PageResult<ModelVO> listModels(ModelQueryRequest request);

    /**
     * 模型在线测试。
     *
     * @param request 测试请求
     * @return 是否测试通过
     */
    boolean testModel(ModelTestRequest request);

    /**
     * 获取模型用量统计。
     *
     * @param modelId 模型 ID
     * @return 用量统计列表
     */
    List<ModelUsageStatVO> getUsageStats(Long modelId);
}
