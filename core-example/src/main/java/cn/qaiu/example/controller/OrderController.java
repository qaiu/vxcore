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

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * 创建订单
     * POST /orders
     */
    @RouteMapping(value = "/orders", method = RouteMethod.POST)
    public Future<JsonResult<?>> createOrder(JsonObject requestBody) {
        LOGGER.info("Creating order: {}", requestBody);

        try {
            Long userId = requestBody.getLong("userId");
            Long productId = requestBody.getLong("productId");
            Integer quantity = requestBody.getInteger("quantity");

            if (userId == null || productId == null || quantity == null) {
                return Future.succeededFuture(JsonResult.error("Missing required fields: userId, productId, quantity"));
            }

            if (quantity <= 0) {
                return Future.succeededFuture(JsonResult.error("Quantity must be greater than 0"));
            }

            return orderService.createOrder(userId, productId, quantity)
                    .map(order -> JsonResult.data("Order created successfully", order.toJson()))
                    .map(result -> (JsonResult<?>) result)
                    .recover(throwable -> {
                        LOGGER.error("Failed to create order", throwable);
                        return Future.succeededFuture(JsonResult.error("Failed to create order: " + throwable.getMessage()));
                    });
        } catch (Exception e) {
            LOGGER.error("Error creating order", e);
            return Future.succeededFuture(JsonResult.error("Error creating order: " + e.getMessage()));
        }
    }

    /**
     * 根据ID获取订单
     * GET /orders/{id}
     */
    @RouteMapping(value = "/orders/:id", method = RouteMethod.GET)
    public Future<JsonResult<?>> getOrderById(Long id) {
        LOGGER.debug("Getting order by ID: {}", id);

        return orderService.getOrderById(id)
                .map(orderOptional -> {
                    if (orderOptional.isPresent()) {
                        return JsonResult.data("Order found", orderOptional.get().toJson());
                    } else {
                        return JsonResult.error("Order not found");
                    }
                })
                .recover(throwable -> {
                    LOGGER.error("Failed to get order", throwable);
                    return Future.succeededFuture(JsonResult.error("Failed to get order: " + throwable.getMessage()));
                });
    }

    /**
     * 根据订单号获取订单
     * GET /orders/search?orderNo={orderNo}
     */
    @RouteMapping(value = "/orders/search", method = RouteMethod.GET)
    public Future<JsonResult<?>> getOrdersByOrderNo(String orderNo) {
        LOGGER.debug("Getting orders by orderNo: {}", orderNo);

        if (orderNo == null || orderNo.trim().isEmpty()) {
            return Future.succeededFuture(JsonResult.error("OrderNo is required"));
        }

        return orderService.getOrdersByOrderNo(orderNo)
                .map(orders -> JsonResult.data("Orders found", orders.stream()
                        .map(Order::toJson)
                        .collect(java.util.stream.Collectors.toList())))
                .map(result -> (JsonResult<?>) result)
                .recover(throwable -> {
                    LOGGER.error("Failed to get orders by orderNo", throwable);
                    return Future.succeededFuture(JsonResult.error("Failed to get orders: " + throwable.getMessage()));
                });
    }

    /**
     * 获取用户的所有订单
     * GET /orders/user/{userId}
     */
    @RouteMapping(value = "/orders/user/:userId", method = RouteMethod.GET)
    public Future<JsonResult<?>> getUserOrders(Long userId) {
        LOGGER.debug("Getting orders for user: {}", userId);

        return orderService.getUserOrders(userId)
                .map(orders -> JsonResult.data("User orders found", orders.stream()
                        .map(Order::toJson)
                        .collect(java.util.stream.Collectors.toList())))
                .recover(throwable -> {
                    LOGGER.error("Failed to get user orders", throwable);
                    return Future.succeededFuture(JsonResult.error("Failed to get user orders: " + throwable.getMessage()));
                });
    }

    /**
     * 获取产品的所有订单
     * GET /orders/product/{productId}
     */
    @RouteMapping(value = "/orders/product/:productId", method = RouteMethod.GET)
    public Future<JsonResult<?>> getProductOrders(Long productId) {
        LOGGER.debug("Getting orders for product: {}", productId);

        return orderService.getProductOrders(productId)
                .map(orders -> JsonResult.data("Product orders found", orders.stream()
                        .map(Order::toJson)
                        .collect(java.util.stream.Collectors.toList())))
                .recover(throwable -> {
                    LOGGER.error("Failed to get product orders", throwable);
                    return Future.succeededFuture(JsonResult.error("Failed to get product orders: " + throwable.getMessage()));
                });
    }

    /**
     * 根据状态获取订单
     * GET /orders/status/{status}
     */
    @RouteMapping(value = "/orders/status/:status", method = RouteMethod.GET)
    public Future<JsonResult<?>> getOrdersByStatus(String status) {
        LOGGER.debug("Getting orders by status: {}", status);

        try {
            Order.OrderStatus orderStatus = Order.OrderStatus.valueOf(status.toUpperCase());
            return orderService.getOrdersByStatus(orderStatus)
                    .map(orders -> JsonResult.data("Orders found", orders.stream()
                            .map(Order::toJson)
                            .collect(java.util.stream.Collectors.toList())))
                    .recover(throwable -> {
                        LOGGER.error("Failed to get orders by status", throwable);
                        return Future.succeededFuture(JsonResult.error("Failed to get orders: " + throwable.getMessage()));
                    });
        } catch (IllegalArgumentException e) {
            return Future.succeededFuture(JsonResult.error("Invalid status: " + status));
        }
    }

    /**
     * 支付订单
     * PUT /orders/{id}/pay
     */
    @RouteMapping(value = "/orders/:id/pay", method = RouteMethod.PUT)
    public Future<JsonResult<?>> payOrder(Long id, JsonObject requestBody) {
        LOGGER.info("Paying order: {}", id);

        try {
            String paymentMethod = requestBody.getString("paymentMethod");
            if (paymentMethod == null || paymentMethod.trim().isEmpty()) {
                return Future.succeededFuture(JsonResult.error("Payment method is required"));
            }

            return orderService.payOrder(id, paymentMethod)
                    .map(success -> {
                        if (success) {
                            return (JsonResult<?>) JsonResult.success("Order paid successfully");
                        } else {
                            return (JsonResult<?>) JsonResult.error("Failed to pay order");
                        }
                    })
                    .recover(throwable -> {
                        LOGGER.error("Failed to pay order", throwable);
                        return Future.succeededFuture(JsonResult.error("Failed to pay order: " + throwable.getMessage()));
                    });
        } catch (Exception e) {
            LOGGER.error("Error paying order", e);
            return Future.succeededFuture(JsonResult.error("Error paying order: " + e.getMessage()));
        }
    }

    /**
     * 发货
     * PUT /orders/{id}/ship
     */
    @RouteMapping(value = "/orders/:id/ship", method = RouteMethod.PUT)
    public Future<JsonResult<?>> shipOrder(Long id) {
        LOGGER.info("Shipping order: {}", id);

        return orderService.shipOrder(id)
                .map(success -> {
                    if (success) {
                        return (JsonResult<?>) JsonResult.success("Order shipped successfully");
                    } else {
                        return (JsonResult<?>) JsonResult.error("Failed to ship order");
                    }
                })
                .recover(throwable -> {
                    LOGGER.error("Failed to ship order", throwable);
                    return Future.succeededFuture(JsonResult.error("Failed to ship order: " + throwable.getMessage()));
                });
    }

    /**
     * 确认收货
     * PUT /orders/{id}/deliver
     */
    @RouteMapping(value = "/orders/:id/deliver", method = RouteMethod.PUT)
    public Future<JsonResult<?>> confirmDelivery(Long id) {
        LOGGER.info("Confirming delivery for order: {}", id);

        return orderService.confirmDelivery(id)
                .map(success -> {
                    if (success) {
                        return (JsonResult<?>) JsonResult.success("Delivery confirmed successfully");
                    } else {
                        return (JsonResult<?>) JsonResult.error("Failed to confirm delivery");
                    }
                })
                .recover(throwable -> {
                    LOGGER.error("Failed to confirm delivery", throwable);
                    return Future.succeededFuture(JsonResult.error("Failed to confirm delivery: " + throwable.getMessage()));
                });
    }

    /**
     * 取消订单
     * PUT /orders/{id}/cancel
     */
    @RouteMapping(value = "/orders/:id/cancel", method = RouteMethod.PUT)
    public Future<JsonResult<?>> cancelOrder(Long id) {
        LOGGER.info("Cancelling order: {}", id);

        return orderService.cancelOrder(id)
                .map(success -> {
                    if (success) {
                        return (JsonResult<?>) JsonResult.success("Order cancelled successfully");
                    } else {
                        return (JsonResult<?>) JsonResult.error("Failed to cancel order");
                    }
                })
                .recover(throwable -> {
                    LOGGER.error("Failed to cancel order", throwable);
                    return Future.succeededFuture(JsonResult.error("Failed to cancel order: " + throwable.getMessage()));
                });
    }

    /**
     * 更新订单状态
     * PUT /orders/{id}/status
     */
    @RouteMapping(value = "/orders/:id/status", method = RouteMethod.PUT)
    public Future<JsonResult<?>> updateOrderStatus(Long id, JsonObject requestBody) {
        LOGGER.info("Updating order {} status", id);

        try {
            String status = requestBody.getString("status");
            if (status == null || status.trim().isEmpty()) {
                return Future.succeededFuture(JsonResult.error("Status is required"));
            }

            Order.OrderStatus orderStatus = Order.OrderStatus.valueOf(status.toUpperCase());
            return orderService.updateOrderStatus(id, orderStatus)
                    .map(success -> {
                        if (success) {
                            return (JsonResult<?>) JsonResult.success("Order status updated successfully");
                        } else {
                            return (JsonResult<?>) JsonResult.error("Failed to update order status");
                        }
                    })
                    .recover(throwable -> {
                        LOGGER.error("Failed to update order status", throwable);
                        return Future.succeededFuture(JsonResult.error("Failed to update order status: " + throwable.getMessage()));
                    });
        } catch (IllegalArgumentException e) {
            return Future.succeededFuture(JsonResult.error("Invalid status: " + requestBody.getString("status")));
        } catch (Exception e) {
            LOGGER.error("Error updating order status", e);
            return Future.succeededFuture(JsonResult.error("Error updating order status: " + e.getMessage()));
        }
    }

    /**
     * 获取订单统计信息
     * GET /orders/statistics
     */
    @RouteMapping(value = "/orders/statistics", method = RouteMethod.GET)
    public Future<JsonResult<?>> getOrderStatistics() {
        LOGGER.debug("Getting order statistics");

        return orderService.getOrderStatistics()
                .map(stats -> JsonResult.data("Order statistics", stats))
                .recover(throwable -> {
                    LOGGER.error("Failed to get order statistics", throwable);
                    return Future.succeededFuture(JsonResult.error("Failed to get order statistics: " + throwable.getMessage()));
                });
    }

    /**
     * 获取用户订单统计
     * GET /orders/user/{userId}/statistics
     */
    @RouteMapping(value = "/orders/user/:userId/statistics", method = RouteMethod.GET)
    public Future<JsonResult<?>> getUserOrderStatistics(Long userId) {
        LOGGER.debug("Getting order statistics for user: {}", userId);

        return orderService.getUserOrderStatistics(userId)
                .map(stats -> JsonResult.data("User order statistics", stats))
                .recover(throwable -> {
                    LOGGER.error("Failed to get user order statistics", throwable);
                    return Future.succeededFuture(JsonResult.error("Failed to get user order statistics: " + throwable.getMessage()));
                });
    }

    /**
     * 搜索订单
     * GET /orders/search?keyword={keyword}
     */
    @RouteMapping(value = "/orders/search", method = RouteMethod.GET)
    public Future<JsonResult<?>> searchOrders(String keyword) {
        LOGGER.debug("Searching orders with keyword: {}", keyword);

        if (keyword == null || keyword.trim().isEmpty()) {
            return Future.succeededFuture(JsonResult.error("Keyword is required"));
        }

        return orderService.searchOrders(keyword)
                .map(orders -> JsonResult.data("Orders found", orders.stream()
                        .map(Order::toJson)
                        .collect(java.util.stream.Collectors.toList())))
                .recover(throwable -> {
                    LOGGER.error("Failed to search orders", throwable);
                    return Future.succeededFuture(JsonResult.error("Failed to search orders: " + throwable.getMessage()));
                });
    }

    /**
     * 删除订单
     * DELETE /orders/{id}
     */
    @RouteMapping(value = "/orders/:id", method = RouteMethod.DELETE)
    public Future<JsonResult<?>> deleteOrder(Long id) {
        LOGGER.info("Deleting order: {}", id);

        return orderService.deleteOrder(id)
                .map(success -> {
                    if (success) {
                        return (JsonResult<?>) JsonResult.success("Order deleted successfully");
                    } else {
                        return (JsonResult<?>) JsonResult.error("Failed to delete order");
                    }
                })
                .recover(throwable -> {
                    LOGGER.error("Failed to delete order", throwable);
                    return Future.succeededFuture(JsonResult.error("Failed to delete order: " + throwable.getMessage()));
                });
    }

    /**
     * 获取所有订单
     * GET /orders
     */
    @RouteMapping(value = "/orders", method = RouteMethod.GET)
    public Future<JsonResult<?>> getAllOrders() {
        LOGGER.debug("Getting all orders");

        return orderService.getAllOrders()
                .map(orders -> JsonResult.data("Orders found", orders.stream()
                        .map(Order::toJson)
                        .collect(java.util.stream.Collectors.toList())))
                .recover(throwable -> {
                    LOGGER.error("Failed to get all orders", throwable);
                    return Future.succeededFuture(JsonResult.error("Failed to get orders: " + throwable.getMessage()));
                });
    }

}
