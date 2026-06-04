package com.aeisp.user.service.impl;

import com.aeisp.common.PageResult;
import com.aeisp.common.code.CommonErrorCode;
import com.aeisp.common.constant.CommonConstants;
import com.aeisp.common.exception.BizException;
import com.aeisp.user.code.UserErrorCode;
import com.aeisp.system.entity.SysRole;
import com.aeisp.system.mapper.SysRoleMapper;
import com.aeisp.system.service.SysConfigService;
import com.aeisp.system.service.SysFeatureSwitchService;
import com.aeisp.user.entity.*;
import com.aeisp.user.mapper.*;
import com.aeisp.user.request.*;
import com.aeisp.user.service.UsrUserService;
import com.aeisp.user.vo.UserExportVO;
import com.aeisp.user.vo.UserImportResultVO;
import com.aeisp.user.vo.UsrUserVO;
import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
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

    private static final Long DEFAULT_USER_ROLE_ID = 7L;
    private static final List<Long> DEFAULT_USER_ROLE_IDS = List.of(DEFAULT_USER_ROLE_ID);

    private final UsrUserMapper usrUserMapper;
    private final UsrUserRoleMapper usrUserRoleMapper;
    private final UsrUserDurationMapper usrUserDurationMapper;
    private final UsrUserBalanceMapper usrUserBalanceMapper;
    private final UsrDurationChangeLogMapper usrDurationChangeLogMapper;
    private final SysRoleMapper sysRoleMapper;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;
    private final SysFeatureSwitchService featureSwitchService;
    private final SysConfigService sysConfigService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean register(UsrUser user) {
        // 校验唯一性
        if (usrUserMapper.selectByUsername(user.getUsername()) != null) {
            throw new BizException(UserErrorCode.USERNAME_EXISTS);
        }
        if (StringUtils.hasText(user.getPhone()) && usrUserMapper.selectByPhone(user.getPhone()) != null) {
            throw new BizException(UserErrorCode.PHONE_EXISTS);
        }
        if (StringUtils.hasText(user.getEmail()) && usrUserMapper.selectByEmail(user.getEmail()) != null) {
            throw new BizException(UserErrorCode.EMAIL_EXISTS);
        }

        // 密码复杂度校验
        validatePassword(user.getPassword());

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
        // 分配默认角色（普通用户）
        bindUserRoles(user.getId(), List.of(DEFAULT_USER_ROLE_ID), null);

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createByAdmin(UserCreateRequest request) {
        // 校验唯一性
        if (usrUserMapper.selectByUsername(request.getUsername()) != null) {
            throw new BizException(UserErrorCode.USERNAME_EXISTS);
        }
        if (StringUtils.hasText(request.getPhone()) && usrUserMapper.selectByPhone(request.getPhone()) != null) {
            throw new BizException(UserErrorCode.PHONE_EXISTS);
        }
        if (StringUtils.hasText(request.getEmail()) && usrUserMapper.selectByEmail(request.getEmail()) != null) {
            throw new BizException(UserErrorCode.EMAIL_EXISTS);
        }

        String rawPassword = request.getPassword();
        if (!StringUtils.hasText(rawPassword)) {
            rawPassword = "123456";
        }
        validatePassword(rawPassword);
        UsrUser user = new UsrUser();
        user.setUsername(request.getUsername());
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setNickname(request.getNickname());
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setStatus(request.getStatus() != null ? request.getStatus() : CommonConstants.USER_STATUS_NORMAL);
        user.setNeedChangePassword(1);
        user.setFailedLoginAttempts(0);
        user.setRegisterIp("system"); // 管理员创建时设置为 system
        user.setRegisterTime(LocalDateTime.now());
        user.setIsCompetition(request.getIsCompetition() != null ? request.getIsCompetition() : 0);
        user.setDeleted(CommonConstants.DELETED_NO);

        int rows = usrUserMapper.insert(user);
        if (rows <= 0) {
            return null;
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
        List<Long> roleIds = CollectionUtils.isEmpty(request.getRoleIds()) ? DEFAULT_USER_ROLE_IDS : request.getRoleIds();
        bindUserRoles(userId, roleIds, null);

        return rawPassword;
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
            user.setLoginCount(0);
            user.setAbnormalLogin(0);
            user.setRegisterTime(now);
            user.setIsCompetition(req.getIsCompetition() != null ? req.getIsCompetition() : 0);
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
            throw new BizException(UserErrorCode.USER_NOT_FOUND);
        }

        // 校验手机号唯一性
        if (StringUtils.hasText(request.getPhone()) && !Objects.equals(existing.getPhone(), request.getPhone())) {
            if (usrUserMapper.selectByPhone(request.getPhone()) != null) {
                throw new BizException(UserErrorCode.PHONE_EXISTS);
            }
        }
        // 校验邮箱唯一性
        if (StringUtils.hasText(request.getEmail()) && !Objects.equals(existing.getEmail(), request.getEmail())) {
            if (usrUserMapper.selectByEmail(request.getEmail()) != null) {
                throw new BizException(UserErrorCode.EMAIL_EXISTS);
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
    public boolean updateStatus(Long userId, Integer status, String reason) {
        UsrUser existing = usrUserMapper.selectById(userId);
        if (existing == null) {
            throw new BizException(UserErrorCode.USER_NOT_FOUND);
        }
        int currentStatus = existing.getStatus() != null ? existing.getStatus() : CommonConstants.USER_STATUS_NORMAL;

        // 状态转换合法性校验
        boolean validTransition = switch (currentStatus) {
            case 1 -> status == 2 || status == 3 || status == 4; // 正常 -> 禁用/冻结/锁定
            case 2 -> status == 1; // 禁用 -> 正常
            case 3 -> status == 1; // 冻结 -> 正常
            case 4 -> status == 1; // 锁定 -> 正常（管理员提前解锁）
            default -> false;
        };
        if (!validTransition) {
            throw new BizException(CommonErrorCode.PARAM_VALIDATION_FAILED, "非法的状态转换");
        }

        UsrUser user = new UsrUser();
        user.setId(userId);
        user.setStatus(status);
        user.setStatusReason(reason);
        // 如果从锁定状态恢复，清空锁定相关字段
        if (status == CommonConstants.USER_STATUS_NORMAL) {
            user.setLockedUntil(null);
            user.setFailedLoginAttempts(0);
        }
        boolean success = usrUserMapper.updateById(user) > 0;
        if (success) {
            eventPublisher.publishEvent(new com.aeisp.common.event.UserStatusChangeEvent(this, userId, status, reason));
        }
        return success;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean resetPassword(Long userId, String newPassword) {
        UsrUser user = new UsrUser();
        user.setId(userId);
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setNeedChangePassword(1);
        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        return usrUserMapper.updateById(user) > 0;
    }

    @Override
    public String generateStrongPassword() {
        String upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lower = "abcdefghijklmnopqrstuvwxyz";
        String digits = "0123456789";
        String special = "!@#$%^&*";
        java.security.SecureRandom random = new java.security.SecureRandom();
        StringBuilder sb = new StringBuilder(10);
        sb.append(upper.charAt(random.nextInt(upper.length())));
        sb.append(lower.charAt(random.nextInt(lower.length())));
        sb.append(digits.charAt(random.nextInt(digits.length())));
        sb.append(special.charAt(random.nextInt(special.length())));
        String all = upper + lower + digits + special;
        for (int i = 4; i < 10; i++) {
            sb.append(all.charAt(random.nextInt(all.length())));
        }
        char[] arr = sb.toString().toCharArray();
        for (int i = arr.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char tmp = arr[i];
            arr[i] = arr[j];
            arr[j] = tmp;
        }
        return new String(arr);
    }

    @Override
    public void handleLoginFailure(Long userId) {
        UsrUser user = usrUserMapper.selectById(userId);
        if (user == null) {
            return;
        }
        int currentStatus = user.getStatus() != null ? user.getStatus() : CommonConstants.USER_STATUS_NORMAL;
        // 只有正常状态的用户才累计失败次数
        if (currentStatus != CommonConstants.USER_STATUS_NORMAL) {
            return;
        }
        int attempts = user.getFailedLoginAttempts() != null ? user.getFailedLoginAttempts() + 1 : 1;
        UsrUser update = new UsrUser();
        update.setId(userId);
        update.setFailedLoginAttempts(attempts);

        // 登录失败锁定：检查功能开关
        if (featureSwitchService.isEnabled("security.lock")) {
            int maxAttempts = parseConfigInt("login_max_fail_attempts", 5);
            int lockDuration = parseConfigInt("threshold.login_lock_duration", 30);
            if (attempts >= maxAttempts) {
                update.setStatus(CommonConstants.USER_STATUS_LOCKED);
                update.setLockedUntil(LocalDateTime.now().plusMinutes(lockDuration));
            }
        }
        usrUserMapper.updateById(update);
    }

    @Override
    public String checkAccountLock(Long userId) {
        UsrUser user = usrUserMapper.selectById(userId);
        if (user == null) {
            return null;
        }
        int status = user.getStatus() != null ? user.getStatus() : CommonConstants.USER_STATUS_NORMAL;
        return switch (status) {
            case 2 -> "账号已禁用";
            case 3 -> "账号已冻结";
            case 4 -> {
                LocalDateTime lockedUntil = user.getLockedUntil();
                if (lockedUntil != null && lockedUntil.isBefore(LocalDateTime.now())) {
                    // 锁定期已满，自动恢复
                    UsrUser update = new UsrUser();
                    update.setId(userId);
                    update.setStatus(CommonConstants.USER_STATUS_NORMAL);
                    update.setLockedUntil(null);
                    update.setFailedLoginAttempts(0);
                    usrUserMapper.updateById(update);
                    yield null;
                }
                long minutesLeft = lockedUntil != null
                        ? java.time.Duration.between(LocalDateTime.now(), lockedUntil).toMinutes()
                        : 0;
                yield "账号已锁定，请 " + Math.max(0, minutesLeft) + " 分钟后重试";
            }
            default -> null;
        };
    }

    @Override
    public void resetFailedAttempts(Long userId) {
        UsrUser user = new UsrUser();
        user.setId(userId);
        user.setFailedLoginAttempts(0);
        usrUserMapper.updateById(user);
    }

    @Override
    public void recordLoginSuccess(Long userId, String loginIp) {
        UsrUser existing = usrUserMapper.selectById(userId);
        if (existing == null) {
            return;
        }
        UsrUser user = new UsrUser();
        user.setId(userId);
        user.setLoginCount((existing.getLoginCount() != null ? existing.getLoginCount() : 0) + 1);
        user.setLastLoginTime(LocalDateTime.now());
        user.setLastLoginIp(loginIp);
        // 异地登录检测：当前IP与上次登录IP不同（且不为空）
        String lastIp = existing.getLastLoginIp();
        if (StringUtils.hasText(lastIp) && !lastIp.equals(loginIp)) {
            user.setAbnormalLogin(1);
        }
        usrUserMapper.updateById(user);
    }

    @Override
    public UsrUser findByUsername(String username) {
        return usrUserMapper.selectByUsername(username);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean adjustDuration(AdjustDurationRequest request) {
        UsrUserDuration duration = usrUserDurationMapper.selectByUserId(request.getUserId());
        if (duration == null) {
            throw new BizException(CommonErrorCode.RESOURCE_NOT_FOUND, "用户时长记录不存在");
        }

        int previousRemaining = duration.getRemainingMinutes();
        int newRemaining;
        int changeMinutes;
        String operationType;

        Integer adjustType = request.getAdjustType();
        if (adjustType == null) {
            // 兼容旧逻辑：根据 deltaMinutes 符号推断类型
            adjustType = request.getDeltaMinutes() < 0 ? 2 : 1;
        }

        switch (adjustType) {
            case 2 -> { // 扣减
                if (request.getDeltaMinutes() < 0) {
                    // 兼容旧格式：deltaMinutes 为负数
                    newRemaining = previousRemaining + request.getDeltaMinutes();
                    changeMinutes = request.getDeltaMinutes();
                } else {
                    newRemaining = previousRemaining - request.getDeltaMinutes();
                    changeMinutes = -request.getDeltaMinutes();
                }
                if (newRemaining < 0) {
                    throw new BizException(CommonErrorCode.PARAM_VALIDATION_FAILED, "剩余时长不足，无法扣除，缺口：" + Math.abs(newRemaining) + " 分钟");
                }
                operationType = "ADMIN_SUBTRACT";
            }
            case 3 -> { // 设定
                newRemaining = request.getDeltaMinutes();
                if (newRemaining < 0) {
                    throw new BizException(CommonErrorCode.PARAM_VALIDATION_FAILED, "设定时长不能为负数");
                }
                changeMinutes = newRemaining - previousRemaining;
                operationType = "ADMIN_SET";
            }
            default -> { // 增加
                newRemaining = previousRemaining + request.getDeltaMinutes();
                changeMinutes = request.getDeltaMinutes();
                operationType = "ADMIN_ADD";
            }
        }

        // 更新时长记录
        UsrUserDuration update = new UsrUserDuration();
        update.setId(duration.getId());
        update.setRemainingMinutes(newRemaining);
        if (changeMinutes > 0) {
            int granted = duration.getTotalGrantedMinutes() != null ? duration.getTotalGrantedMinutes() : 0;
            update.setTotalGrantedMinutes(granted + changeMinutes);
        } else if (changeMinutes < 0) {
            int consumed = duration.getTotalConsumedMinutes() != null ? duration.getTotalConsumedMinutes() : 0;
            update.setTotalConsumedMinutes(consumed + Math.abs(changeMinutes));
        }
        update.setUpdatedAt(LocalDateTime.now());
        usrUserDurationMapper.updateById(update);

        // 记录变更日志
        UsrDurationChangeLog log = new UsrDurationChangeLog();
        log.setUserId(request.getUserId());
        log.setOperationType(operationType);
        log.setChangeMinutes(changeMinutes);
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
        if (request.getIsCompetition() != null) {
            wrapper.eq(UsrUser::getIsCompetition, request.getIsCompetition());
        }

        // 角色筛选
        if (!CollectionUtils.isEmpty(request.getRoleIds())) {
            LambdaQueryWrapper<UsrUserRole> roleWrapper = Wrappers.lambdaQuery();
            roleWrapper.in(UsrUserRole::getRoleId, request.getRoleIds());
            List<UsrUserRole> userRoles = usrUserRoleMapper.selectList(roleWrapper);
            List<Long> roleUserIds = userRoles.stream().map(UsrUserRole::getUserId).distinct().toList();
            if (roleUserIds.isEmpty()) {
                return PageResult.of(new Page<>(), List.of());
            }
            wrapper.in(UsrUser::getId, roleUserIds);
        }

        // 剩余时长范围筛选
        if (request.getRemainingMinutesMin() != null || request.getRemainingMinutesMax() != null) {
            LambdaQueryWrapper<UsrUserDuration> durationWrapper = Wrappers.lambdaQuery();
            if (request.getRemainingMinutesMin() != null) {
                durationWrapper.ge(UsrUserDuration::getRemainingMinutes, request.getRemainingMinutesMin());
            }
            if (request.getRemainingMinutesMax() != null) {
                durationWrapper.le(UsrUserDuration::getRemainingMinutes, request.getRemainingMinutesMax());
            }
            List<UsrUserDuration> durations = usrUserDurationMapper.selectList(durationWrapper);
            List<Long> durationUserIds = durations.stream().map(UsrUserDuration::getUserId).distinct().toList();
            if (durationUserIds.isEmpty()) {
                return PageResult.of(new Page<>(), List.of());
            }
            wrapper.in(UsrUser::getId, durationUserIds);
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserImportResultVO importFromExcel(MultipartFile file) {
        String filename = file.getOriginalFilename();
        if (filename == null || !(filename.endsWith(".xlsx") || filename.endsWith(".xls"))) {
            throw new BizException(CommonErrorCode.PARAM_VALIDATION_FAILED, "仅支持 .xlsx 或 .xls 格式的 Excel 文件");
        }
        List<UserImportRow> rows;
        try {
            rows = EasyExcel.read(file.getInputStream()).head(UserImportRow.class).sheet().doReadSync();
        } catch (IOException e) {
            throw new BizException(CommonErrorCode.SYSTEM_ERROR, "读取 Excel 文件失败");
        }

        UserImportResultVO result = new UserImportResultVO();
        result.setTotal(rows.size());
        if (rows.isEmpty()) {
            result.setSuccessCount(0);
            result.setFailCount(0);
            return result;
        }

        if (rows.size() > 1000) {
            throw new BizException(CommonErrorCode.PARAM_VALIDATION_FAILED, "单次导入最多 1000 条记录");
        }

        List<UserImportResultVO.FailItem> failList = new ArrayList<>();
        Set<String> usernameSet = new HashSet<>();

        for (int i = 0; i < rows.size(); i++) {
            UserImportRow row = rows.get(i);
            int rowNum = i + 2;
            String username = row.getUsername();

            if (!StringUtils.hasText(username)) {
                failList.add(createFailItem(rowNum, "", "用户名不能为空"));
                continue;
            }
            if (!username.matches("^[a-zA-Z][a-zA-Z0-9_]{3,19}$")) {
                failList.add(createFailItem(rowNum, username, "用户名格式不正确"));
                continue;
            }
            if (!StringUtils.hasText(row.getPassword())) {
                failList.add(createFailItem(rowNum, username, "初始密码不能为空"));
                continue;
            }
            if (!usernameSet.add(username)) {
                failList.add(createFailItem(rowNum, username, "Excel 内用户名重复"));
                continue;
            }
            if (usrUserMapper.selectByUsername(username) != null) {
                failList.add(createFailItem(rowNum, username, "用户名已存在"));
            }
        }

        if (!failList.isEmpty()) {
            result.setSuccessCount(0);
            result.setFailCount(failList.size());
            result.setFailList(failList);
            return result;
        }

        Set<String> allRoleNames = rows.stream()
                .map(UserImportRow::getRoleNames)
                .filter(StringUtils::hasText)
                .flatMap(rn -> Arrays.stream(rn.split(",")))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .collect(Collectors.toSet());
        Map<String, Long> roleNameToIdMap = Map.of();
        if (!allRoleNames.isEmpty()) {
            LambdaQueryWrapper<SysRole> roleWrapper = Wrappers.lambdaQuery();
            roleWrapper.in(SysRole::getRoleName, allRoleNames);
            List<SysRole> roles = sysRoleMapper.selectList(roleWrapper);
            roleNameToIdMap = roles.stream()
                    .collect(Collectors.toMap(SysRole::getRoleName, SysRole::getId, (a, b) -> a));
        }

        int successCount = 0;
        LocalDateTime now = LocalDateTime.now();
        for (UserImportRow row : rows) {
            UsrUser user = new UsrUser();
            user.setUsername(row.getUsername());
            user.setPassword(passwordEncoder.encode(row.getPassword()));
            user.setNickname(row.getRemark());
            user.setStatus(CommonConstants.USER_STATUS_NORMAL);
            user.setNeedChangePassword(0);
            user.setFailedLoginAttempts(0);
            user.setLoginCount(0);
            user.setAbnormalLogin(0);
            user.setRegisterTime(now);
            user.setIsCompetition(row.getIsCompetition() != null ? row.getIsCompetition() : 0);
            user.setDeleted(CommonConstants.DELETED_NO);
            usrUserMapper.insert(user);

            Long userId = user.getId();
            createDefaultDuration(userId);
            createDefaultBalance(userId);

            if (StringUtils.hasText(row.getRoleNames())) {
                List<Long> roleIds = Arrays.stream(row.getRoleNames().split(","))
                        .map(String::trim)
                        .filter(StringUtils::hasText)
                        .map(roleNameToIdMap::get)
                        .filter(Objects::nonNull)
                        .distinct()
                        .toList();
                if (!roleIds.isEmpty()) {
                    bindUserRoles(userId, roleIds, null);
                }
            }
            successCount++;
        }

        result.setSuccessCount(successCount);
        result.setFailCount(0);
        return result;
    }

    private UserImportResultVO.FailItem createFailItem(int rowNum, String username, String reason) {
        UserImportResultVO.FailItem item = new UserImportResultVO.FailItem();
        item.setRowNum(rowNum);
        item.setUsername(username);
        item.setReason(reason);
        return item;
    }

    @Override
    public void exportToExcel(UserQueryRequest request, HttpServletResponse response) {
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

        boolean emptyResult = false;

        // 角色筛选
        if (!CollectionUtils.isEmpty(request.getRoleIds())) {
            LambdaQueryWrapper<UsrUserRole> roleWrapper = Wrappers.lambdaQuery();
            roleWrapper.in(UsrUserRole::getRoleId, request.getRoleIds());
            List<UsrUserRole> userRoles = usrUserRoleMapper.selectList(roleWrapper);
            List<Long> roleUserIds = userRoles.stream().map(UsrUserRole::getUserId).distinct().toList();
            if (roleUserIds.isEmpty()) {
                emptyResult = true;
            } else {
                wrapper.in(UsrUser::getId, roleUserIds);
            }
        }

        // 剩余时长范围筛选
        if (!emptyResult && (request.getRemainingMinutesMin() != null || request.getRemainingMinutesMax() != null)) {
            LambdaQueryWrapper<UsrUserDuration> durationWrapper = Wrappers.lambdaQuery();
            if (request.getRemainingMinutesMin() != null) {
                durationWrapper.ge(UsrUserDuration::getRemainingMinutes, request.getRemainingMinutesMin());
            }
            if (request.getRemainingMinutesMax() != null) {
                durationWrapper.le(UsrUserDuration::getRemainingMinutes, request.getRemainingMinutesMax());
            }
            List<UsrUserDuration> durations = usrUserDurationMapper.selectList(durationWrapper);
            List<Long> durationUserIds = durations.stream().map(UsrUserDuration::getUserId).distinct().toList();
            if (durationUserIds.isEmpty()) {
                emptyResult = true;
            } else {
                wrapper.in(UsrUser::getId, durationUserIds);
            }
        }

        wrapper.orderByDesc(UsrUser::getCreatedAt);

        List<UsrUser> userList = emptyResult ? List.of() : usrUserMapper.selectList(wrapper);

        List<Long> userIds = userList.stream().map(UsrUser::getId).toList();
        Map<Long, UsrUserDuration> durationMap = batchQueryDuration(userIds);
        Map<Long, UsrUserBalance> balanceMap = batchQueryBalance(userIds);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(CommonConstants.DEFAULT_DATE_TIME_FORMAT);

        List<UserExportVO> voList = userList.stream().map(u -> {
            UserExportVO vo = new UserExportVO();
            vo.setUsername(u.getUsername());
            vo.setNickname(u.getNickname());
            vo.setPhone(u.getPhone());
            vo.setRegisterTime(u.getRegisterTime() != null ? u.getRegisterTime().format(formatter) : "");
            vo.setStatusLabel(switch (u.getStatus() != null ? u.getStatus() : 1) {
                case 2 -> "禁用";
                case 3 -> "冻结";
                case 4 -> "锁定";
                default -> "正常";
            });
            UsrUserDuration duration = durationMap.get(u.getId());
            vo.setRemainingMinutes(duration != null ? duration.getRemainingMinutes() : 0);
            UsrUserBalance balance = balanceMap.get(u.getId());
            vo.setTotalRechargeCents(balance != null ? balance.getTotalRechargeCents() : 0);
            vo.setLastLoginTime(u.getLastLoginTime() != null ? u.getLastLoginTime().format(formatter) : "");
            return vo;
        }).toList();

        try {
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("UTF-8");
            String fileName = URLEncoder.encode("用户列表", StandardCharsets.UTF_8).replaceAll("\\+", "%20");
            response.setHeader("Content-Disposition", "attachment;filename=" + fileName + ".xlsx");
            EasyExcel.write(response.getOutputStream(), UserExportVO.class).sheet("用户列表").doWrite(voList);
        } catch (IOException e) {
            throw new BizException(CommonErrorCode.SYSTEM_ERROR, "导出 Excel 失败");
        }
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
        if (userIds == null || userIds.isEmpty()) {
            return Map.of();
        }
        LambdaQueryWrapper<UsrUserDuration> wrapper = Wrappers.lambdaQuery();
        wrapper.in(UsrUserDuration::getUserId, userIds);
        return usrUserDurationMapper.selectList(wrapper).stream()
                .collect(Collectors.toMap(UsrUserDuration::getUserId, d -> d));
    }

    /**
     * 批量查询用户余额。
     */
    private Map<Long, UsrUserBalance> batchQueryBalance(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Map.of();
        }
        LambdaQueryWrapper<UsrUserBalance> wrapper = Wrappers.lambdaQuery();
        wrapper.in(UsrUserBalance::getUserId, userIds);
        return usrUserBalanceMapper.selectList(wrapper).stream()
                .collect(Collectors.toMap(UsrUserBalance::getUserId, b -> b));
    }

    /**
     * 批量查询用户角色编码。
     */
    private Map<Long, List<String>> batchQueryRoleCodes(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Map.of();
        }
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
        vo.setLoginCount(user.getLoginCount());
        vo.setAbnormalLogin(user.getAbnormalLogin());
        vo.setLastLoginTime(user.getLastLoginTime());
        vo.setLastLoginIp(user.getLastLoginIp());
        vo.setRegisterIp(user.getRegisterIp());
        vo.setRegisterDeviceInfo(user.getRegisterDeviceInfo());
        vo.setRegisterTime(user.getRegisterTime());
        vo.setInvitationCodeUsed(user.getInvitationCodeUsed());
        vo.setIsCompetition(user.getIsCompetition());
        vo.setRoleCodes(roleCodes);
        vo.setRemainingMinutes(duration != null ? duration.getRemainingMinutes() : 0);
        vo.setBalanceCents(balance != null ? balance.getBalanceCents() : 0);
        vo.setCreatedAt(user.getCreatedAt());
        vo.setUpdatedAt(user.getUpdatedAt());
        return vo;
    }

    /**
     * 密码复杂度校验。
     * 当 security.password 开关关闭时，仅检查非空和最小长度兜底。
     * 当密码是默认密码 "123456" 时，跳过所有复杂度验证。
     */
    private void validatePassword(String password) {
        log.info("validatePassword: password='{}', length={}", password, password != null ? password.length() : -1);
        if (password != null) {
            StringBuilder hex = new StringBuilder();
            for (char c : password.toCharArray()) {
                hex.append(String.format("%04x ", (int) c));
            }
            log.info("validatePassword: password hex: {}", hex.toString().trim());
        }
        if (!StringUtils.hasText(password)) {
            throw new BizException(CommonErrorCode.PARAM_VALIDATION_FAILED, "密码不能为空");
        }
        // 默认密码 123456 跳过复杂度验证
        if ("123456".equals(password)) {
            log.info("validatePassword: default password detected, skipping validation");
            return;
        }
        int minLength = parseConfigInt("password_min_length", 6);
        log.info("validatePassword: minLength={}", minLength);
        if (password.length() < minLength) {
            throw new BizException(CommonErrorCode.PARAM_VALIDATION_FAILED,
                    "密码长度不能少于 " + minLength + " 位");
        }
        if (featureSwitchService.isEnabled("security.password")) {
            if (!password.matches("^(?=.*[A-Za-z])(?=.*\\d).*+$")) {
                throw new BizException(CommonErrorCode.PARAM_VALIDATION_FAILED,
                        "密码必须同时包含字母和数字");
            }
        }
    }

    private int parseConfigInt(String key, int defaultValue) {
        String value = sysConfigService.getConfigValue(key, "all");
        if (!StringUtils.hasText(value)) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
