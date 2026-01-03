package cn.qaiu.db.datasource;

import cn.qaiu.vx.core.lifecycle.DataSourceManagerInterface;
import cn.qaiu.vx.core.lifecycle.DataSourceProvider;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 数据库数据源提供者实现
 * 实现DataSourceProvider接口，为core模块提供数据源管理功能
 * 
 * @author <a href="https://qaiu.top">QAIU</a>
 */
public class DatabaseDataSourceProvider implements DataSourceProvider {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseDataSourceProvider.class);
    
    private DataSourceManagerInterface dataSourceManager;
    
    @Override
    public String getName() {
        return "database-datasource-provider";
    }
    
    @Override
    public boolean supports(String type) {
        // 支持常见的数据库类型
        return "h2".equalsIgnoreCase(type) || 
               "mysql".equalsIgnoreCase(type) || 
               "postgresql".equalsIgnoreCase(type) ||
               "postgres".equalsIgnoreCase(type) ||
               "oracle".equalsIgnoreCase(type) ||
               "sqlserver".equalsIgnoreCase(type);
    }
    
    @Override
    public DataSourceManagerInterface createDataSourceManager(Vertx vertx) {
        if (dataSourceManager == null) {
            dataSourceManager = cn.qaiu.db.datasource.DataSourceManager.getInstance();
        }
        return dataSourceManager;
    }
    
    @Override
    public Future<Void> initializeDataSources(Vertx vertx, JsonObject config) {
        LOGGER.info("Initializing datasources with provider: {}", getName());
        
        // 创建数据源管理器
        createDataSourceManager(vertx);
        
        // 获取数据库配置
        JsonObject databaseConfig = config.getJsonObject("database");
        if (databaseConfig == null || databaseConfig.isEmpty()) {
            LOGGER.warn("No database configuration found");
            return Future.succeededFuture();
        }
        
        // 注册所有数据源
        Future<Void> registrationFuture = Future.succeededFuture();
        for (String dataSourceName : databaseConfig.fieldNames()) {
            JsonObject dataSourceConfig = databaseConfig.getJsonObject(dataSourceName);
            if (dataSourceConfig != null) {
                String type = dataSourceConfig.getString("type");
                if (supports(type)) {
                    // 转换为DataSourceConfig对象
                    DataSourceConfig configObj = new DataSourceConfig();
                    configObj.setName(dataSourceName);
                    configObj.setType(type);
                    configObj.setUrl(dataSourceConfig.getString("url"));
                    configObj.setUsername(dataSourceConfig.getString("username"));
                    configObj.setPassword(dataSourceConfig.getString("password"));
                    
                    // 设置连接池参数
                    if (dataSourceConfig.containsKey("max_pool_size")) {
                        configObj.setMaxPoolSize(dataSourceConfig.getInteger("max_pool_size"));
                    }
                    if (dataSourceConfig.containsKey("min_pool_size")) {
                        configObj.setMinPoolSize(dataSourceConfig.getInteger("min_pool_size"));
                    }
                    
                    registrationFuture = registrationFuture.compose(v -> 
                        dataSourceManager.registerDataSource(dataSourceName, configObj.toJsonObject()));
                } else {
                    LOGGER.warn("Unsupported datasource type: {} for datasource: {}", type, dataSourceName);
                }
            }
        }
        
        // 初始化所有数据源
        return registrationFuture.compose(v -> dataSourceManager.initializeAllDataSources());
    }
    
    @Override
    public Future<Void> closeAllDataSources() {
        if (dataSourceManager != null) {
            return dataSourceManager.closeAllDataSources();
        }
        return Future.succeededFuture();
    }
    
    @Override
    public Object getPool(String name) {
        if (dataSourceManager != null) {
            return dataSourceManager.getPool(name);
        }
        return null;
    }
    
    @Override
    public List<String> getDataSourceNames() {
        if (dataSourceManager != null) {
            return dataSourceManager.getDataSourceNames();
        }
        return List.of();
    }
}
