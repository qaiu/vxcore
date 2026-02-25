package cn.qaiu.demo.overview;

import cn.qaiu.vx.core.VXCoreApplication;
import cn.qaiu.vx.core.annotations.App;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 验证 01-overview.md 中的启动方式
 * 文档描述: 使用 @App + VXCoreApplication.run() 启动应用
 */
@App
public class OverviewApp {

    private static final Logger log = LoggerFactory.getLogger(OverviewApp.class);

    public static void main(String[] args) {
        log.info("Starting demo-01-overview...");
        VXCoreApplication.run(args, OverviewApp::onStartup);
    }

    private static void onStartup(JsonObject config) {
        log.info("demo-01-overview started successfully");
        JsonObject server = config.getJsonObject("server");
        if (server != null) {
            int port = server.getInteger("port", 8080);
            log.info("Application is running on port: {}", port);
        }
    }
}
