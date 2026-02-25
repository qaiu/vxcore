package cn.qaiu.demo.di.controller;

import cn.qaiu.demo.di.service.CounterService;
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
 * 测试场景2: 构造函数注入 (具体类)
 * 
 * 验证 07-di-framework.md "最佳实践" 章节 (第435-446行):
 * <pre>
 * @Service
 * public class UserService {
 *     private final UserDao userDao;
 *     private final CacheService cacheService;
 *
 *     @Inject
 *     public UserService(UserDao userDao, CacheService cacheService) {
 *         this.userDao = userDao;
 *         this.cacheService = cacheService;
 *     }
 * }
 * </pre>
 * 
 * 框架行为分析:
 * - RouterHandlerFactory.tryConstructorInjection() 查找 @Inject 构造器
 * - 解析每个参数类型，通过 resolveService() 获取实例
 * - 对具体类: ReflectionUtil.newWithNoParam()
 * 
 * 预期: CounterService 应该被成功注入
 */
@RouteHandler("/api/di-ctor")
public class CtorInjectController {

    private static final Logger log = LoggerFactory.getLogger(CtorInjectController.class);

    private final CounterService counterService;

    @Inject
    public CtorInjectController(CounterService counterService) {
        this.counterService = counterService;
        log.info("CtorInjectController created with counterService={}", counterService);
    }

    /**
     * 测试构造函数注入
     * GET /api/di-ctor/ctor-inject
     */
    @RouteMapping(value = "/ctor-inject", method = RouteMethod.GET)
    public Future<JsonResult<JsonObject>> testCtorInject() {
        log.info("testCtorInject called, counterService={}", counterService);

        JsonObject result = new JsonObject();
        if (counterService != null) {
            int count = counterService.increment();
            result.put("status", "SUCCESS");
            result.put("counter", count);
            result.put("serviceInstanceId", counterService.getInstanceId());
            result.put("injectionType", "CONSTRUCTOR_INJECT");
        } else {
            result.put("status", "FAILED");
            result.put("message", "counterService is NULL - constructor injection did not work!");
            result.put("injectionType", "CONSTRUCTOR_INJECT");
        }
        return Future.succeededFuture(JsonResult.data(result));
    }
}
