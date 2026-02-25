package cn.qaiu.vx.core.verticle;

import static cn.qaiu.vx.core.util.ConfigConstant.*;

import cn.qaiu.vx.core.handlerfactory.RouterHandlerFactory;
import cn.qaiu.vx.core.lifecycle.FrameworkLifecycleManager;
import cn.qaiu.vx.core.lifecycle.RouterComponent;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.LocalMap;
import io.vertx.ext.web.Router;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 路由Verticle 使用组合模式，依赖RouterComponent进行路由管理 同时兼容旧的Deploy启动方式
 *
 * @author <a href="https://qaiu.top">QAIU</a>
 */
public class RouterVerticle extends AbstractVerticle {

  private static final Logger LOGGER = LoggerFactory.getLogger(RouterVerticle.class);

  private RouterComponent routerComponent;
  private HttpServer httpServer;

  @Override
  public void start(Promise<Void> startPromise) {
    try {
      // 尝试获取配置 - 优先从FrameworkLifecycleManager获取，否则从sharedData获取（兼容旧Deploy方式）
      JsonObject globalConfig = getGlobalConfig();
      if (globalConfig == null) {
        throw new IllegalStateException("Global configuration not found");
      }

      // 获取路由器 - 优先从RouterComponent获取，否则新建（兼容旧Deploy方式）
      Router router = getOrCreateRouter(globalConfig);

      // 获取服务器配置
      JsonObject serverConfig = globalConfig.getJsonObject("server");
      if (serverConfig == null) {
        serverConfig = globalConfig.getJsonObject(SERVER);
      }
      if (serverConfig == null) {
        throw new IllegalStateException("Server configuration not found");
      }
      int port = serverConfig.getInteger("port", 8080);
      String host = serverConfig.getString("host", "0.0.0.0");

      // 创建HTTP服务器
      httpServer = vertx.createHttpServer();

      // 启动服务器
      httpServer
          .requestHandler(router)
          .listen(port, host)
          .onSuccess(
              server -> {
                LOGGER.info("HTTP server started on {}:{}", host, port);
                startPromise.complete();
              })
          .onFailure(
              error -> {
                LOGGER.error("Failed to start HTTP server", error);
                startPromise.fail(error);
              });

    } catch (Exception e) {
      LOGGER.error("Failed to start RouterVerticle", e);
      startPromise.fail(e);
    }
  }

  @Override
  public void stop(Promise<Void> stopPromise) {
    try {
      if (httpServer != null) {
        httpServer
            .close()
            .onSuccess(
                v -> {
                  LOGGER.info("HTTP server stopped successfully");
                  stopPromise.complete();
                })
            .onFailure(
                error -> {
                  LOGGER.error("Failed to stop HTTP server", error);
                  stopPromise.fail(error);
                });
      } else {
        stopPromise.complete();
      }
    } catch (Exception e) {
      LOGGER.error("Failed to stop RouterVerticle", e);
      stopPromise.fail(e);
    }
  }

  /** 获取全局配置 优先从FrameworkLifecycleManager获取，否则从sharedData获取（兼容旧Deploy方式） */
  private JsonObject getGlobalConfig() {
    // 尝试从FrameworkLifecycleManager获取
    FrameworkLifecycleManager lifecycleManager = FrameworkLifecycleManager.getInstance();
    JsonObject config = lifecycleManager.getGlobalConfig();
    if (config != null) {
      LOGGER.debug("Using configuration from FrameworkLifecycleManager");
      return config;
    }

    // 兼容旧的Deploy方式：从sharedData获取
    LocalMap<String, Object> localMap = vertx.sharedData().getLocalMap(LOCAL);
    if (localMap != null) {
      config = (JsonObject) localMap.get(GLOBAL_CONFIG);
      if (config != null) {
        LOGGER.debug("Using configuration from sharedData (legacy Deploy mode)");
        return config;
      }
    }

    LOGGER.warn("No configuration found from either FrameworkLifecycleManager or sharedData");
    return null;
  }

  /** 获取或创建路由器 优先从RouterComponent获取，否则新建（兼容旧Deploy方式） */
  private Router getOrCreateRouter(JsonObject globalConfig) {
    // 尝试从FrameworkLifecycleManager获取RouterComponent
    FrameworkLifecycleManager lifecycleManager = FrameworkLifecycleManager.getInstance();

    try {
      routerComponent =
          (RouterComponent)
              lifecycleManager.getComponents().stream()
                  .filter(component -> component instanceof RouterComponent)
                  .findFirst()
                  .orElse(null);

      if (routerComponent != null && routerComponent.getRouter() != null) {
        LOGGER.debug("Using Router from RouterComponent");
        return routerComponent.getRouter();
      }
    } catch (Exception e) {
      LOGGER.debug("RouterComponent not available, creating new router");
    }

    // 兼容旧的Deploy方式：创建新的路由器
    String gatewayPrefix = getGatewayPrefix(globalConfig);
    RouterHandlerFactory routerHandlerFactory = new RouterHandlerFactory(gatewayPrefix);
    Router router = routerHandlerFactory.createRouter();
    LOGGER.debug("Created new Router with gateway prefix: {}", gatewayPrefix);
    return router;
  }

  /** 获取网关前缀 */
  private String getGatewayPrefix(JsonObject config) {
    // 新配置格式
    JsonObject customConfig = config.getJsonObject("custom");
    if (customConfig != null && customConfig.containsKey("gatewayPrefix")) {
      return customConfig.getString("gatewayPrefix");
    }
    // 旧配置格式
    customConfig = config.getJsonObject(CUSTOM);
    if (customConfig != null && customConfig.containsKey("gatewayPrefix")) {
      return customConfig.getString("gatewayPrefix");
    }
    return "api"; // 默认前缀
  }
}
