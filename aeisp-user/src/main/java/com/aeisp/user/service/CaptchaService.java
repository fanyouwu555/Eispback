package com.aeisp.user.service;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import com.aeisp.common.constant.CacheConstants;
import com.aeisp.common.util.RedisUtil;
import com.aeisp.user.vo.CaptchaVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 验证码服务。
 *
 * <p>基于 Hutool 生成图形验证码，存储到 Redis 并设置过期时间。</p>
 *
 * @author AEISP Team
 */
@Service
@RequiredArgsConstructor
public class CaptchaService {

    private final RedisUtil redisUtil;

    /**
     * 验证码过期时间（分钟）。
     */
    private static final long CAPTCHA_EXPIRE_MINUTES = 5;

    /**
     * 验证码图片宽度。
     */
    private static final int CAPTCHA_WIDTH = 120;

    /**
     * 验证码图片高度。
     */
    private static final int CAPTCHA_HEIGHT = 40;

    /**
     * 验证码字符数。
     */
    private static final int CAPTCHA_CODE_COUNT = 4;

    /**
     * 生成图形验证码。
     *
     * @return 验证码 VO
     */
    public CaptchaVO generateCaptcha() {
        LineCaptcha captcha = CaptchaUtil.createLineCaptcha(CAPTCHA_WIDTH, CAPTCHA_HEIGHT, CAPTCHA_CODE_COUNT, 20);
        String code = captcha.getCode();
        String imageBase64 = captcha.getImageBase64Data();
        String captchaKey = UUID.randomUUID().toString();

        String redisKey = CacheConstants.CACHE_CAPTCHA + captchaKey;
        redisUtil.set(redisKey, code, CAPTCHA_EXPIRE_MINUTES, TimeUnit.MINUTES);

        return CaptchaVO.builder()
                .captchaKey(captchaKey)
                .captchaImage(imageBase64)
                .expireSeconds(CAPTCHA_EXPIRE_MINUTES * 60)
                .build();
    }

    /**
     * 校验验证码。
     *
     * @param captchaKey  验证码标识
     * @param captchaCode 用户输入的验证码
     * @return true 表示校验通过
     */
    public boolean verifyCaptcha(String captchaKey, String captchaCode) {
        if (captchaKey == null || captchaKey.isBlank() || captchaCode == null || captchaCode.isBlank()) {
            return false;
        }
        String redisKey = CacheConstants.CACHE_CAPTCHA + captchaKey;
        String storedCode = redisUtil.get(redisKey);
        if (storedCode == null) {
            return false;
        }
        boolean valid = storedCode.equalsIgnoreCase(captchaCode);
        if (valid) {
            redisUtil.delete(redisKey);
        }
        return valid;
    }
}
