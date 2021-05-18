package com.example.aspect;


import com.example.exception.CustomHttpException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


/**
 * 系统切面类
 */
//@Aspect
//@Component
public class SystemAspect {
    /**
     * 日志声明
     */
    private static Logger logger= LoggerFactory.getLogger(SystemAspect.class);
    MethodSignature signature;
    Optional<Object> name;
    @Around("execution(* com.example.controller.*.*(..))")
    public Object TokenCheck(ProceedingJoinPoint proceedingJoinPoint) throws Throwable{
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();

        signature=(MethodSignature)proceedingJoinPoint.getSignature();
        Method method=signature.getMethod();
//        检测是否有name参数

        //获取函数参数和值
        Map<String,Object> parameterMap=new HashMap<>();
        Object[] args=proceedingJoinPoint.getArgs();
        String[] paraNames = signature.getParameterNames();
        //检查是否经过token校验
        Optional<Object> tokenCheck=Optional.ofNullable(request.getHeader("Token-Checked"));
        name=Optional.ofNullable(request.getHeader("name"));
        if(!"true".equals(tokenCheck.orElse(""))){
            String message="请求错误,未经过token校验";
            logger.error(message);
            throw new CustomHttpException(message, HttpStatus.UNAUTHORIZED.value());
        }
        for (int i = 0; i <paraNames.length ; i++) {
            parameterMap.put(paraNames[i],args[i]);
            logger.info(name.orElse("system").toString()+":"+method.getName()+"-params:"+paraNames[i]+":"+args[i].toString());
        }
        return proceedingJoinPoint.proceed();
    }

    /**
     * 日志输出返回值
     * @param result
     * @author john.xiao
     * @date 2020-10-30 09：37
     */
    @AfterReturning(returning = "result",pointcut ="execution(*  com.example.controller.*.*(..))" )
    public void getReturnMessage(Object result){
        Method method=signature.getMethod();
        logger.info(name.orElse("system")+":"+method.getName()+"-result:"+result);
    }


}
