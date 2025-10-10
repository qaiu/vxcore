package cn.qaiu.db.datasource;

// import io.vertx.core.Vertx; // 未使用

/**
 * 数据源上下文管理器
 * 支持线程级别的数据源切换
 * 
 * @author <a href="https://qaiu.top">QAIU</a>
 */
public class DataSourceContext {
    
    private static final ThreadLocal<String> CONTEXT_HOLDER = new ThreadLocal<>();
    
    private static final String DEFAULT_DATASOURCE = "default";
    
    /**
     * 设置当前线程的数据源
     */
    public static void setDataSource(String dataSourceName) {
        if (dataSourceName != null && !dataSourceName.trim().isEmpty()) {
            CONTEXT_HOLDER.set(dataSourceName);
        } else {
            CONTEXT_HOLDER.set(DEFAULT_DATASOURCE);
        }
    }
    
    /**
     * 获取当前线程的数据源
     */
    public static String getDataSource() {
        String dataSource = CONTEXT_HOLDER.get();
        return dataSource != null ? dataSource : DEFAULT_DATASOURCE;
    }
    
    /**
     * 清除当前线程的数据源
     */
    public static void clearDataSource() {
        CONTEXT_HOLDER.remove();
    }
    
    /**
     * 检查是否设置了数据源
     */
    public static boolean hasDataSource() {
        return CONTEXT_HOLDER.get() != null;
    }
    
    /**
     * 在指定数据源上下文中执行操作
     */
    public static <T> T executeWithDataSource(String dataSourceName, java.util.function.Supplier<T> operation) {
        String previousDataSource = getDataSource();
        try {
            setDataSource(dataSourceName);
            return operation.get();
        } finally {
            setDataSource(previousDataSource);
        }
    }
    
    /**
     * 在指定数据源上下文中执行操作（无返回值）
     */
    public static void executeWithDataSource(String dataSourceName, Runnable operation) {
        String previousDataSource = getDataSource();
        try {
            setDataSource(dataSourceName);
            operation.run();
        } finally {
            setDataSource(previousDataSource);
        }
    }
}
