package cn.qaiu.vx.core.proxy;

import io.vertx.core.json.JsonObject;

/**
 * WebSocket代理配置
 * 
 * @author QAIU
 */
public class WebSocketProxyConfig {
    
    private String path;
    private String origin;
    private String targetPath;
    private String targetHost;
    private int targetPort;
    private boolean enabled;
    
    public WebSocketProxyConfig() {
        this.enabled = true;
    }
    
    public WebSocketProxyConfig(String path, String origin) {
        this();
        this.path = path;
        this.origin = origin;
        parseOrigin(origin);
    }
    
    /**
     * 从JSON配置创建WebSocket代理配置
     */
    public static WebSocketProxyConfig fromJson(JsonObject json) {
        WebSocketProxyConfig config = new WebSocketProxyConfig();
        config.setPath(json.getString("path"));
        config.setOrigin(json.getString("origin"));
        config.setEnabled(json.getBoolean("enabled", true));
        return config;
    }
    
    /**
     * 解析origin地址
     */
    private void parseOrigin(String origin) {
        if (origin == null || origin.isEmpty()) {
            return;
        }
        
        // 解析格式: host:port/path
        String[] parts = origin.split("/", 2);
        String hostPort = parts[0];
        this.targetPath = parts.length > 1 ? "/" + parts[1] : "/";
        
        // 解析host:port
        String[] hostPortParts = hostPort.split(":");
        this.targetHost = hostPortParts[0];
        this.targetPort = hostPortParts.length > 1 ? Integer.parseInt(hostPortParts[1]) : 80;
    }
    
    /**
     * 检查路径是否匹配
     */
    public boolean matches(String requestPath) {
        if (path == null || path.isEmpty()) {
            return false;
        }
        
        if (path.startsWith("~")) {
            // 正则匹配
            String regex = path.substring(1);
            return requestPath.matches(regex);
        } else {
            // 前缀匹配
            return requestPath.startsWith(path);
        }
    }
    
    // Getters and Setters
    public String getPath() { return path; }
    public void setPath(String path) { 
        this.path = path; 
    }
    
    public String getOrigin() { return origin; }
    public void setOrigin(String origin) { 
        this.origin = origin;
        parseOrigin(origin);
    }
    
    public String getTargetPath() { return targetPath; }
    public void setTargetPath(String targetPath) { this.targetPath = targetPath; }
    
    public String getTargetHost() { return targetHost; }
    public void setTargetHost(String targetHost) { this.targetHost = targetHost; }
    
    public int getTargetPort() { return targetPort; }
    public void setTargetPort(int targetPort) { this.targetPort = targetPort; }
    
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    
    @Override
    public String toString() {
        return "WebSocketProxyConfig{" +
                "path='" + path + '\'' +
                ", origin='" + origin + '\'' +
                ", targetHost='" + targetHost + '\'' +
                ", targetPort=" + targetPort +
                ", targetPath='" + targetPath + '\'' +
                ", enabled=" + enabled +
                '}';
    }
}
