package com.bilibili.api.aspect;

import com.bilibili.constant.AuthRoleConstant;
import com.bilibili.domain.UserMoment;
import com.bilibili.domain.auth.UserRole;
import com.bilibili.exception.ConditionException;
import com.bilibili.service.UserRoleService;
import com.bilibili.support.UserSupport;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Order(1)
@Component
@Aspect
public class DataLimitedAspect {

    @Autowired
    private UserSupport userSupport;

    @Autowired
    private UserRoleService userRoleService;

    @Pointcut("@annotation(com.bilibili.domain.annotation.DataLimited)")
    public void check(){
    }

    @Before("check()")
    public void doBefore(JoinPoint joinPoint){
        Long userId = userSupport.getCurrentUserId();
        List<UserRole> userRoleList = userRoleService.getUserRoleByUserId(userId);
        Set<String> roleCodeSet = userRoleList.stream().map(UserRole::getRoleCode).collect(Collectors.toSet());
        Object[] args = joinPoint.getArgs();
        for(Object arg : args){
          if (arg instanceof UserMoment) {
              UserMoment userMoment = (UserMoment)arg;
              String type = userMoment.getType();
              // LV1仅允许发布视频
              if (roleCodeSet.contains(AuthRoleConstant.ROLE_LV1) && ! "0".equals(type)) {
                  throw new ConditionException("参数异常");
              }
          }
        }
    }
}
