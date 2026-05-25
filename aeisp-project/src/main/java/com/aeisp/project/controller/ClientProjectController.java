package com.aeisp.project.controller;

import com.aeisp.common.PageResult;
import com.aeisp.common.Result;
import com.aeisp.common.security.CustomUserDetails;
import com.aeisp.project.dto.request.CreateProjectRequest;
import com.aeisp.project.dto.request.UpdateProjectRequest;
import com.aeisp.project.dto.vo.ClientProjectVO;
import com.aeisp.project.service.PrjProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/client/projects")
@RequiredArgsConstructor
@Tag(name = "客户端项目管理", description = "Unity 客户端项目创建、上传、更新、删除")
public class ClientProjectController {

    private final PrjProjectService projectService;

    @PostMapping
    @Operation(summary = "创建项目", description = "用户创建新项目，返回 projectId 和资源 URL")
    public Result<ClientProjectVO> createProject(
            @AuthenticationPrincipal CustomUserDetails user,
            @Valid @RequestBody CreateProjectRequest request) {
        ClientProjectVO vo = projectService.createClientProject(
                user.getUserId(), request.getTemplateId(), request.getProjectName());
        return Result.success(vo);
    }

    @GetMapping
    @Operation(summary = "用户项目列表", description = "获取当前用户的项目列表（分页）")
    public Result<PageResult<ClientProjectVO>> listProjects(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(projectService.listClientProjects(user.getUserId(), page, size));
    }

    @PostMapping("/{projectId}/upload")
    @Operation(summary = "上传项目 ZIP", description = "上传 ZIP 文件到项目的 NFS 存储目录")
    public Result<Map<String, String>> uploadZip(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long projectId,
            @RequestParam("file") MultipartFile file) throws IOException {
        String storageUrl = projectService.uploadProjectZip(
                projectId, user.getUserId(), file.getBytes());
        return Result.success(Map.of("storageUrl", storageUrl));
    }

    @PutMapping("/{projectId}")
    @Operation(summary = "更新项目名称", description = "更新项目的名称等元信息")
    public Result<Void> updateProject(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long projectId,
            @Valid @RequestBody UpdateProjectRequest request) {
        projectService.updateClientProject(projectId, user.getUserId(), request.getProjectName());
        return Result.success();
    }

    @DeleteMapping("/{projectId}")
    @Operation(summary = "删除项目（含文件）", description = "删除项目记录并清理 NFS 上的文件")
    public Result<Void> deleteProject(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long projectId) {
        projectService.deleteClientProject(projectId, user.getUserId());
        return Result.success();
    }
}