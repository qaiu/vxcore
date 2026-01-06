package cn.qaiu.vx.core.aop;

import static org.junit.jupiter.api.Assertions.*;

import cn.qaiu.vx.core.lifecycle.LifecycleComponent;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.concurrent.TimeUnit;

/**
 * AopComponent 单元测试
 *
 * @author <a href="https://qaiu.top">QAIU</a>
 */
@ExtendWith(VertxExtension.class)
@DisplayName("AOP组件测试")
public class AopComponentTest {

    private AopComponent aopComponent;

    @BeforeEach
    void setUp() {
        aopComponent = new AopComponent();
        AspectRegistry.getInstance().clear();
    }

    @AfterEach
    void tearDown() {
        AspectRegistry.getInstance().clear();
    }

    @Test
    @DisplayName("测试实现 LifecycleComponent 接口")
    void testImplementsLifecycleComponent() {
        assertTrue(aopComponent instanceof LifecycleComponent, 
            "AopComponent 应该实现 LifecycleComponent 接口");
    }

    @Test
    @DisplayName("测试组件名称")
    void testGetName() {
        assertEquals("AopComponent", aopComponent.getName());
    }

    @Test
    @DisplayName("测试组件优先级")
    void testGetPriority() {
        assertEquals(12, aopComponent.getPriority(), "优先级应该是 12");
    }

    @Test
    @DisplayName("测试初始化 - AOP启用")
    void testInitializeWithAopEnabled(Vertx vertx, VertxTestContext testContext) throws InterruptedException {
        JsonObject config = new JsonObject()
            .put("aop", new JsonObject()
                .put("enabled", true)
                .put("scan-packages", new JsonArray().add("cn.qaiu.vx.core.aop")));

        aopComponent.initialize(vertx, config)
            .onComplete(testContext.succeeding(v -> {
                assertTrue(aopComponent.isEnabled(), "AOP 应该被启用");
                testContext.completeNow();
            }));

        assertTrue(testContext.awaitCompletion(5, TimeUnit.SECONDS));
    }

    @Test
    @DisplayName("测试初始化 - AOP禁用")
    void testInitializeWithAopDisabled(Vertx vertx, VertxTestContext testContext) throws InterruptedException {
        JsonObject config = new JsonObject()
            .put("aop", new JsonObject()
                .put("enabled", false));

        aopComponent.initialize(vertx, config)
            .onComplete(testContext.succeeding(v -> {
                assertFalse(aopComponent.isEnabled(), "AOP 应该被禁用");
                testContext.completeNow();
            }));

        assertTrue(testContext.awaitCompletion(5, TimeUnit.SECONDS));
    }

    @Test
    @DisplayName("测试初始化 - 默认配置")
    void testInitializeWithDefaultConfig(Vertx vertx, VertxTestContext testContext) throws InterruptedException {
        JsonObject config = new JsonObject();

        aopComponent.initialize(vertx, config)
            .onComplete(testContext.succeeding(v -> {
                assertTrue(aopComponent.isEnabled(), "默认应该启用 AOP");
                testContext.completeNow();
            }));

        assertTrue(testContext.awaitCompletion(5, TimeUnit.SECONDS));
    }

    @Test
    @DisplayName("测试启动")
    void testStart(Vertx vertx, VertxTestContext testContext) throws InterruptedException {
        JsonObject config = new JsonObject()
            .put("aop", new JsonObject().put("enabled", true));

        aopComponent.initialize(vertx, config)
            .compose(v -> aopComponent.start())
            .onComplete(testContext.succeeding(v -> {
                testContext.completeNow();
            }));

        assertTrue(testContext.awaitCompletion(5, TimeUnit.SECONDS));
    }

    @Test
    @DisplayName("测试停止")
    void testStop(Vertx vertx, VertxTestContext testContext) throws InterruptedException {
        JsonObject config = new JsonObject()
            .put("aop", new JsonObject().put("enabled", true));

        aopComponent.initialize(vertx, config)
            .compose(v -> aopComponent.start())
            .compose(v -> aopComponent.stop())
            .onComplete(testContext.succeeding(v -> {
                testContext.completeNow();
            }));

        assertTrue(testContext.awaitCompletion(5, TimeUnit.SECONDS));
    }

    @Test
    @DisplayName("测试获取 AOP 配置")
    void testGetAopConfig(Vertx vertx, VertxTestContext testContext) throws InterruptedException {
        JsonObject aopConfig = new JsonObject()
            .put("enabled", true)
            .put("custom-key", "custom-value");
        JsonObject config = new JsonObject().put("aop", aopConfig);

        aopComponent.initialize(vertx, config)
            .onComplete(testContext.succeeding(v -> {
                JsonObject returnedConfig = aopComponent.getAopConfig();
                assertNotNull(returnedConfig);
                assertEquals("custom-value", returnedConfig.getString("custom-key"));
                testContext.completeNow();
            }));

        assertTrue(testContext.awaitCompletion(5, TimeUnit.SECONDS));
    }

    @Test
    @DisplayName("测试禁用时启动不执行任何操作")
    void testStartWhenDisabled(Vertx vertx, VertxTestContext testContext) throws InterruptedException {
        JsonObject config = new JsonObject()
            .put("aop", new JsonObject().put("enabled", false));

        aopComponent.initialize(vertx, config)
            .compose(v -> aopComponent.start())
            .onComplete(testContext.succeeding(v -> {
                assertFalse(aopComponent.isEnabled());
                testContext.completeNow();
            }));

        assertTrue(testContext.awaitCompletion(5, TimeUnit.SECONDS));
    }
}
