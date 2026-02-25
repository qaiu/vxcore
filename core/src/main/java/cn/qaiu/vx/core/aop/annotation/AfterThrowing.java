package cn.qaiu.vx.core.aop.annotation;

import java.lang.annotation.*;

/**
 * 异常后通知注解
 *
 * <p>在目标方法抛出异常后执行通知方法。如果方法正常返回，则不会执行此通知。 可以访问抛出的异常对象。
 *
 * <pre>
 * &#64;AfterThrowing(value = "execution(* cn.qaiu.*.service.*.*(..))", throwing = "ex")
 * public void logAfterThrowing(JoinPoint jp, Throwable ex) {
 *     log.error("方法 {} 抛出异常: {}", jp.getSignature(), ex.getMessage());
 * }
 * </pre>
 *
 * @author qaiu
 * @since 1.0.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AfterThrowing {

  /** 切点表达式或切点方法引用 */
  String value();

  /**
   * 绑定异常对象的参数名
   *
   * <p>如果指定，通知方法必须有一个同名参数来接收异常对象
   */
  String throwing() default "";
}
