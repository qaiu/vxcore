package cn.qaiu.example;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * VXCore 示例应用主类
 * 演示完整的三层架构：Controller -> Service -> DAO -> Database
 * 
 * @author QAIU
 */
public class ExampleApplication extends AbstractVerticle {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ExampleApplication.class);
    
    @Override
    public void start(Promise<Void> startPromise) {
        LOGGER.info("Starting VXCore Example Application...");
        
        LOGGER.info("VXCore Example Application started successfully!");
        LOGGER.info("Application is ready to serve requests via VXCore framework");
        LOGGER.info("Available endpoints:");
        LOGGER.info("  - GET /api/users/ - 获取用户列表");
        LOGGER.info("  - GET /api/users/{id} - 获取用户详情");
        LOGGER.info("  - POST /api/users/ - 创建用户");
        LOGGER.info("  - PUT /api/users/{id} - 更新用户");
        LOGGER.info("  - DELETE /api/users/{id} - 删除用户");
        LOGGER.info("  - GET /api/products/ - 获取产品列表");
        LOGGER.info("  - GET /api/products/{id} - 获取产品详情");
        LOGGER.info("  - POST /api/products/ - 创建产品");
        LOGGER.info("  - PUT /api/products/{id} - 更新产品");
        LOGGER.info("  - DELETE /api/products/{id} - 删除产品");
        startPromise.complete();
    }
    
    @Override
    public void stop(Promise<Void> stopPromise) {
        LOGGER.info("Stopping VXCore Example Application...");
        LOGGER.info("Application stopped successfully");
        stopPromise.complete();
    }
}