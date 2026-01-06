package cn.qaiu.vx.core.aop.annotation;

import java.lang.annotation.*;

/**
 * 切点定义注解
 * <p>
 * 用于定义可重用的切点表达式，可以在其他通知注解中引用。
 * </p>
 *
 * <pre>
 * &#64;Aspect
 * public class ServiceAspect {
 *
 *     // 定义切点
 *     &#64;Pointcut("execution(* cn.qaiu.*.service.*.*(..))")
 *     public void serviceMethod() {}
 *
 *     // 引用切点
 *     &#64;Before("serviceMethod()")
 *     public void beforeService(JoinPoint jp) {
 *         // ...
 *     }
 *
 *     // 组合切点
 *     &#64;Pointcut("serviceMethod() && @annotation(cn.qaiu.vx.core.aop.Loggable)")
 *     public void loggableServiceMethod() {}
 * }
 * </pre>
 *
 * @author qaiu
 * @since 1.0.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Pointcut {
    
    /**
     * 切点表达式
     * <p>
     * 支持的表达式格式:
     * <ul>
     *   <li>execution(modifiers? return-type declaring-type? method-name(params) throws?)</li>
     *   <li>@annotation(annotation-type) - 匹配带有指定注解的方法</li>
     *   <li>within(type-pattern) - 匹配指定类型内的所有方法</li>
     *   <li>组合表达式: && (与), || (或), ! (非)</li>
     * </ul>
     * </p>
     */
    String value();
}
