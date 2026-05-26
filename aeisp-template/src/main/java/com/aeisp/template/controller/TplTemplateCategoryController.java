package com.aeisp.template.controller;

import com.aeisp.common.Result;
import com.aeisp.template.dto.TplTemplateCategoryDTO;
import com.aeisp.template.dto.TplTemplateCategoryVO;
import com.aeisp.template.service.TplTemplateCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "模板分类", description = "模板三级分类管理")
@RestController
@RequestMapping("/api/v1/template-categories")
@RequiredArgsConstructor
public class TplTemplateCategoryController {

    private final TplTemplateCategoryService categoryService;

    @Operation(summary = "分类树")
    @GetMapping("/tree")
    public Result<List<TplTemplateCategoryVO>> getTree() {
        return Result.success(categoryService.getTree());
    }

    @Operation(summary = "全部分类列表")
    @GetMapping("/list")
    public Result<List<TplTemplateCategoryVO>> listAll() {
        return Result.success(categoryService.listAll());
    }

    @Operation(summary = "分类详情")
    @GetMapping("/{id}")
    public Result<TplTemplateCategoryVO> getById(@PathVariable Long id) {
        return Result.success(categoryService.getById(id));
    }

    @PreAuthorize("hasAuthority('template:category:manage')")
    @Operation(summary = "新增分类")
    @PostMapping
    public Result<Long> create(@RequestBody TplTemplateCategoryDTO dto) {
        return Result.success(categoryService.create(dto));
    }

    @PreAuthorize("hasAuthority('template:category:manage')")
    @Operation(summary = "编辑分类")
    @PutMapping("/{id}")
    public Result<Boolean> update(@PathVariable Long id, @RequestBody TplTemplateCategoryDTO dto) {
        return Result.success(categoryService.update(id, dto));
    }

    @PreAuthorize("hasAuthority('template:category:manage')")
    @Operation(summary = "删除分类")
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(categoryService.delete(id));
    }
}