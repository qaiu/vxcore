package cn.qaiu.vx.core.config;

import static org.junit.jupiter.api.Assertions.*;

import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * ConfigResolver 单元测试
 *
 * @author <a href="https://qaiu.top">QAIU</a>
 */
@DisplayName("配置解析器测试")
public class ConfigResolverTest {

  private JsonObject testConfig;

  @BeforeEach
  void setUp() {
    testConfig = new JsonObject()
        .put("url", "jdbc:mysql://localhost:3306/test")
        .put("user", "root")
        .put("password", "secret")
        .put("port", 8080)
        .put("maxPoolSize", 10)
        .put("enabled", true)
        .put("timeout", 30L)
        .put("nested", new JsonObject().put("key", "value"));
  }

  @Test
  @DisplayName("测试使用别名获取字符串值")
  void testGetStringWithAlias() {
    ConfigResolver resolver = new ConfigResolver(testConfig);

    // 使用别名 jdbcUrl 获取 url 的值
    String jdbcUrl = resolver.getString("jdbcUrl");
    assertEquals("jdbc:mysql://localhost:3306/test", jdbcUrl, "应该通过别名获取到url的值");

    // 使用别名 username 获取 user 的值
    String username = resolver.getString("username");
    assertEquals("root", username, "应该通过别名获取到user的值");
  }

  @Test
  @DisplayName("测试直接获取字符串值")
  void testGetStringDirect() {
    ConfigResolver resolver = new ConfigResolver(testConfig);

    String url = resolver.getString("url");
    assertEquals("jdbc:mysql://localhost:3306/test", url);

    String password = resolver.getString("password");
    assertEquals("secret", password);
  }

  @Test
  @DisplayName("测试获取字符串带默认值")
  void testGetStringWithDefault() {
    ConfigResolver resolver = new ConfigResolver(testConfig);

    String missing = resolver.getString("nonexistent", "default");
    assertEquals("default", missing, "不存在的键应该返回默认值");

    String existing = resolver.getString("url", "default");
    assertEquals("jdbc:mysql://localhost:3306/test", existing, "存在的键应该返回实际值");
  }

  @Test
  @DisplayName("测试获取整数值")
  void testGetInteger() {
    ConfigResolver resolver = new ConfigResolver(testConfig);

    Integer port = resolver.getInteger("port");
    assertEquals(8080, port);

    // 使用别名 serverPort 获取 port 的值
    Integer serverPort = resolver.getInteger("serverPort");
    assertEquals(8080, serverPort, "应该通过别名获取到port的值");
  }

  @Test
  @DisplayName("测试获取整数值带默认值")
  void testGetIntegerWithDefault() {
    ConfigResolver resolver = new ConfigResolver(testConfig);

    Integer missing = resolver.getInteger("nonexistent", 9999);
    assertEquals(9999, missing);

    Integer existing = resolver.getInteger("port", 9999);
    assertEquals(8080, existing);
  }

  @Test
  @DisplayName("测试获取长整型值")
  void testGetLong() {
    ConfigResolver resolver = new ConfigResolver(testConfig);

    Long value = resolver.getLong("timeout");
    assertEquals(30L, value);
  }

  @Test
  @DisplayName("测试获取长整型值带默认值")
  void testGetLongWithDefault() {
    ConfigResolver resolver = new ConfigResolver(testConfig);

    Long missing = resolver.getLong("nonexistent", 100L);
    assertEquals(100L, missing);

    Long existing = resolver.getLong("timeout", 100L);
    assertEquals(30L, existing);
  }

  @Test
  @DisplayName("测试获取布尔值")
  void testGetBoolean() {
    ConfigResolver resolver = new ConfigResolver(testConfig);

    Boolean enabled = resolver.getBoolean("enabled");
    assertTrue(enabled);

    Boolean missing = resolver.getBoolean("nonexistent", false);
    assertFalse(missing);
  }

  @Test
  @DisplayName("测试获取JsonObject值")
  void testGetJsonObject() {
    ConfigResolver resolver = new ConfigResolver(testConfig);

    JsonObject nested = resolver.getJsonObject("nested");
    assertNotNull(nested);
    assertEquals("value", nested.getString("key"));
  }

  @Test
  @DisplayName("测试获取JsonObject或空")
  void testGetJsonObjectOrEmpty() {
    ConfigResolver resolver = new ConfigResolver(testConfig);

    JsonObject nested = resolver.getJsonObjectOrEmpty("nested");
    assertNotNull(nested);
    assertEquals("value", nested.getString("key"));

    JsonObject missing = resolver.getJsonObjectOrEmpty("nonexistent");
    assertNotNull(missing);
    assertTrue(missing.isEmpty());
  }

  @Test
  @DisplayName("测试包含键检查")
  void testContainsKey() {
    ConfigResolver resolver = new ConfigResolver(testConfig);

    assertTrue(resolver.containsKey("url"), "应该包含url键");
    assertTrue(resolver.containsKey("jdbcUrl"), "应该通过别名找到url键");
    assertFalse(resolver.containsKey("nonexistent"), "不应该包含不存在的键");
  }

  @Test
  @DisplayName("测试别名优先级 - 实际键优先")
  void testAliasPriority() {
    // 如果配置中同时存在实际键和别名键，应该优先返回实际键的值
    JsonObject config = new JsonObject()
        .put("url", "actual-url")
        .put("jdbcUrl", "alias-url");

    ConfigResolver resolver = new ConfigResolver(config);

    // 直接获取 url
    String url = resolver.getString("url");
    assertEquals("actual-url", url);

    // 通过别名获取，应该先找 jdbcUrl（因为它实际存在）
    String jdbcUrl = resolver.getString("jdbcUrl");
    assertEquals("alias-url", jdbcUrl);
  }

  @Test
  @DisplayName("测试null配置")
  void testNullConfig() {
    ConfigResolver resolver = new ConfigResolver(null);

    assertNull(resolver.getString("anyKey"));
    assertFalse(resolver.containsKey("anyKey"));
  }

  @Test
  @DisplayName("测试空配置")
  void testEmptyConfig() {
    ConfigResolver resolver = new ConfigResolver(new JsonObject());

    assertNull(resolver.getString("anyKey"));
    assertEquals("default", resolver.getString("anyKey", "default"));
  }

  @Test
  @DisplayName("测试数据源配置别名")
  void testDataSourceAliases() {
    JsonObject config = new JsonObject()
        .put("database", new JsonObject()
            .put("primary", new JsonObject()
                .put("url", "jdbc:mysql://localhost/db")));

    ConfigResolver resolver = new ConfigResolver(config);

    // 通过 datasources 别名获取 database 的值
    JsonObject datasources = resolver.getJsonObject("datasources");
    assertNotNull(datasources, "应该通过别名获取到database配置");
    assertTrue(datasources.containsKey("primary"));
  }

  @Test
  @DisplayName("测试getRawConfig方法")
  void testGetRawConfig() {
    ConfigResolver resolver = new ConfigResolver(testConfig);

    JsonObject raw = resolver.getRawConfig();
    assertNotNull(raw);
    assertEquals("jdbc:mysql://localhost:3306/test", raw.getString("url"));
  }

  @Test
  @DisplayName("测试子解析器")
  void testGetSubResolver() {
    ConfigResolver resolver = new ConfigResolver(testConfig);

    ConfigResolver subResolver = resolver.getSubResolver("nested");
    assertNotNull(subResolver);
    assertEquals("value", subResolver.getString("key"));
  }
}
