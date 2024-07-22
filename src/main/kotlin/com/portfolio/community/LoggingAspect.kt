package com.portfolio.community

import org.aspectj.lang.JoinPoint
import org.aspectj.lang.annotation.AfterReturning
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Before
import org.aspectj.lang.annotation.Pointcut
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Aspect
@Component
class LoggingAspect {
    private val logger = LoggerFactory.getLogger(LoggingAspect::class.java)

    @Pointcut("execution(* com.portfolio.community..*(..))")
    fun applicationPackagePointcut() {}

    @Before("applicationPackagePointcut()")
    fun logBeforeMethodExecution(joinPoint: JoinPoint) {
        logger.info("Before method: ${joinPoint.signature.name}")
    }

    @AfterReturning(pointcut = "applicationPackagePointcut()", returning = "result")
    fun logAfterMethodExecution(joinPoint: JoinPoint, result: Any?) {
        logger.info("After method: ${joinPoint.signature.name}, return value: $result")
    }
}