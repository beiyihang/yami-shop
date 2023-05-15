package com.yami.shop.common.handler;

import cn.hutool.core.util.CharsetUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yami.shop.common.exception.YamiShopBindException;
import com.yami.shop.common.response.ServerResponseEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Objects;

/**
 * @author 北易航
 * @date 2022/3/28 14:15
 */
@Component
public class HttpHandler {

    private static final Logger logger = LoggerFactory.getLogger(HttpHandler.class);

    @Autowired
    private ObjectMapper objectMapper;

    public <T> void printServerResponseToWeb(ServerResponseEntity<T> serverResponseEntity) {
        // 检查输入参数是否为空
        if (serverResponseEntity == null) {
            logger.info("print obj is null");
            return;
        }

        // 获取当前请求的 ServletRequestAttributes
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        // 检查 ServletRequestAttributes 是否为空
        if (requestAttributes == null) {
            logger.error("requestAttributes is null, can not print to web");
            return;
        }

        // 获取 HttpServletResponse 对象
        HttpServletResponse response = requestAttributes.getResponse();
        // 检查 HttpServletResponse 是否为空
        if (response == null) {
            logger.error("httpServletResponse is null, can not print to web");
            return;
        }

        // 打印错误信息到日志
        logger.error("response error: " + serverResponseEntity.getMsg());

        // 设置响应的字符编码和内容类型
        response.setCharacterEncoding(CharsetUtil.UTF_8);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        PrintWriter printWriter = null;
        try {
            // 获取响应输出流
            printWriter = response.getWriter();
            // 将 ServerResponseEntity 对象转换为 JSON 字符串，并写入响应输出流中
            printWriter.write(objectMapper.writeValueAsString(serverResponseEntity));
        } catch (IOException e) {
            // 抛出 YamiShopBindException 异常，表示处理 IO 异常
            throw new YamiShopBindException("IO 异常", e);
        }
    }

    public <T> void printServerResponseToWeb(YamiShopBindException yamiShopBindException) {
        // 检查输入参数是否为空
        if (yamiShopBindException == null) {
            logger.info("print obj is null");
            return;
        }

        // 检查 YamiShopBindException 是否包含 ServerResponseEntity
        if (Objects.nonNull(yamiShopBindException.getServerResponseEntity())) {
            // 如果存在 ServerResponseEntity，则调用 printServerResponseToWeb 方法打印到 Web 响应
            printServerResponseToWeb(yamiShopBindException.getServerResponseEntity());
            return;
        }

        // 创建新的 ServerResponseEntity 对象，并设置错误代码和错误消息
        ServerResponseEntity<T> serverResponseEntity = new ServerResponseEntity<>();
        serverResponseEntity.setCode(yamiShopBindException.getCode());
        serverResponseEntity.setMsg(yamiShopBindException.getMessage());

        // 调用 printServerResponseToWeb 方法将 ServerResponseEntity 打印到 Web 响应
        printServerResponseToWeb(serverResponseEntity);
    }

}
