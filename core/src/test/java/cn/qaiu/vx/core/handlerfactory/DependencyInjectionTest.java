package cn.qaiu.vx.core.handlerfactory;

import static org.junit.jupiter.api.Assertions.*;

import cn.qaiu.vx.core.annotations.RouteHandler;
import cn.qaiu.vx.core.annotations.RouteMapping;
import cn.qaiu.vx.core.enums.RouteMethod;
import cn.qaiu.vx.core.model.JsonResult;
import cn.qaiu.vx.core.util.VertxHolder;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import javax.inject.Inject;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 依赖注入功能单元测试
 * 测试Controller的自动依赖注入功能(使用普通类而非EventBus服务)
 *
 * @author <a href="https://qaiu.top">QAIU</a>
 */
@ExtendWith(VertxExtension.class)
@DisplayName("依赖注入单元测试")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DependencyInjectionTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(DependencyInjectionTest.class);
  
  private RouterHandlerFactory routerHandlerFactory;
  private Vertx vertx;

  @BeforeEach
  void setUp(Vertx vertx) {
    this.vertx = vertx;
    VertxHolder.init(vertx);
    this.routerHandlerFactory = new RouterHandlerFactory("");
  }

  // =================== 测试用的普通Service类 (非EventBus服务) ===================
  
  /**
   * 普通Service类用于测试字段注入
   */
  @javax.inject.Singleton
  public static class SimpleService {
    private String name = "SimpleService";
    
    public String getName() {
      return name;
    }
    
    public String getData() {
      return "test-data-from-simple-service";
    }
  }
  
  /**
   * 另一个Service用于测试多字段注入
   */
  @javax.inject.Singleton
  public static class AnotherService {
    private String type = "AnotherService";
    
    public String getType() {
      return type;
    }
  }

  // =================== 测试用的Controller ===================
  
  /**
   * 测试字段注入的Controller (注入普通类)
   */
  @RouteHandler("/field-inject")
  public static class FieldInjectionController {
    
    @Inject
    SimpleService simpleService;  // package-private for Dagger2
    
    public FieldInjectionController() {
      // 无参构造器
    }
    
    public boolean isServiceInjected() {
      return simpleService != null;
    }
    
    @RouteMapping(value = "/test", method = RouteMethod.GET)
    public Future<JsonResult<String>> test() {
      if (simpleService == null) {
        return Future.succeededFuture(JsonResult.error("Service not injected"));
      }
      return Future.succeededFuture(JsonResult.data(simpleService.getData()));
    }
  }

  /**
   * 测试构造器注入的Controller (带无参构造作为后备)
   */
  @RouteHandler("/constructor-inject")
  public static class ConstructorInjectionController {
    
    private SimpleService simpleService;
    private boolean constructorCalled = false;
    
    // 无参构造器作为后备
    public ConstructorInjectionController() {
    }
    
    @Inject
    public ConstructorInjectionController(SimpleService simpleService) {
      this.simpleService = simpleService;
      this.constructorCalled = true;
    }
    
    public boolean isConstructorCalled() {
      return constructorCalled;
    }
    
    public boolean isServiceInjected() {
      return simpleService != null;
    }
    
    @RouteMapping(value = "/test", method = RouteMethod.GET)
    public Future<JsonResult<String>> test() {
      if (simpleService == null) {
        return Future.succeededFuture(JsonResult.error("Service not injected"));
      }
      return Future.succeededFuture(JsonResult.data(simpleService.getData()));
    }
  }

  /**
   * 测试无注入的Controller (应该能正常创建)
   */
  @RouteHandler("/no-inject")
  public static class NoInjectionController {
    
    private boolean created = false;
    
    public NoInjectionController() {
      this.created = true;
    }
    
    public boolean isCreated() {
      return created;
    }
    
    @RouteMapping(value = "/test", method = RouteMethod.GET)
    public Future<JsonResult<String>> test() {
      return Future.succeededFuture(JsonResult.data("no-injection-ok"));
    }
  }

  /**
   * 测试多个字段注入的Controller
   */
  @RouteHandler("/multi-inject")
  public static class MultipleInjectionController {
    
    @Inject
    SimpleService simpleService;  // package-private for Dagger2
    
    @Inject
    AnotherService anotherService;  // package-private for Dagger2
    
    public int countInjected() {
      int count = 0;
      if (simpleService != null) count++;
      if (anotherService != null) count++;
      return count;
    }
  }

  /**
   * 测试注入普通非Service类(应该返回null)
   */
  @RouteHandler("/inject-non-service")
  public static class NonServiceInjectionController {
    
    @Inject
    String someString;  // 不是Service,应该保持null
    
    public boolean isStringNull() {
      return someString == null;
    }
  }

  // =================== 单元测试 ===================

  @Test
  @Order(1)
  @DisplayName("测试字段注入 - 注入普通Service类")
  void testFieldInjection(VertxTestContext testContext) {
    testContext.verify(() -> {
      try {
        // 使用反射调用createControllerInstance
        java.lang.reflect.Method method = RouterHandlerFactory.class.getDeclaredMethod(
            "createControllerInstance", Class.class);
        method.setAccessible(true);
        
        Object instance = method.invoke(routerHandlerFactory, FieldInjectionController.class);
        assertNotNull(instance, "Controller实例不应为空");
        assertTrue(instance instanceof FieldInjectionController, "应该是正确的类型");
        
        FieldInjectionController controller = (FieldInjectionController) instance;
        
        // 验证Service已注入
        assertTrue(controller.isServiceInjected(), "SimpleService应该被注入");
        
        LOGGER.info("✅ 字段注入测试通过");
        testContext.completeNow();
      } catch (Exception e) {
        LOGGER.error("字段注入测试失败", e);
        testContext.failNow(e);
      }
    });
  }

  @Test
  @Order(2)
  @DisplayName("测试构造器注入 - 注入普通Service类")
  void testConstructorInjection(VertxTestContext testContext) {
    testContext.verify(() -> {
      try {
        java.lang.reflect.Method method = RouterHandlerFactory.class.getDeclaredMethod(
            "createControllerInstance", Class.class);
        method.setAccessible(true);
        
        Object instance = method.invoke(routerHandlerFactory, ConstructorInjectionController.class);
        assertNotNull(instance, "Controller实例不应为空");
        assertTrue(instance instanceof ConstructorInjectionController, "应该是正确的类型");
        
        ConstructorInjectionController controller = (ConstructorInjectionController) instance;
        
        // 验证构造器被调用
        assertTrue(controller.isConstructorCalled(), "带@Inject的构造器应该被调用");
        
        // 验证Service已注入
        assertTrue(controller.isServiceInjected(), "SimpleService应该被注入");
        
        LOGGER.info("✅ 构造器注入测试通过");
        testContext.completeNow();
      } catch (Exception e) {
        LOGGER.error("构造器注入测试失败", e);
        testContext.failNow(e);
      }
    });
  }

  @Test
  @Order(3)
  @DisplayName("测试无注入Controller")
  void testNoInjection(VertxTestContext testContext) {
    testContext.verify(() -> {
      try {
        java.lang.reflect.Method method = RouterHandlerFactory.class.getDeclaredMethod(
            "createControllerInstance", Class.class);
        method.setAccessible(true);
        
        Object instance = method.invoke(routerHandlerFactory, NoInjectionController.class);
        assertNotNull(instance, "Controller实例不应为空");
        assertTrue(instance instanceof NoInjectionController, "应该是正确的类型");
        
        NoInjectionController controller = (NoInjectionController) instance;
        assertTrue(controller.isCreated(), "Controller应该被创建");
        
        LOGGER.info("✅ 无注入Controller测试通过");
        testContext.completeNow();
      } catch (Exception e) {
        LOGGER.error("无注入Controller测试失败", e);
        testContext.failNow(e);
      }
    });
  }

  @Test
  @Order(4)
  @DisplayName("测试多个字段注入")
  void testMultipleFieldInjection(VertxTestContext testContext) {
    testContext.verify(() -> {
      try {
        java.lang.reflect.Method method = RouterHandlerFactory.class.getDeclaredMethod(
            "createControllerInstance", Class.class);
        method.setAccessible(true);
        
        Object instance = method.invoke(routerHandlerFactory, MultipleInjectionController.class);
        assertNotNull(instance, "Controller实例不应为空");
        
        MultipleInjectionController controller = (MultipleInjectionController) instance;
        
        // 验证多个字段都被注入
        assertEquals(2, controller.countInjected(), "应该注入2个Service");
        
        LOGGER.info("✅ 多个字段注入测试通过");
        testContext.completeNow();
      } catch (Exception e) {
        LOGGER.error("多个字段注入测试失败", e);
        testContext.failNow(e);
      }
    });
  }

  @Test
  @Order(5)
  @DisplayName("测试注入非Service类(应该保持null)")
  void testNonServiceInjection(VertxTestContext testContext) {
    testContext.verify(() -> {
      try {
        java.lang.reflect.Method method = RouterHandlerFactory.class.getDeclaredMethod(
            "createControllerInstance", Class.class);
        method.setAccessible(true);
        
        Object instance = method.invoke(routerHandlerFactory, NonServiceInjectionController.class);
        assertNotNull(instance, "Controller实例应该被创建(即使注入失败)");
        
        NonServiceInjectionController controller = (NonServiceInjectionController) instance;
        
        // 验证非Service类没有被注入(保持为null)
        assertTrue(controller.isStringNull(), "非Service类应该保持null");
        
        LOGGER.info("✅ 非Service注入测试通过");
        testContext.completeNow();
      } catch (Exception e) {
        LOGGER.error("非Service注入测试失败", e);
        testContext.failNow(e);
      }
    });
  }

  @Test
  @Order(6)
  @DisplayName("测试resolveService方法 - 非接口类型应返回null")
  void testResolveService(VertxTestContext testContext) {
    testContext.verify(() -> {
      try {
        java.lang.reflect.Method method = RouterHandlerFactory.class.getDeclaredMethod(
            "resolveService", Class.class);
        method.setAccessible(true);
        
        // 测试解析普通类(非接口)
        Object service = method.invoke(routerHandlerFactory, SimpleService.class);
        assertNotNull(service, "应该能创建普通Service类实例");
        assertTrue(service instanceof SimpleService, "应该是SimpleService类型");
        
        // 测试解析基本类型(应该返回null)
        Object primitiveService = method.invoke(routerHandlerFactory, String.class);
        assertNull(primitiveService, "基本类型应该返回null");
        
        LOGGER.info("✅ resolveService方法测试通过");
        testContext.completeNow();
      } catch (Exception e) {
        LOGGER.error("resolveService方法测试失败", e);
        testContext.failNow(e);
      }
    });
  }

  @Test
  @Order(7)
  @DisplayName("测试完整的Router创建流程")
  void testFullRouterCreation(VertxTestContext testContext) {
    testContext.verify(() -> {
      try {
        Router router = routerHandlerFactory.createRouter();
        assertNotNull(router, "Router应该被创建");
        
        LOGGER.info("✅ 完整Router创建流程测试通过");
        testContext.completeNow();
      } catch (Exception e) {
        LOGGER.error("完整Router创建流程测试失败", e);
        testContext.failNow(e);
      }
    });
  }
}
