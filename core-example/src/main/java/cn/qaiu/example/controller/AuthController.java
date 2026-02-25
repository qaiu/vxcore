package cn.qaiu.example.controller;

import cn.qaiu.vx.core.annotations.RouteHandler;
import cn.qaiu.vx.core.annotations.RouteMapping;
import cn.qaiu.vx.core.enums.RouteMethod;
import cn.qaiu.vx.core.model.JsonResult;
import cn.qaiu.vx.core.security.Anonymous;
import cn.qaiu.vx.core.security.Authenticated;
import cn.qaiu.vx.core.security.JwtAuthProvider;
import cn.qaiu.vx.core.security.SecurityConfig;
import cn.qaiu.vx.core.security.SecurityContext;
import cn.qaiu.vx.core.util.SharedDataUtil;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 认证控制器
 * 演示登录、登出、刷新Token等认证功能
 *
 * @author <a href="https://qaiu.top">QAIU</a>
 */
@RouteHandler("/api/auth")
public class AuthController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthController.class);

    // 模拟用户数据库
    private static final Map<String, JsonObject> USERS = new ConcurrentHashMap<>() {{
        put("admin", new JsonObject()
                .put("userId", "1")
                .put("username", "admin")
                .put("password", "admin123")
                .put("roles", "admin,user")
                .put("permissions", "user:*,order:read,system:admin"));
        put("user", new JsonObject()
                .put("userId", "2")
                .put("username", "user")
                .put("password", "user123")
                .put("roles", "user")
                .put("permissions", "user:read,order:read"));
        put("guest", new JsonObject()
                .put("userId", "3")
                .put("username", "guest")
                .put("password", "guest123")
                .put("roles", "guest")
                .put("permissions", "public:read"));
    }};

    private JwtAuthProvider jwtAuthProvider;

    /**
     * 获取或初始化JwtAuthProvider
     */
    private JwtAuthProvider getJwtAuthProvider() {
        if (jwtAuthProvider == null) {
            JsonObject globalConfig = SharedDataUtil.getGlobalConfig();
            JsonObject securityConfig = globalConfig != null ? 
                    globalConfig.getJsonObject("security") : null;
            if (securityConfig != null) {
                SecurityConfig config = new SecurityConfig(securityConfig);
                this.jwtAuthProvider = new JwtAuthProvider(config);
            } else {
                // 使用默认配置
                this.jwtAuthProvider = new JwtAuthProvider(new SecurityConfig());
            }
        }
        return jwtAuthProvider;
    }

    /**
     * 用户登录
     * POST /api/auth/login
     * 
     * @param username 用户名
     * @param password 密码
     * @return JWT Token
     */
    @Anonymous
    @RouteMapping(value = "/login", method = RouteMethod.POST)
    public Future<JsonResult<JsonObject>> login(String username, String password) {
        LOGGER.info("User login attempt: {}", username);

        JsonObject user = USERS.get(username);
        if (user == null || !user.getString("password").equals(password)) {
            LOGGER.warn("Login failed for user: {}", username);
            return Future.succeededFuture(JsonResult.error("用户名或密码错误", 401));
        }

        try {
            JwtAuthProvider provider = getJwtAuthProvider();
            
            // 准备用户信息用于Token生成
            JsonObject userInfo = new JsonObject()
                    .put("sub", user.getString("userId"))
                    .put("username", user.getString("username"))
                    .put("roles", user.getString("roles"))
                    .put("permissions", user.getString("permissions"));

            // 生成访问Token和刷新Token
            String accessToken = provider.generateToken(userInfo);
            String refreshToken = provider.generateRefreshToken(userInfo);

            JsonObject result = new JsonObject()
                    .put("accessToken", accessToken)
                    .put("refreshToken", refreshToken)
                    .put("tokenType", "Bearer")
                    .put("expiresIn", 3600)
                    .put("user", new JsonObject()
                            .put("userId", user.getString("userId"))
                            .put("username", user.getString("username"))
                            .put("roles", user.getString("roles").split(",")));

            LOGGER.info("User logged in successfully: {}", username);
            return Future.succeededFuture(JsonResult.data(result));
        } catch (Exception e) {
            LOGGER.error("Token generation failed", e);
            return Future.succeededFuture(JsonResult.error("登录失败: " + e.getMessage(), 500));
        }
    }

    /**
     * 刷新Token
     * POST /api/auth/refresh
     *
     * @param refreshToken 刷新Token
     * @return 新的JWT Token
     */
    @Anonymous
    @RouteMapping(value = "/refresh", method = RouteMethod.POST)
    public Future<JsonResult<JsonObject>> refreshToken(String refreshToken) {
        LOGGER.info("Token refresh attempt");

        try {
            JwtAuthProvider provider = getJwtAuthProvider();
            
            return provider.authenticate(refreshToken)
                    .compose(user -> {
                        // 从原Token获取用户信息
                        JsonObject principal = user.principal();
                        
                        // 生成新Token
                        String newAccessToken = provider.generateToken(principal);
                        String newRefreshToken = provider.generateRefreshToken(principal);

                        JsonObject result = new JsonObject()
                                .put("accessToken", newAccessToken)
                                .put("refreshToken", newRefreshToken)
                                .put("tokenType", "Bearer")
                                .put("expiresIn", 3600);

                        LOGGER.info("Token refreshed successfully for user: {}", principal.getString("sub"));
                        return Future.succeededFuture(JsonResult.data(result));
                    })
                    .recover(err -> {
                        LOGGER.warn("Token refresh failed: {}", err.getMessage());
                        return Future.succeededFuture(JsonResult.error("刷新Token无效或已过期", 401));
                    });
        } catch (Exception e) {
            LOGGER.error("Token refresh failed", e);
            return Future.succeededFuture(JsonResult.error("Token刷新失败: " + e.getMessage(), 500));
        }
    }

    /**
     * 获取当前用户信息
     * GET /api/auth/me
     * 
     * @param ctx 路由上下文
     * @return 当前用户信息
     */
    @Authenticated
    @RouteMapping(value = "/me", method = RouteMethod.GET)
    public Future<JsonResult<JsonObject>> getCurrentUser(RoutingContext ctx) {
        SecurityContext securityContext = SecurityContext.fromContext(ctx);
        
        if (securityContext == null) {
            return Future.succeededFuture(JsonResult.error("未认证", 401));
        }

        JsonObject userInfo = new JsonObject()
                .put("userId", securityContext.getUserId())
                .put("username", securityContext.getUsername())
                .put("roles", securityContext.getRoles())
                .put("permissions", securityContext.getPermissions());

        return Future.succeededFuture(JsonResult.data(userInfo));
    }

    /**
     * 用户登出
     * POST /api/auth/logout
     */
    @Authenticated
    @RouteMapping(value = "/logout", method = RouteMethod.POST)
    public Future<JsonResult<String>> logout(RoutingContext ctx) {
        SecurityContext securityContext = SecurityContext.fromContext(ctx);
        
        if (securityContext != null) {
            String userId = securityContext.getUserId();
            LOGGER.info("User logging out: {}", userId);
            
            // 从请求头获取Token并加入黑名单
            String authHeader = ctx.request().getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                try {
                    JwtAuthProvider provider = getJwtAuthProvider();
                    provider.revokeToken(token);
                    LOGGER.info("Token revoked for user: {}", userId);
                } catch (Exception e) {
                    LOGGER.warn("Failed to revoke token for user {}: {}", userId, e.getMessage());
                }
            }
        }

        return Future.succeededFuture(JsonResult.success("登出成功"));
    }
}
