package cn.qaiu.vx.core.verticle;

import cn.qaiu.vx.core.lifecycle.FrameworkLifecycleManager;
import cn.qaiu.vx.core.lifecycle.ServiceRegistryComponent;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 服务注册Verticle
 * 使用组合模式，依赖ServiceRegistryComponent进行服务管理
 * 
 * @author <a href="https://qaiu.top">QAIU</a>
 */
public class ServiceVerticle extends AbstractVerticle {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceVerticle.class);
    
    private ServiceRegistryComponent serviceRegistryComponent;

    @Override
    public void start(Promise<Void> startPromise) {
        try {
            // 获取框架生命周期管理器
            FrameworkLifecycleManager lifecycleManager = FrameworkLifecycleManager.getInstance();
            
            // 获取服务注册组件
            serviceRegistryComponent = (ServiceRegistryComponent) lifecycleManager
                .getComponents().stream()
                .filter(component -> component instanceof ServiceRegistryComponent)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("ServiceRegistryComponent not found"));
            
            // 启动服务注册
            serviceRegistryComponent.start()
                .onSuccess(v -> {
                    LOGGER.info("ServiceVerticle started successfully");
                    startPromise.complete();
                })
                .onFailure(error -> {
                    LOGGER.error("Failed to start ServiceVerticle", error);
                    startPromise.fail(error);
                });
                
        } catch (Exception e) {
            LOGGER.error("Failed to start ServiceVerticle", e);
            startPromise.fail(e);
        }
    }
    
    @Override
    public void stop(Promise<Void> stopPromise) {
        try {
            if (serviceRegistryComponent != null) {
                serviceRegistryComponent.stop()
                    .onSuccess(v -> {
                        LOGGER.info("ServiceVerticle stopped successfully");
                        stopPromise.complete();
                    })
                    .onFailure(error -> {
                        LOGGER.error("Failed to stop ServiceVerticle", error);
                        stopPromise.fail(error);
                    });
            } else {
                stopPromise.complete();
            }
        } catch (Exception e) {
            LOGGER.error("Failed to stop ServiceVerticle", e);
            stopPromise.fail(e);
        }
    }
}