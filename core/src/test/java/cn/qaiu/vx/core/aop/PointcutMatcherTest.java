package cn.qaiu.vx.core.aop;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

/**
 * PointcutMatcher 单元测试
 *
 * @author <a href="https://qaiu.top">QAIU</a>
 */
@DisplayName("切点匹配器测试")
public class PointcutMatcherTest {

    @Nested
    @DisplayName("execution 表达式测试")
    class ExecutionExpressionTest {

        @Test
        @DisplayName("测试匹配任意返回类型的方法")
        void testMatchAnyReturnType() throws NoSuchMethodException {
            PointcutMatcher matcher = new PointcutMatcher("execution(* cn.qaiu.vx.core.aop.PointcutMatcherTest.*(..))");
            
            Method method = PointcutMatcherTest.class.getDeclaredMethod("testMethod");
            assertTrue(matcher.matches(method), "应该匹配当前类的方法");
        }

        @Test
        @DisplayName("测试匹配指定返回类型")
        void testMatchSpecificReturnType() throws NoSuchMethodException {
            PointcutMatcher matcher = new PointcutMatcher("execution(String cn.qaiu.vx.core.aop.PointcutMatcherTest.*(..))");
            
            Method stringMethod = PointcutMatcherTest.class.getDeclaredMethod("stringMethod");
            Method voidMethod = PointcutMatcherTest.class.getDeclaredMethod("testMethod");
            
            assertTrue(matcher.matches(stringMethod), "应该匹配返回String的方法");
            assertFalse(matcher.matches(voidMethod), "不应该匹配返回void的方法");
        }

        @Test
        @DisplayName("测试通配符包路径匹配")
        void testWildcardPackageMatch() throws NoSuchMethodException {
            PointcutMatcher matcher = new PointcutMatcher("execution(* cn.qaiu..*.*(..))");
            
            Method method = PointcutMatcherTest.class.getDeclaredMethod("testMethod");
            assertTrue(matcher.matches(method), "应该匹配 cn.qaiu 包下任意子包的方法");
        }

        @Test
        @DisplayName("测试方法名通配符匹配")
        void testMethodNameWildcard() throws NoSuchMethodException {
            PointcutMatcher matcher = new PointcutMatcher("execution(* cn.qaiu.vx.core.aop.PointcutMatcherTest.test*(..))");
            
            Method testMethod = PointcutMatcherTest.class.getDeclaredMethod("testMethod");
            Method stringMethod = PointcutMatcherTest.class.getDeclaredMethod("stringMethod");
            
            assertTrue(matcher.matches(testMethod), "应该匹配 test 开头的方法");
            assertFalse(matcher.matches(stringMethod), "不应该匹配非 test 开头的方法");
        }
    }

    @Nested
    @DisplayName("@annotation 表达式测试")
    class AnnotationExpressionTest {

        @Test
        @DisplayName("测试匹配带有指定注解的方法")
        void testMatchAnnotatedMethod() throws NoSuchMethodException {
            PointcutMatcher matcher = new PointcutMatcher("@annotation(cn.qaiu.vx.core.aop.Loggable)");
            
            Method annotatedMethod = TestService.class.getDeclaredMethod("annotatedMethod");
            Method plainMethod = TestService.class.getDeclaredMethod("plainMethod");
            
            assertTrue(matcher.matches(annotatedMethod), "应该匹配带有 @Loggable 注解的方法");
            assertFalse(matcher.matches(plainMethod), "不应该匹配没有注解的方法");
        }
    }

    @Nested
    @DisplayName("within 表达式测试")
    class WithinExpressionTest {

        @Test
        @DisplayName("测试匹配指定类内的方法")
        void testMatchWithinClass() throws NoSuchMethodException {
            PointcutMatcher matcher = new PointcutMatcher("within(cn.qaiu.vx.core.aop.PointcutMatcherTest)");
            
            Method method = PointcutMatcherTest.class.getDeclaredMethod("testMethod");
            assertTrue(matcher.matches(method), "应该匹配指定类内的方法");
        }

        @Test
        @DisplayName("测试匹配包内所有类的方法")
        void testMatchWithinPackage() throws NoSuchMethodException {
            PointcutMatcher matcher = new PointcutMatcher("within(cn.qaiu.vx.core.aop.*)");
            
            Method method = PointcutMatcherTest.class.getDeclaredMethod("testMethod");
            assertTrue(matcher.matches(method), "应该匹配包内类的方法");
        }
    }

    @Nested
    @DisplayName("无效表达式测试")
    class InvalidExpressionTest {

        @Test
        @DisplayName("测试无效表达式抛出异常")
        void testInvalidExpression() {
            assertThrows(IllegalArgumentException.class, () -> {
                new PointcutMatcher("invalid expression");
            }, "无效表达式应该抛出 IllegalArgumentException");
        }
    }

    // 测试用辅助方法
    void testMethod() {
    }

    String stringMethod() {
        return "test";
    }

    // 测试用服务类
    static class TestService {
        @Loggable
        public void annotatedMethod() {
        }

        public void plainMethod() {
        }
    }
}
