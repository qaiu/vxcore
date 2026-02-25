package cn.qaiu.vx.core.security;

import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import java.lang.reflect.Method;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 安全拦截器 实现JWT认证和权限校验
 *
 * @author <a href="https://qaiu.top">QAIU</a>
 */
public class SecurityInterceptor implements Handler<RoutingContext> {

  private static final Logger LOGGER = LoggerFactory.getLogger(SecurityInterceptor.class);

  private final JwtAuthProvider jwtAuthProvider;
  private final SecurityConfig config;
  private final Pattern[] authPathPatterns;
  private final Pattern[] ignorePathPatterns;

  public SecurityInterceptor(JwtAuthProvider jwtAuthProvider, SecurityConfig config) {
    this.jwtAuthProvider = jwtAuthProvider;
    this.config = config;
    this.authPathPatterns = compilePatterns(config.getAuthPaths());
    this.ignorePathPatterns = compilePatterns(config.getIgnorePaths());
  }

  /**
   * 从JsonObject配置创建SecurityInterceptor
   *
   * @param securityConfig 安全配置JsonObject
   */
  public SecurityInterceptor(JsonObject securityConfig) {
    this.config = new SecurityConfig(securityConfig);
    this.jwtAuthProvider = new JwtAuthProvider(this.config);
    this.authPathPatterns = compilePatterns(config.getAuthPaths());
    this.ignorePathPatterns = compilePatterns(config.getIgnorePaths());
  }

  /** 编译正则表达式数组 */
  private Pattern[] compilePatterns(String[] patterns) {
    if (patterns == null || patterns.length == 0) {
      return new Pattern[0];
    }

    Pattern[] compiled = new Pattern[patterns.length];
    for (int i = 0; i < patterns.length; i++) {
      compiled[i] = Pattern.compile(patterns[i]);
    }
    return compiled;
  }

  @Override
  public void handle(RoutingContext ctx) {
    String path = ctx.request().path();

    // 检查是否在忽略路径列表中
    if (isPathIgnored(path)) {
      LOGGER.debug("Path '{}' is in ignore list, skipping authentication", path);
      ctx.next();
      return;
    }

    // 检查是否需要认证（基于配置的路径正则）
    boolean requiresAuth = isPathRequiresAuth(path);

    // 检查是否有方法/类级别的安全注解
    SecurityRequirement requirement = getSecurityRequirement(ctx);

    // 如果标记了 @Anonymous，跳过认证
    if (requirement.isAnonymous()) {
      LOGGER.debug("Path '{}' is marked as anonymous, skipping authentication", path);
      ctx.next();
      return;
    }

    // 如果需要认证或有安全注解
    if (requiresAuth || requirement.requiresAuthentication()) {
      performAuthentication(ctx, requirement);
    } else {
      // 尝试可选认证（如果有Token则解析，没有也继续）
      performOptionalAuthentication(ctx);
    }
  }

  /** 执行认证 */
  @SuppressWarnings("deprecation") // setUser is deprecated but UserContext doesn't provide a setter
  private void performAuthentication(RoutingContext ctx, SecurityRequirement requirement) {
    String token = extractToken(ctx.request());

    if (token == null || token.isEmpty()) {
      // 检查是否为可选认证
      if (requirement.isOptional()) {
        ctx.next();
        return;
      }

      LOGGER.debug("No token found for path: {}", ctx.request().path());
      sendUnauthorized(ctx, "Missing authentication token");
      return;
    }

    jwtAuthProvider
        .authenticate(token)
        .onSuccess(
            user -> {
              // 创建安全上下文
              SecurityContext securityContext = new SecurityContext(user);
              ctx.put(SecurityContext.getContextKey(), securityContext);
              ctx.setUser(user);

              // 检查权限和角色
              if (!checkPermissions(securityContext, requirement)) {
                LOGGER.debug("Permission denied for user: {}", securityContext.getUserId());
                sendForbidden(ctx, "Insufficient permissions");
                return;
              }

              if (!checkRoles(securityContext, requirement)) {
                LOGGER.debug("Role check failed for user: {}", securityContext.getUserId());
                sendForbidden(ctx, "Insufficient roles");
                return;
              }

              LOGGER.debug("Authentication successful for user: {}", securityContext.getUserId());
              ctx.next();
            })
        .onFailure(
            err -> {
              LOGGER.debug("Authentication failed: {}", err.getMessage());
              sendUnauthorized(ctx, "Invalid or expired token");
            });
  }

  /** 执行可选认证 */
  @SuppressWarnings("deprecation") // setUser is deprecated but UserContext doesn't provide a setter
  private void performOptionalAuthentication(RoutingContext ctx) {
    String token = extractToken(ctx.request());

    if (token == null || token.isEmpty()) {
      ctx.next();
      return;
    }

    jwtAuthProvider
        .authenticate(token)
        .onSuccess(
            user -> {
              SecurityContext securityContext = new SecurityContext(user);
              ctx.put(SecurityContext.getContextKey(), securityContext);
              ctx.setUser(user);
              ctx.next();
            })
        .onFailure(
            err -> {
              // 可选认证失败不阻止请求
              LOGGER.debug("Optional authentication failed: {}", err.getMessage());
              ctx.next();
            });
  }

  /** 从请求中提取Token */
  private String extractToken(HttpServerRequest request) {
    String header = request.getHeader(config.getTokenHeader());

    if (header == null || header.isEmpty()) {
      // 尝试从查询参数获取
      return request.getParam("access_token");
    }

    String prefix = config.getTokenPrefix();
    if (header.startsWith(prefix)) {
      return header.substring(prefix.length());
    }

    return header;
  }

  /** 检查路径是否需要认证 */
  private boolean isPathRequiresAuth(String path) {
    for (Pattern pattern : authPathPatterns) {
      if (pattern.matcher(path).matches()) {
        return true;
      }
    }
    return false;
  }

  /** 检查路径是否在忽略列表中 */
  private boolean isPathIgnored(String path) {
    for (Pattern pattern : ignorePathPatterns) {
      if (pattern.matcher(path).matches()) {
        return true;
      }
    }
    return false;
  }

  /** 获取安全要求（从注解） */
  private SecurityRequirement getSecurityRequirement(RoutingContext ctx) {
    SecurityRequirement requirement = new SecurityRequirement();

    // 从路由数据中获取处理方法信息
    Object handlerMethod = ctx.get("_handler_method");
    Object handlerClass = ctx.get("_handler_class");

    if (handlerMethod instanceof Method) {
      Method method = (Method) handlerMethod;

      // 检查 @Anonymous
      if (method.isAnnotationPresent(Anonymous.class)) {
        requirement.setAnonymous(true);
        return requirement;
      }

      // 检查 @Authenticated
      Authenticated authenticated = method.getAnnotation(Authenticated.class);
      if (authenticated != null) {
        requirement.setRequiresAuthentication(true);
        requirement.setOptional(authenticated.optional());
      }

      // 检查 @RequiresPermissions
      RequiresPermissions permissions = method.getAnnotation(RequiresPermissions.class);
      if (permissions != null) {
        requirement.setRequiresAuthentication(true);
        requirement.setRequiredPermissions(permissions.value());
        requirement.setPermissionLogical(permissions.logical());
      }

      // 检查 @RequiresRoles
      RequiresRoles roles = method.getAnnotation(RequiresRoles.class);
      if (roles != null) {
        requirement.setRequiresAuthentication(true);
        requirement.setRequiredRoles(roles.value());
        requirement.setRoleLogical(roles.logical());
      }
    }

    // 检查类级别注解
    if (handlerClass instanceof Class<?>) {
      Class<?> clazz = (Class<?>) handlerClass;

      // 如果方法没有 @Anonymous，检查类级别
      if (!requirement.isAnonymous() && clazz.isAnnotationPresent(Anonymous.class)) {
        requirement.setAnonymous(true);
        return requirement;
      }

      // 类级别 @Authenticated
      if (!requirement.requiresAuthentication()) {
        Authenticated authenticated = clazz.getAnnotation(Authenticated.class);
        if (authenticated != null) {
          requirement.setRequiresAuthentication(true);
          requirement.setOptional(authenticated.optional());
        }
      }

      // 类级别权限（如果方法级别没有设置）
      if (requirement.getRequiredPermissions() == null
          || requirement.getRequiredPermissions().length == 0) {
        RequiresPermissions permissions = clazz.getAnnotation(RequiresPermissions.class);
        if (permissions != null) {
          requirement.setRequiresAuthentication(true);
          requirement.setRequiredPermissions(permissions.value());
          requirement.setPermissionLogical(permissions.logical());
        }
      }

      // 类级别角色
      if (requirement.getRequiredRoles() == null || requirement.getRequiredRoles().length == 0) {
        RequiresRoles roles = clazz.getAnnotation(RequiresRoles.class);
        if (roles != null) {
          requirement.setRequiresAuthentication(true);
          requirement.setRequiredRoles(roles.value());
          requirement.setRoleLogical(roles.logical());
        }
      }
    }

    return requirement;
  }

  /** 检查权限 */
  private boolean checkPermissions(SecurityContext context, SecurityRequirement requirement) {
    String[] required = requirement.getRequiredPermissions();
    if (required == null || required.length == 0) {
      return true;
    }

    if (requirement.getPermissionLogical() == RequiresPermissions.Logical.OR) {
      return context.hasAnyPermission(required);
    } else {
      return context.hasAllPermissions(required);
    }
  }

  /** 检查角色 */
  private boolean checkRoles(SecurityContext context, SecurityRequirement requirement) {
    String[] required = requirement.getRequiredRoles();
    if (required == null || required.length == 0) {
      return true;
    }

    if (requirement.getRoleLogical() == RequiresPermissions.Logical.OR) {
      return context.hasAnyRole(required);
    } else {
      return context.hasAllRoles(required);
    }
  }

  /** 发送401未授权响应 */
  private void sendUnauthorized(RoutingContext ctx, String message) {
    ctx.response()
        .setStatusCode(401)
        .putHeader("Content-Type", "application/json")
        .putHeader("WWW-Authenticate", "Bearer")
        .end("{\"code\":401,\"message\":\"" + message + "\"}");
  }

  /** 发送403禁止访问响应 */
  private void sendForbidden(RoutingContext ctx, String message) {
    ctx.response()
        .setStatusCode(403)
        .putHeader("Content-Type", "application/json")
        .end("{\"code\":403,\"message\":\"" + message + "\"}");
  }

  /** 安全要求内部类 */
  private static class SecurityRequirement {
    private boolean anonymous = false;
    private boolean requiresAuthentication = false;
    private boolean optional = false;
    private String[] requiredPermissions;
    private String[] requiredRoles;
    private RequiresPermissions.Logical permissionLogical = RequiresPermissions.Logical.AND;
    private RequiresPermissions.Logical roleLogical = RequiresPermissions.Logical.AND;

    // Getters and Setters
    public boolean isAnonymous() {
      return anonymous;
    }

    public void setAnonymous(boolean anonymous) {
      this.anonymous = anonymous;
    }

    public boolean requiresAuthentication() {
      return requiresAuthentication;
    }

    public void setRequiresAuthentication(boolean requiresAuthentication) {
      this.requiresAuthentication = requiresAuthentication;
    }

    public boolean isOptional() {
      return optional;
    }

    public void setOptional(boolean optional) {
      this.optional = optional;
    }

    public String[] getRequiredPermissions() {
      return requiredPermissions;
    }

    public void setRequiredPermissions(String[] requiredPermissions) {
      this.requiredPermissions = requiredPermissions;
    }

    public String[] getRequiredRoles() {
      return requiredRoles;
    }

    public void setRequiredRoles(String[] requiredRoles) {
      this.requiredRoles = requiredRoles;
    }

    public RequiresPermissions.Logical getPermissionLogical() {
      return permissionLogical;
    }

    public void setPermissionLogical(RequiresPermissions.Logical permissionLogical) {
      this.permissionLogical = permissionLogical;
    }

    public RequiresPermissions.Logical getRoleLogical() {
      return roleLogical;
    }

    public void setRoleLogical(RequiresPermissions.Logical roleLogical) {
      this.roleLogical = roleLogical;
    }
  }
}
