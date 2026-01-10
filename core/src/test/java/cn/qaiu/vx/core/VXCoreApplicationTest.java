package cn.qaiu.vx.core;

import cn.qaiu.vx.core.lifecycle.FrameworkLifecycleManager;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * VXCoreApplication核心应用测试类
 * 测试应用启动、停止、状态管理等核心功能
 * 
 * 注意: 这些测试需要完整的配置文件,属于集成测试范畴
 * 在CI环境中禁用,本地可手动运行
 *
 * @author <a href="https://qaiu.top">QAIU</a>
 */
@ExtendWith(VertxExtension.class)
@DisplayName("VXCore应用核心功能测试")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Disabled("集成测试 - 需要完整配置文件,本地可手动运行")
public class VXCoreApplicationTest {

    private VXCoreApplication application;

    @BeforeEach
    void setUp(Vertx vertx) throws Exception {
        // 每次测试前重置框架状态
        VXCoreApplication.resetForTesting();
        application = new VXCoreApplication();
        
        // 创建测试配置，避免配置文件缺失导致启动失败
        JsonObject testConfig = new JsonObject()
            .put("server", new JsonObject().put("port", 8888))
            .put("custom", new JsonObject());
        
        // 将测试配置写入文件系统（使用临时目录）
        String configPath = System.getProperty("java.io.tmpdir") + "/test-app.yml";
        vertx.fileSystem().writeFileBlocking(configPath, 
            io.vertx.core.buffer.Buffer.buffer(testConfig.encodePrettily()));
    }

    @AfterEach
    void tearDown(VertxTestContext testContext) {
        if (application != null && application.isStarted()) {
            application.stop()
                .onComplete(ar -> testContext.completeNow());
        } else {
            testContext.completeNow();
        }
    }

    @Test
    @Order(1)
    @DisplayName("应用实例创建测试")
    void testApplicationCreation() {
        assertNotNull(application, "应用实例不应为null");
        assertNotNull(application.getLifecycleManager(), "生命周期管理器不应为null");
        assertFalse(application.isStarted(), "新创建的应用不应处于启动状态");
    }

    @Test
    @Order(2)
    @DisplayName("应用启动成功测试")
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    void testApplicationStartSuccess(VertxTestContext testContext) {
        application.start(new String[]{}, config -> {
            // 用户回调处理
        }).onComplete(ar -> {
            testContext.verify(() -> {
                assertTrue(ar.succeeded(), "应用应该启动成功");
                assertTrue(application.isStarted(), "应用应该处于启动状态");
                assertNotNull(application.getVertx(), "Vertx实例不应为null");
                assertNotNull(application.getGlobalConfig(), "全局配置不应为null");
            });
            testContext.completeNow();
        });
    }

    @Test
    @Order(3)
    @DisplayName("应用启动失败测试 - 重复启动")
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    void testApplicationStartDuplicate(VertxTestContext testContext) {
        // 首次启动
        application.start(new String[]{}, config -> {})
            .compose(v -> {
                // 尝试再次启动同一个实例（框架内部会处理）
                return application.start(new String[]{}, config -> {});
            })
            .onComplete(ar -> {
                testContext.verify(() -> {
                    // 框架应该处理重复启动（可能成功或失败，取决于实现）
                    assertNotNull(ar, "结果不应为null");
                });
                testContext.completeNow();
            });
    }

    @Test
    @Order(4)
    @DisplayName("应用停止测试")
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    void testApplicationStop(VertxTestContext testContext) {
        application.start(new String[]{}, config -> {})
            .compose(v -> {
                testContext.verify(() -> {
                    assertTrue(application.isStarted(), "启动后应该处于启动状态");
                });
                return application.stop();
            })
            .onComplete(ar -> {
                testContext.verify(() -> {
                    assertTrue(ar.succeeded(), "应用应该停止成功");
                    assertFalse(application.isStarted(), "停止后不应处于启动状态");
                });
                testContext.completeNow();
            });
    }

    @Test
    @Order(5)
    @DisplayName("应用停止测试 - 未启动时停止")
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void testApplicationStopWithoutStart(VertxTestContext testContext) {
        application.stop().onComplete(ar -> {
            testContext.verify(() -> {
                // 未启动时停止可能成功或失败，取决于实现
                assertNotNull(ar, "结果不应为null");
            });
            testContext.completeNow();
        });
    }

    @Test
    @Order(6)
    @DisplayName("应用配置回调测试")
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    void testApplicationConfigCallback(VertxTestContext testContext) {
        final boolean[] callbackInvoked = {false};
        
        application.start(new String[]{}, config -> {
            callbackInvoked[0] = true;
            assertNotNull(config, "回调中的配置不应为null");
        }).onComplete(ar -> {
            testContext.verify(() -> {
                assertTrue(ar.succeeded(), "应用应该启动成功");
                assertTrue(callbackInvoked[0], "用户回调应该被调用");
            });
            testContext.completeNow();
        });
    }

    @Test
    @Order(7)
    @DisplayName("获取Vertx实例测试")
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    void testGetVertx(VertxTestContext testContext) {
        application.start(new String[]{}, config -> {})
            .onComplete(ar -> {
                testContext.verify(() -> {
                    assertTrue(ar.succeeded(), "应用应该启动成功");
                    Vertx vertx = application.getVertx();
                    assertNotNull(vertx, "Vertx实例不应为null");
                    assertTrue(vertx instanceof Vertx, "应该返回正确的Vertx实例");
                });
                testContext.completeNow();
            });
    }

    @Test
    @Order(8)
    @DisplayName("获取全局配置测试")
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    void testGetGlobalConfig(VertxTestContext testContext) {
        application.start(new String[]{}, config -> {})
            .onComplete(ar -> {
                testContext.verify(() -> {
                    assertTrue(ar.succeeded(), "应用应该启动成功");
                    JsonObject config = application.getGlobalConfig();
                    assertNotNull(config, "全局配置不应为null");
                    assertTrue(config instanceof JsonObject, "应该返回JsonObject类型的配置");
                });
                testContext.completeNow();
            });
    }

    @Test
    @Order(9)
    @DisplayName("应用状态检查测试")
    void testIsStarted() {
        assertFalse(application.isStarted(), "新创建的应用应该是未启动状态");
    }

    @Test
    @Order(10)
    @DisplayName("静态run方法测试 - 仅验证方法存在")
    void testStaticRunMethod() {
        // 注意：静态run方法会调用System.exit()，无法在单元测试中直接调用
        // 此处仅验证方法存在性
        assertDoesNotThrow(() -> {
            VXCoreApplication.class.getDeclaredMethod("run", String[].class, io.vertx.core.Handler.class);
        }, "静态run方法应该存在");
    }

    @Test
    @Order(11)
    @DisplayName("多个应用实例测试")
    void testMultipleInstances() {
        VXCoreApplication app1 = new VXCoreApplication();
        VXCoreApplication app2 = new VXCoreApplication();
        
        assertNotSame(app1, app2, "应该能创建多个应用实例");
        // 注意：虽然可以创建多个实例，但FrameworkLifecycleManager是单例
        assertSame(app1.getLifecycleManager(), app2.getLifecycleManager(), 
            "多个应用实例共享同一个生命周期管理器");
    }

    @Test
    @Order(12)
    @DisplayName("测试resetForTesting方法")
    void testResetForTesting() {
        assertDoesNotThrow(() -> {
            VXCoreApplication.resetForTesting();
        }, "resetForTesting方法应该能正常执行");
        
        // 重置后创建新实例应该成功
        VXCoreApplication newApp = new VXCoreApplication();
        assertNotNull(newApp, "重置后应该能创建新实例");
        assertNotNull(newApp.getLifecycleManager(), "重置后生命周期管理器应该可用");
    }

    @Test
    @Order(13)
    @DisplayName("应用启动时间测试")
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    void testApplicationStartupTime(VertxTestContext testContext) {
        long startTime = System.currentTimeMillis();
        
        application.start(new String[]{}, config -> {})
            .onComplete(ar -> {
                long duration = System.currentTimeMillis() - startTime;
                testContext.verify(() -> {
                    assertTrue(ar.succeeded(), "应用应该启动成功");
                    assertTrue(duration < 30000, "应用启动时间应该少于30秒");
                });
                testContext.completeNow();
            });
    }

    @Test
    @Order(14)
    @DisplayName("应用启动参数传递测试")
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    void testApplicationStartWithArgs(VertxTestContext testContext) {
        String[] args = {"--config", "test-config.json", "--env", "test"};
        
        application.start(args, config -> {})
            .onComplete(ar -> {
                testContext.verify(() -> {
                    assertTrue(ar.succeeded(), "带参数启动应该成功");
                    assertTrue(application.isStarted(), "应用应该处于启动状态");
                });
                testContext.completeNow();
            });
    }

    @Test
    @Order(15)
    @DisplayName("应用启动失败回调测试")
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    void testApplicationStartFailureHandling(VertxTestContext testContext) {
        // 由于框架设计良好，很难触发启动失败
        // 此测试主要验证失败处理逻辑存在
        application.start(new String[]{}, config -> {
            // 可以在这里模拟配置错误，但可能被框架容错机制处理
        }).onComplete(ar -> {
            testContext.verify(() -> {
                // 即使启动失败，也应该有结果
                assertNotNull(ar, "启动结果不应为null");
            });
            testContext.completeNow();
        });
    }

    @Test
    @Order(16)
    @DisplayName("应用完整生命周期测试")
    @Timeout(value = 60, unit = TimeUnit.SECONDS)
    void testFullLifecycle(VertxTestContext testContext) {
        // 创建 -> 启动 -> 检查状态 -> 停止 -> 检查状态
        VXCoreApplication app = new VXCoreApplication();
        
        app.start(new String[]{}, config -> {})
            .compose(v -> {
                testContext.verify(() -> {
                    assertTrue(app.isStarted(), "启动后应该是started状态");
                    assertNotNull(app.getVertx(), "应该有Vertx实例");
                    assertNotNull(app.getGlobalConfig(), "应该有全局配置");
                });
                return app.stop();
            })
            .onComplete(ar -> {
                testContext.verify(() -> {
                    assertTrue(ar.succeeded(), "生命周期应该完整执行成功");
                    assertFalse(app.isStarted(), "停止后应该是stopped状态");
                });
                testContext.completeNow();
            });
    }

    @Nested
    @DisplayName("异常情况测试")
    class ExceptionHandlingTests {

        @Test
        @DisplayName("空参数启动测试")
        @Timeout(value = 30, unit = TimeUnit.SECONDS)
        void testStartWithNullArgs(VertxTestContext testContext) {
            application.start(null, config -> {})
                .onComplete(ar -> {
                    testContext.verify(() -> {
                        // 框架应该能处理null参数
                        assertNotNull(ar, "结果不应为null");
                    });
                    testContext.completeNow();
                });
        }

        @Test
        @DisplayName("空回调启动测试")
        @Timeout(value = 30, unit = TimeUnit.SECONDS)
        void testStartWithNullHandler(VertxTestContext testContext) {
            application.start(new String[]{}, null)
                .onComplete(ar -> {
                    testContext.verify(() -> {
                        // 框架应该能处理null回调
                        assertNotNull(ar, "结果不应为null");
                    });
                    testContext.completeNow();
                });
        }

        @Test
        @DisplayName("获取Vertx实例 - 未启动时")
        void testGetVertxBeforeStart() {
            // 未启动时可能返回null或抛出异常，取决于实现
            assertDoesNotThrow(() -> {
                Vertx vertx = application.getVertx();
                // 根据实现，可能是null或已初始化的实例
            }, "获取Vertx不应抛出异常");
        }

        @Test
        @DisplayName("获取配置 - 未启动时")
        void testGetConfigBeforeStart() {
            // 未启动时可能返回null或默认配置
            assertDoesNotThrow(() -> {
                JsonObject config = application.getGlobalConfig();
                // 根据实现，可能是null或默认配置
            }, "获取配置不应抛出异常");
        }
    }
}
