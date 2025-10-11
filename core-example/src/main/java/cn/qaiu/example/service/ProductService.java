package cn.qaiu.example.service;

import cn.qaiu.db.dsl.lambda.JService;
import cn.qaiu.db.dsl.lambda.LambdaPageResult;
import cn.qaiu.example.entity.Product;
import io.vertx.core.Future;

import java.math.BigDecimal;
import java.util.List;

/**
 * 产品服务接口
 * 
 * @author <a href="https://qaiu.top">QAIU</a>
 */
public interface ProductService extends JService<Product, Long> {

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
    Future<List<Product>> findByPriceRange(BigDecimal minPrice, BigDecimal maxPrice);

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