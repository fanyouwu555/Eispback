package com.aeisp.project.controller;

import com.aeisp.common.PageResult;
import com.aeisp.common.Result;
import com.aeisp.project.dto.request.ProjectQueryRequest;
import com.aeisp.project.dto.vo.PrjProjectVO;
import com.aeisp.project.service.PrjProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
@Tag(name = "项目管理", description = "用户项目的全生命周期管理，包括列表查询、归档、删除")
public class PrjProjectController {

    private final PrjProjectService projectService;

    @PreAuthorize("hasAuthority('project:list')")
    @GetMapping
    @Operation(summary = "项目列表", description = "分页查询项目列表，支持关键字、用户、时间范围、状态筛选")
    public Result<PageResult<PrjProjectVO>> listProjects(@Valid ProjectQueryRequest request) {
        return Result.success(projectService.listProjects(request));
    }

    @PreAuthorize("hasAuthority('project:list')")
    @GetMapping("/{id}")
    @Operation(summary = "项目详情", description = "获取单个项目的详细信息")
    public Result<PrjProjectVO> getDetail(@PathVariable Long id) {
        return Result.success(projectService.getDetail(id));
    }

    @PreAuthorize("hasAuthority('project:manage')")
    @PostMapping("/{id}/archive")
    @Operation(summary = "强制归档", description = "将指定项目强制归档")
    public Result<Boolean> archiveProject(@PathVariable Long id) {
        return Result.success(projectService.archiveProject(id));
    }

    @PreAuthorize("hasAuthority('project:manage')")
    @DeleteMapping("/{id}")
    @Operation(summary = "强制删除", description = "逻辑删除指定项目")
    public Result<Boolean> deleteProject(@PathVariable Long id) {
        return Result.success(projectService.deleteProject(id));
    }
}