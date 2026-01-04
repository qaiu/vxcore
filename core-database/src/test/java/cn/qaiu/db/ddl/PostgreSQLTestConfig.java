package cn.qaiu.db.ddl;

import io.vertx.core.Vertx;
import io.vertx.jdbcclient.JDBCConnectOptions;
import io.vertx.jdbcclient.JDBCPool;
import io.vertx.sqlclient.PoolOptions;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * PostgreSQL测试配置 支持从环境变量（CI环境）或配置文件（本地开发）读取配置
 *
 * @author <a href="https://qaiu.top">QAIU</a>
 */
public class PostgreSQLTestConfig {

  /** 从环境变量获取配置值，如果不存在则返回默认值 */
  private static String getEnvOrDefault(String envKey, String defaultValue) {
    String value = System.getenv(envKey);
    return value != null && !value.isEmpty() ? value : defaultValue;
  }

  /** 检查是否在CI环境中（通过环境变量判断） */
  private static boolean isInCiEnvironment() {
    return System.getenv("CI") != null || System.getenv("POSTGRES_URL") != null;
  }

  private static Properties loadProperties() {
    Properties props = new Properties();

    // 优先从环境变量加载（CI环境）
    if (isInCiEnvironment()) {
      String postgresUrl = System.getenv("POSTGRES_URL");
      String postgresUser = getEnvOrDefault("POSTGRES_USER", "postgres");
      String postgresPassword = getEnvOrDefault("POSTGRES_PASSWORD", "postgres");

      if (postgresUrl != null && !postgresUrl.isEmpty()) {
        props.setProperty("postgresql.url", postgresUrl);
        props.setProperty("postgresql.user", postgresUser);
        props.setProperty("postgresql.password", postgresPassword);
        props.setProperty("postgresql.max_pool_size", "10");
        System.out.println("✅ PostgreSQL config loaded from environment variables (CI mode)");
        return props;
      }
    }

    // 从配置文件加载（本地开发环境）
    try (InputStream input =
        PostgreSQLTestConfig.class
            .getClassLoader()
            .getResourceAsStream("postgresql-test.properties")) {
      if (input != null) {
        props.load(input);
        System.out.println("✅ PostgreSQL config loaded from postgresql-test.properties");
      }
    } catch (IOException e) {
      System.out.println("⚠️ Failed to load postgresql-test.properties: " + e.getMessage());
    }
    return props;
  }

  /** 创建PostgreSQL连接池 */
  public static JDBCPool createPostgreSQLPool(Vertx vertx) {
    Properties props = loadProperties();

    if (!props.containsKey("postgresql.url")) {
      System.out.println("⚠️ PostgreSQL not configured (no env vars or properties file)");
      return null;
    }

    PoolOptions poolOptions =
        new PoolOptions()
            .setMaxSize(Integer.parseInt(props.getProperty("postgresql.max_pool_size", "10")));
    JDBCConnectOptions connectOptions =
        new JDBCConnectOptions()
            .setJdbcUrl(props.getProperty("postgresql.url"))
            .setUser(props.getProperty("postgresql.user"))
            .setPassword(props.getProperty("postgresql.password"));

    return JDBCPool.pool(vertx, connectOptions, poolOptions);
  }

  /** 创建PostgreSQL连接池（指定数据库名） */
  public static JDBCPool createPostgreSQLPool(Vertx vertx, String databaseName) {
    Properties props = loadProperties();

    if (!props.containsKey("postgresql.url")) {
      System.out.println("⚠️ PostgreSQL not configured (no env vars or properties file)");
      return null;
    }

    PoolOptions poolOptions =
        new PoolOptions()
            .setMaxSize(Integer.parseInt(props.getProperty("postgresql.max_pool_size", "10")));

    // 替换数据库名
    String url = props.getProperty("postgresql.url");
    String baseUrl = url.substring(0, url.lastIndexOf('/') + 1);
    String queryPart = url.contains("?") ? url.substring(url.indexOf('?')) : "";
    String newUrl = baseUrl + databaseName + queryPart;

    JDBCConnectOptions connectOptions =
        new JDBCConnectOptions()
            .setJdbcUrl(newUrl)
            .setUser(props.getProperty("postgresql.user"))
            .setPassword(props.getProperty("postgresql.password"));

    return JDBCPool.pool(vertx, connectOptions, poolOptions);
  }

  /** 检查PostgreSQL连接是否可用 */
  public static boolean isPostgreSQLAvailable() {
    try {
      // 尝试加载PostgreSQL驱动
      Class.forName("org.postgresql.Driver");
      // 检查配置是否存在（环境变量或配置文件）
      Properties props = loadProperties();
      return props.containsKey("postgresql.url");
    } catch (ClassNotFoundException e) {
      return false;
    }
  }
}
