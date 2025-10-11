package cn.qaiu.db.dsl.core.executor;

import cn.qaiu.db.pool.JDBCType;
import io.vertx.sqlclient.Pool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 执行器策略注册表
 * 管理不同数据源类型的执行器策略
 * 
 * @author <a href="https://qaiu.top">QAIU</a>
 */
public class ExecutorStrategyRegistry {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ExecutorStrategyRegistry.class);
    
    private static final ExecutorStrategyRegistry INSTANCE = new ExecutorStrategyRegistry();
    
    private final Map<JDBCType, ExecutorStrategy> strategies = new ConcurrentHashMap<>();
    private final Map<Class<? extends Pool>, ExecutorStrategy> poolStrategies = new ConcurrentHashMap<>();
    
    private ExecutorStrategyRegistry() {
        registerBuiltinStrategies();
        loadExternalStrategies();
    }
    
    /**
     * 获取单例实例
     * 
     * @return 执行器策略注册表实例
     */
    public static ExecutorStrategyRegistry getInstance() {
        return INSTANCE;
    }
    
    /**
     * 注册内置执行器策略
     * 包括MySQL、PostgreSQL和H2策略
     */
    private void registerBuiltinStrategies() {
        // 注册MySQL策略（如果可用）
        try {
            registerStrategy(new MySQLExecutorStrategy());
            LOGGER.debug("Registered MySQL executor strategy");
        } catch (NoClassDefFoundError e) {
            LOGGER.warn("MySQL client not available, skipping MySQL executor strategy: {}", e.getMessage());
        }
        
        // 注册PostgreSQL策略（如果可用）
        try {
            registerStrategy(new PostgreSQLExecutorStrategy());
            LOGGER.debug("Registered PostgreSQL executor strategy");
        } catch (NoClassDefFoundError e) {
            LOGGER.warn("PostgreSQL client not available, skipping PostgreSQL executor strategy: {}", e.getMessage());
        }
        
        // 注册H2策略
        registerStrategy(new H2ExecutorStrategy());
        LOGGER.debug("Registered H2 executor strategy");
        
        LOGGER.info("Registered builtin executor strategies: H2 (MySQL/PostgreSQL strategies skipped if clients not available)");
    }
    
    /**
     * 通过SPI加载外部执行器策略
     * 支持动态扩展执行器策略
     */
    private void loadExternalStrategies() {
        try {
            ServiceLoader<ExecutorStrategy> serviceLoader = ServiceLoader.load(ExecutorStrategy.class);
            for (ExecutorStrategy strategy : serviceLoader) {
                registerStrategy(strategy);
                LOGGER.info("Loaded external executor strategy: {}", strategy.getSupportedType());
            }
        } catch (Exception e) {
            LOGGER.warn("Failed to load external executor strategies", e);
        }
    }
    
    /**
     * 注册执行器策略
     * 
     * @param strategy 执行器策略
     */
    public void registerStrategy(ExecutorStrategy strategy) {
        JDBCType type = strategy.getSupportedType();
        Class<? extends Pool> poolType = strategy.getPoolType();
        
        strategies.put(type, strategy);
        poolStrategies.put(poolType, strategy);
        
        LOGGER.debug("Registered executor strategy for type: {} and pool: {}", 
                type, poolType.getSimpleName());
    }
    
    /**
     * 根据数据源类型获取执行器策略
     * 
     * @param type 数据源类型
     * @return 执行器策略
     * @throws IllegalArgumentException 如果找不到对应的策略
     */
    public ExecutorStrategy getStrategy(JDBCType type) {
        ExecutorStrategy strategy = strategies.get(type);
        if (strategy == null) {
            throw new IllegalArgumentException("No executor strategy found for type: " + type);
        }
        return strategy;
    }
    
    /**
     * 根据连接池获取执行器策略
     * 
     * @param pool 连接池
     * @return 执行器策略
     * @throws IllegalArgumentException 如果找不到对应的策略
     */
    public ExecutorStrategy getStrategy(Pool pool) {
        // 首先尝试精确匹配
        ExecutorStrategy strategy = poolStrategies.get(pool.getClass());
        if (strategy != null) {
            return strategy;
        }
        
        // 然后尝试通过策略的supports方法匹配
        for (ExecutorStrategy s : strategies.values()) {
            if (s.supports(pool)) {
                return s;
            }
        }
        
        throw new IllegalArgumentException("No executor strategy found for pool: " + pool.getClass().getName());
    }
    
    /**
     * 检查是否支持指定的数据源类型
     * 
     * @param type 数据源类型
     * @return 是否支持
     */
    public boolean supports(JDBCType type) {
        return strategies.containsKey(type);
    }
    
    /**
     * 检查是否支持指定的连接池
     * 
     * @param pool 连接池
     * @return 是否支持
     */
    public boolean supports(Pool pool) {
        try {
            getStrategy(pool);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
    
    /**
     * 获取所有支持的数据源类型
     * 
     * @return 支持的数据源类型集合
     */
    public java.util.Set<JDBCType> getSupportedTypes() {
        return strategies.keySet();
    }
}
