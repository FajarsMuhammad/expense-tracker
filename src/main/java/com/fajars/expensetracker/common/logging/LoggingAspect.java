package com.fajars.expensetracker.common.logging;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Aspect for logging method execution in controllers and use cases
 */
@Aspect
@Component
@Slf4j
public class LoggingAspect {

    /**
     * Log all controller methods
     */
    @Around("execution(* com.fajars.expensetracker..*.controller..*(..))")
    public Object logControllerMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        return logMethodExecution(joinPoint, "CONTROLLER");
    }

    /**
     * Log all use case methods
     */
    @Around("execution(* com.fajars.expensetracker..*.usecase..*UseCase.*(..))")
    public Object logUseCaseMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        return logMethodExecution(joinPoint, "USECASE");
    }

    private Object logMethodExecution(ProceedingJoinPoint joinPoint, String layer) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String className = signature.getDeclaringType().getSimpleName();
        String methodName = signature.getName();

        // Sanitize arguments (hide sensitive data)
        String args = Arrays.stream(joinPoint.getArgs())
                .map(this::sanitizeArgument)
                .collect(Collectors.joining(", "));

        log.debug("[{}] Entering: {}.{}() with args: [{}]", layer, className, methodName, args);

        long startTime = System.currentTimeMillis();
        Object result = null;
        try {
            result = joinPoint.proceed();
            long executionTime = System.currentTimeMillis() - startTime;
            log.debug("[{}] Completed: {}.{}() in {}ms", layer, className, methodName, executionTime);
            return result;
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            log.error("[{}] Exception in {}.{}() after {}ms: {}",
                    layer, className, methodName, executionTime, e.getMessage());
            throw e;
        }
    }

    /**
     * Sanitize argument for logging (mask sensitive data)
     */
    private String sanitizeArgument(Object arg) {
        if (arg == null) {
            return "null";
        }

        String argString = arg.toString();

        // Check if it contains sensitive data keywords
        if (SensitiveDataFilter.containsSensitiveData(argString)) {
            // If it's a complex object with sensitive data, just show class name
            if (argString.contains("password") || argString.contains("token")) {
                return arg.getClass().getSimpleName() + "[REDACTED]";
            }
            // Otherwise mask the sensitive parts
            argString = SensitiveDataFilter.maskSensitiveData(argString);
        }

        // Limit length to avoid huge logs
        if (argString.length() > 200) {
            return argString.substring(0, 200) + "...";
        }

        return argString;
    }
}
