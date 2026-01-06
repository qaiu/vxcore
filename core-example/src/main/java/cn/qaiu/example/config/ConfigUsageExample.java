package cn.qaiu.example.config;

import cn.qaiu.vx.core.config.ConfigAliasRegistry;
import cn.qaiu.vx.core.config.ConfigBinder;
import cn.qaiu.vx.core.config.ConfigResolver;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 配置管理使用示例
 * 演示如何使用 ConfigAliasRegistry、ConfigResolver 和 ConfigBinder
 *
 * @author <a href="https://qaiu.top">QAIU</a>
 */
public class ConfigUsageExample {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigUsageExample.class);

    /**
     * 演示配置别名机制
     */
    public static void demonstrateAliasRegistry() {
        ConfigAliasRegistry registry = ConfigAliasRegistry.getInstance();

        // 获取规范名称
        String canonical1 = registry.getCanonicalName("url");
        String canonical2 = registry.getCanonicalName("jdbcUrl");
        LOGGER.info("'url' canonical name: {}", canonical1);
        LOGGER.info("'jdbcUrl' canonical name: {}", canonical2);

        // 检查是否为同一组别名
        boolean isAlias = registry.isAlias("url", "jdbcUrl");
        LOGGER.info("'url' and 'jdbcUrl' are aliases: {}", isAlias);

        // 注册自定义别名组
        registry.registerAliasGroup("customTimeout", "timeout", "connectTimeout", "connTimeout");
        LOGGER.info("Custom alias group registered for 'customTimeout'");
    }

    /**
     * 演示配置解析器
     */
    public static void demonstrateConfigResolver() {
        // 模拟配置数据
        JsonObject config = new JsonObject()
                .put("url", "jdbc:mysql://localhost:3306/test")
                .put("user", "root")
                .put("pass", "secret");

        ConfigResolver resolver = new ConfigResolver(config);

        // 使用别名获取配置值
        String jdbcUrl = resolver.getString("jdbcUrl");  // 会自动查找 url
        String username = resolver.getString("username"); // 会自动查找 user
        String password = resolver.getString("password"); // 会自动查找 pass

        LOGGER.info("JDBC URL (via alias 'jdbcUrl'): {}", jdbcUrl);
        LOGGER.info("Username (via alias 'username'): {}", username);
        LOGGER.info("Password (via alias 'password'): {}", password);

        // 使用默认值
        Integer maxPoolSize = resolver.getInteger("maximumPoolSize", 10);
        LOGGER.info("Max pool size (with default): {}", maxPoolSize);
    }

    /**
     * 演示配置绑定器
     */
    public static void demonstrateConfigBinder() {
        // 模拟配置数据
        JsonObject config = new JsonObject()
                .put("host", "localhost")
                .put("port", 8080)
                .put("enableSsl", true)
                .put("maxConnections", 100);

        ConfigBinder binder = new ConfigBinder();

        // 绑定到POJO类
        ServerConfig serverConfig = binder.bind(config, ServerConfig.class);
        LOGGER.info("Bound server config: host={}, port={}, ssl={}, maxConn={}",
                serverConfig.host, serverConfig.port, serverConfig.enableSsl, serverConfig.maxConnections);

        // 注册自定义绑定策略
        binder.registerStrategy(CustomType.class, (key, value) -> new CustomType(value.toString()));
    }

    /**
     * 示例服务器配置类
     */
    public static class ServerConfig {
        public String host;
        public int port;
        public boolean enableSsl;
        public int maxConnections;
    }

    /**
     * 示例自定义类型
     */
    public static class CustomType {
        private final String value;

        public CustomType(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    public static void main(String[] args) {
        LOGGER.info("=== Config Alias Registry Demo ===");
        demonstrateAliasRegistry();

        LOGGER.info("\n=== Config Resolver Demo ===");
        demonstrateConfigResolver();

        LOGGER.info("\n=== Config Binder Demo ===");
        demonstrateConfigBinder();
    }
}
