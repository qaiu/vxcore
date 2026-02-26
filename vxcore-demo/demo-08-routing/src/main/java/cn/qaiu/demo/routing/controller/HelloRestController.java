package cn.qaiu.demo.routing.controller;

import cn.qaiu.vx.core.annotations.RouteHandler;
import cn.qaiu.vx.core.annotations.RouteMapping;
import cn.qaiu.vx.core.enums.RouteMethod;
import cn.qaiu.vx.core.model.JsonResult;
import io.vertx.core.Future;

/**
 * 新增 Hello 控制器
 */
@RouteHandler
public class HelloRestController {

    /** 同步返回，无 Future 封装 */
    @RouteMapping(value = "/hello", method = RouteMethod.GET)
    public String hello() {
        return "fromVxcoreREST";
    }

    /** 异步返回，带 Future 封装，便于与 /hello 压测对比 */
    @RouteMapping(value = "/hello-async", method = RouteMethod.GET)
    public Future<String> helloAsync() {
        return Future.succeededFuture("fromVxcoreREST");
    }
}

