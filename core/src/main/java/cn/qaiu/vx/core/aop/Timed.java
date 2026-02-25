package cn.qaiu.vx.core.aop;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * 计时注解
 *
 * <p>标记在方法上，自动统计方法执行时间。 可以设置慢方法阈值，超过阈值会输出警告日志。
 *
 * <pre>
 * &#64;Timed
 * public Future&lt;List&lt;User&gt;&gt; findAll() {
 *     // 自动统计执行时间
 * }
 *
 * &#64;Timed(slowThreshold = 1000, unit = TimeUnit.MILLISECONDS)
 * public Data processData(Request request) {
 *     // 执行时间超过1000ms会输出警告
 * }
 * </pre>
 *
 * @author qaiu
 * @since 1.0.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Timed {

  /** 指标名称，默认使用方法签名 */
  String value() default "";

  /** 慢方法阈值，超过此值会输出警告日志 默认为 0，表示不检查 */
  long slowThreshold() default 0;

  /** 时间单位 */
  TimeUnit unit() default TimeUnit.MILLISECONDS;

  /** 是否记录到指标系统（如果有的话） */
  boolean recordMetrics() default true;

  /** 额外的标签/维度，格式为 "key=value" */
  String[] tags() default {};
}
