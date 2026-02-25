package cn.qaiu.vx.core.config;

import io.vertx.core.json.JsonObject;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 数据源配置解析器 专门处理多种数据源配置格式的兼容性
 *
 * @author <a href="https://qaiu.top">QAIU</a>
 */
public class DataSourceConfigResolver {

  private static final Logger LOGGER = LoggerFactory.getLogger(DataSourceConfigResolver.class);

  /** 数据源配置键的优先级顺序 */
  private static final List<String> DATASOURCE_CONFIG_KEYS =
      Arrays.asList(
          "datasources", // 新格式（推荐）
          "database", // 简化格式
          "dataSource" // 旧格式（兼容）
          );

  private final ConfigResolver resolver;
  private final ConfigAliasRegistry aliasRegistry;

  public DataSourceConfigResolver(JsonObject config) {
    this.resolver = new ConfigResolver(config);
    this.aliasRegistry = ConfigAliasRegistry.getInstance();
  }

  /**
   * 获取所有数据源配置 自动识别配置格式并转换为统一格式
   *
   * @return 数据源名称 -> 数据源配置 的映射
   */
  public Map<String, DataSourceConfig> resolveAllDataSources() {
    Map<String, DataSourceConfig> result = new LinkedHashMap<>();

    // 按优先级查找数据源配置
    JsonObject rawConfig = findDataSourceConfig();
    if (rawConfig == null || rawConfig.isEmpty()) {
      LOGGER.warn("No datasource configuration found");
      return result;
    }

    // 解析每个数据源
    for (String name : rawConfig.fieldNames()) {
      Object value = rawConfig.getValue(name);
      if (value instanceof JsonObject) {
        DataSourceConfig dsConfig = parseDataSourceConfig(name, (JsonObject) value);
        if (dsConfig != null) {
          result.put(name, dsConfig);
          LOGGER.debug("Resolved datasource config: {}", name);
        }
      }
    }

    return result;
  }

  /**
   * 获取主数据源配置
   *
   * @return 主数据源配置
   */
  public DataSourceConfig resolvePrimaryDataSource() {
    Map<String, DataSourceConfig> all = resolveAllDataSources();

    // 优先返回名为 "primary" 的数据源
    if (all.containsKey("primary")) {
      return all.get("primary");
    }

    // 否则返回第一个数据源
    return all.isEmpty() ? null : all.values().iterator().next();
  }

  /** 查找数据源配置节点 */
  private JsonObject findDataSourceConfig() {
    for (String key : DATASOURCE_CONFIG_KEYS) {
      JsonObject config = resolver.getJsonObject(key);
      if (config != null && !config.isEmpty()) {
        LOGGER.info("Found datasource config under key: '{}'", key);

        // 检查是否是单数据源格式（直接包含 url/jdbcUrl）
        if (isSingleDataSourceFormat(config)) {
          // 转换为多数据源格式
          return new JsonObject().put("primary", config);
        }

        return config;
      }
    }
    return null;
  }

  /**
   * 获取数据源配置JsonObject 返回统一的多数据源格式配置
   *
   * @return 数据源配置JsonObject（多数据源格式）
   */
  public JsonObject resolveDataSourcesConfig() {
    return findDataSourceConfig();
  }

  /** 检查是否是单数据源格式 */
  private boolean isSingleDataSourceFormat(JsonObject config) {
    // 如果直接包含 url 或 jdbcUrl，说明是单数据源格式
    return config.containsKey("url")
        || config.containsKey("jdbcUrl")
        || config.containsKey("jdbc-url");
  }

  /** 解析单个数据源配置 */
  private DataSourceConfig parseDataSourceConfig(String name, JsonObject config) {
    ConfigResolver dsResolver = new ConfigResolver(config, aliasRegistry);

    DataSourceConfig dsConfig = new DataSourceConfig();
    dsConfig.setName(name);

    // 使用别名解析URL
    String url = dsResolver.getString("url");
    dsConfig.setUrl(url);

    // 自动推断数据库类型
    String type = dsResolver.getString("type");
    if (type == null && url != null) {
      type = inferDatabaseType(url);
      LOGGER.debug("Inferred database type '{}' from URL for datasource: {}", type, name);
    }
    dsConfig.setType(type);

    // 解析其他配置
    dsConfig.setDriverClassName(dsResolver.getString("driverClassName"));
    dsConfig.setUsername(dsResolver.getString("username"));
    dsConfig.setPassword(dsResolver.getString("password"));
    dsConfig.setMaxPoolSize(dsResolver.getInteger("maxPoolSize", 10));
    dsConfig.setMinPoolSize(dsResolver.getInteger("minPoolSize", 2));

    return dsConfig;
  }

  /** 从JDBC URL推断数据库类型 */
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
    } else if (lowerUrl.contains(":mariadb:")) {
      return "mariadb";
    }
    return null;
  }

  /** 数据源配置类 */
  public static class DataSourceConfig {
    private String name;
    private String type;
    private String url;
    private String driverClassName;
    private String username;
    private String password;
    private Integer maxPoolSize;
    private Integer minPoolSize;

    // Getters and Setters
    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public String getType() {
      return type;
    }

    public void setType(String type) {
      this.type = type;
    }

    public String getUrl() {
      return url;
    }

    public void setUrl(String url) {
      this.url = url;
    }

    public String getDriverClassName() {
      return driverClassName;
    }

    public void setDriverClassName(String driverClassName) {
      this.driverClassName = driverClassName;
    }

    public String getUsername() {
      return username;
    }

    public void setUsername(String username) {
      this.username = username;
    }

    public String getPassword() {
      return password;
    }

    public void setPassword(String password) {
      this.password = password;
    }

    public Integer getMaxPoolSize() {
      return maxPoolSize;
    }

    public void setMaxPoolSize(Integer maxPoolSize) {
      this.maxPoolSize = maxPoolSize;
    }

    public Integer getMinPoolSize() {
      return minPoolSize;
    }

    public void setMinPoolSize(Integer minPoolSize) {
      this.minPoolSize = minPoolSize;
    }

    /** 转换为JsonObject */
    public JsonObject toJsonObject() {
      JsonObject json = new JsonObject();
      if (name != null) json.put("name", name);
      if (type != null) json.put("type", type);
      if (url != null) json.put("url", url);
      if (driverClassName != null) json.put("driverClassName", driverClassName);
      if (username != null) json.put("username", username);
      if (password != null) json.put("password", password);
      if (maxPoolSize != null) json.put("maxPoolSize", maxPoolSize);
      if (minPoolSize != null) json.put("minPoolSize", minPoolSize);
      return json;
    }

    @Override
    public String toString() {
      return "DataSourceConfig{"
          + "name='"
          + name
          + '\''
          + ", type='"
          + type
          + '\''
          + ", url='"
          + url
          + '\''
          + ", username='"
          + username
          + '\''
          + '}';
    }
  }
}
