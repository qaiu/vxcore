package cn.qaiu.vx.core.registry;

import cn.qaiu.vx.core.annotaions.Service;
import cn.qaiu.vx.core.util.AnnotationNameGenerator;
import cn.qaiu.vx.core.util.ReflectionUtil;
import io.vertx.core.Vertx;
import io.vertx.serviceproxy.ServiceBinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 服务注册器 - 替代BaseAsyncService
 * 自动扫描@Service注解的类并注册到EventBus
 * <br>Create date 2025-01-27
 *
 * @author <a href="https://qaiu.top">QAIU</a>
 */
public class ServiceRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceRegistry.class);
    private static final AtomicInteger ID = new AtomicInteger(1);

    private final Vertx vertx;
    private final ServiceBinder serviceBinder;
    private final Map<String, Object> registeredServices;

    public ServiceRegistry(Vertx vertx) {
        this.vertx = vertx;
        this.serviceBinder = new ServiceBinder(vertx);
        this.registeredServices = new HashMap<>();
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
        StringBuilder serviceNames = new StringBuilder();

        for (Class<?> serviceClass : serviceClasses) {
            try {
                ServiceInfo serviceInfo = analyzeServiceClass(serviceClass);
                if (serviceInfo != null) {
                    Object serviceInstance = ReflectionUtil.newWithNoParam(serviceClass);
                    registerService(serviceInfo, serviceInstance);
                    
                    serviceNames.append(serviceInfo.getServiceName()).append("|");
                    registeredCount++;
                    
                    LOGGER.info("Registered service: {} -> {}", serviceInfo.getServiceName(), serviceInfo.getAddress());
                }
            } catch (Exception e) {
                LOGGER.error("Failed to register service: {}", serviceClass.getName(), e);
            }
        }

        if (registeredCount > 0) {
            LOGGER.info("Registered {} async services -> id: {}, names: {}", 
                registeredCount, ID.getAndIncrement(), serviceNames.toString());
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
            // 检查是否有@Service注解
            Service serviceAnnotation = serviceClass.getAnnotation(Service.class);
            if (serviceAnnotation == null) {
                LOGGER.warn("Class {} is not annotated with @Service", serviceClass.getName());
                return null;
            }

            // 获取实现的接口
            Class<?>[] interfaces = serviceClass.getInterfaces();
            if (interfaces.length == 0) {
                LOGGER.warn("Service class {} does not implement any interface", serviceClass.getName());
                return null;
            }

            // 使用第一个接口作为服务接口
            Class<?> serviceInterface = interfaces[0];
            String address = serviceInterface.getName();
            String serviceName = AnnotationNameGenerator.getEffectiveName(serviceClass);

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
     */
    @SuppressWarnings("unchecked")
    private void registerService(ServiceInfo serviceInfo, Object serviceInstance) {
        try {
            serviceBinder
                .setAddress(serviceInfo.getAddress())
                .register((Class<Object>) serviceInfo.getServiceInterface(), serviceInstance);
            
            registeredServices.put(serviceInfo.getServiceName(), serviceInstance);
        } catch (Exception e) {
            LOGGER.error("Failed to register service: {}", serviceInfo.getServiceName(), e);
            throw new RuntimeException("Service registration failed", e);
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

    /**
     * 服务信息内部类
     */
    private static class ServiceInfo {
        private final String serviceName;
        private final String address;
        private final Class<?> serviceInterface;
        private final Class<?> serviceClass;

        public ServiceInfo(String serviceName, String address, Class<?> serviceInterface, Class<?> serviceClass) {
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
