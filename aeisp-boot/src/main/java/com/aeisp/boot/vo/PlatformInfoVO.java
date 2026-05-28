package com.aeisp.boot.vo;

import lombok.Builder;
import lombok.Data;

/**
 * 平台信息 VO。
 */
@Data
@Builder
public class PlatformInfoVO {

    private String name;
    private String website;
    private String foreDomain;
    private String backDomain;
    private String copyright;
    private String icp;
}
