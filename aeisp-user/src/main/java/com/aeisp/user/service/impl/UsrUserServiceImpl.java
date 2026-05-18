package com.aeisp.user.service.impl;

import com.aeisp.common.PageResult;
import com.aeisp.common.constant.CommonConstants;
import com.aeisp.common.exception.BizException;
import com.aeisp.user.entity.UsrUser;
import com.aeisp.user.mapper.UsrUserMapper;
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
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

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
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean register(UsrUser user) {
        // 校验唯一性
        if (usrUserMapper.selectByUsername(user.getUsername()) != null) {
            throw new BizException("用户名已存在");
        }
        if (usrUserMapper.selectByPhone(user.getPhone()) != null) {
            throw new BizException("手机号已存在");
        }
        if (usrUserMapper.selectByEmail(user.getEmail()) != null) {
            throw new BizException("邮箱已存在");
        }

        // 加密密码并设置默认值
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setStatus(CommonConstants.STATUS_ENABLED);
        user.setRemainingDuration(0L);
        user.setTotalRecharge(0L);
        user.setRegisterTime(LocalDateTime.now());
        user.setDeleted(CommonConstants.DELETED_NO);
        user.setRoleCode(CommonConstants.DEFAULT_FRONTEND_ROLE_CODE);

        return usrUserMapper.insert(user) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createByAdmin(UserCreateRequest request) {
        // 校验唯一性
        if (usrUserMapper.selectByUsername(request.getUsername()) != null) {
            throw new BizException("用户名已存在");
        }
        if (usrUserMapper.selectByPhone(request.getPhone()) != null) {
            throw new BizException("手机号已存在");
        }
        if (usrUserMapper.selectByEmail(request.getEmail()) != null) {
            throw new BizException("邮箱已存在");
        }

        UsrUser user = new UsrUser();
        user.setUsername(request.getUsername());
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setStatus(request.getStatus() != null ? request.getStatus() : CommonConstants.STATUS_ENABLED);
        user.setRemainingDuration(request.getRemainingDuration() != null ? request.getRemainingDuration() : 0L);
        user.setTotalRecharge(0L);
        user.setRegisterTime(LocalDateTime.now());
        user.setDeleted(CommonConstants.DELETED_NO);
        user.setRoleCode(CommonConstants.DEFAULT_FRONTEND_ROLE_CODE);

        return usrUserMapper.insert(user) > 0;
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
            user.setPassword(passwordEncoder.encode(req.getPassword()));
            user.setStatus(req.getStatus() != null ? req.getStatus() : CommonConstants.STATUS_ENABLED);
            user.setRemainingDuration(req.getRemainingDuration() != null ? req.getRemainingDuration() : 0L);
            user.setTotalRecharge(0L);
            user.setRegisterTime(now);
            user.setDeleted(CommonConstants.DELETED_NO);
            user.setRoleCode(CommonConstants.DEFAULT_FRONTEND_ROLE_CODE);
            return user;
        }).toList();
        return usrUserMapper.batchInsert(users) > 0;
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
        // 不修改密码
        return usrUserMapper.updateById(user) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateStatus(Long userId, Integer status) {
        UsrUser user = new UsrUser();
        user.setId(userId);
        user.setStatus(status);
        return usrUserMapper.updateById(user) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean resetPassword(Long userId, String newPassword) {
        UsrUser user = new UsrUser();
        user.setId(userId);
        user.setPassword(passwordEncoder.encode(newPassword));
        return usrUserMapper.updateById(user) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean adjustDuration(AdjustDurationRequest request) {
        UsrUser existing = usrUserMapper.selectById(request.getUserId());
        if (existing == null) {
            throw new BizException("用户不存在");
        }

        long newDuration = existing.getRemainingDuration() + request.getDeltaMinutes();
        if (newDuration < 0) {
            throw new BizException("剩余时长不足，无法扣除");
        }

        UsrUser user = new UsrUser();
        user.setId(request.getUserId());
        user.setRemainingDuration(newDuration);
        return usrUserMapper.updateById(user) > 0;
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

        List<UsrUserVO> voList = resultPage.getRecords().stream()
                .map(this::convertToVO)
                .toList();
        return PageResult.of(resultPage, voList);
    }

    @Override
    public UsrUserVO getUserDetail(Long userId) {
        UsrUser user = usrUserMapper.selectById(userId);
        if (user == null || Objects.equals(user.getDeleted(), CommonConstants.DELETED_YES)) {
            return null;
        }
        return convertToVO(user);
    }

    /**
     * 将实体转换为 VO。
     */
    private UsrUserVO convertToVO(UsrUser user) {
        UsrUserVO vo = new UsrUserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setPhone(user.getPhone());
        vo.setEmail(user.getEmail());
        vo.setStatus(user.getStatus());
        vo.setRemainingDuration(user.getRemainingDuration());
        vo.setTotalRecharge(user.getTotalRecharge());
        vo.setLastLoginTime(user.getLastLoginTime());
        vo.setRegisterIp(user.getRegisterIp());
        vo.setRegisterDevice(user.getRegisterDevice());
        vo.setRegisterTime(user.getRegisterTime());
        vo.setRoleCode(user.getRoleCode());
        vo.setCreatedAt(user.getCreatedAt());
        vo.setUpdatedAt(user.getUpdatedAt());
        return vo;
    }
}
