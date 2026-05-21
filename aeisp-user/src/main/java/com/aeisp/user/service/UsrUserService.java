package com.aeisp.user.service;

import com.aeisp.common.PageResult;
import com.aeisp.user.entity.UsrUser;
import com.aeisp.user.request.AdjustDurationRequest;
import com.aeisp.user.request.UserBatchCreateRequest;
import com.aeisp.user.request.UserCreateRequest;
import com.aeisp.user.request.UserQueryRequest;
import com.aeisp.user.request.UserUpdateRequest;
import com.aeisp.user.vo.UserImportResultVO;
import com.aeisp.user.vo.UsrUserVO;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

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
     * @return 创建成功返回明文密码，失败返回null
     */
    String createByAdmin(UserCreateRequest request);

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
     * 修改用户状态（正常/禁用/冻结/锁定）。
     *
     * @param userId 用户 ID
     * @param status 目标状态
     * @param reason 状态变更原因
     * @return true 表示修改成功
     */
    boolean updateStatus(Long userId, Integer status, String reason);

    /**
     * 重置用户密码。
     *
     * @param userId      用户 ID
     * @param newPassword 新密码（明文）
     * @return true 表示重置成功
     */
    boolean resetPassword(Long userId, String newPassword);

    /**
     * 生成 10 位随机强密码（大写、小写、数字、特殊字符各至少一位）。
     *
     * @return 强密码明文
     */
    String generateStrongPassword();

    /**
     * 处理登录失败事件（递增失败次数，达到阈值后自动锁定）。
     *
     * @param userId 用户 ID
     */
    void handleLoginFailure(Long userId);

    /**
     * 检查账号锁定状态，锁定期满自动恢复。
     *
     * @param userId 用户 ID
     * @return 若账号被锁定/禁用/冻结返回提示信息，正常返回 null
     */
    String checkAccountLock(Long userId);

    /**
     * 清零登录失败次数（登录成功后调用）。
     *
     * @param userId 用户 ID
     */
    void resetFailedAttempts(Long userId);

    /**
     * 记录登录成功，更新登录次数和异地登录标记。
     *
     * @param userId 用户 ID
     * @param loginIp 当前登录 IP
     */
    void recordLoginSuccess(Long userId, String loginIp);

    /**
     * 根据用户名查找用户。
     *
     * @param username 用户名
     * @return 用户实体，不存在返回 null
     */
    UsrUser findByUsername(String username);

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

    /**
     * 从 Excel 批量导入用户。
     *
     * @param file Excel 文件
     * @return 导入结果
     */
    UserImportResultVO importFromExcel(MultipartFile file);

    /**
     * 导出用户列表到 Excel。
     *
     * @param request  查询条件
     * @param response HTTP 响应
     */
    void exportToExcel(UserQueryRequest request, HttpServletResponse response);
}
