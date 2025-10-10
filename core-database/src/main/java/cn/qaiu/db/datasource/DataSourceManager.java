package cn.qaiu.db.datasource;

import cn.qaiu.db.dsl.core.JooqExecutor;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.Pool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 数据源管理器
 * 支持多数据源的配置、创建、管理和动态切换
 * 
 * @author <a href="https://qaiu.top">QAIU</a>
 */
public class DataSourceManager {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(DataSourceManager.class);
    
    private static final AtomicReference<DataSourceManager> INSTANCE = new AtomicReference<>();
    
    private final Vertx vertx;
    private final Map<String, Pool> pools = new ConcurrentHashMap<>();
    private final Map<String, JooqExecutor> executors = new ConcurrentHashMap<>();
    private final Map<String, DataSourceConfig> configs = new ConcurrentHashMap<>();
    private final DataSourceProviderRegistry providerRegistry;
    
    private String defaultDataSource = "default";
    
    private DataSourceManager(Vertx vertx) {
        this.vertx = vertx;
        this.providerRegistry = new DataSourceProviderRegistry();
        this.providerRegistry.registerBuiltinProviders();
    }
    
    /**
     * 获取单例实例
     */
    public static DataSourceManager getInstance(Vertx vertx) {
        return INSTANCE.updateAndGet(manager -> 
            manager == null ? new DataSourceManager(vertx) : manager);
    }
    
    /**
     * 注册数据源配置
     */
    public Future<Void> registerDataSource(String name, DataSourceConfig config) {
        return Future.future(promise -> {
            try {
                configs.put(name, config);
                LOGGER.info("Registered datasource config: {}", name);
                promise.complete();
            } catch (Exception e) {
                LOGGER.error("Failed to register datasource config: {}", name, e);
                promise.fail(e);
            }
        });
    }
    
    /**
     * 从JsonObject注册数据源配置
     */
    public Future<Void> registerDataSource(String name, JsonObject config) {
        DataSourceConfig dsConfig = DataSourceConfig.fromJsonObject(name, config);
        return registerDataSource(name, dsConfig);
    }
    
    /**
     * 初始化数据源
     */
    public Future<Void> initializeDataSource(String name) {
        return Future.future(promise -> {
            DataSourceConfig config = configs.get(name);
            if (config == null) {
                promise.fail("DataSource config not found: " + name);
                return;
            }
            
            if (pools.containsKey(name)) {
                LOGGER.warn("DataSource already initialized: {}", name);
                promise.complete();
                return;
            }
            
            DataSourceProvider provider = providerRegistry.getProvider(config.getType());
            if (provider == null) {
                promise.fail("No provider found for datasource type: " + config.getType());
                return;
            }
            
            provider.createPool(config)
                .onSuccess(pool -> {
                    pools.put(name, pool);
                    JooqExecutor executor = new JooqExecutor(pool);
                    executors.put(name, executor);
                    LOGGER.info("Initialized datasource: {}", name);
                    promise.complete();
                })
                .onFailure(error -> {
                    LOGGER.error("Failed to initialize datasource: {}", name, error);
                    promise.fail(error);
                });
        });
    }
    
    /**
     * 初始化所有数据源
     */
    public Future<Void> initializeAllDataSources() {
        return Future.future(promise -> {
            if (configs.isEmpty()) {
                LOGGER.warn("No datasource configs found");
                promise.complete();
                return;
            }
            
            Future<Void> allFutures = Future.succeededFuture();
            for (String name : configs.keySet()) {
                allFutures = allFutures.compose(v -> initializeDataSource(name));
            }
            
            allFutures.onComplete(promise);
        });
    }
    
    /**
     * 获取数据源连接池
     */
    public Pool getPool(String name) {
        Pool pool = pools.get(name);
        if (pool == null) {
            LOGGER.warn("Pool not found for datasource: {}, using default", name);
            return pools.get(defaultDataSource);
        }
        return pool;
    }
    
    /**
     * 获取JooqExecutor
     */
    public JooqExecutor getExecutor(String name) {
        JooqExecutor executor = executors.get(name);
        if (executor == null) {
            LOGGER.warn("Executor not found for datasource: {}, using default", name);
            return executors.get(defaultDataSource);
        }
        return executor;
    }
    
    /**
     * 获取默认数据源连接池
     */
    public Pool getDefaultPool() {
        return getPool(defaultDataSource);
    }
    
    /**
     * 获取默认JooqExecutor
     */
    public JooqExecutor getDefaultExecutor() {
        return getExecutor(defaultDataSource);
    }
    
    /**
     * 设置默认数据源
     */
    public void setDefaultDataSource(String name) {
        if (configs.containsKey(name)) {
            this.defaultDataSource = name;
            LOGGER.info("Set default datasource: {}", name);
        } else {
            LOGGER.warn("Cannot set default datasource, config not found: {}", name);
        }
    }
    
    /**
     * 检查数据源是否可用
     */
    public Future<Boolean> isDataSourceAvailable(String name) {
        return Future.future(promise -> {
            DataSourceConfig config = configs.get(name);
            if (config == null) {
                promise.complete(false);
                return;
            }
            
            DataSourceProvider provider = providerRegistry.getProvider(config.getType());
            if (provider == null) {
                promise.complete(false);
                return;
            }
            
            provider.isAvailable(config).onComplete(promise);
        });
    }
    
    /**
     * 关闭数据源
     */
    public Future<Void> closeDataSource(String name) {
        return Future.future(promise -> {
            Pool pool = pools.remove(name);
            executors.remove(name);
            
            if (pool == null) {
                promise.complete();
                return;
            }
            
            DataSourceConfig config = configs.get(name);
            if (config != null) {
                DataSourceProvider provider = providerRegistry.getProvider(config.getType());
                if (provider != null) {
                    provider.close(pool).onComplete(promise);
                } else {
                    pool.close().onComplete(promise);
                }
            } else {
                pool.close().onComplete(promise);
            }
            
            LOGGER.info("Closed datasource: {}", name);
        });
    }
    
    /**
     * 关闭所有数据源
     */
    public Future<Void> closeAllDataSources() {
        return Future.future(promise -> {
            Future<Void> allFutures = Future.succeededFuture();
            for (String name : pools.keySet()) {
                allFutures = allFutures.compose(v -> closeDataSource(name));
            }
            
            allFutures.onComplete(promise);
        });
    }
    
    /**
     * 获取所有数据源名称
     */
    public java.util.Set<String> getDataSourceNames() {
        return configs.keySet();
    }
    
    /**
     * 获取数据源配置
     */
    public DataSourceConfig getDataSourceConfig(String name) {
        return configs.get(name);
    }
}
