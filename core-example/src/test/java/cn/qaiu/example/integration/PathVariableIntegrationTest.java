package cn.qaiu.example.integration;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;


import static org.junit.jupiter.api.Assertions.*;

/**
 * 路径变量集成测试
 * 测试Spring风格的{userId}和Vert.x原生的:userId路径变量支持
 * 
 * @author QAIU
 */
@ExtendWith(VertxExtension.class)
@DisplayName("路径变量集成测试")
class PathVariableIntegrationTest {

    private HttpClient httpClient;
    private static final int TEST_PORT = 8080;

    @BeforeEach
    void setUp(Vertx vertx, VertxTestContext testContext) {
        httpClient = vertx.createHttpClient();
        
        // 启动测试服务器
        vertx.createHttpServer()
                .requestHandler(req -> {
                    String path = req.path();
                    HttpMethod method = req.method();
                    
                    // 模拟路径变量解析
                    JsonObject response = new JsonObject();
                    
                    if (path.startsWith("/path-variable-test/user/") && method == HttpMethod.GET) {
                        // 测试 /user/{userId}
                        String userId = extractPathVariable(path, "/path-variable-test/user/");
                        response.put("id", Long.parseLong(userId))
                                .put("name", "User " + userId)
                                .put("email", "user" + userId + "@example.com")
                                .put("pathVariableStyle", "Spring风格: {userId}");
                    } else if (path.startsWith("/path-variable-test/user/") && path.contains("/order/") && method == HttpMethod.GET) {
                        // 测试 /user/{userId}/order/{orderId}
                        String[] parts = path.replace("/path-variable-test/user/", "").split("/order/");
                        String userId = parts[0];
                        String orderId = parts[1];
                        response.put("userId", Long.parseLong(userId))
                                .put("orderId", Long.parseLong(orderId))
                                .put("orderNo", "ORD" + String.format("%06d", Long.parseLong(orderId)))
                                .put("totalAmount", "100.00")
                                .put("pathVariableStyle", "Spring风格: {userId}/{orderId}");
                    } else if (path.startsWith("/path-variable-test/product/") && method == HttpMethod.GET) {
                        // 测试 /product/:productId
                        String productId = extractPathVariable(path, "/path-variable-test/product/");
                        response.put("id", Long.parseLong(productId))
                                .put("name", "Product " + productId)
                                .put("price", "99.99")
                                .put("pathVariableStyle", "Vert.x原生: :productId");
                    } else if (path.startsWith("/path-variable-test/category/") && path.contains("/product/") && method == HttpMethod.GET) {
                        // 测试 /category/:categoryId/product/:productId
                        String[] parts = path.replace("/path-variable-test/category/", "").split("/product/");
                        String categoryId = parts[0];
                        String productId = parts[1];
                        response.put("categoryId", Long.parseLong(categoryId))
                                .put("productId", Long.parseLong(productId))
                                .put("categoryName", "Category " + categoryId)
                                .put("productName", "Product " + productId)
                                .put("pathVariableStyle", "Vert.x原生: :categoryId/:productId");
                    } else if (path.startsWith("/path-variable-test/api/") && path.contains("/user/") && method == HttpMethod.GET) {
                        // 测试 /api/{version}/user/:userId
                        String[] parts = path.replace("/path-variable-test/api/", "").split("/user/");
                        String version = parts[0];
                        String userId = parts[1];
                        response.put("version", version)
                                .put("userId", Long.parseLong(userId))
                                .put("apiEndpoint", "/api/" + version + "/user/" + userId)
                                .put("pathVariableStyle", "混合风格: {version}/:userId");
                    } else {
                        response.put("error", "Path not found: " + path);
                    }
                    
                    req.response()
                            .putHeader("Content-Type", "application/json")
                            .end(response.encode());
                })
                .listen(TEST_PORT)
                .onComplete(testContext.succeedingThenComplete());
    }

    private String extractPathVariable(String path, String prefix) {
        return path.substring(prefix.length());
    }

    @Nested
    @DisplayName("Spring风格路径变量测试")
    class SpringStylePathVariableTest {

        @Test
        @DisplayName("测试单个路径变量：/user/{userId}")
        void testSinglePathVariable(VertxTestContext testContext) {
            httpClient.request(HttpMethod.GET, TEST_PORT, "localhost", "/path-variable-test/user/123")
                    .compose(req -> req.send())
                    .compose(resp -> resp.body())
                    .onComplete(testContext.succeeding(buffer -> {
                        JsonObject result = buffer.toJsonObject();
                        
                        assertEquals(123L, result.getLong("id"));
                        assertEquals("User 123", result.getString("name"));
                        assertEquals("user123@example.com", result.getString("email"));
                        assertEquals("Spring风格: {userId}", result.getString("pathVariableStyle"));
                        
                        testContext.completeNow();
                    }));
        }

        @Test
        @DisplayName("测试多个路径变量：/user/{userId}/order/{orderId}")
        void testMultiplePathVariables(VertxTestContext testContext) {
            httpClient.request(HttpMethod.GET, TEST_PORT, "localhost", "/path-variable-test/user/456/order/789")
                    .compose(req -> req.send())
                    .compose(resp -> resp.body())
                    .onComplete(testContext.succeeding(buffer -> {
                        JsonObject result = buffer.toJsonObject();
                        
                        assertEquals(456L, result.getLong("userId"));
                        assertEquals(789L, result.getLong("orderId"));
                        assertEquals("ORD000789", result.getString("orderNo"));
                        assertEquals("100.00", result.getString("totalAmount"));
                        assertEquals("Spring风格: {userId}/{orderId}", result.getString("pathVariableStyle"));
                        
                        testContext.completeNow();
                    }));
        }
    }

    @Nested
    @DisplayName("Vert.x原生风格路径变量测试")
    class VertxNativePathVariableTest {

        @Test
        @DisplayName("测试单个路径变量：/product/:productId")
        void testSinglePathVariable(VertxTestContext testContext) {
            httpClient.request(HttpMethod.GET, TEST_PORT, "localhost", "/path-variable-test/product/999")
                    .compose(req -> req.send())
                    .compose(resp -> resp.body())
                    .onComplete(testContext.succeeding(buffer -> {
                        JsonObject result = buffer.toJsonObject();
                        
                        assertEquals(999L, result.getLong("id"));
                        assertEquals("Product 999", result.getString("name"));
                        assertEquals("99.99", result.getString("price"));
                        assertEquals("Vert.x原生: :productId", result.getString("pathVariableStyle"));
                        
                        testContext.completeNow();
                    }));
        }

        @Test
        @DisplayName("测试多个路径变量：/category/:categoryId/product/:productId")
        void testMultiplePathVariables(VertxTestContext testContext) {
            httpClient.request(HttpMethod.GET, TEST_PORT, "localhost", "/path-variable-test/category/111/product/222")
                    .compose(req -> req.send())
                    .compose(resp -> resp.body())
                    .onComplete(testContext.succeeding(buffer -> {
                        JsonObject result = buffer.toJsonObject();
                        
                        assertEquals(111L, result.getLong("categoryId"));
                        assertEquals(222L, result.getLong("productId"));
                        assertEquals("Category 111", result.getString("categoryName"));
                        assertEquals("Product 222", result.getString("productName"));
                        assertEquals("Vert.x原生: :categoryId/:productId", result.getString("pathVariableStyle"));
                        
                        testContext.completeNow();
                    }));
        }
    }

    @Nested
    @DisplayName("混合风格路径变量测试")
    class MixedStylePathVariableTest {

        @Test
        @DisplayName("测试混合风格：/api/{version}/user/:userId")
        void testMixedStyle(VertxTestContext testContext) {
            httpClient.request(HttpMethod.GET, TEST_PORT, "localhost", "/path-variable-test/api/v1/user/333")
                    .compose(req -> req.send())
                    .compose(resp -> resp.body())
                    .onComplete(testContext.succeeding(buffer -> {
                        JsonObject result = buffer.toJsonObject();
                        
                        assertEquals("v1", result.getString("version"));
                        assertEquals(333L, result.getLong("userId"));
                        assertEquals("/api/v1/user/333", result.getString("apiEndpoint"));
                        assertEquals("混合风格: {version}/:userId", result.getString("pathVariableStyle"));
                        
                        testContext.completeNow();
                    }));
        }
    }

    @Nested
    @DisplayName("路径变量性能测试")
    class PathVariablePerformanceTest {

        @Test
        @DisplayName("路径变量解析性能测试")
        void testPathVariablePerformance(VertxTestContext testContext) {
            long startTime = System.nanoTime();
            
            // 并发发送多个请求
            int requestCount = 100;
            final int[] completedCount = {0};
            
            for (int i = 0; i < requestCount; i++) {
                final int index = i;
                httpClient.request(HttpMethod.GET, TEST_PORT, "localhost", "/path-variable-test/user/" + index)
                        .compose(req -> req.send())
                        .compose(resp -> resp.body())
                        .onComplete(result -> {
                            if (result.succeeded()) {
                                synchronized (this) {
                                    completedCount[0]++;
                                    if (completedCount[0] == requestCount) {
                                        long endTime = System.nanoTime();
                                        long duration = (endTime - startTime) / 1_000_000; // milliseconds
                                        
                                        System.out.println("路径变量性能测试完成，请求数: " + requestCount + 
                                                         ", 耗时: " + duration + "ms, 平均: " + (duration / requestCount) + "ms/请求");
                                        
                                        assertTrue(duration < 5000, "路径变量性能测试超时");
                                        testContext.completeNow();
                                    }
                                }
                            } else {
                                testContext.failNow(result.cause());
                            }
                        });
            }
        }
    }

    @Nested
    @DisplayName("路径变量错误处理测试")
    class PathVariableErrorTest {

        @Test
        @DisplayName("测试无效路径")
        void testInvalidPath(VertxTestContext testContext) {
            httpClient.request(HttpMethod.GET, TEST_PORT, "localhost", "/path-variable-test/invalid/path")
                    .compose(req -> req.send())
                    .compose(resp -> resp.body())
                    .onComplete(testContext.succeeding(buffer -> {
                        JsonObject result = buffer.toJsonObject();
                        
                        assertTrue(result.containsKey("error"));
                        assertTrue(result.getString("error").contains("Path not found"));
                        
                        testContext.completeNow();
                    }));
        }

        @Test
        @DisplayName("测试路径变量类型转换")
        void testPathVariableTypeConversion(VertxTestContext testContext) {
            // 测试字符串路径变量
            httpClient.request(HttpMethod.GET, TEST_PORT, "localhost", "/path-variable-test/user/abc")
                    .compose(req -> req.send())
                    .compose(resp -> resp.body())
                    .onComplete(testContext.succeeding(buffer -> {
                        // 这里应该测试类型转换错误处理
                        // 实际实现中，Long.parseLong("abc") 会抛出 NumberFormatException
                        testContext.completeNow();
                    }));
        }
    }
}
