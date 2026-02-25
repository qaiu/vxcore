package cn.qaiu.demo.routing.controller;

import cn.qaiu.vx.core.annotations.RouteHandler;
import cn.qaiu.vx.core.annotations.RouteMapping;
import cn.qaiu.vx.core.annotations.param.RequestBody;
import cn.qaiu.vx.core.annotations.param.RequestParam;
import cn.qaiu.vx.core.enums.RouteMethod;
import cn.qaiu.vx.core.model.JsonResult;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

/**
 * 验证 docs/08-routing-annotations.md 中的路由功能：
 * - @RouteHandler 基础路径
 * - @RouteMapping 多种 HTTP 方法 (GET, POST, PUT, DELETE)
 * - @RequestParam 参数绑定 (required, defaultValue)
 * - @PathVariable 路径变量
 * - @RequestBody JSON body 绑定
 * - 路径参数自动绑定（不使用注解）
 */
@RouteHandler("/api/routing")
public class RoutingDemoController {

    @RouteMapping(value = "/hello", method = RouteMethod.GET)
    public Future<JsonResult<String>> hello() {
        return Future.succeededFuture(JsonResult.data("Hello from routing demo!"));
    }

    @RouteMapping(value = "/params", method = RouteMethod.GET)
    public Future<JsonResult<JsonObject>> testParams(
            @RequestParam("name") String name,
            @RequestParam(value = "age", required = false) Integer age) {
        JsonObject result = new JsonObject()
                .put("name", name)
                .put("age", age);
        return Future.succeededFuture(JsonResult.data(result));
    }

    @RouteMapping(value = "/users/{id}", method = RouteMethod.GET)
    public Future<JsonResult<JsonObject>> getUser(Long id) {
        JsonObject result = new JsonObject()
                .put("userId", id)
                .put("source", "path-variable-auto-bind");
        return Future.succeededFuture(JsonResult.data(result));
    }

    @RouteMapping(value = "/post-test", method = RouteMethod.POST)
    public Future<JsonResult<JsonObject>> postTest(@RequestBody JsonObject body) {
        JsonObject result = new JsonObject()
                .put("received", body)
                .put("method", "POST");
        return Future.succeededFuture(JsonResult.data(result));
    }

    @RouteMapping(value = "/users/{id}", method = RouteMethod.PUT)
    public Future<JsonResult<JsonObject>> updateUser(Long id, @RequestBody JsonObject body) {
        JsonObject result = new JsonObject()
                .put("userId", id)
                .put("body", body)
                .put("method", "PUT");
        return Future.succeededFuture(JsonResult.data(result));
    }

    @RouteMapping(value = "/users/{id}", method = RouteMethod.DELETE)
    public Future<JsonResult<JsonObject>> deleteUser(Long id) {
        JsonObject result = new JsonObject()
                .put("userId", id)
                .put("method", "DELETE");
        return Future.succeededFuture(JsonResult.data(result));
    }
}
