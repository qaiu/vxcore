package cn.qaiu.vx.core.config;

import cn.qaiu.vx.core.annotaions.config.ConfigurationProperties;
import cn.qaiu.vx.core.annotaions.config.ConfigurationProperty;

/**
 * 数据库配置类
 * 演示配置元数据功能
 * 
 * @author QAIU
 */
@ConfigurationProperties(prefix = "database", description = "数据库连接配置")
public class DatabaseConfig {
    
    @ConfigurationProperty(
        description = "数据库类型",
        defaultValue = "h2",
        allowedValues = {"h2", "mysql", "postgresql", "oracle"},
        group = "connection"
    )
    private String type;
    
    @ConfigurationProperty(
        description = "数据库主机地址",
        defaultValue = "localhost",
        required = true,
        group = "connection"
    )
    private String host;
    
    @ConfigurationProperty(
        description = "数据库端口",
        defaultValue = "3306",
        type = ConfigurationProperty.PropertyType.INTEGER,
        minValue = 1,
        maxValue = 65535,
        group = "connection"
    )
    private int port;
    
    @ConfigurationProperty(
        description = "数据库名称",
        required = true,
        group = "connection"
    )
    private String database;
    
    @ConfigurationProperty(
        description = "用户名",
        required = true,
        group = "auth"
    )
    private String username;
    
    @ConfigurationProperty(
        description = "密码",
        required = true,
        group = "auth"
    )
    private String password;
    
    @ConfigurationProperty(
        description = "连接池最大连接数",
        defaultValue = "10",
        type = ConfigurationProperty.PropertyType.INTEGER,
        minValue = 1,
        maxValue = 100,
        group = "pool"
    )
    private int maxPoolSize;
    
    @ConfigurationProperty(
        description = "连接超时时间（秒）",
        defaultValue = "30",
        type = ConfigurationProperty.PropertyType.INTEGER,
        minValue = 1,
        maxValue = 300,
        group = "pool"
    )
    private int connectionTimeout;
    
    @ConfigurationProperty(
        description = "是否启用SSL",
        defaultValue = "false",
        type = ConfigurationProperty.PropertyType.BOOLEAN,
        group = "security"
    )
    private boolean sslEnabled;
    
    // Getters and setters
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    
    public String getHost() { return host; }
    public void setHost(String host) { this.host = host; }
    
    public int getPort() { return port; }
    public void setPort(int port) { this.port = port; }
    
    public String getDatabase() { return database; }
    public void setDatabase(String database) { this.database = database; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public int getMaxPoolSize() { return maxPoolSize; }
    public void setMaxPoolSize(int maxPoolSize) { this.maxPoolSize = maxPoolSize; }
    
    public int getConnectionTimeout() { return connectionTimeout; }
    public void setConnectionTimeout(int connectionTimeout) { this.connectionTimeout = connectionTimeout; }
    
    public boolean isSslEnabled() { return sslEnabled; }
    public void setSslEnabled(boolean sslEnabled) { this.sslEnabled = sslEnabled; }
}
