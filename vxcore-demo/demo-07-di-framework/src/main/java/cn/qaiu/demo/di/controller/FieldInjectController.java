package cn.qaiu.demo.di.controller;

import cn.qaiu.demo.di.service.GreetingService;
import cn.qaiu.vx.core.annotations.RouteHandler;
import cn.qaiu.vx.core.annotations.RouteMapping;
import cn.qaiu.vx.core.enums.RouteMethod;
import cn.qaiu.vx.core.model.JsonResult;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

/**
 * 测试场景1: 字段注入 (具体类)
 * 
 * 验证 07-di-framework.md "控制器注入" 章节 (第370-385行):
 * <pre>
 * @RouteHandler("/api/users")
 * public class UserController {
 *     @Inject
 *     private UserService userService;
 *     ...
 * }
 * </pre>
 * 
 * 框架行为分析:
 * - RouterHandlerFactory.createControllerInstance() 会先尝试构造器注入
 * - 失败后用无参构造器创建实例，再遍历 @Inject 字段调用 injectFields()
 * - resolveService() 对具体类调用 ReflectionUtil.newWithNoParam()
 * 
 * 预期: GreetingService 应该被成功注入(通过反射 new)
 * 但不会是单例 - 每个 Controller 会得到一个新的 GreetingService 实例
 */
@RouteHandler("/api/di")
public class FieldInjectController {

    private static final Logger log = LoggerFactory.getLogger(FieldInjectController.class);

    @Inject
    GreetingService greetingService;

    /**
     * 测试字段注入是否工作
     * GET /api/di/field-inject?name=xxx
     */
    @RouteMapping(value = "/field-inject", method = RouteMethod.GET)
    public Future<JsonResult<JsonObject>> testFieldInject(String name) {
        log.info("testFieldInject called, greetingService={}", greetingService);

        JsonObject result = new JsonObject();
        if (greetingService != null) {
            result.put("status", "SUCCESS");
            result.put("message", greetingService.greet(name));
            result.put("serviceInstanceId", greetingService.getInstanceId());
            result.put("callCount", greetingService.getCallCount());
            result.put("injectionType", "FIELD_INJECT");
        } else {
            result.put("status", "FAILED");
            result.put("message", "greetingService is NULL - field injection did not work!");
            result.put("injectionType", "FIELD_INJECT");
        }
        return Future.succeededFuture(JsonResult.data(result));
    }

    /**
     * 验证单例行为 - 多次调用应该看到 callCount 递增(如果是单例)
     * GET /api/di/verify-singleton
     */
    @RouteMapping(value = "/verify-singleton", method = RouteMethod.GET)
    public Future<JsonResult<JsonObject>> verifySingleton() {
        log.info("verifySingleton called");

        JsonObject result = new JsonObject();
        if (greetingService != null) {
            greetingService.greet("singleton-test");
            result.put("instanceId", greetingService.getInstanceId());
            result.put("callCount", greetingService.getCallCount());
            result.put("note", "如果 callCount 随调用递增，说明是单例; 如果始终为1，说明每次创建新实例");
        } else {
            result.put("status", "FAILED");
            result.put("message", "greetingService is NULL");
        }
        return Future.succeededFuture(JsonResult.data(result));
    }
}
