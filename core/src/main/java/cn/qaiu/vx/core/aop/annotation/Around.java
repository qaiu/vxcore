package cn.qaiu.vx.core.aop.annotation;

import java.lang.annotation.*;

/**
 * 环绕通知注解
 *
 * <p>环绕通知是最强大的通知类型，可以完全控制目标方法的执行。 通知方法必须调用 {@code ProceedingJoinPoint.proceed()} 来执行目标方法。
 *
 * <p>支持异步方法（返回 {@code Future<T>} 的方法），会自动处理异步结果。
 *
 * <pre>
 * &#64;Around("execution(* cn.qaiu.*.service.*.*(..))")
 * public Object around(ProceedingJoinPoint pjp) throws Throwable {
 *     long start = System.currentTimeMillis();
 *     try {
 *         Object result = pjp.proceed();
 *         // 如果是Future，需要特殊处理
 *         if (result instanceof Future) {
 *             return ((Future<?>) result).map(r -> {
 *                 log.info("异步方法执行耗时: {}ms", System.currentTimeMillis() - start);
 *                 return r;
 *             });
 *         }
 *         log.info("方法执行耗时: {}ms", System.currentTimeMillis() - start);
 *         return result;
 *     } catch (Throwable e) {
 *         log.error("方法执行异常", e);
 *         throw e;
 *     }
 * }
 * </pre>
 *
 * @author qaiu
 * @since 1.0.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Around {

  /** 切点表达式或切点方法引用 */
  String value();
}
