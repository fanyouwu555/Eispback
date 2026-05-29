package com.aeisp.library.service.impl;

import com.aeisp.common.PageResult;
import com.aeisp.common.exception.BizException;
import com.aeisp.common.service.ResourceServerService;
import com.aeisp.library.code.LibraryErrorCode;
import com.aeisp.library.dto.request.CreateLibraryRequest;
import com.aeisp.library.dto.request.LibraryQueryRequest;
import com.aeisp.library.dto.request.UpdateLibraryRequest;
import com.aeisp.library.dto.vo.LibResourceDetailVO;
import com.aeisp.library.dto.vo.LibResourceVO;
import com.aeisp.library.dto.vo.LibResourceVersionVO;
import com.aeisp.library.entity.LibResource;
import com.aeisp.library.entity.LibResourceVersion;
import com.aeisp.library.mapper.LibResourceMapper;
import com.aeisp.library.mapper.LibResourceVersionMapper;
import com.aeisp.library.service.LibraryResourceService;
import com.aeisp.library.service.LibraryStorageService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LibraryResourceServiceImpl implements LibraryResourceService {

    private final LibResourceMapper resourceMapper;
    private final LibResourceVersionMapper versionMapper;
    private final LibraryStorageService libraryStorageService;
    private final ResourceServerService resourceServerService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createLibrary(CreateLibraryRequest request) {
        // 1. 保存主表
        LibResource resource = new LibResource();
        resource.setResourceCode(generateResourceCode());
        resource.setResourceName(request.getResourceName());
        resource.setDescription(request.getDescription());
        resource.setStatus(0);
        resource.setDownloadCount(0L);
        resource.setCreatedAt(LocalDateTime.now());
        resourceMapper.insert(resource);

        // 2. 存储 ZIP 到本地
        String relativePath = libraryStorageService.storeZip(resource.getId(), request.getVersionNo(), request.getZipFile());
        String zipAbsPath = libraryStorageService.getZipAbsolutePath(resource.getId(), request.getVersionNo());

        // 3. 上传 ZIP 到资源服务器
        String storageUrl = uploadZipToServer(resource.getId(), request.getVersionNo(), zipAbsPath, relativePath);

        // 4. 解压并上传提取文件
        String extractDir = new File(zipAbsPath).getParent() + "/extracted";
        libraryStorageService.extractZip(zipAbsPath, extractDir);
        resourceServerService.uploadExtractedFiles(resource.getId(), request.getVersionNo(), new File(extractDir));

        // 5. 计算 MD5
        String fileHash = calcFileHash(request.getZipFile());

        // 6. 保存版本记录
        LibResourceVersion version = new LibResourceVersion();
        version.setResourceId(resource.getId());
        version.setVersionNo(request.getVersionNo());
        version.setFilePath(relativePath);
        version.setStorageUrl(storageUrl);
        version.setFileSize(request.getZipFile().getSize());
        version.setFileHash(fileHash);
        version.setChangelog(request.getChangelog());
        version.setCreatedAt(LocalDateTime.now());
        versionMapper.insert(version);

        // 7. 更新当前版本
        resource.setCurrentVersionId(version.getId());
        resourceMapper.updateById(resource);

        // 8. 清理临时文件
        libraryStorageService.deleteVersionFiles(resource.getId(), request.getVersionNo());

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
    public boolean uploadNewVersion(Long resourceId, MultipartFile zipFile, String versionNo, String changelog) {
        LibResource resource = resourceMapper.selectById(resourceId);
        if (resource == null) {
            throw new BizException(LibraryErrorCode.LIBRARY_NOT_FOUND);
        }

        // 检查版本号是否已存在
        LibResourceVersion existing = versionMapper.selectOne(
                new LambdaQueryWrapper<LibResourceVersion>()
                        .eq(LibResourceVersion::getResourceId, resourceId)
                        .eq(LibResourceVersion::getVersionNo, versionNo));
        if (existing != null) {
            throw new BizException(LibraryErrorCode.VERSION_EXISTS);
        }

        // 存储 ZIP
        String relativePath = libraryStorageService.storeZip(resourceId, versionNo, zipFile);
        String zipAbsPath = libraryStorageService.getZipAbsolutePath(resourceId, versionNo);

        // 上传 ZIP 到资源服务器
        String storageUrl = uploadZipToServer(resourceId, versionNo, zipAbsPath, relativePath);

        // 解压并上传提取文件
        String extractDir = new File(zipAbsPath).getParent() + "/extracted";
        libraryStorageService.extractZip(zipAbsPath, extractDir);
        resourceServerService.uploadExtractedFiles(resourceId, versionNo, new File(extractDir));

        // 计算 MD5
        String fileHash = calcFileHash(zipFile);

        // 保存版本记录
        LibResourceVersion version = new LibResourceVersion();
        version.setResourceId(resourceId);
        version.setVersionNo(versionNo);
        version.setFilePath(relativePath);
        version.setStorageUrl(storageUrl);
        version.setFileSize(zipFile.getSize());
        version.setFileHash(fileHash);
        version.setChangelog(changelog);
        version.setCreatedAt(LocalDateTime.now());
        versionMapper.insert(version);

        // 更新当前版本
        resource.setCurrentVersionId(version.getId());
        resource.setUpdatedAt(LocalDateTime.now());
        resourceMapper.updateById(resource);

        // 清理临时文件
        libraryStorageService.deleteVersionFiles(resourceId, versionNo);

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean rollbackVersion(Long resourceId, Long versionId) {
        LibResource resource = resourceMapper.selectById(resourceId);
        if (resource == null) {
            throw new BizException(LibraryErrorCode.LIBRARY_NOT_FOUND);
        }

        LibResourceVersion version = versionMapper.selectById(versionId);
        if (version == null || !version.getResourceId().equals(resourceId)) {
            throw new BizException(LibraryErrorCode.VERSION_NOT_BELONG);
        }

        resource.setCurrentVersionId(versionId);
        resource.setUpdatedAt(LocalDateTime.now());
        return resourceMapper.updateById(resource) > 0;
    }

    @Override
    public boolean toggleStatus(Long resourceId, Integer status) {
        LibResource resource = resourceMapper.selectById(resourceId);
        if (resource == null) {
            throw new BizException(LibraryErrorCode.LIBRARY_NOT_FOUND);
        }
        resource.setStatus(status);
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

        // 逻辑删除主表
        resourceMapper.deleteById(resourceId);

        // 逻辑删除所有版本
        versionMapper.delete(
                new LambdaQueryWrapper<LibResourceVersion>()
                        .eq(LibResourceVersion::getResourceId, resourceId));

        return true;
    }

    @Override
    public PageResult<LibResourceVO> listLibraries(LibraryQueryRequest request) {
        LambdaQueryWrapper<LibResource> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LibResource::getDeleted, 0);

        if (StringUtils.hasText(request.getResourceName())) {
            wrapper.like(LibResource::getResourceName, request.getResourceName());
        }
        if (request.getStatus() != null) {
            wrapper.eq(LibResource::getStatus, request.getStatus());
        }
        if (request.getIds() != null && !request.getIds().isEmpty()) {
            wrapper.in(LibResource::getId, request.getIds());
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

        LibResourceDetailVO detailVO = new LibResourceDetailVO();
        BeanUtils.copyProperties(convertToVO(resource), detailVO);

        // 查询所有版本
        List<LibResourceVersion> versions = versionMapper.selectList(
                new LambdaQueryWrapper<LibResourceVersion>()
                        .eq(LibResourceVersion::getResourceId, resourceId)
                        .eq(LibResourceVersion::getDeleted, 0)
                        .orderByDesc(LibResourceVersion::getCreatedAt));

        List<LibResourceVersionVO> versionVOList = new ArrayList<>();
        for (LibResourceVersion version : versions) {
            LibResourceVersionVO versionVO = new LibResourceVersionVO();
            BeanUtils.copyProperties(version, versionVO);
            versionVO.setIsCurrent(resource.getCurrentVersionId() != null && resource.getCurrentVersionId().equals(version.getId()));
            versionVOList.add(versionVO);
        }
        detailVO.setVersionList(versionVOList);

        // 当前版本文件树
        String currentVersionNo = getCurrentVersionNo(resource);
        if (currentVersionNo != null) {
            detailVO.setFileTree(libraryStorageService.listFiles(resourceId, currentVersionNo));
        } else {
            detailVO.setFileTree(new ArrayList<>());
        }

        return detailVO;
    }

    @Override
    public List<LibResourceVO> listOnlineLibraries() {
        LambdaQueryWrapper<LibResource> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LibResource::getStatus, 1)
                .eq(LibResource::getDeleted, 0)
                .orderByDesc(LibResource::getCreatedAt);

        return resourceMapper.selectList(wrapper).stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public void incrementDownloadCount(Long resourceId) {
        LibResource resource = resourceMapper.selectById(resourceId);
        if (resource != null) {
            resource.setDownloadCount(resource.getDownloadCount() + 1);
            resourceMapper.updateById(resource);
        }
    }

    private LibResourceVO convertToVO(LibResource resource) {
        LibResourceVO vo = new LibResourceVO();
        BeanUtils.copyProperties(resource, vo);
        vo.setStatusLabel(getStatusLabel(resource.getStatus()));

        // 查询当前版本信息
        if (resource.getCurrentVersionId() != null) {
            LibResourceVersion version = versionMapper.selectById(resource.getCurrentVersionId());
            if (version != null) {
                vo.setCurrentVersionNo(version.getVersionNo());
                vo.setFileSize(version.getFileSize());
                vo.setFileSizeLabel(formatFileSize(version.getFileSize()));
            }
        }
        return vo;
    }

    private String getCurrentVersionNo(LibResource resource) {
        if (resource.getCurrentVersionId() == null) {
            return null;
        }
        LibResourceVersion version = versionMapper.selectById(resource.getCurrentVersionId());
        return version != null ? version.getVersionNo() : null;
    }

    private String getStatusLabel(Integer status) {
        if (status == null) return "未知";
        return switch (status) {
            case 0 -> "正常";
            case 1 -> "上架";
            case 2 -> "下架";
            case 3 -> "违规";
            default -> "未知";
        };
    }

    private String formatFileSize(Long size) {
        if (size == null || size <= 0) return "-";
        if (size < 1024) return size + " B";
        if (size < 1024 * 1024) return String.format("%.2f KB", size / 1024.0);
        if (size < 1024L * 1024 * 1024) return String.format("%.2f MB", size / (1024.0 * 1024));
        return String.format("%.2f GB", size / (1024.0 * 1024 * 1024));
    }

    private String generateResourceCode() {
        String prefix = "LIB" + java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.BASIC_ISO_DATE);
        String maxCode = resourceMapper.selectMaxResourceCode(prefix);
        int seq = 1;
        if (maxCode != null && maxCode.length() > prefix.length()) {
            try {
                seq = Integer.parseInt(maxCode.substring(prefix.length())) + 1;
            } catch (NumberFormatException e) {
                log.warn("解析最大资源编码序号失败: {}", maxCode);
            }
        }
        return prefix + String.format("%04d", seq);
    }

    private String uploadZipToServer(Long resourceId, String versionNo, String zipAbsPath, String relativePath) {
        try {
            byte[] zipBytes = Files.readAllBytes(java.nio.file.Paths.get(zipAbsPath));
            return resourceServerService.uploadFile(relativePath, zipBytes);
        } catch (IOException e) {
            log.error("上传 ZIP 到资源服务器失败: resourceId={}, versionNo={}", resourceId, versionNo, e);
            throw new BizException(LibraryErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    private String calcFileHash(MultipartFile file) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bytes = file.getBytes();
            byte[] digest = md.digest(bytes);
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException | IOException e) {
            log.warn("计算文件哈希失败", e);
            return null;
        }
    }
}
