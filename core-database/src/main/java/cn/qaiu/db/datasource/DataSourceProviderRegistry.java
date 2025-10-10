package cn.qaiu.db.datasource;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.jdbcclient.JDBCPool;
// import io.vertx.pgclient.PgPool; // 暂时注释掉，避免依赖问题
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.PoolOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 数据源提供者注册表
 * 支持SPI机制动态加载数据源提供者
 * 
 * @author <a href="https://qaiu.top">QAIU</a>
 */
public class DataSourceProviderRegistry {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(DataSourceProviderRegistry.class);
    
    private final Map<String, DataSourceProvider> providers = new ConcurrentHashMap<>();
    
    /**
     * 注册内置的数据源提供者
     */
    public void registerBuiltinProviders() {
        // 注册JDBC提供者
        registerProvider(new JdbcDataSourceProvider());
        
        // 注册PostgreSQL提供者
        registerProvider(new PostgreSQLDataSourceProvider());
        
        // 注册MySQL提供者
        registerProvider(new MySQLDataSourceProvider());
        
        // 注册H2提供者
        registerProvider(new H2DataSourceProvider());
        
        // 通过SPI加载外部提供者
        loadExternalProviders();
    }
    
    /**
     * 通过SPI加载外部数据源提供者
     */
    private void loadExternalProviders() {
        try {
            ServiceLoader<DataSourceProvider> serviceLoader = ServiceLoader.load(DataSourceProvider.class);
            for (DataSourceProvider provider : serviceLoader) {
                registerProvider(provider);
                LOGGER.info("Loaded external datasource provider: {}", provider.getType());
            }
        } catch (Exception e) {
            LOGGER.warn("Failed to load external datasource providers", e);
        }
    }
    
    /**
     * 注册数据源提供者
     */
    public void registerProvider(DataSourceProvider provider) {
        providers.put(provider.getType(), provider);
        LOGGER.info("Registered datasource provider: {}", provider.getType());
    }
    
    /**
     * 获取数据源提供者
     */
    public DataSourceProvider getProvider(String type) {
        return providers.get(type);
    }
    
    /**
     * 获取所有已注册的提供者类型
     */
    public java.util.Set<String> getProviderTypes() {
        return providers.keySet();
    }
    
    /**
     * JDBC数据源提供者（通用）
     */
    public static class JdbcDataSourceProvider implements DataSourceProvider {
        
        @Override
        public String getType() {
            return "jdbc";
        }
        
        @Override
        public Future<Pool> createPool(DataSourceConfig config) {
            return Future.future(promise -> {
                try {
                    Vertx vertx = Vertx.currentContext() != null ? 
                        Vertx.currentContext().owner() : Vertx.vertx();
                    
                    Pool pool = JDBCPool.pool(vertx, config.toJsonObject());
                    promise.complete(pool);
                } catch (Exception e) {
                    promise.fail(e);
                }
            });
        }
        
        @Override
        public Future<Boolean> isAvailable(DataSourceConfig config) {
            return Future.future(promise -> {
                createPool(config)
                    .compose(pool -> pool.getConnection())
                    .onSuccess(conn -> {
                        conn.close();
                        promise.complete(true);
                    })
                    .onFailure(error -> promise.complete(false));
            });
        }
        
        @Override
        public Future<Void> close(Pool pool) {
            return pool.close();
        }
    }
    
    /**
     * PostgreSQL数据源提供者
     */
    public static class PostgreSQLDataSourceProvider implements DataSourceProvider {
        
        @Override
        public String getType() {
            return "postgresql";
        }
        
        @Override
        public Future<Pool> createPool(DataSourceConfig config) {
            return Future.future(promise -> {
                try {
                    Vertx vertx = Vertx.currentContext() != null ? 
                        Vertx.currentContext().owner() : Vertx.vertx();
                    
                    PoolOptions poolOptions = new PoolOptions()
                        .setMaxSize(config.getMaxPoolSize());
                    
                    // 使用JDBC Pool for PostgreSQL (暂时使用JDBC，避免依赖问题)
                    Pool pool = JDBCPool.pool(vertx, config.toJsonObject());
                    promise.complete(pool);
                } catch (Exception e) {
                    promise.fail(e);
                }
            });
        }
        
        @Override
        public Future<Boolean> isAvailable(DataSourceConfig config) {
            return Future.future(promise -> {
                createPool(config)
                    .compose(pool -> pool.getConnection())
                    .onSuccess(conn -> {
                        conn.close();
                        promise.complete(true);
                    })
                    .onFailure(error -> promise.complete(false));
            });
        }
        
        @Override
        public Future<Void> close(Pool pool) {
            return pool.close();
        }
    }
    
    /**
     * MySQL数据源提供者
     */
    public static class MySQLDataSourceProvider implements DataSourceProvider {
        
        @Override
        public String getType() {
            return "mysql";
        }
        
        @Override
        public Future<Pool> createPool(DataSourceConfig config) {
            return Future.future(promise -> {
                try {
                    Vertx vertx = Vertx.currentContext() != null ? 
                        Vertx.currentContext().owner() : Vertx.vertx();
                    
                    PoolOptions poolOptions = new PoolOptions()
                        .setMaxSize(config.getMaxPoolSize());
                    
                    // 使用JDBC Pool for MySQL
                    Pool pool = JDBCPool.pool(vertx, config.toJsonObject());
                    promise.complete(pool);
                } catch (Exception e) {
                    promise.fail(e);
                }
            });
        }
        
        @Override
        public Future<Boolean> isAvailable(DataSourceConfig config) {
            return Future.future(promise -> {
                createPool(config)
                    .compose(pool -> pool.getConnection())
                    .onSuccess(conn -> {
                        conn.close();
                        promise.complete(true);
                    })
                    .onFailure(error -> promise.complete(false));
            });
        }
        
        @Override
        public Future<Void> close(Pool pool) {
            return pool.close();
        }
    }
    
    /**
     * H2数据源提供者
     */
    public static class H2DataSourceProvider implements DataSourceProvider {
        
        @Override
        public String getType() {
            return "h2";
        }
        
        @Override
        public Future<Pool> createPool(DataSourceConfig config) {
            return Future.future(promise -> {
                try {
                    Vertx vertx = Vertx.currentContext() != null ? 
                        Vertx.currentContext().owner() : Vertx.vertx();
                    
                    PoolOptions poolOptions = new PoolOptions()
                        .setMaxSize(config.getMaxPoolSize());
                    
                    // 使用JDBC Pool for H2
                    Pool pool = JDBCPool.pool(vertx, config.toJsonObject());
                    promise.complete(pool);
                } catch (Exception e) {
                    promise.fail(e);
                }
            });
        }
        
        @Override
        public Future<Boolean> isAvailable(DataSourceConfig config) {
            return Future.future(promise -> {
                createPool(config)
                    .compose(pool -> pool.getConnection())
                    .onSuccess(conn -> {
                        conn.close();
                        promise.complete(true);
                    })
                    .onFailure(error -> promise.complete(false));
            });
        }
        
        @Override
        public Future<Void> close(Pool pool) {
            return pool.close();
        }
    }
}
