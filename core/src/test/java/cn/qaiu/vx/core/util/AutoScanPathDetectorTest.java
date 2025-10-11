package cn.qaiu.vx.core.util;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 自动扫描路径检测器测试
 * <br>Create date 2025-01-27
 *
 * @author <a href="https://qaiu.top">QAIU</a>
 */
public class AutoScanPathDetectorTest {

    /**
     * 测试基本路径检测
     */
    @Test
    public void testDetectScanPaths() {
        // 测试cn.qaiu.vx.core.util包
        Set<String> paths = AutoScanPathDetector.detectScanPaths(TestMainClass.class);
        
        assertNotNull(paths);
        assertTrue(paths.contains("cn.qaiu.vx.core.util"));
        assertTrue(paths.contains("cn.qaiu"));
        
        System.out.println("Detected paths for cn.qaiu.vx.core.util.TestMainClass: " + paths);
    }

    /**
     * 测试路径格式化
     */
    @Test
    public void testFormatScanPaths() {
        Set<String> paths = Set.of("cn.qaiu.example", "cn.qaiu");
        String formatted = AutoScanPathDetector.formatScanPaths(paths);
        
        // Set的顺序可能不同，所以检查包含关系
        assertTrue(formatted.contains("cn.qaiu"));
        assertTrue(formatted.contains("cn.qaiu.example"));
        assertTrue(formatted.contains(","));
    }

    /**
     * 测试路径验证
     */
    @Test
    public void testIsValidScanPath() {
        assertTrue(AutoScanPathDetector.isValidScanPath("cn.qaiu.example"));
        assertTrue(AutoScanPathDetector.isValidScanPath("com.example"));
        assertFalse(AutoScanPathDetector.isValidScanPath(""));
        assertFalse(AutoScanPathDetector.isValidScanPath(null));
        assertFalse(AutoScanPathDetector.isValidScanPath("123.invalid"));
    }

    /**
     * 测试推荐信息生成
     */
    @Test
    public void testGetRecommendation() {
        String recommendation = AutoScanPathDetector.getRecommendation(TestMainClass.class);
        
        assertNotNull(recommendation);
        assertTrue(recommendation.contains("cn.qaiu.vx.core.util.AutoScanPathDetectorTest$TestMainClass"));
        assertTrue(recommendation.contains("cn.qaiu.vx.core.util"));
        assertTrue(recommendation.contains("cn.qaiu"));
        
        System.out.println("Recommendation:\n" + recommendation);
    }

    /**
     * 测试堆栈跟踪检测
     */
    @Test
    public void testDetectScanPathsFromStackTrace() {
        Set<String> paths = AutoScanPathDetector.detectScanPathsFromStackTrace();
        
        assertNotNull(paths);
        assertFalse(paths.isEmpty());
        
        System.out.println("Detected paths from stack trace: " + paths);
    }

    /**
     * 测试空类处理
     */
    @Test
    public void testDetectScanPathsWithNull() {
        Set<String> paths = AutoScanPathDetector.detectScanPaths(null);
        
        assertNotNull(paths);
        assertEquals(1, paths.size());
        assertTrue(paths.contains("cn.qaiu"));
    }

    /**
     * 测试默认包处理
     */
    @Test
    public void testDetectScanPathsWithDefaultPackage() {
        Set<String> paths = AutoScanPathDetector.detectScanPaths(DefaultPackageClass.class);
        
        assertNotNull(paths);
        assertTrue(paths.size() >= 1);
        assertTrue(paths.contains("cn.qaiu"));
    }

    // 测试用的内部类

    /**
     * 测试主类（模拟cn.qaiu.example包）
     */
    public static class TestMainClass {
        public static void main(String[] args) {
            // 测试用
        }
    }

    /**
     * 默认包测试类
     */
    public static class DefaultPackageClass {
        // 测试用
    }
}
