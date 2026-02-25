package cn.qaiu.vx.core.aop.annotation;

import java.lang.annotation.*;

/**
 * 标记一个类为切面类
 *
 * <p>切面类中可以定义切点和通知方法，用于在目标方法执行前后插入横切逻辑。
 *
 * <pre>
 * &#64;Aspect
 * &#64;Order(10)
 * public class LoggingAspect {
 *     &#64;Before("execution(* cn.qaiu.*.service.*.*(..))")
 *     public void logBefore(JoinPoint jp) {
 *         // 前置逻辑
 *     }
 * }
 * </pre>
 *
 * @author qaiu
 * @since 1.0.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Aspect {

  /** 切面名称，默认为类名 */
  String value() default "";
}
