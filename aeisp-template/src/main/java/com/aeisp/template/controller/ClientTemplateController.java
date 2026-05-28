package com.aeisp.template.controller;

import com.aeisp.common.Result;
import com.aeisp.common.exception.BizException;
import com.aeisp.common.security.CustomUserDetails;
import com.aeisp.template.code.TemplateErrorCode;
import com.aeisp.template.entity.TplTemplate;
import com.aeisp.template.entity.TplTemplateUsageLog;
import com.aeisp.template.entity.TplTemplateVersion;
import com.aeisp.template.mapper.TplTemplateMapper;
import com.aeisp.template.mapper.TplTemplateUsageLogMapper;
import com.aeisp.template.mapper.TplTemplateVersionMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

/**
 * 客户端模板下载 Controller。
 *
 * <p>提供 Unity 客户端专用的模板下载接口，下载时记录使用日志。</p>
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/client/templates")
@RequiredArgsConstructor
@Tag(name = "客户端模板下载", description = "Unity 客户端模板下载与日志记录")
public class ClientTemplateController {

    private final TplTemplateMapper templateMapper;
    private final TplTemplateVersionMapper versionMapper;
    private final TplTemplateUsageLogMapper usageLogMapper;

    /**
     * 客户端下载模板 ZIP。
     *
     * <p>记录下载日志后 302 重定向到资源服务器 URL。</p>
     */
    @GetMapping("/{id}/download")
    @Operation(summary = "客户端下载模板", description = "Unity 客户端下载模板 ZIP，记录使用日志")
    public ResponseEntity<Void> downloadTemplate(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long id,
            @RequestParam(value = "versionNo", required = false) String versionNo) {
        // 1. 校验模板存在且已上线
        TplTemplate template = templateMapper.selectById(id);
        if (template == null) {
            throw new BizException(TemplateErrorCode.TEMPLATE_NOT_FOUND);
        }
        if (template.getStatus() == null || template.getStatus() != 1) {
            throw new BizException(TemplateErrorCode.TEMPLATE_NOT_PUBLISHED);
        }

        // 2. 获取目标版本
        String targetVersion = versionNo != null ? versionNo : getCurrentVersionNo(id);
        if (targetVersion == null) {
            throw new BizException(TemplateErrorCode.VERSION_NOT_FOUND);
        }

        // 3. 查询版本记录获取 storageUrl
        TplTemplateVersion version = versionMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<TplTemplateVersion>()
                        .eq(TplTemplateVersion::getTemplateId, id)
                        .eq(TplTemplateVersion::getVersionNo, targetVersion));
        if (version == null || version.getStorageUrl() == null) {
            throw new BizException(TemplateErrorCode.VERSION_NOT_FOUND);
        }

        // 4. 记录下载日志
        recordUsageLog(user.getUserId(), id, template.getTemplateName(), targetVersion);

        // 5. 302 重定向到资源服务器 URL
        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, version.getStorageUrl())
                .build();
    }

    private String getCurrentVersionNo(Long templateId) {
        TplTemplate template = templateMapper.selectById(templateId);
        if (template == null || template.getCurrentVersionId() == null) {
            return null;
        }
        TplTemplateVersion version = versionMapper.selectById(template.getCurrentVersionId());
        return version != null ? version.getVersionNo() : null;
    }

    private void recordUsageLog(Long userId, Long templateId, String templateName, String versionNo) {
        try {
            TplTemplateUsageLog usageLog = new TplTemplateUsageLog();
            usageLog.setUserId(userId);
            usageLog.setTemplateId(templateId);
            usageLog.setTemplateName(templateName);
            usageLog.setVersionNo(versionNo);
            usageLog.setActionType("download");
            usageLog.setCreatedAt(LocalDateTime.now());
            usageLogMapper.insert(usageLog);

            log.debug("记录模板下载日志: userId={}, templateId={}, versionNo={}", userId, templateId, versionNo);
        } catch (Exception e) {
            log.warn("记录模板使用日志失败: {}", e.getMessage());
        }
    }
}
