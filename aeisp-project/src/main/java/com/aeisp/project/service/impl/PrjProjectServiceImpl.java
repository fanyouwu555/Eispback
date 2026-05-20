package com.aeisp.project.service.impl;

import com.aeisp.common.PageResult;
import com.aeisp.common.exception.BizException;
import com.aeisp.project.dto.request.ProjectQueryRequest;
import com.aeisp.project.dto.vo.PrjProjectVO;
import com.aeisp.project.entity.PrjProject;
import com.aeisp.project.enums.ProjectStatusEnum;
import com.aeisp.project.mapper.PrjProjectMapper;
import com.aeisp.project.service.PrjProjectService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PrjProjectServiceImpl implements PrjProjectService {

    private final PrjProjectMapper projectMapper;

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
            throw new BizException("项目不存在");
        }
        return convertToVO(project);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean archiveProject(Long id) {
        PrjProject project = projectMapper.selectById(id);
        if (project == null) {
            throw new BizException("项目不存在");
        }
        if (Integer.valueOf(2).equals(project.getStatus())) {
            throw new BizException("项目已归档，无需重复操作");
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
            throw new BizException("项目不存在");
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
}