package cn.qaiu.vx.core.security;

import java.lang.annotation.*;

/**
 * 角色校验注解 用于标记需要特定角色才能访问的端点
 *
 * <p>使用示例：
 *
 * <pre>
 * &#64;RequiresRoles("admin")
 * public void adminOnly(RoutingContext ctx) { }
 *
 * &#64;RequiresRoles(value = {"admin", "manager"}, logical = Logical.OR)
 * public void adminOrManager(RoutingContext ctx) { }
 * </pre>
 *
 * @author <a href="https://qaiu.top">QAIU</a>
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequiresRoles {

  /**
   * 所需角色列表
   *
   * @return 角色名称数组
   */
  String[] value();

  /**
   * 多角色之间的逻辑关系
   *
   * @return 逻辑关系
   */
  RequiresPermissions.Logical logical() default RequiresPermissions.Logical.AND;
}
