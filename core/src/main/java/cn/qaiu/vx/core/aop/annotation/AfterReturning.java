package cn.qaiu.vx.core.aop.annotation;

import java.lang.annotation.*;

/**
 * 返回后通知注解
 *
 * <p>在目标方法正常返回后执行通知方法。如果方法抛出异常，则不会执行此通知。 可以访问方法的返回值。
 *
 * <pre>
 * &#64;AfterReturning(value = "execution(* cn.qaiu.*.service.*.*(..))", returning = "result")
 * public void logAfterReturning(JoinPoint jp, Object result) {
 *     log.info("方法 {} 返回: {}", jp.getSignature(), result);
 * }
 * </pre>
 *
 * @author qaiu
 * @since 1.0.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AfterReturning {

  /** 切点表达式或切点方法引用 */
  String value();

  /**
   * 绑定返回值的参数名
   *
   * <p>如果指定，通知方法必须有一个同名参数来接收返回值
   */
  String returning() default "";
}
