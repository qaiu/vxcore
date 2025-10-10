package cn.qaiu.db.datasource;

import io.vertx.core.json.JsonObject;

/**
 * 数据源配置类
 * 
 * @author <a href="https://qaiu.top">QAIU</a>
 */
public class DataSourceConfig {
    
    private String name;
    private String type;
    private String url;
    private String username;
    private String password;
    private String driver;
    private int maxPoolSize = 10;
    private int minPoolSize = 1;
    private int initialPoolSize = 1;
    private long maxIdleTime = 30 * 60 * 1000; // 30分钟
    private long connectionTimeout = 30 * 1000; // 30秒
    private boolean autoCommit = true;
    private JsonObject additionalConfig = new JsonObject();
    
    public DataSourceConfig() {}
    
    public DataSourceConfig(String name, String type, String url, String username, String password) {
        this.name = name;
        this.type = type;
        this.url = url;
        this.username = username;
        this.password = password;
    }
    
    // Getters and Setters
    public String getUsername() {
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
    
    public String getDriver() {
        return driver;
    }
    
    public void setDriver(String driver) {
        this.driver = driver;
    }
    
    public int getMaxPoolSize() {
        return maxPoolSize;
    }
    
    public void setMaxPoolSize(int maxPoolSize) {
        this.maxPoolSize = maxPoolSize;
    }
    
    public int getMinPoolSize() {
        return minPoolSize;
    }
    
    public void setMinPoolSize(int minPoolSize) {
        this.minPoolSize = minPoolSize;
    }
    
    public int getInitialPoolSize() {
        return initialPoolSize;
    }
    
    public void setInitialPoolSize(int initialPoolSize) {
        this.initialPoolSize = initialPoolSize;
    }
    
    public long getMaxIdleTime() {
        return maxIdleTime;
    }
    
    public void setMaxIdleTime(long maxIdleTime) {
        this.maxIdleTime = maxIdleTime;
    }
    
    public long getConnectionTimeout() {
        return connectionTimeout;
    }
    
    public void setConnectionTimeout(long connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }
    
    public boolean isAutoCommit() {
        return autoCommit;
    }
    
    public void setAutoCommit(boolean autoCommit) {
        this.autoCommit = autoCommit;
    }
    
    public JsonObject getAdditionalConfig() {
        return additionalConfig;
    }
    
    public void setAdditionalConfig(JsonObject additionalConfig) {
        this.additionalConfig = additionalConfig;
    }
    
    /**
     * 转换为JsonObject配置
     */
    public JsonObject toJsonObject() {
        JsonObject config = new JsonObject()
            .put("jdbcUrl", url)
            .put("user", username)
            .put("password", password)
            .put("max_pool_size", maxPoolSize)
            .put("min_pool_size", minPoolSize)
            .put("initial_pool_size", initialPoolSize)
            .put("max_idle_time", maxIdleTime)
            .put("connection_timeout", connectionTimeout)
            .put("auto_commit", autoCommit);
        
        if (driver != null) {
            config.put("driver_class", driver);
        }
        
        // 合并额外配置
        if (additionalConfig != null) {
            config.mergeIn(additionalConfig);
        }
        
        return config;
    }
    
    /**
     * 从JsonObject创建配置
     */
    public static DataSourceConfig fromJsonObject(String name, JsonObject config) {
        DataSourceConfig dsConfig = new DataSourceConfig();
        dsConfig.setName(name);
        dsConfig.setType(config.getString("type", "jdbc"));
        dsConfig.setUrl(config.getString("jdbcUrl"));
        dsConfig.setUsername(config.getString("user"));
        dsConfig.setPassword(config.getString("password"));
        dsConfig.setDriver(config.getString("driver_class"));
        dsConfig.setMaxPoolSize(config.getInteger("max_pool_size", 10));
        dsConfig.setMinPoolSize(config.getInteger("min_pool_size", 1));
        dsConfig.setInitialPoolSize(config.getInteger("initial_pool_size", 1));
        dsConfig.setMaxIdleTime(config.getLong("max_idle_time", 30 * 60 * 1000L));
        dsConfig.setConnectionTimeout(config.getLong("connection_timeout", 30 * 1000L));
        dsConfig.setAutoCommit(config.getBoolean("auto_commit", true));
        
        // 复制其他配置
        JsonObject additional = config.copy();
        additional.remove("type");
        additional.remove("jdbcUrl");
        additional.remove("user");
        additional.remove("password");
        additional.remove("driver_class");
        additional.remove("max_pool_size");
        additional.remove("min_pool_size");
        additional.remove("initial_pool_size");
        additional.remove("max_idle_time");
        additional.remove("connection_timeout");
        additional.remove("auto_commit");
        dsConfig.setAdditionalConfig(additional);
        
        return dsConfig;
    }
}
