package com.aeisp.template.controller;

import com.aeisp.common.PageResult;
import com.aeisp.common.Result;
import com.aeisp.template.dto.request.CreateTemplateRequest;
import com.aeisp.template.dto.request.TemplateQueryRequest;
import com.aeisp.template.dto.request.UpdateTemplateRequest;
import com.aeisp.template.dto.vo.TplTemplateDetailVO;
import com.aeisp.template.dto.vo.TplTemplateVO;
import com.aeisp.common.PageResult;
import com.aeisp.template.entity.TplTemplateUsageLog;
import com.aeisp.template.mapper.TplTemplateUsageLogMapper;
import com.aeisp.template.service.TemplateStorageService;
import com.aeisp.template.service.TplTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 模板管理 Controller。
 *
 * <p>提供模板资源的统一管理、版本控制、上下线和统计接口。</p>
 *
 * @author AEISP Team
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/templates")
@RequiredArgsConstructor
@Tag(name = "模板管理", description = "编程模板资源的统一管理、版本控制、上下线和统计")
public class TemplateController {

    private final TplTemplateService templateService;
    private final TemplateStorageService templateStorageService;
    private final TplTemplateUsageLogMapper tplTemplateUsageLogMapper;

    /**
     * 创建模板（含首个版本 ZIP）。
     */
    @PreAuthorize("hasAuthority('template:create')")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "创建模板", description = "创建模板并上传首个版本的 ZIP 文件")
    public Result<Boolean> createTemplate(@Valid @ModelAttribute CreateTemplateRequest request) {
        return Result.success(templateService.createTemplate(request));
    }

    /**
     * 修改模板信息。
     */
    @PreAuthorize("hasAuthority('template:update')")
    @PutMapping("/{id}")
    @Operation(summary = "修改模板信息", description = "仅修改模板基础信息，不修改文件")
    public Result<Boolean> updateTemplateInfo(@PathVariable Long id,
                                              @Valid @RequestBody UpdateTemplateRequest request) {
        return Result.success(templateService.updateTemplateInfo(id, request));
    }

    /**
     * 上传新版本。
     */
    @PreAuthorize("hasAuthority('template:version:manage')")
    @PostMapping(value = "/{id}/versions", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "上传新版本", description = "为指定模板上传新版本 ZIP 文件")
    public Result<Boolean> uploadNewVersion(@PathVariable Long id,
                                            @RequestParam("zipFile") MultipartFile zipFile,
                                            @RequestParam("versionNo") String versionNo,
                                            @RequestParam(value = "changelog", required = false) String changelog) {
        return Result.success(templateService.uploadNewVersion(id, zipFile, versionNo, changelog));
    }

    /**
     * 回滚到指定版本。
     */
    @PreAuthorize("hasAuthority('template:version:manage')")
    @PostMapping("/{id}/rollback/{versionId}")
    @Operation(summary = "回滚版本", description = "将模板回滚到指定版本")
    public Result<Boolean> rollbackVersion(@PathVariable Long id, @PathVariable Long versionId) {
        return Result.success(templateService.rollbackVersion(id, versionId));
    }

    /**
     * 上下线切换。
     */
    @PreAuthorize("hasAuthority('template:update')")
    @PostMapping("/{id}/status")
    @Operation(summary = "上下线切换", description = "切换模板的上线/下线状态")
    public Result<Boolean> toggleStatus(@PathVariable Long id, @RequestParam Integer status) {
        return Result.success(templateService.toggleStatus(id, status));
    }

    /**
     * 删除模板。
     */
    @PreAuthorize("hasAuthority('template:delete')")
    @DeleteMapping("/{id}")
    @Operation(summary = "删除模板", description = "逻辑删除模板及其所有版本文件")
    public Result<Boolean> deleteTemplate(@PathVariable Long id) {
        return Result.success(templateService.deleteTemplate(id));
    }

    /**
     * 模板列表（管理端）。
     */
    @PreAuthorize("hasAuthority('template:read')")
    @GetMapping
    @Operation(summary = "模板列表", description = "分页查询模板列表（管理端）")
    public Result<PageResult<TplTemplateVO>> listTemplates(TemplateQueryRequest request) {
        return Result.success(templateService.listTemplates(request));
    }

    /**
     * 模板详情。
     */
    @PreAuthorize("hasAuthority('template:read')")
    @GetMapping("/{id}")
    @Operation(summary = "模板详情", description = "获取模板详情（含当前版本、历史版本、文件结构）")
    public Result<TplTemplateDetailVO> getDetail(@PathVariable Long id) {
        return Result.success(templateService.getDetail(id));
    }

    /**
     * 查询文件结构。
     */
    @PreAuthorize("hasAuthority('template:read')")
    @GetMapping("/{id}/files")
    @Operation(summary = "文件结构", description = "获取模板当前版本的文件树结构")
    public Result<List<String>> listFiles(@PathVariable Long id,
                                          @RequestParam(value = "versionNo", required = false) String versionNo) {
        TplTemplateDetailVO detail = templateService.getDetail(id);
        String targetVersion = versionNo != null ? versionNo
                : (detail.getCurrentVersion() != null ? detail.getCurrentVersion().getVersionNo() : null);
        if (targetVersion == null) {
            return Result.success(List.of());
        }
        return Result.success(templateStorageService.listFiles(id, targetVersion));
    }

    /**
     * 读取文件内容。
     */
    @PreAuthorize("hasAuthority('template:read')")
    @GetMapping("/{id}/files/content")
    @Operation(summary = "读取文件内容", description = "读取模板版本中指定文件的内容")
    public Result<byte[]> readFileContent(@PathVariable Long id,
                                          @RequestParam String versionNo,
                                          @RequestParam String filePath) {
        byte[] content = templateStorageService.readFile(id, versionNo, filePath);
        return Result.success(content);
    }

    /**
     * 下载当前版本的 ZIP。
     */
    @PreAuthorize("hasAuthority('template:read')")
    @GetMapping("/{id}/download")
    @Operation(summary = "下载 ZIP", description = "下载模板指定版本的 ZIP 文件")
    public ResponseEntity<Resource> downloadZip(@PathVariable Long id,
                                                @RequestParam(value = "versionNo", required = false) String versionNo) {
        TplTemplateDetailVO detail = templateService.getDetail(id);
        String targetVersion = versionNo != null ? versionNo
                : (detail.getCurrentVersion() != null ? detail.getCurrentVersion().getVersionNo() : null);
        if (targetVersion == null) {
            return ResponseEntity.notFound().build();
        }
        String zipPath = templateStorageService.getZipAbsolutePath(id, targetVersion);
        if (zipPath == null) {
            return ResponseEntity.notFound().build();
        }
        File file = new File(zipPath);
        Resource resource = new FileSystemResource(file);

        // 记录模板下载日志
        recordUsageLog(id, detail.getTemplateName(), targetVersion, "download");

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    private void recordUsageLog(Long templateId, String templateName, String versionNo, String actionType) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
                Object principal = auth.getPrincipal();
                Long userId = extractUserId(principal);
                if (userId != null) {
                    TplTemplateUsageLog usageLog = new TplTemplateUsageLog();
                    usageLog.setUserId(userId);
                    usageLog.setTemplateId(templateId);
                    usageLog.setTemplateName(templateName);
                    usageLog.setVersionNo(versionNo);
                    usageLog.setActionType(actionType);
                    usageLog.setCreatedAt(LocalDateTime.now());
                    tplTemplateUsageLogMapper.insert(usageLog);
                }
            }
        } catch (Exception e) {
            log.warn("记录模板使用日志失败: {}", e.getMessage());
        }
    }

    private Long extractUserId(Object principal) {
        if (principal == null) {
            return null;
        }
        try {
            java.lang.reflect.Method getUserIdMethod = principal.getClass().getMethod("getUserId");
            Object result = getUserIdMethod.invoke(principal);
            return result instanceof Long ? (Long) result : null;
        } catch (Exception e) {
            return null;
        }
    }

    // ==================== 用户端接口 ====================

    /**
     * 查询上线的模板列表（前端使用）。
     */
    @GetMapping("/public")
    @Operation(summary = "上线模板列表", description = "查询所有上线的模板列表，支持场景筛选（前端使用）")
    public Result<List<TplTemplateVO>> listOnlineTemplates(@RequestParam(value = "scenario", required = false) String scenario) {
        return Result.success(templateService.listOnlineTemplates(scenario));
    }

    /**
     * 模板使用统计。
     */
    @PreAuthorize("hasAuthority('template:read')")
    @GetMapping("/statistics")
    @Operation(summary = "模板统计", description = "获取模板使用统计数据")
    public Result<Map<String, Object>> getStatistics() {
        Map<String, Object> stats = templateService.getStatistics();
        return Result.success(stats);
    }

    /**
     * 查询上线模板详情（前端使用）。
     */
    @GetMapping("/public/{id}")
    @Operation(summary = "上线模板详情", description = "查询上线模板详情（前端使用）")
    public Result<TplTemplateDetailVO> getPublicDetail(@PathVariable Long id) {
        return Result.success(templateService.getPublicDetail(id));
    }

    /**
     * 查询指定用户的模板使用记录。
     */
    @PreAuthorize("hasAuthority('user:permission:read')")
    @GetMapping("/usage-logs/{userId}")
    @Operation(summary = "用户模板使用记录", description = "查询指定用户的模板下载/使用记录")
    public Result<List<TplTemplateUsageLog>> getUserTemplateUsageLogs(@PathVariable Long userId) {
        return Result.success(tplTemplateUsageLogMapper.selectByUserId(userId));
    }
}
