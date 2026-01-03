package cn.qaiu.example.service;

import cn.qaiu.db.dsl.lambda.SimpleJService;
import cn.qaiu.example.entity.OrderDetail;
import cn.qaiu.vx.core.codegen.CustomProxyGen;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

/**
 * 订单详情服务接口
 *
 * @author QAIU
 */
@CustomProxyGen
public interface OrderDetailService extends SimpleJService<OrderDetail, Long> {

  /** 获取订单完整信息（包含订单详情和用户信息） */
  Future<OrderWithDetailsAndUser> getOrderWithDetailsAndUser(Long orderId);

  /** 获取用户消费分析 */
  Future<JsonObject> getUserConsumptionAnalysis(Long userId);
}
