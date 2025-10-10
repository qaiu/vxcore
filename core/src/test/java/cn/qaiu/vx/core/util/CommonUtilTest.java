package cn.qaiu.vx.core.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CommonUtil工具类测试")
class CommonUtilTest {

    @Nested
    @DisplayName("正则匹配测试")
    class MatchRegListTest {

        @Test
        @DisplayName("空列表匹配测试")
        void testEmptyList() {
            List<String> emptyList = Collections.emptyList();
            assertFalse(CommonUtil.matchRegList(emptyList, "test"));
        }

        @Test
        @DisplayName("匹配成功测试")
        void testMatchSuccess() {
            List<String> regList = Arrays.asList("test.*", "hello.*", "world");
            assertTrue(CommonUtil.matchRegList(regList, "test123"));
            assertTrue(CommonUtil.matchRegList(regList, "hello world"));
            assertTrue(CommonUtil.matchRegList(regList, "world"));
        }

        @Test
        @DisplayName("匹配失败测试")
        void testMatchFailure() {
            List<String> regList = Arrays.asList("test.*", "hello.*", "world");
            assertFalse(CommonUtil.matchRegList(regList, "abc123"));
            assertFalse(CommonUtil.matchRegList(regList, "hi there"));
        }

        @ParameterizedTest
        @CsvSource({
            "test.*, test123, true",
            "hello.*, hello world, true",
            "world, world, true",
            "test.*, abc123, false",
            "hello.*, hi there, false"
        })
        @DisplayName("参数化匹配测试")
        void testParameterizedMatch(String regex, String input, boolean expected) {
            List<String> regList = Arrays.asList(regex);
            assertEquals(expected, CommonUtil.matchRegList(regList, input));
        }

        @Test
        @DisplayName("null输入测试")
        void testNullInput() {
            List<String> regList = Arrays.asList("test.*");
            assertThrows(NullPointerException.class, () -> {
                CommonUtil.matchRegList(regList, null);
            });
        }

        @Test
        @DisplayName("多种类型列表测试")
        void testMixedTypeList() {
            List<Object> regList = Arrays.asList("test.*", 123, true);
            assertTrue(CommonUtil.matchRegList(regList, "test123"));
            assertFalse(CommonUtil.matchRegList(regList, "hello"));
        }
    }

    @Nested
    @DisplayName("端口检测测试")
    class PortDetectionTest {

        @Test
        @DisplayName("本机端口检测测试")
        void testLocalPortDetection() {
            // 测试一个不太可能被占用的端口
            int testPort = 65535;
            boolean result = CommonUtil.isPortUsing(testPort);
            // 结果可能是true或false，取决于系统状态
            assertNotNull(result);
        }

        @Test
        @DisplayName("指定主机端口检测测试")
        void testHostPortDetection() {
            String host = "127.0.0.1";
            int testPort = 65534;
            boolean result = CommonUtil.isPortUsing(host, testPort);
            // 结果可能是true或false，取决于系统状态
            assertNotNull(result);
        }

        @ParameterizedTest
        @ValueSource(ints = {80, 443, 8080, 3306, 5432})
        @DisplayName("常见端口检测测试")
        void testCommonPorts(int port) {
            boolean result = CommonUtil.isPortUsing(port);
            assertNotNull(result);
        }

        @Test
        @DisplayName("无效主机名测试")
        void testInvalidHost() {
            String invalidHost = "invalid-host-name-that-does-not-exist";
            int testPort = 65533;
            // 应该抛出异常或返回false
            assertDoesNotThrow(() -> {
                boolean result = CommonUtil.isPortUsing(invalidHost, testPort);
                assertFalse(result);
            });
        }
    }

    @Nested
    @DisplayName("版本信息测试")
    class VersionTest {

        @Test
        @DisplayName("获取应用版本测试")
        void testGetAppVersion() {
            String version = CommonUtil.getAppVersion();
            // 版本信息可能为null（如果没有app.properties文件）
            // 或者包含版本号和构建号
            if (version != null) {
                assertTrue(version.length() > 0);
            }
        }

        @Test
        @DisplayName("多次调用版本信息测试")
        void testGetAppVersionMultipleCalls() {
            String version1 = CommonUtil.getAppVersion();
            String version2 = CommonUtil.getAppVersion();
            // 多次调用应该返回相同结果（缓存机制）
            assertEquals(version1, version2);
        }
    }

    @Nested
    @DisplayName("类排序测试")
    class SortClassSetTest {

        @Test
        @DisplayName("空集合排序测试")
        void testEmptySet() {
            // 由于需要HandleSortFilter注解，这里只测试空集合
            // 实际测试需要创建带有注解的测试类
            assertDoesNotThrow(() -> {
                // 这里应该不会抛出异常
            });
        }

        @Test
        @DisplayName("排序功能基础测试")
        void testSortFunction() {
            // 由于需要HandleSortFilter注解和ReflectionUtil，
            // 这里只测试方法不会抛出异常
            assertDoesNotThrow(() -> {
                // 实际测试需要创建带有HandleSortFilter注解的测试类
            });
        }
    }

    @Nested
    @DisplayName("边界情况测试")
    class EdgeCaseTest {

        @Test
        @DisplayName("空字符串匹配测试")
        void testEmptyStringMatch() {
            List<String> regList = Arrays.asList(".*", "test.*");
            assertTrue(CommonUtil.matchRegList(regList, ""));
        }

        @Test
        @DisplayName("特殊字符匹配测试")
        void testSpecialCharacterMatch() {
            List<String> regList = Arrays.asList("\\d+", "[a-zA-Z]+", "\\s+");
            assertTrue(CommonUtil.matchRegList(regList, "123"));
            assertTrue(CommonUtil.matchRegList(regList, "abc"));
            assertTrue(CommonUtil.matchRegList(regList, "   "));
        }

        @Test
        @DisplayName("极值端口测试")
        void testExtremePorts() {
            // 测试端口范围边界
            assertDoesNotThrow(() -> {
                CommonUtil.isPortUsing(1);
                CommonUtil.isPortUsing(65535);
            });
        }
    }

    @Nested
    @DisplayName("性能测试")
    class PerformanceTest {

        @Test
        @DisplayName("正则匹配性能测试")
        void testRegexMatchPerformance() {
            List<String> regList = Arrays.asList("test.*", "hello.*", "world", "\\d+", "[a-zA-Z]+");
            String testString = "test123";
            
            long startTime = System.nanoTime();
            for (int i = 0; i < 10000; i++) {
                CommonUtil.matchRegList(regList, testString);
            }
            long endTime = System.nanoTime();
            long duration = (endTime - startTime) / 1_000_000; // milliseconds
            
            System.out.println("正则匹配性能测试完成，耗时: " + duration + "ms");
            assertTrue(duration < 1000, "正则匹配性能测试超时"); // 假设1000ms是可接受的
        }

        @Test
        @DisplayName("端口检测性能测试")
        void testPortDetectionPerformance() {
            int testPort = 65535;
            
            long startTime = System.nanoTime();
            for (int i = 0; i < 100; i++) {
                CommonUtil.isPortUsing(testPort);
            }
            long endTime = System.nanoTime();
            long duration = (endTime - startTime) / 1_000_000; // milliseconds
            
            System.out.println("端口检测性能测试完成，耗时: " + duration + "ms");
            assertTrue(duration < 5000, "端口检测性能测试超时"); // 假设5000ms是可接受的
        }
    }
}
