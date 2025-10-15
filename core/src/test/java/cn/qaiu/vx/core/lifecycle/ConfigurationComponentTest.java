package cn.qaiu.vx.core.lifecycle;

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

import static org.junit.jupiter.api.Assertions.*;

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
        
        configurationComponent.initialize(vertx, config)
            .onSuccess(v -> {
                testContext.verify(() -> {
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
        
        configurationComponent.initialize(vertx, validConfig)
            .onSuccess(v -> {
                testContext.verify(() -> {
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
    @DisplayName("测试配置验证 - 缺少服务器配置")
    void testConfigurationValidationMissingServer(VertxTestContext testContext) {
        JsonObject invalidConfig = new JsonObject()
            .put("datasources", new JsonObject());
        
        configurationComponent.initialize(vertx, invalidConfig)
            .onSuccess(v -> {
                testContext.failNow("应该失败");
            })
            .onFailure(error -> {
                testContext.verify(() -> {
                    assertTrue(error.getMessage().contains("Server configuration is required"));
                    testContext.completeNow();
                });
            });
    }
    
    @Test
    @DisplayName("测试配置验证 - 缺少端口配置")
    void testConfigurationValidationMissingPort(VertxTestContext testContext) {
        JsonObject invalidConfig = new JsonObject()
            .put("server", new JsonObject().put("host", "0.0.0.0"))
            .put("datasources", new JsonObject());
        
        configurationComponent.initialize(vertx, invalidConfig)
            .onSuccess(v -> {
                testContext.failNow("应该失败");
            })
            .onFailure(error -> {
                testContext.verify(() -> {
                    assertTrue(error.getMessage().contains("Server port is required"));
                    testContext.completeNow();
                });
            });
    }
    
    @Test
    @DisplayName("测试扫描路径自动检测")
    void testAutoDetectScanPaths(VertxTestContext testContext) {
        JsonObject config = new JsonObject()
            .put("server", new JsonObject()
                .put("port", 8080)
                .put("host", "0.0.0.0"))
            .put("datasources", new JsonObject());
        
        configurationComponent.initialize(vertx, config)
            .onSuccess(v -> {
                testContext.verify(() -> {
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
        JsonObject config = new JsonObject()
            .put("server", new JsonObject()
                .put("port", 8080)
                .put("host", "0.0.0.0"))
            .put("datasources", new JsonObject())
            .put("custom", new JsonObject()
                .put("baseLocations", "com.example.test"));
        
        configurationComponent.initialize(vertx, config)
            .onSuccess(v -> {
                testContext.verify(() -> {
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
        configurationComponent.initialize(vertx, createValidConfig())
            .compose(v -> configurationComponent.stop())
            .onSuccess(v -> {
                testContext.verify(() -> {
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
    
    /**
     * 创建有效配置
     */
    private JsonObject createValidConfig() {
        return new JsonObject()
            .put("server", new JsonObject()
                .put("port", 8080)
                .put("host", "0.0.0.0"))
            .put("datasources", new JsonObject()
                .put("default", new JsonObject()
                    .put("type", "h2")
                    .put("url", "jdbc:h2:mem:testdb")
                    .put("username", "sa")
                    .put("password", "")));
    }
}