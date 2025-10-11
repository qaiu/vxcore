package cn.qaiu.vx.core.util;

import cn.qaiu.vx.core.annotaions.App;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @App注解功能测试
 * <br>Create date 2025-01-27
 *
 * @author <a href="https://qaiu.top">QAIU</a>
 */
public class AppAnnotationTest {

    /**
     * 测试@App注解配置的扫描路径
     */
    @Test
    public void testAppAnnotationScanPaths() {
        // 测试配置了baseScanPackage的@App注解
        Set<String> paths = AutoScanPathDetector.detectScanPaths(TestAppWithScanPackage.class);
        
        assertNotNull(paths);
        assertTrue(paths.contains("com.example.service"));
        assertTrue(paths.contains("com.example.controller"));
        assertTrue(paths.contains("com.example"));
        
        System.out.println("App-annotated scan paths: " + paths);
    }

    /**
     * 测试@App注解但没有配置baseScanPackage
     */
    @Test
    public void testAppAnnotationWithoutScanPackage() {
        // 测试没有配置baseScanPackage的@App注解
        Set<String> paths = AutoScanPathDetector.detectScanPaths(TestAppWithoutScanPackage.class);
        
        assertNotNull(paths);
        assertTrue(paths.contains("cn.qaiu.vx.core.util"));
        assertTrue(paths.contains("cn.qaiu"));
        
        System.out.println("App-annotated (fallback) scan paths: " + paths);
    }

    /**
     * 测试@App注解的推荐信息
     */
    @Test
    public void testAppAnnotationRecommendation() {
        String recommendation = AutoScanPathDetector.getRecommendation(TestAppWithScanPackage.class);
        
        assertNotNull(recommendation);
        assertTrue(recommendation.contains("App-annotated scan paths"));
        assertTrue(recommendation.contains("com.example.service"));
        assertTrue(recommendation.contains("com.example.controller"));
        
        System.out.println("App annotation recommendation:\n" + recommendation);
    }

    /**
     * 测试@App注解的多个包配置
     */
    @Test
    public void testAppAnnotationMultiplePackages() {
        Set<String> paths = AutoScanPathDetector.detectScanPaths(TestAppWithMultiplePackages.class);
        
        assertNotNull(paths);
        assertTrue(paths.contains("com.example.service"));
        assertTrue(paths.contains("com.example.controller"));
        assertTrue(paths.contains("com.example.repository"));
        assertTrue(paths.contains("com.example"));
        
        System.out.println("App-annotated multiple packages scan paths: " + paths);
    }

    /**
     * 测试@App注解的空配置
     */
    @Test
    public void testAppAnnotationEmptyConfig() {
        Set<String> paths = AutoScanPathDetector.detectScanPaths(TestAppWithEmptyConfig.class);
        
        assertNotNull(paths);
        assertTrue(paths.contains("cn.qaiu.vx.core.util"));
        assertTrue(paths.contains("cn.qaiu"));
        
        System.out.println("App-annotated empty config scan paths: " + paths);
    }

    /**
     * 测试@App注解的无效包名
     */
    @Test
    public void testAppAnnotationInvalidPackage() {
        Set<String> paths = AutoScanPathDetector.detectScanPaths(TestAppWithInvalidPackage.class);
        
        assertNotNull(paths);
        assertTrue(paths.contains("com.example.valid"));
        // 无效包名应该被忽略
        assertFalse(paths.contains("123.invalid"));
        
        System.out.println("App-annotated invalid package scan paths: " + paths);
    }

    // 测试用的内部类

    /**
     * 测试应用类（配置了baseScanPackage）
     */
    @App(
        name = "TestApp",
        version = "1.0.0",
        description = "Test application with scan package",
        baseScanPackage = "com.example.service,com.example.controller,com.example"
    )
    public static class TestAppWithScanPackage {
        public static void main(String[] args) {
            // 测试用
        }
    }

    /**
     * 测试应用类（没有配置baseScanPackage）
     */
    @App(
        name = "TestAppWithoutScan",
        version = "1.0.0",
        description = "Test application without scan package"
    )
    public static class TestAppWithoutScanPackage {
        public static void main(String[] args) {
            // 测试用
        }
    }

    /**
     * 测试应用类（配置了多个包）
     */
    @App(
        name = "TestAppMultiple",
        version = "1.0.0",
        description = "Test application with multiple packages",
        baseScanPackage = "com.example.service, com.example.controller, com.example.repository, com.example"
    )
    public static class TestAppWithMultiplePackages {
        public static void main(String[] args) {
            // 测试用
        }
    }

    /**
     * 测试应用类（空配置）
     */
    @App(
        name = "TestAppEmpty",
        version = "1.0.0",
        description = "Test application with empty config",
        baseScanPackage = ""
    )
    public static class TestAppWithEmptyConfig {
        public static void main(String[] args) {
            // 测试用
        }
    }

    /**
     * 测试应用类（包含无效包名）
     */
    @App(
        name = "TestAppInvalid",
        version = "1.0.0",
        description = "Test application with invalid package",
        baseScanPackage = "com.example.valid, 123.invalid, com.example"
    )
    public static class TestAppWithInvalidPackage {
        public static void main(String[] args) {
            // 测试用
        }
    }
}
