package com.bilibili.service.handler;

import com.bilibili.domain.JsonResponse;
import com.bilibili.exception.ConditionException;
import com.bilibili.service.UserService;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

/**
 * 全局异常处理
 */
@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CommonGlobalExceptionHandler {
    
    @ExceptionHandler(value = Exception.class)  // 标识这是一个异常处理器
    @ResponseBody 
    public JsonResponse<String> commonExceptionHandler(HttpServletRequest request, Exception e) {
        String errorMsg = e.getMessage();
        if (e instanceof ConditionException) {
            String errorCode = ((ConditionException)e).getCode();
            // 抛异常直接返回
            return  new JsonResponse<>(errorCode, errorMsg);
        }
        return new JsonResponse<>("500", errorMsg); // TODO: 将errorMsg报错信息返回给前端会不会不太安全？
    }
}
