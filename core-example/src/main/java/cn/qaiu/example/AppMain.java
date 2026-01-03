package cn.qaiu.example;

import cn.qaiu.vx.core.Deploy;
import cn.qaiu.vx.core.annotaions.App;
import cn.qaiu.vx.core.util.VertxHolder;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 程序入口 <br>
 * Create date 2025-10-10
 *
 * @author qaiu
 */
@App
public class AppMain {

  private static final Logger log = LoggerFactory.getLogger(AppMain.class);

  public static void main(String[] args) {
    Deploy.run(args, AppMain::exec);
  }

  /**
   * 框架回调方法 初始化数据库/缓存等
   *
   * @param jsonObject 配置
   */
  private static void exec(JsonObject jsonObject) {
    log.info("VXCore Example Application Starting...");
    log.info("Configuration loaded: {}", jsonObject.encodePrettily());

    // 启动示例应用
    ExampleApplication app = new ExampleApplication();
    VertxHolder.getVertxInstance()
        .deployVerticle(app)
        .onSuccess(
            deploymentId -> {
              log.info("✅ VXCore Example Application started successfully");
              log.info(
                  "📱 Application is running on port: {}",
                  jsonObject.getJsonObject("server").getInteger("port", 6400));
            })
        .onFailure(
            throwable -> {
              log.error("❌ Failed to start VXCore Example Application", throwable);
              System.exit(-1);
            });
  }
}
