package cn.qaiu.example.service;

import cn.qaiu.db.dsl.core.JooqExecutor;
import cn.qaiu.db.dsl.lambda.JServiceImpl;
import cn.qaiu.db.dsl.lambda.LambdaPageResult;
import cn.qaiu.example.entity.Product;
import cn.qaiu.vx.core.annotations.Service;
import io.vertx.core.Future;
import java.math.BigDecimal;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 产品服务实现类 演示 JService 的使用，支持 DI 注入
 *
 * @author <a href="https://qaiu.top">QAIU</a>
 */
@Service
@Singleton
public class ProductServiceImpl extends JServiceImpl<Product, Long> implements ProductService {

  private static final Logger LOGGER = LoggerFactory.getLogger(ProductServiceImpl.class);

  /**
   * 构造函数 - 使用 DI 注入 JooqExecutor
   *
   * @param executor JooqExecutor 实例（由 DI 容器自动注入）
   */
  @Inject
  public ProductServiceImpl(JooqExecutor executor) {
    super(executor, Product.class);
    LOGGER.info("ProductServiceImpl initialized with DI injection");
  }

  /** 无参构造函数 - VXCore框架要求 */
  public ProductServiceImpl() {
    super();
  }

  @Override
  public Future<List<Product>> findAvailableProducts() {
    LOGGER.info("查找在售产品");
    return lambdaList(
        lambdaQuery()
            .eq(Product::getStatus, Product.ProductStatus.ACTIVE)
            .orderByDesc(Product::getCreateTime));
  }

  @Override
  public Future<List<Product>> findByPriceRange(String minPrice, String maxPrice) {
    LOGGER.info("根据价格范围查找产品: {} - {}", minPrice, maxPrice);
    BigDecimal min = new BigDecimal(minPrice);
    BigDecimal max = new BigDecimal(maxPrice);
    return lambdaList(
        lambdaQuery()
            .ge(Product::getPrice, min)
            .le(Product::getPrice, max)
            .eq(Product::getStatus, Product.ProductStatus.ACTIVE)
            .orderByAsc(Product::getPrice));
  }

  @Override
  public Future<List<Product>> findByCategory(Long categoryId) {
    LOGGER.info("根据分类查找产品: {}", categoryId);
    return lambdaList(
        lambdaQuery()
            .eq(Product::getCategory, categoryId.toString())
            .eq(Product::getStatus, Product.ProductStatus.ACTIVE)
            .orderByDesc(Product::getCreateTime));
  }

  @Override
  public Future<LambdaPageResult<Product>> searchProducts(String keyword, long page, long size) {
    LOGGER.info("搜索产品: {}, 页码: {}, 每页: {}", keyword, page, size);

    if (keyword == null || keyword.trim().isEmpty()) {
      return lambdaPage(
          lambdaQuery()
              .eq(Product::getStatus, Product.ProductStatus.ACTIVE)
              .orderByDesc(Product::getCreateTime),
          page,
          size);
    }

    return lambdaPage(
        lambdaQuery()
            .like(Product::getName, keyword)
            .eq(Product::getStatus, Product.ProductStatus.ACTIVE)
            .orderByDesc(Product::getCreateTime),
        page,
        size);
  }

  @Override
  public Future<List<Product>> getHotProducts(int limit) {
    LOGGER.info("获取热销产品, 限制数量: {}", limit);
    return lambdaList(
        lambdaQuery()
            .eq(Product::getStatus, Product.ProductStatus.ACTIVE)
            .gt(Product::getStock, 0)
            .orderByDesc(Product::getCreateTime)
            .limit(limit));
  }

  @Override
  public Future<Long> countProducts() {
    LOGGER.info("统计产品数量");
    return count();
  }

  @Override
  public Future<BigDecimal> getAveragePrice() {
    LOGGER.info("获取产品平均价格");
    return avg("price")
        .map(
            optional -> {
              if (optional.isPresent()) {
                Object value = optional.get();
                if (value instanceof BigDecimal) {
                  return (BigDecimal) value;
                } else if (value instanceof Number) {
                  return BigDecimal.valueOf(((Number) value).doubleValue());
                }
              }
              return BigDecimal.ZERO;
            });
  }

  // =================== ProductService接口方法实现 ===================

  @Override
  public Future<Product> getProductById(Long id) {
    LOGGER.info("根据ID获取产品: {}", id);
    return lambdaOne(lambdaQuery().eq(Product::getId, id)).map(opt -> opt.orElse(null));
  }

  @Override
  public Future<List<Product>> getAll() {
    LOGGER.info("获取所有产品");
    return list();
  }

  @Override
  public Future<LambdaPageResult<Product>> page(long page, long size) {
    LOGGER.info("分页获取产品: page={}, size={}", page, size);
    return lambdaPage(lambdaQuery().orderByDesc(Product::getCreateTime), page, size);
  }

  @Override
  public Future<Product> create(Product entity) {
    LOGGER.info("创建产品: {}", entity);
    return insert(entity).map(opt -> opt.orElse(null));
  }

  @Override
  public Future<Boolean> updateProduct(Product entity) {
    LOGGER.info("更新产品: {}", entity);
    return super.update(entity).map(opt -> opt.isPresent());
  }

  @Override
  public Future<Boolean> deleteProduct(Long id) {
    LOGGER.info("删除产品: {}", id);
    return super.delete(id);
  }

  @Override
  public Future<List<Product>> search(String keyword) {
    LOGGER.info("搜索产品: {}", keyword);
    if (keyword == null || keyword.trim().isEmpty()) {
      return Future.succeededFuture(List.of());
    }
    return lambdaList(
        lambdaQuery().like(Product::getName, keyword).orderByDesc(Product::getCreateTime));
  }

  @Override
  public Future<io.vertx.core.json.JsonObject> getStatistics() {
    LOGGER.info("获取产品统计信息");
    return count()
        .map(
            total -> {
              io.vertx.core.json.JsonObject stats = new io.vertx.core.json.JsonObject();
              stats.put("totalProducts", total);
              return stats;
            });
  }
}
