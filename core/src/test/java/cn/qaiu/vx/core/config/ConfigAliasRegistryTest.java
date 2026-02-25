package cn.qaiu.vx.core.config;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * ConfigAliasRegistry 单元测试
 *
 * @author <a href="https://qaiu.top">QAIU</a>
 */
@DisplayName("配置别名注册表测试")
public class ConfigAliasRegistryTest {

  private ConfigAliasRegistry registry;

  @BeforeEach
  void setUp() {
    registry = ConfigAliasRegistry.getInstance();
    // 清理自定义别名组，保留默认的
    // 注意：单例模式下需要注意测试隔离
  }

  @Test
  @DisplayName("测试单例模式")
  void testSingleton() {
    ConfigAliasRegistry instance1 = ConfigAliasRegistry.getInstance();
    ConfigAliasRegistry instance2 = ConfigAliasRegistry.getInstance();
    assertSame(instance1, instance2, "应该返回同一个实例");
  }

  @Test
  @DisplayName("测试默认URL别名组")
  void testDefaultUrlAliases() {
    // 验证 url 别名组
    String canonical = registry.getCanonicalName("jdbcUrl");
    assertEquals("url", canonical, "jdbcUrl应该映射到url");

    canonical = registry.getCanonicalName("jdbc-url");
    assertEquals("url", canonical, "jdbc-url应该映射到url");

    canonical = registry.getCanonicalName("url");
    assertEquals("url", canonical, "url应该返回自身");
  }

  @Test
  @DisplayName("测试默认用户名别名组")
  void testDefaultUsernameAliases() {
    String canonical = registry.getCanonicalName("user");
    assertEquals("username", canonical, "user应该映射到username");

    canonical = registry.getCanonicalName("userName");
    assertEquals("username", canonical, "userName应该映射到username");
  }

  @Test
  @DisplayName("测试默认密码别名组")
  void testDefaultPasswordAliases() {
    String canonical = registry.getCanonicalName("pwd");
    assertEquals("password", canonical, "pwd应该映射到password");

    canonical = registry.getCanonicalName("pass");
    assertEquals("password", canonical, "pass应该映射到password");
  }

  @Test
  @DisplayName("测试默认数据源别名组")
  void testDefaultDatasourcesAliases() {
    String canonical = registry.getCanonicalName("database");
    assertEquals("datasources", canonical, "database应该映射到datasources");

    canonical = registry.getCanonicalName("dataSource");
    assertEquals("datasources", canonical, "dataSource应该映射到datasources");
  }

  @Test
  @DisplayName("测试默认驱动别名组")
  void testDefaultDriverAliases() {
    String canonical = registry.getCanonicalName("driver");
    assertEquals("driverClassName", canonical, "driver应该映射到driverClassName");

    canonical = registry.getCanonicalName("driver-class-name");
    assertEquals("driverClassName", canonical, "driver-class-name应该映射到driverClassName");
  }

  @Test
  @DisplayName("测试默认端口别名组")
  void testDefaultPortAliases() {
    String canonical = registry.getCanonicalName("serverPort");
    assertEquals("port", canonical, "serverPort应该映射到port");

    canonical = registry.getCanonicalName("server-port");
    assertEquals("port", canonical, "server-port应该映射到port");
  }

  @Test
  @DisplayName("测试注册自定义别名组")
  void testRegisterCustomAliasGroup() {
    registry.registerAliasGroup(
        "timeout", "timeout", "connectTimeout", "connTimeout", "connection-timeout");

    String canonical = registry.getCanonicalName("connectTimeout");
    assertEquals("timeout", canonical, "connectTimeout应该映射到timeout");

    canonical = registry.getCanonicalName("connTimeout");
    assertEquals("timeout", canonical, "connTimeout应该映射到timeout");

    canonical = registry.getCanonicalName("connection-timeout");
    assertEquals("timeout", canonical, "connection-timeout应该映射到timeout");
  }

  @Test
  @DisplayName("测试isAlias方法")
  void testIsAlias() {
    assertTrue(registry.isAlias("url", "jdbcUrl"), "url和jdbcUrl应该是别名关系");
    assertTrue(registry.isAlias("jdbcUrl", "url"), "jdbcUrl和url应该是别名关系");
    assertTrue(registry.isAlias("url", "url"), "同一个键应该是别名关系");
    assertFalse(registry.isAlias("url", "password"), "url和password不应该是别名关系");
  }

  @Test
  @DisplayName("测试获取所有别名组")
  void testGetAllAliasGroups() {
    Map<String, Set<String>> allGroups = registry.getAllAliasGroups();
    assertNotNull(allGroups, "别名组映射不应为null");
    assertFalse(allGroups.isEmpty(), "应该有默认的别名组");
    assertTrue(allGroups.containsKey("url"), "应该包含url别名组");
    assertTrue(allGroups.containsKey("username"), "应该包含username别名组");
  }

  @Test
  @DisplayName("测试获取特定别名组的所有别名")
  void testGetAliases() {
    Set<String> urlAliases = registry.getAliases("url");
    assertNotNull(urlAliases, "url别名组不应为null");
    assertTrue(urlAliases.contains("jdbcUrl"), "url别名组应该包含jdbcUrl");
    assertTrue(urlAliases.contains("jdbc-url"), "url别名组应该包含jdbc-url");
  }

  @Test
  @DisplayName("测试未注册的键返回原键")
  void testUnregisteredKeyReturnsItself() {
    String result = registry.getCanonicalName("unknownKey");
    assertEquals("unknownKey", result, "未注册的键应该返回原键");
  }

  @Test
  @DisplayName("测试null输入")
  void testNullInput() {
    String result = registry.getCanonicalName(null);
    assertNull(result, "null输入应该返回null");

    assertFalse(registry.isAlias(null, "url"), "null不应该是任何键的别名");
    assertFalse(registry.isAlias("url", null), "null不应该是任何键的别名");
  }

  @Test
  @DisplayName("测试空字符串输入")
  void testEmptyStringInput() {
    String result = registry.getCanonicalName("");
    assertEquals("", result, "空字符串应该返回空字符串");
  }

  @Test
  @DisplayName("测试大小写不敏感性")
  void testCaseInsensitivity() {
    // 别名是大小写不敏感的，因为注册时同时注册了小写版本
    String canonical1 = registry.getCanonicalName("jdbcUrl");
    String canonical2 = registry.getCanonicalName("JDBCURL");
    // 都应该解析为 "url"
    assertEquals(canonical1, canonical2, "别名应该是大小写不敏感的");
    assertEquals("url", canonical1, "jdbcUrl 应该解析为 url");
    assertEquals("url", canonical2, "JDBCURL 应该解析为 url");
  }
}
