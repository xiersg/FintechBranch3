package com.financialfinshieldguard.gold.aop;

import com.financialfinshieldguard.gold.model.OperateLog;
import com.financialfinshieldguard.gold.service.OperateLogService;
import com.financialfinshieldguard.gold.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Arrays;

@Slf4j
@Component
@Aspect
/**
 * 记录日志 增删改
 */
public class LogAspect {

    //自动注入！
    @Autowired
    private HttpServletRequest request;

    @Autowired
    private OperateLogService operateLogService;

    @Around("@annotation(com.financialfinshieldguard.gold.annotation.Log)")
    public Object recordLog(ProceedingJoinPoint joinPoint) throws Throwable {

        //获取操作人ID(从request的token中解析出来！！！)
        String jwt = request.getHeader("token");
        Claims claims = JwtUtil.parse(jwt);
        String userIdStr = claims.getSubject();
        Long userId = Long.parseLong(userIdStr);

        //获取当前操作时间
        LocalDateTime operateTime = LocalDateTime.now();
        //获取执行方法的全类名
        String className = joinPoint.getTarget().getClass().getName();
        //获取执行方法名
        String methodName = joinPoint.getSignature().getName();
        //获取方法运行时参数
        Object[] args = joinPoint.getArgs();
        String methodParams = Arrays.toString(args);

        long begin = System.currentTimeMillis();
        //返回值
        Object result = joinPoint.proceed();
        long end = System.currentTimeMillis();

        //方法执行时长
        Long costTime = end - begin;

        OperateLog log = new OperateLog(null, userId, operateTime, className, methodName, methodParams, result.toString(), costTime);
        //插入日志
        operateLogService.save(log);
        LogAspect.log.info("插入操作日志:{}", log);

        return result;
    };
}
