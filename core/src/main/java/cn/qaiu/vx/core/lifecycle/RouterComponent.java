package cn.qaiu.vx.core.lifecycle;

import cn.qaiu.vx.core.handlerfactory.RouterHandlerFactory;
import cn.qaiu.vx.core.registry.ServiceRegistry;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 路由管理组件 负责路由的创建、配置和管理
 *
 * @author <a href="https://qaiu.top">QAIU</a>
 */
public class RouterComponent implements LifecycleComponent {

  private static final Logger LOGGER = LoggerFactory.getLogger(RouterComponent.class);

  private RouterHandlerFactory routerHandlerFactory;
  private Router router;

  @Override
  public Future<Void> initialize(Vertx vertx, JsonObject config) {

    return Future.future(
        promise -> {
          try {
            // 1. 获取网关前缀
            String gatewayPrefix = getGatewayPrefix(config);

            // 2. 创建路由处理器工厂
            routerHandlerFactory = new RouterHandlerFactory(gatewayPrefix);

            // 3. 注入 ServiceRegistry 供 Controller DI 使用
            ServiceRegistry registry = findServiceRegistry();
            if (registry != null) {
              routerHandlerFactory.setServiceRegistry(registry);
              LOGGER.info("ServiceRegistry injected into RouterHandlerFactory");
            }

            // 4. 创建路由器
            router = routerHandlerFactory.createRouter();

            LOGGER.info("Router component initialized successfully with prefix: {}", gatewayPrefix);
            promise.complete();
          } catch (Exception e) {
            LOGGER.error("Failed to initialize router component", e);
            promise.fail(e);
          }
        });
  }

  /** 从 FrameworkLifecycleManager 查找已初始化的 ServiceRegistry */
  private ServiceRegistry findServiceRegistry() {
    try {
      for (LifecycleComponent component : FrameworkLifecycleManager.getInstance().getComponents()) {
        if (component instanceof ServiceRegistryComponent src) {
          return src.getServiceRegistry();
        }
      }
    } catch (Exception e) {
      LOGGER.warn("Failed to find ServiceRegistry: {}", e.getMessage());
    }
    return null;
  }

  /** 获取网关前缀 */
  private String getGatewayPrefix(JsonObject config) {
    JsonObject customConfig = config.getJsonObject("custom");
    if (customConfig != null && customConfig.containsKey("gatewayPrefix")) {
      return customConfig.getString("gatewayPrefix");
    }
    return "api"; // 默认前缀
  }

  @Override
  public int getPriority() {
    return 40; // 第四优先级
  }

  /** 获取路由器 */
  public Router getRouter() {
    return router;
  }

  /** 获取路由处理器工厂 */
  public RouterHandlerFactory getRouterHandlerFactory() {
    return routerHandlerFactory;
  }
}
