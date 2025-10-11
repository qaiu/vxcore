package cn.qaiu.example.dao;

import cn.qaiu.db.dsl.core.JooqExecutor;
import cn.qaiu.db.dsl.lambda.LambdaDao;
import cn.qaiu.example.entity.OrderDetail;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import org.jooq.Field;
import org.jooq.Query;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * 订单详情数据访问对象 (DAO) - 演示 MyBatis-Plus 风格的 Lambda 查询
 * 支持 DI 注入，框架自动管理 JooqExecutor
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
 * - lambdaUpdate()
 * - lambdaDelete()
 * 
 * @author QAIU
 */
@Singleton
public class OrderDetailDao extends LambdaDao<OrderDetail, Long> {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderDetailDao.class);

    /**
     * 构造函数 - 使用 DI 注入 JooqExecutor
     * 
     * @param executor JooqExecutor 实例（由 DI 容器自动注入）
     */
    @Inject
    public OrderDetailDao(JooqExecutor executor) {
        super(executor, OrderDetail.class);
        LOGGER.info("OrderDetailDao initialized with DI injection");
    }

    /**
     * 根据订单ID查询订单详情列表
     */
    public Future<List<OrderDetail>> findByOrderId(Long orderId) {
        return lambdaList(lambdaQuery()
                .eq(OrderDetail::getOrderId, orderId));
    }

    /**
     * 根据商品ID查询订单详情列表
     */
    public Future<List<OrderDetail>> findByProductId(Long productId) {
        return lambdaList(lambdaQuery()
                .eq(OrderDetail::getProductId, productId));
    }

    /**
     * 根据分类查询订单详情列表
     */
    public Future<List<OrderDetail>> findByCategory(String category) {
        return lambdaList(lambdaQuery()
                .eq(OrderDetail::getCategory, category));
    }

    /**
     * 根据订单ID删除订单详情
     */
    public Future<Integer> deleteByOrderId(Long orderId) {
        return lambdaDelete(lambdaQuery()
                .eq(OrderDetail::getOrderId, orderId));
    }

    /**
     * 根据商品ID删除订单详情
     */
    public Future<Integer> deleteByProductId(Long productId) {
        return lambdaDelete(lambdaQuery()
                .eq(OrderDetail::getProductId, productId));
    }

    /**
     * 统计订单详情数量
     */
    public Future<Long> countByOrderId(Long orderId) {
        return lambdaCount(lambdaQuery()
                .eq(OrderDetail::getOrderId, orderId));
    }

    /**
     * 统计商品销售数量
     */
    public Future<Long> countByProductId(Long productId) {
        return lambdaCount(lambdaQuery()
                .eq(OrderDetail::getProductId, productId));
    }

    /**
     * 查询指定价格范围的订单详情
     */
    public Future<List<OrderDetail>> findByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        return lambdaList(lambdaQuery()
                .ge(OrderDetail::getUnitPrice, minPrice)
                .le(OrderDetail::getUnitPrice, maxPrice)
                .orderByDesc(OrderDetail::getUnitPrice));
    }

    /**
     * 查询指定数量范围的订单详情
     */
    public Future<List<OrderDetail>> findByQuantityRange(Integer minQuantity, Integer maxQuantity) {
        return lambdaList(lambdaQuery()
                .ge(OrderDetail::getQuantity, minQuantity)
                .le(OrderDetail::getQuantity, maxQuantity)
                .orderByDesc(OrderDetail::getQuantity));
    }

    /**
     * 批量更新订单详情的分类
     */
    public Future<Integer> updateCategoryByOrderId(Long orderId, String newCategory) {
        OrderDetail updateEntity = new OrderDetail();
        updateEntity.setCategory(newCategory);
        return lambdaUpdate(lambdaQuery()
                .eq(OrderDetail::getOrderId, orderId), updateEntity);
    }

    /**
     * 批量更新订单详情的价格
     */
    public Future<Integer> updatePriceByProductId(Long productId, BigDecimal newPrice) {
        OrderDetail updateEntity = new OrderDetail();
        updateEntity.setUnitPrice(newPrice);
        return lambdaUpdate(lambdaQuery()
                .eq(OrderDetail::getProductId, productId), updateEntity);
    }

    /**
     * 查询订单详情包含用户信息（三表连接查询）
     */
    public Future<List<JsonObject>> findOrderWithDetailsAndUser(Long orderId) {
        return executor.executeQuery(DSL.select(
                DSL.field("od.id").as("detailId"),
                DSL.field("od.order_id").as("orderId"),
                DSL.field("od.product_name").as("productName"),
                DSL.field("od.category"),
                DSL.field("od.quantity"),
                DSL.field("od.subtotal"),
                DSL.field("o.order_no").as("orderNo"),
                DSL.field("o.user_id").as("userId"),
                DSL.field("u.name").as("userName"),
                DSL.field("u.email").as("userEmail")
        )
                .from(DSL.table("order_details").as("od"))
                .join(DSL.table("orders").as("o")).on(DSL.field("od.order_id", Long.class).eq(DSL.field("o.id", Long.class)))
                .join(DSL.table("dsl_user").as("u")).on(DSL.field("o.user_id", Long.class).eq(DSL.field("u.id", Long.class)))
                .where(DSL.field("od.order_id", Long.class).eq(orderId)))
                .map(result -> {
                    List<JsonObject> jsonList = new java.util.ArrayList<>();
                    for (io.vertx.sqlclient.Row row : result) {
                        JsonObject json = new JsonObject();
                        json.put("detailId", row.getValue("detailId"));
                        json.put("orderId", row.getValue("orderId"));
                        json.put("productName", row.getValue("productName"));
                        json.put("category", row.getValue("category"));
                        json.put("quantity", row.getValue("quantity"));
                        json.put("subtotal", row.getValue("subtotal"));
                        json.put("orderNo", row.getValue("orderNo"));
                        json.put("userId", row.getValue("userId"));
                        json.put("userName", row.getValue("userName"));
                        json.put("userEmail", row.getValue("userEmail"));
                        jsonList.add(json);
                    }
                    return jsonList;
                });
    }

    /**
     * 获取用户订单统计信息（包含订单详情）
     */
    public Future<JsonObject> getUserOrderStatisticsWithDetails(Long userId) {
        return executor.executeQuery(DSL.select(
                DSL.field("u.id").as("userId"),
                DSL.field("u.name").as("userName"),
                DSL.field("u.email").as("userEmail"),
                DSL.count(DSL.field("o.id")).as("totalOrders"),
                DSL.count(DSL.field("od.id")).as("totalItems"),
                DSL.sum(DSL.field("od.quantity", Integer.class)).as("totalQuantity")
        )
                .from(DSL.table("dsl_user").as("u"))
                .leftJoin(DSL.table("orders").as("o")).on(DSL.field("u.id", Long.class).eq(DSL.field("o.user_id", Long.class)))
                .leftJoin(DSL.table("order_details").as("od")).on(DSL.field("o.id", Long.class).eq(DSL.field("od.order_id", Long.class)))
                .where(DSL.field("u.id", Long.class).eq(userId))
                .groupBy(DSL.field("u.id"), DSL.field("u.name"), DSL.field("u.email")))
                .map(result -> {
                    if (result.size() == 0) {
                        return new JsonObject();
                    }
                    io.vertx.sqlclient.Row firstRow = result.iterator().next();
                    JsonObject json = new JsonObject();
                    json.put("userId", firstRow.getValue("userId"));
                    json.put("userName", firstRow.getValue("userName"));
                    json.put("userEmail", firstRow.getValue("userEmail"));
                    json.put("totalOrders", firstRow.getValue("totalOrders"));
                    json.put("totalItems", firstRow.getValue("totalItems"));
                    json.put("totalQuantity", firstRow.getValue("totalQuantity"));
                    return json;
                });
    }

    /**
     * 获取商品销售统计信息（按分类分组）
     */
    public Future<List<JsonObject>> getProductSalesStatisticsByCategory() {
        return executor.executeQuery(DSL.select(
                DSL.field("category"),
                DSL.count(DSL.field("product_id")).as("productCount"),
                DSL.count(DSL.field("id")).as("salesCount"),
                DSL.sum(DSL.field("quantity", Integer.class)).as("totalQuantity")
        )
                .from(DSL.table("order_details"))
                .groupBy(DSL.field("category"))
                .orderBy(DSL.count(DSL.field("id")).desc()))
                .map(result -> {
                    List<JsonObject> jsonList = new java.util.ArrayList<>();
                    for (io.vertx.sqlclient.Row row : result) {
                        JsonObject json = new JsonObject();
                        json.put("category", row.getValue("category"));
                        json.put("productCount", row.getValue("productCount"));
                        json.put("salesCount", row.getValue("salesCount"));
                        json.put("totalQuantity", row.getValue("totalQuantity"));
                        jsonList.add(json);
                    }
                    return jsonList;
                });
    }
}
