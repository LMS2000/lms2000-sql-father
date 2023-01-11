package com.lms.sqlfather.aop;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.lms.sqlfather.annotation.AuthCheck;
import com.lms.sqlfather.common.ErrorCode;
import com.lms.sqlfather.exception.BusinessException;
import com.lms.sqlfather.model.entity.User;
import com.lms.sqlfather.service.UserService;
import org.apache.commons.collections4.BagUtils;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Aspect
@Component
public class AuthInterceptor {

    @Resource
    private UserService userService;

    @Around("@annotation(authCheck)")
    public Object doInterceptor(ProceedingJoinPoint joinPoint, AuthCheck authCheck) throws Throwable {
        List<String> anyRolesList = Arrays.stream(authCheck.anyRole()).filter(StringUtils::isNotBlank).collect(Collectors.toList());

        String mustRole = authCheck.mustRole();

        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();

        User loginUser = userService.getLoginUser(request);

        if (CollectionUtils.isNotEmpty(anyRolesList)) {
            String userRole = loginUser.getUserRole();
            if (!anyRolesList.contains(userRole)) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
            }
        }

        if (StringUtils.isNotBlank(mustRole)) {
            String userRole = loginUser.getUserRole();
            if (!mustRole.equals(userRole)) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
            }
        }

        return joinPoint.proceed();
    }
}
