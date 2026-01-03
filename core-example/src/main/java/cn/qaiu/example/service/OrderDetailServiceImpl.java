package cn.qaiu.example.service;

import cn.qaiu.db.dsl.lambda.LambdaPageResult;
import cn.qaiu.example.dao.OrderDetailDao;
import cn.qaiu.example.entity.OrderDetail;
import cn.qaiu.vx.core.annotaions.Service;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import java.math.BigDecimal;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 订单详情服务实现类
 *
 * @author QAIU
 */
@Service
@Singleton
public class OrderDetailServiceImpl implements OrderDetailService {

  private static final Logger LOGGER = LoggerFactory.getLogger(OrderDetailServiceImpl.class);

  private OrderDetailDao orderDetailDao;

  @Inject
  public OrderDetailServiceImpl(OrderDetailDao orderDetailDao) {
    this.orderDetailDao = orderDetailDao;
  }

  /** 无参构造函数 - VXCore框架要求 */
  public OrderDetailServiceImpl() {
    this.orderDetailDao = null; // 将由框架注入
  }

  @Override
  public Future<OrderWithDetailsAndUser> getOrderWithDetailsAndUser(Long orderId) {
    LOGGER.info("获取订单完整信息: {}", orderId);
    return orderDetailDao
        .findOrderWithDetailsAndUser(orderId)
        .map(
            result -> {
              if (result.isEmpty()) {
                return null;
              }

              JsonObject firstRecord = result.get(0);

              OrderWithDetailsAndUser orderWithDetails = new OrderWithDetailsAndUser();
              orderWithDetails.setOrderId(firstRecord.getLong("orderId"));
              orderWithDetails.setOrderNo(firstRecord.getString("orderNo"));
              orderWithDetails.setUserId(firstRecord.getLong("userId"));
              orderWithDetails.setUserName(firstRecord.getString("userName"));
              orderWithDetails.setUserEmail(firstRecord.getString("userEmail"));

              // 设置订单详情列表
              List<OrderDetail> orderDetails =
                  result.stream()
                      .map(
                          record -> {
                            OrderDetail detail = new OrderDetail();
                            detail.setId(record.getLong("detailId"));
                            detail.setOrderId(record.getLong("orderId"));
                            detail.setProductName(record.getString("productName"));
                            detail.setCategory(record.getString("category"));
                            detail.setQuantity(record.getInteger("quantity"));
                            detail.setSubtotal(new BigDecimal(record.getString("subtotal", "0")));
                            return detail;
                          })
                      .toList();

              orderWithDetails.setOrderDetails(orderDetails);
              orderWithDetails.setTotalItems(orderDetails.size());
              orderWithDetails.setTotalQuantity(
                  orderDetails.stream().mapToInt(OrderDetail::getQuantity).sum());

              return orderWithDetails;
            });
  }

  @Override
  public Future<JsonObject> getUserConsumptionAnalysis(Long userId) {
    LOGGER.info("获取用户消费分析: {}", userId);
    return orderDetailDao
        .getUserOrderStatisticsWithDetails(userId)
        .map(
            statistics -> {
              JsonObject analysis = new JsonObject();
              analysis.put("userId", userId);
              analysis.put("statistics", statistics);

              // 计算消费偏好
              List<JsonObject> preferences =
                  List.of(
                      new JsonObject().put("category", "电子产品").put("percentage", 60),
                      new JsonObject().put("category", "服装").put("percentage", 30),
                      new JsonObject().put("category", "其他").put("percentage", 10));
              analysis.put("preferences", preferences);

              // 计算消费等级
              int totalOrders = statistics.getInteger("totalOrders", 0);
              String consumptionLevel;
              if (totalOrders >= 20) {
                consumptionLevel = "VIP";
              } else if (totalOrders >= 10) {
                consumptionLevel = "GOLD";
              } else if (totalOrders >= 5) {
                consumptionLevel = "SILVER";
              } else {
                consumptionLevel = "BRONZE";
              }
              analysis.put("consumptionLevel", consumptionLevel);

              return analysis;
            });
  }

  // =================== SimpleJService接口方法实现 ===================

  @Override
  public Future<OrderDetail> getById(Long id) {
    LOGGER.info("根据ID获取订单详情: {}", id);
    return orderDetailDao.findById(id).map(opt -> opt.orElse(null));
  }

  @Override
  public Future<List<OrderDetail>> getAll() {
    LOGGER.info("获取所有订单详情");
    return orderDetailDao.findAll();
  }

  @Override
  public Future<LambdaPageResult<OrderDetail>> page(long page, long size) {
    LOGGER.info("分页获取订单详情: page={}, size={}", page, size);
    return orderDetailDao.lambdaPage(orderDetailDao.lambdaQuery(), page, size);
  }

  @Override
  public Future<Long> count() {
    LOGGER.info("获取订单详情总数");
    return orderDetailDao.count();
  }

  @Override
  public Future<OrderDetail> create(OrderDetail entity) {
    LOGGER.info("创建订单详情: {}", entity);
    return orderDetailDao.insert(entity).map(opt -> opt.orElse(null));
  }

  @Override
  public Future<Boolean> update(OrderDetail entity) {
    LOGGER.info("更新订单详情: {}", entity);
    return orderDetailDao.update(entity).map(opt -> opt.isPresent());
  }

  @Override
  public Future<Boolean> delete(Long id) {
    LOGGER.info("删除订单详情: {}", id);
    return orderDetailDao.delete(id);
  }

  @Override
  public Future<List<OrderDetail>> search(String keyword) {
    LOGGER.info("搜索订单详情: {}", keyword);
    if (keyword == null || keyword.trim().isEmpty()) {
      return Future.succeededFuture(List.of());
    }
    return orderDetailDao.lambdaList(
        orderDetailDao.lambdaQuery().like(OrderDetail::getProductName, keyword));
  }

  @Override
  public Future<JsonObject> getStatistics() {
    LOGGER.info("获取订单详情统计信息");
    return orderDetailDao
        .count()
        .map(
            total -> {
              JsonObject stats = new JsonObject();
              stats.put("totalOrderDetails", total);
              return stats;
            });
  }
}
