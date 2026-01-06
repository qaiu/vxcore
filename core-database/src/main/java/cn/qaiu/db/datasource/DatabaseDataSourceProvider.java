package cn.qaiu.db.datasource;

import cn.qaiu.vx.core.lifecycle.DataSourceManagerInterface;
import cn.qaiu.vx.core.lifecycle.DataSourceProvider;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 数据库数据源提供者实现 实现DataSourceProvider接口，为core模块提供数据源管理功能
 *
 * @author <a href="https://qaiu.top">QAIU</a>
 */
public class DatabaseDataSourceProvider implements DataSourceProvider {

  private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseDataSourceProvider.class);

  private DataSourceManagerInterface dataSourceManager;

  @Override
  public String getName() {
    return "database-datasource-provider";
  }

  @Override
  public boolean supports(String type) {
    // 支持常见的数据库类型
    return "h2".equalsIgnoreCase(type)
        || "mysql".equalsIgnoreCase(type)
        || "postgresql".equalsIgnoreCase(type)
        || "postgres".equalsIgnoreCase(type)
        || "oracle".equalsIgnoreCase(type)
        || "sqlserver".equalsIgnoreCase(type);
  }

  @Override
  public DataSourceManagerInterface createDataSourceManager(Vertx vertx) {
    if (dataSourceManager == null) {
      dataSourceManager = cn.qaiu.db.datasource.DataSourceManager.getInstance();
    }
    return dataSourceManager;
  }

  @Override
  public Future<Void> initializeDataSources(Vertx vertx, JsonObject config) {
    LOGGER.info("Initializing datasources with provider: {}", getName());

    // 创建数据源管理器
    createDataSourceManager(vertx);

    // 获取数据库配置（兼容多种配置格式）
    // 优先级: database > datasources > dataSource
    JsonObject databaseConfig = config.getJsonObject("database");
    if (databaseConfig == null || databaseConfig.isEmpty()) {
      databaseConfig = config.getJsonObject("datasources");
    }
    if (databaseConfig == null || databaseConfig.isEmpty()) {
      // 兼容旧的 dataSource 配置格式
      JsonObject oldDataSource = config.getJsonObject("dataSource");
      if (oldDataSource != null && !oldDataSource.isEmpty()) {
        databaseConfig = new JsonObject().put("primary", oldDataSource);
        LOGGER.info("Using legacy 'dataSource' configuration format");
      }
    }
    if (databaseConfig == null || databaseConfig.isEmpty()) {
      LOGGER.warn("No database configuration found");
      return Future.succeededFuture();
    }
    LOGGER.info("Found database configuration with {} entries: {}", databaseConfig.fieldNames().size(), databaseConfig.fieldNames());

    // 注册所有数据源
    Future<Void> registrationFuture = Future.succeededFuture();
    for (String dataSourceName : databaseConfig.fieldNames()) {
      JsonObject dataSourceConfig = databaseConfig.getJsonObject(dataSourceName);
      if (dataSourceConfig != null) {
        String type = dataSourceConfig.getString("type");
        
        // 如果没有指定 type，从 jdbcUrl 推断
        if (type == null || type.isEmpty()) {
          String jdbcUrl = dataSourceConfig.getString("jdbcUrl");
          if (jdbcUrl == null) {
            jdbcUrl = dataSourceConfig.getString("url");
          }
          type = inferDatabaseType(jdbcUrl);
          LOGGER.info("Inferred database type '{}' from URL for datasource: {}", type, dataSourceName);
        }
        
        if (supports(type)) {
          // 转换为DataSourceConfig对象
          DataSourceConfig configObj = new DataSourceConfig();
          configObj.setName(dataSourceName);
          configObj.setType(type);
          // 兼容 url 和 jdbcUrl 两种配置方式
          String url = dataSourceConfig.getString("url");
          if (url == null) {
            url = dataSourceConfig.getString("jdbcUrl");
          }
          configObj.setUrl(url);
          configObj.setUsername(dataSourceConfig.getString("username"));
          configObj.setPassword(dataSourceConfig.getString("password"));

          // 设置连接池参数
          if (dataSourceConfig.containsKey("max_pool_size")) {
            configObj.setMaxPoolSize(dataSourceConfig.getInteger("max_pool_size"));
          }
          if (dataSourceConfig.containsKey("min_pool_size")) {
            configObj.setMinPoolSize(dataSourceConfig.getInteger("min_pool_size"));
          }

          final String dsName = dataSourceName;
          final JsonObject finalConfig = configObj.toJsonObject();
          registrationFuture =
              registrationFuture.compose(
                  v -> {
                    LOGGER.info("Registering datasource: {} with config: {}", dsName, finalConfig);
                    return dataSourceManager.registerDataSource(dsName, finalConfig);
                  });
        } else {
          LOGGER.warn("Unsupported datasource type: {} for datasource: {}", type, dataSourceName);
        }
      }
    }

    // 保存第一个数据源名称用于设置默认数据源
    final String firstDataSourceName = databaseConfig.fieldNames().iterator().next();
    
    // 初始化所有数据源
    return registrationFuture.compose(v -> {
      LOGGER.info("Initializing all registered datasources...");
      return dataSourceManager.initializeAllDataSources();
    }).compose(v -> {
      // 设置第一个数据源为默认数据源
      if (firstDataSourceName != null) {
        dataSourceManager.setDefaultDataSource(firstDataSourceName);
        LOGGER.info("Set default datasource to: {}", firstDataSourceName);
      }
      return Future.succeededFuture();
    });
  }
  
  /**
   * 从 JDBC URL 推断数据库类型
   */
  private String inferDatabaseType(String jdbcUrl) {
    if (jdbcUrl == null) {
      return null;
    }
    String lowerUrl = jdbcUrl.toLowerCase();
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

  @Override
  public Future<Void> closeAllDataSources() {
    if (dataSourceManager != null) {
      return dataSourceManager.closeAllDataSources();
    }
    return Future.succeededFuture();
  }

  @Override
  public Object getPool(String name) {
    if (dataSourceManager != null) {
      return dataSourceManager.getPool(name);
    }
    return null;
  }

  @Override
  public List<String> getDataSourceNames() {
    if (dataSourceManager != null) {
      return dataSourceManager.getDataSourceNames();
    }
    return List.of();
  }
}
