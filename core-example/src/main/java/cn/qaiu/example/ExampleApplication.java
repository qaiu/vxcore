package cn.qaiu.example;

import cn.qaiu.db.datasource.DataSourceConfig;
import cn.qaiu.db.datasource.DataSourceManager;
import cn.qaiu.example.util.AutoTableManager;
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
        
        // 初始化数据源（同步等待完成）
        initializeDataSource()
            .onSuccess(v -> {
                LOGGER.info("✅ DataSource initialized successfully");
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
            })
            .onFailure(err -> {
                LOGGER.error("❌ Failed to initialize datasource", err);
                startPromise.fail(err);
            });
    }
    
    /**
     * 初始化数据源
     */
    private io.vertx.core.Future<Void> initializeDataSource() {
        // 获取DataSourceManager实例
        DataSourceManager manager = DataSourceManager.getInstance(vertx);
        
        // 创建H2内存数据库配置
        DataSourceConfig h2Config = new DataSourceConfig(
            "default",  // name
            "h2",       // type
            "jdbc:h2:mem:vxcore_example;DB_CLOSE_DELAY=-1;MODE=MySQL;DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE",  // url
            "sa",       // username
            ""          // password
        );
        h2Config.setMaxPoolSize(10);
        
        // 注册数据源配置
        return manager.registerDataSource("default", h2Config)
            .compose(v -> {
                LOGGER.info("Default H2 datasource config registered");
                // 初始化数据源（创建连接池和JooqExecutor）
                return manager.initializeDataSource("default");
            })
            .compose(v -> {
                LOGGER.info("Default H2 datasource initialized successfully");
                // 获取默认JooqExecutor
                cn.qaiu.db.dsl.core.JooqExecutor executor = manager.getDefaultExecutor();
                if (executor == null) {
                    return io.vertx.core.Future.failedFuture("Failed to get default JooqExecutor");
                }
                LOGGER.info("Got default JooqExecutor: {}", executor);
                // 初始化数据库表
                return new AutoTableManager(executor).createAllTables();
            });
    }
    
    @Override
    public void stop(Promise<Void> stopPromise) {
        LOGGER.info("Stopping VXCore Example Application...");
        LOGGER.info("Application stopped successfully");
        stopPromise.complete();
    }
}