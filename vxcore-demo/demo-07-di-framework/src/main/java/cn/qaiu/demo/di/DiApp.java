package cn.qaiu.demo.di;

import cn.qaiu.vx.core.VXCoreApplication;
import cn.qaiu.vx.core.annotations.App;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DI框架验证应用入口
 * 
 * 验证 07-di-framework.md 中描述的依赖注入能力:
 * 1. @Inject 字段注入到 @RouteHandler Controller
 * 2. @Inject 构造函数注入
 * 3. @Service 注解标记的 Service 类
 * 4. 接口 vs 具体类的注入差异
 */
@App
public class DiApp {

    private static final Logger log = LoggerFactory.getLogger(DiApp.class);

    public static void main(String[] args) {
        log.info("Starting demo-07-di-framework...");
        VXCoreApplication.run(args, DiApp::onStartup);
    }

    private static void onStartup(JsonObject config) {
        log.info("demo-07-di-framework started successfully");
        log.info("=== DI VERIFICATION MODULE ===");
        log.info("Test the following endpoints:");
        log.info("  GET /api/di/field-inject      - 验证字段注入(具体类)");
        log.info("  GET /api/di/ctor-inject        - 验证构造函数注入(具体类)");
        log.info("  GET /api/di/interface-inject   - 验证接口注入");
        log.info("  GET /api/di/verify-singleton   - 验证单例行为");
        JsonObject server = config.getJsonObject("server");
        if (server != null) {
            int port = server.getInteger("port", 8080);
            log.info("Application is running on port: {}", port);
        }
    }
}
