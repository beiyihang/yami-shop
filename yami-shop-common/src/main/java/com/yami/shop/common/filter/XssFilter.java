

package com.yami.shop.common.filter;

import com.yami.shop.common.xss.XssWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 一些简单的安全过滤：
 * xss
 * @author 北易航
 */
@Component
public class XssFilter implements Filter {
    Logger logger = LoggerFactory.getLogger(getClass().getName());

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        // 将 ServletRequest 和 ServletResponse 转换为 HttpServletRequest 和 HttpServletResponse
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        // 打印请求的 URI
        logger.info("uri: {}", req.getRequestURI());

        // xss 过滤
        // 将请求对象包装为 XssWrapper，用于进行 XSS 过滤
        chain.doFilter(new XssWrapper(req), resp);
    }


    @Override
    public void destroy() {

    }
}
