package cn.qaiu.vx.core.lifecycle;

import cn.qaiu.vx.core.handlerfactory.RouterHandlerFactory;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 路由管理组件
 * 负责路由的创建、配置和管理
 * 
 * @author <a href="https://qaiu.top">QAIU</a>
 */
public class RouterComponent implements LifecycleComponent {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(RouterComponent.class);
    
    private Vertx vertx;
    private RouterHandlerFactory routerHandlerFactory;
    private Router router;
    
    @Override
    public Future<Void> initialize(Vertx vertx, JsonObject config) {
        this.vertx = vertx;
        
        return Future.future(promise -> {
            try {
                // 1. 获取网关前缀
                String gatewayPrefix = getGatewayPrefix(config);
                
                // 2. 创建路由处理器工厂
                routerHandlerFactory = new RouterHandlerFactory(gatewayPrefix);
                
                // 3. 创建路由器
                router = routerHandlerFactory.createRouter();
                
                LOGGER.info("Router component initialized successfully with prefix: {}", gatewayPrefix);
                promise.complete();
            } catch (Exception e) {
                LOGGER.error("Failed to initialize router component", e);
                promise.fail(e);
            }
        });
    }
    
    /**
     * 获取网关前缀
     */
    private String getGatewayPrefix(JsonObject config) {
        JsonObject customConfig = config.getJsonObject("custom");
        if (customConfig != null && customConfig.containsKey("gatewayPrefix")) {
            return customConfig.getString("gatewayPrefix");
        }
        return "api"; // 默认前缀
    }
    
    @Override
    public int getPriority() {
        return 40; // 第四优先级
    }
    
    /**
     * 获取路由器
     */
    public Router getRouter() {
        return router;
    }
    
    /**
     * 获取路由处理器工厂
     */
    public RouterHandlerFactory getRouterHandlerFactory() {
        return routerHandlerFactory;
    }
}