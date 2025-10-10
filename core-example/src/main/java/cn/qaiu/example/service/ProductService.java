package cn.qaiu.example.service;

import cn.qaiu.example.dao.ProductDao;
import cn.qaiu.example.entity.Product;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * 产品服务层
 * 演示 VXCore 框架的业务逻辑处理
 * 
 * @author QAIU
 */
public class ProductService {
    
    private final ProductDao productDao;
    
    public ProductService() {
        this.productDao = new ProductDao();
    }
    
    /**
     * 创建产品
     */
    public Future<Product> createProduct(Product product) {
        return productDao.save(product);
    }
    
    /**
     * 根据ID获取产品
     */
    public Future<Product> getProductById(Long id) {
        return productDao.getById(id);
    }
    
    /**
     * 根据分类获取产品
     */
    public Future<List<Product>> getProductsByCategory(String category) {
        return productDao.findByCategory(category);
    }
    
    /**
     * 获取所有活跃产品
     */
    public Future<List<Product>> getActiveProducts() {
        return productDao.findActiveProducts();
    }
    
    /**
     * 根据价格范围获取产品
     */
    public Future<List<Product>> getProductsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        return productDao.findByPriceRange(minPrice, maxPrice);
    }
    
    /**
     * 更新产品信息
     */
    public Future<Optional<Product>> updateProduct(Product product) {
        return productDao.update(product);
    }
    
    /**
     * 删除产品
     */
    public Future<Boolean> deleteProduct(Long id) {
        return productDao.deleteById(id);
    }
    
    /**
     * 更新产品库存
     */
    public Future<Boolean> updateProductStock(Long productId, Integer stock) {
        return productDao.updateStock(productId, stock);
    }
    
    /**
     * 更新产品价格
     */
    public Future<Boolean> updateProductPrice(Long productId, BigDecimal price) {
        return productDao.updatePrice(productId, price);
    }
    
    /**
     * 更新产品状态
     */
    public Future<Boolean> updateProductStatus(Long productId, Product.ProductStatus status) {
        return productDao.updateStatus(productId, status);
    }
    
    /**
     * 获取产品统计信息
     */
    public Future<JsonObject> getProductStatistics() {
        return Future.all(
                productDao.countByCategory("Electronics"),
                productDao.countByCategory("Clothing"),
                productDao.countByCategory("Books")
        ).map(result -> {
            JsonObject stats = new JsonObject();
            stats.put("electronics", result.resultAt(0));
            stats.put("clothing", result.resultAt(1));
            stats.put("books", result.resultAt(2));
            return stats;
        });
    }
    
    /**
     * 获取库存不足的产品
     */
    public Future<List<Product>> getLowStockProducts(Integer threshold) {
        return productDao.findLowStockProducts(threshold);
    }
    
    /**
     * 根据名称搜索产品
     */
    public Future<List<Product>> searchProductsByName(String namePattern) {
        return productDao.findByNameLike(namePattern);
    }
}

