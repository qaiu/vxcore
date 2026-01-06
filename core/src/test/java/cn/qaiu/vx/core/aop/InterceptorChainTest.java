package cn.qaiu.vx.core.aop;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * InterceptorChain 单元测试
 *
 * @author <a href="https://qaiu.top">QAIU</a>
 */
@DisplayName("拦截器链测试")
public class InterceptorChainTest {

    private Method testMethod;
    private Object target;

    @BeforeEach
    void setUp() throws NoSuchMethodException {
        testMethod = TestTarget.class.getDeclaredMethod("doSomething", String.class);
        target = new TestTarget();
    }

    @Test
    @DisplayName("测试空拦截器链直接调用目标方法")
    void testEmptyChain() throws Throwable {
        InterceptorChain chain = new InterceptorChain();
        
        Callable<Object> targetInvoker = () -> "result";
        Object result = chain.intercept(target, testMethod, new Object[]{"arg"}, targetInvoker);
        
        assertEquals("result", result, "空链应该直接返回目标方法结果");
        assertTrue(chain.isEmpty(), "链应该为空");
    }

    @Test
    @DisplayName("测试单个拦截器执行")
    void testSingleInterceptor() throws Throwable {
        InterceptorChain chain = new InterceptorChain();
        AtomicInteger counter = new AtomicInteger(0);
        
        chain.addInterceptor((t, m, args, invoker) -> {
            counter.incrementAndGet();
            return invoker.call();
        });
        
        Callable<Object> targetInvoker = () -> "result";
        Object result = chain.intercept(target, testMethod, new Object[]{"arg"}, targetInvoker);
        
        assertEquals("result", result);
        assertEquals(1, counter.get(), "拦截器应该被调用一次");
    }

    @Test
    @DisplayName("测试多个拦截器按顺序执行")
    void testMultipleInterceptorsOrder() throws Throwable {
        InterceptorChain chain = new InterceptorChain();
        StringBuilder order = new StringBuilder();
        
        chain.addInterceptor((t, m, args, invoker) -> {
            order.append("A-before ");
            Object result = invoker.call();
            order.append("A-after ");
            return result;
        });
        
        chain.addInterceptor((t, m, args, invoker) -> {
            order.append("B-before ");
            Object result = invoker.call();
            order.append("B-after ");
            return result;
        });
        
        Callable<Object> targetInvoker = () -> {
            order.append("target ");
            return "result";
        };
        
        chain.intercept(target, testMethod, new Object[]{"arg"}, targetInvoker);
        
        assertEquals("A-before B-before target B-after A-after ", order.toString(),
            "拦截器应该按 A -> B -> target -> B -> A 的顺序执行");
    }

    @Test
    @DisplayName("测试拦截器可以修改返回值")
    void testInterceptorModifyResult() throws Throwable {
        InterceptorChain chain = new InterceptorChain();
        
        chain.addInterceptor((t, m, args, invoker) -> {
            Object result = invoker.call();
            return result + " modified";
        });
        
        Callable<Object> targetInvoker = () -> "original";
        Object result = chain.intercept(target, testMethod, new Object[]{"arg"}, targetInvoker);
        
        assertEquals("original modified", result, "拦截器应该能修改返回值");
    }

    @Test
    @DisplayName("测试拦截器可以阻止目标方法执行")
    void testInterceptorPreventExecution() throws Throwable {
        InterceptorChain chain = new InterceptorChain();
        AtomicInteger targetCalled = new AtomicInteger(0);
        
        chain.addInterceptor((t, m, args, invoker) -> {
            // 不调用 invoker.call()，直接返回
            return "intercepted";
        });
        
        Callable<Object> targetInvoker = () -> {
            targetCalled.incrementAndGet();
            return "target result";
        };
        
        Object result = chain.intercept(target, testMethod, new Object[]{"arg"}, targetInvoker);
        
        assertEquals("intercepted", result);
        assertEquals(0, targetCalled.get(), "目标方法不应该被调用");
    }

    @Test
    @DisplayName("测试拦截器异常传播")
    void testInterceptorExceptionPropagation() {
        InterceptorChain chain = new InterceptorChain();
        
        chain.addInterceptor((t, m, args, invoker) -> {
            throw new RuntimeException("Interceptor error");
        });
        
        Callable<Object> targetInvoker = () -> "result";
        
        assertThrows(RuntimeException.class, () -> {
            chain.intercept(target, testMethod, new Object[]{"arg"}, targetInvoker);
        }, "拦截器异常应该被传播");
    }

    @Test
    @DisplayName("测试获取拦截器数量")
    void testGetInterceptorCount() {
        InterceptorChain chain = new InterceptorChain();
        assertEquals(0, chain.size());
        
        chain.addInterceptor((t, m, args, invoker) -> invoker.call());
        assertEquals(1, chain.size());
        
        chain.addInterceptor((t, m, args, invoker) -> invoker.call());
        assertEquals(2, chain.size());
    }

    // 测试目标类
    static class TestTarget {
        public String doSomething(String arg) {
            return "done: " + arg;
        }
    }
}
