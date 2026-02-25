package cn.qaiu.vx.core.lifecycle;

import static cn.qaiu.vx.core.util.ConfigConstant.*;

import cn.qaiu.vx.core.config.ConfigAliasRegistry;
import cn.qaiu.vx.core.config.ConfigBinder;
import cn.qaiu.vx.core.config.ConfigResolver;
import cn.qaiu.vx.core.util.AutoScanPathDetector;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.LocalMap;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 配置管理组件 负责配置的加载、验证和分发
 *
 * <p>使用 {@link ConfigAliasRegistry} 提供配置别名机制， 使用 {@link ConfigResolver} 提供别名感知的配置值获取， 使用 {@link
 * ConfigBinder} 提供类型安全的配置绑定。
 *
 * @author <a href="https://qaiu.top">QAIU</a>
 */
public class ConfigurationComponent implements LifecycleComponent {

  private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationComponent.class);

  private Vertx vertx;
  private JsonObject globalConfig;
  private ConfigResolver configResolver;
  private ConfigBinder configBinder;

  @Override
  public Future<Void> initialize(Vertx vertx, JsonObject config) {
    this.vertx = vertx;
    this.globalConfig = config != null ? config.copy() : new JsonObject();

    return Future.future(
        promise -> {
          try {
            // 0. 初始化配置解析器和绑定器
            this.configResolver = new ConfigResolver(config);
            this.configBinder = new ConfigBinder();

            // 1. 自动检测扫描路径
            autoDetectScanPaths(config);

            // 2. 验证配置
            validateConfiguration(config);

            // 3. 存储配置到共享数据
            storeConfiguration(config);

            LOGGER.info("Configuration component initialized successfully");
            LOGGER.info(
                "ConfigAliasRegistry initialized with {} alias groups",
                ConfigAliasRegistry.getInstance().getAllAliasGroups().size());
            promise.complete();
          } catch (Exception e) {
            LOGGER.error("Failed to initialize configuration component", e);
            promise.fail(e);
          }
        });
  }

  /** 自动检测扫描路径 */
  private void autoDetectScanPaths(JsonObject config) {
    try {
      Set<String> autoDetectedPaths = AutoScanPathDetector.detectScanPathsFromStackTrace();

      JsonObject customConfig = config.getJsonObject(CUSTOM);
      if (customConfig != null && customConfig.containsKey(BASE_LOCATIONS)) {
        String configuredPaths = customConfig.getString(BASE_LOCATIONS);
        LOGGER.info("Using configured baseLocations: {}", configuredPaths);
        LOGGER.info(
            "Auto-detected paths (not used): {}",
            AutoScanPathDetector.formatScanPaths(autoDetectedPaths));
      } else {
        String autoPaths = AutoScanPathDetector.formatScanPaths(autoDetectedPaths);

        if (customConfig == null) {
          customConfig = new JsonObject();
          config.put(CUSTOM, customConfig);
        }

        customConfig.put(BASE_LOCATIONS, autoPaths);

        if (isAppAnnotationUsed(autoDetectedPaths)) {
          LOGGER.info("App-annotated baseLocations: {}", autoPaths);
          LOGGER.info("Using @App annotation configuration for scan paths.");
        } else {
          LOGGER.info("Auto-configured baseLocations: {}", autoPaths);
          LOGGER.info(
              "You can override this by setting 'baseLocations' in your config file or using @App annotation.");
        }
      }
    } catch (Exception e) {
      LOGGER.warn("Failed to auto-detect scan paths, using default: cn.qaiu", e);

      JsonObject customConfig = config.getJsonObject(CUSTOM);
      if (customConfig == null) {
        customConfig = new JsonObject();
        config.put(CUSTOM, customConfig);
      }
      customConfig.put(BASE_LOCATIONS, "cn.qaiu");
    }
  }

  /** 检查是否使用了@App注解 */
  private boolean isAppAnnotationUsed(Set<String> scanPaths) {
    try {
      StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
      for (StackTraceElement element : stackTrace) {
        if ("main".equals(element.getMethodName())) {
          String className = element.getClassName();
          Class<?> mainClass = Class.forName(className);
          return mainClass.isAnnotationPresent(cn.qaiu.vx.core.annotations.App.class);
        }
      }
    } catch (Exception e) {
      LOGGER.debug("Failed to check @App annotation", e);
    }
    return false;
  }

  /** 验证配置 */
  private void validateConfiguration(JsonObject config) {
    // 验证服务器配置（使用ConfigResolver支持别名）
    JsonObject serverConfig = config.getJsonObject("server");
    if (serverConfig == null) {
      LOGGER.warn("No 'server' configuration found, will use default settings");
      // 创建默认服务器配置
      serverConfig = new JsonObject().put("port", 8080).put("host", "0.0.0.0");
      config.put("server", serverConfig);
    }

    // 使用ConfigResolver获取端口配置（支持port/serverPort别名）
    ConfigResolver serverResolver = new ConfigResolver(serverConfig);
    Integer port = serverResolver.getInteger("port");
    if (port == null) {
      serverConfig.put("port", 8080);
      LOGGER.info("Server port not specified, using default: 8080");
    }

    // 验证数据源配置（使用别名机制兼容多种配置格式）
    // 优先级: database > datasources > dataSource (通过别名机制统一处理)
    JsonObject datasources = configResolver.getJsonObject("database");
    if (datasources == null) {
      datasources = configResolver.getJsonObject("datasources");
    }
    // 兼容旧的 dataSource 配置格式
    JsonObject oldDataSource = configResolver.getJsonObject("dataSource");

    if ((datasources == null || datasources.isEmpty()) && oldDataSource != null) {
      LOGGER.info("Using legacy 'dataSource' configuration format");
      // 转换旧格式为新格式
      datasources = new JsonObject().put("primary", oldDataSource);
      config.put("datasources", datasources);
    }

    if (datasources == null || datasources.isEmpty()) {
      LOGGER.warn("No datasource configuration found");
    }

    LOGGER.info("Configuration validation completed");
  }

  /** 存储配置到共享数据 */
  private void storeConfiguration(JsonObject config) {
    LocalMap<String, Object> localMap = vertx.sharedData().getLocalMap(LOCAL);
    localMap.put(GLOBAL_CONFIG, config);
    localMap.put(SERVER, config.getJsonObject("server"));

    JsonObject customConfig = config.getJsonObject(CUSTOM);
    if (customConfig != null) {
      localMap.put(CUSTOM_CONFIG, customConfig);
    }

    LOGGER.info("Configuration stored in shared data");
  }

  /**
   * 获取配置解析器
   *
   * @return ConfigResolver实例
   */
  public ConfigResolver getConfigResolver() {
    return configResolver;
  }

  /**
   * 获取配置绑定器
   *
   * @return ConfigBinder实例
   */
  public ConfigBinder getConfigBinder() {
    return configBinder;
  }

  /**
   * 获取全局配置
   *
   * @return 全局配置 JsonObject 的不可变副本
   */
  public JsonObject getGlobalConfig() {
    return globalConfig != null ? globalConfig.copy() : new JsonObject();
  }

  @Override
  public int getPriority() {
    return 10; // 最高优先级
  }
}
