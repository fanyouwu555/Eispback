package com.aeisp.user.service.impl;

import com.aeisp.common.PageResult;
import com.aeisp.common.constant.CommonConstants;
import com.aeisp.common.exception.BizException;
import com.aeisp.system.entity.SysRole;
import com.aeisp.system.mapper.SysRoleMapper;
import com.aeisp.user.entity.*;
import com.aeisp.user.mapper.*;
import com.aeisp.user.request.AdjustDurationRequest;
import com.aeisp.user.request.UserBatchCreateRequest;
import com.aeisp.user.request.UserCreateRequest;
import com.aeisp.user.request.UserQueryRequest;
import com.aeisp.user.request.UserUpdateRequest;
import com.aeisp.user.service.UsrUserService;
import com.aeisp.user.vo.UsrUserVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 前端用户 Service 实现类。
 *
 * @author AEISP Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UsrUserServiceImpl implements UsrUserService {

    private final UsrUserMapper usrUserMapper;
    private final UsrUserRoleMapper usrUserRoleMapper;
    private final UsrUserDurationMapper usrUserDurationMapper;
    private final UsrUserBalanceMapper usrUserBalanceMapper;
    private final UsrDurationChangeLogMapper usrDurationChangeLogMapper;
    private final SysRoleMapper sysRoleMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean register(UsrUser user) {
        // 校验唯一性
        if (usrUserMapper.selectByUsername(user.getUsername()) != null) {
            throw new BizException("用户名已存在");
        }
        if (StringUtils.hasText(user.getPhone()) && usrUserMapper.selectByPhone(user.getPhone()) != null) {
            throw new BizException("手机号已存在");
        }
        if (StringUtils.hasText(user.getEmail()) && usrUserMapper.selectByEmail(user.getEmail()) != null) {
            throw new BizException("邮箱已存在");
        }

        // 加密密码并设置默认值
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setStatus(CommonConstants.USER_STATUS_NORMAL);
        user.setNeedChangePassword(0);
        user.setFailedLoginAttempts(0);
        user.setRegisterTime(LocalDateTime.now());
        user.setDeleted(CommonConstants.DELETED_NO);

        int rows = usrUserMapper.insert(user);
        if (rows <= 0) {
            return false;
        }

        // 同步创建时长记录
        createDefaultDuration(user.getId());
        // 同步创建余额记录
        createDefaultBalance(user.getId());

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createByAdmin(UserCreateRequest request) {
        // 校验唯一性
        if (usrUserMapper.selectByUsername(request.getUsername()) != null) {
            throw new BizException("用户名已存在");
        }
        if (StringUtils.hasText(request.getPhone()) && usrUserMapper.selectByPhone(request.getPhone()) != null) {
            throw new BizException("手机号已存在");
        }
        if (StringUtils.hasText(request.getEmail()) && usrUserMapper.selectByEmail(request.getEmail()) != null) {
            throw new BizException("邮箱已存在");
        }

        UsrUser user = new UsrUser();
        user.setUsername(request.getUsername());
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setNickname(request.getNickname());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setStatus(request.getStatus() != null ? request.getStatus() : CommonConstants.USER_STATUS_NORMAL);
        user.setNeedChangePassword(0);
        user.setFailedLoginAttempts(0);
        user.setRegisterTime(LocalDateTime.now());
        user.setDeleted(CommonConstants.DELETED_NO);

        int rows = usrUserMapper.insert(user);
        if (rows <= 0) {
            return false;
        }

        Long userId = user.getId();

        // 同步创建时长记录
        UsrUserDuration duration = new UsrUserDuration();
        duration.setUserId(userId);
        duration.setRemainingMinutes(request.getRemainingMinutes() != null ? request.getRemainingMinutes() : 0);
        duration.setTotalGrantedMinutes(request.getRemainingMinutes() != null ? request.getRemainingMinutes() : 0);
        duration.setTotalConsumedMinutes(0);
        duration.setWarningThreshold(120);
        usrUserDurationMapper.insert(duration);

        // 同步创建余额记录
        createDefaultBalance(userId);

        // 同步创建角色关联
        if (!CollectionUtils.isEmpty(request.getRoleIds())) {
            bindUserRoles(userId, request.getRoleIds(), null);
        }

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchCreate(UserBatchCreateRequest request) {
        if (request == null || request.getUsers() == null || request.getUsers().isEmpty()) {
            return false;
        }
        LocalDateTime now = LocalDateTime.now();
        List<UsrUser> users = request.getUsers().stream().map(req -> {
            UsrUser user = new UsrUser();
            user.setUsername(req.getUsername());
            user.setPhone(req.getPhone());
            user.setEmail(req.getEmail());
            user.setNickname(req.getNickname());
            user.setPassword(passwordEncoder.encode(req.getPassword()));
            user.setStatus(req.getStatus() != null ? req.getStatus() : CommonConstants.USER_STATUS_NORMAL);
            user.setNeedChangePassword(0);
            user.setFailedLoginAttempts(0);
            user.setRegisterTime(now);
            user.setDeleted(CommonConstants.DELETED_NO);
            return user;
        }).toList();

        int rows = usrUserMapper.batchInsert(users);
        if (rows <= 0) {
            return false;
        }

        // 为每个用户创建时长和余额记录
        for (UsrUser user : users) {
            createDefaultDuration(user.getId());
            createDefaultBalance(user.getId());
        }

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateUser(UserUpdateRequest request) {
        UsrUser existing = usrUserMapper.selectById(request.getId());
        if (existing == null) {
            throw new BizException("用户不存在");
        }

        // 校验手机号唯一性
        if (StringUtils.hasText(request.getPhone()) && !Objects.equals(existing.getPhone(), request.getPhone())) {
            if (usrUserMapper.selectByPhone(request.getPhone()) != null) {
                throw new BizException("手机号已存在");
            }
        }
        // 校验邮箱唯一性
        if (StringUtils.hasText(request.getEmail()) && !Objects.equals(existing.getEmail(), request.getEmail())) {
            if (usrUserMapper.selectByEmail(request.getEmail()) != null) {
                throw new BizException("邮箱已存在");
            }
        }

        UsrUser user = new UsrUser();
        user.setId(request.getId());
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setNickname(request.getNickname());
        user.setAvatarUrl(request.getAvatarUrl());
        // 不修改密码
        int rows = usrUserMapper.updateById(user);

        // 更新角色关联
        if (request.getRoleIds() != null) {
            // 先删除旧关联
            LambdaQueryWrapper<UsrUserRole> wrapper = Wrappers.lambdaQuery();
            wrapper.eq(UsrUserRole::getUserId, request.getId());
            usrUserRoleMapper.delete(wrapper);
            // 再插入新关联
            if (!request.getRoleIds().isEmpty()) {
                bindUserRoles(request.getId(), request.getRoleIds(), null);
            }
        }

        return rows > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateStatus(Long userId, Integer status) {
        UsrUser user = new UsrUser();
        user.setId(userId);
        user.setStatus(status);
        // 如果从锁定状态恢复，清空锁定相关字段
        if (status == CommonConstants.USER_STATUS_NORMAL) {
            user.setLockedUntil(null);
            user.setFailedLoginAttempts(0);
        }
        return usrUserMapper.updateById(user) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean resetPassword(Long userId, String newPassword) {
        UsrUser user = new UsrUser();
        user.setId(userId);
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setNeedChangePassword(1);
        return usrUserMapper.updateById(user) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean adjustDuration(AdjustDurationRequest request) {
        UsrUserDuration duration = usrUserDurationMapper.selectByUserId(request.getUserId());
        if (duration == null) {
            throw new BizException("用户时长记录不存在");
        }

        int previousRemaining = duration.getRemainingMinutes();
        int newRemaining = previousRemaining + request.getDeltaMinutes();

        if (newRemaining < 0) {
            throw new BizException("剩余时长不足，无法扣除，缺口：" + Math.abs(newRemaining) + " 分钟");
        }

        // 更新时长记录
        UsrUserDuration update = new UsrUserDuration();
        update.setId(duration.getId());
        update.setRemainingMinutes(newRemaining);
        if (request.getDeltaMinutes() > 0) {
            update.setTotalGrantedMinutes(duration.getTotalGrantedMinutes() + request.getDeltaMinutes());
        } else {
            update.setTotalConsumedMinutes(duration.getTotalConsumedMinutes() + Math.abs(request.getDeltaMinutes()));
        }
        update.setUpdatedAt(LocalDateTime.now());
        usrUserDurationMapper.updateById(update);

        // 记录变更日志
        UsrDurationChangeLog log = new UsrDurationChangeLog();
        log.setUserId(request.getUserId());
        log.setOperationType(request.getDeltaMinutes() >= 0 ? "ADMIN_ADD" : "ADMIN_SUBTRACT");
        log.setChangeMinutes(request.getDeltaMinutes());
        log.setPreviousRemaining(previousRemaining);
        log.setCurrentRemaining(newRemaining);
        log.setReason(request.getReason());
        log.setOperatorType(2); // 管理员
        log.setCreatedAt(LocalDateTime.now());
        usrDurationChangeLogMapper.insert(log);

        return true;
    }

    @Override
    public PageResult<UsrUserVO> listUsers(UserQueryRequest request) {
        LambdaQueryWrapper<UsrUser> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(UsrUser::getDeleted, CommonConstants.DELETED_NO);

        if (StringUtils.hasText(request.getUsername())) {
            wrapper.like(UsrUser::getUsername, request.getUsername());
        }
        if (StringUtils.hasText(request.getPhone())) {
            wrapper.like(UsrUser::getPhone, request.getPhone());
        }
        if (request.getStatus() != null) {
            wrapper.eq(UsrUser::getStatus, request.getStatus());
        }
        if (request.getRegisterTimeStart() != null) {
            wrapper.ge(UsrUser::getRegisterTime, request.getRegisterTimeStart());
        }
        if (request.getRegisterTimeEnd() != null) {
            wrapper.le(UsrUser::getRegisterTime, request.getRegisterTimeEnd());
        }
        wrapper.orderByDesc(UsrUser::getCreatedAt);

        Page<UsrUser> page = new Page<>(request.getPageNum(), request.getPageSize());
        Page<UsrUser> resultPage = usrUserMapper.selectPage(page, wrapper);

        if (resultPage.getRecords().isEmpty()) {
            return PageResult.of(resultPage, List.of());
        }

        // 批量查询关联数据
        List<Long> userIds = resultPage.getRecords().stream().map(UsrUser::getId).toList();
        Map<Long, UsrUserDuration> durationMap = batchQueryDuration(userIds);
        Map<Long, UsrUserBalance> balanceMap = batchQueryBalance(userIds);
        Map<Long, List<String>> roleCodesMap = batchQueryRoleCodes(userIds);

        List<UsrUserVO> voList = resultPage.getRecords().stream()
                .map(u -> convertToVO(u, durationMap.get(u.getId()), balanceMap.get(u.getId()), roleCodesMap.get(u.getId())))
                .toList();
        return PageResult.of(resultPage, voList);
    }

    @Override
    public UsrUserVO getUserDetail(Long userId) {
        UsrUser user = usrUserMapper.selectById(userId);
        if (user == null || Objects.equals(user.getDeleted(), CommonConstants.DELETED_YES)) {
            return null;
        }
        UsrUserDuration duration = usrUserDurationMapper.selectByUserId(userId);
        UsrUserBalance balance = usrUserBalanceMapper.selectByUserId(userId);
        List<String> roleCodes = queryRoleCodesByUserId(userId);
        return convertToVO(user, duration, balance, roleCodes);
    }

    /**
     * 创建默认时长记录。
     */
    private void createDefaultDuration(Long userId) {
        UsrUserDuration duration = new UsrUserDuration();
        duration.setUserId(userId);
        duration.setTotalGrantedMinutes(0);
        duration.setTotalConsumedMinutes(0);
        duration.setRemainingMinutes(0);
        duration.setWarningThreshold(120);
        usrUserDurationMapper.insert(duration);
    }

    /**
     * 创建默认余额记录。
     */
    private void createDefaultBalance(Long userId) {
        UsrUserBalance balance = new UsrUserBalance();
        balance.setUserId(userId);
        balance.setBalanceCents(0);
        balance.setTotalRechargeCents(0);
        balance.setTotalConsumedCents(0);
        usrUserBalanceMapper.insert(balance);
    }

    /**
     * 绑定用户角色。
     */
    private void bindUserRoles(Long userId, List<Long> roleIds, Long grantedBy) {
        LocalDateTime now = LocalDateTime.now();
        List<UsrUserRole> list = roleIds.stream().distinct().map(roleId -> {
            UsrUserRole ur = new UsrUserRole();
            ur.setUserId(userId);
            ur.setRoleId(roleId);
            ur.setGrantedBy(grantedBy);
            ur.setGrantedAt(now);
            ur.setCreatedAt(now);
            return ur;
        }).toList();
        usrUserRoleMapper.batchInsert(list);
    }

    /**
     * 批量查询用户时长。
     */
    private Map<Long, UsrUserDuration> batchQueryDuration(List<Long> userIds) {
        LambdaQueryWrapper<UsrUserDuration> wrapper = Wrappers.lambdaQuery();
        wrapper.in(UsrUserDuration::getUserId, userIds);
        return usrUserDurationMapper.selectList(wrapper).stream()
                .collect(Collectors.toMap(UsrUserDuration::getUserId, d -> d));
    }

    /**
     * 批量查询用户余额。
     */
    private Map<Long, UsrUserBalance> batchQueryBalance(List<Long> userIds) {
        LambdaQueryWrapper<UsrUserBalance> wrapper = Wrappers.lambdaQuery();
        wrapper.in(UsrUserBalance::getUserId, userIds);
        return usrUserBalanceMapper.selectList(wrapper).stream()
                .collect(Collectors.toMap(UsrUserBalance::getUserId, b -> b));
    }

    /**
     * 批量查询用户角色编码。
     */
    private Map<Long, List<String>> batchQueryRoleCodes(List<Long> userIds) {
        LambdaQueryWrapper<UsrUserRole> wrapper = Wrappers.lambdaQuery();
        wrapper.in(UsrUserRole::getUserId, userIds);
        List<UsrUserRole> userRoles = usrUserRoleMapper.selectList(wrapper);

        Set<Long> roleIds = userRoles.stream().map(UsrUserRole::getRoleId).collect(Collectors.toSet());
        Map<Long, String> roleCodeMap = Map.of();
        if (!roleIds.isEmpty()) {
            List<SysRole> roles = sysRoleMapper.selectBatchIds(roleIds);
            roleCodeMap = roles.stream().collect(Collectors.toMap(SysRole::getId, SysRole::getRoleCode));
        }

        Map<Long, String> finalRoleCodeMap = roleCodeMap;
        return userRoles.stream()
                .collect(Collectors.groupingBy(
                        UsrUserRole::getUserId,
                        Collectors.mapping(ur -> finalRoleCodeMap.getOrDefault(ur.getRoleId(), ""), Collectors.toList())
                ));
    }

    /**
     * 查询单个用户的角色编码列表。
     */
    private List<String> queryRoleCodesByUserId(Long userId) {
        List<Long> roleIds = usrUserRoleMapper.selectRoleIdsByUserId(userId);
        if (CollectionUtils.isEmpty(roleIds)) {
            return List.of();
        }
        List<SysRole> roles = sysRoleMapper.selectBatchIds(roleIds);
        return roles.stream().map(SysRole::getRoleCode).toList();
    }

    /**
     * 将实体转换为 VO。
     */
    private UsrUserVO convertToVO(UsrUser user, UsrUserDuration duration, UsrUserBalance balance, List<String> roleCodes) {
        UsrUserVO vo = new UsrUserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setPhone(user.getPhone());
        vo.setEmail(user.getEmail());
        vo.setNickname(user.getNickname());
        vo.setAvatarUrl(user.getAvatarUrl());
        vo.setStatus(user.getStatus());
        vo.setStatusReason(user.getStatusReason());
        vo.setLockedUntil(user.getLockedUntil());
        vo.setNeedChangePassword(user.getNeedChangePassword());
        vo.setFailedLoginAttempts(user.getFailedLoginAttempts());
        vo.setLastLoginTime(user.getLastLoginTime());
        vo.setLastLoginIp(user.getLastLoginIp());
        vo.setRegisterIp(user.getRegisterIp());
        vo.setRegisterDeviceInfo(user.getRegisterDeviceInfo());
        vo.setRegisterTime(user.getRegisterTime());
        vo.setInvitationCodeUsed(user.getInvitationCodeUsed());
        vo.setRoleCodes(roleCodes);
        vo.setRemainingMinutes(duration != null ? duration.getRemainingMinutes() : 0);
        vo.setBalanceCents(balance != null ? balance.getBalanceCents() : 0);
        vo.setCreatedAt(user.getCreatedAt());
        vo.setUpdatedAt(user.getUpdatedAt());
        return vo;
    }
}
