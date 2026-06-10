package com.aeisp.library.service.impl;

import com.aeisp.common.PageResult;
import com.aeisp.common.exception.BizException;
import com.aeisp.library.code.LibraryErrorCode;
import com.aeisp.library.dto.request.CreateLibraryRequest;
import com.aeisp.library.dto.request.LibraryQueryRequest;
import com.aeisp.library.dto.request.UpdateLibraryRequest;
import com.aeisp.library.dto.vo.LibResourceDetailVO;
import com.aeisp.library.dto.vo.LibResourceVO;
import com.aeisp.library.entity.LibResource;
import com.aeisp.library.mapper.LibResourceMapper;
import com.aeisp.library.service.LibraryResourceService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class LibraryResourceServiceImpl implements LibraryResourceService {

    private final LibResourceMapper resourceMapper;

    public LibraryResourceServiceImpl(LibResourceMapper resourceMapper) {
        this.resourceMapper = resourceMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createLibrary(CreateLibraryRequest request) {
        LibResource resource = new LibResource();
        resource.setResourceName(request.getResourceName());
        resource.setDescription(request.getDescription());
        resource.setCreatedAt(LocalDateTime.now());
        resourceMapper.insert(resource);
        return true;
    }

    @Override
    public boolean updateLibraryInfo(Long resourceId, UpdateLibraryRequest request) {
        LibResource resource = resourceMapper.selectById(resourceId);
        if (resource == null) {
            throw new BizException(LibraryErrorCode.LIBRARY_NOT_FOUND);
        }
        resource.setResourceName(request.getResourceName());
        resource.setDescription(request.getDescription());
        resource.setUpdatedAt(LocalDateTime.now());
        return resourceMapper.updateById(resource) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteLibrary(Long resourceId) {
        LibResource resource = resourceMapper.selectById(resourceId);
        if (resource == null) {
            throw new BizException(LibraryErrorCode.LIBRARY_NOT_FOUND);
        }
        resourceMapper.deleteById(resourceId);
        return true;
    }

    @Override
    public PageResult<LibResourceVO> listLibraries(LibraryQueryRequest request) {
        LambdaQueryWrapper<LibResource> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LibResource::getDeleted, 0);

        if (StringUtils.hasText(request.getResourceName())) {
            wrapper.like(LibResource::getResourceName, request.getResourceName());
        }

        wrapper.orderByDesc(LibResource::getCreatedAt);

        Page<LibResource> page = new Page<>(request.getPageNum(), request.getPageSize());
        Page<LibResource> resultPage = resourceMapper.selectPage(page, wrapper);

        List<LibResourceVO> voList = resultPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return PageResult.of(resultPage, voList);
    }

    @Override
    public LibResourceDetailVO getDetail(Long resourceId) {
        LibResource resource = resourceMapper.selectById(resourceId);
        if (resource == null) {
            throw new BizException(LibraryErrorCode.LIBRARY_NOT_FOUND);
        }
        LibResourceDetailVO vo = new LibResourceDetailVO();
        BeanUtils.copyProperties(convertToVO(resource), vo);
        return vo;
    }

    @Override
    public List<LibResourceVO> listOnlineLibraries() {
        LambdaQueryWrapper<LibResource> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LibResource::getDeleted, 0)
                .orderByDesc(LibResource::getCreatedAt);

        return resourceMapper.selectList(wrapper).stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    private LibResourceVO convertToVO(LibResource resource) {
        LibResourceVO vo = new LibResourceVO();
        BeanUtils.copyProperties(resource, vo);
        return vo;
    }
}
