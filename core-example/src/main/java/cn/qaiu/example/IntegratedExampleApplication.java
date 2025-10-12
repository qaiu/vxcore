package cn.qaiu.example;

import cn.qaiu.db.datasource.DataSourceConfig;
import cn.qaiu.db.datasource.DataSourceManager;
import cn.qaiu.db.datasource.DataSourceManagerFactory;
import cn.qaiu.vx.core.VXCoreApplication;
import cn.qaiu.vx.core.lifecycle.DataSourceComponent;
import cn.qaiu.vx.core.lifecycle.FrameworkLifecycleManager;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 集成示例应用
 * 演示如何将core-database模块的实现注入到core模块中
 * 避免循环依赖问题
 * 
 * @author QAIU
 */
public class IntegratedExampleApplication extends AbstractVerticle {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(IntegratedExampleApplication.class);
    
    @Override
    public void start(Promise<Void> startPromise) {
        LOGGER.info("Starting Integrated VXCore Example Application...");
        
        // 使用VXCoreApplication启动框架
        VXCoreApplication.run(vertx, config -> {
            LOGGER.info("VXCore framework started, injecting database implementation...");
            
            // 注入core-database模块的实现到core模块
            injectDatabaseImplementation()
                .onSuccess(v -> {
                    LOGGER.info("✅ Database implementation injected successfully");
                    LOGGER.info("Integrated VXCore Example Application started successfully!");
                    startPromise.complete();
                })
                .onFailure(err -> {
                    LOGGER.error("❌ Failed to inject database implementation", err);
                    startPromise.fail(err);
                });
        });
    }
    
    /**
     * 注入数据库实现
     * 将core-database模块的DataSourceManager实现注入到core模块的DataSourceComponent中
     */
    private io.vertx.core.Future<Void> injectDatabaseImplementation() {
        return io.vertx.core.Future.future(promise -> {
            try {
                // 获取框架生命周期管理器
                FrameworkLifecycleManager lifecycleManager = FrameworkLifecycleManager.getInstance();
                
                // 获取DataSourceComponent
                DataSourceComponent dataSourceComponent = lifecycleManager.getComponents().stream()
                    .filter(component -> component instanceof DataSourceComponent)
                    .map(component -> (DataSourceComponent) component)
                    .findFirst()
                    .orElse(null);
                
                if (dataSourceComponent == null) {
                    promise.fail("DataSourceComponent not found");
                    return;
                }
                
                // 创建core-database模块的DataSourceManager实现
                DataSourceManager databaseManager = DataSourceManagerFactory.getInstance(vertx);
                
                // 注入实现
                dataSourceComponent.setDataSourceManager(databaseManager);
                
                // 初始化数据源
                initializeDataSources(databaseManager)
                    .onSuccess(v -> {
                        LOGGER.info("Data sources initialized successfully");
                        promise.complete();
                    })
                    .onFailure(promise::fail);
                    
            } catch (Exception e) {
                LOGGER.error("Failed to inject database implementation", e);
                promise.fail(e);
            }
        });
    }
    
    /**
     * 初始化数据源
     */
    private io.vertx.core.Future<Void> initializeDataSources(DataSourceManager manager) {
        // 创建H2内存数据库配置
        DataSourceConfig h2Config = new DataSourceConfig(
            "default",  // name
            "h2",       // type
            "jdbc:h2:mem:vxcore_example;DB_CLOSE_DELAY=-1;MODE=MySQL;DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE",  // url
            "sa",       // username
            ""          // password
        );
        h2Config.setMaxPoolSize(10);
        
        // 注册并初始化数据源
        return manager.registerDataSource("default", h2Config)
            .compose(v -> {
                LOGGER.info("Default H2 datasource config registered");
                return manager.initializeDataSource("default");
            })
            .compose(v -> {
                LOGGER.info("Default H2 datasource initialized successfully");
                return io.vertx.core.Future.succeededFuture();
            });
    }
    
    @Override
    public void stop(Promise<Void> stopPromise) {
        LOGGER.info("Stopping Integrated VXCore Example Application...");
        
        // 停止框架
        FrameworkLifecycleManager.getInstance().stop()
            .onSuccess(v -> {
                LOGGER.info("VXCore framework stopped successfully");
                stopPromise.complete();
            })
            .onFailure(err -> {
                LOGGER.error("Failed to stop VXCore framework", err);
                stopPromise.fail(err);
            });
    }
}