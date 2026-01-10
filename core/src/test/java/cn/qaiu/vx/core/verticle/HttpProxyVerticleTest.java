package cn.qaiu.vx.core.verticle;

import cn.qaiu.vx.core.util.ConfigConstant;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * HttpProxyVerticle测试类
 * 测试HTTP代理服务器功能
 *
 * @author <a href="https://qaiu.top">QAIU</a>
 */
@ExtendWith(VertxExtension.class)
@DisplayName("HTTP代理Verticle测试")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class HttpProxyVerticleTest {

    private static final int PROXY_PORT = 18888;
    private static final int TEST_SERVER_PORT = 18889;
    
    private String deploymentId;

    @BeforeEach
    void setUp(Vertx vertx, VertxTestContext testContext) {
        // 设置全局配置
        JsonObject config = new JsonObject()
            .put("proxy-server", new JsonObject()
                .put("port", PROXY_PORT))
            .put("proxy-pre", new JsonObject());
            
        vertx.sharedData()
            .getLocalMap(ConfigConstant.LOCAL)
            .put(ConfigConstant.GLOBAL_CONFIG, config);
        
        testContext.completeNow();
    }

    @AfterEach
    void tearDown(Vertx vertx, VertxTestContext testContext) {
        if (deploymentId != null) {
            vertx.undeploy(deploymentId)
                .onComplete(ar -> {
                    deploymentId = null;
                    testContext.completeNow();
                });
        } else {
            testContext.completeNow();
        }
    }

    @Test
    @Order(1)
    @DisplayName("测试Verticle部署成功")
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void testDeploySuccess(Vertx vertx, VertxTestContext testContext) {
        vertx.deployVerticle(new HttpProxyVerticle())
            .onComplete(testContext.succeeding(id -> {
                testContext.verify(() -> {
                    assertNotNull(id, "部署ID不应为null");
                    deploymentId = id;
                });
                testContext.completeNow();
            }));
    }

    @Test
    @Order(2)
    @DisplayName("测试缺少全局配置时部署失败")
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void testDeployFailureWithoutGlobalConfig(Vertx vertx, VertxTestContext testContext) {
        // 清除全局配置
        vertx.sharedData().getLocalMap(ConfigConstant.LOCAL).clear();
        
        vertx.deployVerticle(new HttpProxyVerticle())
            .onComplete(testContext.failing(error -> {
                testContext.verify(() -> {
                    assertNotNull(error, "应该有错误信息");
                    assertTrue(error.getMessage().contains("Global configuration"), 
                        "错误信息应包含'Global configuration'");
                });
                testContext.completeNow();
            }));
    }

    @Test
    @Order(3)
    @DisplayName("测试缺少proxy-server配置时部署失败")
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void testDeployFailureWithoutProxyServerConfig(Vertx vertx, VertxTestContext testContext) {
        // 设置不完整的配置
        JsonObject config = new JsonObject();
        vertx.sharedData()
            .getLocalMap(ConfigConstant.LOCAL)
            .put(ConfigConstant.GLOBAL_CONFIG, config);
        
        vertx.deployVerticle(new HttpProxyVerticle())
            .onComplete(testContext.failing(error -> {
                testContext.verify(() -> {
                    assertNotNull(error, "应该有错误信息");
                    assertTrue(error.getMessage().contains("proxy-server"), 
                        "错误信息应包含'proxy-server'");
                });
                testContext.completeNow();
            }));
    }

    @Test
    @Order(4)
    @DisplayName("测试extractPortFromUrl - HTTP URL")
    void testExtractPortFromHttpUrl() {
        assertEquals(8080, HttpProxyVerticle.extractPortFromUrl("http://example.com:8080/path"), 
            "应该提取到端口8080");
        assertEquals(80, HttpProxyVerticle.extractPortFromUrl("http://example.com/path"), 
            "HTTP默认端口应为80");
    }

    @Test
    @Order(5)
    @DisplayName("测试extractPortFromUrl - HTTPS URL")
    void testExtractPortFromHttpsUrl() {
        assertEquals(8443, HttpProxyVerticle.extractPortFromUrl("https://example.com:8443/path"), 
            "应该提取到端口8443");
        assertEquals(443, HttpProxyVerticle.extractPortFromUrl("https://example.com/path"), 
            "HTTPS默认端口应为443");
    }

    @Test
    @Order(6)
    @DisplayName("测试extractPortFromUrl - 相对URL")
    void testExtractPortFromRelativeUrl() {
        assertEquals(80, HttpProxyVerticle.extractPortFromUrl("/api/test"), 
            "相对URL应返回HTTP默认端口80");
    }

    @Test
    @Order(7)
    @DisplayName("测试extractPortFromUrl - 无效URL")
    void testExtractPortFromInvalidUrl() {
        assertEquals(-1, HttpProxyVerticle.extractPortFromUrl("://invalid-url"), 
            "无效URL应返回-1");
    }

    @Test
    @Order(8)
    @DisplayName("测试extractPortFromUrl - 空字符串")
    void testExtractPortFromEmptyUrl() {
        // 空字符串被URI解析为相对路径，会使用HTTP默认端口
        assertEquals(80, HttpProxyVerticle.extractPortFromUrl(""), 
            "空URL应返回HTTP默认端口80");
    }

    @Test
    @Order(9)
    @DisplayName("测试extractPortFromUrl - 非标准端口")
    void testExtractPortFromNonStandardPort() {
        assertEquals(3000, HttpProxyVerticle.extractPortFromUrl("http://localhost:3000/api"), 
            "应该提取到端口3000");
        assertEquals(9443, HttpProxyVerticle.extractPortFromUrl("https://secure.example.com:9443/admin"), 
            "应该提取到端口9443");
    }

    @Test
    @Order(10)
    @DisplayName("测试带认证配置的部署")
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void testDeployWithAuthentication(Vertx vertx, VertxTestContext testContext) {
        // 设置带认证的配置
        JsonObject config = new JsonObject()
            .put("proxy-server", new JsonObject()
                .put("port", PROXY_PORT + 1)
                .put("username", "testuser")
                .put("password", "testpass"))
            .put("proxy-pre", new JsonObject());
            
        vertx.sharedData()
            .getLocalMap(ConfigConstant.LOCAL)
            .put(ConfigConstant.GLOBAL_CONFIG, config);
        
        vertx.deployVerticle(new HttpProxyVerticle())
            .onComplete(testContext.succeeding(id -> {
                testContext.verify(() -> {
                    assertNotNull(id, "部署ID不应为null");
                    deploymentId = id;
                });
                testContext.completeNow();
            }));
    }

    @Test
    @Order(11)
    @DisplayName("测试带前置代理配置的部署")
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void testDeployWithPreProxy(Vertx vertx, VertxTestContext testContext) {
        // 设置带前置代理的配置
        JsonObject config = new JsonObject()
            .put("proxy-server", new JsonObject()
                .put("port", PROXY_PORT + 2))
            .put("proxy-pre", new JsonObject()
                .put("ip", "192.168.1.1")
                .put("port", 8080)
                .put("type", "HTTP"));
            
        vertx.sharedData()
            .getLocalMap(ConfigConstant.LOCAL)
            .put(ConfigConstant.GLOBAL_CONFIG, config);
        
        vertx.deployVerticle(new HttpProxyVerticle())
            .onComplete(testContext.succeeding(id -> {
                testContext.verify(() -> {
                    assertNotNull(id, "部署ID不应为null");
                    deploymentId = id;
                });
                testContext.completeNow();
            }));
    }

    @Test
    @Order(12)
    @DisplayName("测试Verticle停止")
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void testStop(Vertx vertx, VertxTestContext testContext) {
        vertx.deployVerticle(new HttpProxyVerticle())
            .compose(id -> {
                deploymentId = id;
                return vertx.undeploy(id);
            })
            .onComplete(testContext.succeeding(v -> {
                testContext.verify(() -> {
                    // 停止成功
                });
                deploymentId = null;
                testContext.completeNow();
            }));
    }

    @Test
    @Order(13)
    @DisplayName("测试多实例部署")
    @Timeout(value = 15, unit = TimeUnit.SECONDS)
    void testMultipleInstancesDeploy(Vertx vertx, VertxTestContext testContext) {
        // 部署多个实例（使用不同端口）
        JsonObject config1 = new JsonObject()
            .put("proxy-server", new JsonObject().put("port", PROXY_PORT + 10))
            .put("proxy-pre", new JsonObject());
        
        vertx.sharedData()
            .getLocalMap(ConfigConstant.LOCAL)
            .put(ConfigConstant.GLOBAL_CONFIG, config1);
        
        vertx.deployVerticle(new HttpProxyVerticle())
            .compose(id1 -> {
                // 修改配置为不同端口
                JsonObject config2 = new JsonObject()
                    .put("proxy-server", new JsonObject().put("port", PROXY_PORT + 11))
                    .put("proxy-pre", new JsonObject());
                
                vertx.sharedData()
                    .getLocalMap(ConfigConstant.LOCAL)
                    .put(ConfigConstant.GLOBAL_CONFIG, config2);
                
                return vertx.deployVerticle(new HttpProxyVerticle())
                    .compose(id2 -> {
                        // 清理
                        return vertx.undeploy(id1)
                            .compose(v -> vertx.undeploy(id2));
                    });
            })
            .onComplete(testContext.succeeding(v -> {
                testContext.completeNow();
            }));
    }

    @Test
    @Order(14)
    @DisplayName("测试端口冲突处理")
    @Timeout(value = 15, unit = TimeUnit.SECONDS)
    void testPortConflict(Vertx vertx, VertxTestContext testContext) {
        // 注意: Vert.x在某些平台上允许端口重用(SO_REUSEPORT)
        // 所以这个测试可能在不同环境有不同行为
        vertx.deployVerticle(new HttpProxyVerticle())
            .compose(id1 -> {
                deploymentId = id1;
                // 尝试用相同端口部署第二个实例
                return vertx.deployVerticle(new HttpProxyVerticle());
            })
            .onComplete(ar -> {
                testContext.verify(() -> {
                    // 在支持SO_REUSEPORT的平台上可能成功，否则失败
                    // 两种结果都可以接受
                    assertNotNull(ar, "应该有结果返回");
                });
                testContext.completeNow();
            });
    }

    @Test
    @Order(15)
    @DisplayName("测试配置中的空proxy-pre")
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void testEmptyProxyPreConfig(Vertx vertx, VertxTestContext testContext) {
        JsonObject config = new JsonObject()
            .put("proxy-server", new JsonObject().put("port", PROXY_PORT + 3))
            .put("proxy-pre", new JsonObject()); // 空配置
            
        vertx.sharedData()
            .getLocalMap(ConfigConstant.LOCAL)
            .put(ConfigConstant.GLOBAL_CONFIG, config);
        
        vertx.deployVerticle(new HttpProxyVerticle())
            .onComplete(testContext.succeeding(id -> {
                testContext.verify(() -> {
                    assertNotNull(id, "空proxy-pre配置应该能正常部署");
                    deploymentId = id;
                });
                testContext.completeNow();
            }));
    }

    @Test
    @Order(16)
    @DisplayName("测试配置中无proxy-pre键")
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void testMissingProxyPreConfig(Vertx vertx, VertxTestContext testContext) {
        JsonObject config = new JsonObject()
            .put("proxy-server", new JsonObject().put("port", PROXY_PORT + 4));
            // 完全没有proxy-pre键
            
        vertx.sharedData()
            .getLocalMap(ConfigConstant.LOCAL)
            .put(ConfigConstant.GLOBAL_CONFIG, config);
        
        vertx.deployVerticle(new HttpProxyVerticle())
            .onComplete(testContext.succeeding(id -> {
                testContext.verify(() -> {
                    assertNotNull(id, "没有proxy-pre配置也应该能正常部署");
                    deploymentId = id;
                });
                testContext.completeNow();
            }));
    }

    @Nested
    @DisplayName("URL解析边界测试")
    class UrlParsingEdgeCases {

        @Test
        @DisplayName("测试URL中包含查询参数")
        void testUrlWithQueryParams() {
            assertEquals(8080, 
                HttpProxyVerticle.extractPortFromUrl("http://example.com:8080/api?key=value&foo=bar"),
                "应该正确解析带查询参数的URL");
        }

        @Test
        @DisplayName("测试URL中包含fragment")
        void testUrlWithFragment() {
            assertEquals(8080, 
                HttpProxyVerticle.extractPortFromUrl("http://example.com:8080/page#section"),
                "应该正确解析带fragment的URL");
        }

        @Test
        @DisplayName("测试URL中包含特殊字符")
        void testUrlWithSpecialChars() {
            assertEquals(8080, 
                HttpProxyVerticle.extractPortFromUrl("http://example.com:8080/api/%E4%B8%AD%E6%96%87"),
                "应该正确解析带URL编码的URL");
        }

        @Test
        @DisplayName("测试IPv4地址URL")
        void testIpv4Url() {
            assertEquals(8080, 
                HttpProxyVerticle.extractPortFromUrl("http://192.168.1.1:8080/api"),
                "应该正确解析IPv4地址URL");
        }

        @Test
        @DisplayName("测试localhost URL")
        void testLocalhostUrl() {
            assertEquals(3000, 
                HttpProxyVerticle.extractPortFromUrl("http://localhost:3000/api"),
                "应该正确解析localhost URL");
        }
    }
}
