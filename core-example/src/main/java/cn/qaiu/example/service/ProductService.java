package cn.qaiu.example.service;

import cn.qaiu.db.dsl.lambda.LambdaPageResult;
import cn.qaiu.example.entity.Product;
import cn.qaiu.vx.core.codegen.CustomProxyGen;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import java.math.BigDecimal;
import java.util.List;

/**
 * 产品服务接口 注意：不继承SimpleJService以避免与JServiceImpl的方法签名冲突
 *
 * @author <a href="https://qaiu.top">QAIU</a>
 */
@CustomProxyGen
public interface ProductService {

  // =================== 基础CRUD方法 ===================

  /**
   * 根据ID获取产品
   *
   * @param id 产品ID
   * @return 产品对象
   */
  Future<Product> getProductById(Long id);

  /**
   * 获取所有产品
   *
   * @return 产品列表
   */
  Future<List<Product>> getAll();

  /**
   * 分页查询产品
   *
   * @param page 页码
   * @param size 每页大小
   * @return 分页结果
   */
  Future<LambdaPageResult<Product>> page(long page, long size);

  /**
   * 创建产品
   *
   * @param entity 产品信息
   * @return 创建的产品
   */
  Future<Product> create(Product entity);

  /**
   * 更新产品
   *
   * @param entity 产品信息
   * @return 是否更新成功
   */
  Future<Boolean> updateProduct(Product entity);

  /**
   * 删除产品
   *
   * @param id 产品ID
   * @return 是否删除成功
   */
  Future<Boolean> deleteProduct(Long id);

  /**
   * 搜索产品
   *
   * @param keyword 关键词
   * @return 产品列表
   */
  Future<List<Product>> search(String keyword);

  /**
   * 获取产品统计信息
   *
   * @return 统计信息
   */
  Future<JsonObject> getStatistics();

  // =================== 业务方法 ===================

  /**
   * 查找在售产品
   *
   * @return 在售产品列表
   */
  Future<List<Product>> findAvailableProducts();

  /**
   * 根据价格范围查找产品
   *
   * @param minPrice 最低价格
   * @param maxPrice 最高价格
   * @return 产品列表
   */
  Future<List<Product>> findByPriceRange(String minPrice, String maxPrice);

  /**
   * 根据分类查找产品
   *
   * @param categoryId 分类ID
   * @return 产品列表
   */
  Future<List<Product>> findByCategory(Long categoryId);

  /**
   * 搜索产品
   *
   * @param keyword 关键词
   * @param page 页码
   * @param size 每页大小
   * @return 分页结果
   */
  Future<LambdaPageResult<Product>> searchProducts(String keyword, long page, long size);

  /**
   * 获取热销产品
   *
   * @param limit 限制数量
   * @return 热销产品列表
   */
  Future<List<Product>> getHotProducts(int limit);

  /**
   * 统计产品数量
   *
   * @return 产品总数
   */
  Future<Long> countProducts();

  /**
   * 获取产品平均价格
   *
   * @return 平均价格
   */
  Future<BigDecimal> getAveragePrice();
}
