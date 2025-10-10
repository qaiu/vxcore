package cn.qaiu.vx.core.performance;

import cn.qaiu.vx.core.util.StringCase;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 内存性能测试
 * 测试核心工具类的内存使用效率和垃圾回收影响
 * 
 * @author QAIU
 */
@ExtendWith(VertxExtension.class)
@DisplayName("内存性能测试")
class MemoryPerformanceTest {

    @BeforeEach
    void setUp(Vertx vertx, VertxTestContext testContext) {
        testContext.completeNow();
    }

    @Test
    @DisplayName("内存分配效率测试")
    void testMemoryAllocationEfficiency(VertxTestContext testContext) {
        Runtime runtime = Runtime.getRuntime();
        
        // 强制垃圾回收
        System.gc();
        Thread.yield();
        try {
            TimeUnit.MILLISECONDS.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        long initialMemory = runtime.totalMemory() - runtime.freeMemory();
        
        List<String> results = new ArrayList<>();
        int operationCount = 100000;
        
        long startTime = System.nanoTime();
        
        for (int i = 0; i < operationCount; i++) {
            String testString = "testString_" + i;
            
            // 执行字符串转换操作
            String camelCase = StringCase.toLittleCamelCase(testString);
            String bigCamelCase = StringCase.toBigCamelCase(testString);
            String underlineLower = StringCase.toUnderlineCase(testString);
            String underlineUpper = StringCase.toUnderlineUpperCase(testString);
            
            // 保存结果避免被优化
            results.add(camelCase + bigCamelCase + underlineLower + underlineUpper);
        }
        
        long endTime = System.nanoTime();
        long executionTime = endTime - startTime;
        
        long finalMemory = runtime.totalMemory() - runtime.freeMemory();
        long memoryUsed = finalMemory - initialMemory;
        
        System.out.println("=== 内存分配效率测试结果 ===");
        System.out.println("操作数量: " + operationCount);
        System.out.println("执行时间: " + (executionTime / 1_000_000) + "ms");
        System.out.println("内存使用: " + (memoryUsed / 1024) + "KB");
        System.out.println("每操作内存: " + (memoryUsed / operationCount) + " bytes");
        System.out.println("每操作时间: " + (executionTime / operationCount) + "ns");
        
        // 验证结果
        assertEquals(operationCount, results.size(), "所有操作都应产生结果");
        
        // 性能断言
        long memoryPerOp = memoryUsed / operationCount;
        long timePerOp = executionTime / operationCount;
        
        assertTrue(memoryPerOp < 1000, "每操作内存使用应小于1000 bytes: " + memoryPerOp);
        assertTrue(timePerOp < 3000, "每操作时间应小于3微秒: " + timePerOp + "ns");
        
        testContext.completeNow();
    }

    @Test
    @DisplayName("垃圾回收影响测试")
    void testGarbageCollectionImpact(VertxTestContext testContext) {
        Runtime runtime = Runtime.getRuntime();
        
        // 记录初始内存状态
        long initialTotalMemory = runtime.totalMemory();
        long initialFreeMemory = runtime.freeMemory();
        long initialUsedMemory = initialTotalMemory - initialFreeMemory;
        
        System.out.println("=== 垃圾回收影响测试 ===");
        System.out.println("初始总内存: " + (initialTotalMemory / 1024 / 1024) + "MB");
        System.out.println("初始空闲内存: " + (initialFreeMemory / 1024 / 1024) + "MB");
        System.out.println("初始使用内存: " + (initialUsedMemory / 1024 / 1024) + "MB");
        
        // 执行大量操作产生垃圾
        int operationCount = 50000;
        long startTime = System.nanoTime();
        
        for (int i = 0; i < operationCount; i++) {
            String testString = "gcTest_" + i + "_" + System.currentTimeMillis();
            
                        // 执行操作但不保存结果，让对象成为垃圾
                        StringCase.toLittleCamelCase(testString);
                        StringCase.toBigCamelCase(testString);
                        StringCase.toUnderlineCase(testString);
                        StringCase.toUnderlineUpperCase(testString);
        }
        
        long endTime = System.nanoTime();
        long executionTime = endTime - startTime;
        
        // 记录操作后内存状态
        long afterTotalMemory = runtime.totalMemory();
        long afterFreeMemory = runtime.freeMemory();
        long afterUsedMemory = afterTotalMemory - afterFreeMemory;
        
        System.out.println("操作后总内存: " + (afterTotalMemory / 1024 / 1024) + "MB");
        System.out.println("操作后空闲内存: " + (afterFreeMemory / 1024 / 1024) + "MB");
        System.out.println("操作后使用内存: " + (afterUsedMemory / 1024 / 1024) + "MB");
        
        // 强制垃圾回收
        System.gc();
        Thread.yield();
        try {
            TimeUnit.MILLISECONDS.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // 记录GC后内存状态
        long gcTotalMemory = runtime.totalMemory();
        long gcFreeMemory = runtime.freeMemory();
        long gcUsedMemory = gcTotalMemory - gcFreeMemory;
        
        System.out.println("GC后总内存: " + (gcTotalMemory / 1024 / 1024) + "MB");
        System.out.println("GC后空闲内存: " + (gcFreeMemory / 1024 / 1024) + "MB");
        System.out.println("GC后使用内存: " + (gcUsedMemory / 1024 / 1024) + "MB");
        
        System.out.println("执行时间: " + (executionTime / 1_000_000) + "ms");
        System.out.println("每操作时间: " + (executionTime / operationCount) + "ns");
        
        // 验证性能
        long timePerOp = executionTime / operationCount;
        assertTrue(timePerOp < 2000, "每操作时间应小于2微秒: " + timePerOp + "ns");
        
        // 验证内存使用合理
        long memoryIncrease = afterUsedMemory - initialUsedMemory;
        long memoryPerOp = memoryIncrease / operationCount;
        assertTrue(memoryPerOp < 2000, "每操作内存增长应小于2000 bytes: " + memoryPerOp);
        
        testContext.completeNow();
    }

    @Test
    @DisplayName("内存泄漏检测测试")
    void testMemoryLeakDetection(VertxTestContext testContext) {
        Runtime runtime = Runtime.getRuntime();
        
        // 强制垃圾回收
        System.gc();
        Thread.yield();
        try {
            TimeUnit.MILLISECONDS.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        long initialMemory = runtime.totalMemory() - runtime.freeMemory();
        
        // 执行多轮操作
        int rounds = 10;
        int operationsPerRound = 10000;
        
        for (int round = 0; round < rounds; round++) {
            List<String> roundResults = new ArrayList<>();
            
            for (int i = 0; i < operationsPerRound; i++) {
                String testString = "leakTest_round_" + round + "_op_" + i;
                
                String result = StringCase.toLittleCamelCase(testString);
                roundResults.add(result);
            }
            
            // 清除引用，让对象成为垃圾
            roundResults.clear();
            
            // 每轮后强制GC
            System.gc();
            Thread.yield();
        }
        
        // 最终强制GC
        System.gc();
        Thread.yield();
        try {
            TimeUnit.MILLISECONDS.sleep(200);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        long finalMemory = runtime.totalMemory() - runtime.freeMemory();
        long memoryIncrease = finalMemory - initialMemory;
        
        System.out.println("=== 内存泄漏检测测试结果 ===");
        System.out.println("测试轮数: " + rounds);
        System.out.println("每轮操作数: " + operationsPerRound);
        System.out.println("总操作数: " + (rounds * operationsPerRound));
        System.out.println("初始内存: " + (initialMemory / 1024) + "KB");
        System.out.println("最终内存: " + (finalMemory / 1024) + "KB");
        System.out.println("内存增长: " + (memoryIncrease / 1024) + "KB");
        
        // 验证无内存泄漏：内存增长应小于1MB
        assertTrue(memoryIncrease < 1024 * 1024, "内存增长应小于1MB: " + (memoryIncrease / 1024) + "KB");
        
        testContext.completeNow();
    }

    @Test
    @DisplayName("大对象处理性能测试")
    void testLargeObjectPerformance(VertxTestContext testContext) {
        // 创建大字符串
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            sb.append("largeString").append(i).append("_");
        }
        String largeString = sb.toString();
        
        Runtime runtime = Runtime.getRuntime();
        
        // 强制垃圾回收
        System.gc();
        Thread.yield();
        try {
            TimeUnit.MILLISECONDS.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        long initialMemory = runtime.totalMemory() - runtime.freeMemory();
        
        int operationCount = 1000;
        long startTime = System.nanoTime();
        
        List<String> results = new ArrayList<>();
        
        for (int i = 0; i < operationCount; i++) {
            String testString = largeString + "_" + i;
            
            // 执行大字符串转换
            String camelCase = StringCase.toLittleCamelCase(testString);
            String bigCamelCase = StringCase.toBigCamelCase(testString);
            String underlineLower = StringCase.toUnderlineCase(testString);
            String underlineUpper = StringCase.toUnderlineUpperCase(testString);
            
            results.add(camelCase + bigCamelCase + underlineLower + underlineUpper);
        }
        
        long endTime = System.nanoTime();
        long executionTime = endTime - startTime;
        
        long finalMemory = runtime.totalMemory() - runtime.freeMemory();
        long memoryUsed = finalMemory - initialMemory;
        
        System.out.println("=== 大对象处理性能测试结果 ===");
        System.out.println("大字符串长度: " + largeString.length());
        System.out.println("操作数量: " + operationCount);
        System.out.println("执行时间: " + (executionTime / 1_000_000) + "ms");
        System.out.println("内存使用: " + (memoryUsed / 1024) + "KB");
        System.out.println("每操作时间: " + (executionTime / operationCount) + "ns");
        System.out.println("每操作内存: " + (memoryUsed / operationCount) + " bytes");
        
        // 验证结果
        assertEquals(operationCount, results.size(), "所有操作都应产生结果");
        
        // 性能断言：大对象处理时间应合理
        long timePerOp = executionTime / operationCount;
        assertTrue(timePerOp < 500000, "每操作时间应小于500微秒: " + timePerOp + "ns");
        
        testContext.completeNow();
    }

    @Test
    @DisplayName("内存使用峰值测试")
    void testMemoryUsagePeak(VertxTestContext testContext) {
        Runtime runtime = Runtime.getRuntime();
        
        // 强制垃圾回收
        System.gc();
        Thread.yield();
        try {
            TimeUnit.MILLISECONDS.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        long initialMemory = runtime.totalMemory() - runtime.freeMemory();
        long peakMemory = initialMemory;
        
        int operationCount = 50000;
        List<String> allResults = new ArrayList<>();
        
        long startTime = System.nanoTime();
        
        for (int i = 0; i < operationCount; i++) {
            String testString = "peakTest_" + i;
            
            // 执行操作并保存结果
            String result = StringCase.toLittleCamelCase(testString);
            allResults.add(result);
            
            // 定期检查内存使用峰值
            if (i % 1000 == 0) {
                long currentMemory = runtime.totalMemory() - runtime.freeMemory();
                if (currentMemory > peakMemory) {
                    peakMemory = currentMemory;
                }
            }
        }
        
        long endTime = System.nanoTime();
        long executionTime = endTime - startTime;
        
        long finalMemory = runtime.totalMemory() - runtime.freeMemory();
        long totalMemoryUsed = finalMemory - initialMemory;
        long peakMemoryUsed = peakMemory - initialMemory;
        
        System.out.println("=== 内存使用峰值测试结果 ===");
        System.out.println("操作数量: " + operationCount);
        System.out.println("执行时间: " + (executionTime / 1_000_000) + "ms");
        System.out.println("初始内存: " + (initialMemory / 1024) + "KB");
        System.out.println("最终内存: " + (finalMemory / 1024) + "KB");
        System.out.println("峰值内存: " + (peakMemory / 1024) + "KB");
        System.out.println("总内存使用: " + (totalMemoryUsed / 1024) + "KB");
        System.out.println("峰值内存使用: " + (peakMemoryUsed / 1024) + "KB");
        System.out.println("每操作时间: " + (executionTime / operationCount) + "ns");
        
        // 验证结果
        assertEquals(operationCount, allResults.size(), "所有操作都应产生结果");
        
        // 性能断言
        long timePerOp = executionTime / operationCount;
        assertTrue(timePerOp < 2000, "每操作时间应小于2微秒: " + timePerOp + "ns");
        
        // 内存使用断言：峰值内存使用应合理
        long peakMemoryPerOp = peakMemoryUsed / operationCount;
        assertTrue(peakMemoryPerOp < 500, "每操作峰值内存应小于500 bytes: " + peakMemoryPerOp);
        
        testContext.completeNow();
    }
}
