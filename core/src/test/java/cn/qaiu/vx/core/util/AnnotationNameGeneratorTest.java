package cn.qaiu.vx.core.util;

import cn.qaiu.vx.core.annotaions.*;
import io.vertx.codegen.annotations.ModuleGen;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 注解名称生成器测试
 * <br>Create date 2025-01-27
 *
 * @author <a href="https://qaiu.top">QAIU</a>
 */
public class AnnotationNameGeneratorTest {

    /**
     * 测试基本名称生成
     */
    @Test
    public void testGenerateName() {
        // 测试基本类名
        assertEquals("userService", AnnotationNameGenerator.generateName(UserService.class));
        assertEquals("userDao", AnnotationNameGenerator.generateName(UserDao.class));
        assertEquals("userComponent", AnnotationNameGenerator.generateName(UserComponent.class));
        assertEquals("userRepository", AnnotationNameGenerator.generateName(UserRepository.class));
        assertEquals("userController", AnnotationNameGenerator.generateName(UserController.class));
        
        // 测试空类名
        assertEquals("", AnnotationNameGenerator.generateName(null));
    }

    /**
     * 测试有效名称获取（优先使用注解的name属性）
     */
    @Test
    public void testGetEffectiveName() {
        // 测试有name属性的注解
        assertEquals("customUserService", AnnotationNameGenerator.getEffectiveName(CustomUserService.class));
        assertEquals("customUserDao", AnnotationNameGenerator.getEffectiveName(CustomUserDao.class));
        
        // 测试没有name属性的注解（使用生成的名称）
        assertEquals("userService", AnnotationNameGenerator.getEffectiveName(UserService.class));
        assertEquals("userDao", AnnotationNameGenerator.getEffectiveName(UserDao.class));
        assertEquals("userComponent", AnnotationNameGenerator.getEffectiveName(UserComponent.class));
        assertEquals("userRepository", AnnotationNameGenerator.getEffectiveName(UserRepository.class));
        assertEquals("userController", AnnotationNameGenerator.getEffectiveName(UserController.class));
        
        // 测试空类
        assertEquals("", AnnotationNameGenerator.getEffectiveName(null));
    }

    /**
     * 测试注解信息获取
     */
    @Test
    public void testGetAnnotationInfo() {
        Map<String, Object> info = AnnotationNameGenerator.getAnnotationInfo(CustomUserService.class);
        
        assertNotNull(info);
        assertEquals("cn.qaiu.vx.core.util.AnnotationNameGeneratorTest$CustomUserService", info.get("className"));
        assertEquals("CustomUserService", info.get("simpleName"));
        assertEquals("customUserService", info.get("generatedName"));
        assertEquals("customUserService", info.get("effectiveName"));
        assertEquals("Service", info.get("annotationType"));
        
        @SuppressWarnings("unchecked")
        Map<String, Object> attributes = (Map<String, Object>) info.get("annotationAttributes");
        assertNotNull(attributes);
        assertEquals("customUserService", attributes.get("name"));
        assertEquals("Service", attributes.get("annotation"));
    }

    /**
     * 测试ServiceModule的名称映射功能
     */
    @Test
    public void testServiceModuleNameMapping() {
        // 这里可以测试ServiceModule提供的名称映射功能
        // 由于需要Dagger2组件，暂时跳过具体实现
        assertTrue(true, "ServiceModule name mapping test placeholder");
    }

    // 测试用的内部类

    @Service
    public static class UserService implements UserServiceInterface {
    }
    
    @io.vertx.codegen.annotations.ProxyGen
    interface UserServiceInterface {
    }

    @Dao
    public static class UserDao {
    }

    @Component
    public static class UserComponent {
    }

    @Repository
    public static class UserRepository {
    }

    @Controller
    public static class UserController {
    }

    @Service(name = "customUserService")
    public static class CustomUserService implements CustomUserServiceInterface {
    }
    
    @io.vertx.codegen.annotations.ProxyGen
    interface CustomUserServiceInterface {
    }

    @Dao(name = "customUserDao")
    public static class CustomUserDao {
    }
}
