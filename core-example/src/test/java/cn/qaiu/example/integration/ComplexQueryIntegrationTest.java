package cn.qaiu.example.integration;

import cn.qaiu.example.dao.OrderDetailDao;
import cn.qaiu.example.entity.Order;
import cn.qaiu.example.entity.OrderDetail;
import cn.qaiu.example.service.OrderDetailService;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 表连接复杂查询集成测试
 * 演示三层结构：Entity -> DAO -> Service
 * 
 * @author QAIU
 */
@ExtendWith(VertxExtension.class)
@DisplayName("表连接复杂查询集成测试")
class ComplexQueryIntegrationTest {

    private OrderDetailDao orderDetailDao;
    private OrderDetailService orderDetailService;
    private cn.qaiu.db.dsl.core.JooqExecutor executor;

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
        executor = new cn.qaiu.db.dsl.core.JooqExecutor(pool);
        orderDetailDao = new OrderDetailDao(executor);
        orderDetailService = new OrderDetailService(orderDetailDao);

        // 初始化数据库表
        executor.executeUpdate(DSL.query("CREATE TABLE IF NOT EXISTS dsl_user (" +
                "id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
                "name VARCHAR(50) NOT NULL, " +
                "email VARCHAR(100) NOT NULL, " +
                "password VARCHAR(255) NOT NULL, " +
                "age INT DEFAULT 0, " +
                "status VARCHAR(20) DEFAULT 'ACTIVE', " +
                "balance DECIMAL(10,2) DEFAULT 0.00, " +
                "email_verified BOOLEAN DEFAULT FALSE, " +
                "bio TEXT, " +
                "create_time DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                "update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP" +
                ")"))
                .compose(v -> executor.executeUpdate(DSL.query("CREATE TABLE IF NOT EXISTS orders (" +
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
                        ")")))
                .compose(v -> executor.executeUpdate(DSL.query("CREATE TABLE IF NOT EXISTS order_details (" +
                        "id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
                        "order_id BIGINT NOT NULL, " +
                        "product_id BIGINT NOT NULL, " +
                        "product_name VARCHAR(100) NOT NULL, " +
                        "unit_price DECIMAL(10,2) NOT NULL, " +
                        "quantity INT NOT NULL DEFAULT 1, " +
                        "subtotal DECIMAL(10,2) NOT NULL, " +
                        "category VARCHAR(50), " +
                        "description TEXT, " +
                        "create_time DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                        "update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP" +
                        ")")))
                .compose(v -> executor.executeUpdate(DSL.query("CREATE TABLE IF NOT EXISTS products (" +
                        "id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
                        "name VARCHAR(100) NOT NULL, " +
                        "description TEXT, " +
                        "category VARCHAR(50), " +
                        "price DECIMAL(10,2) NOT NULL, " +
                        "stock INT DEFAULT 0, " +
                        "status VARCHAR(20) DEFAULT 'ACTIVE', " +
                        "create_time DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                        "update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP" +
                        ")")))
                .compose(v -> executor.executeUpdate(DSL.query("DELETE FROM order_details")))
                .compose(v -> executor.executeUpdate(DSL.query("DELETE FROM orders")))
                .compose(v -> executor.executeUpdate(DSL.query("DELETE FROM products")))
                .compose(v -> executor.executeUpdate(DSL.query("DELETE FROM dsl_user")))
                .onComplete(testContext.succeedingThenComplete());
    }

    @Nested
    @DisplayName("表连接查询测试")
    class JoinQueryTest {

        @Test
        @DisplayName("三表连接查询测试 - 订单详情包含用户信息")
        void testThreeTableJoinQuery(VertxTestContext testContext) {
            // 准备测试数据
            executor.executeUpdate(DSL.query("INSERT INTO dsl_user (id, name, email, password, age, status) VALUES (1, '张三', 'zhangsan@example.com', 'password123', 25, 'ACTIVE')"))
                    .compose(v -> executor.executeUpdate(DSL.query("INSERT INTO orders (id, order_no, user_id, product_id, quantity, unit_price, total_amount, status) VALUES (1, 'ORD001', 1, 1, 2, 100.00, 200.00, 'PAID')")))
                    .compose(v -> executor.executeUpdate(DSL.query("INSERT INTO order_details (id, order_id, product_id, product_name, unit_price, quantity, subtotal, category) VALUES (1, 1, 1, '测试商品1', 100.00, 2, 200.00, '电子产品')")))
                    .compose(v -> orderDetailDao.findOrderWithDetailsAndUser(1L))
                    .onComplete(testContext.succeeding(result -> {
                        assertNotNull(result);
                        assertFalse(result.isEmpty());
                        
                        JsonObject firstRecord = result.get(0);
                        assertEquals(1L, firstRecord.getLong("orderId"));
                        assertEquals("ORD001", firstRecord.getString("orderNo"));
                        assertEquals(1L, firstRecord.getLong("userId"));
                        assertEquals("张三", firstRecord.getString("userName"));
                        assertEquals("zhangsan@example.com", firstRecord.getString("userEmail"));
                        assertEquals(1L, firstRecord.getLong("detailId"));
                        assertEquals("测试商品1", firstRecord.getString("productName"));
                        assertEquals("电子产品", firstRecord.getString("category"));
                        
                        testContext.completeNow();
                    }));
        }

        @Test
        @DisplayName("多表连接聚合查询测试 - 用户订单统计")
        void testMultiTableJoinAggregationQuery(VertxTestContext testContext) {
            // 准备测试数据
            executor.executeUpdate(DSL.query("INSERT INTO dsl_user (id, name, email, password, age, status) VALUES (1, '李四', 'lisi@example.com', 'password123', 30, 'ACTIVE')"))
                    .compose(v -> executor.executeUpdate(DSL.query("INSERT INTO orders (id, order_no, user_id, product_id, quantity, unit_price, total_amount, status) VALUES (1, 'ORD001', 1, 1, 2, 100.00, 200.00, 'PAID')")))
                    .compose(v -> executor.executeUpdate(DSL.query("INSERT INTO orders (id, order_no, user_id, product_id, quantity, unit_price, total_amount, status) VALUES (2, 'ORD002', 1, 2, 1, 150.00, 150.00, 'SHIPPED')")))
                    .compose(v -> executor.executeUpdate(DSL.query("INSERT INTO order_details (id, order_id, product_id, product_name, unit_price, quantity, subtotal, category) VALUES (1, 1, 1, '商品1', 100.00, 2, 200.00, '电子产品')")))
                    .compose(v -> executor.executeUpdate(DSL.query("INSERT INTO order_details (id, order_id, product_id, product_name, unit_price, quantity, subtotal, category) VALUES (2, 2, 2, '商品2', 150.00, 1, 150.00, '服装')")))
                    .compose(v -> orderDetailDao.getUserOrderStatisticsWithDetails(1L))
                    .onComplete(testContext.succeeding(result -> {
                        assertNotNull(result);
                        assertEquals(1L, result.getLong("userId"));
                        assertEquals("李四", result.getString("userName"));
                        assertEquals("lisi@example.com", result.getString("userEmail"));
                        assertEquals(2, result.getInteger("totalOrders"));
                        assertEquals(2, result.getInteger("totalItems"));
                        assertEquals(3, result.getInteger("totalQuantity"));
                        
                        testContext.completeNow();
                    }));
        }

        @Test
        @DisplayName("分组统计查询测试 - 商品销售统计按分类")
        void testGroupByStatisticsQuery(VertxTestContext testContext) {
            // 准备测试数据
            executor.executeUpdate(DSL.query("INSERT INTO orders (id, order_no, user_id, product_id, quantity, unit_price, total_amount, status) VALUES (1, 'ORD001', 1, 1, 2, 100.00, 200.00, 'PAID')"))
                    .compose(v -> executor.executeUpdate(DSL.query("INSERT INTO orders (id, order_no, user_id, product_id, quantity, unit_price, total_amount, status) VALUES (2, 'ORD002', 2, 2, 1, 150.00, 150.00, 'SHIPPED')")))
                    .compose(v -> executor.executeUpdate(DSL.query("INSERT INTO order_details (id, order_id, product_id, product_name, unit_price, quantity, subtotal, category) VALUES (1, 1, 1, '手机', 100.00, 2, 200.00, '电子产品')")))
                    .compose(v -> executor.executeUpdate(DSL.query("INSERT INTO order_details (id, order_id, product_id, product_name, unit_price, quantity, subtotal, category) VALUES (2, 2, 2, '衣服', 150.00, 1, 150.00, '服装')")))
                    .compose(v -> executor.executeUpdate(DSL.query("INSERT INTO order_details (id, order_id, product_id, product_name, unit_price, quantity, subtotal, category) VALUES (3, 1, 3, '耳机', 50.00, 1, 50.00, '电子产品')")))
                    .compose(v -> orderDetailDao.getProductSalesStatisticsByCategory())
                    .onComplete(testContext.succeeding(result -> {
                        assertNotNull(result);
                        assertFalse(result.isEmpty());
                        
                        // 检查电子产品分类统计
                        JsonObject electronics = result.stream()
                                .filter(item -> "电子产品".equals(item.getString("category")))
                                .findFirst()
                                .orElse(null);
                        assertNotNull(electronics);
                        assertEquals(2, electronics.getInteger("productCount"));
                        assertEquals(2, electronics.getInteger("salesCount"));
                        assertEquals(3, electronics.getInteger("totalQuantity"));
                        
                        // 检查服装分类统计
                        JsonObject clothing = result.stream()
                                .filter(item -> "服装".equals(item.getString("category")))
                                .findFirst()
                                .orElse(null);
                        assertNotNull(clothing);
                        assertEquals(1, clothing.getInteger("productCount"));
                        assertEquals(1, clothing.getInteger("salesCount"));
                        assertEquals(1, clothing.getInteger("totalQuantity"));
                        
                        testContext.completeNow();
                    }));
        }
    }

    @Nested
    @DisplayName("服务层业务逻辑测试")
    class ServiceLayerTest {

        @Test
        @DisplayName("获取订单完整信息服务测试")
        void testGetOrderWithDetailsAndUserService(VertxTestContext testContext) {
            // 准备测试数据
            executor.executeUpdate(DSL.query("INSERT INTO dsl_user (id, name, email, password, age, status) VALUES (1, '王五', 'wangwu@example.com', 'password123', 28, 'ACTIVE')"))
                    .compose(v -> executor.executeUpdate(DSL.query("INSERT INTO orders (id, order_no, user_id, product_id, quantity, unit_price, total_amount, status, payment_method) VALUES (1, 'ORD001', 1, 1, 2, 100.00, 200.00, 'PAID', 'ALIPAY')")))
                    .compose(v -> executor.executeUpdate(DSL.query("INSERT INTO order_details (id, order_id, product_id, product_name, unit_price, quantity, subtotal, category) VALUES (1, 1, 1, '笔记本电脑', 100.00, 2, 200.00, '电子产品')")))
                    .compose(v -> orderDetailService.getOrderWithDetailsAndUser(1L))
                    .onComplete(testContext.succeeding(result -> {
                        assertNotNull(result);
                        assertEquals(1L, result.getOrderId());
                        assertEquals("ORD001", result.getOrderNo());
                        assertEquals(1L, result.getUserId());
                        assertEquals("王五", result.getUserName());
                        assertEquals("wangwu@example.com", result.getUserEmail());
                        assertEquals(Order.OrderStatus.PAID, result.getOrderStatus());
                        assertEquals("ALIPAY", result.getPaymentMethod());
                        
                        assertNotNull(result.getOrderDetails());
                        assertEquals(1, result.getOrderDetails().size());
                        OrderDetail detail = result.getOrderDetails().get(0);
                        assertEquals("笔记本电脑", detail.getProductName());
                        assertEquals("电子产品", detail.getCategory());
                        assertEquals(2, detail.getQuantity());
                        assertEquals(new BigDecimal("200.00"), detail.getSubtotal());
                        
                        assertEquals(1, result.getTotalItems());
                        assertEquals(2, result.getTotalQuantity());
                        
                        testContext.completeNow();
                    }));
        }

        @Test
        @DisplayName("用户消费分析服务测试")
        void testUserConsumptionAnalysisService(VertxTestContext testContext) {
            // 准备测试数据
            executor.executeUpdate(DSL.query("INSERT INTO dsl_user (id, name, email, password, age, status) VALUES (1, '赵六', 'zhaoliu@example.com', 'password123', 35, 'ACTIVE')"))
                    .compose(v -> executor.executeUpdate(DSL.query("INSERT INTO orders (id, order_no, user_id, product_id, quantity, unit_price, total_amount, status) VALUES (1, 'ORD001', 1, 1, 2, 100.00, 200.00, 'PAID')")))
                    .compose(v -> executor.executeUpdate(DSL.query("INSERT INTO orders (id, order_no, user_id, product_id, quantity, unit_price, total_amount, status) VALUES (2, 'ORD002', 1, 2, 1, 150.00, 150.00, 'SHIPPED')")))
                    .compose(v -> executor.executeUpdate(DSL.query("INSERT INTO order_details (id, order_id, product_id, product_name, unit_price, quantity, subtotal, category) VALUES (1, 1, 1, '商品1', 100.00, 2, 200.00, '电子产品')")))
                    .compose(v -> executor.executeUpdate(DSL.query("INSERT INTO order_details (id, order_id, product_id, product_name, unit_price, quantity, subtotal, category) VALUES (2, 2, 2, '商品2', 150.00, 1, 150.00, '电子产品')")))
                    .compose(v -> orderDetailService.getUserConsumptionAnalysis(1L))
                    .onComplete(testContext.succeeding(result -> {
                        assertNotNull(result);
                        assertEquals(1L, result.getLong("userId"));
                        
                        JsonObject statistics = result.getJsonObject("statistics");
                        assertNotNull(statistics);
                        assertEquals(2, statistics.getInteger("totalOrders"));
                        assertEquals(2, statistics.getInteger("totalItems"));
                        
                        @SuppressWarnings("unchecked")
                        List<JsonObject> preferences = result.getJsonArray("preferences").getList();
                        assertNotNull(preferences);
                        assertFalse(preferences.isEmpty());
                        
                        String consumptionLevel = result.getString("consumptionLevel");
                        assertNotNull(consumptionLevel);
                        assertTrue(List.of("VIP", "GOLD", "SILVER", "BRONZE").contains(consumptionLevel));
                        
                        testContext.completeNow();
                    }));
        }
    }

    @Nested
    @DisplayName("复杂查询性能测试")
    class PerformanceTest {

        @Test
        @DisplayName("大量数据表连接查询性能测试")
        void testLargeDataJoinQueryPerformance(VertxTestContext testContext) {
            // 准备大量测试数据
            Future<Void> prepareData = Future.succeededFuture();
            for (int i = 1; i <= 100; i++) {
                final int index = i;
                prepareData = prepareData.compose(v -> {
                    return executor.executeUpdate(DSL.query("INSERT INTO dsl_user (id, name, email, password, age, status) VALUES (" + index + ", '用户" + index + "', 'user" + index + "@example.com', 'password123', " + (20 + index % 50) + ", 'ACTIVE')"))
                            .compose(v2 -> executor.executeUpdate(DSL.query("INSERT INTO orders (id, order_no, user_id, product_id, quantity, unit_price, total_amount, status) VALUES (" + index + ", 'ORD" + String.format("%03d", index) + "', " + index + ", 1, 2, 100.00, 200.00, 'PAID')")))
                            .compose(v3 -> executor.executeUpdate(DSL.query("INSERT INTO order_details (id, order_id, product_id, product_name, unit_price, quantity, subtotal, category) VALUES (" + index + ", " + index + ", 1, '商品" + index + "', 100.00, 2, 200.00, '电子产品')")))
                            .map(v4 -> null);
                });
            }
            
            prepareData.compose(v -> {
                long startTime = System.nanoTime();
                return orderDetailDao.getUserOrderStatisticsWithDetails(1L)
                        .map(result -> {
                            long endTime = System.nanoTime();
                            long duration = (endTime - startTime) / 1_000_000; // milliseconds
                            
                            System.out.println("大量数据表连接查询性能测试完成，耗时: " + duration + "ms");
                            assertTrue(duration < 1000, "大量数据表连接查询性能测试超时");
                            return result;
                        });
            }).onComplete(testContext.succeedingThenComplete());
        }
    }
}
