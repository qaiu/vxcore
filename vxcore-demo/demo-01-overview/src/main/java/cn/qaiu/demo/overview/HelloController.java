package cn.qaiu.demo.overview;

import cn.qaiu.vx.core.annotations.RouteHandler;
import cn.qaiu.vx.core.annotations.RouteMapping;
import cn.qaiu.vx.core.enums.RouteMethod;
import cn.qaiu.vx.core.model.JsonResult;
import io.vertx.core.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 验证 01-overview.md 中的控制器示例
 *
 * 文档原文使用 HttpMethod.GET, 但实际框架使用 RouteMethod.GET
 * 这是一个文档不一致问题 (ISSUE-001)
 *
 * 文档原文:
 * <pre>
 * @RouteHandler("/api")
 * public class UserController {
 *     @RouteMapping(value = "/users", method = HttpMethod.GET)
 *     public Future<JsonResult> getUsers(@RequestParam("page") int page) {
 *         ...
 *     }
 * }
 * </pre>
 */
@RouteHandler("/api")
public class HelloController {

    private static final Logger log = LoggerFactory.getLogger(HelloController.class);

    /**
     * 简单的 hello 接口
     * 验证: @RouteHandler + @RouteMapping 基本路由
     */
    @RouteMapping(value = "/hello", method = RouteMethod.GET)
    public Future<JsonResult<String>> hello(String name) {
        log.info("hello called with name: {}", name);
        String greeting = "Hello, " + (name != null ? name : "VXCore") + "!";
        return Future.succeededFuture(JsonResult.data(greeting));
    }

    /**
     * 验证无参路由
     */
    @RouteMapping(value = "/status", method = RouteMethod.GET)
    public Future<JsonResult<String>> status() {
        return Future.succeededFuture(JsonResult.data("demo-01-overview is running"));
    }
}
