package cn.qaiu.demo.config.controller;

import cn.qaiu.vx.core.annotations.RouteHandler;
import cn.qaiu.vx.core.annotations.RouteMapping;
import cn.qaiu.vx.core.enums.RouteMethod;
import cn.qaiu.vx.core.model.JsonResult;
import cn.qaiu.vx.core.util.VertxHolder;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

/**
 * 验证 docs/10-configuration.md 中的配置管理功能:
 * - YAML 配置加载 (application.yml + app-dev.yml)
 * - 配置合并优先级
 * - SharedData 配置访问
 */
@RouteHandler("/api/config")
public class ConfigDemoController {

    @SuppressWarnings("unchecked")
    private JsonObject getGlobalConfig() {
        return (JsonObject) VertxHolder.getVertxInstance()
            .sharedData().getLocalMap("local").get("globalConfig");
    }

    @RouteMapping(value = "/all", method = RouteMethod.GET)
    public Future<JsonResult<JsonObject>> getAllConfig() {
        JsonObject config = getGlobalConfig();
        if (config == null) {
            return Future.succeededFuture(JsonResult.data(new JsonObject().put("error", "config not found")));
        }
        return Future.succeededFuture(JsonResult.data(config));
    }

    @RouteMapping(value = "/custom", method = RouteMethod.GET)
    public Future<JsonResult<JsonObject>> getCustomConfig() {
        JsonObject config = getGlobalConfig();
        JsonObject result = new JsonObject();
        if (config != null) {
            result.put("custom", config.getJsonObject("custom"));
            result.put("demo", config.getJsonObject("demo"));
        }
        return Future.succeededFuture(JsonResult.data(result));
    }

    @RouteMapping(value = "/server", method = RouteMethod.GET)
    public Future<JsonResult<JsonObject>> getServerConfig() {
        JsonObject config = getGlobalConfig();
        JsonObject result = new JsonObject();
        if (config != null) {
            result.put("server", config.getJsonObject("server"));
        }
        return Future.succeededFuture(JsonResult.data(result));
    }
}
