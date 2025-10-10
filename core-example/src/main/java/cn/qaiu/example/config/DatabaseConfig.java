package cn.qaiu.example.config;

/**
 * 数据库配置类
 * 
 * @author QAIU
 */
public class DatabaseConfig {
    
    private String url;
    private String username;
    private String password;
    private String driver;
    private Integer maxPoolSize;
    private Integer minPoolSize;
    
    public DatabaseConfig() {
        this.url = "jdbc:h2:mem:testdb";
        this.username = "root";
        this.password = "123456";
        this.driver = "org.h2.Driver";
        this.maxPoolSize = 10;
        this.minPoolSize = 1;
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
    
    @Override
    public String toString() {
        return "DatabaseConfig{" +
                "url='" + url + '\'' +
                ", username='" + username + '\'' +
                ", password='[PROTECTED]'" +
                ", driver='" + driver + '\'' +
                ", maxPoolSize=" + maxPoolSize +
                ", minPoolSize=" + minPoolSize +
                '}';
    }
}

