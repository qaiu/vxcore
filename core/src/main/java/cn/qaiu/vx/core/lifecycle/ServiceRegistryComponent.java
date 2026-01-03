package cn.qaiu.vx.core.lifecycle;

import cn.qaiu.vx.core.di.ServiceComponent;
import cn.qaiu.vx.core.di.DaggerServiceComponent;
import cn.qaiu.vx.core.registry.ServiceRegistry;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;

/**
 * 服务注册组件
 * 负责服务的扫描、注册和管理
 * 
 * @author <a href="https://qaiu.top">QAIU</a>
 */
public class ServiceRegistryComponent implements LifecycleComponent {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceRegistryComponent.class);
    
    private Vertx vertx;
    private ServiceComponent serviceComponent;
    private ServiceRegistry serviceRegistry;
    private Map<String, Set<Class<?>>> annotatedClassesMap;
    private Map<String, String> annotatedClassNamesMap;
    
    @Override
    public Future<Void> initialize(Vertx vertx, JsonObject config) {
        this.vertx = vertx;
        
        return Future.future(promise -> {
            try {
                // 1. 初始化Dagger2组件
                serviceComponent = DaggerServiceComponent.create();
                
                // 2. 获取注解类映射
                annotatedClassesMap = serviceComponent.annotatedClassesMap();
                annotatedClassNamesMap = serviceComponent.annotatedClassNamesMap();
                
                // 3. 创建服务注册表
                serviceRegistry = new ServiceRegistry(vertx);
                
                // 4. 记录扫描结果
                logAnnotatedClasses();
                
                LOGGER.info("Service registry component initialized successfully");
                promise.complete();
            } catch (Exception e) {
                LOGGER.error("Failed to initialize service registry component", e);
                promise.fail(e);
            }
        });
    }
    
    @Override
    public Future<Void> start() {
        return Future.future(promise -> {
            try {
                // 获取Service注解的类集合
                Set<Class<?>> handlers = serviceComponent.serviceClasses();
                
                // 注册所有服务
                int registeredCount = serviceRegistry.registerServices(handlers);
                LOGGER.info("Service registration completed. Total registered: {}", registeredCount);
                
                promise.complete();
            } catch (Exception e) {
                LOGGER.error("Failed to start service registry", e);
                promise.fail(e);
            }
        });
    }
    
    /**
     * 记录所有扫描到的注解类
     */
    private void logAnnotatedClasses() {
        if (annotatedClassesMap != null) {
            LOGGER.info("=== Annotated Classes Scan Results ===");
            annotatedClassesMap.forEach((annotationType, classes) -> {
                LOGGER.info("@{} classes found: {}", annotationType, classes.size());
                classes.forEach(clazz -> {
                    String effectiveName = annotatedClassNamesMap != null ? 
                        annotatedClassNamesMap.get(clazz.getName()) : 
                        clazz.getSimpleName();
                    LOGGER.info("  - {} -> {}", clazz.getSimpleName(), effectiveName);
                });
            });
            LOGGER.info("=== End of Scan Results ===");
        }
    }
    
    @Override
    public int getPriority() {
        return 30; // 第三优先级
    }
    
    /**
     * 获取服务组件
     */
    public ServiceComponent getServiceComponent() {
        return serviceComponent;
    }
    
    /**
     * 获取服务注册表
     */
    public ServiceRegistry getServiceRegistry() {
        return serviceRegistry;
    }
}