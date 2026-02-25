package cn.qaiu.vx.core.aop;

import cn.qaiu.vx.core.aop.annotation.Aspect;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 切面注册表
 *
 * <p>负责扫描、存储和管理所有的切面类。 是 AOP 框架的核心组件之一。
 *
 * @author qaiu
 * @since 1.0.0
 */
public class AspectRegistry {

  private static final Logger LOGGER = LoggerFactory.getLogger(AspectRegistry.class);

  private static final AspectRegistry INSTANCE = new AspectRegistry();

  private final Map<Class<?>, AspectMetadata> aspectMap = new ConcurrentHashMap<>();
  private final List<AspectMetadata> sortedAspects = new ArrayList<>();
  private volatile boolean initialized = false;

  private AspectRegistry() {}

  public static AspectRegistry getInstance() {
    return INSTANCE;
  }

  /**
   * 扫描并注册指定包下的所有切面类
   *
   * @param basePackage 基础包名
   */
  public synchronized void scan(String basePackage) {
    if (basePackage == null || basePackage.isEmpty()) {
      LOGGER.warn("扫描包名为空，跳过切面扫描");
      return;
    }

    LOGGER.info("开始扫描切面类，包路径: {}", basePackage);

    try {
      Reflections reflections = new Reflections(basePackage);
      Set<Class<?>> aspectClasses = reflections.getTypesAnnotatedWith(Aspect.class);

      for (Class<?> aspectClass : aspectClasses) {
        registerAspect(aspectClass);
      }

      // 按优先级排序
      sortAspects();

      initialized = true;
      LOGGER.info("切面扫描完成，共注册 {} 个切面", aspectMap.size());
    } catch (Exception e) {
      LOGGER.error("扫描切面类时发生错误", e);
    }
  }

  /**
   * 注册一个切面类
   *
   * @param aspectClass 切面类
   */
  public void registerAspect(Class<?> aspectClass) {
    if (aspectMap.containsKey(aspectClass)) {
      LOGGER.debug("切面类已注册，跳过: {}", aspectClass.getName());
      return;
    }

    try {
      Object aspectInstance = aspectClass.getDeclaredConstructor().newInstance();
      AspectMetadata metadata = new AspectMetadata(aspectClass, aspectInstance);
      aspectMap.put(aspectClass, metadata);

      // 重新排序切面列表
      sortAspects();

      LOGGER.debug("注册切面: {} (order={})", aspectClass.getSimpleName(), metadata.getOrder());
    } catch (Exception e) {
      LOGGER.error("创建切面实例失败: {}", aspectClass.getName(), e);
    }
  }

  /**
   * 注册一个切面实例
   *
   * @param aspectInstance 切面实例
   */
  public void registerAspect(Object aspectInstance) {
    Class<?> aspectClass = aspectInstance.getClass();
    if (!aspectClass.isAnnotationPresent(Aspect.class)) {
      LOGGER.warn("类 {} 没有 @Aspect 注解，跳过注册", aspectClass.getName());
      return;
    }

    if (aspectMap.containsKey(aspectClass)) {
      LOGGER.debug("切面类已注册，跳过: {}", aspectClass.getName());
      return;
    }

    AspectMetadata metadata = new AspectMetadata(aspectClass, aspectInstance);
    aspectMap.put(aspectClass, metadata);

    // 重新排序切面列表
    sortAspects();

    LOGGER.debug("注册切面实例: {} (order={})", aspectClass.getSimpleName(), metadata.getOrder());
  }

  /** 按优先级排序切面 */
  private void sortAspects() {
    sortedAspects.clear();
    sortedAspects.addAll(aspectMap.values());
    sortedAspects.sort(Comparator.comparingInt(AspectMetadata::getOrder));
  }

  /**
   * 获取所有已排序的切面
   *
   * @return 按优先级排序的切面列表
   */
  public List<AspectMetadata> getSortedAspects() {
    return Collections.unmodifiableList(sortedAspects);
  }

  /**
   * 获取指定切面类的元数据
   *
   * @param aspectClass 切面类
   * @return 切面元数据，如果不存在返回null
   */
  public AspectMetadata getAspect(Class<?> aspectClass) {
    return aspectMap.get(aspectClass);
  }

  /**
   * 检查是否已初始化
   *
   * @return 如果已初始化返回true
   */
  public boolean isInitialized() {
    return initialized;
  }

  /**
   * 获取已注册的切面数量
   *
   * @return 切面数量
   */
  public int getAspectCount() {
    return aspectMap.size();
  }

  /** 清空所有注册的切面 */
  public synchronized void clear() {
    aspectMap.clear();
    sortedAspects.clear();
    initialized = false;
    LOGGER.debug("已清空所有注册的切面");
  }
}
