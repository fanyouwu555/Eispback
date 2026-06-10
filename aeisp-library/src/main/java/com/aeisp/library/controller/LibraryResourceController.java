package com.aeisp.library.controller;

import com.aeisp.common.PageResult;
import com.aeisp.common.Result;
import com.aeisp.library.dto.request.CreateLibraryRequest;
import com.aeisp.library.dto.request.LibraryQueryRequest;
import com.aeisp.library.dto.request.UpdateLibraryRequest;
import com.aeisp.library.dto.vo.LibResourceDetailVO;
import com.aeisp.library.dto.vo.LibResourceVO;
import com.aeisp.library.service.LibraryResourceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/library-resources")
@RequiredArgsConstructor
@Tag(name = "库资源管理", description = "库资源名称与描述管理")
public class LibraryResourceController {

    private final LibraryResourceService libraryResourceService;

    @PreAuthorize("hasAuthority('library:create')")
    @PostMapping
    @Operation(summary = "创建库资源", description = "创建库资源记录（仅名称+描述）")
    public Result<Boolean> createLibrary(@Valid @RequestBody CreateLibraryRequest request) {
        return Result.success(libraryResourceService.createLibrary(request));
    }

    @PreAuthorize("hasAuthority('library:update')")
    @PutMapping("/{id}")
    @Operation(summary = "修改库资源", description = "修改库资源名称和描述")
    public Result<Boolean> updateLibraryInfo(@PathVariable Long id, @Valid @RequestBody UpdateLibraryRequest request) {
        return Result.success(libraryResourceService.updateLibraryInfo(id, request));
    }

    @PreAuthorize("hasAuthority('library:delete')")
    @DeleteMapping("/{id}")
    @Operation(summary = "删除库资源", description = "逻辑删除库资源")
    public Result<Boolean> deleteLibrary(@PathVariable Long id) {
        return Result.success(libraryResourceService.deleteLibrary(id));
    }

    @PreAuthorize("hasAuthority('library:read')")
    @GetMapping
    @Operation(summary = "库资源列表", description = "分页查询库资源列表")
    public Result<PageResult<LibResourceVO>> listLibraries(LibraryQueryRequest request) {
        return Result.success(libraryResourceService.listLibraries(request));
    }

    @PreAuthorize("hasAuthority('library:read')")
    @GetMapping("/{id}")
    @Operation(summary = "库资源详情", description = "获取库资源详情")
    public Result<LibResourceDetailVO> getDetail(@PathVariable Long id) {
        return Result.success(libraryResourceService.getDetail(id));
    }

    @GetMapping("/public")
    @Operation(summary = "库资源下拉列表", description = "查询所有库资源（前端下拉选择用）")
    public Result<List<LibResourceVO>> listOnlineLibraries() {
        return Result.success(libraryResourceService.listOnlineLibraries());
    }
}
