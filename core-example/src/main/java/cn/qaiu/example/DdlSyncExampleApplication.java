package cn.qaiu.example;

import cn.qaiu.db.orm.DdlSyncStrategy;
import cn.qaiu.db.orm.EnableDdlSync;
import cn.qaiu.vx.core.VXCoreApplication;
import cn.qaiu.vx.core.annotaions.App;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 带自动DDL同步的示例应用
 * 演示如何使用 @EnableDdlSync 注解启用自动ORM功能
 *
 * <p>配置说明：
 * <ul>
 *   <li>strategy: DDL同步策略
 *     <ul>
 *       <li>AUTO - 自动检测并同步表结构（默认）</li>
 *       <li>CREATE - 只创建表，不更新</li>
 *       <li>UPDATE - 只更新表，不创建</li>
 *       <li>VALIDATE - 只验证不执行</li>
 *       <li>NONE - 禁用DDL同步</li>
 *     </ul>
 *   </li>
 *   <li>entityPackages - 实体类包路径</li>
 *   <li>showDdl - 是否在日志中显示DDL语句</li>
 *   <li>autoExecute - 是否自动执行DDL</li>
 * </ul>
 *
 * @author <a href="https://qaiu.top">QAIU</a>
 */
@App
@EnableDdlSync(
        strategy = DdlSyncStrategy.AUTO,
        entityPackages = {"cn.qaiu.example.entity"},
        showDdl = true,
        autoExecute = true
)
public class DdlSyncExampleApplication {

    private static final Logger log = LoggerFactory.getLogger(DdlSyncExampleApplication.class);

    public static void main(String[] args) {
        log.info("Starting DDL Sync Example Application...");

        // 使用VXCoreApplication启动
        VXCoreApplication.run(args, DdlSyncExampleApplication::onStartup);
    }

    /**
     * 框架启动回调
     * DDL同步会在数据源初始化后自动执行
     *
     * @param config 全局配置
     */
    private static void onStartup(JsonObject config) {
        log.info("✅ DDL Sync Example Application started successfully");

        // 打印服务器信息
        JsonObject server = config.getJsonObject("server");
        if (server != null) {
            int port = server.getInteger("port", 8080);
            log.info("📱 Application is running on port: {}", port);
        }

        // 打印数据源信息
        JsonObject datasources = config.getJsonObject("datasources");
        if (datasources == null) {
            datasources = config.getJsonObject("database");
        }
        if (datasources != null && !datasources.isEmpty()) {
            log.info("📦 Configured datasources: {}", datasources.fieldNames());
            log.info("🔄 DDL synchronization is enabled with AUTO strategy");
        } else {
            log.warn("⚠️ No datasource configured, DDL sync will be skipped");
        }

        log.info("Configuration summary:");
        log.info("  - Server port: {}", server != null ? server.getInteger("port") : "default");
        log.info("  - Gateway prefix: {}", config.getJsonObject("custom", new JsonObject()).getString("gatewayPrefix", "/"));
        log.info("  - DDL Strategy: AUTO");
        log.info("  - Entity packages: cn.qaiu.example.entity");
    }
}
