package cn.qaiu.db.dsl.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SQL审计统计工具
 * 
 * 提供SQL执行统计信息的查询和管理功能
 * 
 * @author <a href="https://qaiu.top">QAIU</a>
 */
public class SqlAuditStatistics {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(SqlAuditStatistics.class);
    
    /**
     * 获取所有SQL统计信息
     */
    public static Map<String, SqlAuditListener.QueryStatistics> getAllStatistics() {
        return SqlAuditListener.getAllStatistics();
    }
    
    /**
     * 获取指定SQL的统计信息
     */
    public static SqlAuditListener.QueryStatistics getStatistics(String normalizedSql) {
        return SqlAuditListener.getQueryStatistics(normalizedSql);
    }
    
    /**
     * 打印所有SQL统计信息
     */
    public static void printAllStatistics() {
        Map<String, SqlAuditListener.QueryStatistics> stats = getAllStatistics();
        
        if (stats.isEmpty()) {
            LOGGER.info("没有SQL执行统计信息");
            return;
        }
        
        LOGGER.info("=== SQL执行统计信息 ===");
        LOGGER.info("总查询类型数: {}", stats.size());
        
        long totalExecutions = 0;
        long totalTime = 0;
        long totalErrors = 0;
        
        for (SqlAuditListener.QueryStatistics stat : stats.values()) {
            totalExecutions += stat.getExecutionCount();
            totalTime += stat.getTotalExecutionTime();
            totalErrors += stat.getErrorCount();
            
            LOGGER.info("SQL: {}", stat.getSql());
            LOGGER.info("  执行次数: {}", stat.getExecutionCount());
            LOGGER.info("  平均耗时: {:.2f}ms", stat.getAverageExecutionTime());
            LOGGER.info("  错误次数: {}", stat.getErrorCount());
            LOGGER.info("  错误率: {:.2f}%", stat.getErrorRate() * 100);
            LOGGER.info("---");
        }
        
        LOGGER.info("=== 总体统计 ===");
        LOGGER.info("总执行次数: {}", totalExecutions);
        LOGGER.info("总执行时间: {}ms", totalTime);
        LOGGER.info("总错误次数: {}", totalErrors);
        LOGGER.info("平均执行时间: {:.2f}ms", totalExecutions > 0 ? (double) totalTime / totalExecutions : 0);
        LOGGER.info("总体错误率: {:.2f}%", totalExecutions > 0 ? (double) totalErrors / totalExecutions * 100 : 0);
    }
    
    /**
     * 打印慢查询统计（执行时间超过指定阈值的查询）
     */
    public static void printSlowQueries(long thresholdMs) {
        Map<String, SqlAuditListener.QueryStatistics> stats = getAllStatistics();
        
        LOGGER.info("=== 慢查询统计 (阈值: {}ms) ===", thresholdMs);
        
        boolean hasSlowQueries = false;
        for (SqlAuditListener.QueryStatistics stat : stats.values()) {
            if (stat.getAverageExecutionTime() > thresholdMs) {
                hasSlowQueries = true;
                LOGGER.warn("慢查询: {}", stat.getSql());
                LOGGER.warn("  平均耗时: {:.2f}ms", stat.getAverageExecutionTime());
                LOGGER.warn("  执行次数: {}", stat.getExecutionCount());
                LOGGER.warn("---");
            }
        }
        
        if (!hasSlowQueries) {
            LOGGER.info("没有发现慢查询");
        }
    }
    
    /**
     * 打印错误查询统计
     */
    public static void printErrorQueries() {
        Map<String, SqlAuditListener.QueryStatistics> stats = getAllStatistics();
        
        LOGGER.info("=== 错误查询统计 ===");
        
        boolean hasErrors = false;
        for (SqlAuditListener.QueryStatistics stat : stats.values()) {
            if (stat.getErrorCount() > 0) {
                hasErrors = true;
                LOGGER.error("错误查询: {}", stat.getSql());
                LOGGER.error("  错误次数: {}", stat.getErrorCount());
                LOGGER.error("  错误率: {:.2f}%", stat.getErrorRate() * 100);
                LOGGER.error("  总执行次数: {}", stat.getExecutionCount());
                LOGGER.error("---");
            }
        }
        
        if (!hasErrors) {
            LOGGER.info("没有发现错误查询");
        }
    }
    
    /**
     * 重置所有统计信息
     */
    public static void resetStatistics() {
        SqlAuditListener.resetStatistics();
        LOGGER.info("SQL统计信息已重置");
    }
    
    /**
     * 导出统计信息为JSON格式
     */
    public static String exportStatisticsAsJson() {
        Map<String, SqlAuditListener.QueryStatistics> stats = getAllStatistics();
        
        StringBuilder json = new StringBuilder();
        json.append("{\n");
        json.append("  \"statistics\": [\n");
        
        boolean first = true;
        for (SqlAuditListener.QueryStatistics stat : stats.values()) {
            if (!first) {
                json.append(",\n");
            }
            first = false;
            
            json.append("    {\n");
            json.append("      \"sql\": \"").append(escapeJson(stat.getSql())).append("\",\n");
            json.append("      \"executionCount\": ").append(stat.getExecutionCount()).append(",\n");
            json.append("      \"totalExecutionTime\": ").append(stat.getTotalExecutionTime()).append(",\n");
            json.append("      \"averageExecutionTime\": ").append(stat.getAverageExecutionTime()).append(",\n");
            json.append("      \"errorCount\": ").append(stat.getErrorCount()).append(",\n");
            json.append("      \"errorRate\": ").append(stat.getErrorRate()).append("\n");
            json.append("    }");
        }
        
        json.append("\n  ]\n");
        json.append("}\n");
        
        return json.toString();
    }
    
    /**
     * 转义JSON字符串
     */
    private static String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\"", "\\\"")
                 .replace("\n", "\\n")
                 .replace("\r", "\\r")
                 .replace("\t", "\\t");
    }
}
