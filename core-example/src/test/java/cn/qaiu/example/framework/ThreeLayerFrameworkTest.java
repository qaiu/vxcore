package cn.qaiu.example.framework;

import cn.qaiu.vx.core.VXCoreApplication;
import cn.qaiu.vx.core.lifecycle.FrameworkLifecycleManager;
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
 * 基础三层框架测试
 * 测试Controller -> Service -> DAO 三层架构
 * 
 * @author <a href="https://qaiu.top">QAIU</a>
 */
@ExtendWith(VertxExtension.class)
@DisplayName("基础三层框架测试")
public class ThreeLayerFrameworkTest {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ThreeLayerFrameworkTest.class);
    
    private VXCoreApplication application;
    private Vertx vertx;
    
    @BeforeEach
    @DisplayName("初始化测试环境")
    void setUp(Vertx vertx, VertxTestContext testContext) {
        this.vertx = vertx;
        this.application = new VXCoreApplication();
        
        // 启动应用
        application.start(new String[]{"test"}, config -> {
            LOGGER.info("Test application started with config: {}", config.encodePrettily());
        }).onSuccess(v -> {
            testContext.completeNow();
        }).onFailure(testContext::failNow);
    }
    
    @AfterEach
    @DisplayName("清理测试环境")
    void tearDown(VertxTestContext testContext) {
        if (application != null) {
            application.stop()
                .onSuccess(v -> {
                    LOGGER.info("Test application stopped");
                    testContext.completeNow();
                })
                .onFailure(testContext::failNow);
        } else {
            testContext.completeNow();
        }
    }
    
    @Test
    @DisplayName("测试框架启动")
    void testFrameworkStartup(VertxTestContext testContext) {
        testContext.verify(() -> {
            assertTrue(application.isStarted(), "应用应该已启动");
            assertNotNull(application.getVertx(), "Vertx实例不应为空");
            assertNotNull(application.getGlobalConfig(), "全局配置不应为空");
            
            FrameworkLifecycleManager lifecycleManager = application.getLifecycleManager();
            assertEquals(FrameworkLifecycleManager.LifecycleState.STARTED, 
                       lifecycleManager.getState(), "框架状态应该是STARTED");
            
            LOGGER.info("Framework startup test passed");
            testContext.completeNow();
        });
    }
    
    @Test
    @DisplayName("测试配置加载")
    void testConfigurationLoading(VertxTestContext testContext) {
        JsonObject config = application.getGlobalConfig();
        
        testContext.verify(() -> {
            assertNotNull(config, "配置不应为空");
            
            // 验证服务器配置
            JsonObject server = config.getJsonObject("server");
            assertNotNull(server, "服务器配置不应为空");
            assertEquals(8080, server.getInteger("port"), "端口应该是8080");
            assertEquals("0.0.0.0", server.getString("host"), "主机应该是0.0.0.0");
            
            // 验证数据源配置
            JsonObject datasources = config.getJsonObject("datasources");
            assertNotNull(datasources, "数据源配置不应为空");
            assertTrue(datasources.containsKey("default"), "应该包含默认数据源");
            
            // 验证自定义配置
            JsonObject custom = config.getJsonObject("custom");
            assertNotNull(custom, "自定义配置不应为空");
            assertTrue(custom.containsKey("baseLocations"), "应该包含扫描路径");
            
            LOGGER.info("Configuration loading test passed");
            testContext.completeNow();
        });
    }
    
    @Test
    @DisplayName("测试组件初始化")
    void testComponentInitialization(VertxTestContext testContext) {
        FrameworkLifecycleManager lifecycleManager = application.getLifecycleManager();
        List<LifecycleComponent> components = lifecycleManager.getComponents();
        
        testContext.verify(() -> {
            assertNotNull(components, "组件列表不应为空");
            assertTrue(components.size() >= 5, "应该有至少5个组件");
            
            // 验证关键组件存在
            boolean hasConfigComponent = components.stream()
                .anyMatch(c -> c instanceof cn.qaiu.vx.core.lifecycle.ConfigurationComponent);
            assertTrue(hasConfigComponent, "应该包含配置组件");
            
            boolean hasDataSourceComponent = components.stream()
                .anyMatch(c -> c instanceof cn.qaiu.vx.core.lifecycle.DataSourceComponent);
            assertTrue(hasDataSourceComponent, "应该包含数据源组件");
            
            boolean hasServiceComponent = components.stream()
                .anyMatch(c -> c instanceof cn.qaiu.vx.core.lifecycle.ServiceRegistryComponent);
            assertTrue(hasServiceComponent, "应该包含服务注册组件");
            
            boolean hasRouterComponent = components.stream()
                .anyMatch(c -> c instanceof cn.qaiu.vx.core.lifecycle.RouterComponent);
            assertTrue(hasRouterComponent, "应该包含路由组件");
            
            LOGGER.info("Component initialization test passed");
            testContext.completeNow();
        });
    }
    
    @Test
    @DisplayName("测试数据源管理")
    void testDataSourceManagement(VertxTestContext testContext) {
        FrameworkLifecycleManager lifecycleManager = application.getLifecycleManager();
        cn.qaiu.vx.core.lifecycle.DataSourceComponent dataSourceComponent = lifecycleManager.getComponents().stream()
            .filter(c -> c instanceof cn.qaiu.vx.core.lifecycle.DataSourceComponent)
            .map(c -> (cn.qaiu.vx.core.lifecycle.DataSourceComponent) c)
            .findFirst()
            .orElse(null);
        
        testContext.verify(() -> {
            assertNotNull(dataSourceComponent, "数据源组件不应为空");
            
            cn.qaiu.vx.core.lifecycle.DataSourceManager dataSourceManager = dataSourceComponent.getDataSourceManager();
            assertNotNull(dataSourceManager, "数据源管理器不应为空");
            
            List<String> dataSourceNames = dataSourceManager.getDataSourceNames();
            assertNotNull(dataSourceNames, "数据源名称列表不应为空");
            
            LOGGER.info("Data source management test passed");
            testContext.completeNow();
        });
    }
    
    @Test
    @DisplayName("测试服务注册")
    void testServiceRegistration(VertxTestContext testContext) {
        FrameworkLifecycleManager lifecycleManager = application.getLifecycleManager();
        cn.qaiu.vx.core.lifecycle.ServiceRegistryComponent serviceComponent = lifecycleManager.getComponents().stream()
            .filter(c -> c instanceof cn.qaiu.vx.core.lifecycle.ServiceRegistryComponent)
            .map(c -> (cn.qaiu.vx.core.lifecycle.ServiceRegistryComponent) c)
            .findFirst()
            .orElse(null);
        
        testContext.verify(() -> {
            assertNotNull(serviceComponent, "服务组件不应为空");
            
            cn.qaiu.vx.core.component.ServiceComponent serviceComponent2 = serviceComponent.getServiceComponent();
            assertNotNull(serviceComponent2, "服务组件实例不应为空");
            
            cn.qaiu.vx.core.component.ServiceRegistry serviceRegistry = serviceComponent.getServiceRegistry();
            assertNotNull(serviceRegistry, "服务注册表不应为空");
            
            LOGGER.info("Service registration test passed");
            testContext.completeNow();
        });
    }
    
    @Test
    @DisplayName("测试路由管理")
    void testRouterManagement(VertxTestContext testContext) {
        FrameworkLifecycleManager lifecycleManager = application.getLifecycleManager();
        cn.qaiu.vx.core.lifecycle.RouterComponent routerComponent = lifecycleManager.getComponents().stream()
            .filter(c -> c instanceof cn.qaiu.vx.core.lifecycle.RouterComponent)
            .map(c -> (cn.qaiu.vx.core.lifecycle.RouterComponent) c)
            .findFirst()
            .orElse(null);
        
        testContext.verify(() -> {
            assertNotNull(routerComponent, "路由组件不应为空");
            
            io.vertx.ext.web.Router router = routerComponent.getRouter();
            assertNotNull(router, "路由器不应为空");
            
            cn.qaiu.vx.core.handlerfactory.RouterHandlerFactory routerHandlerFactory = routerComponent.getRouterHandlerFactory();
            assertNotNull(routerHandlerFactory, "路由处理器工厂不应为空");
            
            LOGGER.info("Router management test passed");
            testContext.completeNow();
        });
    }
    
    @Test
    @DisplayName("测试框架状态管理")
    void testFrameworkStateManagement(VertxTestContext testContext) {
        FrameworkLifecycleManager lifecycleManager = application.getLifecycleManager();
        
        testContext.verify(() -> {
            FrameworkLifecycleManager.LifecycleState state = lifecycleManager.getState();
            assertEquals(FrameworkLifecycleManager.LifecycleState.STARTED, state, 
                        "框架状态应该是STARTED");
            
            io.vertx.core.Vertx vertx = lifecycleManager.getVertx();
            assertNotNull(vertx, "Vertx实例不应为空");
            
            io.vertx.core.json.JsonObject config = lifecycleManager.getGlobalConfig();
            assertNotNull(config, "全局配置不应为空");
            
            LOGGER.info("Framework state management test passed");
            testContext.completeNow();
        });
    }
    
    @Test
    @DisplayName("测试应用重启")
    void testApplicationRestart(VertxTestContext testContext) {
        // 停止应用
        application.stop()
            .compose(v -> {
                // 重新启动
                return application.start(new String[]{"test"}, config -> {
                    LOGGER.info("Application restarted");
                });
            })
            .onSuccess(v -> {
                testContext.verify(() -> {
                    assertTrue(application.isStarted(), "重启后应用应该已启动");
                    assertEquals(FrameworkLifecycleManager.LifecycleState.STARTED, 
                               application.getLifecycleManager().getState(), 
                               "重启后框架状态应该是STARTED");
                    
                    LOGGER.info("Application restart test passed");
                    testContext.completeNow();
                });
            })
            .onFailure(testContext::failNow);
    }
    
    @Test
    @DisplayName("测试并发启动")
    void testConcurrentStartup(VertxTestContext testContext) {
        VXCoreApplication app1 = new VXCoreApplication();
        VXCoreApplication app2 = new VXCoreApplication();
        
        Future.all(
            app1.start(new String[]{"test1"}, config -> LOGGER.info("App1 started")),
            app2.start(new String[]{"test2"}, config -> LOGGER.info("App2 started"))
        ).onSuccess(v -> {
            testContext.verify(() -> {
                assertTrue(app1.isStarted(), "应用1应该已启动");
                assertTrue(app2.isStarted(), "应用2应该已启动");
                
                LOGGER.info("Concurrent startup test passed");
                testContext.completeNow();
            });
        }).onFailure(testContext::failNow);
    }
}