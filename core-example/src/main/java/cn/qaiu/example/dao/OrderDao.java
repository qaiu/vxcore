package cn.qaiu.example.dao;

import cn.qaiu.db.dsl.core.JooqExecutor;
import cn.qaiu.db.dsl.lambda.LambdaDao;
import cn.qaiu.example.entity.Order;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import org.jooq.Field;
import org.jooq.Query;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * 订单数据访问对象 (DAO) - 演示 MyBatis-Plus 风格的 Lambda 查询
 * 
 * 继承 LambdaDao，父类已提供基础 CRUD 功能：
 * - insert(T entity)
 * - update(T entity) 
 * - delete(ID id)
 * - findById(ID id)
 * - findAll()
 * - findByCondition(Condition condition)
 * - count()
 * - lambdaQuery()
 * 
 * 本类只提供个性化的业务查询方法。
 * 
 * @author QAIU
 */
public class OrderDao extends LambdaDao<Order, Long> {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderDao.class);

    /**
     * 默认构造函数
     */
    public OrderDao() {
        super();
    }

    /**
     * 带参数的构造函数
     */
    public OrderDao(JooqExecutor executor) {
        super(executor, Order.class);
    }

    // =================== 个性化业务查询方法 ===================
    
    /**
     * 创建订单
     */
    public Future<Order> createOrder(String orderNo, Long userId, Long productId, Integer quantity, BigDecimal unitPrice) {
        LOGGER.debug("Creating order: orderNo={}, userId={}, productId={}", orderNo, userId, productId);
        
        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setProductId(productId);
        order.setQuantity(quantity);
        order.setUnitPrice(unitPrice);
        order.calculateTotalAmount();
        order.setStatus(Order.OrderStatus.PENDING);
        
        return insert(order).map(optionalOrder -> {
            if (optionalOrder.isPresent()) {
                return optionalOrder.get();
            } else {
                throw new RuntimeException("Failed to create order");
            }
        });
    }
    
    /**
     * 根据订单号查找订单 - 演示 Lambda 查询
     */
    public Future<List<Order>> findByOrderNo(String orderNo) {
        LOGGER.debug("Finding order by orderNo: {}", orderNo);
        
        return lambdaList(lambdaQuery()
                .eq(Order::getOrderNo, orderNo));
    }
    
    /**
     * 根据用户ID查找订单
     */
    public Future<List<Order>> findByUserId(Long userId) {
        LOGGER.debug("Finding orders by userId: {}", userId);
        
        return lambdaList(lambdaQuery()
                .eq(Order::getUserId, userId));
    }
    
    /**
     * 根据商品ID查找订单
     */
    public Future<List<Order>> findByProductId(Long productId) {
        LOGGER.debug("Finding orders by productId: {}", productId);
        
        return lambdaList(lambdaQuery()
                .eq(Order::getProductId, productId));
    }
    
    /**
     * 根据状态查找订单
     */
    public Future<List<Order>> findByStatus(Order.OrderStatus status) {
        LOGGER.debug("Finding orders by status: {}", status);
        
        return lambdaList(lambdaQuery()
                .eq(Order::getStatus, status));
    }
    
    /**
     * 根据金额范围查找订单
     */
    public Future<List<Order>> findByAmountRange(BigDecimal minAmount, BigDecimal maxAmount) {
        LOGGER.debug("Finding orders by amount range: {}-{}", minAmount, maxAmount);
        
        return lambdaList(lambdaQuery()
                .between(Order::getTotalAmount, minAmount, maxAmount));
    }
    
    /**
     * 搜索订单
     */
    public Future<List<Order>> searchOrders(String keyword) {
        LOGGER.debug("Searching orders with keyword: {}", keyword);
        
        return lambdaList(lambdaQuery()
                .like(Order::getOrderNo, "%" + keyword + "%"));
    }
    
    /**
     * 获取订单状态统计
     */
    public Future<JsonObject> getOrderStatusStatistics() {
        LOGGER.debug("Getting order status statistics");
        
        return findAll().map(allOrders -> {
            JsonObject stats = new JsonObject();
            for (Order.OrderStatus status : Order.OrderStatus.values()) {
                long count = allOrders.stream()
                        .filter(order -> order.getStatus() == status)
                        .count();
                stats.put(status.name(), count);
            }
            return stats;
        });
    }
    
    /**
     * 支付订单
     */
    public Future<Boolean> payOrder(Long orderId, String paymentMethod) {
        LOGGER.debug("Paying order: {}", orderId);
        
        return findById(orderId).compose(optionalOrder -> {
            if (optionalOrder.isPresent()) {
                Order order = optionalOrder.get();
                order.pay(paymentMethod);
                return update(order).map(updatedOrder -> updatedOrder.isPresent());
            } else {
                return Future.succeededFuture(false);
            }
        });
    }
    
    /**
     * 发货订单
     */
    public Future<Boolean> shipOrder(Long orderId) {
        LOGGER.debug("Shipping order: {}", orderId);
        
        return findById(orderId).compose(optionalOrder -> {
            if (optionalOrder.isPresent()) {
                Order order = optionalOrder.get();
                order.ship();
                return update(order).map(updatedOrder -> updatedOrder.isPresent());
            } else {
                return Future.succeededFuture(false);
            }
        });
    }
    
    /**
     * 确认收货
     */
    public Future<Boolean> confirmDelivery(Long orderId) {
        LOGGER.debug("Confirming delivery for order: {}", orderId);
        
        return findById(orderId).compose(optionalOrder -> {
            if (optionalOrder.isPresent()) {
                Order order = optionalOrder.get();
                order.confirmDelivery();
                return update(order).map(updatedOrder -> updatedOrder.isPresent());
            } else {
                return Future.succeededFuture(false);
            }
        });
    }
    
    /**
     * 取消订单
     */
    public Future<Boolean> cancelOrder(Long orderId) {
        LOGGER.debug("Cancelling order: {}", orderId);
        
        return findById(orderId).compose(optionalOrder -> {
            if (optionalOrder.isPresent()) {
                Order order = optionalOrder.get();
                order.cancel();
                return update(order).map(updatedOrder -> updatedOrder.isPresent());
            } else {
                return Future.succeededFuture(false);
            }
        });
    }
    
    /**
     * 更新订单状态
     */
    public Future<Boolean> updateOrderStatus(Long orderId, Order.OrderStatus status) {
        LOGGER.debug("Updating order {} status to {}", orderId, status);
        
        return findById(orderId).compose(optionalOrder -> {
            if (optionalOrder.isPresent()) {
                Order order = optionalOrder.get();
                order.setStatus(status);
                return update(order).map(updatedOrder -> updatedOrder.isPresent());
            } else {
                return Future.succeededFuture(false);
            }
        });
    }
    
    /**
     * 批量插入订单
     */
    public Future<Integer> insertBatch(List<Order> orders) {
        LOGGER.debug("Batch inserting {} orders", orders.size());
        // 简化实现：逐个插入
        Future<Void> result = Future.succeededFuture();
        for (Order order : orders) {
            result = result.compose(v -> insert(order).map(opt -> null));
        }
        return result.map(v -> orders.size());
    }
    
    /**
     * Lambda插入（兼容性方法）
     */
    public Future<Optional<Order>> lambdaInsert(Order order) {
        LOGGER.debug("Lambda inserting order: {}", order.getOrderNo());
        return insert(order);
    }
    
    /**
     * 根据时间范围查找订单
     */
    public Future<List<Order>> findByTimeRange(java.time.LocalDateTime startTime, java.time.LocalDateTime endTime) {
        LOGGER.debug("Finding orders by time range: {} - {}", startTime, endTime);
        // 简化实现：使用 findAll 然后过滤
        return findAll().map(allOrders -> 
            allOrders.stream()
                .filter(order -> order.getCreateTime().isAfter(startTime) && order.getCreateTime().isBefore(endTime))
                .collect(java.util.stream.Collectors.toList())
        );
    }
    
    /**
     * 根据ID删除订单
     */
    public Future<Boolean> deleteById(Long id) {
        LOGGER.debug("Deleting order by ID: {}", id);
        return delete(id);
    }

    /**
     * 获取用户订单统计 - 演示聚合查询
     */
    public Future<JsonObject> getUserOrderStatistics(Long userId) {
        LOGGER.debug("Getting order statistics for user: {}", userId);
        
        Field<Long> userIdField = DSL.field("user_id", Long.class);
        Field<BigDecimal> totalAmountField = DSL.field("total_amount", BigDecimal.class);
        
        Query query = DSL.select(
                DSL.count().as("total_orders"),
                DSL.sum(totalAmountField).as("total_amount"),
                DSL.avg(totalAmountField).as("avg_amount"),
                DSL.max(totalAmountField).as("max_amount"),
                DSL.min(totalAmountField).as("min_amount")
        )
        .from(DSL.table(getTableName()))
        .where(userIdField.eq(userId));
        
        return executor.executeQuery(query)
                .map(rows -> {
                    if (rows.size() == 0) {
                        return new JsonObject()
                                .put("totalOrders", 0)
                                .put("totalAmount", 0)
                                .put("avgAmount", 0)
                                .put("maxAmount", 0)
                                .put("minAmount", 0);
                    }
                    
                    var row = rows.iterator().next();
                    return new JsonObject()
                            .put("totalOrders", row.getInteger("total_orders"))
                            .put("totalAmount", row.getBigDecimal("total_amount"))
                            .put("avgAmount", row.getBigDecimal("avg_amount"))
                            .put("maxAmount", row.getBigDecimal("max_amount"))
                            .put("minAmount", row.getBigDecimal("min_amount"));
                });
    }

}
