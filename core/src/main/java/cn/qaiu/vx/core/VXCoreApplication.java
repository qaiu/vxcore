package cn.qaiu.vx.core;

import cn.qaiu.vx.core.lifecycle.FrameworkLifecycleManager;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * VXCore应用启动类
 * 使用组合模式管理框架生命周期，替代原有的继承模式
 * 
 * @author <a href="https://qaiu.top">QAIU</a>
 */
public class VXCoreApplication {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(VXCoreApplication.class);
    
    private final FrameworkLifecycleManager lifecycleManager;
    private final long startTime;
    
    public VXCoreApplication() {
        this.lifecycleManager = FrameworkLifecycleManager.getInstance();
        this.startTime = System.currentTimeMillis();
    }
    
    /**
     * 启动应用
     * 
     * @param args 启动参数
     * @param userHandler 用户回调处理器
     * @return 启动结果
     */
    public Future<Void> start(String[] args, Handler<JsonObject> userHandler) {
        return lifecycleManager.start(args, userHandler)
            .onSuccess(v -> {
                long duration = System.currentTimeMillis() - startTime;
                LOGGER.info("VXCore application started successfully in {}ms", duration);
            })
            .onFailure(error -> {
                LOGGER.error("Failed to start VXCore application", error);
            });
    }
    
    /**
     * 停止应用
     * 
     * @return 停止结果
     */
    public Future<Void> stop() {
        return lifecycleManager.stop()
            .onSuccess(v -> {
                LOGGER.info("VXCore application stopped successfully");
            })
            .onFailure(error -> {
                LOGGER.error("Failed to stop VXCore application", error);
            });
    }
    
    /**
     * 获取框架生命周期管理器
     * 
     * @return 生命周期管理器
     */
    public FrameworkLifecycleManager getLifecycleManager() {
        return lifecycleManager;
    }
    
    /**
     * 获取Vertx实例
     * 
     * @return Vertx实例
     */
    public Vertx getVertx() {
        return lifecycleManager.getVertx();
    }
    
    /**
     * 获取全局配置
     * 
     * @return 全局配置
     */
    public JsonObject getGlobalConfig() {
        return lifecycleManager.getGlobalConfig();
    }
    
    /**
     * 检查应用是否已启动
     * 
     * @return 是否已启动
     */
    public boolean isStarted() {
        return lifecycleManager.getState() == FrameworkLifecycleManager.LifecycleState.STARTED;
    }
    
    /**
     * 静态方法：快速启动
     * 
     * @param args 启动参数
     * @param userHandler 用户回调处理器
     */
    public static void run(String[] args, Handler<JsonObject> userHandler) {
        VXCoreApplication app = new VXCoreApplication();
        app.start(args, userHandler)
            .onFailure(error -> {
                LOGGER.error("Application startup failed", error);
                System.exit(1);
            });
    }
    
    /**
     * 静态方法：快速启动（无用户回调）
     * 
     * @param args 启动参数
     */
    public static void run(String[] args) {
        run(args, null);
    }
}