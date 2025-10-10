package cn.qaiu.example.service;

import cn.qaiu.example.dao.OrderDao;
import cn.qaiu.example.dao.ProductDao;
import cn.qaiu.example.dao.UserDao;
import cn.qaiu.example.entity.Order;
import cn.qaiu.example.entity.Product;
import cn.qaiu.example.entity.User;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 订单服务类
 * 处理订单相关的业务逻辑
 * 
 * @author QAIU
 */
public class OrderService {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderService.class);

    private final OrderDao orderDao;
    private final UserDao userDao;
    private final ProductDao productDao;

    public OrderService(OrderDao orderDao, UserDao userDao, ProductDao productDao) {
        this.orderDao = orderDao;
        this.userDao = userDao;
        this.productDao = productDao;
    }

    /**
     * 创建订单
     */
    public Future<Order> createOrder(Long userId, Long productId, Integer quantity) {
        LOGGER.info("Creating order: userId={}, productId={}, quantity={}", userId, productId, quantity);

        // 验证用户存在
        return userDao.findById(userId)
                .compose(userOptional -> {
                    if (userOptional.isEmpty()) {
                        return Future.failedFuture("User not found: " + userId);
                    }
                    User user = userOptional.get();
                    
                    // 验证产品存在
                    return productDao.findById(productId)
                            .compose(productOptional -> {
                                if (productOptional.isEmpty()) {
                                    return Future.failedFuture("Product not found: " + productId);
                                }
                                Product product = productOptional.get();
                                
                                // 验证库存
                                if (product.getStock() < quantity) {
                                    return Future.failedFuture("Insufficient stock. Available: " + product.getStock() + ", Requested: " + quantity);
                                }
                                
                                // 验证产品状态
                                if (product.getStatus() != Product.ProductStatus.ACTIVE) {
                                    return Future.failedFuture("Product is not active: " + product.getStatus());
                                }
                                
                                // 生成订单号
                                String orderNo = generateOrderNo();
                                
                                // 创建订单
                                return orderDao.createOrder(orderNo, userId, productId, quantity, product.getPrice())
                                        .compose(order -> {
                                            // 减少产品库存
                                            return productDao.updateStock(productId, product.getStock() - quantity)
                                                    .map(updated -> order);
                                        });
                            });
                });
    }

    /**
     * 根据ID获取订单
     */
    public Future<Optional<Order>> getOrderById(Long orderId) {
        LOGGER.debug("Getting order by ID: {}", orderId);
        return orderDao.findById(orderId);
    }

    /**
     * 根据订单号获取订单
     */
    public Future<List<Order>> getOrdersByOrderNo(String orderNo) {
        LOGGER.debug("Getting orders by orderNo: {}", orderNo);
        return orderDao.findByOrderNo(orderNo);
    }

    /**
     * 获取用户的所有订单
     */
    public Future<List<Order>> getUserOrders(Long userId) {
        LOGGER.debug("Getting orders for user: {}", userId);
        return orderDao.findByUserId(userId);
    }

    /**
     * 获取产品的所有订单
     */
    public Future<List<Order>> getProductOrders(Long productId) {
        LOGGER.debug("Getting orders for product: {}", productId);
        return orderDao.findByProductId(productId);
    }

    /**
     * 根据状态获取订单
     */
    public Future<List<Order>> getOrdersByStatus(Order.OrderStatus status) {
        LOGGER.debug("Getting orders by status: {}", status);
        return orderDao.findByStatus(status);
    }

    /**
     * 支付订单
     */
    public Future<Boolean> payOrder(Long orderId, String paymentMethod) {
        LOGGER.info("Paying order: orderId={}, paymentMethod={}", orderId, paymentMethod);

        return orderDao.findById(orderId)
                .compose(orderOptional -> {
                    if (orderOptional.isEmpty()) {
                        return Future.failedFuture("Order not found: " + orderId);
                    }
                    
                    Order order = orderOptional.get();
                    if (order.getStatus() != Order.OrderStatus.PENDING) {
                        return Future.failedFuture("Order cannot be paid. Current status: " + order.getStatus());
                    }
                    
                    return orderDao.payOrder(orderId, paymentMethod);
                });
    }

    /**
     * 发货
     */
    public Future<Boolean> shipOrder(Long orderId) {
        LOGGER.info("Shipping order: {}", orderId);

        return orderDao.findById(orderId)
                .compose(orderOptional -> {
                    if (orderOptional.isEmpty()) {
                        return Future.failedFuture("Order not found: " + orderId);
                    }
                    
                    Order order = orderOptional.get();
                    if (order.getStatus() != Order.OrderStatus.PAID) {
                        return Future.failedFuture("Order cannot be shipped. Current status: " + order.getStatus());
                    }
                    
                    return orderDao.shipOrder(orderId);
                });
    }

    /**
     * 确认收货
     */
    public Future<Boolean> confirmDelivery(Long orderId) {
        LOGGER.info("Confirming delivery for order: {}", orderId);

        return orderDao.findById(orderId)
                .compose(orderOptional -> {
                    if (orderOptional.isEmpty()) {
                        return Future.failedFuture("Order not found: " + orderId);
                    }
                    
                    Order order = orderOptional.get();
                    if (order.getStatus() != Order.OrderStatus.SHIPPED) {
                        return Future.failedFuture("Order cannot be confirmed. Current status: " + order.getStatus());
                    }
                    
                    return orderDao.confirmDelivery(orderId);
                });
    }

    /**
     * 取消订单
     */
    public Future<Boolean> cancelOrder(Long orderId) {
        LOGGER.info("Cancelling order: {}", orderId);

        return orderDao.findById(orderId)
                .compose(orderOptional -> {
                    if (orderOptional.isEmpty()) {
                        return Future.failedFuture("Order not found: " + orderId);
                    }
                    
                    Order order = orderOptional.get();
                    if (order.getStatus() == Order.OrderStatus.DELIVERED || order.getStatus() == Order.OrderStatus.CANCELLED) {
                        return Future.failedFuture("Order cannot be cancelled. Current status: " + order.getStatus());
                    }
                    
                    return orderDao.cancelOrder(orderId)
                            .compose(cancelled -> {
                                if (cancelled && order.getStatus() == Order.OrderStatus.PAID) {
                                    // 如果订单已支付，需要恢复库存
                                    return productDao.findById(order.getProductId())
                                            .compose(productOptional -> {
                                                if (productOptional.isPresent()) {
                                                    Product product = productOptional.get();
                                                    return productDao.updateStock(order.getProductId(), product.getStock() + order.getQuantity());
                                                }
                                                return Future.succeededFuture();
                                            });
                                }
                                return Future.succeededFuture();
                            });
                });
    }

    /**
     * 更新订单状态
     */
    public Future<Boolean> updateOrderStatus(Long orderId, Order.OrderStatus status) {
        LOGGER.info("Updating order {} status to {}", orderId, status);
        return orderDao.updateOrderStatus(orderId, status);
    }

    /**
     * 获取订单统计信息
     */
    public Future<JsonObject> getOrderStatistics() {
        LOGGER.debug("Getting order statistics");
        return orderDao.getOrderStatusStatistics();
    }

    /**
     * 获取用户订单统计
     */
    public Future<JsonObject> getUserOrderStatistics(Long userId) {
        LOGGER.debug("Getting order statistics for user: {}", userId);
        return orderDao.getUserOrderStatistics(userId);
    }

    /**
     * 搜索订单
     */
    public Future<List<Order>> searchOrders(String keyword) {
        LOGGER.debug("Searching orders with keyword: {}", keyword);
        return orderDao.searchOrders(keyword);
    }

    /**
     * 根据金额范围查找订单
     */
    public Future<List<Order>> getOrdersByAmountRange(BigDecimal minAmount, BigDecimal maxAmount) {
        LOGGER.debug("Getting orders by amount range: {}-{}", minAmount, maxAmount);
        return orderDao.findByAmountRange(minAmount, maxAmount);
    }

    /**
     * 根据时间范围查找订单
     */
    public Future<List<Order>> getOrdersByTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        LOGGER.debug("Getting orders by time range: {} - {}", startTime, endTime);
        return orderDao.findByTimeRange(startTime, endTime);
    }

    /**
     * 删除订单
     */
    public Future<Boolean> deleteOrder(Long orderId) {
        LOGGER.info("Deleting order: {}", orderId);
        return orderDao.deleteById(orderId);
    }

    /**
     * 获取所有订单
     */
    public Future<List<Order>> getAllOrders() {
        LOGGER.debug("Getting all orders");
        return orderDao.findAll();
    }

    /**
     * 生成订单号
     */
    private String generateOrderNo() {
        return "ORD" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

}
