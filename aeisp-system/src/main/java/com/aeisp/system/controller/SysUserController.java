package com.aeisp.system.controller;

import com.aeisp.common.PageResult;
import com.aeisp.common.Result;
import com.aeisp.common.constant.ResultCode;
import com.aeisp.system.annotation.OperationLog;
import com.aeisp.system.dto.CreateUserRequest;
import com.aeisp.system.dto.UpdateUserRequest;
import com.aeisp.system.dto.UserQueryRequest;
import com.aeisp.system.entity.SysUser;
import com.aeisp.system.service.SysUserService;
import com.aeisp.system.vo.SysUserVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 后台管理员账号管理控制器。
 *
 * <p>提供用户的增删改查接口，路径前缀 {@code /api/v1/system/users}。</p>
 *
 * <p><b>注意：</b>当前版本暂未添加 {@code @PreAuthorize} 注解，权限控制将在后续 Security 集成任务中完成。</p>
 *
 * @author AEISP Team
 */
@RestController
@RequestMapping("/api/v1/system/users")
@RequiredArgsConstructor
public class SysUserController {

    private final SysUserService sysUserService;

    /**
     * 分页查询用户列表。
     *
     * @param request 查询条件
     * @return 分页结果
     */
    @PreAuthorize("hasAuthority('system:user:manage')")
    @GetMapping
    public Result<PageResult<SysUserVO>> listUsers(UserQueryRequest request) {
        PageResult<SysUserVO> result = sysUserService.listUsers(request);
        return Result.success(result);
    }

    /**
     * 根据 ID 查询用户详情。
     *
     * @param id 用户 ID
     * @return 用户信息
     */
    @PreAuthorize("hasAuthority('system:user:manage')")
    @GetMapping("/{id}")
    public Result<SysUserVO> getUserById(@PathVariable Long id) {
        SysUser user = sysUserService.getById(id);
        if (user == null) {
            return Result.error(ResultCode.NOT_FOUND, "用户不存在");
        }
        SysUserVO vo = new SysUserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setRealName(user.getRealName());
        vo.setEmail(user.getEmail());
        vo.setPhone(user.getPhone());
        vo.setStatus(user.getStatus());
        vo.setCreatedAt(user.getCreatedAt());
        vo.setUpdatedAt(user.getUpdatedAt());
        return Result.success(vo);
    }

    /**
     * 创建用户。
     *
     * @param request 创建参数
     * @return 操作结果
     */
    @PreAuthorize("hasAuthority('system:user:manage')")
    @OperationLog(module = "用户管理", operation = "新增")
    @PostMapping
    public Result<Void> createUser(@Valid @RequestBody CreateUserRequest request) {
        SysUser user = new SysUser();
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword());
        user.setRealName(request.getRealName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        boolean success = sysUserService.createUser(user, request.getRoleIds());
        return success ? Result.success() : Result.error(ResultCode.INTERNAL_ERROR, "创建用户失败");
    }

    /**
     * 更新用户。
     *
     * @param request 更新参数
     * @return 操作结果
     */
    @PreAuthorize("hasAuthority('system:user:manage')")
    @OperationLog(module = "用户管理", operation = "修改", sensitivity = 2)
    @PutMapping("/{id}")
    public Result<Void> updateUser(@PathVariable Long id, @Valid @RequestBody UpdateUserRequest request) {
        request.setId(id);
        SysUser user = new SysUser();
        user.setId(request.getId());
        user.setPassword(request.getPassword());
        user.setRealName(request.getRealName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setStatus(request.getStatus());
        boolean success = sysUserService.updateUser(user, request.getRoleIds());
        return success ? Result.success() : Result.error(ResultCode.INTERNAL_ERROR, "更新用户失败");
    }

    /**
     * 删除用户。
     *
     * @param id 用户 ID
     * @return 操作结果
     */
    @PreAuthorize("hasAuthority('system:user:manage')")
    @OperationLog(module = "用户管理", operation = "删除")
    @DeleteMapping("/{id}")
    public Result<Void> deleteUser(@PathVariable Long id) {
        boolean success = sysUserService.deleteUser(id);
        return success ? Result.success() : Result.error(ResultCode.INTERNAL_ERROR, "删除用户失败");
    }
}
