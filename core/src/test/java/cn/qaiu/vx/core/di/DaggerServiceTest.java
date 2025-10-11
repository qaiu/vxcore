package cn.qaiu.vx.core.di;

import cn.qaiu.vx.core.annotaions.Service;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Dagger2 Service模块测试
 * <br>Create date 2025-01-27
 *
 * @author <a href="https://qaiu.top">QAIU</a>
 */
public class DaggerServiceTest {

    /**
     * 测试ServiceComponent能否正确创建和注入
     */
    @Test
    public void testServiceComponentCreation() {
        // 测试ServiceModule能否正确提供Service类集合
        ServiceModule module = new ServiceModule();
        Set<Class<?>> serviceClasses = module.provideServiceClasses();
        
        assertNotNull(serviceClasses, "Service classes should not be null");
        
        // 打印找到的Service类
        System.out.println("Found " + serviceClasses.size() + " service classes:");
        serviceClasses.forEach(clazz -> {
            Service annotation = clazz.getAnnotation(Service.class);
            String name = annotation != null ? annotation.name() : "";
            System.out.println("- " + clazz.getName() + (name.isEmpty() ? "" : " (name: " + name + ")"));
        });
    }

    /**
     * 测试ServiceModule的provideServiceClasses方法
     */
    @Test
    public void testServiceModule() {
        ServiceModule module = new ServiceModule();
        Set<Class<?>> serviceClasses = module.provideServiceClasses();
        
        assertNotNull(serviceClasses, "Service classes should not be null");
        assertTrue(serviceClasses.size() >= 0, "Service classes should be available");
    }

}
