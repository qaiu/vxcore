package cn.qaiu.example.controller;

import cn.qaiu.vx.core.annotaions.RouteHandler;
import cn.qaiu.vx.core.annotaions.RouteMapping;
import cn.qaiu.vx.core.base.BaseHttpApi;
import cn.qaiu.vx.core.model.JsonResult;
import cn.qaiu.vx.core.enums.RouteMethod;
import cn.qaiu.example.entity.Order;
import cn.qaiu.example.service.OrderService;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 订单控制器
 * 处理订单相关的HTTP请求
 * 
 * @author QAIU
 */
@RouteHandler(order = 1)
public class OrderController implements BaseHttpApi {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderController.class);

    private final OrderService orderService;

    /**
     * 构造函数
     * 
     * @param orderService 订单服务实例
     */
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * 创建订单
     * POST /orders
     */
    @RouteMapping(value = "/orders", method = RouteMethod.POST)
    public Future<Order> createOrder(JsonObject requestBody) {
        LOGGER.info("Creating order: {}", requestBody);

        Long userId = requestBody.getLong("userId");
        Long productId = requestBody.getLong("productId");
        Integer quantity = requestBody.getInteger("quantity");

        if (userId == null || productId == null || quantity == null) {
            throw new IllegalArgumentException("Missing required fields: userId, productId, quantity");
        }

        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }

        return orderService.createOrder(userId, productId, quantity);
    }

    /**
     * 根据ID获取订单
     * GET /orders/{id}
     */
    @RouteMapping(value = "/orders/:id", method = RouteMethod.GET)
    public Future<Order> getOrderById(Long id) {
        LOGGER.debug("Getting order by ID: {}", id);

        return orderService.getOrderById(id);
    }

    /**
     * 根据订单号获取订单
     * GET /orders/search?orderNo={orderNo}
     */
    @RouteMapping(value = "/orders/search", method = RouteMethod.GET)
    public Future<List<Order>> getOrdersByOrderNo(String orderNo) {
        LOGGER.debug("Getting orders by orderNo: {}", orderNo);

        if (orderNo == null || orderNo.trim().isEmpty()) {
            throw new IllegalArgumentException("OrderNo is required");
        }

        return orderService.getOrdersByOrderNo(orderNo);
    }

    /**
     * 获取用户的所有订单
     * GET /orders/user/{userId}
     */
    @RouteMapping(value = "/orders/user/:userId", method = RouteMethod.GET)
    public Future<List<Order>> getUserOrders(Long userId) {
        LOGGER.debug("Getting orders for user: {}", userId);

        return orderService.getUserOrders(userId);
    }

    /**
     * 获取产品的所有订单
     * GET /orders/product/{productId}
     */
    @RouteMapping(value = "/orders/product/:productId", method = RouteMethod.GET)
    public Future<List<Order>> getProductOrders(Long productId) {
        LOGGER.debug("Getting orders for product: {}", productId);

        return orderService.getProductOrders(productId);
    }

    /**
     * 根据状态获取订单
     * GET /orders/status/{status}
     */
    @RouteMapping(value = "/orders/status/:status", method = RouteMethod.GET)
    public Future<List<Order>> getOrdersByStatus(String status) {
        LOGGER.debug("Getting orders by status: {}", status);

        try {
            Order.OrderStatus orderStatus = Order.OrderStatus.valueOf(status.toUpperCase());
            return orderService.getOrdersByStatus(orderStatus);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status: " + status);
        }
    }

    /**
     * 支付订单
     * PUT /orders/{id}/pay
     */
    @RouteMapping(value = "/orders/:id/pay", method = RouteMethod.PUT)
    public Future<Boolean> payOrder(Long id, JsonObject requestBody) {
        LOGGER.info("Paying order: {}", id);

        String paymentMethod = requestBody.getString("paymentMethod");
        if (paymentMethod == null || paymentMethod.trim().isEmpty()) {
            throw new IllegalArgumentException("Payment method is required");
        }

        return orderService.payOrder(id, paymentMethod);
    }

    /**
     * 发货
     * PUT /orders/{id}/ship
     */
    @RouteMapping(value = "/orders/:id/ship", method = RouteMethod.PUT)
    public Future<Boolean> shipOrder(Long id) {
        LOGGER.info("Shipping order: {}", id);

        return orderService.shipOrder(id);
    }

    /**
     * 确认收货
     * PUT /orders/{id}/deliver
     */
    @RouteMapping(value = "/orders/:id/deliver", method = RouteMethod.PUT)
    public Future<Boolean> confirmDelivery(Long id) {
        LOGGER.info("Confirming delivery for order: {}", id);

        return orderService.confirmDelivery(id);
    }

    /**
     * 取消订单
     * PUT /orders/{id}/cancel
     */
    @RouteMapping(value = "/orders/:id/cancel", method = RouteMethod.PUT)
    public Future<Boolean> cancelOrder(Long id) {
        LOGGER.info("Cancelling order: {}", id);

        return orderService.cancelOrder(id);
    }

    /**
     * 更新订单状态
     * PUT /orders/{id}/status
     */
    @RouteMapping(value = "/orders/:id/status", method = RouteMethod.PUT)
    public Future<Boolean> updateOrderStatus(Long id, JsonObject requestBody) {
        LOGGER.info("Updating order {} status", id);

        String status = requestBody.getString("status");
        if (status == null || status.trim().isEmpty()) {
            throw new IllegalArgumentException("Status is required");
        }

        try {
            Order.OrderStatus orderStatus = Order.OrderStatus.valueOf(status.toUpperCase());
            return orderService.updateOrderStatus(id, status);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status: " + status);
        }
    }

    /**
     * 获取订单统计信息
     * GET /orders/statistics
     */
    @RouteMapping(value = "/orders/statistics", method = RouteMethod.GET)
    public Future<JsonObject> getOrderStatistics() {
        LOGGER.debug("Getting order statistics");

        return orderService.getOrderStatistics();
    }

    /**
     * 获取用户订单统计
     * GET /orders/user/{userId}/statistics
     */
    @RouteMapping(value = "/orders/user/:userId/statistics", method = RouteMethod.GET)
    public Future<JsonObject> getUserOrderStatistics(Long userId) {
        LOGGER.debug("Getting order statistics for user: {}", userId);

        return orderService.getUserOrderStatistics(userId);
    }

    /**
     * 搜索订单
     * GET /orders/search?keyword={keyword}
     */
    @RouteMapping(value = "/orders/search", method = RouteMethod.GET)
    public Future<List<Order>> searchOrders(String keyword) {
        LOGGER.debug("Searching orders with keyword: {}", keyword);

        if (keyword == null || keyword.trim().isEmpty()) {
            throw new IllegalArgumentException("Keyword is required");
        }

        return orderService.searchOrders(keyword);
    }

    /**
     * 删除订单
     * DELETE /orders/{id}
     */
    @RouteMapping(value = "/orders/:id", method = RouteMethod.DELETE)
    public Future<Boolean> deleteOrder(Long id) {
        LOGGER.info("Deleting order: {}", id);

        return orderService.deleteOrder(id);
    }

    /**
     * 获取所有订单
     * GET /orders
     */
    @RouteMapping(value = "/orders", method = RouteMethod.GET)
    public Future<List<Order>> getAllOrders() {
        LOGGER.debug("Getting all orders");

        return orderService.getAllOrders();
    }

}
