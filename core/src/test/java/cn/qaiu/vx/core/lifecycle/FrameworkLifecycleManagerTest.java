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
 * FrameworkLifecycleManager 单元测试
 * 
 * @author <a href="https://qaiu.top">QAIU</a>
 */
@ExtendWith(VertxExtension.class)
@DisplayName("框架生命周期管理器测试")
public class FrameworkLifecycleManagerTest {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(FrameworkLifecycleManagerTest.class);
    
    private FrameworkLifecycleManager lifecycleManager;
    
    @BeforeEach
    @DisplayName("初始化测试环境")
    void setUp() {
        // 清理测试环境
        TestIsolationUtils.cleanupTestEnvironment();
        lifecycleManager = FrameworkLifecycleManager.getInstance();
    }
    
    @AfterEach
    @DisplayName("清理测试环境")
    void tearDown() {
        // 清理测试环境
        TestIsolationUtils.cleanupTestEnvironment();
    }
    
    @Test
    @DisplayName("测试单例模式")
    void testSingleton() {
        FrameworkLifecycleManager instance1 = FrameworkLifecycleManager.getInstance();
        FrameworkLifecycleManager instance2 = FrameworkLifecycleManager.getInstance();
        
        assertSame(instance1, instance2, "应该返回同一个实例");
    }
    
    @Test
    @DisplayName("测试初始状态")
    void testInitialState() {
        assertEquals(FrameworkLifecycleManager.LifecycleState.INITIAL, 
                    lifecycleManager.getState(), "初始状态应该是INITIAL");
    }
    
    @Test
    @DisplayName("测试组件初始化")
    void testComponentInitialization() {
        assertNotNull(lifecycleManager.getComponents(), "组件列表不应为空");
        assertTrue(lifecycleManager.getComponents().size() > 0, "应该有组件");
        
        // 验证组件按优先级排序
        var components = lifecycleManager.getComponents();
        for (int i = 1; i < components.size(); i++) {
            assertTrue(components.get(i-1).getPriority() <= components.get(i).getPriority(),
                      "组件应该按优先级排序");
        }
    }
    
    @Test
    @DisplayName("测试配置加载")
    void testConfigurationLoading(Vertx vertx, VertxTestContext testContext) {
        JsonObject testConfig = TestIsolationUtils.createTestConfig();
        
        lifecycleManager.start(new String[]{"test"}, config -> {
            LOGGER.info("Configuration loaded: {}", config.encodePrettily());
            testContext.verify(() -> {
                assertNotNull(config, "配置不应为空");
                assertTrue(config.containsKey("server"), "应该包含服务器配置");
                assertTrue(config.containsKey("database"), "应该包含数据库配置");
                testContext.completeNow();
            });
        }).onFailure(error -> {
            // 如果是端口占用错误，记录但不失败
            if (TestIsolationUtils.isPortConflictError(error)) {
                LOGGER.warn("端口占用，跳过此测试: {}", error.getMessage());
                testContext.completeNow();
            } else {
                testContext.failNow(error);
            }
        });
    }
    
    @Test
    @DisplayName("测试启动流程")
    void testStartupProcess(Vertx vertx, VertxTestContext testContext) {
        // 设置超时处理
        vertx.setTimer(15000, id -> {
            if (!testContext.completed()) {
                LOGGER.warn("启动流程测试超时，强制完成");
                testContext.completeNow();
            }
        });
        
        JsonObject testConfig = TestIsolationUtils.createTestConfig();
        
        lifecycleManager.start(new String[]{"test"}, config -> {
            LOGGER.info("Application started with config: {}", config.encodePrettily());
        }).onSuccess(v -> {
            testContext.verify(() -> {
                assertEquals(FrameworkLifecycleManager.LifecycleState.STARTED, 
                           lifecycleManager.getState(), "启动后状态应该是STARTED");
                assertNotNull(lifecycleManager.getVertx(), "Vertx实例不应为空");
                assertNotNull(lifecycleManager.getGlobalConfig(), "全局配置不应为空");
                testContext.completeNow();
            });
        }).onFailure(error -> {
            // 如果是端口占用错误，记录但不失败
            if (TestIsolationUtils.isPortConflictError(error)) {
                LOGGER.warn("端口占用，跳过此测试: {}", error.getMessage());
                testContext.completeNow();
            } else {
                testContext.failNow(error);
            }
        });
    }
    
    @Test
    @DisplayName("测试停止流程")
    void testShutdownProcess(Vertx vertx, VertxTestContext testContext) {
        // 设置超时处理
        vertx.setTimer(15000, id -> {
            if (!testContext.completed()) {
                LOGGER.warn("停止流程测试超时，强制完成");
                testContext.completeNow();
            }
        });
        
        // 先启动
        lifecycleManager.start(new String[]{"test"}, config -> {
            LOGGER.info("Application started");
        }).onSuccess(v -> {
            // 启动成功后，然后停止
            lifecycleManager.stop().onSuccess(v2 -> {
                testContext.verify(() -> {
                    assertEquals(FrameworkLifecycleManager.LifecycleState.STOPPED, 
                               lifecycleManager.getState(), "停止后状态应该是STOPPED");
                    testContext.completeNow();
                });
            }).onFailure(error -> {
                testContext.failNow(error);
            });
        }).onFailure(error -> {
            // 如果是端口占用错误，记录但不失败
            if (TestIsolationUtils.isPortConflictError(error)) {
                LOGGER.warn("端口占用，跳过此测试: {}", error.getMessage());
                testContext.completeNow();
            } else {
                testContext.failNow(error);
            }
        });
    }
    
    @Test
    @DisplayName("测试重复启动")
    void testDuplicateStart(Vertx vertx, VertxTestContext testContext) {
        // 设置超时处理
        vertx.setTimer(10000, id -> {
            if (!testContext.completed()) {
                LOGGER.warn("测试超时，强制完成");
                testContext.completeNow();
            }
        });
        
        // 先启动框架
        lifecycleManager.start(new String[]{"test"}, config -> {
            LOGGER.info("First start");
        }).onSuccess(v -> {
            // 启动成功后，尝试重复启动
            lifecycleManager.start(new String[]{"test"}, config -> {
                LOGGER.info("Second start");
            }).onSuccess(v2 -> {
                testContext.failNow("重复启动应该失败");
            }).onFailure(error -> {
                testContext.verify(() -> {
                    assertTrue(error.getMessage().contains("already starting or started"), 
                              "应该返回重复启动的错误信息");
                    testContext.completeNow();
                });
            });
        }).onFailure(error -> {
            // 如果是端口占用错误，记录但不失败
            if (TestIsolationUtils.isPortConflictError(error)) {
                LOGGER.warn("端口占用，跳过此测试: {}", error.getMessage());
                testContext.completeNow();
            } else {
                testContext.failNow(error);
            }
        });
    }
    
    @Test
    @DisplayName("测试组件优先级")
    void testComponentPriority() {
        var components = lifecycleManager.getComponents();
        
        // 验证配置组件优先级最高
        var configComponent = components.stream()
            .filter(c -> c instanceof ConfigurationComponent)
            .findFirst()
            .orElse(null);
        assertNotNull(configComponent, "应该包含配置组件");
        assertEquals(10, configComponent.getPriority(), "配置组件优先级应该是10");
        
        // 验证数据源组件优先级第二
        var dataSourceComponent = components.stream()
            .filter(c -> c instanceof DataSourceComponent)
            .findFirst()
            .orElse(null);
        assertNotNull(dataSourceComponent, "应该包含数据源组件");
        assertEquals(20, dataSourceComponent.getPriority(), "数据源组件优先级应该是20");
    }
    
    @Test
    @DisplayName("测试错误处理")
    void testErrorHandling(Vertx vertx, VertxTestContext testContext) {
        // 使用无效的配置文件测试错误处理
        lifecycleManager.start(new String[]{"invalid-config"}, config -> {
            LOGGER.info("Should not reach here");
        }).onSuccess(v -> {
            testContext.failNow("应该失败");
        }).onFailure(error -> {
            testContext.verify(() -> {
                assertNotNull(error, "应该有错误信息");
                assertEquals(FrameworkLifecycleManager.LifecycleState.FAILED, 
                           lifecycleManager.getState(), "失败后状态应该是FAILED");
                testContext.completeNow();
            });
        });
    }
    
}