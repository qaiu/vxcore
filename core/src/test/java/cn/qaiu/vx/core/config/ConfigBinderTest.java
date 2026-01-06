package cn.qaiu.vx.core.config;

import static org.junit.jupiter.api.Assertions.*;

import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * ConfigBinder 单元测试
 *
 * @author <a href="https://qaiu.top">QAIU</a>
 */
@DisplayName("配置绑定器测试")
public class ConfigBinderTest {

  private ConfigBinder binder;

  @BeforeEach
  void setUp() {
    binder = new ConfigBinder();
  }

  @Test
  @DisplayName("测试绑定基本类型")
  void testBindBasicTypes() {
    JsonObject config = new JsonObject()
        .put("name", "TestApp")
        .put("port", 8080)
        .put("enabled", true)
        .put("timeout", 30.5)
        .put("maxConnections", 100L);

    SimpleConfig result = binder.bind(config, SimpleConfig.class);

    assertNotNull(result);
    assertEquals("TestApp", result.getName());
    assertEquals(8080, result.getPort());
    assertTrue(result.isEnabled());
    assertEquals(30.5, result.getTimeout());
    assertEquals(100L, result.getMaxConnections());
  }

  @Test
  @DisplayName("测试绑定使用别名")
  void testBindWithAlias() {
    JsonObject config = new JsonObject()
        .put("jdbcUrl", "jdbc:mysql://localhost/test")
        .put("user", "root")
        .put("pwd", "secret");

    DatabaseConfig result = binder.bind(config, DatabaseConfig.class);

    assertNotNull(result);
    assertEquals("jdbc:mysql://localhost/test", result.getUrl());
    assertEquals("root", result.getUsername());
    assertEquals("secret", result.getPassword());
  }

  @Test
  @DisplayName("测试绑定嵌套对象")
  void testBindNestedObject() {
    JsonObject config = new JsonObject()
        .put("name", "MainApp")
        .put("database", new JsonObject()
            .put("url", "jdbc:h2:mem:test")
            .put("username", "sa"));

    AppConfig result = binder.bind(config, AppConfig.class);

    assertNotNull(result);
    assertEquals("MainApp", result.getName());
    assertNotNull(result.getDatabase());
    assertEquals("jdbc:h2:mem:test", result.getDatabase().getString("url"));
  }

  @Test
  @DisplayName("测试绑定null配置")
  void testBindNullConfig() {
    SimpleConfig result = binder.bind(null, SimpleConfig.class);
    assertNull(result);
  }

  @Test
  @DisplayName("测试绑定空配置")
  void testBindEmptyConfig() {
    SimpleConfig result = binder.bind(new JsonObject(), SimpleConfig.class);

    assertNotNull(result);
    assertNull(result.getName());
    // 原始类型应该有默认值
    assertEquals(0, result.getPort());
    assertFalse(result.isEnabled());
  }

  @Test
  @DisplayName("测试绑定到已存在的实例")
  void testBindToInstance() {
    JsonObject config = new JsonObject()
        .put("name", "UpdatedName")
        .put("port", 9090);

    SimpleConfig instance = new SimpleConfig();
    instance.setName("OriginalName");
    instance.setPort(8080);

    binder.bindToInstance(config, instance);

    assertEquals("UpdatedName", instance.getName());
    assertEquals(9090, instance.getPort());
  }

  @Test
  @DisplayName("测试注册自定义策略")
  void testRegisterCustomStrategy() {
    binder.registerStrategy(CustomType.class, (value, targetType) -> {
      if (value != null) {
        return new CustomType(value.toString());
      }
      return null;
    });

    JsonObject config = new JsonObject()
        .put("customField", "custom-value");

    CustomConfig result = binder.bind(config, CustomConfig.class);

    assertNotNull(result);
    assertNotNull(result.getCustomField());
    assertEquals("custom-value", result.getCustomField().getValue());
  }

  @Test
  @DisplayName("测试类型转换 - 字符串到数字")
  void testTypeConversionStringToNumber() {
    JsonObject config = new JsonObject()
        .put("port", "8080")  // 字符串形式的数字
        .put("timeout", "30.5");

    SimpleConfig result = binder.bind(config, SimpleConfig.class);

    assertNotNull(result);
    assertEquals(8080, result.getPort());
    assertEquals(30.5, result.getTimeout());
  }

  @Test
  @DisplayName("测试类型转换 - 数字到字符串")
  void testTypeConversionNumberToString() {
    JsonObject config = new JsonObject()
        .put("name", 12345);  // 数字会被转换为字符串

    SimpleConfig result = binder.bind(config, SimpleConfig.class);

    assertNotNull(result);
    assertEquals("12345", result.getName());
  }

  @Test
  @DisplayName("测试原始类型的默认值")
  void testPrimitiveDefaults() {
    JsonObject config = new JsonObject();

    PrimitiveConfig result = binder.bind(config, PrimitiveConfig.class);

    assertNotNull(result);
    assertEquals(0, result.getIntValue());
    assertEquals(0L, result.getLongValue());
    assertEquals(0.0, result.getDoubleValue());
    assertFalse(result.isBoolValue());
  }

  @Test
  @DisplayName("测试包装类型null处理")
  void testWrapperTypeNullHandling() {
    JsonObject config = new JsonObject();

    WrapperConfig result = binder.bind(config, WrapperConfig.class);

    assertNotNull(result);
    assertNull(result.getIntegerValue());
    assertNull(result.getLongValue());
    assertNull(result.getDoubleValue());
    assertNull(result.getBoolValue());
  }

  // ============ 测试用POJO类 ============

  public static class SimpleConfig {
    private String name;
    private int port;
    private boolean enabled;
    private double timeout;
    private long maxConnections;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getPort() { return port; }
    public void setPort(int port) { this.port = port; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public double getTimeout() { return timeout; }
    public void setTimeout(double timeout) { this.timeout = timeout; }

    public long getMaxConnections() { return maxConnections; }
    public void setMaxConnections(long maxConnections) { this.maxConnections = maxConnections; }
  }

  public static class DatabaseConfig {
    private String url;
    private String username;
    private String password;

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
  }

  public static class AppConfig {
    private String name;
    private JsonObject database;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public JsonObject getDatabase() { return database; }
    public void setDatabase(JsonObject database) { this.database = database; }
  }

  public static class CustomType {
    private final String value;

    public CustomType(String value) { this.value = value; }

    public String getValue() { return value; }
  }

  public static class CustomConfig {
    private CustomType customField;

    public CustomType getCustomField() { return customField; }
    public void setCustomField(CustomType customField) { this.customField = customField; }
  }

  public static class PrimitiveConfig {
    private int intValue;
    private long longValue;
    private double doubleValue;
    private boolean boolValue;

    public int getIntValue() { return intValue; }
    public void setIntValue(int intValue) { this.intValue = intValue; }

    public long getLongValue() { return longValue; }
    public void setLongValue(long longValue) { this.longValue = longValue; }

    public double getDoubleValue() { return doubleValue; }
    public void setDoubleValue(double doubleValue) { this.doubleValue = doubleValue; }

    public boolean isBoolValue() { return boolValue; }
    public void setBoolValue(boolean boolValue) { this.boolValue = boolValue; }
  }

  public static class WrapperConfig {
    private Integer integerValue;
    private Long longValue;
    private Double doubleValue;
    private Boolean boolValue;

    public Integer getIntegerValue() { return integerValue; }
    public void setIntegerValue(Integer integerValue) { this.integerValue = integerValue; }

    public Long getLongValue() { return longValue; }
    public void setLongValue(Long longValue) { this.longValue = longValue; }

    public Double getDoubleValue() { return doubleValue; }
    public void setDoubleValue(Double doubleValue) { this.doubleValue = doubleValue; }

    public Boolean getBoolValue() { return boolValue; }
    public void setBoolValue(Boolean boolValue) { this.boolValue = boolValue; }
  }
}
