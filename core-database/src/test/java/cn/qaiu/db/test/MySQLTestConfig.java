package cn.qaiu.db.test;

import io.vertx.core.Vertx;
import io.vertx.jdbcclient.JDBCConnectOptions;
import io.vertx.jdbcclient.JDBCPool;
import io.vertx.sqlclient.PoolOptions;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * MySQL测试配置工具类 支持从环境变量（CI环境）或配置文件（本地开发）读取配置
 *
 * @author <a href="https://qaiu.top">QAIU</a>
 */
public class MySQLTestConfig {

  /** 从环境变量获取配置值，如果不存在则返回默认值 */
  private static String getEnvOrDefault(String envKey, String defaultValue) {
    String value = System.getenv(envKey);
    return value != null && !value.isEmpty() ? value : defaultValue;
  }

  /** 检查是否在CI环境中（通过环境变量判断） */
  private static boolean isInCiEnvironment() {
    return System.getenv("CI") != null || System.getenv("MYSQL_URL") != null;
  }

  private static Properties loadProperties() {
    Properties props = new Properties();

    // 优先从环境变量加载（CI环境）
    if (isInCiEnvironment()) {
      String mysqlUrl = System.getenv("MYSQL_URL");
      String mysqlUser = getEnvOrDefault("MYSQL_USER", "root");
      String mysqlPassword = getEnvOrDefault("MYSQL_PASSWORD", "root");

      if (mysqlUrl != null && !mysqlUrl.isEmpty()) {
        props.setProperty("mysql.url", mysqlUrl);
        props.setProperty("mysql.user", mysqlUser);
        props.setProperty("mysql.password", mysqlPassword);
        props.setProperty("mysql.max_pool_size", "10");
        System.out.println("✅ MySQL config loaded from environment variables (CI mode)");
        return props;
      }
    }

    // 从配置文件加载（本地开发环境）
    try (InputStream input =
        MySQLTestConfig.class.getClassLoader().getResourceAsStream("mysql-test.properties")) {
      if (input != null) {
        props.load(input);
        System.out.println("✅ MySQL config loaded from mysql-test.properties");
      }
    } catch (IOException e) {
      System.out.println("⚠️ Failed to load mysql-test.properties: " + e.getMessage());
    }
    return props;
  }

  /** 创建MySQL连接池 */
  public static JDBCPool createMySQLPool(Vertx vertx) {
    Properties props = loadProperties();

    if (!props.containsKey("mysql.url")) {
      System.out.println("⚠️ MySQL not configured (no env vars or properties file)");
      return null;
    }

    PoolOptions poolOptions =
        new PoolOptions()
            .setMaxSize(Integer.parseInt(props.getProperty("mysql.max_pool_size", "10")));
    JDBCConnectOptions connectOptions =
        new JDBCConnectOptions()
            .setJdbcUrl(props.getProperty("mysql.url"))
            .setUser(props.getProperty("mysql.user"))
            .setPassword(props.getProperty("mysql.password"));

    return JDBCPool.pool(vertx, connectOptions, poolOptions);
  }

  /** 检查MySQL连接是否可用 */
  public static boolean isMySQLAvailable() {
    try {
      Class.forName("com.mysql.cj.jdbc.Driver");
      // 检查配置是否存在（环境变量或配置文件）
      Properties props = loadProperties();
      return props.containsKey("mysql.url");
    } catch (ClassNotFoundException e) {
      return false;
    }
  }
}
