package cn.qaiu.vx.core.lifecycle;

import static org.junit.jupiter.api.Assertions.*;

import cn.qaiu.vx.core.config.ConfigResolver;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ConfigurationComponent 单元测试
 *
 * @author <a href="https://qaiu.top">QAIU</a>
 */
@ExtendWith(VertxExtension.class)
@DisplayName("配置管理组件测试")
public class ConfigurationComponentTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationComponentTest.class);

  private ConfigurationComponent configurationComponent;
  private Vertx vertx;

  @BeforeEach
  @DisplayName("初始化测试环境")
  void setUp(Vertx vertx) {
    this.vertx = vertx;
    this.configurationComponent = new ConfigurationComponent();
  }

  @Test
  @DisplayName("测试组件初始化")
  void testComponentInitialization(VertxTestContext testContext) {
    JsonObject config = createValidConfig();

    configurationComponent
        .initialize(vertx, config)
        .onSuccess(
            v -> {
              testContext.verify(
                  () -> {
                    assertEquals("ConfigurationComponent", configurationComponent.getName());
                    assertEquals(10, configurationComponent.getPriority());
                    LOGGER.info("Configuration component initialized successfully");
                    testContext.completeNow();
                  });
            })
        .onFailure(testContext::failNow);
  }

  @Test
  @DisplayName("测试配置验证 - 有效配置")
  void testConfigurationValidationValid(VertxTestContext testContext) {
    JsonObject validConfig = createValidConfig();

    configurationComponent
        .initialize(vertx, validConfig)
        .onSuccess(
            v -> {
              testContext.verify(
                  () -> {
                    // 验证配置已存储到共享数据
                    var sharedData = vertx.sharedData().getLocalMap("local");
                    assertNotNull(sharedData.get("globalConfig"), "全局配置应该已存储");
                    assertNotNull(sharedData.get("server"), "服务器配置应该已存储");
                    testContext.completeNow();
                  });
            })
        .onFailure(testContext::failNow);
  }

  @Test
  @DisplayName("测试配置验证 - 缺少服务器配置时自动创建默认配置")
  void testConfigurationValidationMissingServerUsesDefaults(VertxTestContext testContext) {
    JsonObject configWithoutServer = new JsonObject().put("datasources", new JsonObject());

    configurationComponent
        .initialize(vertx, configWithoutServer)
        .onSuccess(
            v -> {
              testContext.verify(
                  () -> {
                    // 验证自动创建了默认服务器配置
                    JsonObject serverConfig = configWithoutServer.getJsonObject("server");
                    assertNotNull(serverConfig, "应该自动创建服务器配置");
                    assertEquals(8080, serverConfig.getInteger("port"), "默认端口应该是8080");
                    assertEquals("0.0.0.0", serverConfig.getString("host"), "默认host应该是0.0.0.0");
                    LOGGER.info("Server configuration created with defaults");
                    testContext.completeNow();
                  });
            })
        .onFailure(testContext::failNow);
  }

  @Test
  @DisplayName("测试配置验证 - 缺少端口配置时使用默认端口")
  void testConfigurationValidationMissingPortUsesDefault(VertxTestContext testContext) {
    JsonObject configWithoutPort =
        new JsonObject()
            .put("server", new JsonObject().put("host", "0.0.0.0"))
            .put("datasources", new JsonObject());

    configurationComponent
        .initialize(vertx, configWithoutPort)
        .onSuccess(
            v -> {
              testContext.verify(
                  () -> {
                    // 验证自动使用默认端口
                    JsonObject serverConfig = configWithoutPort.getJsonObject("server");
                    assertNotNull(serverConfig, "服务器配置应该存在");
                    assertEquals(8080, serverConfig.getInteger("port"), "默认端口应该是8080");
                    LOGGER.info("Server port set to default 8080");
                    testContext.completeNow();
                  });
            })
        .onFailure(testContext::failNow);
  }

  @Test
  @DisplayName("测试扫描路径自动检测")
  void testAutoDetectScanPaths(VertxTestContext testContext) {
    JsonObject config =
        new JsonObject()
            .put("server", new JsonObject().put("port", 8080).put("host", "0.0.0.0"))
            .put("datasources", new JsonObject());

    configurationComponent
        .initialize(vertx, config)
        .onSuccess(
            v -> {
              testContext.verify(
                  () -> {
                    // 验证扫描路径已设置
                    var sharedData = vertx.sharedData().getLocalMap("local");
                    var customConfig = (JsonObject) sharedData.get("customConfig");
                    assertNotNull(customConfig, "自定义配置应该已设置");
                    assertTrue(customConfig.containsKey("baseLocations"), "应该包含扫描路径");
                    testContext.completeNow();
                  });
            })
        .onFailure(testContext::failNow);
  }

  @Test
  @DisplayName("测试预配置扫描路径")
  void testPreConfiguredScanPaths(VertxTestContext testContext) {
    JsonObject config =
        new JsonObject()
            .put("server", new JsonObject().put("port", 8080).put("host", "0.0.0.0"))
            .put("datasources", new JsonObject())
            .put("custom", new JsonObject().put("baseLocations", "com.example.test"));

    configurationComponent
        .initialize(vertx, config)
        .onSuccess(
            v -> {
              testContext.verify(
                  () -> {
                    var sharedData = vertx.sharedData().getLocalMap("local");
                    var customConfig = (JsonObject) sharedData.get("customConfig");
                    assertEquals("com.example.test", customConfig.getString("baseLocations"));
                    testContext.completeNow();
                  });
            })
        .onFailure(testContext::failNow);
  }

  @Test
  @DisplayName("测试组件停止")
  void testComponentStop(VertxTestContext testContext) {
    configurationComponent
        .initialize(vertx, createValidConfig())
        .compose(v -> configurationComponent.stop())
        .onSuccess(
            v -> {
              testContext.verify(
                  () -> {
                    LOGGER.info("Configuration component stopped successfully");
                    testContext.completeNow();
                  });
            })
        .onFailure(testContext::failNow);
  }

  @Test
  @DisplayName("测试优先级")
  void testPriority() {
    assertEquals(10, configurationComponent.getPriority(), "配置组件应该有最高优先级");
  }

  @Test
  @DisplayName("测试ConfigResolver获取")
  void testGetConfigResolver(VertxTestContext testContext) {
    JsonObject config = createValidConfig();

    configurationComponent
        .initialize(vertx, config)
        .onSuccess(
            v -> {
              testContext.verify(
                  () -> {
                    assertNotNull(configurationComponent.getConfigResolver(), 
                        "ConfigResolver应该已初始化");
                    testContext.completeNow();
                  });
            })
        .onFailure(testContext::failNow);
  }

  @Test
  @DisplayName("测试ConfigBinder获取")
  void testGetConfigBinder(VertxTestContext testContext) {
    JsonObject config = createValidConfig();

    configurationComponent
        .initialize(vertx, config)
        .onSuccess(
            v -> {
              testContext.verify(
                  () -> {
                    assertNotNull(configurationComponent.getConfigBinder(), 
                        "ConfigBinder应该已初始化");
                    testContext.completeNow();
                  });
            })
        .onFailure(testContext::failNow);
  }

  @Test
  @DisplayName("测试获取全局配置")
  void testGetGlobalConfig(VertxTestContext testContext) {
    JsonObject config = createValidConfig();

    configurationComponent
        .initialize(vertx, config)
        .onSuccess(
            v -> {
              testContext.verify(
                  () -> {
                    JsonObject globalConfig = configurationComponent.getGlobalConfig();
                    assertNotNull(globalConfig, "全局配置应该存在");
                    assertTrue(globalConfig.containsKey("server"), "应该包含server配置");
                    testContext.completeNow();
                  });
            })
        .onFailure(testContext::failNow);
  }

  @Test
  @DisplayName("测试旧格式dataSource配置兼容")
  void testLegacyDataSourceConfig(VertxTestContext testContext) {
    JsonObject legacyConfig = new JsonObject()
        .put("server", new JsonObject().put("port", 8080).put("host", "0.0.0.0"))
        .put("dataSource", new JsonObject()
            .put("url", "jdbc:h2:mem:testdb")
            .put("username", "sa")
            .put("password", ""));

    configurationComponent
        .initialize(vertx, legacyConfig)
        .onSuccess(
            v -> {
              testContext.verify(
                  () -> {
                    // 通过ConfigResolver验证别名机制正确工作
                    // dataSource 是 datasources 的别名，所以两者都应该能获取到值
                    ConfigResolver resolver = configurationComponent.getConfigResolver();
                    assertNotNull(resolver, "ConfigResolver应该已初始化");
                    
                    // 通过 datasources 别名获取值（实际上是 dataSource）
                    JsonObject datasourcesByAlias = resolver.getJsonObject("datasources");
                    assertNotNull(datasourcesByAlias, "应该通过别名datasources获取到dataSource的值");
                    
                    // 验证原始 dataSource 键的值可以获取
                    JsonObject dataSourceDirect = resolver.getJsonObject("dataSource");
                    assertNotNull(dataSourceDirect, "应该能获取dataSource的值");
                    assertEquals("jdbc:h2:mem:testdb", dataSourceDirect.getString("url"), "URL应该正确");
                    
                    testContext.completeNow();
                  });
            })
        .onFailure(testContext::failNow);
  }

  /** 创建有效配置 */
  private JsonObject createValidConfig() {
    return new JsonObject()
        .put("server", new JsonObject().put("port", 8080).put("host", "0.0.0.0"))
        .put(
            "datasources",
            new JsonObject()
                .put(
                    "default",
                    new JsonObject()
                        .put("type", "h2")
                        .put("url", "jdbc:h2:mem:testdb")
                        .put("username", "sa")
                        .put("password", "")));
  }
}
