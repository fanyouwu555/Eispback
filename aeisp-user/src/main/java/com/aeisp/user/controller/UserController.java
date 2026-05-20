package com.aeisp.user.controller;

import com.aeisp.common.PageResult;
import com.aeisp.common.Result;
import com.aeisp.common.constant.ResultCode;
import com.aeisp.common.util.TokenBlacklistUtil;
import com.aeisp.system.annotation.OperationLog;
import com.aeisp.user.request.*;
import com.aeisp.user.service.UsrUserService;
import com.aeisp.user.vo.UserImportResultVO;
import com.aeisp.user.vo.UsrUserVO;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import com.aeisp.common.exception.BizException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 用户管理 Controller。
 *
 * <p>提供后台用户管理接口，包括列表查询、详情、创建、更新、状态修改、密码重置、
 * 时长调整及批量导入。路径前缀 {@code /api/v1/users}。</p>
 *
 * @author AEISP Team
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UsrUserService usrUserService;
    private final TokenBlacklistUtil tokenBlacklistUtil;
    private final PasswordEncoder passwordEncoder;

    /**
     * 用户列表分页查询。
     */
    @PreAuthorize("hasAuthority('user:read')")
    @GetMapping
    public Result<PageResult<UsrUserVO>> listUsers(UserQueryRequest request) {
        PageResult<UsrUserVO> result = usrUserService.listUsers(request);
        return Result.success(result);
    }

    /**
     * 用户详情。
     */
    @PreAuthorize("hasAuthority('user:read')")
    @GetMapping("/{id}")
    public Result<UsrUserVO> getUserDetail(@PathVariable Long id) {
        UsrUserVO vo = usrUserService.getUserDetail(id);
        if (vo == null) {
            return Result.error(ResultCode.NOT_FOUND, "用户不存在");
        }
        return Result.success(vo);
    }

    /**
     * 后台创建用户。
     */
    @PreAuthorize("hasAuthority('user:create')")
    @PostMapping
    public Result<String> createUser(@Valid @RequestBody UserCreateRequest request) {
        String password = usrUserService.createByAdmin(request);
        return password != null ? Result.success(password, "创建成功") : Result.error(ResultCode.INTERNAL_ERROR, "创建失败");
    }

    /**
     * 更新用户信息。
     */
    @PreAuthorize("hasAuthority('user:update')")
    @PutMapping("/{id}")
    public Result<Void> updateUser(@PathVariable Long id,
                                   @Valid @RequestBody UserUpdateRequest request) {
        request.setId(id);
        boolean success = usrUserService.updateUser(request);
        return success ? Result.success() : Result.error(ResultCode.INTERNAL_ERROR, "更新失败");
    }

    /**
     * 修改用户状态。
     */
    @PreAuthorize("hasAuthority('user:status:update')")
    @OperationLog(module = "用户管理", operation = "修改状态", sensitivity = 2)
    @PatchMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id,
                                     @Valid @RequestBody StatusUpdateRequest request) {
        verifyAdminPassword(request.getAdminPassword());
        boolean success = usrUserService.updateStatus(id, request.getStatus(), request.getReason());
        return success ? Result.success() : Result.error(ResultCode.INTERNAL_ERROR, "状态修改失败");
    }

    /**
     * 重置用户密码。
     *
     * <p>后台生成 10 位强密码（含大写、小写、数字、特殊字符各至少一位），
     * 强制注销该用户所有在线会话。</p>
     */
    @PreAuthorize("hasAuthority('user:password:reset')")
    @OperationLog(module = "用户管理", operation = "重置密码", sensitivity = 2)
    @PostMapping("/{id}/reset-password")
    public Result<String> resetPassword(@PathVariable Long id,
                                        @Valid @RequestBody ResetPasswordRequest request) {
        verifyAdminPassword(request.getAdminPassword());
        String newPassword = StringUtils.hasText(request.getNewPassword())
                ? request.getNewPassword()
                : usrUserService.generateStrongPassword();
        boolean success = usrUserService.resetPassword(id, newPassword);
        if (success) {
            tokenBlacklistUtil.addUserToBlacklist(id,
                    new java.util.Date(System.currentTimeMillis() + 86400000L));
        }
        return success ? Result.success(newPassword, "密码已重置，请通知用户尽快修改") :
                Result.error(ResultCode.INTERNAL_ERROR, "密码重置失败");
    }

    /**
     * 手动调整用户剩余时长。
     */
    @PreAuthorize("hasAuthority('user:duration:adjust')")
    @OperationLog(module = "用户管理", operation = "调整时长", sensitivity = 2)
    @PostMapping("/{id}/adjust-duration")
    public Result<Void> adjustDuration(@PathVariable Long id,
                                       @Valid @RequestBody AdjustDurationRequest request) {
        verifyAdminPassword(request.getAdminPassword());
        request.setUserId(id);
        boolean success = usrUserService.adjustDuration(request);
        return success ? Result.success() : Result.error(ResultCode.INTERNAL_ERROR, "时长调整失败");
    }

    /**
     * 批量导入用户。
     */
    @PreAuthorize("hasAuthority('user:create')")
    @PostMapping("/batch")
    public Result<Void> batchCreate(@Valid @RequestBody UserBatchCreateRequest request) {
        boolean success = usrUserService.batchCreate(request);
        return success ? Result.success() : Result.error(ResultCode.INTERNAL_ERROR, "批量导入失败");
    }

    /**
     * Excel 批量导入用户。
     */
    @PreAuthorize("hasAuthority('user:create')")
    @PostMapping("/import-excel")
    public Result<UserImportResultVO> importExcel(@RequestParam("file") MultipartFile file) {
        UserImportResultVO result = usrUserService.importFromExcel(file);
        return Result.success(result);
    }

    /**
     * 导出用户列表到 Excel。
     */
    @PreAuthorize("hasAuthority('user:read')")
    @GetMapping("/export-excel")
    public void exportExcel(UserQueryRequest request, HttpServletResponse response) {
        usrUserService.exportToExcel(request, response);
    }

    /**
     * 校验当前管理员的登录密码（敏感操作二次确认）。
     *
     * @param adminPassword 管理员明文密码
     */
    private void verifyAdminPassword(String adminPassword) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof UserDetails details)) {
            throw new BizException("无法获取当前管理员信息");
        }
        if (!passwordEncoder.matches(adminPassword, details.getPassword())) {
            throw new BizException("管理员密码错误");
        }
    }
}
