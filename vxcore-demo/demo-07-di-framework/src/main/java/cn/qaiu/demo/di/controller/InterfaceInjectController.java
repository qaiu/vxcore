package cn.qaiu.demo.di.controller;

import cn.qaiu.demo.di.service.MessageService;
import cn.qaiu.demo.di.service.MessageServiceImpl;
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
 * 测试场景3: 接口注入 vs 实现类注入
 * 
 * 框架 DI 行为:
 * - resolveService() 优先从 ServiceRegistry 查找已注册的服务实例
 * - 接口和具体类都能通过 ServiceRegistry 解析，且返回同一实例(单例)
 * - 如果 ServiceRegistry 中找不到，接口走 EventBus 代理，具体类走反射 new
 */
@RouteHandler("/api/di-iface")
public class InterfaceInjectController {

    private static final Logger log = LoggerFactory.getLogger(InterfaceInjectController.class);

    @Inject
    MessageService messageServiceByInterface;

    @Inject
    MessageServiceImpl messageServiceByImpl;

    /**
     * GET /api/di-iface/interface-inject
     */
    @RouteMapping(value = "/interface-inject", method = RouteMethod.GET)
    public Future<JsonResult<JsonObject>> testInterfaceInject(String key) {
        log.info("testInterfaceInject called");
        String testKey = key != null ? key : "test-key";

        JsonObject result = new JsonObject();

        // 测试A: 接口注入结果
        JsonObject interfaceResult = new JsonObject();
        if (messageServiceByInterface != null) {
            interfaceResult.put("status", "SUCCESS");
            interfaceResult.put("message", messageServiceByInterface.getMessage(testKey));
            interfaceResult.put("actualType", messageServiceByInterface.getClass().getName());
        } else {
            interfaceResult.put("status", "FAILED");
            interfaceResult.put("reason", "接口类型注入失败 - 可能是因为没有在EventBus上注册服务代理");
        }
        result.put("interfaceInject", interfaceResult);

        // 测试B: 实现类注入结果
        JsonObject implResult = new JsonObject();
        if (messageServiceByImpl != null) {
            implResult.put("status", "SUCCESS");
            implResult.put("message", messageServiceByImpl.getMessage(testKey));
            implResult.put("actualType", messageServiceByImpl.getClass().getName());
        } else {
            implResult.put("status", "FAILED");
            implResult.put("reason", "实现类注入失败");
        }
        result.put("implInject", implResult);

        boolean sameInstance = messageServiceByInterface != null && messageServiceByImpl != null
            && messageServiceByInterface == messageServiceByImpl;
        result.put("sameInstance", sameInstance);
        result.put("conclusion", sameInstance
            ? "接口和实现类注入返回同一实例(ServiceRegistry单例)"
            : "接口和实现类注入返回不同实例");

        return Future.succeededFuture(JsonResult.data(result));
    }
}
