package cn.qaiu.example.integration;

import cn.qaiu.example.SimpleExampleApplication;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import static org.junit.jupiter.api.Assertions.*;

/**
 * HTTP请求流程集成测试
 * 测试从HTTP请求到响应的完整流程
 * 
 * @author QAIU
 */
@ExtendWith(VertxExtension.class)
@DisplayName("HTTP请求流程集成测试")
class HttpRequestFlowIntegrationTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpRequestFlowIntegrationTest.class);
    
    private HttpClient httpClient;
    private SimpleExampleApplication application;
    
    @BeforeEach
    void setUp(Vertx vertx, VertxTestContext testContext) {
        
        // 创建HTTP客户端
        HttpClientOptions options = new HttpClientOptions()
                .setConnectTimeout(5000)
                .setIdleTimeout(30)
                .setKeepAlive(true);
        this.httpClient = vertx.createHttpClient(options);
        
        // 初始化应用
        this.application = new SimpleExampleApplication(vertx);
        
        // 初始化路由工厂
        // this.routerFactory = new RouterHandlerFactory("/api"); // 暂时未使用
        
        // 启动应用
        vertx.deployVerticle(application)
                .onSuccess(deploymentId -> {
                    LOGGER.info("✅ Application started successfully, deployment ID: {}", deploymentId);
                    testContext.completeNow();
                })
                .onFailure(testContext::failNow);
    }
    
    @Nested
    @DisplayName("基础HTTP请求测试")
    class BasicHttpRequestTest {
        
        @Test
        @DisplayName("测试GET请求 - 健康检查")
        void testGetHealthCheck(VertxTestContext testContext) {
            LOGGER.info("Testing GET /api/system/health");
            
            httpClient.request(HttpMethod.GET, 8080, "localhost", "/api/system/health")
                    .compose(request -> request.send())
                    .onSuccess(response -> {
                        testContext.verify(() -> {
                            assertEquals(200, response.statusCode(), "健康检查应该返回200状态码");
                            LOGGER.info("✅ Health check test passed: {}", response.statusCode());
                        });
                        
                        response.body()
                                .onSuccess(body -> {
                                    testContext.verify(() -> {
                                        assertNotNull(body, "响应体不应为空");
                                        LOGGER.info("Health check response: {}", body.toString());
                                    });
                                    testContext.completeNow();
                                })
                                .onFailure(testContext::failNow);
                    })
                    .onFailure(testContext::failNow);
        }
        
        @Test
        @DisplayName("测试GET请求 - 用户列表")
        void testGetUserList(VertxTestContext testContext) {
            LOGGER.info("Testing GET /api/user/");
            
            httpClient.request(HttpMethod.GET, 8080, "localhost", "/api/user/")
                    .compose(request -> request.send())
                    .onSuccess(response -> {
                        testContext.verify(() -> {
                            assertEquals(200, response.statusCode(), "用户列表请求应该返回200状态码");
                            LOGGER.info("✅ User list test passed: {}", response.statusCode());
                        });
                        
                        response.body()
                                .onSuccess(body -> {
                                    testContext.verify(() -> {
                                        assertNotNull(body, "响应体不应为空");
                                        LOGGER.info("User list response: {}", body.toString());
                                    });
                                    testContext.completeNow();
                                })
                                .onFailure(testContext::failNow);
                    })
                    .onFailure(testContext::failNow);
        }
        
        @Test
        @DisplayName("测试GET请求 - 路径参数绑定")
        void testGetUserById(VertxTestContext testContext) {
            LOGGER.info("Testing GET /api/user/1");
            
            httpClient.request(HttpMethod.GET, 8080, "localhost", "/api/user/1")
                    .compose(request -> request.send())
                    .onSuccess(response -> {
                        testContext.verify(() -> {
                            // 用户可能不存在，所以200或404都是合理的
                            assertTrue(response.statusCode() == 200 || response.statusCode() == 404, 
                                    "用户查询应该返回200或404状态码");
                            LOGGER.info("✅ User by ID test passed: {}", response.statusCode());
                        });
                        
                        response.body()
                                .onSuccess(body -> {
                                    testContext.verify(() -> {
                                        assertNotNull(body, "响应体不应为空");
                                        LOGGER.info("User by ID response: {}", body.toString());
                                    });
                                    testContext.completeNow();
                                })
                                .onFailure(testContext::failNow);
                    })
                    .onFailure(testContext::failNow);
        }
        
        @Test
        @DisplayName("测试GET请求 - 查询参数绑定")
        void testGetUserByEmail(VertxTestContext testContext) {
            LOGGER.info("Testing GET /api/user/?email=test@example.com");
            
            httpClient.request(HttpMethod.GET, 8080, "localhost", "/api/user/?email=test@example.com")
                    .compose(request -> request.send())
                    .onSuccess(response -> {
                        testContext.verify(() -> {
                            // 用户可能不存在，所以200或404都是合理的
                            assertTrue(response.statusCode() == 200 || response.statusCode() == 404, 
                                    "用户查询应该返回200或404状态码");
                            LOGGER.info("✅ User by email test passed: {}", response.statusCode());
                        });
                        
                        response.body()
                                .onSuccess(body -> {
                                    testContext.verify(() -> {
                                        assertNotNull(body, "响应体不应为空");
                                        LOGGER.info("User by email response: {}", body.toString());
                                    });
                                    testContext.completeNow();
                                })
                                .onFailure(testContext::failNow);
                    })
                    .onFailure(testContext::failNow);
        }
    }
    
    @Nested
    @DisplayName("POST请求测试")
    class PostRequestTest {
        
        @Test
        @DisplayName("测试POST请求 - 创建用户")
        void testPostCreateUser(VertxTestContext testContext) {
            LOGGER.info("Testing POST /api/user/");
            
            JsonObject userData = new JsonObject()
                    .put("username", "testuser")
                    .put("email", "testuser@example.com")
                    .put("password", "password123")
                    .put("age", 25)
                    .put("status", "ACTIVE");
            
            httpClient.request(HttpMethod.POST, 8080, "localhost", "/api/user/")
                    .compose(request -> {
                        request.putHeader("Content-Type", "application/json");
                        return request.send(userData.toBuffer());
                    })
                    .onSuccess(response -> {
                        testContext.verify(() -> {
                            assertEquals(201, response.statusCode(), "创建用户应该返回201状态码");
                            LOGGER.info("✅ Create user test passed: {}", response.statusCode());
                        });
                        
                        response.body()
                                .onSuccess(body -> {
                                    testContext.verify(() -> {
                                        assertNotNull(body, "响应体不应为空");
                                        LOGGER.info("Create user response: {}", body.toString());
                                    });
                                    testContext.completeNow();
                                })
                                .onFailure(testContext::failNow);
                    })
                    .onFailure(testContext::failNow);
        }
        
        @Test
        @DisplayName("测试POST请求 - 无效数据")
        void testPostInvalidData(VertxTestContext testContext) {
            LOGGER.info("Testing POST /api/user/ with invalid data");
            
            JsonObject invalidData = new JsonObject()
                    .put("username", "") // 无效的用户名
                    .put("email", "invalid-email"); // 无效的邮箱格式
            
            httpClient.request(HttpMethod.POST, 8080, "localhost", "/api/user/")
                    .compose(request -> {
                        request.putHeader("Content-Type", "application/json");
                        return request.send(invalidData.toBuffer());
                    })
                    .onSuccess(response -> {
                        testContext.verify(() -> {
                            assertEquals(400, response.statusCode(), "无效数据应该返回400状态码");
                            LOGGER.info("✅ Invalid data test passed: {}", response.statusCode());
                        });
                        
                        response.body()
                                .onSuccess(body -> {
                                    testContext.verify(() -> {
                                        assertNotNull(body, "响应体不应为空");
                                        LOGGER.info("Invalid data response: {}", body.toString());
                                    });
                                    testContext.completeNow();
                                })
                                .onFailure(testContext::failNow);
                    })
                    .onFailure(testContext::failNow);
        }
    }
    
    @Nested
    @DisplayName("PUT请求测试")
    class PutRequestTest {
        
        @Test
        @DisplayName("测试PUT请求 - 更新用户")
        void testPutUpdateUser(VertxTestContext testContext) {
            LOGGER.info("Testing PUT /api/user/1");
            
            JsonObject updateData = new JsonObject()
                    .put("username", "updateduser")
                    .put("email", "updated@example.com")
                    .put("age", 30);
            
            httpClient.request(HttpMethod.PUT, 8080, "localhost", "/api/user/1")
                    .compose(request -> {
                        request.putHeader("Content-Type", "application/json");
                        return request.send(updateData.toBuffer());
                    })
                    .onSuccess(response -> {
                        testContext.verify(() -> {
                            // 用户可能不存在，所以200或404都是合理的
                            assertTrue(response.statusCode() == 200 || response.statusCode() == 404, 
                                    "更新用户应该返回200或404状态码");
                            LOGGER.info("✅ Update user test passed: {}", response.statusCode());
                        });
                        
                        response.body()
                                .onSuccess(body -> {
                                    testContext.verify(() -> {
                                        assertNotNull(body, "响应体不应为空");
                                        LOGGER.info("Update user response: {}", body.toString());
                                    });
                                    testContext.completeNow();
                                })
                                .onFailure(testContext::failNow);
                    })
                    .onFailure(testContext::failNow);
        }
    }
    
    @Nested
    @DisplayName("DELETE请求测试")
    class DeleteRequestTest {
        
        @Test
        @DisplayName("测试DELETE请求 - 删除用户")
        void testDeleteUser(VertxTestContext testContext) {
            LOGGER.info("Testing DELETE /api/user/1");
            
            httpClient.request(HttpMethod.DELETE, 8080, "localhost", "/api/user/1")
                    .compose(request -> request.send())
                    .onSuccess(response -> {
                        testContext.verify(() -> {
                            // 用户可能不存在，所以200或404都是合理的
                            assertTrue(response.statusCode() == 200 || response.statusCode() == 404, 
                                    "删除用户应该返回200或404状态码");
                            LOGGER.info("✅ Delete user test passed: {}", response.statusCode());
                        });
                        
                        response.body()
                                .onSuccess(body -> {
                                    testContext.verify(() -> {
                                        assertNotNull(body, "响应体不应为空");
                                        LOGGER.info("Delete user response: {}", body.toString());
                                    });
                                    testContext.completeNow();
                                })
                                .onFailure(testContext::failNow);
                    })
                    .onFailure(testContext::failNow);
        }
    }
    
    @Nested
    @DisplayName("异常处理测试")
    class ExceptionHandlingTest {
        
        @Test
        @DisplayName("测试404错误处理")
        void test404Error(VertxTestContext testContext) {
            LOGGER.info("Testing 404 error handling");
            
            httpClient.request(HttpMethod.GET, 8080, "localhost", "/api/nonexistent")
                    .compose(request -> request.send())
                    .onSuccess(response -> {
                        testContext.verify(() -> {
                            assertEquals(404, response.statusCode(), "不存在的路径应该返回404状态码");
                            LOGGER.info("✅ 404 error test passed: {}", response.statusCode());
                        });
                        
                        response.body()
                                .onSuccess(body -> {
                                    testContext.verify(() -> {
                                        assertNotNull(body, "响应体不应为空");
                                        LOGGER.info("404 error response: {}", body.toString());
                                    });
                                    testContext.completeNow();
                                })
                                .onFailure(testContext::failNow);
                    })
                    .onFailure(testContext::failNow);
        }
        
        @Test
        @DisplayName("测试500错误处理")
        void test500Error(VertxTestContext testContext) {
            LOGGER.info("Testing 500 error handling");
            
            // 发送会导致服务器错误的请求
            httpClient.request(HttpMethod.GET, 8080, "localhost", "/api/system/error")
                    .compose(request -> request.send())
                    .onSuccess(response -> {
                        testContext.verify(() -> {
                            assertEquals(500, response.statusCode(), "服务器错误应该返回500状态码");
                            LOGGER.info("✅ 500 error test passed: {}", response.statusCode());
                        });
                        
                        response.body()
                                .onSuccess(body -> {
                                    testContext.verify(() -> {
                                        assertNotNull(body, "响应体不应为空");
                                        LOGGER.info("500 error response: {}", body.toString());
                                    });
                                    testContext.completeNow();
                                })
                                .onFailure(testContext::failNow);
                    })
                    .onFailure(testContext::failNow);
        }
    }
    
    @Nested
    @DisplayName("方法重载测试")
    class MethodOverloadTest {
        
        @Test
        @DisplayName("测试方法重载 - 根据ID查询")
        void testMethodOverloadById(VertxTestContext testContext) {
            LOGGER.info("Testing method overload by ID");
            
            httpClient.request(HttpMethod.GET, 8080, "localhost", "/api/system/test/overload?id=1")
                    .compose(request -> request.send())
                    .onSuccess(response -> {
                        testContext.verify(() -> {
                            assertEquals(200, response.statusCode(), "方法重载测试应该返回200状态码");
                            LOGGER.info("✅ Method overload by ID test passed: {}", response.statusCode());
                        });
                        
                        response.body()
                                .onSuccess(body -> {
                                    testContext.verify(() -> {
                                        assertNotNull(body, "响应体不应为空");
                                        LOGGER.info("Method overload by ID response: {}", body.toString());
                                    });
                                    testContext.completeNow();
                                })
                                .onFailure(testContext::failNow);
                    })
                    .onFailure(testContext::failNow);
        }
        
        @Test
        @DisplayName("测试方法重载 - 根据名称查询")
        void testMethodOverloadByName(VertxTestContext testContext) {
            LOGGER.info("Testing method overload by name");
            
            httpClient.request(HttpMethod.GET, 8080, "localhost", "/api/system/test/overload?name=test")
                    .compose(request -> request.send())
                    .onSuccess(response -> {
                        testContext.verify(() -> {
                            assertEquals(200, response.statusCode(), "方法重载测试应该返回200状态码");
                            LOGGER.info("✅ Method overload by name test passed: {}", response.statusCode());
                        });
                        
                        response.body()
                                .onSuccess(body -> {
                                    testContext.verify(() -> {
                                        assertNotNull(body, "响应体不应为空");
                                        LOGGER.info("Method overload by name response: {}", body.toString());
                                    });
                                    testContext.completeNow();
                                })
                                .onFailure(testContext::failNow);
                    })
                    .onFailure(testContext::failNow);
        }
        
        @Test
        @DisplayName("测试方法重载 - 多个参数")
        void testMethodOverloadMultipleParams(VertxTestContext testContext) {
            LOGGER.info("Testing method overload with multiple parameters");
            
            httpClient.request(HttpMethod.GET, 8080, "localhost", "/api/system/test/overload?id=1&name=test")
                    .compose(request -> request.send())
                    .onSuccess(response -> {
                        testContext.verify(() -> {
                            assertEquals(200, response.statusCode(), "方法重载测试应该返回200状态码");
                            LOGGER.info("✅ Method overload multiple params test passed: {}", response.statusCode());
                        });
                        
                        response.body()
                                .onSuccess(body -> {
                                    testContext.verify(() -> {
                                        assertNotNull(body, "响应体不应为空");
                                        LOGGER.info("Method overload multiple params response: {}", body.toString());
                                    });
                                    testContext.completeNow();
                                })
                                .onFailure(testContext::failNow);
                    })
                    .onFailure(testContext::failNow);
        }
    }
    
    @Nested
    @DisplayName("性能测试")
    class PerformanceTest {
        
        @Test
        @DisplayName("测试并发请求处理")
        void testConcurrentRequests(VertxTestContext testContext) {
            LOGGER.info("Testing concurrent request handling");
            
            int concurrentRequests = 10;
            final int[] completedRequests = {0};
            
            for (int i = 0; i < concurrentRequests; i++) {
                final int requestId = i;
                httpClient.request(HttpMethod.GET, 8080, "localhost", "/api/system/health")
                        .compose(request -> request.send())
                        .onSuccess(response -> {
                            testContext.verify(() -> {
                                assertEquals(200, response.statusCode(), "并发请求应该返回200状态码");
                                LOGGER.info("✅ Concurrent request {} passed: {}", requestId, response.statusCode());
                            });
                            
                            synchronized (this) {
                                completedRequests[0]++;
                                if (completedRequests[0] == concurrentRequests) {
                                    LOGGER.info("✅ All {} concurrent requests completed", concurrentRequests);
                                    testContext.completeNow();
                                }
                            }
                        })
                        .onFailure(error -> {
                            LOGGER.error("❌ Concurrent request {} failed", requestId, error);
                            testContext.failNow(error);
                        });
            }
        }
        
        @Test
        @DisplayName("测试请求响应时间")
        void testResponseTime(VertxTestContext testContext) {
            LOGGER.info("Testing request response time");
            
            long startTime = System.currentTimeMillis();
            
            httpClient.request(HttpMethod.GET, 8080, "localhost", "/api/system/health")
                    .compose(request -> request.send())
                    .onSuccess(response -> {
                        long endTime = System.currentTimeMillis();
                        long responseTime = endTime - startTime;
                        
                        testContext.verify(() -> {
                            assertEquals(200, response.statusCode(), "请求应该返回200状态码");
                            assertTrue(responseTime < 1000, "响应时间应该小于1秒，实际: " + responseTime + "ms");
                            LOGGER.info("✅ Response time test passed: {}ms", responseTime);
                        });
                        
                        testContext.completeNow();
                    })
                    .onFailure(testContext::failNow);
        }
    }
}
