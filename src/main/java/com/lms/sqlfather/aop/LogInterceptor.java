package com.lms.sqlfather.aop;


import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.UUID;

@Slf4j
@Aspect
@Component
public class LogInterceptor {


     public Object doInterceptor(ProceedingJoinPoint joinPoint) throws Throwable {
         StopWatch stopWatch=new StopWatch();

         stopWatch.start();

         RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
         HttpServletRequest request=((ServletRequestAttributes) requestAttributes).getRequest();
         // 生成请求唯一 id
         String requestId = UUID.randomUUID().toString();
         String url = request.getRequestURI();

         // 获取请求参数
         Object[] args = joinPoint.getArgs();
         String reqParam = "[" + StringUtils.join(args, ", ") + "]";
         // 输出请求日志
         log.info("request start，id: {}, path: {}, ip: {}, params: {}", requestId, url,
                 request.getRemoteHost(), reqParam);
         // 执行原方法
         Object result = joinPoint.proceed();
         // 输出响应日志
         stopWatch.stop();
         long totalTimeMillis = stopWatch.getTotalTimeMillis();
         log.info("request end, id: {}, cost: {}ms", requestId, totalTimeMillis);
         return result;
     }

}
