package com.aeisp.project.service;

import com.aeisp.common.PageResult;
import com.aeisp.project.dto.request.ProjectQueryRequest;
import com.aeisp.project.dto.vo.ClientProjectVO;
import com.aeisp.project.dto.vo.PrjProjectVO;

public interface PrjProjectService {

    PageResult<PrjProjectVO> listProjects(ProjectQueryRequest request);

    PrjProjectVO getDetail(Long id);

    boolean archiveProject(Long id);

    boolean deleteProject(Long id);

    // --- client-facing API methods ---

    ClientProjectVO createClientProject(Long userId, Long templateId, String projectName);

    PageResult<ClientProjectVO> listClientProjects(Long userId, int pageNum, int pageSize);

    String uploadProjectZip(Long projectId, Long userId, byte[] zipData);

    void updateClientProject(Long projectId, Long userId, String projectName);

    void deleteClientProject(Long projectId, Long userId);
}