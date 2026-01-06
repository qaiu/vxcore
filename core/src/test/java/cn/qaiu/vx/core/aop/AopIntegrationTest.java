package cn.qaiu.vx.core.aop;

import static org.junit.jupiter.api.Assertions.*;

import cn.qaiu.vx.core.aop.annotation.After;
import cn.qaiu.vx.core.aop.annotation.AfterThrowing;
import cn.qaiu.vx.core.aop.annotation.Around;
import cn.qaiu.vx.core.aop.annotation.Aspect;
import cn.qaiu.vx.core.aop.annotation.Before;
import cn.qaiu.vx.core.aop.annotation.Order;
import org.junit.jupiter.api.*;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * AOP 框架集成测试
 * 测试完整的 AOP 流程：切面注册 -> 方法拦截 -> 通知执行
 *
 * @author <a href="https://qaiu.top">QAIU</a>
 */
@DisplayName("AOP 集成测试")
public class AopIntegrationTest {

    private static final List<String> executionLog = new ArrayList<>();

    @BeforeEach
    void setUp() {
        executionLog.clear();
        AspectRegistry.getInstance().clear();
        AspectProcessor.getInstance().clearCache();
    }

    @AfterEach
    void tearDown() {
        AspectRegistry.getInstance().clear();
        AspectProcessor.getInstance().clearCache();
    }

    // ============ 测试注解 ============
    
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @interface TestLoggable {
    }

    @Test
    @DisplayName("测试 @Before 通知执行")
    void testBeforeAdviceExecution() throws Throwable {
        // 注册切面
        AspectRegistry.getInstance().registerAspect(new BeforeTestAspect());
        
        // 获取拦截器链并执行
        var method = UserService.class.getDeclaredMethod("findById", Long.class);
        InterceptorChain chain = AspectProcessor.getInstance().getInterceptorChain(method);
        
        UserService target = new UserService();
        Callable<Object> invoker = () -> {
            executionLog.add("target");
            return "user";
        };
        
        chain.intercept(target, method, new Object[]{1L}, invoker);
        
        // 验证执行顺序
        assertTrue(executionLog.contains("before"), "应该执行 before 通知");
        assertTrue(executionLog.contains("target"), "应该执行目标方法");
        assertTrue(executionLog.indexOf("before") < executionLog.indexOf("target"), 
            "before 应该在 target 之前执行");
    }

    @Test
    @DisplayName("测试 @After 通知执行")
    void testAfterAdviceExecution() throws Throwable {
        AspectRegistry.getInstance().registerAspect(new AfterTestAspect());
        
        var method = UserService.class.getDeclaredMethod("findById", Long.class);
        InterceptorChain chain = AspectProcessor.getInstance().getInterceptorChain(method);
        
        UserService target = new UserService();
        Callable<Object> invoker = () -> {
            executionLog.add("target");
            return "user";
        };
        
        chain.intercept(target, method, new Object[]{1L}, invoker);
        
        assertTrue(executionLog.contains("after"), "应该执行 after 通知");
        assertTrue(executionLog.indexOf("target") < executionLog.indexOf("after"), 
            "after 应该在 target 之后执行");
    }

    @Test
    @DisplayName("测试 @Around 通知执行")
    void testAroundAdviceExecution() throws Throwable {
        AspectRegistry.getInstance().registerAspect(new AroundTestAspect());
        
        var method = UserService.class.getDeclaredMethod("findById", Long.class);
        InterceptorChain chain = AspectProcessor.getInstance().getInterceptorChain(method);
        
        UserService target = new UserService();
        Callable<Object> invoker = () -> {
            executionLog.add("target");
            return "user";
        };
        
        chain.intercept(target, method, new Object[]{1L}, invoker);
        
        // 验证完整的环绕执行
        assertEquals(List.of("around-before", "target", "around-after"), executionLog);
    }

    @Test
    @DisplayName("测试多切面按优先级执行")
    void testMultipleAspectsOrderedExecution() throws Throwable {
        // 注册切面（按不同顺序注册）
        AspectRegistry.getInstance().registerAspect(new LowPriorityAspect());  // order=100
        AspectRegistry.getInstance().registerAspect(new HighPriorityAspect()); // order=1
        
        var method = UserService.class.getDeclaredMethod("findById", Long.class);
        InterceptorChain chain = AspectProcessor.getInstance().getInterceptorChain(method);
        
        UserService target = new UserService();
        Callable<Object> invoker = () -> {
            executionLog.add("target");
            return "user";
        };
        
        chain.intercept(target, method, new Object[]{1L}, invoker);
        
        // 高优先级切面应该先执行
        int highIndex = executionLog.indexOf("high-before");
        int lowIndex = executionLog.indexOf("low-before");
        assertTrue(highIndex < lowIndex, "高优先级切面应该先执行");
    }

    @Test
    @DisplayName("测试异常情况下 @After 仍然执行")
    void testAfterExecutesOnException() throws Throwable {
        AspectRegistry.getInstance().registerAspect(new AfterTestAspect());
        
        var method = UserService.class.getDeclaredMethod("findById", Long.class);
        InterceptorChain chain = AspectProcessor.getInstance().getInterceptorChain(method);
        
        UserService target = new UserService();
        Callable<Object> invoker = () -> {
            executionLog.add("target");
            throw new RuntimeException("Test exception");
        };
        
        assertThrows(RuntimeException.class, () -> {
            chain.intercept(target, method, new Object[]{1L}, invoker);
        });
        
        assertTrue(executionLog.contains("after"), "异常时 after 仍应该执行");
    }

    @Test
    @DisplayName("测试 @AfterThrowing 捕获异常")
    void testAfterThrowingCatchesException() throws Throwable {
        AspectRegistry.getInstance().registerAspect(new AfterThrowingTestAspect());
        
        var method = UserService.class.getDeclaredMethod("findById", Long.class);
        InterceptorChain chain = AspectProcessor.getInstance().getInterceptorChain(method);
        
        UserService target = new UserService();
        Callable<Object> invoker = () -> {
            throw new RuntimeException("Test exception");
        };
        
        assertThrows(RuntimeException.class, () -> {
            chain.intercept(target, method, new Object[]{1L}, invoker);
        });
        
        assertTrue(executionLog.contains("afterThrowing:Test exception"), 
            "应该捕获并处理异常");
    }

    @Test
    @DisplayName("测试 @annotation 切点表达式")
    void testAnnotationPointcut() throws Throwable {
        AspectRegistry.getInstance().registerAspect(new AnnotationBasedAspect());
        
        // 有 @TestLoggable 注解的方法
        var annotatedMethod = UserService.class.getDeclaredMethod("loggableMethod");
        InterceptorChain chain1 = AspectProcessor.getInstance().getInterceptorChain(annotatedMethod);
        
        UserService target = new UserService();
        chain1.intercept(target, annotatedMethod, new Object[]{}, () -> {
            executionLog.add("annotated-target");
            return null;
        });
        
        assertTrue(executionLog.contains("annotation-before"), "带注解的方法应该被拦截");
        
        // 没有 @TestLoggable 注解的方法
        executionLog.clear();
        AspectProcessor.getInstance().clearCache();
        
        var plainMethod = UserService.class.getDeclaredMethod("plainMethod");
        InterceptorChain chain2 = AspectProcessor.getInstance().getInterceptorChain(plainMethod);
        
        chain2.intercept(target, plainMethod, new Object[]{}, () -> {
            executionLog.add("plain-target");
            return null;
        });
        
        assertFalse(executionLog.contains("annotation-before"), "无注解的方法不应该被拦截");
    }

    // ============ 测试用服务类 ============

    static class UserService {
        public String findById(Long id) {
            return "user:" + id;
        }

        @TestLoggable
        public void loggableMethod() {
        }

        public void plainMethod() {
        }
    }

    // ============ 测试切面 ============

    @Aspect
    static class BeforeTestAspect {
        @Before("execution(* cn.qaiu.vx.core.aop.AopIntegrationTest.UserService.*(..))")
        public void before(JoinPoint jp) {
            executionLog.add("before");
        }
    }

    @Aspect
    static class AfterTestAspect {
        @After("execution(* cn.qaiu.vx.core.aop.AopIntegrationTest.UserService.*(..))")
        public void after(JoinPoint jp) {
            executionLog.add("after");
        }
    }

    @Aspect
    static class AroundTestAspect {
        @Around("execution(* cn.qaiu.vx.core.aop.AopIntegrationTest.UserService.*(..))")
        public Object around(ProceedingJoinPoint pjp) throws Throwable {
            executionLog.add("around-before");
            Object result = pjp.proceed();
            executionLog.add("around-after");
            return result;
        }
    }

    @Aspect
    static class AfterThrowingTestAspect {
        @AfterThrowing(value = "execution(* cn.qaiu.vx.core.aop.AopIntegrationTest.UserService.*(..))", throwing = "ex")
        public void afterThrowing(JoinPoint jp, Throwable ex) {
            executionLog.add("afterThrowing:" + ex.getMessage());
        }
    }

    @Aspect
    @Order(1)
    static class HighPriorityAspect {
        @Before("execution(* cn.qaiu.vx.core.aop.AopIntegrationTest.UserService.*(..))")
        public void before(JoinPoint jp) {
            executionLog.add("high-before");
        }
    }

    @Aspect
    @Order(100)
    static class LowPriorityAspect {
        @Before("execution(* cn.qaiu.vx.core.aop.AopIntegrationTest.UserService.*(..))")
        public void before(JoinPoint jp) {
            executionLog.add("low-before");
        }
    }

    @Aspect
    static class AnnotationBasedAspect {
        @Before("@annotation(cn.qaiu.vx.core.aop.AopIntegrationTest.TestLoggable)")
        public void before(JoinPoint jp) {
            executionLog.add("annotation-before");
        }
    }
}
