package cn.qaiu.vx.core.verticle;

import cn.qaiu.vx.core.lifecycle.FrameworkLifecycleManager;
import cn.qaiu.vx.core.lifecycle.RouterComponent;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 路由Verticle
 * 使用组合模式，依赖RouterComponent进行路由管理
 * 
 * @author <a href="https://qaiu.top">QAIU</a>
 */
public class RouterVerticle extends AbstractVerticle {

    private static final Logger LOGGER = LoggerFactory.getLogger(RouterVerticle.class);
    
    private RouterComponent routerComponent;
    private HttpServer httpServer;

    @Override
    public void start(Promise<Void> startPromise) {
        try {
            // 获取框架生命周期管理器
            FrameworkLifecycleManager lifecycleManager = FrameworkLifecycleManager.getInstance();
            
            // 获取路由组件
            routerComponent = (RouterComponent) lifecycleManager
                .getComponents().stream()
                .filter(component -> component instanceof RouterComponent)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("RouterComponent not found"));
            
            // 获取路由器
            Router router = routerComponent.getRouter();
            
            // 获取服务器配置
            JsonObject serverConfig = lifecycleManager.getGlobalConfig().getJsonObject("server");
            int port = serverConfig.getInteger("port", 8080);
            String host = serverConfig.getString("host", "0.0.0.0");
            
            // 创建HTTP服务器
            httpServer = vertx.createHttpServer();
            
            // 启动服务器
            httpServer.requestHandler(router)
                .listen(port, host)
                .onSuccess(server -> {
                    LOGGER.info("HTTP server started on {}:{}", host, port);
                    startPromise.complete();
                })
                .onFailure(error -> {
                    LOGGER.error("Failed to start HTTP server", error);
                    startPromise.fail(error);
                });
                
        } catch (Exception e) {
            LOGGER.error("Failed to start RouterVerticle", e);
            startPromise.fail(e);
        }
    }
    
    @Override
    public void stop(Promise<Void> stopPromise) {
        try {
            if (httpServer != null) {
                httpServer.close()
                    .onSuccess(v -> {
                        LOGGER.info("HTTP server stopped successfully");
                        stopPromise.complete();
                    })
                    .onFailure(error -> {
                        LOGGER.error("Failed to stop HTTP server", error);
                        stopPromise.fail(error);
                    });
            } else {
                stopPromise.complete();
            }
        } catch (Exception e) {
            LOGGER.error("Failed to stop RouterVerticle", e);
            stopPromise.fail(e);
        }
    }
}