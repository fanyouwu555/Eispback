package com.aeisp.system.service;

import com.aeisp.common.PageResult;
import com.aeisp.system.dto.UserQueryRequest;
import com.aeisp.system.entity.SysUser;
import com.aeisp.system.vo.SysUserVO;

import java.util.List;

/**
 * 后台管理员账号 Service 接口。
 *
 * @author AEISP Team
 */
public interface SysUserService {

    /**
     * 根据用户名查询用户。
     *
     * @param username 登录用户名
     * @return 用户实体
     */
    SysUser getByUsername(String username);

    /**
     * 根据 ID 查询用户。
     *
     * @param userId 用户 ID
     * @return 用户实体
     */
    SysUser getById(Long userId);

    /**
     * 创建用户并分配角色。
     *
     * @param user    用户实体
     * @param roleIds 角色 ID 列表
     * @return {@code true} 创建成功
     */
    boolean createUser(SysUser user, List<Long> roleIds);

    /**
     * 更新用户及角色分配。
     *
     * @param user    用户实体
     * @param roleIds 角色 ID 列表
     * @return {@code true} 更新成功
     */
    boolean updateUser(SysUser user, List<Long> roleIds);

    /**
     * 删除用户（逻辑删除）。
     *
     * @param userId 用户 ID
     * @return {@code true} 删除成功
     */
    boolean deleteUser(Long userId);

    /**
     * 分页查询用户列表。
     *
     * @param request 查询条件
     * @return 分页结果
     */
    PageResult<SysUserVO> listUsers(UserQueryRequest request);
}
