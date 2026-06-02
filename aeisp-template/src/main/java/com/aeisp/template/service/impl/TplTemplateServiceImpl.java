package com.aeisp.template.service.impl;

import com.aeisp.common.PageResult;
import com.aeisp.common.code.CommonErrorCode;
import com.aeisp.common.exception.BizException;
import com.aeisp.template.code.TemplateErrorCode;
import com.aeisp.template.dto.TplTemplateCategoryVO;
import com.aeisp.template.dto.request.CreateTemplateRequest;
import com.aeisp.template.dto.request.TemplateQueryRequest;
import com.aeisp.template.dto.request.UpdateTemplateRequest;
import com.aeisp.template.dto.vo.FileNodeVO;
import com.aeisp.template.dto.vo.TplTemplateDetailVO;
import com.aeisp.template.dto.vo.TplTemplateVO;
import com.aeisp.template.dto.vo.TplTemplateVersionVO;
import com.aeisp.template.entity.TplTemplate;
import com.aeisp.template.entity.TplTemplateVersion;
import com.aeisp.template.enums.TemplateStatusEnum;
import com.aeisp.template.mapper.TplTemplateMapper;
import com.aeisp.template.mapper.TplTemplateVersionMapper;
import com.aeisp.common.service.ResourceServerService;
import com.aeisp.template.service.TemplateStorageService;
import com.aeisp.template.service.TplTemplateCategoryService;
import com.aeisp.template.service.TplTemplateService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 模板主表 Service 实现类。
 *
 * @author AEISP Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TplTemplateServiceImpl implements TplTemplateService {

    private final TplTemplateMapper templateMapper;
    private final TplTemplateVersionMapper versionMapper;
    private final TemplateStorageService templateStorageService;
    private final TplTemplateCategoryService categoryService;
    private final ResourceServerService resourceServerService;
    private final com.aeisp.template.mapper.TemplateLibraryRelationMapper templateLibraryRelationMapper;

    @Override
    public Long createTemplate(CreateTemplateRequest request) {
        // 保存模板主表
        TplTemplate template = new TplTemplate();
        template.setTemplateName(request.getTemplateName());
        // 自动生成 template_code
        template.setTemplateCode(generateTemplateCode());
        template.setDescription(request.getDescription());
        template.setSortWeight(request.getSortWeight());
        template.setTopCategoryId(request.getTopCategoryId());
        template.setFirstCategoryId(request.getFirstCategoryId());
        template.setSecondCategoryId(request.getSecondCategoryId());
        template.setIsPaid(request.getIsPaid() != null ? request.getIsPaid() : 0);
        template.setFeeType(request.getFeeType());
        template.setPrice(request.getPrice());
        template.setOnlineTime(parseDateTime(request.getOnlineTime()));
        template.setValidTime(parseDateTime(request.getValidTime()));
        template.setCreator(request.getCreator());
        template.setProduceDate(parseDate(request.getProduceDate()));
        template.setDetailDesc(request.getDetailDesc());
        template.setDifficulty(request.getDifficulty());
        template.setStatus(TemplateStatusEnum.OFFLINE.getCode());
        template.setUsageCount(0L);
        template.setDownloadCount(0L);
        template.setFavoriteCount(0L);
        template.setVisitCount(0L);
        Long adminId = getCurrentAdminId();
        if (adminId != null) {
            template.setCreatedBy(adminId);
        }
        templateMapper.insert(template);

        // 上传封面图到资源服务器（使用模板 ID 作为路径）
        if (request.getCoverImage() != null && !request.getCoverImage().isEmpty()) {
            try {
                String coverUrl = templateStorageService.storeCoverImage(template.getId(), request.getCoverImage());
                template.setPreviewImage(coverUrl);
            } catch (Exception e) {
                log.error("上传封面图失败: templateId={}", template.getId(), e);
            }
        }

        // 上传缩略图到资源服务器（使用模板 ID 作为路径）
        if (request.getThumbnail() != null && !request.getThumbnail().isEmpty()) {
            try {
                String thumbnailUrl = templateStorageService.storeThumbnail(template.getId(), request.getThumbnail());
                template.setThumbnail(thumbnailUrl);
            } catch (Exception e) {
                log.error("上传缩略图失败: templateId={}", template.getId(), e);
            }
        }

        // 1. 暂存 ZIP 到本地临时目录
        String relativePath = templateStorageService.storeZip(template.getId(), request.getVersionNo(), request.getZipFile());

        // 2. 读取本地 ZIP 文件并上传到资源服务器
        String zipAbsPath = templateStorageService.getZipAbsolutePath(template.getId(), request.getVersionNo());
        String storageUrl = null;
        if (zipAbsPath != null) {
            try {
                byte[] zipBytes = java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(zipAbsPath));
                storageUrl = resourceServerService.uploadFile(relativePath, zipBytes);
                log.info("ZIP 上传到资源服务器完成: {}", storageUrl);
            } catch (IOException e) {
                log.error("上传 ZIP 到资源服务器失败, zipAbsPath={}", zipAbsPath, e);
            }
        } else {
            log.warn("zipAbsPath 为空，跳过 NFS ZIP 上传");
        }

        // 3. 解压 ZIP 到本地
        String destDir = zipAbsPath != null ? zipAbsPath.substring(0, zipAbsPath.lastIndexOf('.')) : null;
        if (zipAbsPath != null) {
            templateStorageService.extractZip(zipAbsPath, destDir);
        }

        // 4. 上传提取的文件到资源服务器
        if (destDir != null) {
            resourceServerService.uploadExtractedFiles(template.getId(), request.getVersionNo(), new java.io.File(destDir));
        }

        // 5. 保存版本记录
        TplTemplateVersion version = new TplTemplateVersion();
        version.setTemplateId(template.getId());
        version.setVersionNo(request.getVersionNo());
        version.setFilePath(relativePath);
        version.setStorageUrl(storageUrl);
        version.setChangelog(request.getChangelog());
        version.setFileSize(request.getZipFile().getSize());
        version.setFileHash(calcFileHash(request.getZipFile()));
        versionMapper.insert(version);

        // 6. 更新当前版本
        template.setCurrentVersionId(version.getId());
        templateMapper.updateById(template);

        // 7. 清理本地临时文件
        if (zipAbsPath != null) {
            templateStorageService.deleteVersionFiles(template.getId(), request.getVersionNo());
        }

        return template.getId();
    }

    @Override
    public boolean updateTemplateInfo(Long templateId, UpdateTemplateRequest request) {
        TplTemplate template = templateMapper.selectById(templateId);
        if (template == null) {
            throw new BizException(TemplateErrorCode.TEMPLATE_NOT_FOUND);
        }
        template.setTemplateName(request.getTemplateName());
        template.setDescription(request.getDescription());
        template.setPreviewImage(request.getPreviewImage());
        template.setThumbnail(request.getThumbnail());
        template.setSortWeight(request.getSortWeight());
        template.setTopCategoryId(request.getTopCategoryId());
        template.setFirstCategoryId(request.getFirstCategoryId());
        template.setSecondCategoryId(request.getSecondCategoryId());
        template.setIsPaid(request.getIsPaid());
        template.setFeeType(request.getFeeType());
        template.setPrice(request.getPrice());
        template.setOnlineTime(parseDateTime(request.getOnlineTime()));
        template.setValidTime(parseDateTime(request.getValidTime()));
        template.setCreator(request.getCreator());
        template.setProduceDate(parseDate(request.getProduceDate()));
        template.setDetailDesc(request.getDetailDesc());
        template.setDifficulty(request.getDifficulty());
        boolean updated = templateMapper.updateById(template) > 0;

        // 更新库资源关联
        if (request.getLibraryIds() != null) {
            setTemplateLibraries(templateId, request.getLibraryIds());
        }

        return updated;
    }

    @Override
    public boolean uploadNewVersion(Long templateId, MultipartFile zipFile, String versionNo,
                                     String changelog, String onlineTime, String validTime,
                                     Integer difficulty, Integer isPaid, String feeType, java.math.BigDecimal price) {
        TplTemplate template = templateMapper.selectById(templateId);
        if (template == null) {
            throw new BizException(TemplateErrorCode.TEMPLATE_NOT_FOUND);
        }

        // 检查版本号是否已存在
        LambdaQueryWrapper<TplTemplateVersion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TplTemplateVersion::getTemplateId, templateId)
                .eq(TplTemplateVersion::getVersionNo, versionNo);
        if (versionMapper.selectCount(wrapper) > 0) {
            throw new BizException(TemplateErrorCode.TEMPLATE_VERSION_EXISTS);
        }

        // 1. 暂存 ZIP 到本地临时目录
        String relativePath = templateStorageService.storeZip(templateId, versionNo, zipFile);

        // 2. 读取本地 ZIP 文件并上传到资源服务器
        String zipAbsPath = templateStorageService.getZipAbsolutePath(templateId, versionNo);
        String storageUrl = null;
        if (zipAbsPath != null) {
            try {
                byte[] zipBytes = java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(zipAbsPath));
                storageUrl = resourceServerService.uploadFile(relativePath, zipBytes);
                log.info("ZIP 上传到资源服务器完成: {}", storageUrl);
            } catch (IOException e) {
                log.error("上传 ZIP 到资源服务器失败", e);
            }
        }

        // 3. 解压 ZIP 到本地
        String destDir = zipAbsPath != null ? zipAbsPath.substring(0, zipAbsPath.lastIndexOf('.')) : null;
        if (zipAbsPath != null) {
            templateStorageService.extractZip(zipAbsPath, destDir);
        }

        // 4. 上传提取的文件到资源服务器
        if (destDir != null) {
            resourceServerService.uploadExtractedFiles(templateId, versionNo, new java.io.File(destDir));
        }

        // 5. 保存版本记录
        TplTemplateVersion version = new TplTemplateVersion();
        version.setTemplateId(templateId);
        version.setVersionNo(versionNo);
        version.setFilePath(relativePath);
        version.setStorageUrl(storageUrl);
        version.setChangelog(changelog);
        version.setFileSize(zipFile.getSize());
        version.setFileHash(calcFileHash(zipFile));
        versionMapper.insert(version);

        // 同步更新模板主表字段（如果新版本提供了值）
        boolean needUpdate = false;
        if (onlineTime != null && !onlineTime.isBlank()) {
            template.setOnlineTime(parseDateTime(onlineTime));
            needUpdate = true;
        }
        if (validTime != null && !validTime.isBlank()) {
            template.setValidTime(parseDateTime(validTime));
            needUpdate = true;
        }
        if (difficulty != null) {
            template.setDifficulty(difficulty);
            needUpdate = true;
        }
        if (isPaid != null) {
            template.setIsPaid(isPaid);
            needUpdate = true;
        }
        if (feeType != null && !feeType.isBlank()) {
            template.setFeeType(feeType);
            needUpdate = true;
        }
        if (price != null) {
            template.setPrice(price);
            needUpdate = true;
        }
        if (needUpdate) {
            templateMapper.updateById(template);
        }

        // 6. 更新当前版本
        template.setCurrentVersionId(version.getId());
        templateMapper.updateById(template);

        // 7. 清理本地临时文件
        if (zipAbsPath != null) {
            templateStorageService.deleteVersionFiles(templateId, versionNo);
        }

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean rollbackVersion(Long templateId, Long versionId) {
        TplTemplate template = templateMapper.selectById(templateId);
        if (template == null) {
            throw new BizException(TemplateErrorCode.TEMPLATE_NOT_FOUND);
        }
        TplTemplateVersion version = versionMapper.selectById(versionId);
        if (version == null || !version.getTemplateId().equals(templateId)) {
            throw new BizException(TemplateErrorCode.VERSION_NOT_BELONG);
        }
        template.setCurrentVersionId(versionId);
        return templateMapper.updateById(template) > 0;
    }

    @Override
    public boolean toggleStatus(Long templateId, Integer status) {
        TplTemplate template = templateMapper.selectById(templateId);
        if (template == null) {
            throw new BizException(TemplateErrorCode.TEMPLATE_NOT_FOUND);
        }
        template.setStatus(status);
        return templateMapper.updateById(template) > 0;
    }

    @Override
    public boolean deleteTemplate(Long templateId) {
        TplTemplate template = templateMapper.selectById(templateId);
        if (template == null) {
            throw new BizException(TemplateErrorCode.TEMPLATE_NOT_FOUND);
        }
        // 逻辑删除模板
        templateMapper.deleteById(templateId);
        // 逻辑删除版本
        LambdaQueryWrapper<TplTemplateVersion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TplTemplateVersion::getTemplateId, templateId);
        versionMapper.delete(wrapper);
        // 清理资源服务器文件
        LambdaQueryWrapper<TplTemplateVersion> versionQuery = new LambdaQueryWrapper<>();
        versionQuery.eq(TplTemplateVersion::getTemplateId, templateId);
        List<TplTemplateVersion> allVersions = versionMapper.selectList(versionQuery);
        for (TplTemplateVersion v : allVersions) {
            resourceServerService.deleteVersionFiles(templateId, v.getVersionNo());
        }
        // 删除物理文件（在 DB 删除之后执行，避免文件已删但 DB 回滚导致不一致）
        templateStorageService.deleteTemplateFiles(templateId);
        return true;
    }

    @Override
    public PageResult<TplTemplateVO> listTemplates(TemplateQueryRequest request) {
        Page<TplTemplate> page = new Page<>(request.getPageNum(), request.getPageSize());
        LambdaQueryWrapper<TplTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(request.getTemplateName()), TplTemplate::getTemplateName, request.getTemplateName())
                .eq(StringUtils.hasText(request.getTemplateCode()), TplTemplate::getTemplateCode, request.getTemplateCode())
                .eq(request.getStatus() != null, TplTemplate::getStatus, request.getStatus())
                .eq(request.getTopCategoryId() != null, TplTemplate::getTopCategoryId, request.getTopCategoryId())
                .eq(request.getFirstCategoryId() != null, TplTemplate::getFirstCategoryId, request.getFirstCategoryId())
                .eq(request.getSecondCategoryId() != null, TplTemplate::getSecondCategoryId, request.getSecondCategoryId())
                .eq(request.getIsPaid() != null, TplTemplate::getIsPaid, request.getIsPaid())
                .orderByDesc(TplTemplate::getSortWeight)
                .orderByDesc(TplTemplate::getCreatedAt);
        Page<TplTemplate> resultPage = templateMapper.selectPage(page, wrapper);

        List<TplTemplateVO> voList = new ArrayList<>();
        for (TplTemplate t : resultPage.getRecords()) {
            voList.add(convertToVO(t));
        }
        return PageResult.of(resultPage, voList);
    }

    @Override
    public TplTemplateDetailVO getDetail(Long templateId) {
        TplTemplate template = templateMapper.selectById(templateId);
        if (template == null) {
            throw new BizException(TemplateErrorCode.TEMPLATE_NOT_FOUND);
        }

        TplTemplateDetailVO vo = new TplTemplateDetailVO();
        BeanUtils.copyProperties(template, vo);

        // LocalDateTime → String 手动转换（BeanUtils 无法自动处理类型差异）
        if (template.getOnlineTime() != null) {
            vo.setOnlineTime(template.getOnlineTime().toString());
        }
        if (template.getValidTime() != null) {
            vo.setValidTime(template.getValidTime().toString());
        }

        // 当前版本
        if (template.getCurrentVersionId() != null) {
            TplTemplateVersion current = versionMapper.selectById(template.getCurrentVersionId());
            if (current != null) {
                TplTemplateVersionVO currentVo = convertToVersionVO(current);
                currentVo.setResourceBaseUrl(resourceServerService.getBaseUrl());
                vo.setCurrentVersion(currentVo);
            }
        }

        // 历史版本
        List<TplTemplateVersion> versions = versionMapper.selectByTemplateId(templateId);
        List<TplTemplateVersionVO> history = new ArrayList<>();
        for (TplTemplateVersion v : versions) {
            if (template.getCurrentVersionId() == null || !v.getId().equals(template.getCurrentVersionId())) {
                TplTemplateVersionVO historyVo = convertToVersionVO(v);
                historyVo.setResourceBaseUrl(resourceServerService.getBaseUrl());
                history.add(historyVo);
            }
        }
        vo.setHistoryVersions(history);

        // 文件结构
        String versionNo = vo.getCurrentVersion() != null ? vo.getCurrentVersion().getVersionNo() : null;
        if (versionNo != null) {
            List<String> filePaths = templateStorageService.listFiles(templateId, versionNo);
            vo.setFileTree(buildFileTree(filePaths));
        } else {
            vo.setFileTree(new ArrayList<>());
        }

        // 关联库资源
        vo.setLibraryIds(getTemplateLibraryIds(templateId));

        return vo;
    }

    @Override
    public TplTemplateDetailVO getPublicDetail(Long templateId) {
        TplTemplate template = templateMapper.selectById(templateId);
        if (template == null) {
            throw new BizException(TemplateErrorCode.TEMPLATE_NOT_FOUND);
        }
        if (!TemplateStatusEnum.ACTIVE.getCode().equals(template.getStatus())) {
            throw new BizException(TemplateErrorCode.TEMPLATE_NOT_PUBLISHED);
        }
        return getDetail(templateId);
    }

    @Override
    public List<TplTemplateVO> listOnlineTemplates() {
        List<TplTemplate> list = templateMapper.selectOnlineList();
        List<TplTemplateVO> voList = new ArrayList<>();
        for (TplTemplate t : list) {
            voList.add(convertToVO(t));
        }
        return voList;
    }

    @Override
    public boolean markViolation(Long templateId, String reason) {
        TplTemplate template = templateMapper.selectById(templateId);
        if (template == null) {
            throw new BizException(TemplateErrorCode.TEMPLATE_NOT_FOUND);
        }
        template.setStatus(TemplateStatusEnum.VIOLATION.getCode());
        template.setViolationReason(reason);
        return templateMapper.updateById(template) > 0;
    }

    @Override
    public void incrementDownloadCount(Long templateId) {
        try {
            TplTemplate template = templateMapper.selectById(templateId);
            if (template != null && template.getDownloadCount() != null) {
                template.setDownloadCount(template.getDownloadCount() + 1);
                templateMapper.updateById(template);
            }
        } catch (Exception e) {
            log.warn("下载计数失败: templateId={}", templateId, e);
        }
    }

    @Override
    public Map<String, Object> getStatistics() {
        Map<String, Object> result = new HashMap<>();

        long total = templateMapper.selectCount(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>());
        long online = templateMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<TplTemplate>()
                        .eq(TplTemplate::getStatus, TemplateStatusEnum.ACTIVE.getCode()));
        long offline = templateMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<TplTemplate>()
                        .eq(TplTemplate::getStatus, TemplateStatusEnum.OFFLINE.getCode()));

        result.put("total", total);
        result.put("online", online);
        result.put("offline", offline);

        // 热门模板（按使用次数排序前10）
        List<TplTemplate> hotList = templateMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<TplTemplate>()
                        .orderByDesc(TplTemplate::getUsageCount)
                        .last("LIMIT 10"));
        result.put("hotTemplates", hotList.stream().map(t -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", t.getId());
            m.put("templateName", t.getTemplateName());
            m.put("usageCount", t.getUsageCount());
            m.put("projectCount", t.getProjectCount());
            return m;
        }).toList());

        return result;
    }

    /**
     * 自动生成模板编码。
     * 格式：TPL + yyyyMMdd + 4位序号，如 TPL202605250001
     */
    private String generateTemplateCode() {
        String prefix = "TPL" + java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.BASIC_ISO_DATE);
        String maxCode = templateMapper.selectMaxTemplateCode(prefix);
        int seq = 1;
        if (maxCode != null && maxCode.length() > prefix.length()) {
            try {
                seq = Integer.parseInt(maxCode.substring(prefix.length())) + 1;
            } catch (NumberFormatException e) {
                log.warn("解析最大模板编码序号失败: {}", maxCode);
            }
        }
        return prefix + String.format("%04d", seq);
    }

    private TplTemplateVO convertToVO(TplTemplate template) {
        TplTemplateVO vo = new TplTemplateVO();
        BeanUtils.copyProperties(template, vo);
        copyNewFields(template, vo);
        vo.setCategoryPath(buildCategoryPath(
                template.getTopCategoryId(),
                template.getFirstCategoryId(),
                template.getSecondCategoryId()));
        if (template.getCurrentVersionId() != null) {
            TplTemplateVersion version = versionMapper.selectById(template.getCurrentVersionId());
            if (version != null) {
                vo.setCurrentVersionNo(version.getVersionNo());
                vo.setResourceUrl(version.getStorageUrl());
            }
        }
        return vo;
    }

    private static void copyNewFields(TplTemplate source, TplTemplateVO target) {
        target.setTopCategoryId(source.getTopCategoryId());
        target.setFirstCategoryId(source.getFirstCategoryId());
        target.setSecondCategoryId(source.getSecondCategoryId());
        target.setIsPaid(source.getIsPaid());
        target.setFeeType(source.getFeeType());
        target.setPrice(source.getPrice());
        target.setOnlineTime(source.getOnlineTime() != null ? source.getOnlineTime().toString() : null);
        target.setValidTime(source.getValidTime() != null ? source.getValidTime().toString() : null);
        target.setCreator(source.getCreator());
        target.setProduceDate(source.getProduceDate() != null ? source.getProduceDate().toString() : null);
        target.setDownloadCount(source.getDownloadCount());
        target.setFavoriteCount(source.getFavoriteCount());
        target.setVisitCount(source.getVisitCount());
        target.setDetailDesc(source.getDetailDesc());
        target.setViolationReason(source.getViolationReason());
        target.setUpdatedAt(source.getUpdatedAt() != null ? source.getUpdatedAt().toString() : null);
    }

    /**
     * 获取模板的分类路径字符串。
     */
    private String buildCategoryPath(Long topId, Long firstId, Long secondId) {
        StringBuilder sb = new StringBuilder();
        try {
            if (topId != null) {
                TplTemplateCategoryVO top = categoryService.getById(topId);
                if (top != null) sb.append(top.getName());
            }
            if (firstId != null) {
                TplTemplateCategoryVO first = categoryService.getById(firstId);
                if (first != null) sb.append("-").append(first.getName());
            }
            if (secondId != null) {
                TplTemplateCategoryVO second = categoryService.getById(secondId);
                if (second != null) sb.append("-").append(second.getName());
            }
        } catch (Exception e) {
            log.warn("构建分类路径失败: template={}-{}-{}", topId, firstId, secondId, e);
        }
        return sb.toString();
    }

    private static TplTemplateVersionVO convertToVersionVO(TplTemplateVersion version) {
        TplTemplateVersionVO vo = new TplTemplateVersionVO();
        BeanUtils.copyProperties(version, vo);
        vo.setFileSize(version.getFileSize());
        vo.setFileHash(version.getFileHash());
        return vo;
    }

    private static List<FileNodeVO> buildFileTree(List<String> filePaths) {
        Map<String, FileNodeVO> dirMap = new HashMap<>();
        List<FileNodeVO> root = new ArrayList<>();

        for (String path : filePaths) {
            String[] parts = path.split("/");
            FileNodeVO parent = null;
            StringBuilder currentPath = new StringBuilder();

            for (int i = 0; i < parts.length; i++) {
                if (currentPath.length() > 0) {
                    currentPath.append("/");
                }
                currentPath.append(parts[i]);
                String key = currentPath.toString();

                FileNodeVO node = dirMap.get(key);
                if (node == null) {
                    node = new FileNodeVO();
                    node.setName(parts[i]);
                    node.setType(i == parts.length - 1 ? "file" : "dir");
                    dirMap.put(key, node);

                    if (parent == null) {
                        root.add(node);
                    } else {
                        parent.getChildren().add(node);
                    }
                }
                parent = node;
            }
        }
        return root;
    }

    /**
     * 计算文件的 SHA-256 哈希值。
     */
    private static String calcFileHash(org.springframework.web.multipart.MultipartFile file) {
        try (java.io.InputStream is = file.getInputStream()) {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] buf = new byte[8192];
            int len;
            while ((len = is.read(buf)) != -1) {
                digest.update(buf, 0, len);
            }
            byte[] hash = digest.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (IOException | NoSuchAlgorithmException e) {
            log.warn("计算文件哈希失败", e);
            return null;
        }
    }

    private static java.time.LocalDateTime parseDateTime(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return java.time.LocalDateTime.parse(value, java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (Exception e) {
            return null;
        }
    }

    private static java.time.LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return java.time.LocalDate.parse(value, java.time.format.DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public List<TplTemplateCategoryVO> listCategoryTreeWithTemplates() {
        // 1. 获取完整分类树
        List<TplTemplateCategoryVO> tree = categoryService.getTree();

        // 2. 获取所有上架模板
        List<TplTemplate> templates = templateMapper.selectOnlineList();

        // 3. 按三级分类 ID 分组
        Map<Long, List<TplTemplateVO>> templateMap = new HashMap<>();
        for (TplTemplate t : templates) {
            if (t.getSecondCategoryId() != null) {
                templateMap.computeIfAbsent(t.getSecondCategoryId(), k -> new ArrayList<>())
                        .add(convertToVO(t));
            }
        }

        // 4. 将模板挂载到三级分类节点
        for (TplTemplateCategoryVO level0 : tree) {
            if (level0.getChildren() != null) {
                for (TplTemplateCategoryVO level1 : level0.getChildren()) {
                    if (level1.getChildren() != null) {
                        for (TplTemplateCategoryVO level2 : level1.getChildren()) {
                            List<TplTemplateVO> list = templateMap.get(level2.getId());
                            if (list != null) {
                                level2.setTemplates(list);
                            }
                        }
                    }
                }
            }
        }

        return tree;
    }

    /**
     * 从 SecurityContext 获取当前登录管理员 ID。
     */
    private static Long getCurrentAdminId() {
        try {
            org.springframework.security.core.Authentication auth =
                    org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) {
                return null;
            }
            Object principal = auth.getPrincipal();
            if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
                java.lang.reflect.Method method = principal.getClass().getMethod("getUserId");
                Object userId = method.invoke(principal);
                return userId instanceof Number ? ((Number) userId).longValue() : null;
            }
        } catch (Exception e) {
            // ignore
        }
        return null;
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    public boolean setTemplateLibraries(Long templateId, java.util.List<Long> libraryIds) {
        // 删除旧关联
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.aeisp.template.entity.TemplateLibraryRelation> wrapper =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        wrapper.eq(com.aeisp.template.entity.TemplateLibraryRelation::getTemplateId, templateId);
        templateLibraryRelationMapper.delete(wrapper);

        // 插入新关联
        if (libraryIds != null && !libraryIds.isEmpty()) {
            for (Long libraryId : libraryIds) {
                com.aeisp.template.entity.TemplateLibraryRelation relation =
                        new com.aeisp.template.entity.TemplateLibraryRelation();
                relation.setTemplateId(templateId);
                relation.setLibraryId(libraryId);
                templateLibraryRelationMapper.insert(relation);
            }
        }
        return true;
    }

    @Override
    public java.util.List<Long> getTemplateLibraryIds(Long templateId) {
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.aeisp.template.entity.TemplateLibraryRelation> wrapper =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        wrapper.eq(com.aeisp.template.entity.TemplateLibraryRelation::getTemplateId, templateId);
        return templateLibraryRelationMapper.selectList(wrapper)
                .stream()
                .map(com.aeisp.template.entity.TemplateLibraryRelation::getLibraryId)
                .toList();
    }
}
