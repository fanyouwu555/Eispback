package com.aeisp.system.controller;

import com.aeisp.common.PageResult;
import com.aeisp.common.Result;
import com.aeisp.common.constant.ResultCode;
import com.aeisp.system.annotation.OperationLog;
import com.aeisp.system.dto.CreateDictTypeRequest;
import com.aeisp.system.dto.DictTypeQueryRequest;
import com.aeisp.system.dto.UpdateDictTypeRequest;
import com.aeisp.system.entity.SysDictType;
import com.aeisp.system.service.DictTypeService;
import com.aeisp.system.vo.DictDataVO;
import com.aeisp.system.vo.DictTypeVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/system/dict-types")
@RequiredArgsConstructor
public class DictTypeController {

    private final DictTypeService dictTypeService;

    @PreAuthorize("hasAuthority('system:dict:list')")
    @GetMapping
    public Result<PageResult<DictTypeVO>> listByPage(DictTypeQueryRequest request) {
        return Result.success(dictTypeService.listByPage(request));
    }

    @PreAuthorize("hasAuthority('system:dict:list')")
    @GetMapping("/all")
    public Result<List<DictTypeVO>> listAll() {
        return Result.success(dictTypeService.listAll());
    }

    @PreAuthorize("hasAuthority('system:dict:list')")
    @GetMapping("/{id}")
    public Result<DictTypeVO> getById(@PathVariable Long id) {
        SysDictType type = dictTypeService.getById(id);
        if (type == null) {
            return Result.error(ResultCode.NOT_FOUND, "字典类型不存在");
        }
        DictTypeVO vo = new DictTypeVO();
        vo.setId(type.getId());
        vo.setDictName(type.getDictName());
        vo.setDictCode(type.getDictCode());
        vo.setDescription(type.getDescription());
        vo.setStatus(type.getStatus());
        vo.setIsSystem(type.getIsSystem());
        vo.setCreatedAt(type.getCreatedAt());
        vo.setUpdatedAt(type.getUpdatedAt());
        return Result.success(vo);
    }

    @PreAuthorize("hasAuthority('system:dict:manage')")
    @OperationLog(module = "数据字典", operation = "新增字典类型")
    @PostMapping
    public Result<Void> createType(@Valid @RequestBody CreateDictTypeRequest request) {
        SysDictType type = new SysDictType();
        type.setDictName(request.getDictName());
        type.setDictCode(request.getDictCode());
        type.setDescription(request.getDescription());
        type.setStatus(request.getStatus() != null ? request.getStatus() : 1);
        type.setIsSystem(request.getIsSystem() != null ? request.getIsSystem() : 0);
        boolean success = dictTypeService.createType(type);
        return success ? Result.success() : Result.error(ResultCode.INTERNAL_ERROR, "创建字典类型失败");
    }

    @PreAuthorize("hasAuthority('system:dict:manage')")
    @OperationLog(module = "数据字典", operation = "修改字典类型", sensitivity = 2)
    @PutMapping("/{id}")
    public Result<Void> updateType(@PathVariable Long id, @Valid @RequestBody UpdateDictTypeRequest request) {
        SysDictType existing = dictTypeService.getById(id);
        if (existing == null) {
            return Result.error(ResultCode.NOT_FOUND, "字典类型不存在");
        }
        if (request.getDictName() != null) existing.setDictName(request.getDictName());
        if (request.getDictCode() != null) existing.setDictCode(request.getDictCode());
        if (request.getDescription() != null) existing.setDescription(request.getDescription());
        if (request.getStatus() != null) existing.setStatus(request.getStatus());
        if (request.getIsSystem() != null) existing.setIsSystem(request.getIsSystem());
        boolean success = dictTypeService.updateType(existing);
        return success ? Result.success() : Result.error(ResultCode.INTERNAL_ERROR, "更新字典类型失败");
    }

    @PreAuthorize("hasAuthority('system:dict:manage')")
    @OperationLog(module = "数据字典", operation = "删除字典类型", sensitivity = 2)
    @DeleteMapping("/{id}")
    public Result<Void> deleteType(@PathVariable Long id) {
        boolean success = dictTypeService.deleteType(id);
        return success ? Result.success() : Result.error(ResultCode.INTERNAL_ERROR, "删除字典类型失败，系统内置类型不可删除");
    }

    @PreAuthorize("hasAuthority('system:dict:list')")
    @GetMapping("/{dictCode}/data")
    public Result<List<DictDataVO>> getDictData(@PathVariable String dictCode) {
        return Result.success(dictTypeService.getDictData(dictCode));
    }
}