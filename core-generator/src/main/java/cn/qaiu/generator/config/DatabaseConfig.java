package cn.qaiu.generator.config;

/**
 * 数据库配置
 * 
 * @author QAIU
 */
public class DatabaseConfig {
    
    private String url;
    private String username;
    private String password;
    private String driverClassName;
    private String schema;
    private String catalog;
    
    public DatabaseConfig() {
    }
    
    public DatabaseConfig(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }
    
    public String getUrl() {
        return url;
    }
    
    public DatabaseConfig setUrl(String url) {
        this.url = url;
        return this;
    }
    
    public String getUsername() {
        return username;
    }
    
    public DatabaseConfig setUsername(String username) {
        this.username = username;
        return this;
    }
    
    public String getPassword() {
        return password;
    }
    
    public DatabaseConfig setPassword(String password) {
        this.password = password;
        return this;
    }
    
    public String getDriverClassName() {
        return driverClassName;
    }
    
    public DatabaseConfig setDriverClassName(String driverClassName) {
        this.driverClassName = driverClassName;
        return this;
    }
    
    public String getSchema() {
        return schema;
    }
    
    public DatabaseConfig setSchema(String schema) {
        this.schema = schema;
        return this;
    }
    
    public String getCatalog() {
        return catalog;
    }
    
    public DatabaseConfig setCatalog(String catalog) {
        this.catalog = catalog;
        return this;
    }
    
    /**
     * 验证配置是否有效
     * 
     * @return 是否有效
     */
    public boolean isValid() {
        return url != null && !url.trim().isEmpty() &&
               username != null && !username.trim().isEmpty();
    }
}