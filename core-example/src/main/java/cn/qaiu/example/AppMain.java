package cn.qaiu.example;

import cn.qaiu.vx.core.VXCoreApplication;
import cn.qaiu.vx.core.annotations.App;
import cn.qaiu.vx.core.util.VertxHolder;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * VXCore 示例应用入口 <br>
 * 使用新的组合模式启动框架
 * 
 * Create date 2025-10-10
 *
 * @author qaiu
 */
@App
public class AppMain {

  private static final Logger log = LoggerFactory.getLogger(AppMain.class);

  public static void main(String[] args) {
    log.info("Starting VXCore Example Application...");
    
    // 使用新风格的VXCoreApplication启动
    VXCoreApplication.run(args, AppMain::onStartup);
  }

  /**
   * 框架启动回调
   * 在这里可以执行应用特定的初始化逻辑
   *
   * @param config 全局配置
   */
  private static void onStartup(JsonObject config) {
    log.info("✅ VXCore Example Application started successfully");
    
    // 打印服务器信息
    JsonObject server = config.getJsonObject("server");
    if (server != null) {
      int port = server.getInteger("port", 8080);
      log.info("📱 Application is running on port: {}", port);
    }
    
    // 打印数据源信息
    JsonObject datasources = config.getJsonObject("datasources");
    if (datasources != null && !datasources.isEmpty()) {
      log.info("📦 Configured datasources: {}", datasources.fieldNames());
    }
    
    // 打印配置概要
    log.info("Configuration summary:");
    log.info("  - Server port: {}", server != null ? server.getInteger("port") : "default");
    log.info("  - Gateway prefix: {}", config.getJsonObject("custom", new JsonObject()).getString("gatewayPrefix", "/"));

    // 部署应用主Verticle
//    ExampleApplication verticle = new ExampleApplication();
//    VertxHolder.getVertxInstance().deployVerticle(verticle);
  }
}
