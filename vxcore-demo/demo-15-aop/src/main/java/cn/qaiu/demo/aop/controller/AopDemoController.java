package cn.qaiu.demo.aop.controller;

import cn.qaiu.demo.aop.service.CalculatorService;
import cn.qaiu.vx.core.annotations.RouteHandler;
import cn.qaiu.vx.core.annotations.RouteMapping;
import cn.qaiu.vx.core.enums.RouteMethod;
import cn.qaiu.vx.core.model.JsonResult;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

import javax.inject.Inject;

@RouteHandler("/api/aop")
public class AopDemoController {

    @Inject
    CalculatorService calculatorService;

    @RouteMapping(value = "/add", method = RouteMethod.GET)
    public Future<JsonResult<JsonObject>> add(Integer a, Integer b) {
        int valA = a != null ? a : 1;
        int valB = b != null ? b : 2;
        int result = calculatorService.add(valA, valB);
        JsonObject data = new JsonObject()
            .put("operation", "add")
            .put("a", valA).put("b", valB)
            .put("result", result);
        return Future.succeededFuture(JsonResult.data(data));
    }

    @RouteMapping(value = "/divide", method = RouteMethod.GET)
    public Future<JsonResult<JsonObject>> divide(Integer a, Integer b) {
        int valA = a != null ? a : 10;
        int valB = b != null ? b : 2;
        try {
            int result = calculatorService.divide(valA, valB);
            JsonObject data = new JsonObject()
                .put("operation", "divide")
                .put("a", valA).put("b", valB)
                .put("result", result);
            return Future.succeededFuture(JsonResult.data(data));
        } catch (ArithmeticException e) {
            return Future.succeededFuture(JsonResult.error(e.getMessage(), 400));
        }
    }
}
