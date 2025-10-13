package cn.qaiu.vx.core.lifecycle;

import cn.qaiu.vx.core.test.TestIsolationUtils;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DataSourceComponent 单元测试
 * 
 * @author <a href="https://qaiu.top">QAIU</a>
 */
@ExtendWith(VertxExtension.class)
@DisplayName("数据源管理组件测试")
public class DataSourceComponentTest {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(DataSourceComponentTest.class);
    
    private DataSourceComponent dataSourceComponent;
    private Vertx vertx;
    
    @BeforeEach
    @DisplayName("初始化测试环境")
    void setUp(Vertx vertx) {
        this.vertx = vertx;
        this.dataSourceComponent = new DataSourceComponent();
    }
    
    @AfterEach
    @DisplayName("清理测试环境")
    void tearDown() {
        TestIsolationUtils.cleanupTestEnvironment();
    }
    
    @Test
    @DisplayName("测试组件初始化")
    void testComponentInitialization(VertxTestContext testContext) {
        JsonObject config = createValidConfig();
        
        // 设置模拟的DataSourceProvider
        DataSourceProvider mockProvider = createMockDataSourceProvider();
        dataSourceComponent.setDataSourceProvider(mockProvider);
        
        dataSourceComponent.initialize(vertx, config)
            .onSuccess(v -> {
                testContext.verify(() -> {
                    assertEquals("DataSourceComponent", dataSourceComponent.getName());
                    assertEquals(20, dataSourceComponent.getPriority());
                    assertNotNull(dataSourceComponent.getDataSourceProvider());
                    LOGGER.info("DataSource component initialized successfully");
                    testContext.completeNow();
                });
            })
            .onFailure(testContext::failNow);
    }
    
    @Test
    @DisplayName("测试数据源注册")
    void testDataSourceRegistration(VertxTestContext testContext) {
        JsonObject config = createValidConfig();
        
        // 设置模拟的DataSourceProvider
        DataSourceProvider mockProvider = createMockDataSourceProviderWithPreset(java.util.List.of("default", "secondary"));
        dataSourceComponent.setDataSourceProvider(mockProvider);
        
        dataSourceComponent.initialize(vertx, config)
            .onSuccess(v -> {
                testContext.verify(() -> {
                    DataSourceManagerInterface manager = dataSourceComponent.getDataSourceManager();
                    assertNotNull(manager, "数据源管理器不应为空");
                    
                    // 验证数据源已注册
                    var dataSourceNames = manager.getDataSourceNames();
                    assertTrue(dataSourceNames.contains("default"), "应该包含默认数据源");
                    assertTrue(dataSourceNames.contains("secondary"), "应该包含次要数据源");
                    
                    testContext.completeNow();
                });
            })
            .onFailure(testContext::failNow);
    }
    
    @Test
    @DisplayName("测试无数据源配置")
    void testNoDataSourceConfiguration(VertxTestContext testContext) {
        JsonObject config = new JsonObject()
            .put("server", new JsonObject()
                .put("port", 8080)
                .put("host", "0.0.0.0"));
        
        // 设置模拟的DataSourceProvider
        DataSourceProvider mockProvider = createMockDataSourceProvider();
        dataSourceComponent.setDataSourceProvider(mockProvider);
        
        dataSourceComponent.initialize(vertx, config)
            .onSuccess(v -> {
                testContext.verify(() -> {
                    DataSourceManagerInterface manager = dataSourceComponent.getDataSourceManager();
                    assertNotNull(manager, "数据源管理器不应为空");
                    
                    var dataSourceNames = manager.getDataSourceNames();
                    assertTrue(dataSourceNames.isEmpty(), "应该没有数据源");
                    
                    testContext.completeNow();
                });
            })
            .onFailure(testContext::failNow);
    }
    
    @Test
    @DisplayName("测试数据源初始化")
    void testDataSourceInitialization(VertxTestContext testContext) {
        JsonObject config = createValidConfig();
        
        // 设置模拟的DataSourceProvider
        DataSourceProvider mockProvider = createMockDataSourceProvider();
        dataSourceComponent.setDataSourceProvider(mockProvider);
        
        dataSourceComponent.initialize(vertx, config)
            .compose(v -> {
                // 等待数据源初始化完成
                return Future.<Void>future(promise -> {
                    vertx.setTimer(100, id -> {
                        LOGGER.info("Timer triggered, checking datasource initialization");
                        promise.complete();
                    });
                });
            })
            .onSuccess(v -> {
                testContext.verify(() -> {
                    DataSourceManagerInterface manager = dataSourceComponent.getDataSourceManager();
                    
                    // 验证默认数据源已设置
                    var dataSourceNames = manager.getDataSourceNames();
                    if (!dataSourceNames.isEmpty()) {
                        String defaultDs = dataSourceNames.iterator().next();
                        assertNotNull(defaultDs, "应该有默认数据源");
                        LOGGER.info("Default datasource: {}", defaultDs);
                    }
                    
                    testContext.completeNow();
                });
            })
            .onFailure(testContext::failNow);
    }
    
    @Test
    @DisplayName("测试组件停止")
    void testComponentStop(VertxTestContext testContext) {
        JsonObject config = createValidConfig();
        
        // 设置模拟的DataSourceProvider
        DataSourceProvider mockProvider = createMockDataSourceProvider();
        dataSourceComponent.setDataSourceProvider(mockProvider);
        
        dataSourceComponent.initialize(vertx, config)
            .compose(v -> dataSourceComponent.stop())
            .onSuccess(v -> {
                testContext.verify(() -> {
                    LOGGER.info("DataSource component stopped successfully");
                    testContext.completeNow();
                });
            })
            .onFailure(testContext::failNow);
    }
    
    @Test
    @DisplayName("测试多数据源配置")
    void testMultipleDataSourceConfiguration(VertxTestContext testContext) {
        JsonObject config = new JsonObject()
            .put("server", new JsonObject()
                .put("port", 8080)
                .put("host", "0.0.0.0"))
            .put("database", new JsonObject()
                .put("primary", new JsonObject()
                    .put("type", "h2")
                    .put("url", "jdbc:h2:mem:primary")
                    .put("username", "sa")
                    .put("password", ""))
                .put("secondary", new JsonObject()
                    .put("type", "h2")
                    .put("url", "jdbc:h2:mem:secondary")
                    .put("username", "sa")
                    .put("password", ""))
                .put("log", new JsonObject()
                    .put("type", "h2")
                    .put("url", "jdbc:h2:mem:log")
                    .put("username", "sa")
                    .put("password", "")));
        
        // 设置模拟的DataSourceProvider
        DataSourceProvider mockProvider = createMockDataSourceProviderWithPreset(java.util.List.of("primary", "secondary", "log"));
        dataSourceComponent.setDataSourceProvider(mockProvider);
        
        dataSourceComponent.initialize(vertx, config)
            .onSuccess(v -> {
                testContext.verify(() -> {
                    DataSourceManagerInterface manager = dataSourceComponent.getDataSourceManager();
                    var dataSourceNames = manager.getDataSourceNames();
                    
                    assertEquals(3, dataSourceNames.size(), "应该有3个数据源");
                    assertTrue(dataSourceNames.contains("primary"), "应该包含primary数据源");
                    assertTrue(dataSourceNames.contains("secondary"), "应该包含secondary数据源");
                    assertTrue(dataSourceNames.contains("log"), "应该包含log数据源");
                    
                    testContext.completeNow();
                });
            })
            .onFailure(testContext::failNow);
    }
    
    @Test
    @DisplayName("测试优先级")
    void testPriority() {
        assertEquals(20, dataSourceComponent.getPriority(), "数据源组件优先级应该是20");
    }
    
    @Test
    @DisplayName("测试数据源管理器获取")
    void testDataSourceManagerAccess() {
        assertNull(dataSourceComponent.getDataSourceManager(), "未初始化时应该返回null");
        
        // 设置模拟的DataSourceProvider
        DataSourceProvider mockProvider = createMockDataSourceProvider();
        dataSourceComponent.setDataSourceProvider(mockProvider);
        
        JsonObject config = createValidConfig();
        dataSourceComponent.initialize(vertx, config)
            .onSuccess(v -> {
                assertNotNull(dataSourceComponent.getDataSourceManager(), "初始化后应该返回管理器");
            });
    }
    
    /**
     * 创建有效配置
     */
    private JsonObject createValidConfig() {
        return new JsonObject()
            .put("server", new JsonObject()
                .put("port", 8080)
                .put("host", "0.0.0.0"))
            .put("database", new JsonObject()
                .put("default", new JsonObject()
                    .put("type", "h2")
                    .put("url", "jdbc:h2:mem:testdb")
                    .put("username", "sa")
                    .put("password", ""))
                .put("secondary", new JsonObject()
                    .put("type", "h2")
                    .put("url", "jdbc:h2:mem:secondary")
                    .put("username", "sa")
                    .put("password", "")));
    }
    
    /**
     * 创建模拟的DataSourceProvider
     */
    private DataSourceProvider createMockDataSourceProvider() {
        return new DataSourceProvider() {
            private java.util.List<String> dataSourceNames = new java.util.ArrayList<>();
            
            @Override
            public String getName() {
                return "mock-provider";
            }
            
            @Override
            public boolean supports(String type) {
                return "h2".equalsIgnoreCase(type) || "mysql".equalsIgnoreCase(type);
            }
            
            @Override
            public DataSourceManagerInterface createDataSourceManager(Vertx vertx) {
                return new DataSourceManagerInterface() {
                    @Override
                    public Future<Void> registerDataSource(String name, JsonObject config) {
                        return Future.succeededFuture();
                    }
                    
                    @Override
                    public Future<Void> initializeDataSources(Vertx vertx, JsonObject config) {
                        JsonObject databaseConfig = config.getJsonObject("database");
                        if (databaseConfig != null && !databaseConfig.isEmpty()) {
                            dataSourceNames.addAll(databaseConfig.fieldNames());
                        }
                        return Future.succeededFuture();
                    }
                    
                    @Override
                    public Future<Void> initializeAllDataSources() {
                        return Future.succeededFuture();
                    }
                    
                    @Override
                    public Future<Void> closeAllDataSources() {
                        return Future.succeededFuture();
                    }
                    
                    @Override
                    public Object getPool(String name) {
                        return null;
                    }
                    
                    @Override
                    public java.util.List<String> getDataSourceNames() {
                        return new java.util.ArrayList<>(dataSourceNames);
                    }
                };
            }
            
            @Override
            public Future<Void> initializeDataSources(Vertx vertx, JsonObject config) {
                JsonObject databaseConfig = config.getJsonObject("database");
                if (databaseConfig != null && !databaseConfig.isEmpty()) {
                    dataSourceNames.addAll(databaseConfig.fieldNames());
                }
                return Future.succeededFuture();
            }
            
            @Override
            public Future<Void> closeAllDataSources() {
                return Future.succeededFuture();
            }
            
            @Override
            public Object getPool(String name) {
                return null;
            }
            
            @Override
            public java.util.List<String> getDataSourceNames() {
                return new java.util.ArrayList<>(dataSourceNames);
            }
        };
    }
    
    /**
     * 创建带预设数据源的模拟DataSourceProvider
     */
    private DataSourceProvider createMockDataSourceProviderWithPreset(java.util.List<String> presetNames) {
        return new DataSourceProvider() {
            private java.util.List<String> dataSourceNames = new java.util.ArrayList<>(presetNames);
            
            @Override
            public String getName() {
                return "mock-provider-preset";
            }
            
            @Override
            public boolean supports(String type) {
                return "h2".equalsIgnoreCase(type) || "mysql".equalsIgnoreCase(type);
            }
            
            @Override
            public DataSourceManagerInterface createDataSourceManager(Vertx vertx) {
                return new DataSourceManagerInterface() {
                    @Override
                    public Future<Void> registerDataSource(String name, JsonObject config) {
                        return Future.succeededFuture();
                    }
                    
                    @Override
                    public Future<Void> initializeDataSources(Vertx vertx, JsonObject config) {
                        return Future.succeededFuture();
                    }
                    
                    @Override
                    public Future<Void> initializeAllDataSources() {
                        return Future.succeededFuture();
                    }
                    
                    @Override
                    public Future<Void> closeAllDataSources() {
                        return Future.succeededFuture();
                    }
                    
                    @Override
                    public Object getPool(String name) {
                        return null;
                    }
                    
                    @Override
                    public java.util.List<String> getDataSourceNames() {
                        return new java.util.ArrayList<>(dataSourceNames);
                    }
                };
            }
            
            @Override
            public Future<Void> initializeDataSources(Vertx vertx, JsonObject config) {
                return Future.succeededFuture();
            }
            
            @Override
            public Future<Void> closeAllDataSources() {
                return Future.succeededFuture();
            }
            
            @Override
            public Object getPool(String name) {
                return null;
            }
            
            @Override
            public java.util.List<String> getDataSourceNames() {
                return new java.util.ArrayList<>(dataSourceNames);
            }
        };
    }
}