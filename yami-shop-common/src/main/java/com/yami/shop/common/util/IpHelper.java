package com.yami.shop.common.util;

import jakarta.servlet.http.HttpServletRequest;

/**
 * IP帮助工具
 * @author 北易航
 */
public class IpHelper {
    private static final String UNKNOWN = "unknown";

    /**
     * 得到用户的真实地址,如果有多个就取第一个
     *
     * @return
     */
    public static String getIpAddr() {
        // 获取当前请求的 HttpServletRequest 对象
        HttpServletRequest request = HttpContextUtils.getHttpServletRequest();

        // 如果 HttpServletRequest 对象为空，则返回 null
        if (request == null) {
            return null;
        }

        // 从请求头中获取 x-forwarded-for 字段的值作为 IP 地址
        String ip = request.getHeader("x-forwarded-for");

        // 如果获取的 IP 地址为空或长度为 0 或与 "unknown" 相同，则从其他请求头中获取 IP 地址
        if (ip == null || ip.length() == 0 || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }

        // 如果获取的 IP 地址为空或长度为 0 或与 "unknown" 相同，则直接从请求中获取 IP 地址
        if (ip == null || ip.length() == 0 || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        // 将获取到的 IP 地址按逗号分隔为多个可能的 IP，取第一个非空的 IP 地址并去除首尾空格，作为最终结果
        String[] ips = ip.split(",");
        return ips[0].trim();
    }



}
