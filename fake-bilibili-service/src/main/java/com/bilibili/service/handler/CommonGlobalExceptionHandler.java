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

@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CommonGlobalExceptionHandler {
    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    public JsonResponse<String> commonExceptionHandler(HttpServletRequest request, Exception e){
        String errorMsg = e.getMessage();
        if (e instanceof ConditionException) {
            String errorCode = ((ConditionException)e).getCode();
            return  new JsonResponse<>(errorCode, errorMsg);
        }
        return new JsonResponse<>("500",errorMsg);
    }

}
