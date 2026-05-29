package com.aeisp.library.controller;

import com.aeisp.common.PageResult;
import com.aeisp.common.Result;
import com.aeisp.library.dto.request.CreateLibraryRequest;
import com.aeisp.library.dto.request.LibraryQueryRequest;
import com.aeisp.library.dto.request.UpdateLibraryRequest;
import com.aeisp.library.dto.vo.LibResourceDetailVO;
import com.aeisp.library.dto.vo.LibResourceVO;
import com.aeisp.library.service.LibraryResourceService;
import com.aeisp.library.service.LibraryStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/library-resources")
@RequiredArgsConstructor
@Tag(name = "库资源管理", description = "库资源的上传、版本控制、上下线和统计")
public class LibraryResourceController {

    private final LibraryResourceService libraryResourceService;
    private final LibraryStorageService libraryStorageService;

    @PreAuthorize("hasAuthority('library:create')")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "创建库资源", description = "创建库资源并上传首个版本的 ZIP 文件")
    public Result<Boolean> createLibrary(@Valid @ModelAttribute CreateLibraryRequest request) {
        return Result.success(libraryResourceService.createLibrary(request));
    }

    @PreAuthorize("hasAuthority('library:update')")
    @PutMapping("/{id}")
    @Operation(summary = "修改库资源信息", description = "仅修改库资源基础信息")
    public Result<Boolean> updateLibraryInfo(@PathVariable Long id, @Valid @RequestBody UpdateLibraryRequest request) {
        return Result.success(libraryResourceService.updateLibraryInfo(id, request));
    }

    @PreAuthorize("hasAuthority('library:version:manage')")
    @PostMapping(value = "/{id}/versions", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "上传新版本", description = "为指定库资源上传新版本")
    public Result<Boolean> uploadNewVersion(@PathVariable Long id,
                                            @RequestParam("zipFile") MultipartFile zipFile,
                                            @RequestParam("versionNo") String versionNo,
                                            @RequestParam(value = "changelog", required = false) String changelog) {
        return Result.success(libraryResourceService.uploadNewVersion(id, zipFile, versionNo, changelog));
    }

    @PreAuthorize("hasAuthority('library:version:manage')")
    @PostMapping("/{id}/rollback/{versionId}")
    @Operation(summary = "回滚版本", description = "将库资源回滚到指定版本")
    public Result<Boolean> rollbackVersion(@PathVariable Long id, @PathVariable Long versionId) {
        return Result.success(libraryResourceService.rollbackVersion(id, versionId));
    }

    @PreAuthorize("hasAuthority('library:update')")
    @PostMapping("/{id}/status")
    @Operation(summary = "上下线切换", description = "切换库资源的上线/下线状态")
    public Result<Boolean> toggleStatus(@PathVariable Long id, @RequestParam Integer status) {
        return Result.success(libraryResourceService.toggleStatus(id, status));
    }

    @PreAuthorize("hasAuthority('library:delete')")
    @DeleteMapping("/{id}")
    @Operation(summary = "删除库资源", description = "逻辑删除库资源及其所有版本")
    public Result<Boolean> deleteLibrary(@PathVariable Long id) {
        return Result.success(libraryResourceService.deleteLibrary(id));
    }

    @PreAuthorize("hasAuthority('library:read')")
    @GetMapping
    @Operation(summary = "库资源列表", description = "分页查询库资源列表（管理端）")
    public Result<PageResult<LibResourceVO>> listLibraries(LibraryQueryRequest request) {
        return Result.success(libraryResourceService.listLibraries(request));
    }

    @PreAuthorize("hasAuthority('library:read')")
    @GetMapping("/{id}")
    @Operation(summary = "库资源详情", description = "获取库资源详情（含当前版本、历史版本、文件结构）")
    public Result<LibResourceDetailVO> getDetail(@PathVariable Long id) {
        return Result.success(libraryResourceService.getDetail(id));
    }

    @PreAuthorize("hasAuthority('library:read')")
    @GetMapping("/{id}/files")
    @Operation(summary = "文件结构", description = "获取库资源当前版本的文件树结构")
    public Result<List<String>> listFiles(@PathVariable Long id) {
        LibResourceDetailVO detail = libraryResourceService.getDetail(id);
        String targetVersion = detail.getCurrentVersionNo();
        if (targetVersion == null) {
            return Result.success(List.of());
        }
        return Result.success(libraryStorageService.listFiles(id, targetVersion));
    }

    @PreAuthorize("hasAuthority('library:read')")
    @GetMapping("/{id}/files/content")
    @Operation(summary = "读取文件内容", description = "读取库资源版本中指定文件的内容")
    public Result<byte[]> readFileContent(@PathVariable Long id,
                                          @RequestParam String versionNo,
                                          @RequestParam String filePath) {
        byte[] content = libraryStorageService.readFile(id, versionNo, filePath);
        return Result.success(content);
    }

    @GetMapping("/public")
    @Operation(summary = "上架库资源列表", description = "查询所有上架的库资源列表（前端下拉选择用）")
    public Result<List<LibResourceVO>> listOnlineLibraries() {
        return Result.success(libraryResourceService.listOnlineLibraries());
    }
}
