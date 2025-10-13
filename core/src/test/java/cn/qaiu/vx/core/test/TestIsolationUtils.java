package cn.qaiu.vx.core.test;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

/**
 * 测试隔离工具类
 * 提供测试隔离相关的工具方法，确保测试之间不会相互影响
 * 
 * @author <a href="https://qaiu.top">QAIU</a>
 */
public class TestIsolationUtils {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(TestIsolationUtils.class);
    
    /**
     * 生成随机端口号
     * 
     * @return 随机端口号 (8000-8999)
     */
    public static int generateRandomPort() {
        return 8000 + (int)(Math.random() * 1000);
    }
    
    /**
     * 生成唯一的数据库名称
     * 
     * @return 唯一的数据库名称
     */
    public static String generateUniqueDbName() {
        return "testdb_" + UUID.randomUUID().toString().replace("-", "");
    }
    
    /**
     * 生成唯一的测试ID
     * 
     * @return 唯一的测试ID
     */
    public static String generateTestId() {
        return "test_" + System.currentTimeMillis() + "_" + 
               Thread.currentThread().getId();
    }
    
    /**
     * 创建测试配置，使用随机端口和唯一数据库
     * 
     * @return 测试配置
     */
    public static JsonObject createTestConfig() {
        return createTestConfig(generateRandomPort(), generateUniqueDbName());
    }
    
    /**
     * 创建测试配置
     * 
     * @param port 端口号
     * @param dbName 数据库名称
     * @return 测试配置
     */
    public static JsonObject createTestConfig(int port, String dbName) {
        return new JsonObject()
            .put("server", new JsonObject()
                .put("port", port)
                .put("host", "127.0.0.1"))
            .put("database", new JsonObject()
                .put("default", new JsonObject()
                    .put("type", "h2")
                    .put("url", "jdbc:h2:mem:" + dbName + ";DB_CLOSE_DELAY=-1;MODE=MySQL")
                    .put("username", "sa")
                    .put("password", "")))
            .put("custom", new JsonObject()
                .put("baseLocations", "cn.qaiu.test")
                .put("gatewayPrefix", "api"));
    }
    
    /**
     * 检查是否是端口占用错误
     * 
     * @param error 错误信息
     * @return 是否是端口占用错误
     */
    public static boolean isPortConflictError(Throwable error) {
        if (error == null) return false;
        String message = error.getMessage();
        return message != null && (
            message.contains("Bind") || 
            message.contains("Address already in use") ||
            message.contains("Port already in use")
        );
    }
    
    /**
     * 安全关闭Vertx实例
     * 
     * @param vertx Vertx实例
     * @param timeoutMs 超时时间（毫秒）
     */
    public static void safeCloseVertx(Vertx vertx, long timeoutMs) {
        if (vertx != null) {
            try {
                vertx.close().toCompletionStage()
                    .toCompletableFuture()
                    .get(timeoutMs, java.util.concurrent.TimeUnit.MILLISECONDS);
                LOGGER.debug("Vertx instance closed successfully");
            } catch (Exception e) {
                LOGGER.warn("Failed to close Vertx instance: {}", e.getMessage());
            }
        }
    }
    
    /**
     * 安全关闭Vertx实例（默认5秒超时）
     * 
     * @param vertx Vertx实例
     */
    public static void safeCloseVertx(Vertx vertx) {
        safeCloseVertx(vertx, 5000);
    }
    
    /**
     * 等待指定时间
     * 
     * @param ms 等待时间（毫秒）
     */
    public static void waitFor(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.warn("Thread interrupted while waiting: {}", e.getMessage());
        }
    }
    
    /**
     * 清理测试环境
     * 重置单例实例，清理静态状态等
     */
    public static void cleanupTestEnvironment() {
        try {
            // 重置FrameworkLifecycleManager实例
            cn.qaiu.vx.core.lifecycle.FrameworkLifecycleManager.resetInstance();
            LOGGER.debug("Test environment cleaned up");
        } catch (Exception e) {
            LOGGER.warn("Failed to cleanup test environment: {}", e.getMessage());
        }
    }
}
