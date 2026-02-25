package cn.qaiu.demo.database;

import cn.qaiu.db.orm.DdlSyncStrategy;
import cn.qaiu.db.orm.EnableDdlSync;
import cn.qaiu.vx.core.VXCoreApplication;
import cn.qaiu.vx.core.annotations.App;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@App
@EnableDdlSync(strategy = DdlSyncStrategy.NONE)
public class DatabaseApp {

    private static final Logger log = LoggerFactory.getLogger(DatabaseApp.class);

    public static void main(String[] args) {
        log.info("Starting demo-13-database...");
        VXCoreApplication.run(args, DatabaseApp::onStartup);
    }

    private static void onStartup(JsonObject config) {
        log.info("demo-13-database started successfully");
        log.info("=== DATABASE VERIFICATION MODULE ===");
        log.info("Test endpoints:");
        log.info("  POST   /api/users          - Create user");
        log.info("  GET    /api/users           - List all users");
        log.info("  GET    /api/users/:id       - Get user by ID");
        log.info("  PUT    /api/users/:id       - Update user");
        log.info("  DELETE /api/users/:id       - Delete user");
        log.info("  GET    /api/users/search?name=xxx  - Search by name");
        log.info("  GET    /api/users/search?status=xx - Filter by status");
        JsonObject server = config.getJsonObject("server");
        if (server != null) {
            log.info("Running on port: {}", server.getInteger("port", 8080));
        }
    }
}
