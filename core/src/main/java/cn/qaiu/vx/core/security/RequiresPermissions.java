package cn.qaiu.vx.core.security;

import java.lang.annotation.*;

/**
 * 权限校验注解
 * 支持权限字符串格式：resource:action，支持通配符 *
 *
 * <p>权限字符串格式：
 * <ul>
 *   <li>{@code user:read} - 用户读取权限</li>
 *   <li>{@code user:write} - 用户写入权限</li>
 *   <li>{@code user:*} - 用户所有权限（通配符）</li>
 *   <li>{@code *:read} - 所有资源的读取权限</li>
 *   <li>{@code admin:*} - 管理员所有权限</li>
 * </ul>
 *
 * <p>使用示例：
 * <pre>
 * &#64;RequiresPermissions("user:read")
 * public void getUser(RoutingContext ctx) { }
 * 
 * &#64;RequiresPermissions(value = {"user:read", "user:write"}, logical = Logical.OR)
 * public void updateUser(RoutingContext ctx) { }
 * 
 * &#64;RequiresPermissions(value = {"admin:*"})
 * public void adminOperation(RoutingContext ctx) { }
 * </pre>
 *
 * @author <a href="https://qaiu.top">QAIU</a>
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequiresPermissions {
    
    /**
     * 所需权限列表
     * 格式：resource:action
     *
     * @return 权限字符串数组
     */
    String[] value();
    
    /**
     * 多权限之间的逻辑关系
     *
     * @return 逻辑关系
     */
    Logical logical() default Logical.AND;
    
    /**
     * 逻辑关系枚举
     */
    enum Logical {
        /**
         * 需要满足所有权限
         */
        AND,
        
        /**
         * 满足任一权限即可
         */
        OR
    }
}
