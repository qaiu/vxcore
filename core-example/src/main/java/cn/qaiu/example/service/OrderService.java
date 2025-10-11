package cn.qaiu.example.service;

import cn.qaiu.db.dsl.lambda.JService;
import cn.qaiu.db.dsl.lambda.LambdaPageResult;
import cn.qaiu.example.entity.Order;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单服务接口
 * 
 * @author <a href="https://qaiu.top">QAIU</a>
 */
public interface OrderService extends JService<Order, Long> {

    /**
     * 根据用户ID查找订单
     * 
     * @param userId 用户ID
     * @return 订单列表
     */
    Future<List<Order>> findByUserId(Long userId);

    /**
     * 根据订单状态查找订单
     * 
     * @param status 订单状态
     * @return 订单列表
     */
    Future<List<Order>> findByStatus(String status);

    /**
     * 根据时间范围查找订单
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 订单列表
     */
    Future<List<Order>> findByTimeRange(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 分页查询用户订单
     * 
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页大小
     * @return 分页结果
     */
    Future<LambdaPageResult<Order>> findUserOrders(Long userId, long page, long size);

    /**
     * 统计用户订单数量
     * 
     * @param userId 用户ID
     * @return 订单数量
     */
    Future<Long> countUserOrders(Long userId);

    /**
     * 计算用户订单总金额
     * 
     * @param userId 用户ID
     * @return 总金额
     */
    Future<BigDecimal> calculateUserTotalAmount(Long userId);

    /**
     * 获取待处理订单
     * 
     * @return 待处理订单列表
     */
    Future<List<Order>> getPendingOrders();

    /**
     * 更新订单状态
     * 
     * @param orderId 订单ID
     * @param status 新状态
     * @return 是否更新成功
     */
    Future<Boolean> updateOrderStatus(Long orderId, String status);

    /**
     * 创建订单
     * 
     * @param userId 用户ID
     * @param productId 商品ID
     * @param quantity 数量
     * @return 创建的订单
     */
    Future<Order> createOrder(Long userId, Long productId, Integer quantity);

    /**
     * 根据ID获取订单
     * 
     * @param id 订单ID
     * @return 订单信息
     */
    Future<Order> getOrderById(Long id);

    /**
     * 根据订单号获取订单
     * 
     * @param orderNo 订单号
     * @return 订单列表
     */
    Future<List<Order>> getOrdersByOrderNo(String orderNo);

    /**
     * 获取用户订单
     * 
     * @param userId 用户ID
     * @return 订单列表
     */
    Future<List<Order>> getUserOrders(Long userId);

    /**
     * 获取商品订单
     * 
     * @param productId 商品ID
     * @return 订单列表
     */
    Future<List<Order>> getProductOrders(Long productId);

    /**
     * 根据状态获取订单
     * 
     * @param status 订单状态
     * @return 订单列表
     */
    Future<List<Order>> getOrdersByStatus(Order.OrderStatus status);

    /**
     * 支付订单
     * 
     * @param orderId 订单ID
     * @param paymentMethod 支付方式
     * @return 是否支付成功
     */
    Future<Boolean> payOrder(Long orderId, String paymentMethod);

    /**
     * 发货
     * 
     * @param orderId 订单ID
     * @return 是否发货成功
     */
    Future<Boolean> shipOrder(Long orderId);

    /**
     * 确认收货
     * 
     * @param orderId 订单ID
     * @return 是否确认成功
     */
    Future<Boolean> confirmDelivery(Long orderId);

    /**
     * 取消订单
     * 
     * @param orderId 订单ID
     * @return 是否取消成功
     */
    Future<Boolean> cancelOrder(Long orderId);

    /**
     * 获取订单统计信息
     * 
     * @return 统计信息
     */
    Future<JsonObject> getOrderStatistics();

    /**
     * 获取用户订单统计信息
     * 
     * @param userId 用户ID
     * @return 统计信息
     */
    Future<JsonObject> getUserOrderStatistics(Long userId);

    /**
     * 搜索订单
     * 
     * @param keyword 关键词
     * @return 订单列表
     */
    Future<List<Order>> searchOrders(String keyword);

    /**
     * 删除订单
     * 
     * @param orderId 订单ID
     * @return 是否删除成功
     */
    Future<Boolean> deleteOrder(Long orderId);

    /**
     * 获取所有订单
     * 
     * @return 订单列表
     */
    Future<List<Order>> getAllOrders();
}