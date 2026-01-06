package cn.qaiu.vx.core.security;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;
import io.vertx.ext.web.RoutingContext;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 安全上下文
 * 封装当前请求的用户信息和权限
 *
 * @author <a href="https://qaiu.top">QAIU</a>
 */
public class SecurityContext {

    private static final String CONTEXT_KEY = "vxcore.security.context";

    private final User user;
    private final JsonObject principal;
    private final Set<String> roles;
    private final Set<String> permissions;

    public SecurityContext(User user) {
        this.user = user;
        this.principal = user != null ? user.principal() : new JsonObject();
        this.roles = extractRoles(principal);
        this.permissions = extractPermissions(principal);
    }

    /**
     * 获取Vert.x User对象
     *
     * @return User对象
     */
    public User getUser() {
        return user;
    }

    /**
     * 获取用户主体信息
     *
     * @return 主体JsonObject
     */
    public JsonObject getPrincipal() {
        return principal;
    }

    /**
     * 获取用户ID
     *
     * @return 用户ID
     */
    public String getUserId() {
        return principal.getString("sub", principal.getString("userId", principal.getString("id")));
    }

    /**
     * 获取用户名
     *
     * @return 用户名
     */
    public String getUsername() {
        return principal.getString("username", principal.getString("name", principal.getString("sub")));
    }

    /**
     * 获取用户角色集合
     *
     * @return 角色集合
     */
    public Set<String> getRoles() {
        return Collections.unmodifiableSet(roles);
    }

    /**
     * 获取用户权限集合
     *
     * @return 权限集合
     */
    public Set<String> getPermissions() {
        return Collections.unmodifiableSet(permissions);
    }

    /**
     * 检查是否有指定角色
     *
     * @param role 角色名
     * @return 是否有该角色
     */
    public boolean hasRole(String role) {
        return roles.contains(role);
    }

    /**
     * 检查是否有任意一个指定角色
     *
     * @param roles 角色列表
     * @return 是否有任意一个角色
     */
    public boolean hasAnyRole(String... roles) {
        for (String role : roles) {
            if (hasRole(role)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 检查是否有所有指定角色
     *
     * @param roles 角色列表
     * @return 是否有所有角色
     */
    public boolean hasAllRoles(String... roles) {
        for (String role : roles) {
            if (!hasRole(role)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 检查是否有指定权限
     * 支持通配符匹配：user:* 匹配 user:read, user:write 等
     *
     * @param permission 权限字符串（格式：resource:action）
     * @return 是否有该权限
     */
    public boolean hasPermission(String permission) {
        if (permission == null || permission.isEmpty()) {
            return true;
        }

        // 直接匹配
        if (permissions.contains(permission)) {
            return true;
        }

        // 检查通配符匹配
        return checkWildcardPermission(permission);
    }

    /**
     * 检查是否有任意一个指定权限
     *
     * @param permissions 权限列表
     * @return 是否有任意一个权限
     */
    public boolean hasAnyPermission(String... permissions) {
        for (String permission : permissions) {
            if (hasPermission(permission)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 检查是否有所有指定权限
     *
     * @param permissions 权限列表
     * @return 是否有所有权限
     */
    public boolean hasAllPermissions(String... permissions) {
        for (String permission : permissions) {
            if (!hasPermission(permission)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 检查通配符权限匹配
     */
    private boolean checkWildcardPermission(String requiredPermission) {
        String[] requiredParts = requiredPermission.split(":");
        if (requiredParts.length != 2) {
            return false;
        }

        String requiredResource = requiredParts[0];
        String requiredAction = requiredParts[1];

        for (String userPermission : permissions) {
            String[] userParts = userPermission.split(":");
            if (userParts.length != 2) {
                continue;
            }

            String userResource = userParts[0];
            String userAction = userParts[1];

            // 检查资源匹配
            boolean resourceMatch = "*".equals(userResource) || 
                                   userResource.equals(requiredResource);
            
            // 检查操作匹配
            boolean actionMatch = "*".equals(userAction) || 
                                 userAction.equals(requiredAction);

            if (resourceMatch && actionMatch) {
                return true;
            }
        }

        return false;
    }

    /**
     * 从JWT claims中提取角色
     */
    private Set<String> extractRoles(JsonObject principal) {
        Set<String> result = new HashSet<>();
        
        // 尝试多种常见的角色字段名
        String[] roleFields = {"roles", "role", "authorities", "groups"};
        
        for (String field : roleFields) {
            Object value = principal.getValue(field);
            if (value instanceof JsonArray) {
                JsonArray arr = (JsonArray) value;
                for (int i = 0; i < arr.size(); i++) {
                    Object item = arr.getValue(i);
                    if (item != null) {
                        result.add(item.toString());
                    }
                }
            } else if (value instanceof String) {
                // 逗号分隔的角色字符串
                String[] parts = ((String) value).split(",");
                for (String part : parts) {
                    String trimmed = part.trim();
                    if (!trimmed.isEmpty()) {
                        result.add(trimmed);
                    }
                }
            }
        }
        
        return result;
    }

    /**
     * 从JWT claims中提取权限
     */
    private Set<String> extractPermissions(JsonObject principal) {
        Set<String> result = new HashSet<>();
        
        // 尝试多种常见的权限字段名
        String[] permFields = {"permissions", "perms", "authorities", "scope", "scopes"};
        
        for (String field : permFields) {
            Object value = principal.getValue(field);
            if (value instanceof JsonArray) {
                JsonArray arr = (JsonArray) value;
                for (int i = 0; i < arr.size(); i++) {
                    Object item = arr.getValue(i);
                    if (item != null) {
                        result.add(item.toString());
                    }
                }
            } else if (value instanceof String) {
                // 空格或逗号分隔的权限字符串
                String[] parts = ((String) value).split("[,\\s]+");
                for (String part : parts) {
                    String trimmed = part.trim();
                    if (!trimmed.isEmpty()) {
                        result.add(trimmed);
                    }
                }
            }
        }
        
        return result;
    }

    /**
     * 检查是否已认证
     *
     * @return 是否已认证
     */
    public boolean isAuthenticated() {
        return user != null && principal != null && !principal.isEmpty();
    }

    /**
     * 获取RoutingContext中的上下文键名
     *
     * @return 上下文键名
     */
    public static String getContextKey() {
        return CONTEXT_KEY;
    }

    /**
     * 从RoutingContext获取SecurityContext
     *
     * @param ctx 路由上下文
     * @return SecurityContext实例，如果不存在则返回null
     */
    public static SecurityContext fromContext(RoutingContext ctx) {
        if (ctx == null) {
            return null;
        }
        return ctx.get(CONTEXT_KEY);
    }

    @Override
    public String toString() {
        return "SecurityContext{" +
                "userId='" + getUserId() + '\'' +
                ", username='" + getUsername() + '\'' +
                ", roles=" + roles +
                ", permissions=" + permissions +
                ", authenticated=" + isAuthenticated() +
                '}';
    }
}
