package cn.qaiu.vx.core.lifecycle;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 代理管理组件
 * 负责反向代理和HTTP代理的配置和管理
 * 
 * @author <a href="https://qaiu.top">QAIU</a>
 */
public class ProxyComponent implements LifecycleComponent {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ProxyComponent.class);
    
    private Vertx vertx;
    private JsonObject proxyConfig;
    private boolean proxyEnabled = false;
    
    @Override
    public Future<Void> initialize(Vertx vertx, JsonObject config) {
        this.vertx = vertx;
        
        return Future.future(promise -> {
            try {
                // 1. 检查代理配置
                proxyConfig = config.getJsonObject("proxy");
                if (proxyConfig != null) {
                    proxyEnabled = proxyConfig.getBoolean("enabled", false);
                    LOGGER.info("Proxy configuration found, enabled: {}", proxyEnabled);
                } else {
                    LOGGER.info("No proxy configuration found");
                }
                
                LOGGER.info("Proxy component initialized successfully");
                promise.complete();
            } catch (Exception e) {
                LOGGER.error("Failed to initialize proxy component", e);
                promise.fail(e);
            }
        });
    }
    
    @Override
    public int getPriority() {
        return 50; // 最低优先级
    }
    
    /**
     * 检查代理是否启用
     */
    public boolean isProxyEnabled() {
        return proxyEnabled;
    }
    
    /**
     * 获取代理配置
     */
    public JsonObject getProxyConfig() {
        return proxyConfig;
    }
}