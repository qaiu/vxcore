package cn.qaiu.vx.core.aop.annotation;

import java.lang.annotation.*;

/**
 * 后置通知注解（最终通知）
 * <p>
 * 在目标方法执行完成后执行通知方法，无论方法是正常返回还是抛出异常都会执行。
 * 类似于 try-finally 中的 finally 块。
 * </p>
 *
 * <pre>
 * &#64;After("execution(* cn.qaiu.*.service.*.*(..))")
 * public void logAfter(JoinPoint jp) {
 *     log.info("方法执行完成: {}", jp.getSignature());
 * }
 * </pre>
 *
 * @author qaiu
 * @since 1.0.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface After {
    
    /**
     * 切点表达式或切点方法引用
     */
    String value();
}
