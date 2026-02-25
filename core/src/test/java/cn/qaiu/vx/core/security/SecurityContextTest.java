package cn.qaiu.vx.core.security;

import static org.junit.jupiter.api.Assertions.*;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * SecurityContext 单元测试
 *
 * @author <a href="https://qaiu.top">QAIU</a>
 */
@DisplayName("安全上下文测试")
public class SecurityContextTest {

  private JsonObject createPrincipal() {
    return new JsonObject()
        .put("sub", "user123")
        .put("username", "testuser")
        .put("roles", new JsonArray().add("admin").add("user"))
        .put(
            "permissions",
            new JsonArray().add("user:read").add("user:write").add("system:*").add("admin:manage"));
  }

  @Test
  @DisplayName("测试获取用户ID - sub字段")
  void testGetUserIdFromSub() {
    JsonObject principal = new JsonObject().put("sub", "user123");
    User user = User.create(principal);
    SecurityContext ctx = new SecurityContext(user);

    assertEquals("user123", ctx.getUserId());
  }

  @Test
  @DisplayName("测试获取用户ID - userId字段")
  void testGetUserIdFromUserId() {
    JsonObject principal = new JsonObject().put("userId", "user456");
    User user = User.create(principal);
    SecurityContext ctx = new SecurityContext(user);

    assertEquals("user456", ctx.getUserId());
  }

  @Test
  @DisplayName("测试获取用户ID - id字段")
  void testGetUserIdFromId() {
    JsonObject principal = new JsonObject().put("id", "user789");
    User user = User.create(principal);
    SecurityContext ctx = new SecurityContext(user);

    assertEquals("user789", ctx.getUserId());
  }

  @Test
  @DisplayName("测试获取用户名")
  void testGetUsername() {
    JsonObject principal = new JsonObject().put("sub", "user123").put("username", "testuser");
    User user = User.create(principal);
    SecurityContext ctx = new SecurityContext(user);

    assertEquals("testuser", ctx.getUsername());
  }

  @Test
  @DisplayName("测试hasRole方法")
  void testHasRole() {
    User user = User.create(createPrincipal());
    SecurityContext ctx = new SecurityContext(user);

    assertTrue(ctx.hasRole("admin"), "应该有admin角色");
    assertTrue(ctx.hasRole("user"), "应该有user角色");
    assertFalse(ctx.hasRole("guest"), "不应该有guest角色");
  }

  @Test
  @DisplayName("测试hasAnyRole方法")
  void testHasAnyRole() {
    User user = User.create(createPrincipal());
    SecurityContext ctx = new SecurityContext(user);

    assertTrue(ctx.hasAnyRole("admin", "guest"), "应该至少有一个角色");
    assertTrue(ctx.hasAnyRole("nobody", "user"), "应该至少有一个角色");
    assertFalse(ctx.hasAnyRole("nobody", "guest"), "不应该有这些角色");
  }

  @Test
  @DisplayName("测试hasAllRoles方法")
  void testHasAllRoles() {
    User user = User.create(createPrincipal());
    SecurityContext ctx = new SecurityContext(user);

    assertTrue(ctx.hasAllRoles("admin", "user"), "应该有所有这些角色");
    assertFalse(ctx.hasAllRoles("admin", "guest"), "不应该有所有这些角色");
  }

  @Test
  @DisplayName("测试hasPermission方法 - 直接匹配")
  void testHasPermissionDirect() {
    User user = User.create(createPrincipal());
    SecurityContext ctx = new SecurityContext(user);

    assertTrue(ctx.hasPermission("user:read"), "应该有user:read权限");
    assertTrue(ctx.hasPermission("user:write"), "应该有user:write权限");
    assertFalse(ctx.hasPermission("user:delete"), "不应该有user:delete权限");
  }

  @Test
  @DisplayName("测试hasPermission方法 - 通配符匹配")
  void testHasPermissionWildcard() {
    User user = User.create(createPrincipal());
    SecurityContext ctx = new SecurityContext(user);

    // system:* 应该匹配 system:任何操作
    assertTrue(ctx.hasPermission("system:read"), "system:*应该匹配system:read");
    assertTrue(ctx.hasPermission("system:write"), "system:*应该匹配system:write");
    assertTrue(ctx.hasPermission("system:delete"), "system:*应该匹配system:delete");
  }

  @Test
  @DisplayName("测试hasPermission方法 - 资源通配符")
  void testHasPermissionResourceWildcard() {
    JsonObject principal =
        new JsonObject().put("sub", "admin").put("permissions", new JsonArray().add("*:read"));
    User user = User.create(principal);
    SecurityContext ctx = new SecurityContext(user);

    assertTrue(ctx.hasPermission("user:read"), "*:read应该匹配user:read");
    assertTrue(ctx.hasPermission("system:read"), "*:read应该匹配system:read");
    assertFalse(ctx.hasPermission("user:write"), "*:read不应该匹配user:write");
  }

  @Test
  @DisplayName("测试hasPermission方法 - 超级权限")
  void testHasPermissionSuperAdmin() {
    JsonObject principal =
        new JsonObject().put("sub", "superadmin").put("permissions", new JsonArray().add("*:*"));
    User user = User.create(principal);
    SecurityContext ctx = new SecurityContext(user);

    assertTrue(ctx.hasPermission("user:read"), "*:*应该匹配任何权限");
    assertTrue(ctx.hasPermission("system:write"), "*:*应该匹配任何权限");
    assertTrue(ctx.hasPermission("admin:delete"), "*:*应该匹配任何权限");
  }

  @Test
  @DisplayName("测试hasAnyPermission方法")
  void testHasAnyPermission() {
    User user = User.create(createPrincipal());
    SecurityContext ctx = new SecurityContext(user);

    assertTrue(ctx.hasAnyPermission("user:read", "unknown:perm"));
    assertTrue(ctx.hasAnyPermission("unknown:perm", "user:write"));
    assertFalse(ctx.hasAnyPermission("unknown:perm1", "unknown:perm2"));
  }

  @Test
  @DisplayName("测试hasAllPermissions方法")
  void testHasAllPermissions() {
    User user = User.create(createPrincipal());
    SecurityContext ctx = new SecurityContext(user);

    assertTrue(ctx.hasAllPermissions("user:read", "user:write"));
    assertFalse(ctx.hasAllPermissions("user:read", "user:delete"));
  }

  @Test
  @DisplayName("测试isAuthenticated方法")
  void testIsAuthenticated() {
    User user = User.create(createPrincipal());
    SecurityContext ctx = new SecurityContext(user);
    assertTrue(ctx.isAuthenticated());

    SecurityContext nullCtx = new SecurityContext(null);
    assertFalse(nullCtx.isAuthenticated());
  }

  @Test
  @DisplayName("测试null用户")
  void testNullUser() {
    SecurityContext ctx = new SecurityContext(null);

    assertNull(ctx.getUser());
    assertNotNull(ctx.getPrincipal());
    assertNull(ctx.getUserId());
    assertNull(ctx.getUsername());
    assertTrue(ctx.getRoles().isEmpty());
    assertTrue(ctx.getPermissions().isEmpty());
    assertFalse(ctx.isAuthenticated());
  }

  @Test
  @DisplayName("测试从逗号分隔的字符串提取角色")
  void testExtractRolesFromString() {
    JsonObject principal = new JsonObject().put("sub", "user1").put("roles", "admin,user,guest");
    User user = User.create(principal);
    SecurityContext ctx = new SecurityContext(user);

    assertTrue(ctx.hasRole("admin"));
    assertTrue(ctx.hasRole("user"));
    assertTrue(ctx.hasRole("guest"));
  }

  @Test
  @DisplayName("测试从空格分隔的字符串提取权限")
  void testExtractPermissionsFromString() {
    JsonObject principal =
        new JsonObject().put("sub", "user1").put("scope", "user:read user:write system:admin");
    User user = User.create(principal);
    SecurityContext ctx = new SecurityContext(user);

    assertTrue(ctx.hasPermission("user:read"));
    assertTrue(ctx.hasPermission("user:write"));
    assertTrue(ctx.hasPermission("system:admin"));
  }

  @Test
  @DisplayName("测试空权限字符串")
  void testEmptyPermission() {
    User user = User.create(createPrincipal());
    SecurityContext ctx = new SecurityContext(user);

    assertTrue(ctx.hasPermission(""), "空权限字符串应该返回true");
    assertTrue(ctx.hasPermission(null), "null权限应该返回true");
  }

  @Test
  @DisplayName("测试获取不可变的角色和权限集合")
  void testImmutableCollections() {
    User user = User.create(createPrincipal());
    SecurityContext ctx = new SecurityContext(user);

    assertThrows(UnsupportedOperationException.class, () -> ctx.getRoles().add("newRole"));
    assertThrows(
        UnsupportedOperationException.class, () -> ctx.getPermissions().add("new:permission"));
  }

  @Test
  @DisplayName("测试toString方法")
  void testToString() {
    User user = User.create(createPrincipal());
    SecurityContext ctx = new SecurityContext(user);

    String str = ctx.toString();
    assertNotNull(str);
    assertTrue(str.contains("user123"));
    assertTrue(str.contains("testuser"));
    assertTrue(str.contains("admin"));
  }

  @Test
  @DisplayName("测试上下文键名")
  void testContextKey() {
    assertEquals("vxcore.security.context", SecurityContext.getContextKey());
  }
}
