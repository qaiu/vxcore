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
    private DataSourceProvider dataSourceProvider;
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
                
                // 使用SPI模式查找数据源提供者
                DataSourceProviderRegistry registry = DataSourceProviderRegistry.getInstance();
                registry.initialize();
                
                // 查找支持的数据源提供者
                DataSourceProvider provider = findSupportedProvider(registry, databaseConfig);
                if (provider != null) {
                    this.dataSourceProvider = provider;
                    LOGGER.info("Found DataSource provider: {} for database configuration", provider.getName());
                } else {
                    LOGGER.warn("No DataSource provider found for database configuration, datasource will not be initialized");
                }
                
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
                
                if (dataSourceProvider != null) {
                    // 使用提供者初始化数据源
                    dataSourceProvider.initializeDataSources(vertx, globalConfig)
                        .onSuccess(v -> {
                            LOGGER.info("DataSource component started successfully with provider: {}", 
                                       dataSourceProvider.getName());
                            promise.complete();
                        })
                        .onFailure(error -> {
                            LOGGER.error("Failed to start datasource component", error);
                            promise.fail(error);
                        });
                } else {
                    LOGGER.info("No DataSource provider available, component started without datasource");
                    promise.complete();
                }
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
                
                if (dataSourceProvider != null) {
                    // 使用提供者关闭数据源
                    dataSourceProvider.closeAllDataSources()
                        .onSuccess(v -> {
                            LOGGER.info("All datasources closed successfully");
                            promise.complete();
                        })
                        .onFailure(error -> {
                            LOGGER.error("Failed to close datasources", error);
                            promise.fail(error);
                        });
                } else {
                    LOGGER.info("No datasource provider to close");
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
     * 获取数据源提供者
     * 
     * @return DataSourceProvider实例
     */
    public DataSourceProvider getDataSourceProvider() {
        return dataSourceProvider;
    }
    
    /**
     * 设置数据源提供者
     * 
     * @param dataSourceProvider DataSourceProvider实例
     */
    public void setDataSourceProvider(DataSourceProvider dataSourceProvider) {
        this.dataSourceProvider = dataSourceProvider;
    }
    
    /**
     * 获取数据源管理器（通过提供者）
     * 
     * @return DataSourceManagerInterface实例
     */
    public DataSourceManagerInterface getDataSourceManager() {
        if (dataSourceProvider != null) {
            return dataSourceProvider.createDataSourceManager(vertx);
        }
        return null;
    }
    
    /**
     * 设置数据源管理器（直接注入实现）
     * 
     * @param dataSourceManager 数据源管理器实现
     */
    public void setDataSourceManager(DataSourceManagerInterface dataSourceManager) {
        // 这个方法主要用于测试或直接注入实现
        // 正常情况下应该通过DataSourceProvider来获取
        LOGGER.warn("Direct injection of DataSourceManager is not recommended. Use DataSourceProvider instead.");
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
    
    /**
     * 查找支持的数据源提供者
     * 
     * @param registry 提供者注册表
     * @param databaseConfig 数据库配置
     * @return 支持的数据源提供者
     */
    private DataSourceProvider findSupportedProvider(DataSourceProviderRegistry registry, JsonObject databaseConfig) {
        // 遍历所有数据源配置，查找支持的提供者
        for (String dataSourceName : databaseConfig.fieldNames()) {
            JsonObject dataSourceConfig = databaseConfig.getJsonObject(dataSourceName);
            if (dataSourceConfig != null) {
                String type = dataSourceConfig.getString("type");
                if (type != null) {
                    DataSourceProvider provider = registry.getProviderByType(type);
                    if (provider != null) {
                        return provider;
                    }
                }
            }
        }
        return null;
    }
}