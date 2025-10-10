package cn.qaiu.example.controller;

import cn.qaiu.vx.core.annotaions.RouteHandler;
import cn.qaiu.vx.core.annotaions.RouteMapping;
import cn.qaiu.vx.core.base.BaseHttpApi;
import cn.qaiu.vx.core.enums.RouteMethod;
import cn.qaiu.vx.core.model.JsonResult;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 示例控制器
 * 演示 VXCore 框架的三层架构
 * 
 * @author QAIU
 */
@RouteHandler(order = 1)
public class ExampleController implements BaseHttpApi {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ExampleController.class);
    
    /**
     * 健康检查端点
     */
    @RouteMapping(value = "/health", method = RouteMethod.GET)
    public void health(RoutingContext ctx) {
        LOGGER.info("Health check requested");
        JsonResult<String> result = JsonResult.data("VXCore Example Application is running");
        doFireJsonResultResponse(ctx, result);
    }
    
    /**
     * 用户列表端点
     */
    @RouteMapping(value = "/users", method = RouteMethod.GET)
    public void getUsers(RoutingContext ctx) {
        LOGGER.info("Get users requested");
        JsonResult<String> result = JsonResult.data("GET /users");
        doFireJsonResultResponse(ctx, result);
    }
    
    /**
     * 产品列表端点
     */
    @RouteMapping(value = "/products", method = RouteMethod.GET)
    public void getProducts(RoutingContext ctx) {
        LOGGER.info("Get products requested");
        JsonResult<String> result = JsonResult.data("GET /products");
        doFireJsonResultResponse(ctx, result);
    }
    
    /**
     * 系统信息端点
     */
    @RouteMapping(value = "/system/info", method = RouteMethod.GET)
    public void getSystemInfo(RoutingContext ctx) {
        LOGGER.info("Get system info requested");
        JsonResult<String> result = JsonResult.data("1.0.0");
        doFireJsonResultResponse(ctx, result);
    }
}
