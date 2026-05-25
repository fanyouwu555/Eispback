package com.aeisp.boot.controller;

import com.aeisp.boot.request.LoginRequest;
import com.aeisp.common.security.CustomUserDetails;
import com.aeisp.boot.vo.LoginResponse;
import com.aeisp.boot.vo.UserInfoVO;
import com.aeisp.boot.util.UserAgentUtil;
import com.aeisp.common.Result;
import com.aeisp.common.constant.CommonConstants;
import com.aeisp.boot.code.BootErrorCode;
import com.aeisp.common.code.CommonErrorCode;
import com.aeisp.common.util.JwtUtil;
import com.aeisp.common.util.TokenBlacklistUtil;
import com.aeisp.system.entity.SysOperationLog;
import com.aeisp.system.service.SysOperationLogService;
import com.aeisp.user.entity.UsrLoginLog;
import com.aeisp.user.entity.UsrUser;
import com.aeisp.system.entity.SysUserBehaviorLog;
import com.aeisp.system.mapper.SysUserBehaviorLogMapper;
import com.aeisp.user.service.UsrLoginLogService;
import com.aeisp.user.service.UsrUserService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * 认证相关 Controller。
 *
 * <p>提供统一的登录、Token 刷新接口，支持管理员（sys_user）和普通用户（usr_user）两套账号体系。
 * 路径前缀 {@code /api/v1/auth}。</p>
 *
 * @author AEISP Team
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UsrLoginLogService usrLoginLogService;
    private final SysOperationLogService sysOperationLogService;
    private final TokenBlacklistUtil tokenBlacklistUtil;
    private final com.aeisp.boot.security.CustomUserDetailsService customUserDetailsService;
    private final UsrUserService usrUserService;
    private final com.aeisp.system.service.SysUserService sysUserService;
    private final SysUserBehaviorLogMapper sysUserBehaviorLogMapper;

    /**
     * 用户登录。
     *
     * <p>支持管理员和普通用户登录，认证成功后返回 Access Token 和 Refresh Token。
     * 同时记录登录日志。</p>
     *
     * @param request     登录请求
     * @param httpRequest HTTP 请求（用于获取 IP 和设备信息）
     * @return 登录结果
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request,
                                       HttpServletRequest httpRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(), request.getPassword()));
            SecurityContextHolder.getContext().setAuthentication(authentication);

            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            List<String> roles = userDetails.getRoles();

            // 前端用户：检查并自动解除过期锁定，清零失败次数，更新登录统计
            if ("user".equals(userDetails.getUserType())) {
                usrUserService.checkAccountLock(userDetails.getUserId());
                usrUserService.resetFailedAttempts(userDetails.getUserId());
                String ip = getClientIp(httpRequest);
                usrUserService.recordLoginSuccess(userDetails.getUserId(), ip);
            }

            String accessToken = jwtUtil.generateToken(
                    String.valueOf(userDetails.getUserId()),
                    userDetails.getUsername(),
                    roles);
            String refreshToken = jwtUtil.generateRefreshToken(
                    String.valueOf(userDetails.getUserId()));

            // 记录登录日志
            recordLoginLog(userDetails, httpRequest, true, null);

            LoginResponse response = LoginResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .expiresIn(jwtUtil.getAccessExpiration() / 1000)
                    .userInfo(UserInfoVO.builder()
                            .id(userDetails.getUserId())
                            .username(userDetails.getUsername())
                            .userType(userDetails.getUserType())
                            .roles(roles)
                            .build())
                    .build();

            return Result.success(response, "登录成功");
        } catch (BadCredentialsException e) {
            log.warn("登录失败，用户名或密码错误: {}", request.getUsername());
            // 尝试查找用户并记录失败次数
            String ip = getClientIp(httpRequest);
            String device = httpRequest.getHeader("User-Agent");
            handleFrontendLoginFailure(request.getUsername(), ip, device);
            return Result.error(BootErrorCode.PASSWORD_MISMATCH);
        } catch (DisabledException e) {
            log.warn("登录失败，账号已禁用: {}", request.getUsername());
            return Result.error(BootErrorCode.ACCOUNT_DISABLED);
        } catch (Exception e) {
            log.error("登录异常: {}", e.getMessage(), e);
            return Result.error(CommonErrorCode.SYSTEM_ERROR, "登录失败，请稍后重试");
        }
    }

    /**
     * 刷新 Access Token。
     *
     * <p>使用 Refresh Token 换取新的 Access Token，刷新前校验黑名单。</p>
     *
     * @param refreshToken Refresh Token
     * @return 新的 Token 信息
     */
    @PostMapping("/refresh")
    public Result<LoginResponse> refresh(@RequestHeader("X-Refresh-Token") String refreshToken) {
        try {
            // 检查 Refresh Token 是否已被拉黑
            if (tokenBlacklistUtil.isBlacklisted(refreshToken)) {
                return Result.error(BootErrorCode.TOKEN_INVALID);
            }
            Claims claims = jwtUtil.parseToken(refreshToken);
            String type = claims.get("type", String.class);
            if (!"refresh".equals(type)) {
                return Result.error(BootErrorCode.TOKEN_TYPE_INVALID);
            }
            String userId = claims.get("userId", String.class);
            CustomUserDetails userDetails = (CustomUserDetails) customUserDetailsService.loadUserByUserId(Long.parseLong(userId));
            String accessToken = jwtUtil.generateToken(userId, userDetails.getUsername(), userDetails.getRoles());
            String newRefreshToken = jwtUtil.generateRefreshToken(userId);

            LoginResponse response = LoginResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(newRefreshToken)
                    .tokenType("Bearer")
                    .expiresIn(jwtUtil.getAccessExpiration() / 1000)
                    .userInfo(UserInfoVO.builder()
                            .id(userDetails.getUserId())
                            .username(userDetails.getUsername())
                            .userType(userDetails.getUserType())
                            .roles(userDetails.getRoles())
                            .build())
                    .build();

            return Result.success(response, "刷新成功");
        } catch (Exception e) {
            log.warn("刷新 Token 失败: {}", e.getMessage());
            return Result.error(BootErrorCode.TOKEN_MISSING);
        }
    }

    /**
     * 用户登出。
     *
     * <p>将当前 Access Token 加入黑名单，使其立即失效。</p>
     *
     * @param authorization Authorization 请求头
     * @return 登出结果
     */
    @PostMapping("/logout")
    public Result<Void> logout(@RequestHeader("Authorization") String authorization,
                               HttpServletRequest httpRequest) {
        String token = resolveBearerToken(authorization);
        Long userId = null;
        if (token != null) {
            try {
                Claims claims = jwtUtil.parseToken(token);
                Date expiration = claims.getExpiration();
                tokenBlacklistUtil.addToBlacklist(token, expiration);
                userId = Long.valueOf(claims.get("userId", String.class));
            } catch (Exception e) {
                log.warn("登出时解析 Token 失败: {}", e.getMessage());
            }
        }
        // 记录用户行为日志
        if (userId != null && sysUserBehaviorLogMapper != null) {
            SysUserBehaviorLog behaviorLog = new SysUserBehaviorLog();
            behaviorLog.setUserId(userId);
            behaviorLog.setBehaviorType("LOGOUT");
            behaviorLog.setIpAddress(getClientIp(httpRequest));
            behaviorLog.setDeviceInfo(httpRequest.getHeader("User-Agent"));
            behaviorLog.setCreatedAt(LocalDateTime.now());
            sysUserBehaviorLogMapper.insert(behaviorLog);
        }
        SecurityContextHolder.clearContext();
        return Result.success(null, "登出成功");
    }

    /**
     * 获取当前登录用户信息。
     *
     * <p>解析 Token 并返回用户基本信息、角色和权限列表。</p>
     *
     * @param authorization Authorization 请求头
     * @return 用户信息
     */
    @GetMapping("/info")
    public Result<UserInfoVO> getInfo(@RequestHeader("Authorization") String authorization) {
        String token = resolveBearerToken(authorization);
        if (token == null) {
            return Result.error(BootErrorCode.TOKEN_MISSING);
        }
        try {
            Claims claims = jwtUtil.parseToken(token);
            String userId = claims.get("userId", String.class);
            CustomUserDetails userDetails = (CustomUserDetails) customUserDetailsService.loadUserByUserId(Long.parseLong(userId));
            UserInfoVO vo = UserInfoVO.builder()
                    .id(userDetails.getUserId())
                    .username(userDetails.getUsername())
                    .userType(userDetails.getUserType())
                    .roles(userDetails.getRoles())
                    .permissions(userDetails.getPermissions())
                    .build();
            return Result.success(vo, "获取成功");
        } catch (Exception e) {
            log.warn("获取用户信息失败: {}", e.getMessage());
            return Result.error(BootErrorCode.TOKEN_INVALID);
        }
    }

    /**
     * 处理前端用户登录失败（记录失败次数）。
     */
    private void handleFrontendLoginFailure(String username, String ip, String device) {
        try {
            UsrUser user = usrUserService.findByUsername(username);
            if (user != null) {
                usrUserService.handleLoginFailure(user.getId());
            }
            recordFailedLoginLog(username, ip, device);
        } catch (Exception ex) {
            log.debug("记录登录失败时异常: {}", ex.getMessage());
        }
    }

    /**
     * 记录失败登录日志（未经过认证，只有用户名）。
     */
    private void recordFailedLoginLog(String username, String ip, String device) {
        try {
            UsrLoginLog log = new UsrLoginLog();
            log.setLoginAccount(username);
            log.setLoginType(1);
            log.setLoginResult(2); // 密码错误
            log.setIpAddress(ip);
            log.setDeviceType(UserAgentUtil.parseDeviceType(device));
            log.setOsInfo(UserAgentUtil.parseOs(device));
            String browser = UserAgentUtil.parseBrowser(device);
            log.setBrowserInfo(browser != null ? browser : device);
            log.setCreatedAt(LocalDateTime.now());
            usrLoginLogService.saveLog(log);
        } catch (Exception e) {
            log.debug("记录失败登录日志异常: {}", e.getMessage());
        }
    }

    private static String resolveBearerToken(String authorization) {
        if (authorization != null && authorization.startsWith("Bearer ")) {
            return authorization.substring(7);
        }
        return null;
    }

    /**
     * 记录登录日志。
     *
     * @param userDetails 用户信息
     * @param request     HTTP 请求
     * @param success     是否成功
     * @param errorMsg    错误信息
     */
    private void recordLoginLog(CustomUserDetails userDetails,
                                HttpServletRequest request,
                                boolean success,
                                String errorMsg) {
        String ip = getClientIp(request);
        String device = request.getHeader("User-Agent");
        if ("user".equals(userDetails.getUserType())) {
            UsrLoginLog log = new UsrLoginLog();
            log.setUserId(userDetails.getUserId());
            log.setLoginAccount(userDetails.getUsername());
            log.setLoginType(1);
            log.setLoginResult(success ? 1 : 2);
            log.setIpAddress(ip);
            log.setDeviceType(UserAgentUtil.parseDeviceType(device));
            log.setOsInfo(UserAgentUtil.parseOs(device));
            String browser = UserAgentUtil.parseBrowser(device);
            log.setBrowserInfo(browser != null ? browser : device);
            log.setCreatedAt(LocalDateTime.now());
            usrLoginLogService.saveLog(log);

            // 记录用户行为日志
            if (success && sysUserBehaviorLogMapper != null) {
                SysUserBehaviorLog behaviorLog = new SysUserBehaviorLog();
                behaviorLog.setUserId(userDetails.getUserId());
                behaviorLog.setBehaviorType("LOGIN");
                behaviorLog.setIpAddress(ip);
                behaviorLog.setDeviceInfo(device);
                behaviorLog.setCreatedAt(LocalDateTime.now());
                sysUserBehaviorLogMapper.insert(behaviorLog);
            }
        } else {
            SysOperationLog log = new SysOperationLog();
            log.setUserId(userDetails.getUserId());
            log.setOperatorUsername(userDetails.getUsername());
            log.setOperationType("AUTH_LOGIN");
            log.setOperationTypeLabel("登录");
            log.setTargetType("user");
            log.setTargetId(String.valueOf(userDetails.getUserId()));
            log.setOperationDetail("{}");
            log.setRequestMethod(request.getMethod());
            log.setRequestUrl(request.getRequestURI());
            log.setStatus(success ? CommonConstants.STATUS_ENABLED : CommonConstants.STATUS_DISABLED);
            log.setErrorMsg(errorMsg);
            log.setIpAddress(ip);
            log.setDuration(0L);
            log.setSensitivity(1);
            log.setCreatedAt(LocalDateTime.now());
            sysOperationLogService.saveLog(log);

            // 更新管理员最后登录 IP 和时间
            if (success) {
                sysUserService.updateLastLoginInfo(userDetails.getUserId(), ip, LocalDateTime.now());
            }
        }
    }

    /**
     * 获取客户端真实 IP。
     *
     * @param request HTTP 请求
     * @return 客户端 IP
     */
    private static String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
