package cn.qaiu.vx.core.aop.annotation;

import java.lang.annotation.*;

/**
 * 前置通知注解
 * <p>
 * 在目标方法执行之前执行通知方法。如果前置通知抛出异常，目标方法将不会执行。
 * </p>
 *
 * <pre>
 * &#64;Before("execution(* cn.qaiu.*.service.*.*(..))")
 * public void logBefore(JoinPoint jp) {
 *     log.info("调用方法: {}", jp.getSignature());
 * }
 * </pre>
 *
 * @author qaiu
 * @since 1.0.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Before {
    
    /**
     * 切点表达式或切点方法引用
     * <p>
     * 支持的表达式格式:
     * <ul>
     *   <li>execution(modifiers? return-type declaring-type? method-name(params) throws?)</li>
     *   <li>@annotation(annotation-type) - 匹配带有指定注解的方法</li>
     *   <li>within(type-pattern) - 匹配指定类型内的所有方法</li>
     * </ul>
     * </p>
     */
    String value();
}
