package com.aeisp.project.service;

import com.aeisp.common.PageResult;
import com.aeisp.project.dto.request.ProjectQueryRequest;
import com.aeisp.project.dto.vo.PrjProjectVO;

public interface PrjProjectService {

    PageResult<PrjProjectVO> listProjects(ProjectQueryRequest request);

    PrjProjectVO getDetail(Long id);

    boolean archiveProject(Long id);

    boolean deleteProject(Long id);
}