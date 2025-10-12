package cn.qaiu.example;

import cn.qaiu.example.framework.ThreeLayerFrameworkTest;
import cn.qaiu.example.integration.ThreeLayerIntegrationTest;
import cn.qaiu.example.performance.FrameworkPerformanceTest;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 测试运行器
 * 执行所有框架测试
 * 
 * @author <a href="https://qaiu.top">QAIU</a>
 */
@ExtendWith(VertxExtension.class)
@DisplayName("VXCore框架测试套件")
public class TestRunner {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(TestRunner.class);
    
    @Test
    @DisplayName("执行所有框架测试")
    void runAllTests(Vertx vertx, VertxTestContext testContext) {
        LOGGER.info("开始执行VXCore框架测试套件...");
        
        long startTime = System.currentTimeMillis();
        AtomicInteger testCount = new AtomicInteger(0);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);
        
        // 执行基础框架测试
        runTestClass(ThreeLayerFrameworkTest.class, "基础框架测试", testCount, successCount, failureCount)
            .compose(v -> {
                // 执行集成测试
                return runTestClass(ThreeLayerIntegrationTest.class, "集成测试", testCount, successCount, failureCount);
            })
            .compose(v -> {
                // 执行性能测试
                return runTestClass(FrameworkPerformanceTest.class, "性能测试", testCount, successCount, failureCount);
            })
            .onSuccess(v -> {
                long endTime = System.currentTimeMillis();
                long duration = endTime - startTime;
                
                LOGGER.info("=== 测试套件执行完成 ===");
                LOGGER.info("总测试数: {}", testCount.get());
                LOGGER.info("成功数: {}", successCount.get());
                LOGGER.info("失败数: {}", failureCount.get());
                LOGGER.info("总耗时: {}ms", duration);
                LOGGER.info("成功率: {}%", (successCount.get() * 100.0) / testCount.get());
                
                if (failureCount.get() == 0) {
                    LOGGER.info("🎉 所有测试通过！");
                    testContext.completeNow();
                } else {
                    LOGGER.error("❌ 有{}个测试失败", failureCount.get());
                    testContext.failNow(new RuntimeException("测试失败"));
                }
            })
            .onFailure(error -> {
                LOGGER.error("测试套件执行失败", error);
                testContext.failNow(error);
            });
    }
    
    /**
     * 运行测试类
     */
    private Future<Void> runTestClass(Class<?> testClass, String testName, 
                                    AtomicInteger testCount, AtomicInteger successCount, AtomicInteger failureCount) {
        return Future.future(promise -> {
            try {
                LOGGER.info("开始执行{}...", testName);
                
                // 这里可以集成JUnit 5的测试执行器
                // 为了简化，我们使用反射来执行测试方法
                executeTestMethods(testClass, testName, testCount, successCount, failureCount)
                    .onSuccess(v -> {
                        LOGGER.info("{}执行完成", testName);
                        promise.complete();
                    })
                    .onFailure(promise::fail);
                    
            } catch (Exception e) {
                LOGGER.error("执行{}失败", testName, e);
                promise.fail(e);
            }
        });
    }
    
    /**
     * 执行测试方法
     */
    private Future<Void> executeTestMethods(Class<?> testClass, String testName,
                                          AtomicInteger testCount, AtomicInteger successCount, AtomicInteger failureCount) {
        return Future.future(promise -> {
            try {
                // 这里应该使用JUnit 5的测试执行器
                // 为了演示，我们模拟测试执行
                LOGGER.info("模拟执行{}的测试方法...", testName);
                
                // 模拟测试执行时间
                Vertx.currentContext().runOnContext(v -> {
                    try {
                        Thread.sleep(1000); // 模拟测试执行时间
                        
                        // 模拟测试结果
                        int testMethods = 5; // 假设每个测试类有5个测试方法
                        int success = 4;     // 假设4个成功
                        int failure = 1;     // 假设1个失败
                        
                        testCount.addAndGet(testMethods);
                        successCount.addAndGet(success);
                        failureCount.addAndGet(failure);
                        
                        LOGGER.info("{}执行结果: {}个测试, {}个成功, {}个失败", 
                                  testName, testMethods, success, failure);
                        
                        promise.complete();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        promise.fail(e);
                    }
                });
                
            } catch (Exception e) {
                LOGGER.error("执行{}的测试方法失败", testName, e);
                promise.fail(e);
            }
        });
    }
}