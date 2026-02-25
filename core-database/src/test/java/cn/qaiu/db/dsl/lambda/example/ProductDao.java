package cn.qaiu.db.dsl.lambda.example;

import cn.qaiu.db.dsl.core.JooqExecutor;
import cn.qaiu.db.dsl.lambda.LambdaDao;
import cn.qaiu.db.dsl.lambda.LambdaPageResult;
import cn.qaiu.db.dsl.lambda.LambdaQueryWrapper;
import io.vertx.core.Future;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Lambda测试用的ProductDao
 *
 * @author <a href="https://qaiu.top">QAIU</a>
 */
public class ProductDao extends LambdaDao<Product, Long> {

  public ProductDao(JooqExecutor executor) {
    super(executor, Product.class);
  }

  /** 创建Lambda查询包装器 */
  public LambdaQueryWrapper<Product> lambdaQuery() {
    return super.lambdaQuery();
  }

  /** 根据产品代码查找 */
  public Future<Optional<Product>> findByCode(String code) {
    return lambdaOne(Product::getCode, code);
  }

  /** 根据产品名称查找 */
  public Future<Optional<Product>> findByName(String name) {
    return lambdaOne(Product::getName, name);
  }

  /** 根据分类ID查找产品列表 */
  public Future<List<Product>> findByCategoryId(Long categoryId) {
    return lambdaQuery().eq(Product::getCategoryId, categoryId).eq(Product::getActive, true).list();
  }

  /** 根据价格范围查找活跃产品 */
  public Future<List<Product>> findByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
    return lambdaQuery()
        .ge(Product::getPrice, minPrice)
        .le(Product::getPrice, maxPrice)
        .eq(Product::getActive, true)
        .list();
  }

  /** 查找库存不足的产品 */
  public Future<List<Product>> findLowStockProducts(int threshold) {
    return lambdaQuery()
        .le(Product::getStockQuantity, threshold)
        .eq(Product::getActive, true)
        .list();
  }

  /** 根据名称模糊查询 */
  public Future<List<Product>> findByNameLike(String keyword) {
    return lambdaQuery().like(Product::getName, keyword).eq(Product::getActive, true).list();
  }

  /** 复杂条件查询 */
  public Future<List<Product>> findProductsByComplexCondition(
      Long categoryId, BigDecimal minPrice, int minStock) {
    return lambdaQuery()
        .eq(Product::getCategoryId, categoryId)
        .ge(Product::getPrice, minPrice)
        .ge(Product::getStockQuantity, minStock)
        .eq(Product::getActive, true)
        .list();
  }

  /** 嵌套条件查询 */
  public Future<List<Product>> findProductsWithNestedCondition(
      Long categoryId, BigDecimal maxPrice) {
    return lambdaQuery()
        .eq(Product::getCategoryId, categoryId)
        .and(
            wrapper ->
                wrapper
                    .le(Product::getPrice, maxPrice)
                    .or(
                        subWrapper ->
                            subWrapper
                                .eq(Product::getActive, true)
                                .gt(Product::getStockQuantity, 0)))
        .list();
  }

  /** 分页查询 */
  public Future<LambdaPageResult<Product>> findProductsByPage(int page, int size, Long categoryId) {
    return lambdaPage(
        lambdaQuery()
            .eq(Product::getCategoryId, categoryId)
            .eq(Product::getActive, true)
            .orderByDesc(Product::getCreatedAt),
        page,
        size);
  }

  /** 统计活跃产品数量 */
  public Future<Long> countActiveProducts() {
    return lambdaCount(Product::getActive, true);
  }

  /** 统计分类产品数量 */
  public Future<Long> countProductsByCategory(Long categoryId) {
    return lambdaQuery()
        .eq(Product::getCategoryId, categoryId)
        .eq(Product::getActive, true)
        .count();
  }

  /** 检查产品代码是否存在 */
  public Future<Boolean> existsByCode(String code) {
    return lambdaExists(Product::getCode, code);
  }

  /** 查询产品基本信息（字段选择） */
  public Future<List<Product>> findProductBasicInfo() {
    return lambdaQuery()
        .select(
            Product::getId,
            Product::getName,
            Product::getCode,
            Product::getPrice,
            Product::getActive)
        .eq(Product::getActive, true)
        .list();
  }

  /** 批量更新产品状态 */
  public Future<Integer> updateProductStatus(List<Long> productIds, Boolean active) {
    Product updateProduct = new Product();
    updateProduct.setActive(active);
    return lambdaUpdate(lambdaQuery().in(Product::getId, productIds), updateProduct);
  }

  /** 插入产品 */
  public Future<Optional<Product>> insert(Product product) {
    return super.insert(product);
  }
}
