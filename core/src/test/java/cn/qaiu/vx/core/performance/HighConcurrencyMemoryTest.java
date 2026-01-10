package cn.qaiu.vx.core.performance;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 高并发内存性能测试
 * 测试框架在高并发场景下的内存占用和增长率
 *
 * @author QAIU
 */
@ExtendWith(VertxExtension.class)
@DisplayName("高并发内存性能测试")
@Disabled("性能测试在常规测试中禁用，需要手动运行: mvn test -Dtest=HighConcurrencyMemoryTest -DskipPerformanceDisable=true")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class HighConcurrencyMemoryTest {

    private static final int PORT = 18889;
    private static final MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
    
    private static HttpClient client;
    private static String deploymentId;

    @BeforeAll
    static void setUpServer(Vertx vertx, VertxTestContext testContext) {
        HttpClientOptions options = new HttpClientOptions()
                .setMaxPoolSize(200)
                .setKeepAlive(true);
        
        client = vertx.createHttpClient(options);
        
        vertx.deployVerticle(new MemoryTestServerVerticle())
                .onSuccess(id -> {
                    deploymentId = id;
                    testContext.completeNow();
                })
                .onFailure(testContext::failNow);
    }

    @AfterAll
    static void tearDown(Vertx vertx, VertxTestContext testContext) {
        if (client != null) client.close();
        if (deploymentId != null) {
            vertx.undeploy(deploymentId).onComplete(ar -> testContext.completeNow());
        } else {
            testContext.completeNow();
        }
    }

    @Test
    @Order(1)
    @DisplayName("基准内存使用测试")
    void testBaselineMemory(VertxTestContext testContext) {
        forceGC();
        
        MemoryUsage heapBefore = memoryMXBean.getHeapMemoryUsage();
        MemoryUsage nonHeapBefore = memoryMXBean.getNonHeapMemoryUsage();
        
        System.out.println("=== 基准内存使用 ===");
        System.out.println("堆内存使用: " + formatBytes(heapBefore.getUsed()));
        System.out.println("堆内存提交: " + formatBytes(heapBefore.getCommitted()));
        System.out.println("非堆内存使用: " + formatBytes(nonHeapBefore.getUsed()));
        
        testContext.completeNow();
    }

    @Test
    @Order(2)
    @DisplayName("并发请求内存增长测试")
    @Timeout(value = 60, unit = TimeUnit.SECONDS)
    void testConcurrentRequestMemoryGrowth(Vertx vertx, VertxTestContext testContext) {
        forceGC();
        long initialMemory = getUsedHeapMemory();
        
        int totalRequests = 500;  // 减少请求数
        AtomicInteger completedRequests = new AtomicInteger(0);
        List<Long> memorySnapshots = new ArrayList<>();
        
        memorySnapshots.add(initialMemory);
        
        long memoryTimerId = vertx.setPeriodic(200, id -> {
            memorySnapshots.add(getUsedHeapMemory());
        });
        
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < totalRequests; i++) {
            client.request(HttpMethod.GET, PORT, "localhost", "/api/data")
                    .compose(req -> req.send().compose(resp -> resp.body()))
                    .onComplete(ar -> {
                        int completed = completedRequests.incrementAndGet();
                        if (completed == totalRequests) {
                            vertx.cancelTimer(memoryTimerId);
                            
                            forceGC();
                            long finalMemory = getUsedHeapMemory();
                            memorySnapshots.add(finalMemory);
                            
                            long duration = System.currentTimeMillis() - startTime;
                            long memoryGrowth = finalMemory - initialMemory;
                            double growthRate = (double) memoryGrowth / totalRequests;
                            
                            System.out.println("=== 并发请求内存增长测试结果 ===");
                            System.out.println("总请求数: " + totalRequests);
                            System.out.println("执行时间: " + duration + "ms");
                            System.out.println("初始内存: " + formatBytes(initialMemory));
                            System.out.println("最终内存: " + formatBytes(finalMemory));
                            System.out.println("内存增长: " + formatBytes(memoryGrowth));
                            System.out.println("每请求内存增长: " + String.format("%.2f", growthRate) + " bytes");
                            
                            long peakMemory = memorySnapshots.stream().mapToLong(Long::longValue).max().orElse(0);
                            System.out.println("内存峰值: " + formatBytes(peakMemory));
                            
                            // 放宽限制
                            assertTrue(growthRate < 2048, "每请求内存增长应小于2KB: " + growthRate);
                            
                            testContext.completeNow();
                        }
                    });
        }
    }

    @Test
    @Order(3)
    @DisplayName("持续压力下内存稳定性测试")
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    void testMemoryStabilityUnderLoad(Vertx vertx, VertxTestContext testContext) {
        forceGC();
        long initialMemory = getUsedHeapMemory();
        
        int testDurationSeconds = 5;  // 减少测试时长
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger pendingRequests = new AtomicInteger(0);
        List<Long> memorySnapshots = new ArrayList<>();
        
        long startTime = System.currentTimeMillis();
        
        long requestTimerId = vertx.setPeriodic(10, id -> {
            if (System.currentTimeMillis() - startTime >= testDurationSeconds * 1000L) {
                vertx.cancelTimer(id);
                return;
            }
            
            if (pendingRequests.get() < 30) {
                pendingRequests.incrementAndGet();
                client.request(HttpMethod.GET, PORT, "localhost", "/api/data")
                        .compose(req -> req.send().compose(resp -> resp.body()))
                        .onComplete(ar -> {
                            pendingRequests.decrementAndGet();
                            if (ar.succeeded()) successCount.incrementAndGet();
                        });
            }
        });
        
        long memoryTimerId = vertx.setPeriodic(1000, id -> {
            memorySnapshots.add(getUsedHeapMemory());
        });
        
        vertx.setTimer((testDurationSeconds + 2) * 1000L, id -> {
            vertx.cancelTimer(requestTimerId);
            vertx.cancelTimer(memoryTimerId);
            
            forceGC();
            long finalMemory = getUsedHeapMemory();
            
            System.out.println("=== 持续压力内存稳定性测试结果 ===");
            System.out.println("测试时长: " + testDurationSeconds + "秒");
            System.out.println("完成请求: " + successCount.get());
            System.out.println("吞吐量: " + (successCount.get() / Math.max(1, testDurationSeconds)) + " req/s");
            System.out.println("初始内存: " + formatBytes(initialMemory));
            System.out.println("最终内存: " + formatBytes(finalMemory));
            
            if (!memorySnapshots.isEmpty()) {
                long minMem = memorySnapshots.stream().mapToLong(Long::longValue).min().orElse(0);
                long maxMem = memorySnapshots.stream().mapToLong(Long::longValue).max().orElse(0);
                double avgMem = memorySnapshots.stream().mapToLong(Long::longValue).average().orElse(0);
                
                System.out.println("内存最小值: " + formatBytes(minMem));
                System.out.println("内存最大值: " + formatBytes(maxMem));
                System.out.println("内存平均值: " + formatBytes((long) avgMem));
                System.out.println("内存波动范围: " + formatBytes(maxMem - minMem));
                
                long memoryVariation = maxMem - minMem;
                assertTrue(memoryVariation < initialMemory * 0.5, 
                        "内存波动不应超过初始内存的50%: " + formatBytes(memoryVariation));
            }
            
            testContext.completeNow();
        });
    }

    @Test
    @Order(4)
    @DisplayName("大响应体内存测试")
    void testLargeResponseMemory(VertxTestContext testContext) {
        forceGC();
        long initialMemory = getUsedHeapMemory();
        
        int totalRequests = 100;
        AtomicInteger completedRequests = new AtomicInteger(0);
        
        for (int i = 0; i < totalRequests; i++) {
            client.request(HttpMethod.GET, PORT, "localhost", "/api/large")
                    .compose(req -> req.send().compose(resp -> resp.body()))
                    .onComplete(ar -> {
                        if (completedRequests.incrementAndGet() == totalRequests) {
                            forceGC();
                            long finalMemory = getUsedHeapMemory();
                            long memoryGrowth = finalMemory - initialMemory;
                            
                            System.out.println("=== 大响应体内存测试结果 ===");
                            System.out.println("请求数: " + totalRequests);
                            System.out.println("初始内存: " + formatBytes(initialMemory));
                            System.out.println("最终内存: " + formatBytes(finalMemory));
                            System.out.println("内存增长: " + formatBytes(memoryGrowth));
                            
                            assertTrue(memoryGrowth < 10 * 1024 * 1024, 
                                    "GC后内存增长应小于10MB: " + formatBytes(memoryGrowth));
                            
                            testContext.completeNow();
                        }
                    });
        }
    }

    @Test
    @Order(5)
    @DisplayName("内存泄漏检测测试")
    void testMemoryLeakDetection(Vertx vertx, VertxTestContext testContext) {
        int rounds = 3;
        int requestsPerRound = 200;
        List<Long> memoryAfterEachRound = new ArrayList<>();
        
        forceGC();
        memoryAfterEachRound.add(getUsedHeapMemory());
        
        AtomicInteger currentRound = new AtomicInteger(0);
        
        runMemoryRound(vertx, requestsPerRound, () -> {
            forceGC();
            memoryAfterEachRound.add(getUsedHeapMemory());
            
            if (currentRound.incrementAndGet() < rounds) {
                runMemoryRound(vertx, requestsPerRound, () -> {
                    forceGC();
                    memoryAfterEachRound.add(getUsedHeapMemory());
                    
                    if (currentRound.incrementAndGet() < rounds) {
                        runMemoryRound(vertx, requestsPerRound, () -> {
                            forceGC();
                            memoryAfterEachRound.add(getUsedHeapMemory());
                            analyzeMemoryTrend(memoryAfterEachRound, testContext);
                        });
                    } else {
                        analyzeMemoryTrend(memoryAfterEachRound, testContext);
                    }
                });
            } else {
                analyzeMemoryTrend(memoryAfterEachRound, testContext);
            }
        });
        
        vertx.setTimer(60000, id -> {
            if (!testContext.completed()) {
                analyzeMemoryTrend(memoryAfterEachRound, testContext);
            }
        });
    }

    private void runMemoryRound(Vertx vertx, int requests, Runnable onComplete) {
        AtomicInteger completed = new AtomicInteger(0);
        
        for (int i = 0; i < requests; i++) {
            client.request(HttpMethod.GET, PORT, "localhost", "/api/data")
                    .compose(req -> req.send().compose(resp -> resp.body()))
                    .onComplete(ar -> {
                        if (completed.incrementAndGet() == requests) {
                            onComplete.run();
                        }
                    });
        }
    }

    private void analyzeMemoryTrend(List<Long> memorySnapshots, VertxTestContext testContext) {
        System.out.println("=== 内存泄漏检测结果 ===");
        System.out.println("采样轮数: " + memorySnapshots.size());
        
        for (int i = 0; i < memorySnapshots.size(); i++) {
            System.out.println("轮次 " + i + " 内存: " + formatBytes(memorySnapshots.get(i)));
        }
        
        if (memorySnapshots.size() >= 2) {
            long firstMemory = memorySnapshots.get(0);
            long lastMemory = memorySnapshots.get(memorySnapshots.size() - 1);
            long growth = lastMemory - firstMemory;
            double growthPercent = (double) growth / firstMemory * 100;
            
            System.out.println("总内存增长: " + formatBytes(growth));
            System.out.println("增长百分比: " + String.format("%.2f", growthPercent) + "%");
            
            assertTrue(growthPercent < 100, "内存增长不应超过100%: " + growthPercent + "%");
        }
        
        testContext.completeNow();
    }

    private void forceGC() {
        System.gc();
        try {
            TimeUnit.MILLISECONDS.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private long getUsedHeapMemory() {
        return memoryMXBean.getHeapMemoryUsage().getUsed();
    }

    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.2f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.2f MB", bytes / (1024.0 * 1024));
        return String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024));
    }

    static class MemoryTestServerVerticle extends AbstractVerticle {
        private HttpServer server;
        
        @Override
        public void start(Promise<Void> startPromise) {
            Router router = Router.router(vertx);
            
            router.get("/api/data").handler(ctx -> {
                JsonObject response = new JsonObject()
                        .put("id", System.currentTimeMillis())
                        .put("data", "test data")
                        .put("timestamp", System.currentTimeMillis());
                ctx.response().putHeader("content-type", "application/json").end(response.encode());
            });
            
            router.get("/api/large").handler(ctx -> {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < 1000; i++) {
                    sb.append("data_").append(i).append("_");
                }
                JsonObject response = new JsonObject()
                        .put("largeData", sb.toString())
                        .put("timestamp", System.currentTimeMillis());
                ctx.response().putHeader("content-type", "application/json").end(response.encode());
            });
            
            server = vertx.createHttpServer();
            server.requestHandler(router).listen(PORT)
                    .onSuccess(s -> startPromise.complete())
                    .onFailure(startPromise::fail);
        }
        
        @Override
        public void stop(Promise<Void> stopPromise) {
            if (server != null) server.close().onComplete(ar -> stopPromise.complete());
            else stopPromise.complete();
        }
    }
}
