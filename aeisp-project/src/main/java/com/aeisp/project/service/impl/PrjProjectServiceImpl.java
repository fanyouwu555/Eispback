package com.aeisp.project.service.impl;

import com.aeisp.common.PageResult;
import com.aeisp.common.exception.BizException;
import com.aeisp.common.service.ResourceServerService;
import com.aeisp.project.code.ProjectErrorCode;
import com.aeisp.project.dto.request.ProjectQueryRequest;
import com.aeisp.project.dto.vo.ClientProjectVO;
import com.aeisp.project.dto.vo.PrjProjectVO;
import com.aeisp.project.entity.PrjProject;
import com.aeisp.project.enums.ProjectStatusEnum;
import com.aeisp.project.mapper.PrjProjectMapper;
import com.aeisp.project.service.PrjProjectService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class PrjProjectServiceImpl implements PrjProjectService {

    private final PrjProjectMapper projectMapper;
    private final ResourceServerService userResourceServer;

    public PrjProjectServiceImpl(PrjProjectMapper projectMapper,
                                 @Qualifier("userResourceServer") ResourceServerService userResourceServer) {
        this.projectMapper = projectMapper;
        this.userResourceServer = userResourceServer;
    }

    // --- existing admin methods (unchanged) ---

    @Override
    public PageResult<PrjProjectVO> listProjects(ProjectQueryRequest request) {
        Page<PrjProject> page = new Page<>(request.getPageNum(), request.getPageSize());
        LambdaQueryWrapper<PrjProject> wrapper = new LambdaQueryWrapper<>();

        wrapper.like(StringUtils.hasText(request.getKeyword()), PrjProject::getProjectName, request.getKeyword())
                .eq(request.getUserId() != null, PrjProject::getUserId, request.getUserId())
                .eq(request.getStatus() != null, PrjProject::getStatus, request.getStatus())
                .ge(request.getStartTime() != null, PrjProject::getCreatedAt, request.getStartTime())
                .le(request.getEndTime() != null, PrjProject::getCreatedAt, request.getEndTime())
                .orderByDesc(PrjProject::getIsPinned)
                .orderByDesc(PrjProject::getCreatedAt);

        Page<PrjProject> resultPage = projectMapper.selectPage(page, wrapper);
        List<PrjProjectVO> voList = new ArrayList<>();
        for (PrjProject p : resultPage.getRecords()) {
            voList.add(convertToVO(p));
        }
        return PageResult.of(resultPage, voList);
    }

    @Override
    public PrjProjectVO getDetail(Long id) {
        PrjProject project = projectMapper.selectById(id);
        if (project == null) {
            throw new BizException(ProjectErrorCode.PROJECT_NOT_FOUND);
        }
        return convertToVO(project);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean archiveProject(Long id) {
        PrjProject project = projectMapper.selectById(id);
        if (project == null) {
            throw new BizException(ProjectErrorCode.PROJECT_NOT_FOUND);
        }
        if (Integer.valueOf(2).equals(project.getStatus())) {
            throw new BizException(ProjectErrorCode.PROJECT_ALREADY_ARCHIVED);
        }
        project.setStatus(ProjectStatusEnum.ARCHIVED.getCode());
        project.setArchivedAt(LocalDateTime.now());
        return projectMapper.updateById(project) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteProject(Long id) {
        PrjProject project = projectMapper.selectById(id);
        if (project == null) {
            throw new BizException(ProjectErrorCode.PROJECT_NOT_FOUND);
        }
        return projectMapper.deleteById(id) > 0;
    }

    private PrjProjectVO convertToVO(PrjProject project) {
        PrjProjectVO vo = new PrjProjectVO();
        BeanUtils.copyProperties(project, vo);
        ProjectStatusEnum statusEnum = ProjectStatusEnum.fromCode(project.getStatus());
        vo.setStatusLabel(statusEnum != null ? statusEnum.getDescription() : "未知");
        return vo;
    }

    // --- new client methods ---

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ClientProjectVO createClientProject(Long userId, Long templateId, String projectName) {
        PrjProject project = new PrjProject();
        project.setUserId(userId);
        project.setTemplateId(templateId);
        project.setProjectName(StringUtils.hasText(projectName) ? projectName : "未命名项目");
        project.setStatus(ProjectStatusEnum.IN_PROGRESS.getCode());
        projectMapper.insert(project);

        // 生成 resourceUrl: base + {userId}/{projectId}/
        String resourceUrl = userResourceServer.getUrl(userId + "/" + project.getId() + "/");
        project.setResourceUrl(resourceUrl);
        projectMapper.updateById(project);

        return toClientVO(project);
    }

    @Override
    public PageResult<ClientProjectVO> listClientProjects(Long userId, int pageNum, int pageSize) {
        Page<PrjProject> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<PrjProject> wrapper = new LambdaQueryWrapper<PrjProject>()
                .eq(PrjProject::getUserId, userId)
                .orderByDesc(PrjProject::getCreatedAt);

        Page<PrjProject> resultPage = projectMapper.selectPage(page, wrapper);
        List<ClientProjectVO> voList = new ArrayList<>();
        for (PrjProject p : resultPage.getRecords()) {
            voList.add(toClientVO(p));
        }
        return PageResult.of(resultPage, voList);
    }

    @Override
    public String uploadProjectZip(Long projectId, Long userId, byte[] zipData) {
        PrjProject project = projectMapper.selectById(projectId);
        if (project == null) {
            throw new BizException(ProjectErrorCode.PROJECT_NOT_FOUND);
        }
        if (!userId.equals(project.getUserId())) {
            throw new BizException(ProjectErrorCode.PROJECT_ACCESS_DENIED);
        }

        String relativePath = userId + "/" + projectId + "/project.zip";
        return userResourceServer.uploadFile(relativePath, zipData);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateClientProject(Long projectId, Long userId, String projectName) {
        PrjProject project = projectMapper.selectById(projectId);
        if (project == null) {
            throw new BizException(ProjectErrorCode.PROJECT_NOT_FOUND);
        }
        if (!userId.equals(project.getUserId())) {
            throw new BizException(ProjectErrorCode.PROJECT_ACCESS_DENIED);
        }
        project.setProjectName(projectName);
        projectMapper.updateById(project);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteClientProject(Long projectId, Long userId) {
        PrjProject project = projectMapper.selectById(projectId);
        if (project == null) {
            throw new BizException(ProjectErrorCode.PROJECT_NOT_FOUND);
        }
        if (!userId.equals(project.getUserId())) {
            throw new BizException(ProjectErrorCode.PROJECT_ACCESS_DENIED);
        }

        // 删除 NFS 上的用户项目文件
        String relativePath = userId + "/" + projectId + "/";
        try {
            userResourceServer.deleteDirectory(relativePath);
        } catch (Exception e) {
            log.warn("删除项目文件失败，继续逻辑删除: projectId={}, path={}", projectId, relativePath, e);
        }

        // DB 逻辑删除
        projectMapper.deleteById(projectId);
    }

    private ClientProjectVO toClientVO(PrjProject project) {
        ClientProjectVO vo = new ClientProjectVO();
        BeanUtils.copyProperties(project, vo);
        vo.setProjectId(project.getId());
        return vo;
    }
}