package cn.qaiu.example.controller;

import cn.qaiu.vx.core.annotations.RouteHandler;
import cn.qaiu.vx.core.annotations.RouteMapping;
import cn.qaiu.vx.core.enums.RouteMethod;
import cn.qaiu.vx.core.model.JsonResult;
import cn.qaiu.vx.core.security.Authenticated;
import cn.qaiu.vx.core.security.RequiresPermissions;
import cn.qaiu.vx.core.security.RequiresRoles;
import cn.qaiu.vx.core.security.SecurityContext;
import cn.qaiu.vx.core.security.Anonymous;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 安全示例控制器
 * 演示各种安全注解的使用方式
 *
 * @author <a href="https://qaiu.top">QAIU</a>
 */
@RouteHandler("/api/secure")
@Authenticated // 类级别认证要求
public class SecuredController {

    private static final Logger LOGGER = LoggerFactory.getLogger(SecuredController.class);

    // 模拟数据存储
    private static final Map<Long, JsonObject> RESOURCES = new ConcurrentHashMap<>();
    private static final AtomicLong ID_GENERATOR = new AtomicLong(1);

    static {
        // 初始化一些示例数据
        RESOURCES.put(1L, new JsonObject().put("id", 1L).put("name", "Resource 1").put("type", "public"));
        RESOURCES.put(2L, new JsonObject().put("id", 2L).put("name", "Resource 2").put("type", "private"));
        RESOURCES.put(3L, new JsonObject().put("id", 3L).put("name", "Admin Resource").put("type", "admin"));
    }

    /**
     * 公开资源 - 无需认证
     * GET /api/secure/public
     */
    @Anonymous
    @RouteMapping(value = "/public", method = RouteMethod.GET)
    public Future<JsonResult<JsonObject>> getPublicResource() {
        LOGGER.info("Accessing public resource");
        return Future.succeededFuture(JsonResult.data(new JsonObject()
                .put("message", "This is a public resource")
                .put("timestamp", System.currentTimeMillis())));
    }

    /**
     * 基本认证资源 - 只需要登录
     * GET /api/secure/authenticated
     */
    @Authenticated
    @RouteMapping(value = "/authenticated", method = RouteMethod.GET)
    public Future<JsonResult<JsonObject>> getAuthenticatedResource(RoutingContext ctx) {
        SecurityContext securityContext = SecurityContext.fromContext(ctx);
        
        LOGGER.info("User {} accessing authenticated resource", 
                securityContext != null ? securityContext.getUserId() : "unknown");
        
        return Future.succeededFuture(JsonResult.data(new JsonObject()
                .put("message", "This resource requires authentication")
                .put("user", securityContext != null ? securityContext.getUserId() : null)
                .put("timestamp", System.currentTimeMillis())));
    }

    /**
     * 读取资源 - 需要 resource:read 权限
     * GET /api/secure/resources
     */
    @RequiresPermissions("user:read")
    @RouteMapping(value = "/resources", method = RouteMethod.GET)
    public Future<JsonResult<List<JsonObject>>> listResources(RoutingContext ctx) {
        SecurityContext securityContext = SecurityContext.fromContext(ctx);
        LOGGER.info("User {} listing resources", 
                securityContext != null ? securityContext.getUserId() : "unknown");
        
        return Future.succeededFuture(JsonResult.data(RESOURCES.values().stream().toList()));
    }

    /**
     * 获取单个资源 - 需要 resource:read 权限
     * GET /api/secure/resources/{id}
     */
    @RequiresPermissions("user:read")
    @RouteMapping(value = "/resources/{id}", method = RouteMethod.GET)
    public Future<JsonResult<JsonObject>> getResource(Long id, RoutingContext ctx) {
        SecurityContext securityContext = SecurityContext.fromContext(ctx);
        LOGGER.info("User {} getting resource {}", 
                securityContext != null ? securityContext.getUserId() : "unknown", id);
        
        JsonObject resource = RESOURCES.get(id);
        if (resource == null) {
            return Future.succeededFuture(JsonResult.error("资源不存在", 404));
        }
        return Future.succeededFuture(JsonResult.data(resource));
    }

    /**
     * 创建资源 - 需要 resource:create 权限
     * POST /api/secure/resources
     */
    @RequiresPermissions("user:create")
    @RouteMapping(value = "/resources", method = RouteMethod.POST)
    public Future<JsonResult<JsonObject>> createResource(String name, String type, RoutingContext ctx) {
        SecurityContext securityContext = SecurityContext.fromContext(ctx);
        LOGGER.info("User {} creating resource", 
                securityContext != null ? securityContext.getUserId() : "unknown");
        
        Long id = ID_GENERATOR.incrementAndGet();
        JsonObject resource = new JsonObject()
                .put("id", id)
                .put("name", name)
                .put("type", type)
                .put("createdBy", securityContext != null ? securityContext.getUserId() : null);
        
        RESOURCES.put(id, resource);
        return Future.succeededFuture(JsonResult.data(resource));
    }

    /**
     * 更新资源 - 需要 resource:update 权限
     * PUT /api/secure/resources/{id}
     */
    @RequiresPermissions("user:update")
    @RouteMapping(value = "/resources/{id}", method = RouteMethod.PUT)
    public Future<JsonResult<JsonObject>> updateResource(Long id, String name, String type, RoutingContext ctx) {
        SecurityContext securityContext = SecurityContext.fromContext(ctx);
        LOGGER.info("User {} updating resource {}", 
                securityContext != null ? securityContext.getUserId() : "unknown", id);
        
        JsonObject resource = RESOURCES.get(id);
        if (resource == null) {
            return Future.succeededFuture(JsonResult.error("资源不存在", 404));
        }
        
        resource.put("name", name).put("type", type)
                .put("updatedBy", securityContext != null ? securityContext.getUserId() : null);
        
        return Future.succeededFuture(JsonResult.data(resource));
    }

    /**
     * 删除资源 - 需要 resource:delete 权限
     * DELETE /api/secure/resources/{id}
     */
    @RequiresPermissions("user:delete")
    @RouteMapping(value = "/resources/{id}", method = RouteMethod.DELETE)
    public Future<JsonResult<String>> deleteResource(Long id, RoutingContext ctx) {
        SecurityContext securityContext = SecurityContext.fromContext(ctx);
        LOGGER.info("User {} deleting resource {}", 
                securityContext != null ? securityContext.getUserId() : "unknown", id);
        
        JsonObject removed = RESOURCES.remove(id);
        if (removed == null) {
            return Future.succeededFuture(JsonResult.error("资源不存在", 404));
        }
        return Future.succeededFuture(JsonResult.success("资源删除成功"));
    }

    /**
     * 管理资源 - 需要 admin 角色
     * GET /api/secure/admin/resources
     */
    @RequiresRoles("admin")
    @RouteMapping(value = "/admin/resources", method = RouteMethod.GET)
    public Future<JsonResult<JsonObject>> getAdminResources(RoutingContext ctx) {
        SecurityContext securityContext = SecurityContext.fromContext(ctx);
        LOGGER.info("Admin {} accessing admin resources", 
                securityContext != null ? securityContext.getUserId() : "unknown");
        
        return Future.succeededFuture(JsonResult.data(new JsonObject()
                .put("totalResources", RESOURCES.size())
                .put("resources", new JsonArray(RESOURCES.values().stream().toList()))
                .put("adminOnly", true)));
    }

    /**
     * 系统管理 - 需要 system:admin 权限和 admin 角色
     * POST /api/secure/admin/system
     */
    @RequiresRoles("admin")
    @RequiresPermissions("system:admin")
    @RouteMapping(value = "/admin/system", method = RouteMethod.POST)
    public Future<JsonResult<JsonObject>> systemAdmin(String action, RoutingContext ctx) {
        SecurityContext securityContext = SecurityContext.fromContext(ctx);
        LOGGER.info("Admin {} performing system action: {}", 
                securityContext != null ? securityContext.getUserId() : "unknown", action);
        
        return Future.succeededFuture(JsonResult.data(new JsonObject()
                .put("action", action)
                .put("status", "executed")
                .put("executedBy", securityContext != null ? securityContext.getUserId() : null)
                .put("timestamp", System.currentTimeMillis())));
    }

    /**
     * 多权限组合 - 需要 user:read 和 order:read 权限（AND逻辑）
     * GET /api/secure/combined
     */
    @RequiresPermissions(value = {"user:read", "order:read"}, logical = RequiresPermissions.Logical.AND)
    @RouteMapping(value = "/combined", method = RouteMethod.GET)
    public Future<JsonResult<JsonObject>> getCombinedResource(RoutingContext ctx) {
        SecurityContext securityContext = SecurityContext.fromContext(ctx);
        LOGGER.info("User {} accessing combined resource", 
                securityContext != null ? securityContext.getUserId() : "unknown");
        
        return Future.succeededFuture(JsonResult.data(new JsonObject()
                .put("message", "This resource requires both user:read AND order:read permissions")
                .put("timestamp", System.currentTimeMillis())));
    }

    /**
     * 任一权限 - 需要 admin:* 或 user:* 权限（OR逻辑）
     * GET /api/secure/any
     */
    @RequiresPermissions(value = {"admin:*", "user:*"}, logical = RequiresPermissions.Logical.OR)
    @RouteMapping(value = "/any", method = RouteMethod.GET)
    public Future<JsonResult<JsonObject>> getAnyPermissionResource(RoutingContext ctx) {
        SecurityContext securityContext = SecurityContext.fromContext(ctx);
        LOGGER.info("User {} accessing any-permission resource", 
                securityContext != null ? securityContext.getUserId() : "unknown");
        
        return Future.succeededFuture(JsonResult.data(new JsonObject()
                .put("message", "This resource requires admin:* OR user:* permission")
                .put("timestamp", System.currentTimeMillis())));
    }
}
