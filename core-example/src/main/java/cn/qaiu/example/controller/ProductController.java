package cn.qaiu.example.controller;

import cn.qaiu.db.dsl.lambda.LambdaQueryWrapper;
import cn.qaiu.example.dao.ProductDao;
import cn.qaiu.example.entity.Product;
import cn.qaiu.vx.core.annotaions.RouteHandler;
import cn.qaiu.vx.core.annotaions.RouteMapping;
import cn.qaiu.vx.core.annotaions.param.RequestParam;
import cn.qaiu.vx.core.annotaions.param.PathVariable;
import cn.qaiu.vx.core.annotaions.param.RequestBody;
import cn.qaiu.vx.core.annotaions.exception.ExceptionHandler;
import cn.qaiu.vx.core.enums.RouteMethod;
import cn.qaiu.vx.core.exception.BusinessException;
import cn.qaiu.vx.core.exception.ValidationException;
import cn.qaiu.vx.core.model.JsonResult;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.Function;

/**
 * 产品控制器
 * 演示复杂查询、聚合查询等功能
 * 
 * @author QAIU
 */
@RouteHandler("/product")
public class ProductController {
    
    private final ProductDao productDao;
    
    public ProductController() {
        this.productDao = new ProductDao();
    }
    
    /**
     * 获取产品列表 - 支持复杂查询条件
     */
    @RouteMapping(value = "/", method = RouteMethod.GET)
    public Future<List<Product>> getProducts(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "minPrice", required = false) BigDecimal minPrice,
            @RequestParam(value = "maxPrice", required = false) BigDecimal maxPrice,
            @RequestParam(value = "status", defaultValue = "ACTIVE") String status) {
        
        return productDao.lambdaQuery()
                .like(name != null, Product::getName, name)
                .eq(category != null, Product::getCategory, category)
                .ge(minPrice != null, Product::getPrice, minPrice)
                .le(maxPrice != null, Product::getPrice, maxPrice)
                .eq(Product::getStatus, status).executePage(page, size);
    }
    
    /**
     * 根据ID获取产品
     */
    @RouteMapping(value = "/{id}", method = RouteMethod.GET)
    public Future<Product> getProductById(@PathVariable("id") Long id) {
        if (id == null || id <= 0) {
            throw new ValidationException("产品ID不能为空或小于等于0");
        }
        
        return productDao.findById(id)
                .map(productOptional -> {
                    if (!productOptional.isPresent()) {
                        throw new BusinessException("产品不存在");
                    }
                    return productOptional.get();
                });
    }
    
    /**
     * 创建产品
     */
    @RouteMapping(value = "/", method = RouteMethod.POST)
    public Future<Product> createProduct(@RequestBody JsonObject productData) {
        if (productData == null || productData.isEmpty()) {
            throw new ValidationException("产品数据不能为空");
        }
        
        String name = productData.getString("name");
        String category = productData.getString("category");
        Object priceObj = productData.getValue("price");
        BigDecimal price = BigDecimal.ZERO;
        if (priceObj instanceof Number) {
            price = new BigDecimal(priceObj.toString());
        }
        
        if (name == null || name.trim().isEmpty()) {
            throw new ValidationException("产品名称不能为空");
        }
        
        if (category == null || category.trim().isEmpty()) {
            throw new ValidationException("产品分类不能为空");
        }
        
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("产品价格必须大于0");
        }
        
        Product product = new Product();
        product.setName(name);
        product.setCategory(category);
        product.setPrice(price);
        product.setStatus(Product.ProductStatus.ACTIVE);
        product.setStock(0); // 默认库存为0
        
        return productDao.lambdaInsert(product)
                .map(result -> {
                    if (!result.isPresent()) {
                        throw new RuntimeException("Failed to create product");
                    }
                    return result.get();
                });
    }
    
    /**
     * 更新产品
     */
    @RouteMapping(value = "/{id}", method = RouteMethod.PUT)
    public Future<Product> updateProduct(
            @PathVariable("id") Long id,
            @RequestBody JsonObject productData) {
        
        if (id == null || id <= 0) {
            throw new ValidationException("产品ID不能为空或小于等于0");
        }
        
        if (productData == null || productData.isEmpty()) {
            throw new ValidationException("产品数据不能为空");
        }
        
        return productDao.findById(id)
                .compose(productOptional -> {
                    if (!productOptional.isPresent()) {
                        throw new BusinessException("产品不存在");
                    }
                    Product existingProduct = productOptional.get();
                    
                    // 更新字段
                    if (productData.containsKey("name")) {
                        existingProduct.setName(productData.getString("name"));
                    }
                    if (productData.containsKey("category")) {
                        existingProduct.setCategory(productData.getString("category"));
                    }
                    if (productData.containsKey("price")) {
                        Object priceObj = productData.getValue("price");
                        if (priceObj instanceof Number) {
                            BigDecimal price = new BigDecimal(priceObj.toString());
                            if (price.compareTo(BigDecimal.ZERO) > 0) {
                                existingProduct.setPrice(price);
                            }
                        }
                    }
                    if (productData.containsKey("stock")) {
                        Integer stock = productData.getInteger("stock");
                        if (stock != null && stock >= 0) {
                            existingProduct.setStock(stock);
                        }
                    }
                    if (productData.containsKey("status")) {
                        String statusStr = productData.getString("status");
                        if (statusStr != null) {
                            try {
                                existingProduct.setStatus(Product.ProductStatus.valueOf(statusStr));
                            } catch (IllegalArgumentException e) {
                                // 忽略无效的状态值
                            }
                        }
                    }
                    
                    return productDao.update(existingProduct)
                            .map(updatedProduct -> {
                                if (!updatedProduct.isPresent()) {
                                    throw new RuntimeException("Failed to update product");
                                }
                                return updatedProduct.get();
                            });
                });
    }
    
    /**
     * 删除产品
     */
    @RouteMapping(value = "/{id}", method = RouteMethod.DELETE)
    public Future<Boolean> deleteProduct(@PathVariable("id") Long id) {
        if (id == null || id <= 0) {
            throw new ValidationException("产品ID不能为空或小于等于0");
        }
        
        return productDao.findById(id)
                .compose(productOptional -> {
                    if (!productOptional.isPresent()) {
                        throw new BusinessException("产品不存在");
                    }
                    return productDao.deleteById(id);
                });
    }
    
    /**
     * 按分类统计产品数量
     */
    @RouteMapping(value = "/stats/category", method = RouteMethod.GET)
    public Future<JsonObject> getProductStatsByCategory() {
        return productDao.getProductStatistics();
    }
    
    /**
     * 按分类统计产品价格
     */
    @RouteMapping(value = "/stats/price", method = RouteMethod.GET)
    public Future<JsonObject> getProductPriceStats(
            @RequestParam(value = "category", required = false) String category) {
        
        return productDao.getProductStatistics();
    }
    
    /**
     * 批量更新产品状态
     */
    @RouteMapping(value = "/batch/status", method = RouteMethod.PUT)
    public Future<Boolean> batchUpdateStatus(
            @RequestBody JsonObject updateData) {
        
        @SuppressWarnings("unchecked")
        List<Long> ids = (List<Long>) updateData.getJsonArray("ids").getList();
        String status = updateData.getString("status");
        
        if (ids == null || ids.isEmpty()) {
            throw new ValidationException("产品ID列表不能为空");
        }
        
        if (status == null || status.trim().isEmpty()) {
            throw new ValidationException("状态不能为空");
        }
        
        if (ids.size() > 100) {
            throw new ValidationException("批量更新数量不能超过100个");
        }
        
        // 批量更新产品状态
        return Future.succeededFuture()
                .compose(v -> {
                    Future<Boolean> future = Future.succeededFuture(true);
                    for (Long id : ids) {
                        future = future.compose(result -> 
                            productDao.updateStatus(id, Product.ProductStatus.valueOf(status))
                        );
                    }
                    return future;
                });
    }
    
    // ========== 异常处理器 ==========
    
    /**
     * 处理验证异常
     */
    @ExceptionHandler(ValidationException.class)
    public JsonResult<String> handleValidationException(ValidationException e) {
        return JsonResult.error("参数验证失败: " + e.getMessage(), 400);
    }
    
    /**
     * 处理业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public JsonResult<String> handleBusinessException(BusinessException e) {
        return JsonResult.error("业务错误: " + e.getMessage(), 404);
    }
}
