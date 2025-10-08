package cn.qaiu.db.dsl.core;

import org.jooq.ExecuteContext;
import org.jooq.ExecuteListener;
import org.jooq.Query;
import org.jooq.Record;
import org.jooq.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * SQL审计监听器
 * 
 * 提供SQL执行审计功能，包括：
 * - SQL语句记录
 * - 执行时间统计
 * - 参数绑定记录
 * - 性能监控
 * - 错误追踪
 * 
 * @author <a href="https://qaiu.top">QAIU</a>
 */
public class SqlAuditListener implements ExecuteListener {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(SqlAuditListener.class);
    
    // 执行统计
    private static final ConcurrentHashMap<String, AtomicLong> queryCounts = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, AtomicLong> totalExecutionTime = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, AtomicLong> errorCounts = new ConcurrentHashMap<>();
    
    // 线程本地存储执行开始时间
    private static final ThreadLocal<LocalDateTime> executionStartTime = new ThreadLocal<>();
    
    @Override
    public void executeStart(ExecuteContext ctx) {
        LocalDateTime startTime = LocalDateTime.now();
        executionStartTime.set(startTime);
        
        // 记录SQL语句
        Query query = ctx.query();
        if (query != null) {
            String sql = query.getSQL();
            String normalizedSql = normalizeSql(sql);
            
            LOGGER.debug("SQL执行开始: {}", sql);
            LOGGER.debug("SQL参数: {}", ctx.data());
            
            // 更新查询计数
            queryCounts.computeIfAbsent(normalizedSql, k -> new AtomicLong(0)).incrementAndGet();
        }
    }
    
    @Override
    public void executeEnd(ExecuteContext ctx) {
        LocalDateTime startTime = executionStartTime.get();
        if (startTime != null) {
            LocalDateTime endTime = LocalDateTime.now();
            Duration executionTime = Duration.between(startTime, endTime);
            long executionTimeMs = executionTime.toMillis();
            
            Query query = ctx.query();
            if (query != null) {
                String sql = query.getSQL();
                String normalizedSql = normalizeSql(sql);
                
                // 更新执行时间统计
                totalExecutionTime.computeIfAbsent(normalizedSql, k -> new AtomicLong(0))
                    .addAndGet(executionTimeMs);
                
                LOGGER.debug("SQL执行完成: {} (耗时: {}ms)", sql, executionTimeMs);
                
                // 性能警告
                if (executionTimeMs > 1000) {
                    LOGGER.warn("慢查询检测: {} (耗时: {}ms)", sql, executionTimeMs);
                }
            }
            
            executionStartTime.remove();
        }
    }
    
    @Override
    public void exception(ExecuteContext ctx) {
        LocalDateTime startTime = executionStartTime.get();
        if (startTime != null) {
            LocalDateTime endTime = LocalDateTime.now();
            Duration executionTime = Duration.between(startTime, endTime);
            
            Query query = ctx.query();
            if (query != null) {
                String sql = query.getSQL();
                String normalizedSql = normalizeSql(sql);
                
                // 更新错误计数
                errorCounts.computeIfAbsent(normalizedSql, k -> new AtomicLong(0)).incrementAndGet();
                
                LOGGER.error("SQL执行异常: {} (耗时: {}ms)", sql, executionTime.toMillis());
                LOGGER.error("异常信息: {}", ctx.exception().getMessage());
            }
            
            executionStartTime.remove();
        }
    }
    
    /**
     * 标准化SQL语句（用于统计）
     */
    private String normalizeSql(String sql) {
        if (sql == null) return "";
        
        // 移除多余的空格和换行
        String normalized = sql.replaceAll("\\s+", " ").trim();
        
        // 移除参数占位符，用于统计相同类型的查询
        normalized = normalized.replaceAll("\\?", "?");
        
        return normalized;
    }
    
    /**
     * 手动增加查询计数（用于测试或手动触发）
     */
    public static void incrementQueryCount(String normalizedSql) {
        queryCounts.computeIfAbsent(normalizedSql, k -> new AtomicLong(0)).incrementAndGet();
    }
    
    /**
     * 获取查询统计信息
     */
    public static QueryStatistics getQueryStatistics(String normalizedSql) {
        long count = queryCounts.getOrDefault(normalizedSql, new AtomicLong(0)).get();
        long totalTime = totalExecutionTime.getOrDefault(normalizedSql, new AtomicLong(0)).get();
        long errors = errorCounts.getOrDefault(normalizedSql, new AtomicLong(0)).get();
        
        return new QueryStatistics(normalizedSql, count, totalTime, errors);
    }
    
    /**
     * 获取所有查询统计信息
     */
    public static ConcurrentHashMap<String, QueryStatistics> getAllStatistics() {
        ConcurrentHashMap<String, QueryStatistics> stats = new ConcurrentHashMap<>();
        
        queryCounts.forEach((sql, count) -> {
            long totalTime = totalExecutionTime.getOrDefault(sql, new AtomicLong(0)).get();
            long errors = errorCounts.getOrDefault(sql, new AtomicLong(0)).get();
            stats.put(sql, new QueryStatistics(sql, count.get(), totalTime, errors));
        });
        
        return stats;
    }
    
    /**
     * 重置统计信息
     */
    public static void resetStatistics() {
        queryCounts.clear();
        totalExecutionTime.clear();
        errorCounts.clear();
    }
    
    /**
     * 查询统计信息
     */
    public static class QueryStatistics {
        private final String sql;
        private final long executionCount;
        private final long totalExecutionTime;
        private final long errorCount;
        
        public QueryStatistics(String sql, long executionCount, long totalExecutionTime, long errorCount) {
            this.sql = sql;
            this.executionCount = executionCount;
            this.totalExecutionTime = totalExecutionTime;
            this.errorCount = errorCount;
        }
        
        public String getSql() { return sql; }
        public long getExecutionCount() { return executionCount; }
        public long getTotalExecutionTime() { return totalExecutionTime; }
        public long getErrorCount() { return errorCount; }
        
        public double getAverageExecutionTime() {
            return executionCount > 0 ? (double) totalExecutionTime / executionCount : 0.0;
        }
        
        public double getErrorRate() {
            return executionCount > 0 ? (double) errorCount / executionCount : 0.0;
        }
        
        @Override
        public String toString() {
            return String.format("QueryStatistics{sql='%s', count=%d, avgTime=%.2fms, errors=%d, errorRate=%.2f%%}",
                sql, executionCount, getAverageExecutionTime(), errorCount, getErrorRate() * 100);
        }
    }
}
