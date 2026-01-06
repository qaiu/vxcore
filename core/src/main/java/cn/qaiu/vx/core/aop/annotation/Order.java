package cn.qaiu.vx.core.aop.annotation;

import java.lang.annotation.*;

/**
 * 切面优先级注解
 * <p>
 * 用于指定切面的执行顺序。数值越小优先级越高，越先执行。
 * 默认优先级为 {@link Integer#MAX_VALUE}。
 * </p>
 *
 * <p>
 * 对于环绕通知，优先级高的切面在外层包裹，优先级低的切面在内层。
 * 即：高优先级的前置逻辑先执行，后置逻辑后执行。
 * </p>
 *
 * <pre>
 * &#64;Aspect
 * &#64;Order(1)  // 最先执行
 * public class SecurityAspect { ... }
 *
 * &#64;Aspect
 * &#64;Order(10)  // 其次执行
 * public class LoggingAspect { ... }
 *
 * &#64;Aspect
 * &#64;Order(100)  // 最后执行
 * public class MetricsAspect { ... }
 * </pre>
 *
 * @author qaiu
 * @since 1.0.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Order {
    
    /**
     * 优先级值，数值越小优先级越高
     */
    int value() default Integer.MAX_VALUE;
}
