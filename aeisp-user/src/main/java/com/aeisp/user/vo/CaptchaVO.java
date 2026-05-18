package com.aeisp.user.vo;

import lombok.Builder;
import lombok.Data;

/**
 * 验证码响应 VO。
 *
 * @author AEISP Team
 */
@Data
@Builder
public class CaptchaVO {

    /**
     * 验证码唯一标识（用于校验时回传）。
     */
    private String captchaKey;

    /**
     * Base64 编码的验证码图片（data:image/png;base64,...）。
     */
    private String captchaImage;

    /**
     * 过期时间（秒）。
     */
    private Long expireSeconds;
}
