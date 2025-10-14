package cn.qaiu.example.dao;

import cn.qaiu.db.dsl.core.JooqExecutor;
import cn.qaiu.db.dsl.lambda.LambdaDao;
import cn.qaiu.example.entity.Product;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import org.jooq.Field;
import org.jooq.Query;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 产品数据访问对象 (DAO) - 演示 MyBatis-Plus 风格的 Lambda 查询
 * 
 * 继承 LambdaDao，父类已提供基础 CRUD 功能：
 * - insert(T entity)
 * - update(T entity) 
 * - delete(ID id)
 * - findById(ID id)
 * - findAll()
 * - findByCondition(Condition condition)
 * - count()
 * - lambdaQuery()
 * 
 * 本类只提供个性化的业务查询方法。
 * 
 * @author QAIU
 */
public class ProductDao extends LambdaDao<Product, Long> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProductDao.class);

    /**
     * 默认构造函数 - 使用自动管理模式
     */
    public ProductDao() {
        super(Product.class);
    }

    /**
     * 带参数的构造函数
     */
    public ProductDao(JooqExecutor executor) {
        super(executor, Product.class);
    }

    // =================== 个性化业务查询方法 ===================
    
    /**
     * 创建产品
     */
    public Future<Product> createProduct(String name, String category, BigDecimal price, Integer stock, String description) {
        LOGGER.debug("Creating product: name={}, category={}, price={}", name, category, price);
        
        Product product = new Product();
        product.setName(name);
        product.setCategory(category);
        product.setPrice(price);
        product.setStock(stock);
        product.setDescription(description);
        product.setStatus(Product.ProductStatus.ACTIVE);
        
        return insert(product).map(optionalProduct -> {
            if (optionalProduct.isPresent()) {
                return optionalProduct.get();
            } else {
                throw new RuntimeException("Failed to create product");
            }
        });
    }
    
    /**
     * 根据分类查找产品 - 演示 Lambda 查询
     */
    public Future<List<Product>> findByCategory(String category) {
        LOGGER.debug("Finding products by category: {}", category);
        
        return lambdaList(lambdaQuery()
                .eq(Product::getCategory, category));
    }
    
    /**
     * 根据状态查找产品
     */
    public Future<List<Product>> findByStatus(Product.ProductStatus status) {
        LOGGER.debug("Finding products by status: {}", status);
        
        return lambdaList(lambdaQuery()
                .eq(Product::getStatus, status));
    }
    
    /**
     * 查找活跃产品
     */
    public Future<List<Product>> findActiveProducts() {
        return findByStatus(Product.ProductStatus.ACTIVE);
    }
    
    /**
     * 根据价格范围查找产品
     */
    public Future<List<Product>> findByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        LOGGER.debug("Finding products by price range: {}-{}", minPrice, maxPrice);
        
        return lambdaList(lambdaQuery()
                .between(Product::getPrice, minPrice, maxPrice));
    }
    
    /**
     * 根据库存范围查找产品
     */
    public Future<List<Product>> findByStockRange(Integer minStock, Integer maxStock) {
        LOGGER.debug("Finding products by stock range: {}-{}", minStock, maxStock);
        
        return lambdaList(lambdaQuery()
                .between(Product::getStock, minStock, maxStock));
    }
    
    /**
     * 搜索产品
     */
    public Future<List<Product>> searchProducts(String keyword) {
        LOGGER.debug("Searching products with keyword: {}", keyword);
        
        return lambdaList(lambdaQuery()
                .like(Product::getName, "%" + keyword + "%"));
    }
    
    /**
     * 获取产品状态统计
     */
    public Future<JsonObject> getProductStatusStatistics() {
        LOGGER.debug("Getting product status statistics");
        
        return findAll().map(allProducts -> {
            JsonObject stats = new JsonObject();
            for (Product.ProductStatus status : Product.ProductStatus.values()) {
                long count = allProducts.stream()
                        .filter(product -> product.getStatus() == status)
                        .count();
                stats.put(status.name(), count);
            }
            return stats;
        });
    }
    
    /**
     * 更新产品库存
     */
    public Future<Boolean> updateStock(Long productId, Integer stock) {
        LOGGER.debug("Updating product stock: productId={}, stock={}", productId, stock);
        
        return findById(productId).compose(optionalProduct -> {
            if (optionalProduct.isPresent()) {
                Product product = optionalProduct.get();
                product.setStock(stock);
                // 如果库存为0，自动设置为缺货状态
                if (stock <= 0) {
                    product.setStatus(Product.ProductStatus.OUT_OF_STOCK);
                }
                return update(product).map(updatedProduct -> updatedProduct.isPresent());
            } else {
                return Future.succeededFuture(false);
            }
        });
    }
    
    /**
     * 更新产品价格
     */
    public Future<Boolean> updatePrice(Long productId, BigDecimal price) {
        LOGGER.debug("Updating product price: productId={}, price={}", productId, price);
        
        return findById(productId).compose(optionalProduct -> {
            if (optionalProduct.isPresent()) {
                Product product = optionalProduct.get();
                product.setPrice(price);
                return update(product).map(updatedProduct -> updatedProduct.isPresent());
            } else {
                return Future.succeededFuture(false);
            }
        });
    }
    
    /**
     * 更新产品状态
     */
    public Future<Boolean> updateStatus(Long productId, Product.ProductStatus status) {
        LOGGER.debug("Updating product {} status to {}", productId, status);
        
        return findById(productId).compose(optionalProduct -> {
            if (optionalProduct.isPresent()) {
                Product product = optionalProduct.get();
                product.setStatus(status);
                return update(product).map(updatedProduct -> updatedProduct.isPresent());
            } else {
                return Future.succeededFuture(false);
            }
        });
    }
    
    /**
     * 激活产品
     */
    public Future<Boolean> activateProduct(Long productId) {
        return updateStatus(productId, Product.ProductStatus.ACTIVE);
    }
    
    /**
     * 停用产品
     */
    public Future<Boolean> deactivateProduct(Long productId) {
        return updateStatus(productId, Product.ProductStatus.INACTIVE);
    }
    
    /**
     * 标记产品停产
     */
    public Future<Boolean> discontinueProduct(Long productId) {
        return updateStatus(productId, Product.ProductStatus.DISCONTINUED);
    }
    
    /**
     * 统计产品数量
     */
    public Future<Long> countByCategory(String category) {
        LOGGER.debug("Counting products by category: {}", category);
        
        return lambdaCount(lambdaQuery()
                .eq(Product::getCategory, category));
    }
    
    /**
     * 统计产品数量
     */
    public Future<Long> countByStatus(Product.ProductStatus status) {
        LOGGER.debug("Counting products by status: {}", status);
        
        return lambdaCount(lambdaQuery()
                .eq(Product::getStatus, status));
    }
    
    /**
     * 查找库存不足的产品
     */
    public Future<List<Product>> findLowStockProducts(Integer threshold) {
        LOGGER.debug("Finding products with low stock (threshold: {})", threshold);
        
        return lambdaList(lambdaQuery()
                .le(Product::getStock, threshold)
                .eq(Product::getStatus, Product.ProductStatus.ACTIVE));
    }
    
    /**
     * 查找缺货产品
     */
    public Future<List<Product>> findOutOfStockProducts() {
        LOGGER.debug("Finding out of stock products");
        
        return lambdaList(lambdaQuery()
                .eq(Product::getStatus, Product.ProductStatus.OUT_OF_STOCK));
    }
    
    /**
     * 根据名称模糊查询
     */
    public Future<List<Product>> findByNameLike(String namePattern) {
        LOGGER.debug("Finding products by name pattern: {}", namePattern);
        
        return lambdaList(lambdaQuery()
                .like(Product::getName, "%" + namePattern + "%"));
    }
    
    /**
     * 批量插入产品
     */
    public Future<Integer> insertBatch(List<Product> products) {
        LOGGER.debug("Batch inserting {} products", products.size());
        // 简化实现：逐个插入
        Future<Void> result = Future.succeededFuture();
        for (Product product : products) {
            result = result.compose(v -> insert(product).map(opt -> null));
        }
        return result.map(v -> products.size());
    }
    
    /**
     * Lambda插入（兼容性方法）
     */
    public Future<Optional<Product>> lambdaInsert(Product product) {
        LOGGER.debug("Lambda inserting product: {}", product.getName());
        return insert(product);
    }
    
    /**
     * 保存产品
     */
    public Future<Product> save(Product product) {
        LOGGER.debug("Saving product: {}", product.getName());
        return insert(product).map(optional -> optional.orElse(product));
    }
    
    /**
     * 根据ID获取产品
     */
    public Future<Product> getById(Long id) {
        LOGGER.debug("Getting product by ID: {}", id);
        return findById(id).map(optional -> optional.orElse(null));
    }
    
    /**
     * 根据时间范围查找产品
     */
    public Future<List<Product>> findByTimeRange(java.time.LocalDateTime startTime, java.time.LocalDateTime endTime) {
        LOGGER.debug("Finding products by time range: {} - {}", startTime, endTime);
        // 简化实现：使用 findAll 然后过滤
        return findAll().map(allProducts -> 
            allProducts.stream()
                .filter(product -> product.getCreateTime().isAfter(startTime) && product.getCreateTime().isBefore(endTime))
                .collect(java.util.stream.Collectors.toList())
        );
    }
    
    /**
     * 根据ID删除产品
     */
    public Future<Boolean> deleteById(Long id) {
        LOGGER.debug("Deleting product by ID: {}", id);
        return delete(id);
    }

    /**
     * 获取产品统计信息 - 演示聚合查询
     */
    public Future<JsonObject> getProductStatistics() {
        LOGGER.debug("Getting product statistics");
        
        Field<BigDecimal> priceField = DSL.field("price", BigDecimal.class);
        Field<Integer> stockField = DSL.field("stock", Integer.class);
        
        Query query = DSL.select(
                DSL.count().as("total_products"),
                DSL.sum(priceField).as("total_value"),
                DSL.avg(priceField).as("avg_price"),
                DSL.max(priceField).as("max_price"),
                DSL.min(priceField).as("min_price"),
                DSL.sum(stockField).as("total_stock")
        )
        .from(DSL.table(getTableName()));
        
        return executor.executeQuery(query)
                .map(rows -> {
                    if (rows.size() == 0) {
                        return new JsonObject()
                                .put("totalProducts", 0)
                                .put("totalValue", 0)
                                .put("avgPrice", 0)
                                .put("maxPrice", 0)
                                .put("minPrice", 0)
                                .put("totalStock", 0);
                    }
                    
                    var row = rows.iterator().next();
                    return new JsonObject()
                            .put("totalProducts", row.getInteger("total_products"))
                            .put("totalValue", row.getBigDecimal("total_value"))
                            .put("avgPrice", row.getBigDecimal("avg_price"))
                            .put("maxPrice", row.getBigDecimal("max_price"))
                            .put("minPrice", row.getBigDecimal("min_price"))
                            .put("totalStock", row.getInteger("total_stock"));
                });
    }

    /**
     * 获取产品及其订单信息 - 演示Join查询
     */
    public Future<List<JsonObject>> getProductsWithOrders() {
        LOGGER.debug("Getting products with orders using join");
        
        return lambdaList(lambdaQuery()
                .leftJoin(cn.qaiu.example.entity.Order.class, (product, order) -> 
                    DSL.field("product_id", Long.class).eq(product.getId())))
                .map(products -> products.stream()
                        .map(Product::toJson)
                        .collect(java.util.stream.Collectors.toList()));
    }

    /**
     * 根据用户ID获取其购买的产品 - 演示多表Join查询
     */
    public Future<List<JsonObject>> getProductsByUserId(Long userId) {
        LOGGER.debug("Getting products by user ID: {} using join", userId);
        
        // 使用原生SQL进行Join查询
        Field<Long> productIdField = DSL.field("p.id", Long.class);
        Field<String> productNameField = DSL.field("p.name", String.class);
        Field<String> productCategoryField = DSL.field("p.category", String.class);
        Field<BigDecimal> productPriceField = DSL.field("p.price", BigDecimal.class);
        
        Query query = DSL.select(
                productIdField,
                productNameField,
                productCategoryField,
                productPriceField
        )
        .from(DSL.table("product").as("p"))
        .innerJoin(DSL.table("order").as("o"))
        .on(DSL.field("p.id", Long.class).eq(DSL.field("o.product_id", Long.class)))
        .where(DSL.field("o.user_id", Long.class).eq(userId));
        
        return executor.executeQuery(query)
                .map(rows -> {
                    List<JsonObject> result = new ArrayList<>();
                    for (var row : rows) {
                        result.add(new JsonObject()
                                .put("id", row.getLong("id"))
                                .put("name", row.getString("name"))
                                .put("category", row.getString("category"))
                                .put("price", row.getBigDecimal("price")));
                    }
                    return result;
                });
    }

    /**
     * 获取产品及其用户统计信息 - 演示复杂Join查询
     */
    public Future<JsonObject> getProductWithUserStats(Long productId) {
        LOGGER.debug("Getting product with user stats for product ID: {}", productId);
        
        // 使用原生SQL进行复杂查询
        Field<Long> productIdField = DSL.field("p.id", Long.class);
        Field<String> productNameField = DSL.field("p.name", String.class);
        Field<BigDecimal> productPriceField = DSL.field("p.price", BigDecimal.class);
        Field<Integer> orderCountField = DSL.count(DSL.field("o.id")).as("order_count");
        Field<Integer> userCountField = DSL.countDistinct(DSL.field("o.user_id")).as("user_count");
        Field<BigDecimal> totalAmountField = DSL.sum(DSL.field("o.total_amount", BigDecimal.class)).as("total_amount");
        
        Query query = DSL.select(
                productIdField,
                productNameField,
                productPriceField,
                orderCountField,
                userCountField,
                totalAmountField
        )
        .from(DSL.table("product").as("p"))
        .leftJoin(DSL.table("order").as("o"))
        .on(DSL.field("p.id", Long.class).eq(DSL.field("o.product_id", Long.class)))
        .where(productIdField.eq(productId))
        .groupBy(productIdField, productNameField, productPriceField);
        
        return executor.executeQuery(query)
                .map(rows -> {
                    if (rows.size() == 0) {
                        return new JsonObject()
                                .put("productId", productId)
                                .put("productName", "")
                                .put("productPrice", 0)
                                .put("orderCount", 0)
                                .put("userCount", 0)
                                .put("totalAmount", 0);
                    }
                    
                    var row = rows.iterator().next();
                    return new JsonObject()
                            .put("productId", row.getLong("id"))
                            .put("productName", row.getString("name"))
                            .put("productPrice", row.getBigDecimal("price"))
                            .put("orderCount", row.getLong("order_count"))
                            .put("userCount", row.getLong("user_count"))
                            .put("totalAmount", row.getBigDecimal("total_amount"));
                });
    }

    /**
     * 获取热销产品排行榜 - 演示Join和排序
     */
    public Future<List<JsonObject>> getHotProducts(Integer limit) {
        LOGGER.debug("Getting hot products with limit: {}", limit);
        
        Field<Long> productIdField = DSL.field("p.id", Long.class);
        Field<String> productNameField = DSL.field("p.name", String.class);
        Field<BigDecimal> productPriceField = DSL.field("p.price", BigDecimal.class);
        Field<Integer> orderCountField = DSL.count(DSL.field("o.id")).as("order_count");
        Field<BigDecimal> totalAmountField = DSL.sum(DSL.field("o.total_amount", BigDecimal.class)).as("total_amount");
        
        Query query = DSL.select(
                productIdField,
                productNameField,
                productPriceField,
                orderCountField,
                totalAmountField
        )
        .from(DSL.table("product").as("p"))
        .innerJoin(DSL.table("order").as("o"))
        .on(DSL.field("p.id", Long.class).eq(DSL.field("o.product_id", Long.class)))
        .groupBy(productIdField, productNameField, productPriceField)
        .orderBy(orderCountField.desc(), totalAmountField.desc())
        .limit(limit != null ? limit : 10);
        
        return executor.executeQuery(query)
                .map(rows -> {
                    List<JsonObject> result = new ArrayList<>();
                    for (var row : rows) {
                        result.add(new JsonObject()
                                .put("productId", row.getLong("id"))
                                .put("productName", row.getString("name"))
                                .put("productPrice", row.getBigDecimal("price"))
                                .put("orderCount", row.getLong("order_count"))
                                .put("totalAmount", row.getBigDecimal("total_amount")));
                    }
                    return result;
                });
    }

}