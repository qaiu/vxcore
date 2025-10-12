package cn.qaiu.vx.core.lifecycle;

import cn.qaiu.db.datasource.DataSourceManager;
import cn.qaiu.db.datasource.DataSourceConfig;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 数据源管理组件
 * 负责多数据源的初始化、管理和生命周期
 * 
 * @author <a href="https://qaiu.top">QAIU</a>
 */
public class DataSourceComponent implements LifecycleComponent {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(DataSourceComponent.class);
    
    private Vertx vertx;
    private DataSourceManager dataSourceManager;
    
    @Override
    public Future<Void> initialize(Vertx vertx, JsonObject config) {
        this.vertx = vertx;
        
        return Future.future(promise -> {
            try {
                // 1. 创建数据源管理器
                dataSourceManager = DataSourceManager.getInstance(vertx);
                
                // 2. 注册数据源配置
                registerDataSources(config);
                
                // 3. 初始化所有数据源
                initializeDataSources();
                
                LOGGER.info("DataSource component initialized successfully");
                promise.complete();
            } catch (Exception e) {
                LOGGER.error("Failed to initialize datasource component", e);
                promise.fail(e);
            }
        });
    }
    
    /**
     * 注册数据源配置
     */
    private void registerDataSources(JsonObject config) {
        JsonObject datasources = config.getJsonObject("datasources");
        if (datasources == null || datasources.isEmpty()) {
            LOGGER.warn("No datasource configuration found");
            return;
        }
        
        for (String name : datasources.fieldNames()) {
            JsonObject dsConfig = datasources.getJsonObject(name);
            if (dsConfig != null) {
                dataSourceManager.registerDataSource(name, dsConfig)
                    .onSuccess(v -> LOGGER.info("Registered datasource: {}", name))
                    .onFailure(error -> LOGGER.error("Failed to register datasource: {}", name, error));
            }
        }
    }
    
    /**
     * 初始化所有数据源
     */
    private void initializeDataSources() {
        dataSourceManager.initializeAllDataSources()
            .onSuccess(v -> {
                LOGGER.info("All datasources initialized successfully");
                // 设置默认数据源
                if (!dataSourceManager.getDataSourceNames().isEmpty()) {
                    String defaultDs = dataSourceManager.getDataSourceNames().iterator().next();
                    dataSourceManager.setDefaultDataSource(defaultDs);
                    LOGGER.info("Set default datasource: {}", defaultDs);
                }
            })
            .onFailure(error -> LOGGER.error("Failed to initialize datasources", error));
    }
    
    @Override
    public Future<Void> stop() {
        return Future.future(promise -> {
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
                promise.complete();
            }
        });
    }
    
    @Override
    public int getPriority() {
        return 20; // 第二优先级
    }
    
    /**
     * 获取数据源管理器
     */
    public DataSourceManager getDataSourceManager() {
        return dataSourceManager;
    }
}