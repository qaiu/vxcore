package cn.qaiu.vx.core.verticle;

import cn.qaiu.vx.core.di.DaggerServiceComponent;
import cn.qaiu.vx.core.di.ServiceComponent;
import cn.qaiu.vx.core.lifecycle.FrameworkLifecycleManager;
import cn.qaiu.vx.core.lifecycle.ServiceRegistryComponent;
import cn.qaiu.vx.core.registry.ServiceRegistry;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 服务注册Verticle 使用组合模式，依赖ServiceRegistryComponent进行服务管理 同时兼容旧的Deploy启动方式
 *
 * @author <a href="https://qaiu.top">QAIU</a>
 */
public class ServiceVerticle extends AbstractVerticle {

  private static final Logger LOGGER = LoggerFactory.getLogger(ServiceVerticle.class);

  private ServiceRegistryComponent serviceRegistryComponent;

  @Override
  public void start(Promise<Void> startPromise) {
    try {
      // 尝试获取ServiceRegistryComponent - 兼容新旧两种启动方式
      serviceRegistryComponent = getOrCreateServiceRegistryComponent();

      if (serviceRegistryComponent != null) {
        // 使用新方式启动
        serviceRegistryComponent
            .start()
            .onSuccess(
                v -> {
                  LOGGER.info("ServiceVerticle started successfully (new mode)");
                  startPromise.complete();
                })
            .onFailure(
                error -> {
                  LOGGER.error("Failed to start ServiceVerticle", error);
                  startPromise.fail(error);
                });
      } else {
        // 兼容旧的Deploy方式：直接初始化并注册服务
        startWithLegacyMode(startPromise);
      }

    } catch (Exception e) {
      LOGGER.error("Failed to start ServiceVerticle", e);
      startPromise.fail(e);
    }
  }

  /** 使用旧模式启动（兼容Deploy启动方式） */
  private void startWithLegacyMode(Promise<Void> startPromise) {
    try {
      LOGGER.info("Starting ServiceVerticle in legacy mode...");

      // 初始化Dagger2组件
      ServiceComponent serviceComponent = DaggerServiceComponent.create();

      // 创建服务注册表
      ServiceRegistry serviceRegistry = new ServiceRegistry(vertx);

      // 获取Service注解的类集合
      Set<Class<?>> handlers = serviceComponent.serviceClasses();

      // 注册所有服务
      int registeredCount = serviceRegistry.registerServices(handlers);
      LOGGER.info(
          "Service registration completed (legacy mode). Total registered: {}", registeredCount);

      startPromise.complete();
    } catch (Exception e) {
      LOGGER.error("Failed to start service registry in legacy mode", e);
      startPromise.fail(e);
    }
  }

  /** 获取或创建ServiceRegistryComponent 优先从FrameworkLifecycleManager获取（如果已经初始化），否则返回null（表示使用旧模式） */
  private ServiceRegistryComponent getOrCreateServiceRegistryComponent() {
    try {
      FrameworkLifecycleManager lifecycleManager = FrameworkLifecycleManager.getInstance();

      // 检查FrameworkLifecycleManager是否已经初始化（通过检查globalConfig和状态）
      JsonObject globalConfig = lifecycleManager.getGlobalConfig();
      if (globalConfig == null) {
        LOGGER.info(
            "FrameworkLifecycleManager not initialized (globalConfig is null), using legacy mode");
        return null;
      }

      // 检查状态是否已启动
      FrameworkLifecycleManager.LifecycleState state = lifecycleManager.getState();
      if (state != FrameworkLifecycleManager.LifecycleState.STARTED
          && state != FrameworkLifecycleManager.LifecycleState.STARTING) {
        LOGGER.info("FrameworkLifecycleManager state is {}, using legacy mode", state);
        return null;
      }

      ServiceRegistryComponent component =
          (ServiceRegistryComponent)
              lifecycleManager.getComponents().stream()
                  .filter(c -> c instanceof ServiceRegistryComponent)
                  .findFirst()
                  .orElse(null);

      // 检查组件是否已经完全初始化
      if (component != null && component.getServiceComponent() != null) {
        LOGGER.info("Using ServiceRegistryComponent from FrameworkLifecycleManager (new mode)");
        return component;
      }

      LOGGER.info("ServiceRegistryComponent not fully initialized, using legacy mode");
      return null;
    } catch (Exception e) {
      LOGGER.info("Failed to get ServiceRegistryComponent, using legacy mode: {}", e.getMessage());
      return null;
    }
  }

  @Override
  public void stop(Promise<Void> stopPromise) {
    try {
      if (serviceRegistryComponent != null) {
        serviceRegistryComponent
            .stop()
            .onSuccess(
                v -> {
                  LOGGER.info("ServiceVerticle stopped successfully");
                  stopPromise.complete();
                })
            .onFailure(
                error -> {
                  LOGGER.error("Failed to stop ServiceVerticle", error);
                  stopPromise.fail(error);
                });
      } else {
        stopPromise.complete();
      }
    } catch (Exception e) {
      LOGGER.error("Failed to stop ServiceVerticle", e);
      stopPromise.fail(e);
    }
  }
}
