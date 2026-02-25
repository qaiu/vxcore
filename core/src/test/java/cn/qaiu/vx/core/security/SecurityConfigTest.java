package cn.qaiu.vx.core.security;

import static org.junit.jupiter.api.Assertions.*;

import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * SecurityConfig 单元测试
 *
 * @author <a href="https://qaiu.top">QAIU</a>
 */
@DisplayName("安全配置测试")
public class SecurityConfigTest {

  @Test
  @DisplayName("测试默认配置")
  void testDefaultConfig() {
    SecurityConfig config = new SecurityConfig(new JsonObject());

    assertFalse(config.isJwtEnabled(), "JWT默认应该禁用");
    assertEquals("RS256", config.getJwtAlgorithm(), "默认算法应该是RS256");
    assertEquals(3600, config.getJwtExpireSeconds(), "默认过期时间应该是3600秒");
    assertEquals("vxcore", config.getJwtIssuer(), "默认发行者应该是vxcore");
    assertEquals("Authorization", config.getTokenHeader(), "默认Header应该是Authorization");
    assertEquals("Bearer ", config.getTokenPrefix(), "默认前缀应该是'Bearer '");
  }

  @Test
  @DisplayName("测试自定义配置")
  void testCustomConfig() {
    JsonObject configJson =
        new JsonObject()
            .put("jwt-enable", true)
            .put("jwt-algorithm", "HS256")
            .put("jwt-expire-seconds", 7200)
            .put("jwt-issuer", "my-app")
            .put("token-header", "X-Auth-Token")
            .put("token-prefix", "Token ");

    SecurityConfig config = new SecurityConfig(configJson);

    assertTrue(config.isJwtEnabled());
    assertEquals("HS256", config.getJwtAlgorithm());
    assertEquals(7200, config.getJwtExpireSeconds());
    assertEquals("my-app", config.getJwtIssuer());
    assertEquals("X-Auth-Token", config.getTokenHeader());
    assertEquals("Token ", config.getTokenPrefix());
  }

  @Test
  @DisplayName("测试认证路径配置")
  void testAuthPaths() {
    JsonObject configJson =
        new JsonObject()
            .put(
                "jwt-auth-reg",
                new io.vertx.core.json.JsonArray().add("/api/secure/.*").add("/api/admin/.*"));

    SecurityConfig config = new SecurityConfig(configJson);
    String[] authPaths = config.getAuthPaths();

    assertNotNull(authPaths);
    assertEquals(2, authPaths.length);
    assertEquals("/api/secure/.*", authPaths[0]);
    assertEquals("/api/admin/.*", authPaths[1]);
  }

  @Test
  @DisplayName("测试忽略路径配置")
  void testIgnorePaths() {
    JsonObject configJson =
        new JsonObject()
            .put(
                "jwt-ignores-reg",
                new io.vertx.core.json.JsonArray()
                    .add("/api/auth/login")
                    .add("/api/auth/register")
                    .add("/health"));

    SecurityConfig config = new SecurityConfig(configJson);
    String[] ignorePaths = config.getIgnorePaths();

    assertNotNull(ignorePaths);
    assertEquals(3, ignorePaths.length);
    assertEquals("/api/auth/login", ignorePaths[0]);
  }

  @Test
  @DisplayName("测试禁用JWT")
  void testJwtDisabled() {
    JsonObject configJson = new JsonObject().put("jwt-enable", false);

    SecurityConfig config = new SecurityConfig(configJson);

    assertFalse(config.isJwtEnabled());
  }

  @Test
  @DisplayName("测试公钥私钥路径配置")
  void testKeyPaths() {
    JsonObject configJson =
        new JsonObject()
            .put("jwt-public-key", "/path/to/public.pem")
            .put("jwt-private-key", "/path/to/private.pem");

    SecurityConfig config = new SecurityConfig(configJson);

    assertEquals("/path/to/public.pem", config.getJwtPublicKeyPath());
    assertEquals("/path/to/private.pem", config.getJwtPrivateKeyPath());
  }

  @Test
  @DisplayName("测试HS256密钥配置")
  void testHs256Secret() {
    JsonObject configJson =
        new JsonObject().put("jwt-algorithm", "HS256").put("jwt-secret", "my-secret-key-for-hs256");

    SecurityConfig config = new SecurityConfig(configJson);

    assertEquals("HS256", config.getJwtAlgorithm());
    assertEquals("my-secret-key-for-hs256", config.getJwtSecret());
  }

  @Test
  @DisplayName("测试null配置使用默认值")
  void testNullConfigUsesDefaults() {
    SecurityConfig config = new SecurityConfig(null);

    assertFalse(config.isJwtEnabled());
    assertEquals("RS256", config.getJwtAlgorithm());
  }

  @Test
  @DisplayName("测试空数组配置")
  void testEmptyArrayConfig() {
    JsonObject configJson =
        new JsonObject()
            .put("jwt-auth-reg", new io.vertx.core.json.JsonArray())
            .put("jwt-ignores-reg", new io.vertx.core.json.JsonArray());

    SecurityConfig config = new SecurityConfig(configJson);

    assertNotNull(config.getAuthPaths());
    assertEquals(0, config.getAuthPaths().length);
    assertNotNull(config.getIgnorePaths());
    assertEquals(0, config.getIgnorePaths().length);
  }

  @Test
  @DisplayName("测试Refresh Token过期配置")
  void testRefreshTokenExpire() {
    JsonObject configJson = new JsonObject().put("refresh-token-expire-seconds", 86400);

    SecurityConfig config = new SecurityConfig(configJson);

    assertEquals(86400, config.getRefreshTokenExpireSeconds());
  }

  @Test
  @DisplayName("测试fromJson静态方法")
  void testFromJson() {
    JsonObject configJson = new JsonObject().put("jwt-enable", true).put("jwt-algorithm", "RS256");

    SecurityConfig config = SecurityConfig.fromJson(configJson);

    assertNotNull(config);
    assertTrue(config.isJwtEnabled());
    assertEquals("RS256", config.getJwtAlgorithm());
  }

  @Test
  @DisplayName("测试toJson方法")
  void testToJson() {
    SecurityConfig config = new SecurityConfig();
    config.setJwtEnabled(true);
    config.setJwtAlgorithm("HS256");
    config.setJwtExpireSeconds(7200);

    JsonObject json = config.toJson();

    assertNotNull(json);
    assertTrue(json.getBoolean("jwtEnabled"));
    assertEquals("HS256", json.getString("jwtAlgorithm"));
    assertEquals(7200, json.getInteger("jwtExpireSeconds"));
  }

  @Test
  @DisplayName("测试驼峰式配置键兼容")
  void testCamelCaseConfigKeys() {
    JsonObject configJson =
        new JsonObject()
            .put("jwtEnabled", true)
            .put("jwtAlgorithm", "HS256")
            .put("jwtExpireSeconds", 7200)
            .put("jwtIssuer", "test-app")
            .put("tokenHeader", "X-Token")
            .put("tokenPrefix", "JWT ");

    SecurityConfig config = new SecurityConfig(configJson);

    assertTrue(config.isJwtEnabled());
    assertEquals("HS256", config.getJwtAlgorithm());
    assertEquals(7200, config.getJwtExpireSeconds());
    assertEquals("test-app", config.getJwtIssuer());
    assertEquals("X-Token", config.getTokenHeader());
    assertEquals("JWT ", config.getTokenPrefix());
  }
}
