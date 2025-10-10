package cn.qaiu.example.dao;

import cn.qaiu.db.dsl.core.JooqExecutor;
import cn.qaiu.example.entity.Order;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(VertxExtension.class)
@DisplayName("订单DAO测试 - 演示 MyBatis-Plus 风格的 Lambda 查询")
class OrderDaoTest {

    private OrderDao orderDao;
    private JooqExecutor executor;

    @BeforeEach
    void setUp(Vertx vertx, VertxTestContext testContext) {
        // 创建 H2 内存数据库配置
        JsonObject config = new JsonObject()
                .put("url", "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE")
                .put("driver_class", "org.h2.Driver")
                .put("user", "sa")
                .put("password", "")
                .put("max_pool_size", 10);

        // 创建连接池
        io.vertx.jdbcclient.JDBCConnectOptions connectOptions = new io.vertx.jdbcclient.JDBCConnectOptions()
                .setJdbcUrl(config.getString("url"))
                .setUser(config.getString("user"))
                .setPassword(config.getString("password"));
        io.vertx.sqlclient.PoolOptions poolOptions = new io.vertx.sqlclient.PoolOptions()
                .setMaxSize(config.getInteger("max_pool_size", 10));
        io.vertx.sqlclient.Pool pool = io.vertx.jdbcclient.JDBCPool.pool(vertx, connectOptions, poolOptions);
        
        // 创建 JooqExecutor
        executor = new JooqExecutor(pool);
        orderDao = new OrderDao(executor);

        // 初始化数据库
        executor.executeUpdate(DSL.query("CREATE TABLE IF NOT EXISTS orders (" +
                "id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
                "order_no VARCHAR(50) NOT NULL UNIQUE, " +
                "user_id BIGINT NOT NULL, " +
                "product_id BIGINT NOT NULL, " +
                "quantity INT NOT NULL DEFAULT 1, " +
                "unit_price DECIMAL(10,2) NOT NULL, " +
                "total_amount DECIMAL(10,2) NOT NULL, " +
                "status VARCHAR(20) NOT NULL DEFAULT 'PENDING', " +
                "payment_method VARCHAR(20), " +
                "payment_time DATETIME, " +
                "shipping_time DATETIME, " +
                "shipping_address TEXT, " +
                "remark TEXT, " +
                "create_time DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                "update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP" +
                ")"))
                .compose(v -> executor.executeUpdate(DSL.query("DELETE FROM orders")))
                .onComplete(testContext.succeedingThenComplete());
    }

    @Nested
    @DisplayName("基本CRUD操作测试")
    class BasicCrudTest {

        @Test
        @DisplayName("创建订单测试")
        void testCreateOrder(VertxTestContext testContext) {
            orderDao.createOrder("ORD001", 1L, 1L, 2, new BigDecimal("100.00"))
                    .onComplete(testContext.succeeding(order -> {
                        assertNotNull(order);
                        assertNotNull(order.getId());
                        assertEquals("ORD001", order.getOrderNo());
                        assertEquals(1L, order.getUserId());
                        assertEquals(1L, order.getProductId());
                        assertEquals(2, order.getQuantity());
                        assertEquals(new BigDecimal("100.00"), order.getUnitPrice());
                        assertEquals(new BigDecimal("200.00"), order.getTotalAmount());
                        assertEquals(Order.OrderStatus.PENDING, order.getStatus());
                        testContext.completeNow();
                    }));
        }

        @Test
        @DisplayName("根据ID查找订单测试")
        void testFindById(VertxTestContext testContext) {
            orderDao.createOrder("ORD001", 1L, 1L, 2, new BigDecimal("100.00"))
                    .compose(order -> orderDao.findById(order.getId()))
                    .onComplete(testContext.succeeding(optional -> {
                        assertTrue(optional.isPresent());
                        Order order = optional.get();
                        assertEquals("ORD001", order.getOrderNo());
                        assertEquals(1L, order.getUserId());
                        testContext.completeNow();
                    }));
        }

        @Test
        @DisplayName("根据订单号查找订单测试")
        void testFindByOrderNo(VertxTestContext testContext) {
            orderDao.createOrder("ORD001", 1L, 1L, 2, new BigDecimal("100.00"))
                    .compose(v -> orderDao.findByOrderNo("ORD001"))
                    .onComplete(testContext.succeeding(orders -> {
                        assertTrue(!orders.isEmpty());
                        Order order = orders.get(0);
                        assertEquals("ORD001", order.getOrderNo());
                        testContext.completeNow();
                    }));
        }

        @Test
        @DisplayName("更新订单状态测试")
        void testUpdateOrderStatus(VertxTestContext testContext) {
            orderDao.createOrder("ORD001", 1L, 1L, 2, new BigDecimal("100.00"))
                    .compose(order -> orderDao.updateOrderStatus(order.getId(), Order.OrderStatus.PAID))
                    .onComplete(testContext.succeeding(result -> {
                        assertTrue(result);
                        testContext.completeNow();
                    }));
        }

        @Test
        @DisplayName("支付订单测试")
        void testPayOrder(VertxTestContext testContext) {
            orderDao.createOrder("ORD001", 1L, 1L, 2, new BigDecimal("100.00"))
                    .compose(order -> orderDao.payOrder(order.getId(), "ALIPAY"))
                    .onComplete(testContext.succeeding(result -> {
                        assertTrue(result);
                        testContext.completeNow();
                    }));
        }

        @Test
        @DisplayName("发货测试")
        void testShipOrder(VertxTestContext testContext) {
            orderDao.createOrder("ORD001", 1L, 1L, 2, new BigDecimal("100.00"))
                    .compose(order -> orderDao.payOrder(order.getId(), "ALIPAY")
                            .compose(v -> orderDao.shipOrder(order.getId())))
                    .onComplete(testContext.succeeding(result -> {
                        assertTrue(result);
                        testContext.completeNow();
                    }));
        }
    }

    @Nested
    @DisplayName("Lambda查询测试 - 演示 MyBatis-Plus 风格")
    class LambdaQueryTest {

        @Test
        @DisplayName("Lambda查询基本功能测试")
        void testLambdaQuery(VertxTestContext testContext) {
            Order order = new Order();
            order.setOrderNo("ORD001");
            order.setUserId(1L);
            order.setProductId(1L);
            order.setQuantity(2);
            order.setUnitPrice(new BigDecimal("100.00"));
            order.setTotalAmount(new BigDecimal("200.00"));
            order.setStatus(Order.OrderStatus.PENDING);

            orderDao.insert(order)
                    .compose(v -> orderDao.lambdaQuery()
                            .eq(Order::getOrderNo, "ORD001")
                            .eq(Order::getUserId, 1L)
                            .list())
                    .onComplete(testContext.succeeding(orders -> {
                        assertEquals(1, orders.size());
                        Order foundOrder = orders.get(0);
                        assertEquals("ORD001", foundOrder.getOrderNo());
                        assertEquals(1L, foundOrder.getUserId());
                        testContext.completeNow();
                    }));
        }

        @Test
        @DisplayName("Lambda查询条件组合测试")
        void testLambdaQueryConditions(VertxTestContext testContext) {
            Order order1 = new Order();
            order1.setOrderNo("ORD001");
            order1.setUserId(1L);
            order1.setProductId(1L);
            order1.setQuantity(2);
            order1.setUnitPrice(new BigDecimal("100.00"));
            order1.setTotalAmount(new BigDecimal("200.00"));
            order1.setStatus(Order.OrderStatus.PENDING);

            Order order2 = new Order();
            order2.setOrderNo("ORD002");
            order2.setUserId(1L);
            order2.setProductId(2L);
            order2.setQuantity(1);
            order2.setUnitPrice(new BigDecimal("200.00"));
            order2.setTotalAmount(new BigDecimal("200.00"));
            order2.setStatus(Order.OrderStatus.PAID);

            orderDao.insert(order1)
                    .compose(v -> orderDao.insert(order2))
                    .compose(v -> orderDao.lambdaQuery()
                            .eq(Order::getUserId, 1L)
                            .eq(Order::getStatus, Order.OrderStatus.PENDING)
                            .list())
                    .onComplete(testContext.succeeding(orders -> {
                        assertEquals(1, orders.size());
                        assertEquals("ORD001", orders.get(0).getOrderNo());
                        testContext.completeNow();
                    }));
        }

        @Test
        @DisplayName("Lambda查询范围条件测试")
        void testLambdaQueryRange(VertxTestContext testContext) {
            Order order1 = new Order();
            order1.setOrderNo("ORD001");
            order1.setUserId(1L);
            order1.setProductId(1L);
            order1.setQuantity(1);
            order1.setUnitPrice(new BigDecimal("50.00"));
            order1.setTotalAmount(new BigDecimal("50.00"));
            order1.setStatus(Order.OrderStatus.PENDING);

            Order order2 = new Order();
            order2.setOrderNo("ORD002");
            order2.setUserId(1L);
            order2.setProductId(2L);
            order2.setQuantity(1);
            order2.setUnitPrice(new BigDecimal("150.00"));
            order2.setTotalAmount(new BigDecimal("150.00"));
            order2.setStatus(Order.OrderStatus.PENDING);

            orderDao.insert(order1)
                    .compose(v -> orderDao.insert(order2))
                    .compose(v -> orderDao.lambdaQuery()
                            .eq(Order::getUserId, 1L)
                            .ge(Order::getTotalAmount, new BigDecimal("100.00"))
                            .list())
                    .onComplete(testContext.succeeding(orders -> {
                        assertEquals(1, orders.size());
                        assertEquals("ORD002", orders.get(0).getOrderNo());
                        testContext.completeNow();
                    }));
        }

        @Test
        @DisplayName("Lambda查询排序测试")
        void testLambdaQueryOrderBy(VertxTestContext testContext) {
            Order order1 = new Order();
            order1.setOrderNo("ORD001");
            order1.setUserId(1L);
            order1.setProductId(1L);
            order1.setQuantity(1);
            order1.setUnitPrice(new BigDecimal("100.00"));
            order1.setTotalAmount(new BigDecimal("100.00"));
            order1.setStatus(Order.OrderStatus.PENDING);

            Order order2 = new Order();
            order2.setOrderNo("ORD002");
            order2.setUserId(1L);
            order2.setProductId(2L);
            order2.setQuantity(1);
            order2.setUnitPrice(new BigDecimal("200.00"));
            order2.setTotalAmount(new BigDecimal("200.00"));
            order2.setStatus(Order.OrderStatus.PENDING);

            orderDao.insert(order1)
                    .compose(v -> orderDao.insert(order2))
                    .compose(v -> orderDao.lambdaQuery()
                            .eq(Order::getUserId, 1L)
                            .orderByDesc(Order::getTotalAmount)
                            .list())
                    .onComplete(testContext.succeeding(orders -> {
                        assertEquals(2, orders.size());
                        assertEquals("ORD002", orders.get(0).getOrderNo());
                        assertEquals("ORD001", orders.get(1).getOrderNo());
                        testContext.completeNow();
                    }));
        }

        @Test
        @DisplayName("Lambda查询分页测试")
        void testLambdaQueryPagination(VertxTestContext testContext) {
            // 创建多个订单
            Future<Void> createOrders = Future.succeededFuture();
            for (int i = 1; i <= 5; i++) {
                final int index = i;
                createOrders = createOrders.compose(v -> {
                    Order order = new Order();
                    order.setOrderNo("ORD" + String.format("%03d", index));
                    order.setUserId(1L);
                    order.setProductId((long) index);
                    order.setQuantity(1);
                    order.setUnitPrice(new BigDecimal("100.00"));
                    order.setTotalAmount(new BigDecimal("100.00"));
                    order.setStatus(Order.OrderStatus.PENDING);
                    return orderDao.insert(order).map(o -> null);
                });
            }

            createOrders.compose(v -> orderDao.lambdaQuery()
                            .eq(Order::getUserId, 1L)
                            .orderByAsc(Order::getOrderNo)
                            .list())
                    .onComplete(testContext.succeeding(orders -> {
                        assertEquals(5, orders.size());
                        assertEquals("ORD001", orders.get(0).getOrderNo());
                        assertEquals("ORD002", orders.get(1).getOrderNo());
                        assertEquals("ORD003", orders.get(2).getOrderNo());
                        testContext.completeNow();
                    }));
        }

        @Test
        @DisplayName("Lambda查询计数测试")
        void testLambdaQueryCount(VertxTestContext testContext) {
            Order order1 = new Order();
            order1.setOrderNo("ORD001");
            order1.setUserId(1L);
            order1.setProductId(1L);
            order1.setQuantity(1);
            order1.setUnitPrice(new BigDecimal("100.00"));
            order1.setTotalAmount(new BigDecimal("100.00"));
            order1.setStatus(Order.OrderStatus.PENDING);

            Order order2 = new Order();
            order2.setOrderNo("ORD002");
            order2.setUserId(1L);
            order2.setProductId(2L);
            order2.setQuantity(1);
            order2.setUnitPrice(new BigDecimal("200.00"));
            order2.setTotalAmount(new BigDecimal("200.00"));
            order2.setStatus(Order.OrderStatus.PAID);

            orderDao.insert(order1)
                    .compose(v -> orderDao.insert(order2))
                    .compose(v -> orderDao.lambdaQuery()
                            .eq(Order::getUserId, 1L)
                            .count())
                    .onComplete(testContext.succeeding(count -> {
                        assertEquals(2L, count);
                        testContext.completeNow();
                    }));
        }

        @Test
        @DisplayName("Lambda查询聚合测试")
        void testLambdaQueryAggregation(VertxTestContext testContext) {
            Order order1 = new Order();
            order1.setOrderNo("ORD001");
            order1.setUserId(1L);
            order1.setProductId(1L);
            order1.setQuantity(1);
            order1.setUnitPrice(new BigDecimal("100.00"));
            order1.setTotalAmount(new BigDecimal("100.00"));
            order1.setStatus(Order.OrderStatus.PENDING);

            Order order2 = new Order();
            order2.setOrderNo("ORD002");
            order2.setUserId(1L);
            order2.setProductId(2L);
            order2.setQuantity(1);
            order2.setUnitPrice(new BigDecimal("200.00"));
            order2.setTotalAmount(new BigDecimal("200.00"));
            order2.setStatus(Order.OrderStatus.PENDING);

            orderDao.insert(order1)
                    .compose(v -> orderDao.insert(order2))
                    .compose(v -> orderDao.lambdaQuery()
                            .eq(Order::getUserId, 1L)
                            .eq(Order::getStatus, Order.OrderStatus.PENDING)
                            .list())
                    .map(orders -> {
                        return orders.stream()
                                .map(Order::getTotalAmount)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                    })
                    .onComplete(testContext.succeeding(total -> {
                        assertEquals(new BigDecimal("300.00"), total);
                        testContext.completeNow();
                    }));
        }
    }

    @Nested
    @DisplayName("查询操作测试")
    class QueryTest {

        @Test
        @DisplayName("根据用户ID查找订单测试")
        void testFindByUserId(VertxTestContext testContext) {
            orderDao.createOrder("ORD001", 1L, 1L, 2, new BigDecimal("100.00"))
                    .compose(v -> orderDao.createOrder("ORD002", 1L, 2L, 1, new BigDecimal("200.00")))
                    .compose(v -> orderDao.findByUserId(1L))
                    .onComplete(testContext.succeeding(orders -> {
                        assertEquals(2, orders.size());
                        orders.forEach(order -> assertEquals(1L, order.getUserId()));
                        testContext.completeNow();
                    }));
        }

        @Test
        @DisplayName("根据商品ID查找订单测试")
        void testFindByProductId(VertxTestContext testContext) {
            orderDao.createOrder("ORD001", 1L, 1L, 2, new BigDecimal("100.00"))
                    .compose(v -> orderDao.createOrder("ORD002", 2L, 1L, 1, new BigDecimal("200.00")))
                    .compose(v -> orderDao.findByProductId(1L))
                    .onComplete(testContext.succeeding(orders -> {
                        assertEquals(2, orders.size());
                        orders.forEach(order -> assertEquals(1L, order.getProductId()));
                        testContext.completeNow();
                    }));
        }

        @Test
        @DisplayName("根据订单状态查找订单测试")
        void testFindByStatus(VertxTestContext testContext) {
            orderDao.createOrder("ORD001", 1L, 1L, 2, new BigDecimal("100.00"))
                    .compose(v -> orderDao.createOrder("ORD002", 2L, 2L, 1, new BigDecimal("200.00")))
                    .compose(v -> orderDao.findByStatus(Order.OrderStatus.PENDING))
                    .onComplete(testContext.succeeding(orders -> {
                        assertEquals(2, orders.size());
                        orders.forEach(order -> assertEquals(Order.OrderStatus.PENDING, order.getStatus()));
                        testContext.completeNow();
                    }));
        }

        @Test
        @DisplayName("根据时间范围查找订单测试")
        void testFindByTimeRange(VertxTestContext testContext) {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime startTime = now.minusHours(1);
            LocalDateTime endTime = now.plusHours(1);

            orderDao.createOrder("ORD001", 1L, 1L, 2, new BigDecimal("100.00"))
                    .compose(v -> orderDao.findByTimeRange(startTime, endTime))
                    .onComplete(testContext.succeeding(orders -> {
                        assertEquals(1, orders.size());
                        assertEquals("ORD001", orders.get(0).getOrderNo());
                        testContext.completeNow();
                    }));
        }

        @Test
        @DisplayName("根据金额范围查找订单测试")
        void testFindByAmountRange(VertxTestContext testContext) {
            orderDao.createOrder("ORD001", 1L, 1L, 2, new BigDecimal("100.00"))
                    .compose(v -> orderDao.createOrder("ORD002", 2L, 2L, 1, new BigDecimal("200.00")))
                    .compose(v -> orderDao.findByAmountRange(new BigDecimal("150.00"), new BigDecimal("250.00")))
                    .onComplete(testContext.succeeding(orders -> {
                        assertEquals(1, orders.size());
                        assertEquals("ORD002", orders.get(0).getOrderNo());
                        testContext.completeNow();
                    }));
        }
    }

    @Nested
    @DisplayName("统计操作测试")
    class StatisticsTest {

        @Test
        @DisplayName("获取用户订单统计测试")
        void testGetUserOrderStatistics(VertxTestContext testContext) {
            orderDao.createOrder("ORD001", 1L, 1L, 2, new BigDecimal("100.00"))
                    .compose(v -> orderDao.createOrder("ORD002", 1L, 2L, 1, new BigDecimal("200.00")))
                    .compose(v -> orderDao.getUserOrderStatistics(1L))
                    .onComplete(testContext.succeeding(statistics -> {
                        assertEquals(2, statistics.getInteger("totalOrders"));
                        assertEquals(new BigDecimal("300.00"), new BigDecimal(statistics.getString("totalAmount")));
                        assertEquals(new BigDecimal("150.00"), new BigDecimal(statistics.getString("avgAmount")));
                        assertEquals(new BigDecimal("200.00"), new BigDecimal(statistics.getString("maxAmount")));
                        assertEquals(new BigDecimal("100.00"), new BigDecimal(statistics.getString("minAmount")));
                        testContext.completeNow();
                    }));
        }

        @Test
        @DisplayName("获取订单状态统计测试")
        void testGetOrderStatusStatistics(VertxTestContext testContext) {
            orderDao.createOrder("ORD001", 1L, 1L, 2, new BigDecimal("100.00"))
                    .compose(v -> orderDao.createOrder("ORD002", 2L, 2L, 1, new BigDecimal("200.00")))
                    .compose(v -> orderDao.getOrderStatusStatistics())
                    .onComplete(testContext.succeeding(statistics -> {
                        assertNotNull(statistics);
                        assertEquals("PENDING", statistics.getString("status"));
                        assertEquals(2, statistics.getInteger("count"));
                        assertEquals(new BigDecimal("300.00"), new BigDecimal(statistics.getString("totalAmount")));
                        testContext.completeNow();
                    }));
        }
    }

    @Nested
    @DisplayName("分页操作测试")
    class PaginationTest {

        @Test
        @DisplayName("分页查询订单测试")
        void testFindPage(VertxTestContext testContext) {
            // 创建多个订单
            Future<Void> createOrders = Future.succeededFuture();
            for (int i = 1; i <= 5; i++) {
                final int index = i;
                createOrders = createOrders.compose(v -> {
                    Order order = new Order();
                    order.setOrderNo("ORD" + String.format("%03d", index));
                    order.setUserId(1L);
                    order.setProductId((long) index);
                    order.setQuantity(1);
                    order.setUnitPrice(new BigDecimal("100.00"));
                    order.setTotalAmount(new BigDecimal("100.00"));
                    order.setStatus(Order.OrderStatus.PENDING);
                    return orderDao.insert(order).map(o -> null);
                });
            }

            createOrders.compose(v -> orderDao.findAll())
                    .onComplete(testContext.succeeding(orders -> {
                        assertEquals(3, orders.size());
                        testContext.completeNow();
                    }));
        }
    }

    @Nested
    @DisplayName("搜索操作测试")
    class SearchTest {

        @Test
        @DisplayName("搜索订单测试")
        void testSearchOrders(VertxTestContext testContext) {
            Order order = new Order();
            order.setOrderNo("ORD001");
            order.setUserId(1L);
            order.setProductId(1L);
            order.setQuantity(1);
            order.setUnitPrice(new BigDecimal("100.00"));
            order.setTotalAmount(new BigDecimal("100.00"));
            order.setStatus(Order.OrderStatus.PENDING);
            order.setRemark("测试订单");

            orderDao.insert(order)
                    .compose(v -> orderDao.searchOrders("ORD001"))
                    .onComplete(testContext.succeeding(orders -> {
                        assertEquals(1, orders.size());
                        assertEquals("ORD001", orders.get(0).getOrderNo());
                        testContext.completeNow();
                    }));
        }
    }

    @Nested
    @DisplayName("批量操作测试")
    class BatchOperationTest {

        @Test
        @DisplayName("批量插入订单测试")
        void testInsertBatch(VertxTestContext testContext) {
            Order order1 = new Order();
            order1.setOrderNo("ORD001");
            order1.setUserId(1L);
            order1.setProductId(1L);
            order1.setQuantity(1);
            order1.setUnitPrice(new BigDecimal("100.00"));
            order1.setTotalAmount(new BigDecimal("100.00"));
            order1.setStatus(Order.OrderStatus.PENDING);

            Order order2 = new Order();
            order2.setOrderNo("ORD002");
            order2.setUserId(2L);
            order2.setProductId(2L);
            order2.setQuantity(1);
            order2.setUnitPrice(new BigDecimal("200.00"));
            order2.setTotalAmount(new BigDecimal("200.00"));
            order2.setStatus(Order.OrderStatus.PENDING);

            orderDao.insertBatch(List.of(order1, order2))
                    .onComplete(testContext.succeeding(count -> {
                        assertEquals(2, count);
                        testContext.completeNow();
                    }));
        }
    }

    @Nested
    @DisplayName("删除操作测试")
    class DeleteTest {

        @Test
        @DisplayName("根据ID删除订单测试")
        void testDeleteById(VertxTestContext testContext) {
            orderDao.createOrder("ORD001", 1L, 1L, 2, new BigDecimal("100.00"))
                    .compose(order -> orderDao.deleteById(order.getId()))
                    .onComplete(testContext.succeeding(result -> {
                        assertTrue(result);
                        testContext.completeNow();
                    }));
        }
    }
}
