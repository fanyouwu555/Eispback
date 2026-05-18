package com.aeisp.template.service.impl;

import com.aeisp.common.PageResult;
import com.aeisp.common.exception.BizException;
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
import com.aeisp.template.service.TemplateStorageService;
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

    @Override
    public boolean createTemplate(CreateTemplateRequest request) {
        // 保存模板主表
        TplTemplate template = new TplTemplate();
        template.setTemplateName(request.getTemplateName());
        template.setScenario(request.getScenario());
        template.setDescription(request.getDescription());
        template.setPreviewImage(request.getPreviewImage());
        template.setSortWeight(request.getSortWeight());
        template.setStatus(TemplateStatusEnum.OFFLINE.getCode());
        template.setUsageCount(0L);
        templateMapper.insert(template);

        // 存储 ZIP 文件（非事务操作，失败不会导致 DB 回滚）
        String relativePath = templateStorageService.storeZip(template.getId(), request.getVersionNo(), request.getZipFile());

        // 保存版本记录
        TplTemplateVersion version = new TplTemplateVersion();
        version.setTemplateId(template.getId());
        version.setVersionNo(request.getVersionNo());
        version.setFilePath(relativePath);
        version.setChangelog(request.getChangelog());
        versionMapper.insert(version);

        // 更新当前版本
        template.setCurrentVersionId(version.getId());
        templateMapper.updateById(template);

        // 解压 ZIP
        String zipAbsPath = templateStorageService.getZipAbsolutePath(template.getId(), request.getVersionNo());
        if (zipAbsPath != null) {
            String destDir = zipAbsPath.substring(0, zipAbsPath.lastIndexOf('.'));
            templateStorageService.extractZip(zipAbsPath, destDir);
        }

        return true;
    }

    @Override
    public boolean updateTemplateInfo(Long templateId, UpdateTemplateRequest request) {
        TplTemplate template = templateMapper.selectById(templateId);
        if (template == null) {
            throw new BizException("模板不存在");
        }
        template.setTemplateName(request.getTemplateName());
        template.setScenario(request.getScenario());
        template.setDescription(request.getDescription());
        template.setPreviewImage(request.getPreviewImage());
        template.setSortWeight(request.getSortWeight());
        return templateMapper.updateById(template) > 0;
    }

    @Override
    public boolean uploadNewVersion(Long templateId, MultipartFile zipFile, String versionNo, String changelog) {
        TplTemplate template = templateMapper.selectById(templateId);
        if (template == null) {
            throw new BizException("模板不存在");
        }

        // 检查版本号是否已存在
        LambdaQueryWrapper<TplTemplateVersion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TplTemplateVersion::getTemplateId, templateId)
                .eq(TplTemplateVersion::getVersionNo, versionNo);
        if (versionMapper.selectCount(wrapper) > 0) {
            throw new BizException("版本号已存在");
        }

        // 存储 ZIP（非事务操作）
        String relativePath = templateStorageService.storeZip(templateId, versionNo, zipFile);

        // 保存版本记录
        TplTemplateVersion version = new TplTemplateVersion();
        version.setTemplateId(templateId);
        version.setVersionNo(versionNo);
        version.setFilePath(relativePath);
        version.setChangelog(changelog);
        versionMapper.insert(version);

        // 更新当前版本
        template.setCurrentVersionId(version.getId());
        templateMapper.updateById(template);

        // 解压
        String zipAbsPath = templateStorageService.getZipAbsolutePath(templateId, versionNo);
        if (zipAbsPath != null) {
            String destDir = zipAbsPath.substring(0, zipAbsPath.lastIndexOf('.'));
            templateStorageService.extractZip(zipAbsPath, destDir);
        }

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean rollbackVersion(Long templateId, Long versionId) {
        TplTemplate template = templateMapper.selectById(templateId);
        if (template == null) {
            throw new BizException("模板不存在");
        }
        TplTemplateVersion version = versionMapper.selectById(versionId);
        if (version == null || !version.getTemplateId().equals(templateId)) {
            throw new BizException("版本不存在或不属于该模板");
        }
        template.setCurrentVersionId(versionId);
        return templateMapper.updateById(template) > 0;
    }

    @Override
    public boolean toggleStatus(Long templateId, Integer status) {
        TplTemplate template = templateMapper.selectById(templateId);
        if (template == null) {
            throw new BizException("模板不存在");
        }
        template.setStatus(status);
        return templateMapper.updateById(template) > 0;
    }

    @Override
    public boolean deleteTemplate(Long templateId) {
        TplTemplate template = templateMapper.selectById(templateId);
        if (template == null) {
            throw new BizException("模板不存在");
        }
        // 逻辑删除模板
        templateMapper.deleteById(templateId);
        // 逻辑删除版本
        LambdaQueryWrapper<TplTemplateVersion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TplTemplateVersion::getTemplateId, templateId);
        versionMapper.delete(wrapper);
        // 删除物理文件（在 DB 删除之后执行，避免文件已删但 DB 回滚导致不一致）
        templateStorageService.deleteTemplateFiles(templateId);
        return true;
    }

    @Override
    public PageResult<TplTemplateVO> listTemplates(TemplateQueryRequest request) {
        Page<TplTemplate> page = new Page<>(request.getPageNum(), request.getPageSize());
        LambdaQueryWrapper<TplTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(request.getTemplateName()), TplTemplate::getTemplateName, request.getTemplateName())
                .eq(StringUtils.hasText(request.getScenario()), TplTemplate::getScenario, request.getScenario())
                .eq(request.getStatus() != null, TplTemplate::getStatus, request.getStatus())
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
            throw new BizException("模板不存在");
        }

        TplTemplateDetailVO vo = new TplTemplateDetailVO();
        BeanUtils.copyProperties(template, vo);

        // 当前版本
        if (template.getCurrentVersionId() != null) {
            TplTemplateVersion current = versionMapper.selectById(template.getCurrentVersionId());
            if (current != null) {
                vo.setCurrentVersion(convertToVersionVO(current));
            }
        }

        // 历史版本
        List<TplTemplateVersion> versions = versionMapper.selectByTemplateId(templateId);
        List<TplTemplateVersionVO> history = new ArrayList<>();
        for (TplTemplateVersion v : versions) {
            if (template.getCurrentVersionId() == null || !v.getId().equals(template.getCurrentVersionId())) {
                history.add(convertToVersionVO(v));
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

        return vo;
    }

    @Override
    public TplTemplateDetailVO getPublicDetail(Long templateId) {
        TplTemplate template = templateMapper.selectById(templateId);
        if (template == null) {
            throw new BizException("模板不存在");
        }
        if (!TemplateStatusEnum.ONLINE.getCode().equals(template.getStatus())) {
            throw new BizException("模板未上线");
        }
        return getDetail(templateId);
    }

    @Override
    public List<TplTemplateVO> listOnlineTemplates(String scenario) {
        List<TplTemplate> list = templateMapper.selectOnlineList(scenario);
        List<TplTemplateVO> voList = new ArrayList<>();
        for (TplTemplate t : list) {
            voList.add(convertToVO(t));
        }
        return voList;
    }

    private TplTemplateVO convertToVO(TplTemplate template) {
        TplTemplateVO vo = new TplTemplateVO();
        BeanUtils.copyProperties(template, vo);
        if (template.getCurrentVersionId() != null) {
            TplTemplateVersion version = versionMapper.selectById(template.getCurrentVersionId());
            if (version != null) {
                vo.setCurrentVersionNo(version.getVersionNo());
            }
        }
        return vo;
    }

    private TplTemplateVersionVO convertToVersionVO(TplTemplateVersion version) {
        TplTemplateVersionVO vo = new TplTemplateVersionVO();
        BeanUtils.copyProperties(version, vo);
        return vo;
    }

    private List<FileNodeVO> buildFileTree(List<String> filePaths) {
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
}
