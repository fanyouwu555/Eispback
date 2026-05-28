package com.aeisp.user.controller;

import com.aeisp.common.Result;
import com.aeisp.common.code.CommonErrorCode;
import com.aeisp.common.exception.BizException;
import com.aeisp.user.entity.UsrUser;
import com.aeisp.user.request.UserRegisterRequest;
import com.aeisp.user.service.CaptchaService;
import com.aeisp.user.service.UsrUserService;
import com.aeisp.user.vo.CaptchaVO;
import com.aeisp.system.service.SysFeatureSwitchService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 认证相关 Controller。
 *
 * <p>提供前端用户的自主注册、Token 刷新等接口。
 * 路径前缀 {@code /api/v1/auth}。</p>
 *
 * @author AEISP Team
 */
@Slf4j
@RestController("userAuthController")
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UsrUserService usrUserService;
    private final CaptchaService captchaService;
    private final SysFeatureSwitchService featureSwitchService;

    /**
     * 获取图形验证码。
     *
     * @return 验证码图片及标识
     */
    @GetMapping("/captcha")
    public Result<CaptchaVO> getCaptcha() {
        CaptchaVO captcha = captchaService.generateCaptcha();
        return Result.success(captcha);
    }

    /**
     * 用户自主注册。
     *
     * @param request 注册请求
     * @param httpRequest HTTP 请求（用于获取注册 IP）
     * @return 注册结果
     */
    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody UserRegisterRequest request,
                                 HttpServletRequest httpRequest) {
        if (!featureSwitchService.isEnabled("user.register")) {
            return Result.error(CommonErrorCode.SYSTEM_ERROR, "注册功能已关闭");
        }
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            return Result.error(CommonErrorCode.PARAM_VALIDATION_FAILED, "两次输入的密码不一致");
        }
        if (!"true".equalsIgnoreCase(request.getAgreement())) {
            return Result.error(CommonErrorCode.PARAM_VALIDATION_FAILED, "请同意用户协议");
        }
        if (!captchaService.verifyCaptcha(request.getCaptchaKey(), request.getCaptchaCode())) {
            return Result.error(CommonErrorCode.PARAM_VALIDATION_FAILED, "验证码错误或已过期");
        }

        UsrUser user = new UsrUser();
        user.setUsername(request.getUsername());
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());
        user.setRegisterIp(getClientIp(httpRequest));
        user.setRegisterDeviceInfo(httpRequest.getHeader("User-Agent"));

        boolean success = usrUserService.register(user);
        return success ? Result.success() : Result.error(CommonErrorCode.SYSTEM_ERROR, "注册失败");
    }

    /**
     * 获取客户端真实 IP。
     */
    private String getClientIp(HttpServletRequest request) {
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
