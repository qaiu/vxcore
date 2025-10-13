package cn.qaiu.db.datasource;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(VertxExtension.class)
@DisplayName("DataSourceManager数据源管理器测试")
class DataSourceManagerTest {

    private Vertx vertx;
    private DataSourceManager dataSourceManager;
    
    @BeforeEach
    void setUp(Vertx vertx) {
        this.vertx = vertx;
        this.dataSourceManager = DataSourceManager.getInstance(vertx);
    }
    
    @AfterEach
    void tearDown() {
        // 清理测试数据源
        if (dataSourceManager != null) {
            try {
                dataSourceManager.closeAllDataSources().toCompletionStage().toCompletableFuture().get(5, TimeUnit.SECONDS);
            } catch (Exception e) {
                // 忽略清理错误
            }
        }
    }

    @Nested
    @DisplayName("单例模式测试")
    class SingletonTest {

        @Test
        @DisplayName("获取单例实例测试")
        void testGetInstance(VertxTestContext testContext) {
            DataSourceManager instance1 = DataSourceManager.getInstance(vertx);
            DataSourceManager instance2 = DataSourceManager.getInstance(vertx);
            
            assertSame(instance1, instance2, "DataSourceManager应该是单例");
            testContext.completeNow();
        }

        @Test
        @DisplayName("不同Vertx实例获取相同单例测试")
        void testDifferentVertxInstances(VertxTestContext testContext) {
            Vertx vertx2 = Vertx.vertx();
            DataSourceManager instance1 = DataSourceManager.getInstance(vertx);
            DataSourceManager instance2 = DataSourceManager.getInstance(vertx2);
            
            assertSame(instance1, instance2, "不同Vertx实例应该返回相同的DataSourceManager（全局单例）");
            
            vertx2.close();
            testContext.completeNow();
        }
    }

    @Nested
    @DisplayName("数据源配置注册测试")
    class DataSourceRegistrationTest {

        @Test
        @DisplayName("注册数据源配置测试")
        void testRegisterDataSource(VertxTestContext testContext) {
            String name = "test-db";
            DataSourceConfig config = new DataSourceConfig();
            config.setName(name);
            config.setType("mysql");
            config.setUrl("jdbc:mysql://localhost:3306/test");
            config.setUsername("test");
            config.setPassword("test");

            dataSourceManager.registerDataSource(name, config)
                .onSuccess(v -> {
                    assertNotNull(dataSourceManager.getDataSourceConfig(name), "数据源配置应该已注册");
                    testContext.completeNow();
                })
                .onFailure(testContext::failNow);
        }

        @Test
        @DisplayName("从JsonObject注册数据源配置测试")
        void testRegisterDataSourceFromJson(VertxTestContext testContext) {
            String name = "test-db-json";
            JsonObject configJson = new JsonObject()
                .put("type", "mysql")
                .put("url", "jdbc:mysql://localhost:3306/test")
                .put("username", "test")
                .put("password", "test");

            dataSourceManager.registerDataSource(name, configJson)
                .onSuccess(v -> {
                    assertNotNull(dataSourceManager.getDataSourceConfig(name), "数据源配置应该已注册");
                    testContext.completeNow();
                })
                .onFailure(testContext::failNow);
        }

        @Test
        @DisplayName("重复注册数据源配置测试")
        void testRegisterDuplicateDataSource(VertxTestContext testContext) {
            String name = "duplicate-db";
            DataSourceConfig config1 = new DataSourceConfig();
            config1.setName(name);
            config1.setType("mysql");
            
            DataSourceConfig config2 = new DataSourceConfig();
            config2.setName(name);
            config2.setType("postgresql");

            dataSourceManager.registerDataSource(name, config1)
                .compose(v -> dataSourceManager.registerDataSource(name, config2))
                .onSuccess(v -> {
                    // 重复注册应该成功，但会覆盖之前的配置
                    assertNotNull(dataSourceManager.getDataSourceConfig(name), "数据源配置应该存在");
                    testContext.completeNow();
                })
                .onFailure(testContext::failNow);
        }
    }

    @Nested
    @DisplayName("数据源初始化测试")
    class DataSourceInitializationTest {

        @Test
        @DisplayName("初始化数据源测试")
        void testInitializeDataSource(VertxTestContext testContext) {
            String name = "init-db";
            DataSourceConfig config = new DataSourceConfig();
            config.setName(name);
            config.setType("h2");
            config.setUrl("jdbc:h2:mem:test");
            config.setUsername("sa");
            config.setPassword("");

            dataSourceManager.registerDataSource(name, config)
                .compose(v -> dataSourceManager.initializeDataSource(name))
                .onSuccess(v -> {
                    assertNotNull(dataSourceManager.getPool(name), "数据源应该已初始化");
                    testContext.completeNow();
                })
                .onFailure(testContext::failNow);
        }

        @Test
        @DisplayName("初始化不存在的数据源测试")
        void testInitializeNonExistentDataSource(VertxTestContext testContext) {
            String name = "non-existent-db";

            dataSourceManager.initializeDataSource(name)
                .onSuccess(v -> testContext.failNow("不应该成功初始化不存在的数据源"))
                .onFailure(error -> {
                    assertTrue(error.getMessage().contains("DataSource config not found"), 
                        "应该返回配置未找到的错误");
                    testContext.completeNow();
                });
        }

        @Test
        @DisplayName("重复初始化数据源测试")
        void testReinitializeDataSource(VertxTestContext testContext) {
            String name = "reinit-db";
            DataSourceConfig config = new DataSourceConfig();
            config.setName(name);
            config.setType("h2");
            config.setUrl("jdbc:h2:mem:test2");
            config.setUsername("sa");
            config.setPassword("");

            dataSourceManager.registerDataSource(name, config)
                .compose(v -> dataSourceManager.initializeDataSource(name))
                .compose(v -> dataSourceManager.initializeDataSource(name)) // 重复初始化
                .onSuccess(v -> {
                    assertNotNull(dataSourceManager.getPool(name), "数据源应该已初始化");
                    testContext.completeNow();
                })
                .onFailure(testContext::failNow);
        }
    }

    @Nested
    @DisplayName("数据源获取测试")
    class DataSourceRetrievalTest {

        @Test
        @DisplayName("获取数据源连接池测试")
        void testGetDataSourcePool(VertxTestContext testContext) {
            String name = "pool-db";
            DataSourceConfig config = new DataSourceConfig();
            config.setName(name);
            config.setType("h2");
            config.setUrl("jdbc:h2:mem:test3");
            config.setUsername("sa");
            config.setPassword("");

            dataSourceManager.registerDataSource(name, config)
                .compose(v -> dataSourceManager.initializeDataSource(name))
                .onSuccess(v -> {
                    var pool = dataSourceManager.getPool(name);
                    assertNotNull(pool, "连接池不应该为null");
                    testContext.completeNow();
                })
                .onFailure(testContext::failNow);
        }

        @Test
        @DisplayName("获取JooqExecutor测试")
        void testGetJooqExecutor(VertxTestContext testContext) {
            String name = "executor-db";
            DataSourceConfig config = new DataSourceConfig();
            config.setName(name);
            config.setType("h2");
            config.setUrl("jdbc:h2:mem:test4");
            config.setUsername("sa");
            config.setPassword("");

            dataSourceManager.registerDataSource(name, config)
                .compose(v -> dataSourceManager.initializeDataSource(name))
                .onSuccess(v -> {
                    var executor = dataSourceManager.getExecutor(name);
                    assertNotNull(executor, "JooqExecutor不应该为null");
                    testContext.completeNow();
                })
                .onFailure(testContext::failNow);
        }

        @Test
        @DisplayName("获取默认数据源测试")
        void testGetDefaultDataSource(VertxTestContext testContext) {
            String name = "default-db";
            DataSourceConfig config = new DataSourceConfig();
            config.setName(name);
            config.setType("h2");
            config.setUrl("jdbc:h2:mem:test5");
            config.setUsername("sa");
            config.setPassword("");

            dataSourceManager.registerDataSource(name, config)
                .compose(v -> dataSourceManager.initializeDataSource(name))
                .onSuccess(v -> {
                    dataSourceManager.setDefaultDataSource(name);
                    assertNotNull(dataSourceManager.getDefaultPool(), "默认数据源应该设置正确");
                    testContext.completeNow();
                })
                .onFailure(testContext::failNow);
        }
    }

    @Nested
    @DisplayName("数据源管理测试")
    class DataSourceManagementTest {

        @Test
        @DisplayName("检查数据源是否存在测试")
        void testHasDataSource(VertxTestContext testContext) {
            String name = "check-db";
            DataSourceConfig config = new DataSourceConfig();
            config.setName(name);
            config.setType("h2");
            config.setUrl("jdbc:h2:mem:test6");
            config.setUsername("sa");
            config.setPassword("");

            assertNull(dataSourceManager.getDataSourceConfig(name), "数据源配置不应该存在");

            dataSourceManager.registerDataSource(name, config)
                .onSuccess(v -> {
                    assertNotNull(dataSourceManager.getDataSourceConfig(name), "数据源配置应该存在");
                    testContext.completeNow();
                })
                .onFailure(testContext::failNow);
        }

        @Test
        @DisplayName("关闭数据源测试")
        void testCloseDataSource(VertxTestContext testContext) {
            String name = "close-db";
            DataSourceConfig config = new DataSourceConfig();
            config.setName(name);
            config.setType("h2");
            config.setUrl("jdbc:h2:mem:test7");
            config.setUsername("sa");
            config.setPassword("");

            dataSourceManager.registerDataSource(name, config)
                .compose(v -> dataSourceManager.initializeDataSource(name))
                .compose(v -> dataSourceManager.closeDataSource(name))
                .onSuccess(v -> {
                    assertNull(dataSourceManager.getPool(name), "数据源应该已关闭");
                    testContext.completeNow();
                })
                .onFailure(testContext::failNow);
        }

        @Test
        @DisplayName("关闭所有数据源测试")
        void testCloseAllDataSources(VertxTestContext testContext) {
            String name1 = "close-all-db1";
            String name2 = "close-all-db2";
            
            DataSourceConfig config1 = new DataSourceConfig();
            config1.setName(name1);
            config1.setType("h2");
            config1.setUrl("jdbc:h2:mem:test8");
            config1.setUsername("sa");
            config1.setPassword("");
            
            DataSourceConfig config2 = new DataSourceConfig();
            config2.setName(name2);
            config2.setType("h2");
            config2.setUrl("jdbc:h2:mem:test9");
            config2.setUsername("sa");
            config2.setPassword("");

            dataSourceManager.registerDataSource(name1, config1)
                .compose(v -> dataSourceManager.registerDataSource(name2, config2))
                .compose(v -> dataSourceManager.initializeDataSource(name1))
                .compose(v -> dataSourceManager.initializeDataSource(name2))
                .compose(v -> dataSourceManager.closeAllDataSources())
                .onSuccess(v -> {
                    assertNull(dataSourceManager.getPool(name1), "数据源1应该已关闭");
                    assertNull(dataSourceManager.getPool(name2), "数据源2应该已关闭");
                    testContext.completeNow();
                })
                .onFailure(testContext::failNow);
        }
    }

    @Nested
    @DisplayName("错误处理测试")
    class ErrorHandlingTest {

        @Test
        @DisplayName("无效数据源配置测试")
        void testInvalidDataSourceConfig(VertxTestContext testContext) {
            String name = "invalid-db";
            DataSourceConfig config = new DataSourceConfig();
            config.setName(name);
            config.setType("invalid-type");
            // 缺少必要的配置

            dataSourceManager.registerDataSource(name, config)
                .compose(v -> dataSourceManager.initializeDataSource(name))
                .onSuccess(v -> testContext.failNow("不应该成功初始化无效配置"))
                .onFailure(error -> {
                    testContext.verify(() -> {
                        assertNotNull(error, "应该返回错误");
                        assertTrue(error.getMessage().contains("No provider found") || 
                                 error.getMessage().contains("invalid-type"), 
                                 "错误信息应该包含provider未找到或invalid-type: " + error.getMessage());
                    });
                    testContext.completeNow();
                });
        }

        @Test
        @DisplayName("不支持的数据库类型测试")
        void testUnsupportedDatabaseType(VertxTestContext testContext) {
            String name = "unsupported-db";
            DataSourceConfig config = new DataSourceConfig();
            config.setName(name);
            config.setType("unsupported-type");

            dataSourceManager.registerDataSource(name, config)
                .compose(v -> dataSourceManager.initializeDataSource(name))
                .onSuccess(v -> testContext.failNow("不应该成功初始化不支持的数据库类型"))
                .onFailure(error -> {
                    assertTrue(error.getMessage().contains("No provider found"), 
                        "应该返回提供者未找到的错误");
                    testContext.completeNow();
                });
        }
    }

    @Nested
    @DisplayName("性能测试")
    class PerformanceTest {

        @Test
        @DisplayName("并发注册数据源测试")
        void testConcurrentDataSourceRegistration(VertxTestContext testContext) {
            int count = 10;
            java.util.List<Future<Void>> futures = new java.util.ArrayList<>();
            
            for (int i = 0; i < count; i++) {
                String name = "concurrent-db-" + i;
                DataSourceConfig config = new DataSourceConfig();
                config.setName(name);
                config.setType("h2");
                config.setUrl("jdbc:h2:mem:test" + i);
                config.setUsername("sa");
                config.setPassword("");
                
                futures.add(dataSourceManager.registerDataSource(name, config));
            }
            
            Future.all(futures)
                .onSuccess(v -> {
                    for (int i = 0; i < count; i++) {
                        String name = "concurrent-db-" + i;
                        assertNotNull(dataSourceManager.getDataSourceConfig(name), 
                            "数据源配置应该存在: " + name);
                    }
                    testContext.completeNow();
                })
                .onFailure(testContext::failNow);
        }

        @Test
        @DisplayName("大量数据源管理性能测试")
        void testLargeScaleDataSourceManagement(VertxTestContext testContext) {
            int count = 100;
            long startTime = System.currentTimeMillis();
            
            java.util.List<Future<Void>> futures = new java.util.ArrayList<>();
            for (int i = 0; i < count; i++) {
                String name = "scale-db-" + i;
                DataSourceConfig config = new DataSourceConfig();
                config.setName(name);
                config.setType("h2");
                config.setUrl("jdbc:h2:mem:scale" + i);
                config.setUsername("sa");
                config.setPassword("");
                
                futures.add(dataSourceManager.registerDataSource(name, config));
            }
            
            Future.all(futures)
                .onSuccess(v -> {
                    long endTime = System.currentTimeMillis();
                    long duration = endTime - startTime;
                    
                    System.out.println("大量数据源注册性能测试完成，耗时: " + duration + "ms");
                    assertTrue(duration < 5000, "大量数据源注册性能测试超时");
                    
                    for (int i = 0; i < count; i++) {
                        String name = "scale-db-" + i;
                        assertNotNull(dataSourceManager.getDataSourceConfig(name), 
                            "数据源配置应该存在: " + name);
                    }
                    testContext.completeNow();
                })
                .onFailure(testContext::failNow);
        }
    }
}
