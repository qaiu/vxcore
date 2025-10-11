package cn.qaiu.example.dao;

import cn.qaiu.db.datasource.DataSource;
import cn.qaiu.db.dsl.core.JooqExecutor;
import cn.qaiu.db.dsl.lambda.LambdaDao;
import cn.qaiu.example.entity.OrderDetail;
import io.vertx.core.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

/**
 * 多数据源订单详情DAO
 * 演示多数据源的DAO使用，支持 DI 注入
 * 
 * @author <a href="https://qaiu.top">QAIU</a>
 */
@Singleton
@DataSource("order") // 指定使用 order 数据源
public class MultiDataSourceOrderDetailDao extends LambdaDao<OrderDetail, Long> {

    private static final Logger LOGGER = LoggerFactory.getLogger(MultiDataSourceOrderDetailDao.class);

    /**
     * 构造函数 - 使用 DI 注入 JooqExecutor
     * 根据 @DataSource 注解自动选择对应的数据源
     * 
     * @param executor JooqExecutor 实例（由 DI 容器根据注解自动注入）
     */
    @Inject
    public MultiDataSourceOrderDetailDao(JooqExecutor executor) {
        super(executor, OrderDetail.class);
        LOGGER.info("MultiDataSourceOrderDetailDao initialized with DI injection for 'order' datasource");
    }

    /**
     * 根据订单ID查询订单详情列表
     */
    public Future<List<OrderDetail>> findByOrderId(Long orderId) {
        LOGGER.info("根据订单ID查询订单详情: {} (使用 order 数据源)", orderId);
        return lambdaList(lambdaQuery()
                .eq(OrderDetail::getOrderId, orderId));
    }

    /**
     * 根据产品ID查询订单详情列表
     */
    public Future<List<OrderDetail>> findByProductId(Long productId) {
        LOGGER.info("根据产品ID查询订单详情: {} (使用 order 数据源)", productId);
        return lambdaList(lambdaQuery()
                .eq(OrderDetail::getProductId, productId));
    }

    /**
     * 演示方法级别的数据源切换
     * 这个方法会使用 backup 数据源
     */
    @DataSource("backup")
    public Future<List<OrderDetail>> findOrderDetailsFromBackup(Long orderId) {
        LOGGER.info("从备份数据源查询订单详情: {}", orderId);
        return lambdaList(lambdaQuery()
                .eq(OrderDetail::getOrderId, orderId));
    }

    /**
     * 演示方法级别的数据源切换
     * 这个方法会使用 archive 数据源
     */
    @DataSource("archive")
    public Future<List<OrderDetail>> findArchivedOrderDetails(Long orderId) {
        LOGGER.info("从归档数据源查询订单详情: {}", orderId);
        return lambdaList(lambdaQuery()
                .eq(OrderDetail::getOrderId, orderId));
    }

    /**
     * 统计订单详情数量
     */
    public Future<Long> countOrderDetails() {
        LOGGER.info("统计订单详情数量 (使用 order 数据源)");
        return count();
    }

    /**
     * 检查订单详情是否存在
     */
    public Future<Boolean> existsByOrderId(Long orderId) {
        LOGGER.info("检查订单详情是否存在: {} (使用 order 数据源)", orderId);
        return lambdaExists(lambdaQuery().eq(OrderDetail::getOrderId, orderId));
    }
}
