package cn.qaiu.vx.core.aop;

import cn.qaiu.vx.core.lifecycle.LifecycleComponent;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AOP 生命周期组件
 *
 * <p>负责在框架启动时初始化 AOP 框架：
 *
 * <ul>
 *   <li>安装 Byte Buddy Agent（如果可用）
 *   <li>扫描并注册切面类
 *   <li>配置切面处理器
 * </ul>
 *
 * <p>配置示例（application.yml）：
 *
 * <pre>
 * aop:
 *   enabled: true
 *   scan-packages:
 *     - cn.qaiu.example.aspect
 *   agent:
 *     enabled: true
 * </pre>
 *
 * @author qaiu
 * @since 1.0.0
 */
public class AopComponent implements LifecycleComponent {

  private static final Logger LOGGER = LoggerFactory.getLogger(AopComponent.class);

  private static final String CONFIG_KEY = "aop";
  private static final String ENABLED_KEY = "enabled";
  private static final String SCAN_PACKAGES_KEY = "scan-packages";
  private static final String AGENT_ENABLED_KEY = "agent.enabled";

  private Vertx vertx;
  private JsonObject aopConfig;
  private boolean enabled = true;

  @Override
  public Future<Void> initialize(Vertx vertx, JsonObject config) {
    this.vertx = vertx;
    this.aopConfig = config.getJsonObject(CONFIG_KEY, new JsonObject()).copy();
    this.enabled = aopConfig.getBoolean(ENABLED_KEY, true);

    if (!enabled) {
      LOGGER.info("AOP 功能已禁用");
      return Future.succeededFuture();
    }

    LOGGER.info("初始化 AOP 组件...");

    // 安装 Agent（如果配置启用）
    boolean agentEnabled = aopConfig.getBoolean(AGENT_ENABLED_KEY, true);
    if (agentEnabled) {
      try {
        AspectProcessor.getInstance().installAgent();
      } catch (Exception e) {
        LOGGER.warn("Byte Buddy Agent 安装失败，将使用代理模式: {}", e.getMessage());
      }
    }

    // 扫描切面类
    scanAspects(config);

    return Future.succeededFuture();
  }

  /** 扫描切面类 */
  private void scanAspects(JsonObject config) {
    AspectRegistry registry = AspectRegistry.getInstance();

    // 从配置获取扫描包
    if (aopConfig.containsKey(SCAN_PACKAGES_KEY)) {
      aopConfig
          .getJsonArray(SCAN_PACKAGES_KEY)
          .forEach(
              pkg -> {
                registry.scan(pkg.toString());
              });
    }

    // 从全局配置获取基础包名
    String basePackage = config.getString("base-package", "");
    if (!basePackage.isEmpty()) {
      registry.scan(basePackage);
    }

    LOGGER.info("AOP 切面扫描完成，共注册 {} 个切面", registry.getAspectCount());
  }

  @Override
  public Future<Void> start() {
    if (!enabled) {
      return Future.succeededFuture();
    }

    LOGGER.info("AOP 组件启动完成");
    return Future.succeededFuture();
  }

  @Override
  public Future<Void> stop() {
    if (!enabled) {
      return Future.succeededFuture();
    }

    // 清理缓存
    AspectProcessor.getInstance().clearCache();
    AspectRegistry.getInstance().clear();

    LOGGER.info("AOP 组件已停止");
    return Future.succeededFuture();
  }

  @Override
  public String getName() {
    return "AopComponent";
  }

  @Override
  public int getPriority() {
    // 优先级设为 12，在服务注册组件之前初始化
    return 12;
  }

  /**
   * 获取 AOP 配置
   *
   * @return AOP 配置对象的不可变副本
   */
  public JsonObject getAopConfig() {
    return aopConfig == null ? new JsonObject() : aopConfig.copy();
  }

  /**
   * 检查 AOP 是否启用
   *
   * @return 如果启用返回 true
   */
  public boolean isEnabled() {
    return enabled;
  }
}
