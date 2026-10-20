package com.restaurant.aspect;

import java.util.Arrays;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * AOP (Aspect Oriented Programming) - practical use: LOGGING.
 *
 * Logging is a "cross-cutting concern": every service needs it, but it is not business logic.
 * Instead of writing log statements in every method, this one aspect is applied automatically
 * to all methods of all classes in the service package.
 *
 *   Aspect     = this class (the logging module)
 *   Pointcut   = WHERE to apply it  -> every method in com.restaurant.service
 *   Advice     = WHAT to do         -> @Around: log before + after and measure time
 *   Join point = one actual method call, e.g. MenuService.findAll()
 */
@Aspect
@Component
public class LoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(LoggingAspect.class);

    @Pointcut("execution(* com.restaurant.service..*(..))")
    public void serviceMethods() {
    }

    @Around("serviceMethods()")
    public Object logExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        String method = joinPoint.getSignature().toShortString();
        log.info("[AOP] --> {} args={}", method, Arrays.toString(joinPoint.getArgs()));
        long start = System.currentTimeMillis();

        Object result = joinPoint.proceed(); // run the real method

        long took = System.currentTimeMillis() - start;
        log.info("[AOP] <-- {} finished in {} ms", method, took);
        return result;
    }

    @AfterThrowing(pointcut = "serviceMethods()", throwing = "ex")
    public void logException(JoinPoint joinPoint, Throwable ex) {
        log.warn("[AOP] !!! {} threw {}: {}", joinPoint.getSignature().toShortString(),
                ex.getClass().getSimpleName(), ex.getMessage());
    }
}
