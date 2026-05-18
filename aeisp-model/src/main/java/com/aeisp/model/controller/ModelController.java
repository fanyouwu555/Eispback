package com.aeisp.model.controller;

import com.aeisp.common.PageResult;
import com.aeisp.common.Result;
import com.aeisp.model.dto.ModelDTO;
import com.aeisp.model.dto.ModelQueryRequest;
import com.aeisp.model.dto.ModelTestRequest;
import com.aeisp.model.dto.ModelUsageStatVO;
import com.aeisp.model.dto.ModelVO;
import com.aeisp.model.service.ModelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 大模型管理控制器。
 *
 * <p>提供模型增删改查、状态管理、在线测试及用量统计接口。</p>
 *
 * @author AEISP Team
 */
@Tag(name = "大模型管理", description = "模型配置与调用管理")
@RestController
@RequestMapping("/api/v1/models")
@RequiredArgsConstructor
public class ModelController {

    private final ModelService modelService;

    /**
     * 新增模型。
     *
     * @param dto 模型数据
     * @return 操作结果
     */
    @Operation(summary = "新增模型")
    @PostMapping
    public Result<Boolean> addModel(@Validated @RequestBody ModelDTO dto) {
        return Result.success(modelService.addModel(dto));
    }

    /**
     * 编辑模型。
     *
     * @param id  模型 ID
     * @param dto 模型数据
     * @return 操作结果
     */
    @Operation(summary = "编辑模型")
    @PutMapping("/{id}")
    public Result<Boolean> updateModel(@PathVariable Long id, @Validated @RequestBody ModelDTO dto) {
        return Result.success(modelService.updateModel(id, dto));
    }

    /**
     * 删除模型。
     *
     * @param id 模型 ID
     * @return 操作结果
     */
    @Operation(summary = "删除模型")
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteModel(@PathVariable Long id) {
        return Result.success(modelService.deleteModel(id));
    }

    /**
     * 分页查询模型列表。
     *
     * @param request 查询条件
     * @return 分页结果
     */
    @Operation(summary = "模型列表")
    @GetMapping
    public Result<PageResult<ModelVO>> listModels(ModelQueryRequest request) {
        return Result.success(modelService.listModels(request));
    }

    /**
     * 获取模型详情。
     *
     * @param id 模型 ID
     * @return 模型详情
     */
    @Operation(summary = "模型详情")
    @GetMapping("/{id}")
    public Result<ModelVO> getModel(@PathVariable Long id) {
        return Result.success(modelService.getModel(id));
    }

    /**
     * 切换模型状态。
     *
     * @param id     模型 ID
     * @param status 目标状态
     * @return 操作结果
     */
    @Operation(summary = "启用/禁用模型")
    @PostMapping("/{id}/status")
    public Result<Boolean> toggleStatus(@PathVariable Long id, @RequestParam Integer status) {
        return Result.success(modelService.toggleStatus(id, status));
    }

    /**
     * 模型在线测试。
     *
     * @param id      模型 ID
     * @param request 测试请求
     * @return 测试结果
     */
    @Operation(summary = "模型在线测试")
    @PostMapping("/{id}/test")
    public Result<Boolean> testModel(@PathVariable Long id, @RequestBody ModelTestRequest request) {
        request.setModelId(id);
        return Result.success(modelService.testModel(request));
    }

    /**
     * 获取模型用量统计。
     *
     * @param id 模型 ID
     * @return 用量统计
     */
    @Operation(summary = "模型用量统计")
    @GetMapping("/{id}/stats")
    public Result<List<ModelUsageStatVO>> getUsageStats(@PathVariable Long id) {
        return Result.success(modelService.getUsageStats(id));
    }
}
