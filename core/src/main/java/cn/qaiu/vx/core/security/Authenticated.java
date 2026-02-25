package cn.qaiu.vx.core.security;

import java.lang.annotation.*;

/**
 * 标记需要认证的方法或类 被标记的端点需要有效的JWT Token才能访问
 *
 * <p>使用示例：
 *
 * <pre>
 * &#64;RouteMapping("/user")
 * &#64;Authenticated
 * public class UserController {
 *     // 所有方法都需要认证
 * }
 *
 * &#64;RouteHandler(method = HttpMethod.GET)
 * &#64;Authenticated
 * public void getProfile(RoutingContext ctx) {
 *     // 只有这个方法需要认证
 * }
 * </pre>
 *
 * @author <a href="https://qaiu.top">QAIU</a>
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Authenticated {

  /**
   * 是否为可选认证 如果为true，未认证的请求也会被允许访问，但不会设置用户上下文
   *
   * @return 是否可选
   */
  boolean optional() default false;
}
