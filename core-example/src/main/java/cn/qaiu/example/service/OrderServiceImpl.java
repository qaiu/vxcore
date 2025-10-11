package cn.qaiu.example.service;

import cn.qaiu.db.dsl.core.JooqExecutor;
import cn.qaiu.db.dsl.lambda.JServiceImpl;
import cn.qaiu.db.dsl.lambda.LambdaPageResult;
import cn.qaiu.example.entity.Order;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单服务实现类
 * 演示 JService 的使用，支持 DI 注入
 * 
 * @author <a href="https://qaiu.top">QAIU</a>
 */
@Singleton
public class OrderServiceImpl extends JServiceImpl<Order, Long> implements OrderService {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderServiceImpl.class);

    /**
     * 构造函数 - 使用 DI 注入 JooqExecutor
     * 
     * @param executor JooqExecutor 实例（由 DI 容器自动注入）
     */
    @Inject
    public OrderServiceImpl(JooqExecutor executor) {
        super(executor, Order.class);
        LOGGER.info("OrderServiceImpl initialized with DI injection");
    }

    @Override
    public Future<List<Order>> findByUserId(Long userId) {
        LOGGER.info("根据用户ID查找订单: {}", userId);
        return lambdaList(lambdaQuery()
                .eq(Order::getUserId, userId)
                .orderByDesc(Order::getCreateTime));
    }

    @Override
    public Future<List<Order>> findByStatus(String status) {
        LOGGER.info("根据订单状态查找订单: {}", status);
        return lambdaList(lambdaQuery()
                .eq(Order::getStatus, status)
                .orderByDesc(Order::getCreateTime));
    }

    @Override
    public Future<List<Order>> findByTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        LOGGER.info("根据时间范围查找订单: {} - {}", startTime, endTime);
        return lambdaList(lambdaQuery()
                .ge(Order::getCreateTime, startTime)
                .le(Order::getCreateTime, endTime)
                .orderByDesc(Order::getCreateTime));
    }

    @Override
    public Future<LambdaPageResult<Order>> findUserOrders(Long userId, long page, long size) {
        LOGGER.info("分页查询用户订单: {}, 页码: {}, 每页: {}", userId, page, size);
        return lambdaPage(lambdaQuery()
                .eq(Order::getUserId, userId)
                .orderByDesc(Order::getCreateTime), page, size);
    }

    @Override
    public Future<Long> countUserOrders(Long userId) {
        LOGGER.info("统计用户订单数量: {}", userId);
        return lambdaCount(lambdaQuery()
                .eq(Order::getUserId, userId));
    }

    @Override
    public Future<BigDecimal> calculateUserTotalAmount(Long userId) {
        LOGGER.info("计算用户订单总金额: {}", userId);
        return lambdaList(lambdaQuery()
                .eq(Order::getUserId, userId)
                .eq(Order::getStatus, Order.OrderStatus.DELIVERED))
                .map(orders -> {
                    BigDecimal total = BigDecimal.ZERO;
                    for (Order order : orders) {
                        if (order.getTotalAmount() != null) {
                            total = total.add(order.getTotalAmount());
                        }
                    }
                    return total;
                });
    }

    @Override
    public Future<List<Order>> getPendingOrders() {
        LOGGER.info("获取待处理订单");
        return lambdaList(lambdaQuery()
                .eq(Order::getStatus, Order.OrderStatus.PENDING)
                .orderByAsc(Order::getCreateTime));
    }

    @Override
    public Future<Boolean> updateOrderStatus(Long orderId, String status) {
        LOGGER.info("更新订单状态: {} -> {}", orderId, status);
        return getById(orderId)
                .compose(optional -> {
                    if (optional.isPresent()) {
                        Order order = optional.get();
                        order.setStatus(Order.OrderStatus.valueOf(status));
                        order.setUpdateTime(LocalDateTime.now());
                        return updateById(order)
                                .map(updatedOrder -> updatedOrder.isPresent());
                    } else {
                        return Future.succeededFuture(false);
                    }
                });
    }

    // 实现 Controller 中调用的方法
    @Override
    public Future<Order> createOrder(Long userId, Long productId, Integer quantity) {
        LOGGER.info("创建订单: userId={}, productId={}, quantity={}", userId, productId, quantity);
        Order order = new Order();
        order.setUserId(userId);
        order.setProductId(productId);
        order.setQuantity(quantity);
        order.setStatus(Order.OrderStatus.PENDING);
        order.setCreateTime(LocalDateTime.now());
        return save(order)
                .map(optional -> {
                    if (optional.isPresent()) {
                        return optional.get();
                    } else {
                        throw new RuntimeException("Failed to create order");
                    }
                });
    }

    @Override
    public Future<Order> getOrderById(Long id) {
        LOGGER.info("根据ID获取订单: {}", id);
        return getById(id).map(optional -> {
            if (optional.isPresent()) {
                return optional.get();
            } else {
                throw new RuntimeException("Order not found with id: " + id);
            }
        });
    }

    @Override
    public Future<List<Order>> getOrdersByOrderNo(String orderNo) {
        LOGGER.info("根据订单号获取订单: {}", orderNo);
        return lambdaList(lambdaQuery().eq(Order::getOrderNo, orderNo));
    }

    @Override
    public Future<List<Order>> getUserOrders(Long userId) {
        return findByUserId(userId);
    }

    @Override
    public Future<List<Order>> getProductOrders(Long productId) {
        LOGGER.info("获取商品订单: {}", productId);
        return lambdaList(lambdaQuery().eq(Order::getProductId, productId));
    }

    @Override
    public Future<List<Order>> getOrdersByStatus(Order.OrderStatus status) {
        LOGGER.info("根据状态获取订单: {}", status);
        return lambdaList(lambdaQuery().eq(Order::getStatus, status));
    }

    @Override
    public Future<Boolean> payOrder(Long orderId, String paymentMethod) {
        LOGGER.info("支付订单: {} -> {}", orderId, paymentMethod);
        return updateOrderStatus(orderId, Order.OrderStatus.PAID.name());
    }

    @Override
    public Future<Boolean> shipOrder(Long orderId) {
        LOGGER.info("发货: {}", orderId);
        return updateOrderStatus(orderId, Order.OrderStatus.SHIPPED.name());
    }

    @Override
    public Future<Boolean> confirmDelivery(Long orderId) {
        LOGGER.info("确认收货: {}", orderId);
        return updateOrderStatus(orderId, Order.OrderStatus.DELIVERED.name());
    }

    @Override
    public Future<Boolean> cancelOrder(Long orderId) {
        LOGGER.info("取消订单: {}", orderId);
        return updateOrderStatus(orderId, Order.OrderStatus.CANCELLED.name());
    }

    @Override
    public Future<JsonObject> getOrderStatistics() {
        LOGGER.info("获取订单统计信息");
        return count().map(totalCount -> {
            JsonObject stats = new JsonObject();
            stats.put("totalOrders", totalCount);
            return stats;
        });
    }

    @Override
    public Future<JsonObject> getUserOrderStatistics(Long userId) {
        LOGGER.info("获取用户订单统计信息: {}", userId);
        return countUserOrders(userId).map(count -> {
            JsonObject stats = new JsonObject();
            stats.put("userOrderCount", count);
            return stats;
        });
    }

    @Override
    public Future<List<Order>> searchOrders(String keyword) {
        LOGGER.info("搜索订单: {}", keyword);
        return lambdaList(lambdaQuery()
                .like(Order::getOrderNo, "%" + keyword + "%")
                .orderByDesc(Order::getCreateTime));
    }

    @Override
    public Future<Boolean> deleteOrder(Long orderId) {
        LOGGER.info("删除订单: {}", orderId);
        return removeById(orderId);
    }

    @Override
    public Future<List<Order>> getAllOrders() {
        LOGGER.info("获取所有订单");
        return findAll();
    }
}
