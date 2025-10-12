package cn.qaiu.vx.core.lifecycle;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 数据源管理组件
 * 负责多数据源的初始化、管理和生命周期
 * 使用接口抽象避免循环依赖
 * 
 * @author <a href="https://qaiu.top">QAIU</a>
 */
public class DataSourceComponent implements LifecycleComponent {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(DataSourceComponent.class);
    
    private Vertx vertx;
    private DataSourceManager dataSourceManager;
    private JsonObject globalConfig;
    
    @Override
    public Future<Void> initialize(Vertx vertx, JsonObject config) {
        this.vertx = vertx;
        this.globalConfig = config;
        
        return Future.future(promise -> {
            try {
                LOGGER.info("Initializing DataSource component...");
                
                // 检查是否有数据源配置
                JsonObject databaseConfig = config.getJsonObject("database");
                if (databaseConfig == null || databaseConfig.isEmpty()) {
                    LOGGER.info("No database configuration found, skipping datasource initialization");
                    promise.complete();
                    return;
                }
                
                // 这里应该通过SPI或者工厂模式来获取DataSourceManager的实现
                // 暂时先记录日志，实际实现会在core-database模块中提供
                LOGGER.info("Database configuration found, datasource manager will be initialized by core-database module");
                
                promise.complete();
            } catch (Exception e) {
                LOGGER.error("Failed to initialize datasource component", e);
                promise.fail(e);
            }
        });
    }
    
    @Override
    public Future<Void> start() {
        return Future.future(promise -> {
            try {
                LOGGER.info("Starting DataSource component...");
                
                // 实际的启动逻辑将在core-database模块中实现
                // 这里只是占位符
                
                LOGGER.info("DataSource component started successfully");
                promise.complete();
            } catch (Exception e) {
                LOGGER.error("Failed to start datasource component", e);
                promise.fail(e);
            }
        });
    }
    
    @Override
    public Future<Void> stop() {
        return Future.future(promise -> {
            try {
                LOGGER.info("Stopping DataSource component...");
                
                if (dataSourceManager != null) {
                    dataSourceManager.closeAllDataSources()
                        .onSuccess(v -> {
                            LOGGER.info("All datasources closed successfully");
                            promise.complete();
                        })
                        .onFailure(error -> {
                            LOGGER.error("Failed to close datasources", error);
                            promise.fail(error);
                        });
                } else {
                    LOGGER.info("No datasource manager to close");
                    promise.complete();
                }
            } catch (Exception e) {
                LOGGER.error("Failed to stop datasource component", e);
                promise.fail(e);
            }
        });
    }
    
    @Override
    public String getName() {
        return "DataSourceComponent";
    }
    
    @Override
    public int getPriority() {
        return 20; // 在ConfigurationComponent之后，ServiceRegistryComponent之前
    }
    
    /**
     * 获取数据源管理器
     * 这个方法将在运行时由core-database模块的实现来设置
     * 
     * @return DataSourceManager实例
     */
    public DataSourceManager getDataSourceManager() {
        return dataSourceManager;
    }
    
    /**
     * 设置数据源管理器
     * 这个方法将由core-database模块在运行时调用
     * 
     * @param dataSourceManager DataSourceManager实例
     */
    public void setDataSourceManager(DataSourceManager dataSourceManager) {
        this.dataSourceManager = dataSourceManager;
    }
    
    /**
     * 检查是否有数据源配置
     * 
     * @return 是否有数据源配置
     */
    public boolean hasDataSourceConfig() {
        if (globalConfig == null) {
            return false;
        }
        JsonObject databaseConfig = globalConfig.getJsonObject("database");
        return databaseConfig != null && !databaseConfig.isEmpty();
    }
}