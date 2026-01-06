package cn.qaiu.vx.core.aop.aspect;

import cn.qaiu.vx.core.aop.ProceedingJoinPoint;
import cn.qaiu.vx.core.aop.Timed;
import cn.qaiu.vx.core.aop.annotation.*;
import io.vertx.core.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/**
 * 计时切面
 * <p>
 * 自动统计带有 {@link Timed} 注解的方法执行时间。
 * 支持异步方法（返回 Future 的方法）。
 * </p>
 *
 * @author qaiu
 * @since 1.0.0
 */
@Aspect
@Order(90)
public class TimingAspect {

    private static final Logger LOGGER = LoggerFactory.getLogger(TimingAspect.class);

    /**
     * 环绕通知 - 处理 @Timed 注解的方法
     */
    @Around("@annotation(cn.qaiu.vx.core.aop.Timed)")
    public Object timeMethod(ProceedingJoinPoint pjp) throws Throwable {
        Method method = pjp.getMethod();
        Timed timed = method.getAnnotation(Timed.class);

        if (timed == null) {
            return pjp.proceed();
        }

        String metricName = timed.value().isEmpty() ? pjp.getShortSignature() : timed.value();
        long startTime = System.nanoTime();

        try {
            Object result = pjp.proceed();

            // 处理异步结果
            if (result instanceof Future<?> future) {
                return future.map(r -> {
                    recordTiming(metricName, startTime, timed);
                    return r;
                }).recover(e -> {
                    recordTiming(metricName, startTime, timed);
                    return Future.failedFuture(e);
                });
            }

            // 同步结果
            recordTiming(metricName, startTime, timed);
            return result;

        } catch (Throwable e) {
            recordTiming(metricName, startTime, timed);
            throw e;
        }
    }

    private void recordTiming(String metricName, long startTime, Timed timed) {
        long endTime = System.nanoTime();
        long durationNanos = endTime - startTime;
        long durationMs = TimeUnit.NANOSECONDS.toMillis(durationNanos);

        // 转换为配置的时间单位
        TimeUnit unit = timed.unit();
        long duration = unit.convert(durationNanos, TimeUnit.NANOSECONDS);

        // 检查慢方法阈值
        long slowThreshold = timed.slowThreshold();
        if (slowThreshold > 0 && duration >= slowThreshold) {
            LOGGER.warn("[SLOW] {} 执行时间 {} {} (阈值: {} {})",
                    metricName, duration, getUnitName(unit), slowThreshold, getUnitName(unit));
        } else {
            LOGGER.debug("[TIMING] {} 执行时间 {}ms", metricName, durationMs);
        }

        // 如果启用了指标记录，可以在这里发送到指标系统
        if (timed.recordMetrics()) {
            recordMetrics(metricName, durationMs, timed.tags());
        }
    }

    /**
     * 记录到指标系统（预留扩展点）
     * <p>
     * 可以集成 Micrometer、Prometheus 等指标系统
     * </p>
     */
    protected void recordMetrics(String metricName, long durationMs, String[] tags) {
        // 预留扩展点，可以在子类中实现具体的指标记录逻辑
        // 例如：meterRegistry.timer(metricName, tags).record(durationMs, TimeUnit.MILLISECONDS);
    }

    private String getUnitName(TimeUnit unit) {
        return switch (unit) {
            case NANOSECONDS -> "ns";
            case MICROSECONDS -> "μs";
            case MILLISECONDS -> "ms";
            case SECONDS -> "s";
            case MINUTES -> "min";
            case HOURS -> "h";
            case DAYS -> "d";
        };
    }
}
