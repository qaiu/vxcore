package cn.qaiu.vx.core.test;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * 测试配置注解
 * 提供统一的测试超时和配置管理
 * 
 * @author <a href="https://qaiu.top">QAIU</a>
 */
public class TestConfig {
    
    /**
     * 快速测试超时注解（5秒）
     */
    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    public @interface FastTest {
        String value() default "";
    }
    
    /**
     * 中等测试超时注解（30秒）
     */
    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @Test
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    public @interface MediumTest {
        String value() default "";
    }
    
    /**
     * 慢速测试超时注解（60秒）
     */
    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @Test
    @Timeout(value = 60, unit = TimeUnit.SECONDS)
    public @interface SlowTest {
        String value() default "";
    }
    
    /**
     * 集成测试超时注解（120秒）
     */
    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @Test
    @Timeout(value = 120, unit = TimeUnit.SECONDS)
    public @interface IntegrationTest {
        String value() default "";
    }
    
    /**
     * 测试超时常量
     */
    public static class Timeouts {
        public static final int FAST = 5;      // 5秒
        public static final int MEDIUM = 30;   // 30秒
        public static final int SLOW = 60;     // 60秒
        public static final int INTEGRATION = 120; // 120秒
    }
    
    /**
     * 测试配置常量
     */
    public static class Config {
        public static final int DEFAULT_POOL_SIZE = 5;
        public static final int DEFAULT_MAX_WAIT_QUEUE_SIZE = 10;
        public static final String DEFAULT_TEST_HOST = "127.0.0.1";
        public static final int DEFAULT_PORT_RANGE_START = 8000;
        public static final int DEFAULT_PORT_RANGE_END = 9000;
    }
}
