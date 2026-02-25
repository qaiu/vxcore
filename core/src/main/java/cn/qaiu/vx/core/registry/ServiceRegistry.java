package cn.qaiu.vx.core.registry;

import cn.qaiu.vx.core.annotations.Service;
import cn.qaiu.vx.core.util.AnnotationNameGenerator;
import cn.qaiu.vx.core.util.ReflectionUtil;
import io.vertx.core.Vertx;
import io.vertx.serviceproxy.ServiceBinder;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 服务注册器 - 替代BaseAsyncService 自动扫描@Service注解的类并注册到EventBus <br>
 * Create date 2025-01-27
 *
 * @author <a href="https://qaiu.top">QAIU</a>
 */
public class ServiceRegistry {

  private static final Logger LOGGER = LoggerFactory.getLogger(ServiceRegistry.class);
  private static final AtomicInteger ID = new AtomicInteger(1);

  private final Vertx vertx;
  private final ServiceBinder serviceBinder;
  private final Map<String, Object> registeredServices;
  private final Map<Class<?>, Object> servicesByType;

  public ServiceRegistry(Vertx vertx) {
    this.vertx = vertx;
    this.serviceBinder = new ServiceBinder(vertx);
    this.registeredServices = new HashMap<>();
    this.servicesByType = new HashMap<>();
  }

  /**
   * 注册所有@Service注解的服务
   *
   * @param serviceClasses 服务类集合
   * @return 注册的服务数量
   */
  public int registerServices(Set<Class<?>> serviceClasses) {
    if (serviceClasses == null || serviceClasses.isEmpty()) {
      LOGGER.info("No service classes found to register");
      return 0;
    }

    int registeredCount = 0;
    int skippedCount = 0;
    int errorCount = 0;
    StringBuilder serviceNames = new StringBuilder();

    LOGGER.info("Starting service registration for {} classes", serviceClasses.size());

    for (Class<?> serviceClass : serviceClasses) {
      try {
        ServiceInfo serviceInfo = analyzeServiceClass(serviceClass);
        if (serviceInfo != null) {
          Object serviceInstance = ReflectionUtil.newWithNoParam(serviceClass);
          boolean success = registerService(serviceInfo, serviceInstance);

          if (success) {
            serviceNames.append(serviceInfo.getServiceName()).append("|");
            registeredCount++;
            LOGGER.debug(
                "Successfully registered service: {} -> {}",
                serviceInfo.getServiceName(),
                serviceInfo.getAddress());
          } else {
            skippedCount++;
            LOGGER.debug("Skipped service registration: {}", serviceInfo.getServiceName());
          }
        } else {
          skippedCount++;
          LOGGER.debug("Skipped service analysis: {}", serviceClass.getName());
        }
      } catch (Exception e) {
        errorCount++;
        LOGGER.error("Failed to register service: {}", serviceClass.getName(), e);
      }
    }

    LOGGER.info(
        "Service registration completed: {} registered, {} skipped, {} errors",
        registeredCount,
        skippedCount,
        errorCount);

    if (registeredCount > 0) {
      LOGGER.info(
          "Registered {} async services -> id: {}, names: {}",
          registeredCount,
          ID.getAndIncrement(),
          serviceNames.toString());
    }

    return registeredCount;
  }

  /**
   * 分析服务类，提取服务信息
   *
   * @param serviceClass 服务类
   * @return 服务信息
   */
  private ServiceInfo analyzeServiceClass(Class<?> serviceClass) {
    try {
      Service serviceAnnotation = serviceClass.getAnnotation(Service.class);
      if (serviceAnnotation == null) {
        LOGGER.warn("Class {} is not annotated with @Service", serviceClass.getName());
        return null;
      }

      String serviceName = AnnotationNameGenerator.getEffectiveName(serviceClass);

      Class<?>[] interfaces = serviceClass.getInterfaces();
      if (interfaces.length == 0) {
        // 无接口的具体类也允许注册到本地注册表
        return new ServiceInfo(serviceName, serviceClass.getName(), null, serviceClass);
      }

      Class<?> serviceInterface = interfaces[0];
      String address = serviceInterface.getName();
      return new ServiceInfo(serviceName, address, serviceInterface, serviceClass);
    } catch (Exception e) {
      LOGGER.error("Failed to analyze service class: {}", serviceClass.getName(), e);
      return null;
    }
  }

  /**
   * 注册单个服务
   *
   * @param serviceInfo 服务信息
   * @param serviceInstance 服务实例
   * @return 是否成功注册
   */
  @SuppressWarnings("unchecked")
  private boolean registerService(ServiceInfo serviceInfo, Object serviceInstance) {
    try {
      Class<?> serviceInterface = serviceInfo.getServiceInterface();

      if (serviceInterface != null) {
        boolean hasProxyGen = hasProxyGenAnnotation(serviceInterface);
        if (hasProxyGen) {
          String proxyHandlerClassName = serviceInterface.getName() + "VertxProxyHandler";
          try {
            Class.forName(proxyHandlerClassName);
            serviceBinder
                .setAddress(serviceInfo.getAddress())
                .register((Class<Object>) serviceInterface, serviceInstance);
            LOGGER.info(
                "Successfully registered @ProxyGen service: {} -> {}",
                serviceInfo.getServiceName(),
                serviceInfo.getAddress());
          } catch (ClassNotFoundException e) {
            LOGGER.warn(
                "Proxy handler class not found for @ProxyGen service {}: {}. "
                    + "Skipping ServiceBinder registration.",
                serviceInfo.getServiceName(),
                proxyHandlerClassName);
          } catch (Exception proxyError) {
            LOGGER.warn(
                "Failed to register @ProxyGen service {} with ServiceBinder: {}. "
                    + "Skipping proxy registration.",
                serviceInfo.getServiceName(),
                proxyError.getMessage());
          }
        } else {
          LOGGER.info(
              "Service {} registering to local registry (no @ProxyGen)",
              serviceInfo.getServiceName());
        }
        // 建立接口到实例的类型索引
        servicesByType.put(serviceInterface, serviceInstance);
      } else {
        LOGGER.info(
            "Concrete service {} registering to local registry (no interface)",
            serviceInfo.getServiceName());
      }

      // 注册到本地映射（按名称）
      registeredServices.put(serviceInfo.getServiceName(), serviceInstance);
      // 建立具体类到实例的类型索引
      servicesByType.put(serviceInfo.getServiceClass(), serviceInstance);
      return true;
    } catch (Exception e) {
      LOGGER.error("Failed to register service: {}", serviceInfo.getServiceName(), e);
      LOGGER.warn("Skipping service registration for: {}", serviceInfo.getServiceName());
      return false;
    }
  }

  /**
   * 获取已注册的服务实例
   *
   * @param serviceName 服务名称
   * @return 服务实例
   */
  public Object getService(String serviceName) {
    return registeredServices.get(serviceName);
  }

  /**
   * 按类型查找已注册的服务实例。 先精确匹配，再按 assignable 兼容查找。
   *
   * @param serviceType 服务类型（接口或具体类）
   * @return 服务实例，未找到返回 null
   */
  public Object getServiceByType(Class<?> serviceType) {
    // 1. 精确匹配
    Object instance = servicesByType.get(serviceType);
    if (instance != null) {
      return instance;
    }
    // 2. assignable 兼容查找
    for (Map.Entry<Class<?>, Object> entry : servicesByType.entrySet()) {
      if (serviceType.isAssignableFrom(entry.getKey())) {
        return entry.getValue();
      }
    }
    return null;
  }

  /**
   * 获取所有已注册的服务
   *
   * @return 服务映射
   */
  public Map<String, Object> getAllServices() {
    return new HashMap<>(registeredServices);
  }

  /**
   * 获取已注册服务的数量
   *
   * @return 服务数量
   */
  public int getServiceCount() {
    return registeredServices.size();
  }

  /** 安全地检查接口是否有@ProxyGen注解 如果注解类不存在（如运行时缺少vertx-codegen依赖），则返回false */
  private boolean hasProxyGenAnnotation(Class<?> serviceInterface) {
    try {
      Class<?> proxyGenClass = Class.forName("io.vertx.codegen.annotations.ProxyGen");
      return serviceInterface.isAnnotationPresent(
          (Class<java.lang.annotation.Annotation>) proxyGenClass);
    } catch (ClassNotFoundException e) {
      LOGGER.debug("ProxyGen annotation class not found in classpath, skipping proxy check");
      return false;
    } catch (Exception e) {
      LOGGER.debug("Failed to check ProxyGen annotation: {}", e.getMessage());
      return false;
    }
  }

  /** 服务信息内部类 */
  private static class ServiceInfo {
    private final String serviceName;
    private final String address;
    private final Class<?> serviceInterface;
    private final Class<?> serviceClass;

    public ServiceInfo(
        String serviceName, String address, Class<?> serviceInterface, Class<?> serviceClass) {
      this.serviceName = serviceName;
      this.address = address;
      this.serviceInterface = serviceInterface;
      this.serviceClass = serviceClass;
    }

    public String getServiceName() {
      return serviceName;
    }

    public String getAddress() {
      return address;
    }

    public Class<?> getServiceInterface() {
      return serviceInterface;
    }

    public Class<?> getServiceClass() {
      return serviceClass;
    }
  }
}
