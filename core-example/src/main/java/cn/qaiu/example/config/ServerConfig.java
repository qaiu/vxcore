package cn.qaiu.example.config;

/**
 * 服务器配置类
 * 
 * @author QAIU
 */
public class ServerConfig {
    
    private String host;
    private Integer port;
    private Integer timeout;
    private String gatewayPrefix;
    
    public ServerConfig() {
        this.host = "0.0.0.0";
        this.port = 6400;
        this.timeout = 30000;
        this.gatewayPrefix = "/";
    }
    
    public String getHost() {
        return host;
    }
    
    public void setHost(String host) {
        this.host = host;
    }
    
    public Integer getPort() {
        return port;
    }
    
    public void setPort(Integer port) {
        this.port = port;
    }
    
    public Integer getTimeout() {
        return timeout;
    }
    
    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }
    
    public String getGatewayPrefix() {
        return gatewayPrefix;
    }
    
    public void setGatewayPrefix(String gatewayPrefix) {
        this.gatewayPrefix = gatewayPrefix;
    }
    
    @Override
    public String toString() {
        return "ServerConfig{" +
                "host='" + host + '\'' +
                ", port=" + port +
                ", timeout=" + timeout +
                ", gatewayPrefix='" + gatewayPrefix + '\'' +
                '}';
    }
}