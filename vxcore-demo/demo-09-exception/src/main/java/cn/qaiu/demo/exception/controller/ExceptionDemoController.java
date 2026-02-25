package cn.qaiu.demo.exception.controller;

import cn.qaiu.vx.core.annotations.RouteHandler;
import cn.qaiu.vx.core.annotations.RouteMapping;
import cn.qaiu.vx.core.enums.RouteMethod;
import cn.qaiu.vx.core.model.JsonResult;
import io.vertx.core.Future;

@RouteHandler("/api/exception")
public class ExceptionDemoController {

    @RouteMapping(value = "/normal", method = RouteMethod.GET)
    public Future<JsonResult<String>> normal() {
        return Future.succeededFuture(JsonResult.data("Normal response"));
    }

    @RouteMapping(value = "/runtime", method = RouteMethod.GET)
    public Future<JsonResult<String>> throwRuntime() {
        throw new RuntimeException("Test runtime exception");
    }

    @RouteMapping(value = "/null-pointer", method = RouteMethod.GET)
    public Future<JsonResult<String>> throwNPE() {
        String s = null;
        return Future.succeededFuture(JsonResult.data(s.length() + ""));
    }

    @RouteMapping(value = "/illegal-arg", method = RouteMethod.GET)
    public Future<JsonResult<String>> throwIllegalArg() {
        throw new IllegalArgumentException("Invalid parameter value");
    }

    @RouteMapping(value = "/future-fail", method = RouteMethod.GET)
    public Future<JsonResult<String>> futureFail() {
        return Future.failedFuture(new RuntimeException("Async operation failed"));
    }
}
