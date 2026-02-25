package cn.qaiu.demo.aop.aspect;

import cn.qaiu.vx.core.aop.annotation.Aspect;
import cn.qaiu.vx.core.aop.annotation.Before;
import cn.qaiu.vx.core.aop.annotation.After;
import cn.qaiu.vx.core.aop.annotation.AfterThrowing;
import cn.qaiu.vx.core.aop.JoinPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 验证 docs/15-aop-guide.md 中的日志切面功能:
 * - @Aspect 标记切面类
 * - @Before 前置通知
 * - @After 后置通知
 * - @AfterThrowing 异常通知
 */
@Aspect
public class LoggingAspect {
    private static final Logger log = LoggerFactory.getLogger(LoggingAspect.class);

    @Before("execution(* cn.qaiu.demo.aop.service.*.*(..))")
    public void logBefore(JoinPoint jp) {
        log.info("[AOP-BEFORE] Method: {}, Args: {}", jp.getSignature(), jp.getArgs());
    }

    @After("execution(* cn.qaiu.demo.aop.service.*.*(..))")
    public void logAfter(JoinPoint jp) {
        log.info("[AOP-AFTER] Method: {} completed", jp.getSignature());
    }

    @AfterThrowing("execution(* cn.qaiu.demo.aop.service.*.*(..))")
    public void logException(JoinPoint jp) {
        log.error("[AOP-ERROR] Method: {} threw exception", jp.getSignature());
    }
}
