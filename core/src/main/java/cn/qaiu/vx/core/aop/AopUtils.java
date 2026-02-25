package cn.qaiu.vx.core.aop;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AOP 工具类
 *
 * <p>提供简化的 AOP 操作方法。
 *
 * @author qaiu
 * @since 1.0.0
 */
public final class AopUtils {

  private static final Logger LOGGER = LoggerFactory.getLogger(AopUtils.class);

  private AopUtils() {
    // 工具类不允许实例化
  }

  /**
   * 为目标对象创建 AOP 代理
   *
   * @param target 目标对象
   * @param <T> 目标类型
   * @return 代理对象，如果不需要代理则返回原对象
   */
  public static <T> T proxy(T target) {
    return AspectProcessor.getInstance().createProxy(target);
  }

  /**
   * 为目标类创建 AOP 代理实例
   *
   * @param targetClass 目标类
   * @param <T> 目标类型
   * @return 代理实例
   */
  public static <T> T proxy(Class<T> targetClass) {
    return AspectProcessor.getInstance().createProxy(targetClass);
  }

  /**
   * 注册切面类
   *
   * @param aspectClass 切面类
   */
  public static void registerAspect(Class<?> aspectClass) {
    AspectRegistry.getInstance().registerAspect(aspectClass);
  }

  /**
   * 注册切面实例
   *
   * @param aspectInstance 切面实例
   */
  public static void registerAspect(Object aspectInstance) {
    AspectRegistry.getInstance().registerAspect(aspectInstance);
  }

  /**
   * 扫描并注册指定包下的所有切面
   *
   * @param basePackage 基础包名
   */
  public static void scanAspects(String basePackage) {
    AspectRegistry.getInstance().scan(basePackage);
  }

  /**
   * 初始化 AOP 框架
   *
   * <p>安装 Byte Buddy Agent 并准备 AOP 基础设施。 建议在应用启动时调用。
   */
  public static void initialize() {
    AspectProcessor.getInstance().installAgent();
    LOGGER.info("AOP 框架初始化完成");
  }

  /**
   * 检查 AOP 是否已初始化
   *
   * @return 如果已初始化返回 true
   */
  public static boolean isInitialized() {
    return AspectRegistry.getInstance().isInitialized();
  }

  /**
   * 检查 Byte Buddy Agent 是否已安装
   *
   * @return 如果已安装返回 true
   */
  public static boolean isAgentInstalled() {
    return AspectProcessor.getInstance().isAgentInstalled();
  }
}
