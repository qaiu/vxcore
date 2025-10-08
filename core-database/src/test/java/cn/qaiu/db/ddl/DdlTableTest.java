package cn.qaiu.db.ddl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.lang.annotation.Annotation;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DdlTable注解测试用例（简化版）
 *
 * @author <a href="https://qaiu.top">QAIU</a>
 */
@DisplayName("DdlTable注解测试")
public class DdlTableTest {

    /**
     * 测试DdlTable注解的基本属性
     */
    @Test
    @DisplayName("测试DdlTable注解基本属性")
    public void testDdlTableAnnotationProperties() {
        // 创建一个测试类来验证注解
        @DdlTable(
            value = "test_table",
            keyFields = "id",
            version = 2,
            autoSync = true,
            comment = "测试表",
            charset = "utf8mb4",
            collate = "utf8mb4_unicode_ci",
            engine = "InnoDB"
        )
        class TestClass {}

        // 验证注解存在
        assertTrue(TestClass.class.isAnnotationPresent(DdlTable.class));
        
        DdlTable annotation = TestClass.class.getAnnotation(DdlTable.class);
        assertNotNull(annotation);
        
        // 验证注解属性
        assertEquals("test_table", annotation.value());
        assertEquals("id", annotation.keyFields());
        assertEquals(2, annotation.version());
        assertTrue(annotation.autoSync());
        assertEquals("测试表", annotation.comment());
        assertEquals("utf8mb4", annotation.charset());
        assertEquals("utf8mb4_unicode_ci", annotation.collate());
        assertEquals("InnoDB", annotation.engine());
    }

    /**
     * 测试DdlTable注解的默认值
     */
    @Test
    @DisplayName("测试DdlTable注解默认值")
    public void testDdlTableAnnotationDefaults() {
        @DdlTable
        class TestClass {}

        DdlTable annotation = TestClass.class.getAnnotation(DdlTable.class);
        assertNotNull(annotation);
        
        // 验证默认值
        assertEquals("", annotation.value());
        assertEquals("id", annotation.keyFields());
        assertEquals(1, annotation.version());
        assertTrue(annotation.autoSync());
        assertEquals("", annotation.comment());
        assertEquals("utf8mb4", annotation.charset());
        assertEquals("utf8mb4_unicode_ci", annotation.collate());
        assertEquals("InnoDB", annotation.engine());
    }

    /**
     * 测试DdlTable注解的保留策略
     */
    @Test
    @DisplayName("测试DdlTable注解保留策略")
    public void testDdlTableAnnotationRetention() {
        // DdlTable类本身没有@DdlTable注解，所以这里应该返回null
        DdlTable annotation = DdlTable.class.getAnnotation(DdlTable.class);
        assertNull(annotation, "DdlTable类本身不应该有@DdlTable注解");
        
        // 验证注解的保留策略是RUNTIME
        Annotation[] annotations = DdlTable.class.getAnnotations();
        boolean hasRetention = false;
        for (Annotation ann : annotations) {
            if (ann.annotationType().getSimpleName().equals("Retention")) {
                hasRetention = true;
                break;
            }
        }
        assertTrue(hasRetention, "DdlTable注解应该有Retention策略");
    }

    /**
     * 测试DdlTable注解的目标类型
     */
    @Test
    @DisplayName("测试DdlTable注解目标类型")
    public void testDdlTableAnnotationTarget() {
        // 验证注解可以用于类上
        @DdlTable
        class TestClass {}
        
        assertTrue(TestClass.class.isAnnotationPresent(DdlTable.class));
    }
}
