package cn.qaiu.vx.core.aop;

import static org.junit.jupiter.api.Assertions.*;

import cn.qaiu.vx.core.aop.Loggable.LogLevel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/**
 * 内置注解测试
 *
 * @author <a href="https://qaiu.top">QAIU</a>
 */
@DisplayName("内置注解测试")
public class BuiltInAnnotationsTest {

    @Nested
    @DisplayName("@Loggable 注解测试")
    class LoggableAnnotationTest {

        @Test
        @DisplayName("测试默认值")
        void testDefaultValues() throws NoSuchMethodException {
            Method method = TestService.class.getDeclaredMethod("defaultLoggable");
            Loggable loggable = method.getAnnotation(Loggable.class);
            
            assertNotNull(loggable);
            assertEquals(LogLevel.INFO, loggable.level(), "默认日志级别应该是 INFO");
            assertFalse(loggable.includeArgs(), "默认不记录参数");
            assertFalse(loggable.includeResult(), "默认不记录返回值");
            assertTrue(loggable.includeTime(), "默认记录执行时间");
            assertEquals("", loggable.prefix(), "默认无前缀");
        }

        @Test
        @DisplayName("测试自定义值")
        void testCustomValues() throws NoSuchMethodException {
            Method method = TestService.class.getDeclaredMethod("customLoggable");
            Loggable loggable = method.getAnnotation(Loggable.class);
            
            assertNotNull(loggable);
            assertEquals(LogLevel.DEBUG, loggable.level());
            assertTrue(loggable.includeArgs());
            assertTrue(loggable.includeResult());
            assertFalse(loggable.includeTime());
            assertEquals("[Service]", loggable.prefix());
        }
    }

    @Nested
    @DisplayName("@Timed 注解测试")
    class TimedAnnotationTest {

        @Test
        @DisplayName("测试默认值")
        void testDefaultValues() throws NoSuchMethodException {
            Method method = TestService.class.getDeclaredMethod("defaultTimed");
            Timed timed = method.getAnnotation(Timed.class);
            
            assertNotNull(timed);
            assertEquals("", timed.value(), "默认指标名称为空");
            assertEquals(0, timed.slowThreshold(), "默认慢方法阈值为0");
            assertEquals(TimeUnit.MILLISECONDS, timed.unit(), "默认时间单位为毫秒");
            assertTrue(timed.recordMetrics(), "默认记录指标");
            assertEquals(0, timed.tags().length, "默认无标签");
        }

        @Test
        @DisplayName("测试自定义值")
        void testCustomValues() throws NoSuchMethodException {
            Method method = TestService.class.getDeclaredMethod("customTimed");
            Timed timed = method.getAnnotation(Timed.class);
            
            assertNotNull(timed);
            assertEquals("custom.metric", timed.value());
            assertEquals(1000, timed.slowThreshold());
            assertEquals(TimeUnit.SECONDS, timed.unit());
            assertFalse(timed.recordMetrics());
            assertArrayEquals(new String[]{"tag1=value1", "tag2=value2"}, timed.tags());
        }
    }

    // 测试服务类
    static class TestService {

        @Loggable
        public void defaultLoggable() {
        }

        @Loggable(
            level = LogLevel.DEBUG,
            includeArgs = true,
            includeResult = true,
            includeTime = false,
            prefix = "[Service]"
        )
        public void customLoggable() {
        }

        @Timed
        public void defaultTimed() {
        }

        @Timed(
            value = "custom.metric",
            slowThreshold = 1000,
            unit = TimeUnit.SECONDS,
            recordMetrics = false,
            tags = {"tag1=value1", "tag2=value2"}
        )
        public void customTimed() {
        }
    }
}
