package cn.qaiu.demo.quickstart;

import cn.qaiu.vx.core.VXCoreApplication;
import cn.qaiu.vx.core.annotations.App;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 验证 02-quick-start.md 的启动方式
 * 文档描述了完整的三层架构: Entity -> Service -> Controller
 */
@App
public class QuickStartApp {

    private static final Logger log = LoggerFactory.getLogger(QuickStartApp.class);

    public static void main(String[] args) {
        log.info("Starting demo-02-quick-start...");
        VXCoreApplication.run(args, QuickStartApp::onStartup);
    }

    private static void onStartup(JsonObject config) {
        log.info("demo-02-quick-start started successfully");
        JsonObject server = config.getJsonObject("server");
        if (server != null) {
            int port = server.getInteger("port", 8080);
            log.info("Application is running on port: {}", port);
        }
    }
}
