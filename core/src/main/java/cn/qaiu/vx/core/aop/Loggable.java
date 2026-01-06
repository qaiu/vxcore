package cn.qaiu.vx.core.aop;

import java.lang.annotation.*;

/**
 * 日志记录注解
 * <p>
 * 标记在方法上，自动记录方法的调用和返回信息。
 * </p>
 *
 * <pre>
 * &#64;Loggable
 * public Future&lt;User&gt; findById(Long id) {
 *     // 方法调用前后会自动记录日志
 * }
 *
 * &#64;Loggable(level = LogLevel.DEBUG, includeArgs = true, includeResult = true)
 * public User createUser(UserDTO dto) {
 *     // 会记录参数和返回值
 * }
 * </pre>
 *
 * @author qaiu
 * @since 1.0.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Loggable {

    /**
     * 日志级别
     */
    LogLevel level() default LogLevel.INFO;

    /**
     * 是否记录方法参数
     */
    boolean includeArgs() default false;

    /**
     * 是否记录返回值
     */
    boolean includeResult() default false;

    /**
     * 是否记录执行时间
     */
    boolean includeTime() default true;

    /**
     * 自定义日志消息前缀
     */
    String prefix() default "";

    /**
     * 日志级别枚举
     */
    enum LogLevel {
        TRACE, DEBUG, INFO, WARN, ERROR
    }
}
