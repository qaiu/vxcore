package cn.qaiu.db.dsl.lambda.example;

import cn.qaiu.db.dsl.core.JooqExecutor;
import cn.qaiu.db.dsl.lambda.LambdaDao;
import cn.qaiu.db.dsl.lambda.LambdaPageResult;
import cn.qaiu.db.dsl.lambda.LambdaQueryWrapper;
import cn.qaiu.db.dsl.lambda.SFunction;
import io.vertx.core.Future;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * 产品DAO - 展示DdlColumn value字段的Lambda查询
 * 
 * @author qaiu
 */
public class ProductDao extends LambdaDao<Product, Long> {
    
    public ProductDao(JooqExecutor executor) {
        super(executor, Product.class);
    }
    
    /**
     * 根据产品名称查询
     */
    public Future<Optional<Product>> findByName(String name) {
        return lambdaOne(Product::getName, name);
    }
    
    /**
     * 根据产品代码查询
     */
    public Future<Optional<Product>> findByCode(String code) {
        return lambdaOne(Product::getCode, code);
    }
    
    /**
     * 根据分类ID查询产品列表
     */
    public Future<List<Product>> findByCategoryId(Long categoryId) {
        return lambdaList(Product::getCategoryId, categoryId);
    }
    
    /**
     * 查询活跃产品
     */
    public Future<List<Product>> findActiveProducts() {
        return lambdaList(Product::getActive, true);
    }
    
    /**
     * 根据价格范围查询产品
     */
    public Future<List<Product>> findByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        return lambdaList(lambdaQuery()
                .ge(Product::getPrice, minPrice)
                .le(Product::getPrice, maxPrice)
                .eq(Product::getActive, true)
                .orderByAsc(Product::getPrice));
    }
    
    /**
     * 查询库存不足的产品
     */
    public Future<List<Product>> findLowStockProducts(Integer threshold) {
        return lambdaList(lambdaQuery()
                .le(Product::getStockQuantity, threshold)
                .eq(Product::getActive, true)
                .orderByAsc(Product::getStockQuantity));
    }
    
    /**
     * 根据产品名称模糊查询
     */
    public Future<List<Product>> findByNameLike(String name) {
        return lambdaList(lambdaQuery()
                .like(Product::getName, name)
                .eq(Product::getActive, true)
                .orderByDesc(Product::getCreatedAt));
    }
    
    /**
     * 复杂查询 - 多条件组合
     */
    public Future<List<Product>> findProductsByComplexCondition(Long categoryId, BigDecimal minPrice, Integer minStock) {
        return lambdaList(lambdaQuery()
                .eq(Product::getCategoryId, categoryId)
                .ge(Product::getPrice, minPrice)
                .ge(Product::getStockQuantity, minStock)
                .eq(Product::getActive, true)
                .orderByDesc(Product::getPrice)
                .orderByAsc(Product::getName));
    }
    
    /**
     * 嵌套条件查询
     */
    public Future<List<Product>> findProductsWithNestedCondition(Long categoryId, BigDecimal maxPrice) {
        return lambdaList(lambdaQuery()
                .eq(Product::getCategoryId, categoryId)
                .and(wrapper -> wrapper
                        .le(Product::getPrice, maxPrice)
                        .or(subWrapper -> subWrapper
                                .eq(Product::getActive, true)
                                .gt(Product::getStockQuantity, 0)))
                .orderByDesc(Product::getCreatedAt));
    }
    
    /**
     * 分页查询产品
     */
    public Future<LambdaPageResult<Product>> findProductsByPage(long current, long size, Long categoryId) {
        LambdaQueryWrapper<Product> wrapper = lambdaQuery()
                .eq(Product::getCategoryId, categoryId)
                .eq(Product::getActive, true)
                .orderByDesc(Product::getCreatedAt);
        
        return lambdaPage(wrapper, current, size);
    }
    
    /**
     * 统计活跃产品数量
     */
    public Future<Long> countActiveProducts() {
        return lambdaCount(lambdaQuery()
                .eq(Product::getActive, true));
    }
    
    /**
     * 统计分类下的产品数量
     */
    public Future<Long> countProductsByCategory(Long categoryId) {
        return lambdaCount(lambdaQuery()
                .eq(Product::getCategoryId, categoryId)
                .eq(Product::getActive, true));
    }
    
    /**
     * 检查产品代码是否存在
     */
    public Future<Boolean> existsByCode(String code) {
        return lambdaExists(Product::getCode, code);
    }
    
    /**
     * 查询产品基本信息（选择特定字段）
     */
    public Future<List<Product>> findProductBasicInfo() {
        return lambdaList(lambdaQuery()
                .select(Product::getId, Product::getName, Product::getCode, Product::getPrice, Product::getActive)
                .eq(Product::getActive, true)
                .orderByAsc(Product::getName));
    }
    
    /**
     * 批量更新产品状态
     */
    public Future<Integer> updateProductStatus(List<Long> productIds, Boolean active) {
        return lambdaUpdate(lambdaQuery()
                .in(Product::getId, productIds), 
                createProductWithStatus(active));
    }
    
    /**
     * 批量更新产品价格
     */
    public Future<Integer> updateProductPrice(List<Long> productIds, BigDecimal newPrice) {
        return lambdaUpdate(lambdaQuery()
                .in(Product::getId, productIds), 
                createProductWithPrice(newPrice));
    }
    
    /**
     * 删除指定分类的产品
     */
    public Future<Integer> deleteProductsByCategory(Long categoryId) {
        return lambdaDelete(lambdaQuery()
                .eq(Product::getCategoryId, categoryId));
    }
    
    /**
     * 创建只有状态的产品对象（用于更新）
     */
    private Product createProductWithStatus(Boolean active) {
        Product product = new Product();
        product.setActive(active);
        return product;
    }
    
    /**
     * 创建只有价格的产品对象（用于更新）
     */
    private Product createProductWithPrice(BigDecimal price) {
        Product product = new Product();
        product.setPrice(price);
        return product;
    }
}
