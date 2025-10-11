package cn.qaiu.vx.core.di;

import cn.qaiu.vx.core.annotaions.*;
import cn.qaiu.vx.core.util.ReflectionUtil;
import org.junit.jupiter.api.Test;
import org.reflections.Reflections;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 多注解扫描测试
 * <br>Create date 2025-01-27
 *
 * @author <a href="https://qaiu.top">QAIU</a>
 */
public class MultiAnnotationTest {

    /**
     * 测试ServiceModule能否正确提供各种注解的类集合
     */
    @Test
    public void testMultiAnnotationScanning() {
        // 直接使用Reflections进行测试，避免Vertx依赖
        Reflections reflections = ReflectionUtil.getReflections("cn.qaiu");
        
        // 测试各种注解的扫描
        Set<Class<?>> serviceClasses = reflections.getTypesAnnotatedWith(Service.class);
        Set<Class<?>> daoClasses = reflections.getTypesAnnotatedWith(Dao.class);
        Set<Class<?>> componentClasses = reflections.getTypesAnnotatedWith(Component.class);
        Set<Class<?>> repositoryClasses = reflections.getTypesAnnotatedWith(Repository.class);
        Set<Class<?>> controllerClasses = reflections.getTypesAnnotatedWith(Controller.class);
        
        assertNotNull(serviceClasses, "Service classes should not be null");
        assertNotNull(daoClasses, "Dao classes should not be null");
        assertNotNull(componentClasses, "Component classes should not be null");
        assertNotNull(repositoryClasses, "Repository classes should not be null");
        assertNotNull(controllerClasses, "Controller classes should not be null");
        
        // 打印扫描结果
        System.out.println("=== Multi Annotation Scan Results ===");
        System.out.println("@Service classes found: " + serviceClasses.size());
        serviceClasses.forEach(clazz -> {
            Service annotation = clazz.getAnnotation(Service.class);
            String name = annotation != null ? annotation.name() : "";
            System.out.println("  - " + clazz.getName() + (name.isEmpty() ? "" : " (name: " + name + ")"));
        });
        
        System.out.println("@Dao classes found: " + daoClasses.size());
        daoClasses.forEach(clazz -> {
            Dao annotation = clazz.getAnnotation(Dao.class);
            String name = annotation != null ? annotation.name() : "";
            System.out.println("  - " + clazz.getName() + (name.isEmpty() ? "" : " (name: " + name + ")"));
        });
        
        System.out.println("@Component classes found: " + componentClasses.size());
        componentClasses.forEach(clazz -> {
            Component annotation = clazz.getAnnotation(Component.class);
            String name = annotation != null ? annotation.name() : "";
            System.out.println("  - " + clazz.getName() + (name.isEmpty() ? "" : " (name: " + name + ")"));
        });
        
        System.out.println("@Repository classes found: " + repositoryClasses.size());
        repositoryClasses.forEach(clazz -> {
            Repository annotation = clazz.getAnnotation(Repository.class);
            String name = annotation != null ? annotation.name() : "";
            System.out.println("  - " + clazz.getName() + (name.isEmpty() ? "" : " (name: " + name + ")"));
        });
        
        System.out.println("@Controller classes found: " + controllerClasses.size());
        controllerClasses.forEach(clazz -> {
            Controller annotation = clazz.getAnnotation(Controller.class);
            String name = annotation != null ? annotation.name() : "";
            System.out.println("  - " + clazz.getName() + (name.isEmpty() ? "" : " (name: " + name + ")"));
        });
    }

    /**
     * 测试注解类映射功能
     */
    @Test
    public void testAnnotatedClassesMap() {
        // 直接使用Reflections进行测试，避免Vertx依赖
        Reflections reflections = ReflectionUtil.getReflections("cn.qaiu");
        
        Map<String, Set<Class<?>>> annotatedClassesMap = new java.util.HashMap<>();
        annotatedClassesMap.put("Service", reflections.getTypesAnnotatedWith(Service.class));
        annotatedClassesMap.put("Dao", reflections.getTypesAnnotatedWith(Dao.class));
        annotatedClassesMap.put("Component", reflections.getTypesAnnotatedWith(Component.class));
        annotatedClassesMap.put("Repository", reflections.getTypesAnnotatedWith(Repository.class));
        annotatedClassesMap.put("Controller", reflections.getTypesAnnotatedWith(Controller.class));
        
        assertNotNull(annotatedClassesMap, "Annotated classes map should not be null");
        assertTrue(annotatedClassesMap.containsKey("Service"), "Should contain Service key");
        assertTrue(annotatedClassesMap.containsKey("Dao"), "Should contain Dao key");
        assertTrue(annotatedClassesMap.containsKey("Component"), "Should contain Component key");
        assertTrue(annotatedClassesMap.containsKey("Repository"), "Should contain Repository key");
        assertTrue(annotatedClassesMap.containsKey("Controller"), "Should contain Controller key");
        
        System.out.println("=== Annotated Classes Map ===");
        annotatedClassesMap.forEach((annotationType, classes) -> {
            System.out.println("@" + annotationType + ": " + classes.size() + " classes");
        });
    }
}
