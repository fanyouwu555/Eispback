package com.aeisp.library.controller;

import com.aeisp.common.Result;
import com.aeisp.common.exception.BizException;
import com.aeisp.common.security.CustomUserDetails;
import com.aeisp.library.code.LibraryErrorCode;
import com.aeisp.library.entity.LibResource;
import com.aeisp.library.entity.LibResourceVersion;
import com.aeisp.library.mapper.LibResourceMapper;
import com.aeisp.library.mapper.LibResourceVersionMapper;
import com.aeisp.library.service.LibraryResourceService;
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

/**
 * 客户端库资源下载 Controller。
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/client/library-resources")
@RequiredArgsConstructor
@Tag(name = "客户端库资源下载", description = "Unity 客户端库资源下载")
public class ClientLibraryController {

    private final LibResourceMapper resourceMapper;
    private final LibResourceVersionMapper versionMapper;
    private final LibraryResourceService libraryResourceService;

    @GetMapping("/{id}/download")
    @Operation(summary = "客户端下载库资源", description = "Unity 客户端下载库资源 ZIP")
    public ResponseEntity<Void> downloadLibrary(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long id,
            @RequestParam(value = "versionNo", required = false) String versionNo) {

        LibResource resource = resourceMapper.selectById(id);
        if (resource == null) {
            throw new BizException(LibraryErrorCode.LIBRARY_NOT_FOUND);
        }
        if (resource.getStatus() == null || resource.getStatus() != 1) {
            throw new BizException(LibraryErrorCode.LIBRARY_NOT_PUBLISHED);
        }

        String targetVersion = versionNo != null ? versionNo : getCurrentVersionNo(id);
        if (targetVersion == null) {
            throw new BizException(LibraryErrorCode.VERSION_NOT_FOUND);
        }

        LibResourceVersion version = versionMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<LibResourceVersion>()
                        .eq(LibResourceVersion::getResourceId, id)
                        .eq(LibResourceVersion::getVersionNo, targetVersion));
        if (version == null || version.getStorageUrl() == null) {
            throw new BizException(LibraryErrorCode.VERSION_NOT_FOUND);
        }

        libraryResourceService.incrementDownloadCount(id);

        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, version.getStorageUrl())
                .build();
    }

    private String getCurrentVersionNo(Long resourceId) {
        LibResource resource = resourceMapper.selectById(resourceId);
        if (resource == null || resource.getCurrentVersionId() == null) {
            return null;
        }
        LibResourceVersion version = versionMapper.selectById(resource.getCurrentVersionId());
        return version != null ? version.getVersionNo() : null;
    }
}
