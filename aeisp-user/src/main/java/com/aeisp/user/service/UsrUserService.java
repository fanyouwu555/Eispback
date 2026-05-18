package com.aeisp.user.service;

import com.aeisp.common.PageResult;
import com.aeisp.user.entity.UsrUser;
import com.aeisp.user.request.AdjustDurationRequest;
import com.aeisp.user.request.UserBatchCreateRequest;
import com.aeisp.user.request.UserCreateRequest;
import com.aeisp.user.request.UserQueryRequest;
import com.aeisp.user.request.UserUpdateRequest;
import com.aeisp.user.vo.UsrUserVO;

import java.util.List;

/**
 * 前端用户 Service 接口。
 *
 * @author AEISP Team
 */
public interface UsrUserService {

    /**
     * 自主注册。
     *
     * @param user 用户实体（含明文密码）
     * @return true 表示注册成功
     */
    boolean register(UsrUser user);

    /**
     * 后台手动创建用户。
     *
     * @param request 创建请求
     * @return true 表示创建成功
     */
    boolean createByAdmin(UserCreateRequest request);

    /**
     * 批量导入用户。
     *
     * @param request 批量创建请求
     * @return true 表示导入成功
     */
    boolean batchCreate(UserBatchCreateRequest request);

    /**
     * 更新用户信息（不修改密码）。
     *
     * @param request 更新请求
     * @return true 表示更新成功
     */
    boolean updateUser(UserUpdateRequest request);

    /**
     * 修改用户状态（正常/禁用/冻结）。
     *
     * @param userId 用户 ID
     * @param status 目标状态
     * @return true 表示修改成功
     */
    boolean updateStatus(Long userId, Integer status);

    /**
     * 重置用户密码。
     *
     * @param userId      用户 ID
     * @param newPassword 新密码（明文）
     * @return true 表示重置成功
     */
    boolean resetPassword(Long userId, String newPassword);

    /**
     * 手动增减用户剩余时长。
     *
     * @param request 调整请求
     * @return true 表示调整成功
     */
    boolean adjustDuration(AdjustDurationRequest request);

    /**
     * 多条件分页查询用户列表。
     *
     * @param request 查询条件
     * @return 分页结果
     */
    PageResult<UsrUserVO> listUsers(UserQueryRequest request);

    /**
     * 获取用户详情。
     *
     * @param userId 用户 ID
     * @return 用户详情 VO
     */
    UsrUserVO getUserDetail(Long userId);
}
