# 安全认证框架

VXCore 提供了完整的声明式安全认证框架，基于 JWT（JSON Web Token）实现，支持灵活的权限控制。

## 📋 目录

- [快速开始](#快速开始)
- [配置说明](#配置说明)
- [注解使用](#注解使用)
- [JWT 认证](#jwt-认证)
- [权限控制](#权限控制)
- [Token 黑名单](#token-黑名单)
- [高级用法](#高级用法)
- [最佳实践](#最佳实践)

## 快速开始

### 1. 启用安全认证

在配置文件 `application.yml` 中启用 JWT 认证：

```yaml
security:
  jwt-enable: true
  jwt-algorithm: RS256
  jwt-expire-seconds: 3600
  jwt-issuer: vxcore
  
  # 需要认证的路径（正则表达式）
  jwt-auth-reg:
    - "/api/.*"
    - "/admin/.*"
  
  # 忽略认证的路径（正则表达式）
  jwt-ignores-reg:
    - "/api/auth/.*"
    - "/api/public/.*"
```

### 2. 使用安全注解

```java
@RouteHandler("/api/users")
public class UserController {

    @Authenticated  // 需要认证
    @GetRoute("/profile")
    public Future<User> getProfile(RoutingContext ctx) {
        SecurityContext security = SecurityContext.from(ctx);
        Long userId = security.getUserId();
        return userService.findById(userId);
    }

    @RequiresRoles({"admin"})  // 需要 admin 角色
    @GetRoute("/all")
    public Future<List<User>> getAllUsers() {
        return userService.findAll();
    }

    @Anonymous  // 允许匿名访问
    @PostRoute("/register")
    public Future<User> register(@RequestBody UserDTO dto) {
        return userService.create(dto);
    }
}
```

## 配置说明

### 完整配置选项

```yaml
security:
  # JWT 基本配置
  jwt-enable: true                    # 是否启用 JWT 认证
  jwt-algorithm: RS256                # 算法：HS256（对称）或 RS256（非对称）
  jwt-expire-seconds: 3600            # Token 过期时间（秒）
  refresh-token-expire-seconds: 604800 # 刷新 Token 过期时间（7天）
  jwt-issuer: vxcore                  # Token 签发者

  # 密钥配置（二选一）
  # 方式1：对称加密（HS256）
  jwt-secret: your-256-bit-secret-key-here
  
  # 方式2：非对称加密（RS256，推荐）
  jwt-public-key: /path/to/public.pem
  jwt-private-key: /path/to/private.pem

  # Token 传输配置
  token-header: Authorization         # Token 请求头名称
  token-prefix: "Bearer "             # Token 前缀

  # 路径配置（正则表达式）
  jwt-auth-reg:                       # 需要认证的路径
    - "/api/.*"
  jwt-ignores-reg:                    # 忽略认证的路径
    - "/api/auth/.*"
    - "/api/public/.*"
    - "/health"

  # 防盗链配置
  ref-enable: false                   # 是否启用防盗链
```

### 生成 RSA 密钥对

```bash
# 生成私钥
openssl genrsa -out private.pem 2048

# 从私钥生成公钥
openssl rsa -in private.pem -pubout -out public.pem
```

## 注解使用

### @Authenticated

标记需要认证的方法或控制器：

```java
// 方法级别
@Authenticated
@GetRoute("/profile")
public Future<User> getProfile() { ... }

// 类级别（所有方法都需要认证）
@Authenticated
@RouteHandler("/api/admin")
public class AdminController { ... }
```

### @Anonymous

标记允许匿名访问的方法：

```java
@Authenticated  // 类级别需要认证
@RouteHandler("/api/users")
public class UserController {

    @Anonymous  // 此方法允许匿名访问
    @PostRoute("/register")
    public Future<User> register() { ... }
}
```

### @RequiresRoles

要求用户具有指定角色：

```java
// 需要 admin 角色
@RequiresRoles({"admin"})
@GetRoute("/admin/users")
public Future<List<User>> getAllUsers() { ... }

// 需要 admin 或 manager 角色（逻辑或）
@RequiresRoles(value = {"admin", "manager"}, logical = Logical.OR)
@GetRoute("/reports")
public Future<Report> getReport() { ... }

// 需要同时具有 admin 和 super 角色（逻辑与）
@RequiresRoles(value = {"admin", "super"}, logical = Logical.AND)
@DeleteRoute("/system/reset")
public Future<Void> resetSystem() { ... }
```

### @RequiresPermissions

要求用户具有指定权限：

```java
// 需要 user:read 权限
@RequiresPermissions({"user:read"})
@GetRoute("/users/{id}")
public Future<User> getUser(@PathParam Long id) { ... }

// 需要 user:read 和 user:write 权限
@RequiresPermissions(value = {"user:read", "user:write"}, logical = Logical.AND)
@PutRoute("/users/{id}")
public Future<User> updateUser(@PathParam Long id, @RequestBody UserDTO dto) { ... }
```

## JWT 认证

### 生成 Token

```java
@RouteHandler("/api/auth")
public class AuthController {

    @Inject
    private JwtAuthProvider jwtAuthProvider;

    @Anonymous
    @PostRoute("/login")
    public Future<JsonObject> login(@RequestBody LoginDTO dto) {
        return userService.authenticate(dto.getUsername(), dto.getPassword())
            .compose(user -> {
                // 构建 Token claims
                JsonObject claims = new JsonObject()
                    .put("sub", user.getId().toString())
                    .put("username", user.getUsername())
                    .put("roles", user.getRoles())
                    .put("permissions", user.getPermissions());

                // 生成 Token
                String accessToken = jwtAuthProvider.generateToken(claims);
                String refreshToken = jwtAuthProvider.generateRefreshToken(claims);

                return Future.succeededFuture(new JsonObject()
                    .put("access_token", accessToken)
                    .put("refresh_token", refreshToken)
                    .put("expires_in", 3600));
            });
    }

    @Anonymous
    @PostRoute("/refresh")
    public Future<JsonObject> refresh(@RequestBody JsonObject body) {
        String refreshToken = body.getString("refresh_token");
        return jwtAuthProvider.verifyToken(refreshToken)
            .compose(user -> {
                // 生成新的 access token
                JsonObject claims = user.principal();
                String newAccessToken = jwtAuthProvider.generateToken(claims);
                
                return Future.succeededFuture(new JsonObject()
                    .put("access_token", newAccessToken)
                    .put("expires_in", 3600));
            });
    }
}
```

### 获取当前用户信息

```java
@Authenticated
@GetRoute("/me")
public Future<JsonObject> getCurrentUser(RoutingContext ctx) {
    // 方式1：使用 SecurityContext
    SecurityContext security = SecurityContext.from(ctx);
    Long userId = security.getUserId();
    String username = security.getUsername();
    Set<String> roles = security.getRoles();
    Set<String> permissions = security.getPermissions();

    // 方式2：直接获取 User 对象
    User user = ctx.user();
    JsonObject principal = user.principal();

    return Future.succeededFuture(new JsonObject()
        .put("id", userId)
        .put("username", username)
        .put("roles", roles));
}
```

## 权限控制

### 编程式权限检查

```java
@Authenticated
@GetRoute("/resources/{id}")
public Future<Resource> getResource(@PathParam Long id, RoutingContext ctx) {
    SecurityContext security = SecurityContext.from(ctx);
    
    // 检查角色
    if (security.hasRole("admin")) {
        return resourceService.findByIdWithDetails(id);
    }
    
    // 检查权限
    if (security.hasPermission("resource:read")) {
        return resourceService.findById(id);
    }
    
    // 检查资源所有权
    return resourceService.findById(id)
        .compose(resource -> {
            if (resource.getOwnerId().equals(security.getUserId())) {
                return Future.succeededFuture(resource);
            }
            return Future.failedFuture(new ForbiddenException("无权访问此资源"));
        });
}
```

### 自定义权限验证器

```java
public class CustomSecurityInterceptor extends SecurityInterceptor {

    @Override
    protected boolean checkCustomPermission(RoutingContext ctx, Method method) {
        // 自定义权限验证逻辑
        CustomAuth auth = method.getAnnotation(CustomAuth.class);
        if (auth != null) {
            SecurityContext security = SecurityContext.from(ctx);
            return customAuthService.check(security, auth.value());
        }
        return true;
    }
}
```

## Token 黑名单

支持 Token 注销和黑名单管理：

```java
@RouteHandler("/api/auth")
public class AuthController {

    @Inject
    private TokenBlacklist tokenBlacklist;

    @Authenticated
    @PostRoute("/logout")
    public Future<Void> logout(RoutingContext ctx) {
        String token = extractToken(ctx);
        
        // 将 Token 加入黑名单
        return tokenBlacklist.add(token)
            .map(v -> {
                ctx.response()
                    .setStatusCode(204)
                    .end();
                return null;
            });
    }

    @Authenticated
    @PostRoute("/logout-all")
    public Future<Void> logoutAll(RoutingContext ctx) {
        SecurityContext security = SecurityContext.from(ctx);
        Long userId = security.getUserId();
        
        // 将用户所有 Token 加入黑名单
        return tokenBlacklist.addAllForUser(userId);
    }
}
```

## 高级用法

### 自定义 Token 提取

```java
public class CustomSecurityConfig extends SecurityConfig {

    @Override
    public String extractToken(HttpServerRequest request) {
        // 优先从 Header 获取
        String token = request.getHeader(getTokenHeader());
        if (token != null && token.startsWith(getTokenPrefix())) {
            return token.substring(getTokenPrefix().length());
        }
        
        // 其次从 Cookie 获取
        Cookie cookie = request.getCookie("access_token");
        if (cookie != null) {
            return cookie.getValue();
        }
        
        // 最后从 Query 参数获取
        return request.getParam("token");
    }
}
```

### 多租户认证

```java
@Aspect
@Order(5)
public class TenantAspect {

    @Before("@annotation(cn.qaiu.vx.core.security.Authenticated)")
    public void checkTenant(JoinPoint jp) {
        RoutingContext ctx = findRoutingContext(jp.getArgs());
        SecurityContext security = SecurityContext.from(ctx);
        
        String tenantId = ctx.request().getHeader("X-Tenant-ID");
        if (!security.getTenantId().equals(tenantId)) {
            throw new ForbiddenException("租户不匹配");
        }
    }
}
```

## 最佳实践

### 1. 使用非对称加密

推荐在生产环境使用 RS256 算法：

```yaml
security:
  jwt-algorithm: RS256
  jwt-public-key: /etc/secrets/jwt/public.pem
  jwt-private-key: /etc/secrets/jwt/private.pem
```

### 2. 合理设置过期时间

```yaml
security:
  jwt-expire-seconds: 900        # Access Token: 15分钟
  refresh-token-expire-seconds: 604800  # Refresh Token: 7天
```

### 3. 敏感操作二次验证

```java
@Authenticated
@RequiresPermissions({"user:delete"})
@DeleteRoute("/users/{id}")
public Future<Void> deleteUser(
        @PathParam Long id,
        @HeaderParam("X-Confirm-Password") String password,
        RoutingContext ctx) {
    
    SecurityContext security = SecurityContext.from(ctx);
    
    // 二次密码验证
    return userService.verifyPassword(security.getUserId(), password)
        .compose(valid -> {
            if (!valid) {
                return Future.failedFuture(new UnauthorizedException("密码错误"));
            }
            return userService.delete(id);
        });
}
```

### 4. 日志审计

```java
@Aspect
@Order(50)
public class SecurityAuditAspect {

    private static final Logger AUDIT_LOG = LoggerFactory.getLogger("SECURITY_AUDIT");

    @After("@annotation(cn.qaiu.vx.core.security.RequiresRoles) || " +
           "@annotation(cn.qaiu.vx.core.security.RequiresPermissions)")
    public void auditSecurityAction(JoinPoint jp) {
        // 记录安全审计日志
        AUDIT_LOG.info("Security action: method={}, user={}, result=success",
            jp.getSignature(), SecurityContext.current().getUsername());
    }
}
```

## 错误响应

安全认证失败时的标准响应格式：

```json
// 401 Unauthorized - 未认证
{
  "code": 401,
  "message": "Token 无效或已过期",
  "timestamp": "2025-01-06T10:00:00Z"
}

// 403 Forbidden - 无权限
{
  "code": 403,
  "message": "没有访问此资源的权限",
  "timestamp": "2025-01-06T10:00:00Z"
}
```

## 相关文档

- [配置管理](10-configuration.md) - 了解更多配置选项
- [异常处理](09-exception-handling.md) - 安全异常处理
- [AOP 指南](15-aop-guide.md) - 使用 AOP 实现安全切面
