package cn.qaiu.example.test;

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
 * 启动序列测试
 * 测试应用启动的各个阶段和顺序
 * 
 * @author <a href="https://qaiu.top">QAIU</a>
 */
@ExtendWith(VertxExtension.class)
@DisplayName("启动序列测试")
public class StartupSequenceTest {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(StartupSequenceTest.class);
    
    private VXCoreApplication application;
    
    @BeforeEach
    void setUp(Vertx vertx, VertxTestContext testContext) {
        
        // 重置框架状态，确保测试隔离
        VXCoreApplication.resetForTesting();
        
        this.application = new VXCoreApplication();
        
        application.start(new String[]{"test"}, config -> {
            LOGGER.info("Startup sequence test application started");
        }).onSuccess(v -> {
            testContext.completeNow();
        }).onFailure(testContext::failNow);
    }
    
    @AfterEach
    void tearDown(VertxTestContext testContext) {
        if (application != null) {
            application.stop()
                .onSuccess(v -> testContext.completeNow())
                .onFailure(testContext::failNow);
        } else {
            testContext.completeNow();
        }
    }
    
    @Test
    @DisplayName("测试应用启动序列")
    void testApplicationStartupSequence(VertxTestContext testContext) {
        testContext.verify(() -> {
            assertTrue(application.isStarted(), "应用应该已启动");
            assertNotNull(application.getVertx(), "Vertx实例不应为空");
            assertNotNull(application.getGlobalConfig(), "全局配置不应为空");
            
            FrameworkLifecycleManager lifecycleManager = application.getLifecycleManager();
            assertEquals(FrameworkLifecycleManager.LifecycleState.STARTED, 
                       lifecycleManager.getState(), "框架状态应该是STARTED");
            
            LOGGER.info("启动序列测试通过");
            testContext.completeNow();
        });
    }
    
    @Test
    @DisplayName("测试配置加载顺序")
    void testConfigurationLoadingSequence(VertxTestContext testContext) {
        JsonObject config = application.getGlobalConfig();
        
        testContext.verify(() -> {
            assertNotNull(config, "配置不应为空");
            
            // 验证服务器配置
            JsonObject server = config.getJsonObject("server");
            assertNotNull(server, "服务器配置不应为空");
            assertEquals(8080, server.getInteger("port"), "端口应该是8080");
            
            // 验证数据源配置
            JsonObject datasources = config.getJsonObject("datasources");
            assertNotNull(datasources, "数据源配置不应为空");
            
            // 验证自定义配置
            JsonObject custom = config.getJsonObject("custom");
            assertNotNull(custom, "自定义配置不应为空");
            assertTrue(custom.containsKey("baseLocations"), "应该包含扫描路径");
            
            LOGGER.info("配置加载顺序测试通过");
            testContext.completeNow();
        });
    }
    
    @Test
    @DisplayName("测试组件初始化顺序")
    void testComponentInitializationSequence(VertxTestContext testContext) {
        FrameworkLifecycleManager lifecycleManager = application.getLifecycleManager();
        
        testContext.verify(() -> {
            assertNotNull(lifecycleManager, "生命周期管理器不应为空");
            
            // 验证关键组件存在
            assertTrue(lifecycleManager.getComponents().size() >= 5, "应该有至少5个组件");
            
            LOGGER.info("组件初始化顺序测试通过");
            testContext.completeNow();
        });
    }
    
    @Test
    @DisplayName("测试启动时间")
    void testStartupTime(VertxTestContext testContext) {
        long startTime = System.currentTimeMillis();
        
        VXCoreApplication testApp = new VXCoreApplication();
        testApp.start(new String[]{"test"}, config -> {
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            
            testContext.verify(() -> {
                assertTrue(duration < 10000, "启动时间应该小于10秒，实际: " + duration + "ms");
                LOGGER.info("启动时间测试通过，耗时: {}ms", duration);
                
                testApp.stop().onSuccess(v -> testContext.completeNow());
            });
        }).onFailure(testContext::failNow);
    }
}