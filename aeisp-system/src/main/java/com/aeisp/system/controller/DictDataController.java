package com.aeisp.system.controller;

import com.aeisp.common.Result;
import com.aeisp.common.constant.ResultCode;
import com.aeisp.system.annotation.OperationLog;
import com.aeisp.system.dto.CreateDictDataRequest;
import com.aeisp.system.dto.UpdateDictDataRequest;
import com.aeisp.system.entity.SysDictData;
import com.aeisp.system.service.DictDataService;
import com.aeisp.system.vo.DictDataVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/system/dict-data")
@RequiredArgsConstructor
public class DictDataController {

    private final DictDataService dictDataService;

    @PreAuthorize("hasAuthority('system:dict:list')")
    @GetMapping("/by-code/{dictCode}")
    public Result<List<DictDataVO>> listByDictCode(@PathVariable String dictCode) {
        return Result.success(dictDataService.listByDictCode(dictCode));
    }

    @PreAuthorize("hasAuthority('system:dict:manage')")
    @OperationLog(module = "数据字典", operation = "新增字典数据")
    @PostMapping
    public Result<Void> createData(@Valid @RequestBody CreateDictDataRequest request) {
        SysDictData data = new SysDictData();
        data.setDictCode(request.getDictCode());
        data.setItemLabel(request.getItemLabel());
        data.setItemValue(request.getItemValue());
        data.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);
        data.setStatus(request.getStatus() != null ? request.getStatus() : 1);
        data.setColor(request.getColor());
        data.setIsDefault(request.getIsDefault() != null ? request.getIsDefault() : 0);
        boolean success = dictDataService.createData(data);
        return success ? Result.success() : Result.error(ResultCode.INTERNAL_ERROR, "创建字典数据失败");
    }

    @PreAuthorize("hasAuthority('system:dict:manage')")
    @OperationLog(module = "数据字典", operation = "修改字典数据", sensitivity = 2)
    @PutMapping("/{id}")
    public Result<Void> updateData(@PathVariable Long id, @Valid @RequestBody UpdateDictDataRequest request) {
        SysDictData existing = dictDataService.getById(id);
        if (existing == null) {
            return Result.error(ResultCode.NOT_FOUND, "字典数据不存在");
        }
        if (request.getItemLabel() != null) existing.setItemLabel(request.getItemLabel());
        if (request.getItemValue() != null) existing.setItemValue(request.getItemValue());
        if (request.getSortOrder() != null) existing.setSortOrder(request.getSortOrder());
        if (request.getStatus() != null) existing.setStatus(request.getStatus());
        if (request.getColor() != null) existing.setColor(request.getColor());
        if (request.getIsDefault() != null) existing.setIsDefault(request.getIsDefault());
        boolean success = dictDataService.updateData(existing);
        return success ? Result.success() : Result.error(ResultCode.INTERNAL_ERROR, "更新字典数据失败");
    }

    @PreAuthorize("hasAuthority('system:dict:manage')")
    @OperationLog(module = "数据字典", operation = "删除字典数据")
    @DeleteMapping("/{id}")
    public Result<Void> deleteData(@PathVariable Long id) {
        boolean success = dictDataService.deleteData(id);
        return success ? Result.success() : Result.error(ResultCode.INTERNAL_ERROR, "删除字典数据失败");
    }
}