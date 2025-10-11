package cn.qaiu.vx.core.verticle;

import cn.qaiu.vx.core.di.ServiceComponent;
import cn.qaiu.vx.core.di.DaggerServiceComponent;
import cn.qaiu.vx.core.registry.ServiceRegistry;
import cn.qaiu.vx.core.util.AnnotationNameGenerator;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;

/**
 * 服务注册到EventBus
 * <br>Create date 2021-05-07 10:26:54
 *
 * @author <a href="https://qaiu.top">QAIU</a>
 */
public class ServiceVerticle extends AbstractVerticle {

    Logger LOGGER = LoggerFactory.getLogger(ServiceVerticle.class);
    private ServiceComponent serviceComponent;
    private Set<Class<?>> handlers;
    private ServiceRegistry serviceRegistry;
    private Map<String, Set<Class<?>>> annotatedClassesMap;
    private Map<String, String> annotatedClassNamesMap;
    @Override
    public void start(Promise<Void> startPromise) {
        try {
            // 初始化Dagger2组件
            serviceComponent = DaggerServiceComponent.create();
            
            // 注入依赖
            serviceComponent.inject(this);
            
            // 获取所有注解类的映射
            annotatedClassesMap = serviceComponent.annotatedClassesMap();
            
            // 获取注解类名称映射
            annotatedClassNamesMap = serviceComponent.annotatedClassNamesMap();
            
            // 获取Service注解的类集合
            handlers = serviceComponent.serviceClasses();
            
            // 创建ServiceRegistry实例
            serviceRegistry = new ServiceRegistry(vertx);
            
            // 记录所有扫描到的注解类
            logAnnotatedClasses();

            // 注册所有服务
            int registeredCount = serviceRegistry.registerServices(handlers);
            LOGGER.info("Service registration completed. Total registered: {}", registeredCount);
            startPromise.complete();
        } catch (Exception e) {
            LOGGER.error("Failed to start ServiceVerticle", e);
            startPromise.fail(e);
        }
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
                        AnnotationNameGenerator.getEffectiveName(clazz);
                    LOGGER.info("  - {} -> {}", clazz.getSimpleName(), effectiveName);
                });
            });
            LOGGER.info("=== End of Scan Results ===");
        }
    }
}
