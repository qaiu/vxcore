package cn.qaiu.example.service;

import cn.qaiu.db.dsl.core.JooqExecutor;
import cn.qaiu.db.dsl.lambda.JServiceImpl;
import cn.qaiu.db.dsl.lambda.LambdaPageResult;
import cn.qaiu.example.entity.Product;
import io.vertx.core.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.math.BigDecimal;
import java.util.List;

/**
 * 产品服务实现类
 * 演示 JService 的使用，支持 DI 注入
 * 
 * @author <a href="https://qaiu.top">QAIU</a>
 */
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

    @Override
    public Future<List<Product>> findAvailableProducts() {
        LOGGER.info("查找在售产品");
        return lambdaList(lambdaQuery()
                .eq(Product::getStatus, Product.ProductStatus.ACTIVE)
                .orderByDesc(Product::getCreateTime));
    }

    @Override
    public Future<List<Product>> findByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        LOGGER.info("根据价格范围查找产品: {} - {}", minPrice, maxPrice);
        return lambdaList(lambdaQuery()
                .ge(Product::getPrice, minPrice)
                .le(Product::getPrice, maxPrice)
                .eq(Product::getStatus, Product.ProductStatus.ACTIVE)
                .orderByAsc(Product::getPrice));
    }

    @Override
    public Future<List<Product>> findByCategory(Long categoryId) {
        LOGGER.info("根据分类查找产品: {}", categoryId);
        return lambdaList(lambdaQuery()
                .eq(Product::getCategory, categoryId.toString())
                .eq(Product::getStatus, Product.ProductStatus.ACTIVE)
                .orderByDesc(Product::getCreateTime));
    }

    @Override
    public Future<LambdaPageResult<Product>> searchProducts(String keyword, long page, long size) {
        LOGGER.info("搜索产品: {}, 页码: {}, 每页: {}", keyword, page, size);
        
        if (keyword == null || keyword.trim().isEmpty()) {
            return lambdaPage(lambdaQuery()
                    .eq(Product::getStatus, Product.ProductStatus.ACTIVE)
                    .orderByDesc(Product::getCreateTime), page, size);
        }
        
        return lambdaPage(lambdaQuery()
                .like(Product::getName, keyword)
                .eq(Product::getStatus, Product.ProductStatus.ACTIVE)
                .orderByDesc(Product::getCreateTime), page, size);
    }

    @Override
    public Future<List<Product>> getHotProducts(int limit) {
        LOGGER.info("获取热销产品, 限制数量: {}", limit);
        return lambdaList(lambdaQuery()
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
                .map(optional -> {
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
}
