package cn.qaiu.vx.core.spi;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

/**
 * ORM同步提供者SPI接口
 * 由core-database模块实现，用于在框架启动时执行DDL同步
 * 
 * <p>使用SPI机制避免core模块直接依赖core-database模块
 *
 * @author <a href="https://qaiu.top">QAIU</a>
 */
public interface OrmSyncProvider {

    /**
     * 获取提供者名称
     *
     * @return 提供者名称
     */
    String getName();

    /**
     * 获取优先级（数值越小优先级越高）
     *
     * @return 优先级
     */
    default int getPriority() {
        return 100;
    }

    /**
     * 检查是否应该执行ORM同步
     * 基于主类上的注解或配置决定
     *
     * @param mainClass 应用主类
     * @param config 全局配置
     * @return 是否应该执行同步
     */
    boolean shouldSync(Class<?> mainClass, JsonObject config);

    /**
     * 执行ORM/DDL同步
     *
     * @param vertx Vertx实例
     * @param mainClass 应用主类（用于获取注解配置）
     * @param config 全局配置
     * @return 同步结果Future
     */
    Future<OrmSyncResult> sync(Vertx vertx, Class<?> mainClass, JsonObject config);

    /**
     * ORM同步结果
     */
    class OrmSyncResult {
        private final boolean success;
        private final int tablesCreated;
        private final int tablesUpdated;
        private final int tablesFailed;
        private final String message;

        public OrmSyncResult(boolean success, int tablesCreated, int tablesUpdated, int tablesFailed, String message) {
            this.success = success;
            this.tablesCreated = tablesCreated;
            this.tablesUpdated = tablesUpdated;
            this.tablesFailed = tablesFailed;
            this.message = message;
        }

        public static OrmSyncResult success(int created, int updated) {
            return new OrmSyncResult(true, created, updated, 0, 
                    String.format("DDL sync completed: %d tables created, %d tables updated", created, updated));
        }

        public static OrmSyncResult failure(String message) {
            return new OrmSyncResult(false, 0, 0, 0, message);
        }

        public static OrmSyncResult skipped(String reason) {
            return new OrmSyncResult(true, 0, 0, 0, "DDL sync skipped: " + reason);
        }

        public boolean isSuccess() { return success; }
        public int getTablesCreated() { return tablesCreated; }
        public int getTablesUpdated() { return tablesUpdated; }
        public int getTablesFailed() { return tablesFailed; }
        public String getMessage() { return message; }

        @Override
        public String toString() {
            return message;
        }
    }
}
