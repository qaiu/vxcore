package cn.qaiu.vx.core.aop;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * AopUtils 单元测试
 *
 * @author <a href="https://qaiu.top">QAIU</a>
 */
@DisplayName("AOP工具类测试")
public class AopUtilsTest {

    @BeforeEach
    void setUp() {
        AspectRegistry.getInstance().clear();
        AspectProcessor.getInstance().clearCache();
    }

    @Test
    @DisplayName("测试代理方法 - 无切面时返回原对象")
    void testProxyWithoutAspects() {
        TestService service = new TestService();
        TestService proxy = AopUtils.proxy(service);
        
        assertSame(service, proxy, "没有匹配的切面时应该返回原对象");
    }

    @Test
    @DisplayName("测试注册切面类")
    void testRegisterAspectClass() {
        AopUtils.registerAspect(TestAspect.class);
        
        assertEquals(1, AspectRegistry.getInstance().getAspectCount());
    }

    @Test
    @DisplayName("测试注册切面实例")
    void testRegisterAspectInstance() {
        TestAspect aspect = new TestAspect();
        AopUtils.registerAspect(aspect);
        
        assertEquals(1, AspectRegistry.getInstance().getAspectCount());
        assertSame(aspect, AspectRegistry.getInstance().getAspect(TestAspect.class).getAspectInstance());
    }

    @Test
    @DisplayName("测试扫描切面")
    void testScanAspects() {
        // 扫描包（可能没有切面，但不应该抛出异常）
        AopUtils.scanAspects("cn.qaiu.nonexistent");
    }

    @Test
    @DisplayName("测试初始化")
    void testInitialize() {
        // 初始化不应该抛出异常
        AopUtils.initialize();
    }

    @Test
    @DisplayName("测试检查初始化状态")
    void testIsInitialized() {
        // 初始状态应该是未初始化
        assertFalse(AopUtils.isInitialized());
        
        // 扫描后应该标记为已初始化
        AopUtils.scanAspects("cn.qaiu.vx.core.aop");
        assertTrue(AopUtils.isInitialized());
    }

    @Test
    @DisplayName("测试检查Agent状态")
    void testIsAgentInstalled() {
        // 这个测试只验证方法可以调用
        boolean installed = AopUtils.isAgentInstalled();
        // 在测试环境中可能返回 true 或 false
    }

    // 测试类
    static class TestService {
        public String doSomething() {
            return "done";
        }
    }

    @cn.qaiu.vx.core.aop.annotation.Aspect
    static class TestAspect {
    }
}
