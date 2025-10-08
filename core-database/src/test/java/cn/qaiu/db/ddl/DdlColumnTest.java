package cn.qaiu.db.ddl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.lang.annotation.Annotation;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DdlColumn注解测试用例
 *
 * @author <a href="https://qaiu.top">QAIU</a>
 */
@DisplayName("DdlColumn注解测试")
public class DdlColumnTest {

    /**
     * 测试DdlColumn注解的基本属性
     */
    @Test
    @DisplayName("测试DdlColumn注解基本属性")
    public void testDdlColumnAnnotationProperties() {
        // 创建一个测试类来验证注解
        class TestClass {
            @DdlColumn(
                name = "test_column",
                type = "VARCHAR",
                length = 100,
                precision = 10,
                scale = 2,
                nullable = false,
                defaultValue = "test",
                defaultValueIsFunction = false,
                autoIncrement = true,
                comment = "测试列",
                uniqueKey = "unique_test",
                indexName = "idx_test",
                version = 2
            )
            private String testField;
        }

        // 验证注解存在
        try {
            java.lang.reflect.Field field = TestClass.class.getDeclaredField("testField");
            assertTrue(field.isAnnotationPresent(DdlColumn.class));
            
            DdlColumn annotation = field.getAnnotation(DdlColumn.class);
            assertNotNull(annotation);
            
            // 验证注解属性
            assertEquals("test_column", annotation.name());
            assertEquals("VARCHAR", annotation.type());
            assertEquals(100, annotation.length());
            assertEquals(10, annotation.precision());
            assertEquals(2, annotation.scale());
            assertFalse(annotation.nullable());
            assertEquals("test", annotation.defaultValue());
            assertFalse(annotation.defaultValueIsFunction());
            assertTrue(annotation.autoIncrement());
            assertEquals("测试列", annotation.comment());
            assertEquals("unique_test", annotation.uniqueKey());
            assertEquals("idx_test", annotation.indexName());
            assertEquals(2, annotation.version());
        } catch (NoSuchFieldException e) {
            fail("测试字段不存在: " + e.getMessage());
        }
    }

    /**
     * 测试DdlColumn注解的默认值
     */
    @Test
    @DisplayName("测试DdlColumn注解默认值")
    public void testDdlColumnAnnotationDefaults() {
        class TestClass {
            @DdlColumn
            private String testField;
        }

        try {
            java.lang.reflect.Field field = TestClass.class.getDeclaredField("testField");
            DdlColumn annotation = field.getAnnotation(DdlColumn.class);
            assertNotNull(annotation);
            
            // 验证默认值
            assertEquals("", annotation.name());
            assertEquals("", annotation.type());
            assertEquals(0, annotation.length());
            assertEquals(0, annotation.precision());
            assertEquals(0, annotation.scale());
            assertTrue(annotation.nullable());
            assertEquals("", annotation.defaultValue());
            assertFalse(annotation.defaultValueIsFunction());
            assertFalse(annotation.autoIncrement());
            assertEquals("", annotation.comment());
            assertEquals("", annotation.uniqueKey());
            assertEquals("", annotation.indexName());
            assertEquals(1, annotation.version());
        } catch (NoSuchFieldException e) {
            fail("测试字段不存在: " + e.getMessage());
        }
    }

    /**
     * 测试DdlColumn注解的保留策略
     */
    @Test
    @DisplayName("测试DdlColumn注解保留策略")
    public void testDdlColumnAnnotationRetention() {
        // DdlColumn类本身没有@DdlColumn注解，所以这里应该返回null
        DdlColumn annotation = DdlColumn.class.getAnnotation(DdlColumn.class);
        assertNull(annotation, "DdlColumn类本身不应该有@DdlColumn注解");
        
        // 验证注解的保留策略是RUNTIME
        Annotation[] annotations = DdlColumn.class.getAnnotations();
        boolean hasRetention = false;
        for (Annotation ann : annotations) {
            if (ann.annotationType().getSimpleName().equals("Retention")) {
                hasRetention = true;
                break;
            }
        }
        assertTrue(hasRetention, "DdlColumn注解应该有Retention策略");
    }

    /**
     * 测试DdlColumn注解的目标类型
     */
    @Test
    @DisplayName("测试DdlColumn注解目标类型")
    public void testDdlColumnAnnotationTarget() {
        // 验证注解可以用于字段上
        class TestClass {
            @DdlColumn
            private String testField;
        }
        
        try {
            java.lang.reflect.Field field = TestClass.class.getDeclaredField("testField");
            assertTrue(field.isAnnotationPresent(DdlColumn.class));
        } catch (NoSuchFieldException e) {
            fail("测试字段不存在: " + e.getMessage());
        }
    }

    /**
     * 测试DdlColumn注解的继承性
     */
    @Test
    @DisplayName("测试DdlColumn注解继承性")
    public void testDdlColumnAnnotationInherited() {
        // 验证注解是否支持继承
        Annotation[] annotations = DdlColumn.class.getAnnotations();
        boolean hasInherited = false;
        for (Annotation ann : annotations) {
            if (ann.annotationType().getSimpleName().equals("Inherited")) {
                hasInherited = true;
                break;
            }
        }
        assertTrue(hasInherited, "DdlColumn注解应该支持继承");
    }
}
