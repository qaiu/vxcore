package cn.qaiu.vx.core.util;

import cn.qaiu.vx.core.annotations.App;
import java.util.concurrent.atomic.AtomicReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 主类持有器 保存应用主类信息，供框架其他组件使用
 *
 * @author <a href="https://qaiu.top">QAIU</a>
 */
public class MainClassHolder {

  private static final Logger LOGGER = LoggerFactory.getLogger(MainClassHolder.class);

  private static final AtomicReference<Class<?>> MAIN_CLASS = new AtomicReference<>();

  private MainClassHolder() {}

  /**
   * 设置主类
   *
   * @param mainClass 主类
   */
  public static void setMainClass(Class<?> mainClass) {
    MAIN_CLASS.set(mainClass);
    LOGGER.debug("Main class set: {}", mainClass != null ? mainClass.getName() : "null");
  }

  /**
   * 获取主类
   *
   * @return 主类，如果未设置则返回null
   */
  public static Class<?> getMainClass() {
    return MAIN_CLASS.get();
  }

  /** 从堆栈跟踪中检测并设置主类 查找带有 @App 注解的主类 */
  public static void detectAndSetMainClass() {
    try {
      StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();

      // 策略1：查找main方法所在的带@App注解的类
      for (StackTraceElement element : stackTrace) {
        if ("main".equals(element.getMethodName())) {
          String className = element.getClassName();
          try {
            Class<?> clazz = Class.forName(className);
            if (clazz.isAnnotationPresent(App.class)) {
              setMainClass(clazz);
              LOGGER.info("Detected main class with @App annotation: {}", className);
              return;
            }
          } catch (ClassNotFoundException e) {
            LOGGER.debug("Class not found: {}", className);
          }
        }
      }

      // 策略2：遍历整个堆栈查找任何带@App注解的类（处理调试器或特殊启动器情况）
      for (StackTraceElement element : stackTrace) {
        String className = element.getClassName();
        // 跳过系统类和框架内部类
        if (className.startsWith("java.")
            || className.startsWith("sun.")
            || className.startsWith("jdk.")
            || className.startsWith("cn.qaiu.vx.core.")
            || className.startsWith("io.vertx.")
            || className.startsWith("org.junit.")
            || className.startsWith("com.intellij.")
            || className.startsWith("org.apache.maven.")) {
          continue;
        }

        try {
          Class<?> clazz = Class.forName(className);
          if (clazz.isAnnotationPresent(App.class)) {
            setMainClass(clazz);
            LOGGER.info("Detected class with @App annotation from stack: {}", className);
            return;
          }
        } catch (ClassNotFoundException | NoClassDefFoundError e) {
          LOGGER.debug("Class not loadable: {}", className);
        }
      }

      LOGGER.warn("No main class with @App annotation found in stack trace");
    } catch (Exception e) {
      LOGGER.warn("Failed to detect main class: {}", e.getMessage());
    }
  }

  /** 清除主类信息（用于测试） */
  public static void clear() {
    MAIN_CLASS.set(null);
  }
}
