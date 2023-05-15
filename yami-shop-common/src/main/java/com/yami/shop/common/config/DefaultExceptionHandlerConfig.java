

package com.yami.shop.common.config;

import com.yami.shop.common.exception.YamiShopBindException;
import com.yami.shop.common.response.ResponseEnum;
import com.yami.shop.common.response.ServerResponseEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.List;

/**
 * 自定义错误处理器
 * @author 北易航
 */
@Slf4j
@Controller
@RestControllerAdvice
public class DefaultExceptionHandlerConfig {

    @ExceptionHandler({ MethodArgumentNotValidException.class, BindException.class })
    public ResponseEntity<ServerResponseEntity<List<String>>> methodArgumentNotValidExceptionHandler(Exception e) {
        log.error("methodArgumentNotValidExceptionHandler", e);
        List<FieldError> fieldErrors = null;

        // 判断异常类型，分别处理MethodArgumentNotValidException和BindException
        if (e instanceof MethodArgumentNotValidException) {
            fieldErrors = ((MethodArgumentNotValidException) e).getBindingResult().getFieldErrors();
        }
        if (e instanceof BindException) {
            fieldErrors = ((BindException) e).getBindingResult().getFieldErrors();
        }

        // 如果fieldErrors为null，则表示没有找到对应的字段错误信息，返回通用的错误响应
        if (fieldErrors == null) {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(ServerResponseEntity.fail(ResponseEnum.METHOD_ARGUMENT_NOT_VALID));
        }

        // 构建字段错误信息列表
        List<String> defaultMessages = new ArrayList<>(fieldErrors.size());
        for (FieldError fieldError : fieldErrors) {
            defaultMessages.add(fieldError.getField() + ":" + fieldError.getDefaultMessage());
        }

        // 返回带有字段错误信息的错误响应
        return ResponseEntity.status(HttpStatus.OK)
                .body(ServerResponseEntity.fail(ResponseEnum.METHOD_ARGUMENT_NOT_VALID, defaultMessages));
    }


    @ExceptionHandler(YamiShopBindException.class)
    public ResponseEntity<ServerResponseEntity<?>> unauthorizedExceptionHandler(YamiShopBindException e) {
        log.error("mall4jExceptionHandler", e);

        ServerResponseEntity<?> serverResponseEntity = e.getServerResponseEntity();

        // 判断是否有自定义的ServerResponseEntity，如果有，则直接返回该响应
        if (serverResponseEntity != null) {
            return ResponseEntity.status(HttpStatus.OK).body(serverResponseEntity);
        }

        // 如果没有自定义的ServerResponseEntity，则构建一个包含错误信息的通用响应
        // 使用异常中的错误码和错误消息构建响应
        return ResponseEntity.status(HttpStatus.OK).body(ServerResponseEntity.fail(e.getCode(), e.getMessage()));
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<ServerResponseEntity<Object>> exceptionHandler(Exception e) {
        log.error("exceptionHandler", e);

        // 记录异常信息
        log.error("exceptionHandler", e);

        // 构建一个通用的错误响应，使用默认的错误码和消息
        ServerResponseEntity<Object> responseEntity = ServerResponseEntity.fail(ResponseEnum.EXCEPTION);

        // 返回响应
        return ResponseEntity.status(HttpStatus.OK).body(responseEntity);
    }

}