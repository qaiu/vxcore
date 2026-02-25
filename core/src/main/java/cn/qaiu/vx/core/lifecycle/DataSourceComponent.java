package cn.qaiu.vx.core.lifecycle;

import cn.qaiu.vx.core.annotations.App;
import cn.qaiu.vx.core.config.DataSourceConfigResolver;
import cn.qaiu.vx.core.spi.OrmSyncProvider;
import cn.qaiu.vx.core.spi.OrmSyncProviderRegistry;
import cn.qaiu.vx.core.util.MainClassHolder;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 数据源管理组件 负责多数据源的初始化、管理和生命周期 
 * 使用接口抽象避免循环依赖
 * 
 * <p>使用 {@link DataSourceConfigResolver} 解析数据源配置，
 * 支持多种配置格式（database/datasources/dataSource）和配置项别名。
 *
 * @author <a href="https://qaiu.top">QAIU</a>
 */
public class DataSourceComponent implements LifecycleComponent {

  private static final Logger LOGGER = LoggerFactory.getLogger(DataSourceComponent.class);

  private Vertx vertx;
  private DataSourceProvider dataSourceProvider;
  private JsonObject globalConfig;

  @Override
  public Future<Void> initialize(Vertx vertx, JsonObject config) {
    this.vertx = vertx;
    this.globalConfig = config != null ? config.copy() : new JsonObject();

    return Future.future(
        promise -> {
          try {
            LOGGER.info("Initializing DataSource component...");
            LOGGER.info("Config keys: {}", config.fieldNames());

            // 使用 DataSourceConfigResolver 解析配置（支持别名和多种配置格式）
            DataSourceConfigResolver configResolver = new DataSourceConfigResolver(config);
            JsonObject databaseConfig = configResolver.resolveDataSourcesConfig();
            
            if (databaseConfig == null || databaseConfig.isEmpty()) {
              LOGGER.info("No database configuration found, skipping datasource initialization");
              promise.complete();
              return;
            }
            LOGGER.info("Found datasource configuration with {} entries: {}", 
                databaseConfig.fieldNames().size(), databaseConfig.fieldNames());

            // 使用SPI模式查找数据源提供者
            DataSourceProviderRegistry registry = DataSourceProviderRegistry.getInstance();
            registry.initialize();

            // 查找支持的数据源提供者
            DataSourceProvider provider = findSupportedProvider(registry, databaseConfig);
            if (provider != null) {
              this.dataSourceProvider = provider;
              LOGGER.info(
                  "Found DataSource provider: {} for database configuration", provider.getName());

              // 在initialize阶段就完成数据源和SQL执行器的初始化
              LOGGER.info(
                  "Initializing datasources and SQL executors during component initialization...");
              provider
                  .initializeDataSources(vertx, config)
                  .compose(v -> {
                    LOGGER.info(
                        "DataSources and SQL executors initialized successfully");
                    // 数据源初始化完成后，执行ORM同步
                    return executeOrmSync();
                  })
                  .onSuccess(
                      v -> {
                        LOGGER.info("DataSource component initialization completed");
                        promise.complete();
                      })
                  .onFailure(
                      error -> {
                        LOGGER.error(
                            "Failed to initialize datasources during component initialization",
                            error);
                        promise.fail(error);
                      });
            } else {
              LOGGER.warn(
                  "No DataSource provider found for database configuration, datasource will not be initialized");
              promise.complete();
            }
          } catch (Exception e) {
            LOGGER.error("Failed to initialize datasource component", e);
            promise.fail(e);
          }
        });
  }

  @Override
  public Future<Void> start() {
    return Future.future(
        promise -> {
          try {
            LOGGER.info("Starting DataSource component...");

            if (dataSourceProvider != null) {
              // 数据源和ORM同步已在initialize阶段完成
              LOGGER.info(
                  "DataSource component started successfully with provider: {}",
                  dataSourceProvider.getName());
            } else {
              LOGGER.info("No DataSource provider available, component started without datasource");
            }
            promise.complete();
          } catch (Exception e) {
            LOGGER.error("Failed to start datasource component", e);
            promise.fail(e);
          }
        });
  }

  /**
   * 执行ORM同步（通过SPI发现的OrmSyncProvider）
   */
  private Future<Void> executeOrmSync() {
    return Future.future(promise -> {
      try {
        // 获取主类
        Class<?> mainClass = findMainClass();
        if (mainClass == null) {
          LOGGER.info("No main class found with @App annotation, skipping ORM sync");
          promise.complete();
          return;
        }
        
        LOGGER.info("Found main class for ORM sync: {}", mainClass.getName());
        
        // 初始化OrmSyncProviderRegistry并获取提供者
        OrmSyncProviderRegistry registry = OrmSyncProviderRegistry.getInstance();
        registry.initialize();
        
        OrmSyncProvider provider = registry.getPreferredProvider();
        if (provider == null) {
          LOGGER.info("No OrmSyncProvider found, skipping ORM sync");
          promise.complete();
          return;
        }
        
        LOGGER.info("Using OrmSyncProvider: {}", provider.getName());
        
        // 检查是否需要同步
        if (!provider.shouldSync(mainClass, globalConfig)) {
          LOGGER.info("ORM sync not required based on configuration");
          promise.complete();
          return;
        }
        
        // 执行同步
        LOGGER.info("Starting ORM/DDL synchronization...");
        provider.sync(vertx, mainClass, globalConfig)
            .onSuccess(result -> {
              if (result.isSuccess()) {
                LOGGER.info("ORM sync completed successfully: {}", result.getMessage());
              } else {
                LOGGER.warn("ORM sync completed with status: {}", result.getMessage());
              }
              promise.complete();
            })
            .onFailure(err -> {
              LOGGER.error("ORM sync failed", err);
              promise.fail(err);
            });
            
      } catch (Exception e) {
        LOGGER.error("Failed to execute ORM sync", e);
        promise.fail(e);
      }
    });
  }
  
  /**
   * 查找主类（优先从MainClassHolder获取）
   */
  private Class<?> findMainClass() {
    // 1. 优先从MainClassHolder获取（在VXCoreApplication.run时已保存）
    Class<?> mainClass = MainClassHolder.getMainClass();
    if (mainClass != null) {
      return mainClass;
    }
    
    // 2. 尝试从堆栈跟踪中查找（作为fallback）
    try {
      StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
      
      for (StackTraceElement element : stackTrace) {
        if ("main".equals(element.getMethodName())) {
          String className = element.getClassName();
          try {
            Class<?> clazz = Class.forName(className);
            if (clazz.isAnnotationPresent(App.class)) {
              return clazz;
            }
          } catch (ClassNotFoundException e) {
            LOGGER.debug("Class not found: {}", className);
          }
        }
      }
    } catch (Exception e) {
      LOGGER.debug("Failed to find main class from stack trace: {}", e.getMessage());
    }
    
    // 3. 尝试从配置中获取
    if (globalConfig != null && globalConfig.containsKey("mainClass")) {
      try {
        String mainClassName = globalConfig.getString("mainClass");
        return Class.forName(mainClassName);
      } catch (ClassNotFoundException e) {
        LOGGER.warn("Main class from config not found: {}", e.getMessage());
      }
    }
    
    return null;
  }

  @Override
  public Future<Void> stop() {
    return Future.future(
        promise -> {
          try {
            LOGGER.info("Stopping DataSource component...");

            if (dataSourceProvider != null) {
              // 使用提供者关闭数据源
              dataSourceProvider
                  .closeAllDataSources()
                  .onSuccess(
                      v -> {
                        LOGGER.info("All datasources closed successfully");
                        promise.complete();
                      })
                  .onFailure(
                      error -> {
                        LOGGER.error("Failed to close datasources", error);
                        promise.fail(error);
                      });
            } else {
              LOGGER.info("No datasource provider to close");
              promise.complete();
            }
          } catch (Exception e) {
            LOGGER.error("Failed to stop datasource component", e);
            promise.fail(e);
          }
        });
  }

  @Override
  public String getName() {
    return "DataSourceComponent";
  }

  @Override
  public int getPriority() {
    return 20; // 在ConfigurationComponent之后，ServiceRegistryComponent之前
  }

  /**
   * 获取数据源提供者
   *
   * @return DataSourceProvider实例
   */
  public DataSourceProvider getDataSourceProvider() {
    return dataSourceProvider;
  }

  /**
   * 设置数据源提供者
   *
   * @param dataSourceProvider DataSourceProvider实例
   */
  public void setDataSourceProvider(DataSourceProvider dataSourceProvider) {
    this.dataSourceProvider = dataSourceProvider;
  }

  /**
   * 获取数据源管理器（通过提供者）
   *
   * @return DataSourceManagerInterface实例
   */
  public DataSourceManagerInterface getDataSourceManager() {
    if (dataSourceProvider != null) {
      return dataSourceProvider.createDataSourceManager(vertx);
    }
    return null;
  }

  /**
   * 设置数据源管理器（直接注入实现）
   *
   * @param dataSourceManager 数据源管理器实现
   */
  public void setDataSourceManager(DataSourceManagerInterface dataSourceManager) {
    // 这个方法主要用于测试或直接注入实现
    // 正常情况下应该通过DataSourceProvider来获取
    LOGGER.warn(
        "Direct injection of DataSourceManager is not recommended. Use DataSourceProvider instead.");
  }

  /**
   * 检查是否有数据源配置
   *
   * @return 是否有数据源配置
   */
  public boolean hasDataSourceConfig() {
    if (globalConfig == null) {
      return false;
    }
    JsonObject databaseConfig = globalConfig.getJsonObject("database");
    return databaseConfig != null && !databaseConfig.isEmpty();
  }

  /**
   * 查找支持的数据源提供者
   *
   * @param registry 提供者注册表
   * @param databaseConfig 数据库配置
   * @return 支持的数据源提供者
   */
  private DataSourceProvider findSupportedProvider(
      DataSourceProviderRegistry registry, JsonObject databaseConfig) {
    // 遍历所有数据源配置，查找支持的提供者
    for (String dataSourceName : databaseConfig.fieldNames()) {
      JsonObject dataSourceConfig = databaseConfig.getJsonObject(dataSourceName);
      if (dataSourceConfig != null) {
        String type = dataSourceConfig.getString("type");
        if (type != null) {
          DataSourceProvider provider = registry.getProviderByType(type);
          if (provider != null) {
            LOGGER.info("Found provider by type '{}': {}", type, provider.getName());
            return provider;
          }
        }
        
        // 如果没有指定 type，检查是否有 jdbcUrl 或 url 来推断数据源类型
        String jdbcUrl = dataSourceConfig.getString("jdbcUrl");
        if (jdbcUrl == null) {
          jdbcUrl = dataSourceConfig.getString("url");
        }
        if (jdbcUrl != null) {
          // 从 JDBC URL 推断数据库类型
          String inferredType = inferDatabaseType(jdbcUrl);
          if (inferredType != null) {
            DataSourceProvider provider = registry.getProviderByType(inferredType);
            if (provider != null) {
              LOGGER.info("Found provider by inferred type '{}' from URL: {}", inferredType, provider.getName());
              return provider;
            }
          }
        }
        
        // 如果仍然没找到，尝试使用任何可用的提供者
        var allProviders = registry.getAllProviders();
        if (!allProviders.isEmpty()) {
          DataSourceProvider defaultProvider = allProviders.get(0);
          LOGGER.info("Using default provider: {}", defaultProvider.getName());
          return defaultProvider;
        }
      }
    }
    return null;
  }
  
  /**
   * 从 JDBC URL 推断数据库类型
   */
  private String inferDatabaseType(String jdbcUrl) {
    if (jdbcUrl == null) {
      return null;
    }
    String lowerUrl = jdbcUrl.toLowerCase(java.util.Locale.ROOT);
    if (lowerUrl.contains(":h2:")) {
      return "h2";
    } else if (lowerUrl.contains(":mysql:")) {
      return "mysql";
    } else if (lowerUrl.contains(":postgresql:") || lowerUrl.contains(":postgres:")) {
      return "postgresql";
    } else if (lowerUrl.contains(":oracle:")) {
      return "oracle";
    } else if (lowerUrl.contains(":sqlserver:") || lowerUrl.contains(":microsoft:")) {
      return "sqlserver";
    } else if (lowerUrl.contains(":sqlite:")) {
      return "sqlite";
    }
    return null;
  }
}
