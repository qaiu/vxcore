package cn.qaiu.vx.core.aop.aspect;

import cn.qaiu.vx.core.aop.Loggable;
import cn.qaiu.vx.core.aop.Loggable.LogLevel;
import cn.qaiu.vx.core.aop.ProceedingJoinPoint;
import cn.qaiu.vx.core.aop.annotation.*;
import io.vertx.core.Future;
import java.lang.reflect.Method;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 日志切面
 *
 * <p>自动记录带有 {@link Loggable} 注解的方法调用日志。
 *
 * @author qaiu
 * @since 1.0.0
 */
@Aspect
@Order(100)
public class LoggingAspect {

  private static final Logger LOGGER = LoggerFactory.getLogger(LoggingAspect.class);

  /** 环绕通知 - 处理 @Loggable 注解的方法 */
  @Around("@annotation(cn.qaiu.vx.core.aop.Loggable)")
  public Object logMethod(ProceedingJoinPoint pjp) throws Throwable {
    Method method = pjp.getMethod();
    Loggable loggable = method.getAnnotation(Loggable.class);

    if (loggable == null) {
      return pjp.proceed();
    }

    String signature = pjp.getShortSignature();
    String prefix = loggable.prefix().isEmpty() ? "" : loggable.prefix() + " ";
    LogLevel level = loggable.level();
    long startTime = System.currentTimeMillis();

    // 记录方法开始
    String enterMessage = buildEnterMessage(prefix, signature, loggable, pjp.getArgs());
    log(level, enterMessage);

    try {
      Object result = pjp.proceed();

      // 处理异步结果
      if (result instanceof Future<?> future) {
        return future
            .map(
                r -> {
                  long duration = System.currentTimeMillis() - startTime;
                  String exitMessage = buildExitMessage(prefix, signature, loggable, r, duration);
                  log(level, exitMessage);
                  return r;
                })
            .recover(
                e -> {
                  long duration = System.currentTimeMillis() - startTime;
                  logError(
                      "{}方法 {} 执行异常 (耗时 {}ms): {}", prefix, signature, duration, e.getMessage());
                  return Future.failedFuture(e);
                });
      }

      // 同步结果
      long duration = System.currentTimeMillis() - startTime;
      String exitMessage = buildExitMessage(prefix, signature, loggable, result, duration);
      log(level, exitMessage);

      return result;

    } catch (Throwable e) {
      long duration = System.currentTimeMillis() - startTime;
      logError("{}方法 {} 执行异常 (耗时 {}ms): {}", prefix, signature, duration, e.getMessage());
      throw e;
    }
  }

  private String buildEnterMessage(
      String prefix, String signature, Loggable loggable, Object[] args) {
    StringBuilder sb = new StringBuilder();
    sb.append(prefix).append("调用方法 ").append(signature);

    if (loggable.includeArgs() && args != null && args.length > 0) {
      sb.append(", 参数: ").append(Arrays.toString(args));
    }

    return sb.toString();
  }

  private String buildExitMessage(
      String prefix, String signature, Loggable loggable, Object result, long duration) {
    StringBuilder sb = new StringBuilder();
    sb.append(prefix).append("方法 ").append(signature).append(" 完成");

    if (loggable.includeTime()) {
      sb.append(" (耗时 ").append(duration).append("ms)");
    }

    if (loggable.includeResult() && result != null) {
      sb.append(", 返回: ").append(formatResult(result));
    }

    return sb.toString();
  }

  private String formatResult(Object result) {
    if (result == null) {
      return "null";
    }
    String str = result.toString();
    if (str.length() > 200) {
      return str.substring(0, 200) + "...";
    }
    return str;
  }

  private void log(LogLevel level, String message) {
    switch (level) {
      case TRACE -> LOGGER.trace(message);
      case DEBUG -> LOGGER.debug(message);
      case INFO -> LOGGER.info(message);
      case WARN -> LOGGER.warn(message);
      case ERROR -> LOGGER.error(message);
    }
  }

  private void logError(String format, Object... args) {
    LOGGER.error(format, args);
  }
}
