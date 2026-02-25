package cn.qaiu.example.controller;

import cn.qaiu.vx.core.annotations.RouteHandler;
import cn.qaiu.vx.core.annotations.RouteMapping;
import cn.qaiu.vx.core.base.BaseHttpApi;
import cn.qaiu.vx.core.enums.RouteMethod;
import io.vertx.core.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 示例控制器 演示 VXCore 框架的三层架构
 *
 * @author QAIU
 */
@RouteHandler(order = 1)
public class ExampleController implements BaseHttpApi {

  private static final Logger LOGGER = LoggerFactory.getLogger(ExampleController.class);

  /** 健康检查端点 */
  @RouteMapping(value = "/health", method = RouteMethod.GET)
  public Future<String> health() {
    LOGGER.info("Health check requested");
    return Future.succeededFuture("VXCore Example Application is running");
  }

  /** 用户列表端点 */
  @RouteMapping(value = "/users", method = RouteMethod.GET)
  public Future<String> getUsers() {
    LOGGER.info("Get users requested");
    return Future.succeededFuture("GET /users");
  }

  /** 产品列表端点 */
  @RouteMapping(value = "/products", method = RouteMethod.GET)
  public Future<String> getProducts() {
    LOGGER.info("Get products requested");
    return Future.succeededFuture("GET /products");
  }

  /** 系统信息端点 */
  @RouteMapping(value = "/system/info", method = RouteMethod.GET)
  public Future<String> getSystemInfo() {
    LOGGER.info("Get system info requested");
    return Future.succeededFuture("1.0.0");
  }
}
