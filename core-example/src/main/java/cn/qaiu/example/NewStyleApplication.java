package cn.qaiu.example;

import cn.qaiu.vx.core.VXCoreApplication;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 新风格应用示例
 * 展示如何使用组合模式的新框架启动方式
 * 
 * @author <a href="https://qaiu.top">QAIU</a>
 */
public class NewStyleApplication {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(NewStyleApplication.class);
    
    public static void main(String[] args) {
        // 方式1：使用静态方法快速启动
        VXCoreApplication.run(args, config -> {
            LOGGER.info("Application started with new style!");
            LOGGER.info("Configuration: {}", config.encodePrettily());
            
            // 在这里可以执行应用特定的初始化逻辑
            initializeApplication(config);
        });
    }
    
    /**
     * 初始化应用
     */
    private static void initializeApplication(JsonObject config) {
        try {
            // 获取数据源配置
            JsonObject datasources = config.getJsonObject("datasources");
            if (datasources != null) {
                LOGGER.info("Found {} datasources", datasources.size());
                datasources.fieldNames().forEach(name -> {
                    JsonObject dsConfig = datasources.getJsonObject(name);
                    LOGGER.info("Datasource {}: {}", name, dsConfig.getString("url"));
                });
            }
            
            // 获取服务器配置
            JsonObject server = config.getJsonObject("server");
            if (server != null) {
                LOGGER.info("Server will start on port: {}", server.getInteger("port"));
            }
            
            LOGGER.info("Application initialization completed successfully");
            
        } catch (Exception e) {
            LOGGER.error("Failed to initialize application", e);
        }
    }
    
    /**
     * 方式2：使用实例方法进行更精细的控制
     */
    public static void runWithInstanceControl(String[] args) {
        VXCoreApplication app = new VXCoreApplication();
        
        app.start(args, config -> {
            LOGGER.info("Application started with instance control!");
            
            // 检查应用状态
            if (app.isStarted()) {
                LOGGER.info("Application is running");
                
                // 获取Vertx实例进行自定义操作
                app.getVertx().setTimer(5000, id -> {
                    LOGGER.info("Timer triggered after 5 seconds");
                });
            }
        });
        
        // 添加关闭钩子
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOGGER.info("Shutting down application...");
            app.stop()
                .onSuccess(v -> LOGGER.info("Application stopped successfully"))
                .onFailure(error -> LOGGER.error("Failed to stop application", error));
        }));
    }
}