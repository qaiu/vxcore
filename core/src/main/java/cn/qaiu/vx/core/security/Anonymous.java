package cn.qaiu.vx.core.security;

import java.lang.annotation.*;

/**
 * 匿名访问注解
 * 标记允许未认证用户访问的端点
 * 
 * <p>当类级别有 {@link Authenticated} 注解时，可以用此注解放行特定方法
 *
 * <p>使用示例：
 * <pre>
 * &#64;RouteMapping("/api")
 * &#64;Authenticated  // 类级别需要认证
 * public class ApiController {
 *     
 *     &#64;Anonymous  // 此方法允许匿名访问
 *     &#64;RouteHandler(path = "/public", method = HttpMethod.GET)
 *     public void publicEndpoint(RoutingContext ctx) { }
 *     
 *     // 此方法需要认证
 *     &#64;RouteHandler(path = "/private", method = HttpMethod.GET)
 *     public void privateEndpoint(RoutingContext ctx) { }
 * }
 * </pre>
 *
 * @author <a href="https://qaiu.top">QAIU</a>
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Anonymous {
}
