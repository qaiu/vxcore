package cn.qaiu.demo.aop.aspect;

import cn.qaiu.vx.core.aop.JoinPoint;
import cn.qaiu.vx.core.aop.annotation.After;
import cn.qaiu.vx.core.aop.annotation.AfterThrowing;
import cn.qaiu.vx.core.aop.annotation.Aspect;
import cn.qaiu.vx.core.aop.annotation.Before;
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
public class ProcessAspect {
    private static final Logger log = LoggerFactory.getLogger(ProcessAspect.class);

    @Before("execution(* cn.qaiu.demo.aop.controller.AopDemoController.*(..))")
    public void logBefore(JoinPoint jp) {
        log.info("[CTRL-BEFORE] Method: {}, Args: {}", jp.getSignature(), jp.getArgs());
    }

    @After("execution(* cn.qaiu.demo.aop.controller.AopDemoController.*(..))")
    public void logAfter(JoinPoint jp) {
        log.info("[CTRL-AFTER] Method: {} completed", jp.getSignature());
    }

    @AfterThrowing("execution(* cn.qaiu.demo.aop.controller.AopDemoController.*(..))")
    public void logException(JoinPoint jp) {
        log.error("[CTRL-ERROR] Method: {} threw exception", jp.getSignature());
    }
}
