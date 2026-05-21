package com.aeisp.boot.util;

/**
 * User-Agent 解析工具类，从 UA 字符串提取设备类型、操作系统、浏览器信息。
 */
public class UserAgentUtil {

    private UserAgentUtil() {}

    /**
     * 解析设备类型：desktop/mobile/tablet/unknown。
     */
    public static String parseDeviceType(String userAgent) {
        if (userAgent == null) return "unknown";
        String ua = userAgent.toLowerCase();
        if (ua.contains("ipad") || ua.contains("tablet") || ua.contains("playbook") || ua.contains("silk")) {
            return "tablet";
        }
        if (ua.contains("mobile") || ua.contains("iphone") || ua.contains("ipod") ||
            ua.contains("android") || ua.contains("blackberry") || ua.contains("windows phone")) {
            return "mobile";
        }
        return "desktop";
    }

    /**
     * 解析操作系统。
     */
    public static String parseOs(String userAgent) {
        if (userAgent == null) return null;
        String ua = userAgent.toLowerCase();
        if (ua.contains("windows nt 10")) return "Windows 10";
        if (ua.contains("windows nt 11")) return "Windows 11";
        if (ua.contains("windows nt 6.3")) return "Windows 8.1";
        if (ua.contains("windows nt 6.2")) return "Windows 8";
        if (ua.contains("windows nt 6.1")) return "Windows 7";
        if (ua.contains("windows")) return "Windows";
        if (ua.contains("mac os x") || ua.contains("macintosh")) return "macOS";
        if (ua.contains("android")) return "Android";
        if (ua.contains("iphone") || ua.contains("ipad") || ua.contains("ipod")) return "iOS";
        if (ua.contains("linux")) return "Linux";
        if (ua.contains("chrome os")) return "Chrome OS";
        return null;
    }

    /**
     * 解析浏览器名称。
     */
    public static String parseBrowser(String userAgent) {
        if (userAgent == null) return null;
        String ua = userAgent.toLowerCase();
        if (ua.contains("edg/") || ua.contains("edge/")) return "Edge";
        if (ua.contains("chrome/")) return "Chrome";
        if (ua.contains("firefox/")) return "Firefox";
        if (ua.contains("safari/") && !ua.contains("chrome")) return "Safari";
        if (ua.contains("opera") || ua.contains("opr/")) return "Opera";
        return null;
    }
}