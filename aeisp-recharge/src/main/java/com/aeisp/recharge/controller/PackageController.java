package com.aeisp.recharge.controller;

import com.aeisp.common.PageResult;
import com.aeisp.common.Result;
import com.aeisp.recharge.dto.PackageDTO;
import com.aeisp.recharge.dto.PackageQueryRequest;
import com.aeisp.recharge.dto.PackageVO;
import com.aeisp.recharge.service.PackageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 时长套餐控制器。
 *
 * <p>提供套餐的增删改查及前端有效套餐列表接口。</p>
 *
 * @author AEISP Team
 */
@Tag(name = "时长套餐", description = "充值套餐管理")
@RestController
@RequestMapping("/api/v1/recharge/packages")
@RequiredArgsConstructor
public class PackageController {

    private final PackageService packageService;

    /**
     * 创建套餐。
     *
     * @param dto 套餐数据
     * @return 操作结果
     */
    @PreAuthorize("hasAuthority('finance:package:manage')")
    @Operation(summary = "创建套餐")
    @PostMapping
    public Result<Boolean> createPackage(@Validated @RequestBody PackageDTO dto) {
        return Result.success(packageService.createPackage(dto));
    }

    /**
     * 更新套餐。
     *
     * @param id  套餐 ID
     * @param dto 套餐数据
     * @return 操作结果
     */
    @PreAuthorize("hasAuthority('finance:package:manage')")
    @Operation(summary = "更新套餐")
    @PutMapping("/{id}")
    public Result<Boolean> updatePackage(@PathVariable Long id, @Validated @RequestBody PackageDTO dto) {
        return Result.success(packageService.updatePackage(id, dto));
    }

    /**
     * 删除套餐。
     *
     * @param id 套餐 ID
     * @return 操作结果
     */
    @PreAuthorize("hasAuthority('finance:package:manage')")
    @Operation(summary = "删除套餐")
    @DeleteMapping("/{id}")
    public Result<Boolean> deletePackage(@PathVariable Long id) {
        return Result.success(packageService.deletePackage(id));
    }

    /**
     * 查询前端展示的有效套餐列表。
     *
     * @return 有效套餐列表
     */
    @Operation(summary = "有效套餐列表")
    @GetMapping("/active")
    public Result<List<PackageVO>> listActivePackages() {
        return Result.success(packageService.listActivePackages());
    }

    /**
     * 分页查询套餐列表。
     *
     * @param request 查询条件
     * @return 分页结果
     */
    @PreAuthorize("hasAuthority('finance:package:manage')")
    @Operation(summary = "套餐列表")
    @GetMapping
    public Result<PageResult<PackageVO>> listPackages(PackageQueryRequest request) {
        return Result.success(packageService.listPackages(request));
    }
}
