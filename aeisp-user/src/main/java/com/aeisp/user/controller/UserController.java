package com.aeisp.user.controller;

import cn.hutool.core.util.RandomUtil;
import com.aeisp.common.PageResult;
import com.aeisp.common.Result;
import com.aeisp.common.constant.ResultCode;
import com.aeisp.user.entity.UsrUser;
import com.aeisp.user.request.*;
import com.aeisp.user.service.UsrUserService;
import com.aeisp.user.vo.UsrUserVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    /**
     * 用户列表分页查询。
     */
    @GetMapping
    public Result<PageResult<UsrUserVO>> listUsers(UserQueryRequest request) {
        PageResult<UsrUserVO> result = usrUserService.listUsers(request);
        return Result.success(result);
    }

    /**
     * 用户详情。
     */
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
    @PostMapping
    public Result<Void> createUser(@Valid @RequestBody UserCreateRequest request) {
        boolean success = usrUserService.createByAdmin(request);
        return success ? Result.success() : Result.error(ResultCode.INTERNAL_ERROR, "创建失败");
    }

    /**
     * 更新用户信息。
     */
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
    @PatchMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id,
                                     @RequestParam Integer status) {
        boolean success = usrUserService.updateStatus(id, status);
        return success ? Result.success() : Result.error(ResultCode.INTERNAL_ERROR, "状态修改失败");
    }

    /**
     * 重置用户密码。
     *
     * <p>后台生成 12 位随机密码并返回给前端（实际应通过邮件/短信通知用户）。</p>
     */
    @PostMapping("/{id}/reset-password")
    public Result<String> resetPassword(@PathVariable Long id) {
        String newPassword = RandomUtil.randomString(12);
        boolean success = usrUserService.resetPassword(id, newPassword);
        return success ? Result.success(newPassword, "密码已重置，请通知用户尽快修改") :
                Result.error(ResultCode.INTERNAL_ERROR, "密码重置失败");
    }

    /**
     * 手动调整用户剩余时长。
     */
    @PostMapping("/{id}/adjust-duration")
    public Result<Void> adjustDuration(@PathVariable Long id,
                                       @Valid @RequestBody AdjustDurationRequest request) {
        request.setUserId(id);
        boolean success = usrUserService.adjustDuration(request);
        return success ? Result.success() : Result.error(ResultCode.INTERNAL_ERROR, "时长调整失败");
    }

    /**
     * 批量导入用户。
     */
    @PostMapping("/batch")
    public Result<Void> batchCreate(@Valid @RequestBody UserBatchCreateRequest request) {
        boolean success = usrUserService.batchCreate(request);
        return success ? Result.success() : Result.error(ResultCode.INTERNAL_ERROR, "批量导入失败");
    }
}
