package cn.qaiu.vx.core.config;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 配置别名注册表
 * 支持配置键的别名映射，如 jdbcUrl ↔ url，实现配置格式兼容
 *
 * @author <a href="https://qaiu.top">QAIU</a>
 */
public class ConfigAliasRegistry {

    private static final ConfigAliasRegistry INSTANCE = new ConfigAliasRegistry();

    /**
     * 别名映射表：key -> 所有别名集合
     * 例如: "jdbcUrl" -> ["jdbcUrl", "url", "jdbc-url"]
     */
    private final Map<String, Set<String>> aliasGroups = new ConcurrentHashMap<>();

    /**
     * 反向映射：任意别名 -> 规范名称
     */
    private final Map<String, String> aliasToCanonical = new ConcurrentHashMap<>();

    private ConfigAliasRegistry() {
        // 注册默认别名
        registerDefaultAliases();
    }

    public static ConfigAliasRegistry getInstance() {
        return INSTANCE;
    }

    /**
     * 注册默认别名映射
     */
    private void registerDefaultAliases() {
        // 数据源配置别名
        registerAliasGroup("datasources", "datasources", "database", "dataSource", "data-source");
        
        // JDBC URL别名
        registerAliasGroup("url", "url", "jdbcUrl", "jdbc-url", "jdbc_url");
        
        // 用户名别名
        registerAliasGroup("username", "username", "user", "userName", "user-name");
        
        // 密码别名
        registerAliasGroup("password", "password", "pwd", "pass");
        
        // 驱动类别名
        registerAliasGroup("driverClassName", "driverClassName", "driver", "driver-class-name", "driverClass");
        
        // 连接池大小别名
        registerAliasGroup("maxPoolSize", "maxPoolSize", "max-pool-size", "max_pool_size", "maximumPoolSize");
        registerAliasGroup("minPoolSize", "minPoolSize", "min-pool-size", "min_pool_size", "minimumPoolSize");
        
        // 服务器配置别名
        registerAliasGroup("port", "port", "serverPort", "server-port");
        registerAliasGroup("host", "host", "serverHost", "server-host", "bindAddress", "bind-address");
        
        // 超时配置别名
        registerAliasGroup("timeout", "timeout", "timeOut", "time-out", "connectionTimeout", "connection-timeout");
        
        // 数据库类型别名
        registerAliasGroup("type", "type", "dbType", "db-type", "databaseType", "database-type");
    }

    /**
     * 注册别名组
     *
     * @param canonicalName 规范名称（首选名称）
     * @param aliases 所有别名（包括规范名称）
     */
    public void registerAliasGroup(String canonicalName, String... aliases) {
        Set<String> aliasSet = new LinkedHashSet<>(Arrays.asList(aliases));
        aliasGroups.put(canonicalName, aliasSet);
        
        for (String alias : aliases) {
            aliasToCanonical.put(alias, canonicalName);
            aliasToCanonical.put(alias.toLowerCase(java.util.Locale.ROOT), canonicalName);
        }
    }

    /**
     * 获取规范名称
     *
     * @param alias 别名
     * @return 规范名称，如果不存在则返回原始名称
     */
    public String getCanonicalName(String alias) {
        if (alias == null) {
            return null;
        }
        String canonical = aliasToCanonical.get(alias);
        if (canonical == null) {
            canonical = aliasToCanonical.get(alias.toLowerCase(java.util.Locale.ROOT));
        }
        return canonical != null ? canonical : alias;
    }

    /**
     * 获取所有别名
     *
     * @param canonicalName 规范名称
     * @return 别名集合
     */
    public Set<String> getAliases(String canonicalName) {
        Set<String> aliases = aliasGroups.get(canonicalName);
        return aliases != null ? Collections.unmodifiableSet(aliases) : Collections.singleton(canonicalName);
    }

    /**
     * 检查两个名称是否为同一配置项的别名
     *
     * @param name1 名称1
     * @param name2 名称2
     * @return 是否为别名关系
     */
    public boolean isAlias(String name1, String name2) {
        if (name1 == null || name2 == null) {
            return false;
        }
        String canonical1 = getCanonicalName(name1);
        String canonical2 = getCanonicalName(name2);
        return canonical1.equals(canonical2);
    }

    /**
     * 获取所有已注册的规范名称
     *
     * @return 规范名称集合
     */
    public Set<String> getAllCanonicalNames() {
        return Collections.unmodifiableSet(aliasGroups.keySet());
    }

    /**
     * 获取所有别名组
     *
     * @return 别名组映射（规范名称 -> 别名集合）
     */
    public Map<String, Set<String>> getAllAliasGroups() {
        return Collections.unmodifiableMap(aliasGroups);
    }
}
