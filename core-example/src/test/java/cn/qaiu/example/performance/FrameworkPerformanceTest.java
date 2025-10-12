package cn.qaiu.example.performance;

import cn.qaiu.example.controller.UserController;
import cn.qaiu.example.dao.UserDao;
import cn.qaiu.example.model.User;
import cn.qaiu.example.service.UserService;
import cn.qaiu.vx.core.VXCoreApplication;
import cn.qaiu.vx.core.model.JsonResult;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 框架性能测试
 * 测试框架在高并发和大数据量下的性能表现
 * 
 * @author <a href="https://qaiu.top">QAIU</a>
 */
@ExtendWith(VertxExtension.class)
@DisplayName("框架性能测试")
public class FrameworkPerformanceTest {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(FrameworkPerformanceTest.class);
    
    private VXCoreApplication application;
    private Vertx vertx;
    private UserController userController;
    private UserService userService;
    private UserDao userDao;
    
    @BeforeEach
    @DisplayName("初始化测试环境")
    void setUp(Vertx vertx, VertxTestContext testContext) {
        this.vertx = vertx;
        this.application = new VXCoreApplication();
        
        // 初始化组件
        this.userDao = new UserDao();
        this.userService = new UserService();
        this.userController = new UserController();
        
        // 启动应用
        application.start(new String[]{"test"}, config -> {
            LOGGER.info("Performance test application started");
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
                    LOGGER.info("Performance test application stopped");
                    testContext.completeNow();
                })
                .onFailure(testContext::failNow);
        } else {
            testContext.completeNow();
        }
    }
    
    @Test
    @DisplayName("测试并发创建用户性能")
    void testConcurrentUserCreationPerformance(VertxTestContext testContext) {
        int concurrentCount = 100;
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);
        AtomicLong totalTime = new AtomicLong(0);
        
        long startTime = System.currentTimeMillis();
        
        List<Future<JsonResult>> futures = new ArrayList<>();
        
        for (int i = 0; i < concurrentCount; i++) {
            User user = new User("性能测试用户" + i, "perf" + i + "@example.com", 20 + (i % 50));
            
            Future<JsonResult> future = userController.createUser(user)
                .onSuccess(result -> {
                    successCount.incrementAndGet();
                    if (result.isSuccess()) {
                        LOGGER.debug("Created user successfully: {}", result.getData());
                    }
                })
                .onFailure(error -> {
                    failureCount.incrementAndGet();
                    LOGGER.error("Failed to create user: {}", error.getMessage());
                });
            
            futures.add(future);
        }
        
        Future.all(futures)
            .onSuccess(results -> {
                long endTime = System.currentTimeMillis();
                long duration = endTime - startTime;
                totalTime.set(duration);
                
                testContext.verify(() -> {
                    LOGGER.info("并发创建用户性能测试结果:");
                    LOGGER.info("- 并发数: {}", concurrentCount);
                    LOGGER.info("- 成功数: {}", successCount.get());
                    LOGGER.info("- 失败数: {}", failureCount.get());
                    LOGGER.info("- 总耗时: {}ms", duration);
                    LOGGER.info("- 平均耗时: {}ms/用户", duration / (double) concurrentCount);
                    LOGGER.info("- QPS: {}", (concurrentCount * 1000.0) / duration);
                    
                    assertTrue(successCount.get() > 0, "应该有成功的操作");
                    assertTrue(duration < 10000, "总耗时应该小于10秒");
                    
                    testContext.completeNow();
                });
            })
            .onFailure(testContext::failNow);
    }
    
    @Test
    @DisplayName("测试批量操作性能")
    void testBatchOperationPerformance(VertxTestContext testContext) {
        int batchSize = 1000;
        List<User> users = new ArrayList<>();
        
        // 准备测试数据
        for (int i = 0; i < batchSize; i++) {
            User user = new User("批量用户" + i, "batch" + i + "@example.com", 20 + (i % 50));
            users.add(user);
        }
        
        long startTime = System.currentTimeMillis();
        
        userController.batchCreateUsers(users)
            .onSuccess(result -> {
                long endTime = System.currentTimeMillis();
                long duration = endTime - startTime;
                
                testContext.verify(() -> {
                    assertTrue(result.isSuccess(), "批量创建应该成功");
                    
                    LOGGER.info("批量操作性能测试结果:");
                    LOGGER.info("- 批量大小: {}", batchSize);
                    LOGGER.info("- 总耗时: {}ms", duration);
                    LOGGER.info("- 平均耗时: {}ms/用户", duration / (double) batchSize);
                    LOGGER.info("- 吞吐量: {} 用户/秒", (batchSize * 1000.0) / duration);
                    
                    assertTrue(duration < 5000, "批量操作应该在5秒内完成");
                    
                    testContext.completeNow();
                });
            })
            .onFailure(testContext::failNow);
    }
    
    @Test
    @DisplayName("测试查询性能")
    void testQueryPerformance(VertxTestContext testContext) {
        int queryCount = 1000;
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicLong totalTime = new AtomicLong(0);
        
        long startTime = System.currentTimeMillis();
        
        List<Future<JsonResult>> futures = new ArrayList<>();
        
        for (int i = 0; i < queryCount; i++) {
            Future<JsonResult> future = userController.getAllUsers()
                .onSuccess(result -> {
                    successCount.incrementAndGet();
                })
                .onFailure(error -> {
                    LOGGER.error("Query failed: {}", error.getMessage());
                });
            
            futures.add(future);
        }
        
        Future.all(futures)
            .onSuccess(results -> {
                long endTime = System.currentTimeMillis();
                long duration = endTime - startTime;
                totalTime.set(duration);
                
                testContext.verify(() -> {
                    LOGGER.info("查询性能测试结果:");
                    LOGGER.info("- 查询次数: {}", queryCount);
                    LOGGER.info("- 成功次数: {}", successCount.get());
                    LOGGER.info("- 总耗时: {}ms", duration);
                    LOGGER.info("- 平均耗时: {}ms/查询", duration / (double) queryCount);
                    LOGGER.info("- QPS: {}", (queryCount * 1000.0) / duration);
                    
                    assertTrue(successCount.get() > 0, "应该有成功的查询");
                    assertTrue(duration < 5000, "查询应该在5秒内完成");
                    
                    testContext.completeNow();
                });
            })
            .onFailure(testContext::failNow);
    }
    
    @Test
    @DisplayName("测试内存使用情况")
    void testMemoryUsage(VertxTestContext testContext) {
        Runtime runtime = Runtime.getRuntime();
        
        // 记录初始内存使用
        long initialMemory = runtime.totalMemory() - runtime.freeMemory();
        LOGGER.info("初始内存使用: {} MB", initialMemory / 1024 / 1024);
        
        // 创建大量用户
        int userCount = 10000;
        List<User> users = new ArrayList<>();
        
        for (int i = 0; i < userCount; i++) {
            User user = new User("内存测试用户" + i, "memory" + i + "@example.com", 20 + (i % 50));
            users.add(user);
        }
        
        // 记录创建后的内存使用
        long afterCreationMemory = runtime.totalMemory() - runtime.freeMemory();
        LOGGER.info("创建{}个用户后内存使用: {} MB", userCount, afterCreationMemory / 1024 / 1024);
        
        // 批量保存用户
        userDao.batchSave(users)
            .onSuccess(savedUsers -> {
                // 记录保存后的内存使用
                long afterSaveMemory = runtime.totalMemory() - runtime.freeMemory();
                
                testContext.verify(() -> {
                    LOGGER.info("内存使用测试结果:");
                    LOGGER.info("- 用户数量: {}", userCount);
                    LOGGER.info("- 初始内存: {} MB", initialMemory / 1024 / 1024);
                    LOGGER.info("- 创建后内存: {} MB", afterCreationMemory / 1024 / 1024);
                    LOGGER.info("- 保存后内存: {} MB", afterSaveMemory / 1024 / 1024);
                    LOGGER.info("- 内存增长: {} MB", (afterSaveMemory - initialMemory) / 1024 / 1024);
                    LOGGER.info("- 每用户内存: {} KB", (afterSaveMemory - initialMemory) / 1024 / userCount);
                    
                    // 验证内存使用合理
                    long memoryGrowth = afterSaveMemory - initialMemory;
                    long memoryPerUser = memoryGrowth / userCount;
                    
                    assertTrue(memoryPerUser < 10 * 1024, "每用户内存使用应该小于10KB");
                    
                    testContext.completeNow();
                });
            })
            .onFailure(testContext::failNow);
    }
    
    @Test
    @DisplayName("测试框架启动性能")
    void testFrameworkStartupPerformance(VertxTestContext testContext) {
        int testRounds = 10;
        List<Long> startupTimes = new ArrayList<>();
        
        Future<Void> testFuture = Future.succeededFuture();
        
        for (int i = 0; i < testRounds; i++) {
            final int round = i;
            testFuture = testFuture.compose(v -> {
                VXCoreApplication testApp = new VXCoreApplication();
                long startTime = System.currentTimeMillis();
                
                return testApp.start(new String[]{"test"}, config -> {
                    long endTime = System.currentTimeMillis();
                    long duration = endTime - startTime;
                    startupTimes.add(duration);
                    LOGGER.info("第{}轮启动耗时: {}ms", round + 1, duration);
                }).compose(startResult -> {
                    return testApp.stop();
                });
            });
        }
        
        testFuture.onSuccess(v -> {
            testContext.verify(() -> {
                // 计算统计信息
                long totalTime = startupTimes.stream().mapToLong(Long::longValue).sum();
                double averageTime = totalTime / (double) testRounds;
                long minTime = startupTimes.stream().mapToLong(Long::longValue).min().orElse(0);
                long maxTime = startupTimes.stream().mapToLong(Long::longValue).max().orElse(0);
                
                LOGGER.info("框架启动性能测试结果:");
                LOGGER.info("- 测试轮数: {}", testRounds);
                LOGGER.info("- 总耗时: {}ms", totalTime);
                LOGGER.info("- 平均耗时: {}ms", averageTime);
                LOGGER.info("- 最短耗时: {}ms", minTime);
                LOGGER.info("- 最长耗时: {}ms", maxTime);
                
                assertTrue(averageTime < 5000, "平均启动时间应该小于5秒");
                assertTrue(maxTime < 10000, "最长启动时间应该小于10秒");
                
                testContext.completeNow();
            });
        }).onFailure(testContext::failNow);
    }
    
    @Test
    @DisplayName("测试压力测试")
    void testStressTest(VertxTestContext testContext) {
        int stressLevel = 500; // 并发数
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);
        
        long startTime = System.currentTimeMillis();
        
        List<Future<JsonResult>> futures = new ArrayList<>();
        
        // 混合操作：创建、查询、更新、删除
        for (int i = 0; i < stressLevel; i++) {
            final int index = i;
            Future<JsonResult> future;
            
            if (i % 4 == 0) {
                // 创建用户
                User user = new User("压力测试用户" + i, "stress" + i + "@example.com", 20 + (i % 50));
                future = userController.createUser(user);
            } else if (i % 4 == 1) {
                // 查询所有用户
                future = userController.getAllUsers();
            } else if (i % 4 == 2) {
                // 根据ID查询用户
                future = userController.getUserById(1L);
            } else {
                // 搜索用户
                future = userController.searchUsers("压力测试");
            }
            
            future.onSuccess(result -> {
                successCount.incrementAndGet();
            }).onFailure(error -> {
                failureCount.incrementAndGet();
            });
            
            futures.add(future);
        }
        
        Future.all(futures)
            .onSuccess(results -> {
                long endTime = System.currentTimeMillis();
                long duration = endTime - startTime;
                
                testContext.verify(() -> {
                    LOGGER.info("压力测试结果:");
                    LOGGER.info("- 并发数: {}", stressLevel);
                    LOGGER.info("- 成功数: {}", successCount.get());
                    LOGGER.info("- 失败数: {}", failureCount.get());
                    LOGGER.info("- 总耗时: {}ms", duration);
                    LOGGER.info("- 成功率: {}%", (successCount.get() * 100.0) / stressLevel);
                    LOGGER.info("- QPS: {}", (stressLevel * 1000.0) / duration);
                    
                    assertTrue(successCount.get() > stressLevel * 0.8, "成功率应该大于80%");
                    assertTrue(duration < 30000, "压力测试应该在30秒内完成");
                    
                    testContext.completeNow();
                });
            })
            .onFailure(testContext::failNow);
    }
}