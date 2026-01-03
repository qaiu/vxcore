package cn.qaiu.db.dsl.core;

import cn.qaiu.db.datasource.DataSource;
import cn.qaiu.db.datasource.DataSourceContext;
import cn.qaiu.db.datasource.DataSourceManager;
import io.vertx.core.Future;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.RowSet;
import org.jooq.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

/**
 * 支持多数据源的DAO基类
 * 通过注解和上下文实现数据源动态切换
 * 
 * @author <a href="https://qaiu.top">QAIU</a>
 */
public abstract class MultiDataSourceDao {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(MultiDataSourceDao.class);
    
    protected final DataSourceManager dataSourceManager;
    protected final Class<?> entityClass;
    
    public MultiDataSourceDao(Class<?> entityClass) {
        this.entityClass = entityClass;
        this.dataSourceManager = DataSourceManager.getInstance();
    }
    
    /**
     * 获取当前数据源名称
     */
    protected String getCurrentDataSource() {
        // 检查方法级别的@DataSource注解
        String methodDataSource = getMethodDataSource();
        if (methodDataSource != null) {
            return methodDataSource;
        }
        
        // 检查类级别的@DataSource注解
        String classDataSource = getClassDataSource();
        if (classDataSource != null) {
            return classDataSource;
        }
        
        // 使用线程上下文中的数据源
        return DataSourceContext.getDataSource();
    }
    
    /**
     * 获取方法级别的数据源注解
     */
    private String getMethodDataSource() {
        try {
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            for (StackTraceElement element : stackTrace) {
                if (element.getClassName().equals(this.getClass().getName())) {
                    String methodName = element.getMethodName();
                    Method[] methods = this.getClass().getDeclaredMethods();
                    for (Method method : methods) {
                        if (method.getName().equals(methodName)) {
                            DataSource annotation = method.getAnnotation(DataSource.class);
                            if (annotation != null) {
                                return annotation.value();
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.warn("Failed to get method datasource annotation", e);
        }
        return null;
    }
    
    /**
     * 获取类级别的数据源注解
     */
    private String getClassDataSource() {
        DataSource annotation = this.getClass().getAnnotation(DataSource.class);
        return annotation != null ? annotation.value() : null;
    }
    
    /**
     * 获取当前数据源的连接池
     */
    protected Pool getCurrentPool() {
        String dataSourceName = getCurrentDataSource();
        Object poolObj = dataSourceManager.getPool(dataSourceName);
        Pool pool = poolObj instanceof Pool ? (Pool) poolObj : null;
        if (pool == null) {
            LOGGER.warn("Pool not found for datasource: {}, using default", dataSourceName);
            pool = dataSourceManager.getDefaultPool();
        }
        return pool;
    }
    
    /**
     * 获取当前数据源的JooqExecutor
     */
    protected JooqExecutor getCurrentExecutor() {
        String dataSourceName = getCurrentDataSource();
        JooqExecutor executor = dataSourceManager.getExecutor(dataSourceName);
        if (executor == null) {
            LOGGER.warn("Executor not found for datasource: {}, using default", dataSourceName);
            executor = dataSourceManager.getDefaultExecutor();
        }
        return executor;
    }
    
    /**
     * 执行查询
     */
    protected Future<RowSet<io.vertx.sqlclient.Row>> executeQuery(Query query) {
        JooqExecutor executor = getCurrentExecutor();
        return executor.executeQuery(query);
    }
    
    /**
     * 执行更新
     */
    protected Future<Integer> executeUpdate(Query query) {
        JooqExecutor executor = getCurrentExecutor();
        return executor.executeUpdate(query);
    }
    
    /**
     * 在指定数据源中执行操作
     */
    protected <T> Future<T> executeWithDataSource(String dataSourceName, 
                                                  java.util.function.Function<JooqExecutor, Future<T>> operation) {
        return Future.future(promise -> {
            String previousDataSource = DataSourceContext.getDataSource();
            try {
                DataSourceContext.setDataSource(dataSourceName);
                JooqExecutor executor = dataSourceManager.getExecutor(dataSourceName);
                if (executor == null) {
                    promise.fail("Executor not found for datasource: " + dataSourceName);
                    return;
                }
                
                operation.apply(executor)
                    .onSuccess(promise::complete)
                    .onFailure(promise::fail);
            } finally {
                DataSourceContext.setDataSource(previousDataSource);
            }
        });
    }
    
    /**
     * 获取实体类
     */
    public Class<?> getEntityClass() {
        return entityClass;
    }
}
