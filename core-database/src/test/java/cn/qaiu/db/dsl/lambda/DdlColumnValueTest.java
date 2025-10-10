package cn.qaiu.db.dsl.lambda;

import cn.qaiu.db.dsl.core.JooqExecutor;
import cn.qaiu.db.dsl.lambda.example.Product;
import cn.qaiu.db.dsl.lambda.example.ProductDao;
import cn.qaiu.db.pool.JDBCPoolInit;
import cn.qaiu.db.pool.JDBCType;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.jdbcclient.JDBCPool;
import io.vertx.sqlclient.PoolOptions;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DdlColumn value字段和Lambda查询功能测试
 * 
 * @author qaiu
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DdlColumnValueTest {
    
    private static final Logger logger = LoggerFactory.getLogger(DdlColumnValueTest.class);
    
    private static Vertx vertx;
    private static JDBCPool pool;
    private static JooqExecutor executor;
    private static ProductDao productDao;
    
    @BeforeAll
    static void setUp() {
        vertx = Vertx.vertx();
        
        // 创建H2内存数据库连接池
        io.vertx.jdbcclient.JDBCConnectOptions connectOptions = new io.vertx.jdbcclient.JDBCConnectOptions()
                .setJdbcUrl("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE")
                .setUser("sa")
                .setPassword("");
        
        pool = JDBCPool.pool(vertx, connectOptions, new PoolOptions().setMaxSize(10));
        executor = new JooqExecutor(pool);
        productDao = new ProductDao(executor);
        
        // 创建测试表
        createTestTable();
    }
    
    @AfterAll
    static void tearDown() {
        if (pool != null) {
            pool.close();
        }
        if (vertx != null) {
            vertx.close();
        }
    }
    
    @BeforeEach
    void setUpEach() {
        // 清空测试数据
        clearTestData();
        // 插入测试数据
        insertTestData();
    }
    
    /**
     * 创建测试表
     */
    private static void createTestTable() {
        String createTableSql = """
            CREATE TABLE IF NOT EXISTS products (
                product_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                product_name VARCHAR(100) NOT NULL,
                product_code VARCHAR(50) NOT NULL UNIQUE,
                category_id BIGINT NOT NULL,
                price DECIMAL(10,2) NOT NULL,
                stock_quantity INTEGER DEFAULT 0,
                description TEXT,
                is_active BOOLEAN DEFAULT TRUE,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
            """;
        
        pool.query(createTableSql).execute()
                .onSuccess(result -> logger.info("Products test table created successfully"))
                .onFailure(error -> logger.error("Failed to create products test table", error));
    }
    
    /**
     * 清空测试数据
     */
    private void clearTestData() {
        pool.query("DELETE FROM products").execute()
                .onSuccess(result -> logger.debug("Products test data cleared"))
                .onFailure(error -> logger.error("Failed to clear products test data", error));
    }
    
    /**
     * 插入测试数据
     */
    private void insertTestData() {
        String insertSql = """
            INSERT INTO products (product_name, product_code, category_id, price, stock_quantity, description, is_active, created_at, updated_at) VALUES
            ('iPhone 15 Pro', 'IPHONE15PRO', 1, 999.99, 50, 'Latest iPhone with advanced features', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
            ('Samsung Galaxy S24', 'SAMSUNG_S24', 1, 899.99, 30, 'Flagship Android smartphone', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
            ('MacBook Pro M3', 'MACBOOK_M3', 2, 1999.99, 20, 'Professional laptop for developers', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
            ('Dell XPS 13', 'DELL_XPS13', 2, 1299.99, 15, 'Ultrabook for business users', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
            ('AirPods Pro', 'AIRPODS_PRO', 3, 249.99, 100, 'Wireless earbuds with noise cancellation', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
            ('Sony WH-1000XM5', 'SONY_WH1000XM5', 3, 399.99, 25, 'Premium noise-cancelling headphones', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
            ('Discontinued Product', 'DISCONTINUED', 1, 99.99, 0, 'This product is no longer available', false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
            """;
        
        pool.query(insertSql).execute()
                .onSuccess(result -> logger.debug("Products test data inserted: {} rows", result.rowCount()))
                .onFailure(error -> logger.error("Failed to insert products test data", error));
    }
    
    @Test
    @Order(1)
    void testDdlColumnValueMapping() {
        logger.info("=== 测试DdlColumn value字段映射 ===");
        
        // 测试通过Lambda表达式查询，验证字段映射是否正确
        productDao.findByCode("IPHONE15PRO")
                .onSuccess(product -> {
                    assertTrue(product.isPresent());
                    assertEquals("iPhone 15 Pro", product.get().getUsername());
                    assertEquals("IPHONE15PRO", product.get().getCode());
                    assertEquals(new BigDecimal("999.99"), product.get().getPrice());
                    assertTrue(product.get().getActive());
                    logger.info("✓ DdlColumn value字段映射测试通过: {}", product.get().getUsername());
                })
                .onFailure(error -> fail("DdlColumn value字段映射测试失败: " + error.getMessage()));
    }
    
    @Test
    @Order(2)
    void testBasicLambdaQueries() {
        logger.info("=== 测试基础Lambda查询 ===");
        
        // 测试等值查询
        productDao.findByName("iPhone 15 Pro")
                .onSuccess(product -> {
                    assertTrue(product.isPresent());
                    assertEquals("IPHONE15PRO", product.get().getCode());
                    logger.info("✓ 等值查询测试通过: {}", product.get().getUsername());
                })
                .onFailure(error -> fail("等值查询失败: " + error.getMessage()));
        
        // 测试分类查询
        productDao.findByCategoryId(1L)
                .onSuccess(products -> {
                    assertTrue(products.size() >= 2); // iPhone和Samsung
                    products.forEach(product -> {
                        assertEquals(Long.valueOf(1L), product.getCategoryId());
                    });
                    logger.info("✓ 分类查询测试通过: 找到 {} 个产品", products.size());
                })
                .onFailure(error -> fail("分类查询失败: " + error.getMessage()));
    }
    
    @Test
    @Order(3)
    void testRangeQueries() {
        logger.info("=== 测试范围查询 ===");
        
        // 测试价格范围查询
        productDao.findByPriceRange(new BigDecimal("500.00"), new BigDecimal("1500.00"))
                .onSuccess(products -> {
                    assertTrue(products.size() >= 3);
                    products.forEach(product -> {
                        assertTrue(product.getPrice().compareTo(new BigDecimal("500.00")) >= 0);
                        assertTrue(product.getPrice().compareTo(new BigDecimal("1500.00")) <= 0);
                        assertTrue(product.getActive());
                    });
                    logger.info("✓ 价格范围查询测试通过: 找到 {} 个产品", products.size());
                })
                .onFailure(error -> fail("价格范围查询失败: " + error.getMessage()));
        
        // 测试库存不足查询
        productDao.findLowStockProducts(30)
                .onSuccess(products -> {
                    assertTrue(products.size() >= 1);
                    products.forEach(product -> {
                        assertTrue(product.getStockQuantity() <= 30);
                        assertTrue(product.getActive());
                    });
                    logger.info("✓ 库存不足查询测试通过: 找到 {} 个产品", products.size());
                })
                .onFailure(error -> fail("库存不足查询失败: " + error.getMessage()));
    }
    
    @Test
    @Order(4)
    void testLikeQueries() {
        logger.info("=== 测试LIKE查询 ===");
        
        // 测试产品名称模糊查询
        productDao.findByNameLike("iPhone")
                .onSuccess(products -> {
                    assertEquals(1, products.size());
                    assertEquals("iPhone 15 Pro", products.get(0).getUsername());
                    assertTrue(products.get(0).getActive());
                    logger.info("✓ LIKE查询测试通过: 找到 {} 个产品", products.size());
                })
                .onFailure(error -> fail("LIKE查询失败: " + error.getMessage()));
    }
    
    @Test
    @Order(5)
    void testComplexQueries() {
        logger.info("=== 测试复杂查询 ===");
        
        // 测试多条件组合查询
        productDao.findProductsByComplexCondition(1L, new BigDecimal("800.00"), 20)
                .onSuccess(products -> {
                    assertTrue(products.size() >= 1);
                    products.forEach(product -> {
                        assertEquals(Long.valueOf(1L), product.getCategoryId());
                        assertTrue(product.getPrice().compareTo(new BigDecimal("800.00")) >= 0);
                        assertTrue(product.getStockQuantity() >= 20);
                        assertTrue(product.getActive());
                    });
                    logger.info("✓ 复杂查询测试通过: 找到 {} 个产品", products.size());
                })
                .onFailure(error -> fail("复杂查询失败: " + error.getMessage()));
    }
    
    @Test
    @Order(6)
    void testNestedConditionQueries() {
        logger.info("=== 测试嵌套条件查询 ===");
        
        // 测试嵌套条件查询
        productDao.findProductsWithNestedCondition(1L, new BigDecimal("1000.00"))
                .onSuccess(products -> {
                    assertTrue(products.size() >= 1);
                    products.forEach(product -> {
                        assertEquals(Long.valueOf(1L), product.getCategoryId());
                        // 价格 <= 1000 或者 (活跃且库存 > 0)
                        assertTrue(product.getPrice().compareTo(new BigDecimal("1000.00")) <= 0 ||
                                 (product.getActive() && product.getStockQuantity() > 0));
                    });
                    logger.info("✓ 嵌套条件查询测试通过: 找到 {} 个产品", products.size());
                })
                .onFailure(error -> fail("嵌套条件查询失败: " + error.getMessage()));
    }
    
    @Test
    @Order(7)
    void testPageQueries() {
        logger.info("=== 测试分页查询 ===");
        
        // 测试分页查询
        productDao.findProductsByPage(1, 3, 1L)
                .onSuccess(pageResult -> {
                    assertNotNull(pageResult);
                    assertTrue(pageResult.getTotal() >= 2);
                    assertEquals(3, pageResult.getSize());
                    assertEquals(1, pageResult.getCurrent());
                    assertTrue(pageResult.getRecords().size() <= 3);
                    
                    pageResult.getRecords().forEach(product -> {
                        assertEquals(Long.valueOf(1L), product.getCategoryId());
                        assertTrue(product.getActive());
                    });
                    
                    logger.info("✓ 分页查询测试通过: 总数={}, 当前页={}, 页大小={}", 
                            pageResult.getTotal(), pageResult.getCurrent(), pageResult.getSize());
                })
                .onFailure(error -> fail("分页查询失败: " + error.getMessage()));
    }
    
    @Test
    @Order(8)
    void testCountQueries() {
        logger.info("=== 测试统计查询 ===");
        
        // 测试活跃产品统计
        productDao.countActiveProducts()
                .onSuccess(count -> {
                    assertTrue(count >= 6);
                    logger.info("✓ 活跃产品统计测试通过: 数量 = {}", count);
                })
                .onFailure(error -> fail("活跃产品统计失败: " + error.getMessage()));
        
        // 测试分类产品统计
        productDao.countProductsByCategory(1L)
                .onSuccess(count -> {
                    assertTrue(count >= 2);
                    logger.info("✓ 分类产品统计测试通过: 数量 = {}", count);
                })
                .onFailure(error -> fail("分类产品统计失败: " + error.getMessage()));
    }
    
    @Test
    @Order(9)
    void testExistsQueries() {
        logger.info("=== 测试存在性查询 ===");
        
        // 测试产品代码存在性
        productDao.existsByCode("IPHONE15PRO")
                .onSuccess(exists -> {
                    assertTrue(exists);
                    logger.info("✓ 产品代码存在性测试通过: 存在 = {}", exists);
                })
                .onFailure(error -> fail("产品代码存在性查询失败: " + error.getMessage()));
        
        // 测试不存在的产品代码
        productDao.existsByCode("NONEXISTENT")
                .onSuccess(exists -> {
                    assertFalse(exists);
                    logger.info("✓ 产品代码不存在性测试通过: 存在 = {}", exists);
                })
                .onFailure(error -> fail("产品代码不存在性查询失败: " + error.getMessage()));
    }
    
    @Test
    @Order(10)
    void testFieldSelectionQueries() {
        logger.info("=== 测试字段选择查询 ===");
        
        // 测试字段选择查询
        productDao.findProductBasicInfo()
                .onSuccess(products -> {
                    assertTrue(products.size() >= 1);
                    products.forEach(product -> {
                        assertNotNull(product.getId());
                        assertNotNull(product.getUsername());
                        assertNotNull(product.getCode());
                        assertNotNull(product.getPrice());
                        assertNotNull(product.getActive());
                        // 其他字段应该为null（未选择）
                        assertNull(product.getCategoryId());
                        assertNull(product.getStockQuantity());
                        assertNull(product.getDescription());
                    });
                    logger.info("✓ 字段选择查询测试通过: 查询到 {} 个产品", products.size());
                })
                .onFailure(error -> fail("字段选择查询失败: " + error.getMessage()));
    }
    
    @Test
    @Order(11)
    void testBatchUpdateQueries() {
        logger.info("=== 测试批量更新查询 ===");
        
        // 测试批量更新产品状态
        List<Long> productIds = Arrays.asList(1L, 2L);
        productDao.updateProductStatus(productIds, false)
                .onSuccess(updatedCount -> {
                    assertEquals(2, updatedCount);
                    
                    // 验证更新结果
                    productDao.lambdaList(Product::getActive, false)
                            .onSuccess(updatedProducts -> {
                                assertTrue(updatedProducts.size() >= 2);
                                logger.info("✓ 批量更新状态测试通过: 更新了 {} 个产品", updatedCount);
                            })
                            .onFailure(error -> fail("验证批量更新结果失败: " + error.getMessage()));
                })
                .onFailure(error -> fail("批量更新失败: " + error.getMessage()));
    }
    
    @Test
    @Order(12)
    void testLambdaQueryWrapperAdvanced() {
        logger.info("=== 测试LambdaQueryWrapper高级功能 ===");
        
        // 测试LambdaQueryWrapper的各种方法
        LambdaQueryWrapper<Product> wrapper = productDao.lambdaQuery()
                .eq(Product::getActive, true)
                .ge(Product::getPrice, new BigDecimal("200.00"))
                .le(Product::getPrice, new BigDecimal("2000.00"))
                .in(Product::getCategoryId, Arrays.asList(1L, 2L, 3L))
                .orderByDesc(Product::getPrice)
                .orderByAsc(Product::getName)
                .limit(5);
        
        productDao.lambdaList(wrapper)
                .onSuccess(products -> {
                    assertTrue(products.size() <= 5);
                    products.forEach(product -> {
                        assertTrue(product.getActive());
                        assertTrue(product.getPrice().compareTo(new BigDecimal("200.00")) >= 0);
                        assertTrue(product.getPrice().compareTo(new BigDecimal("2000.00")) <= 0);
                        assertTrue(Arrays.asList(1L, 2L, 3L).contains(product.getCategoryId()));
                    });
                    logger.info("✓ LambdaQueryWrapper高级功能测试通过: 查询到 {} 个产品", products.size());
                })
                .onFailure(error -> fail("LambdaQueryWrapper高级功能测试失败: " + error.getMessage()));
    }
    
    @Test
    @Order(13)
    void testInAndNotInQueries() {
        logger.info("=== 测试IN和NOT IN查询 ===");
        
        // 测试IN查询
        List<String> codes = Arrays.asList("IPHONE15PRO", "SAMSUNG_S24", "MACBOOK_M3");
        productDao.lambdaList(productDao.lambdaQuery().in(Product::getCode, codes))
                .onSuccess(products -> {
                    assertEquals(3, products.size());
                    products.forEach(product -> {
                        assertTrue(codes.contains(product.getCode()));
                    });
                    logger.info("✓ IN查询测试通过: 查询到 {} 个产品", products.size());
                })
                .onFailure(error -> fail("IN查询失败: " + error.getMessage()));
    }
    
    @Test
    @Order(14)
    void testIsNullAndIsNotNullQueries() {
        logger.info("=== 测试IS NULL和IS NOT NULL查询 ===");
        
        // 先插入一个description为null的产品
        Product newProduct = new Product();
        newProduct.setName("Test Product");
        newProduct.setCode("TEST_NULL_DESC");
        newProduct.setCategoryId(1L);
        newProduct.setPrice(new BigDecimal("99.99"));
        newProduct.setStockQuantity(10);
        newProduct.setDescription(null); // 显式设置为null
        newProduct.setActive(true);
        
        productDao.insert(newProduct)
                .onSuccess(inserted -> {
                    // 测试IS NULL查询
                    productDao.lambdaList(productDao.lambdaQuery().isNull(Product::getDescription))
                            .onSuccess(products -> {
                                assertTrue(products.size() >= 1);
                                products.forEach(product -> {
                                    assertNull(product.getDescription());
                                });
                                logger.info("✓ IS NULL查询测试通过: 查询到 {} 个产品", products.size());
                            })
                            .onFailure(error -> fail("IS NULL查询失败: " + error.getMessage()));
                })
                .onFailure(error -> fail("插入测试产品失败: " + error.getMessage()));
    }
    
    @Test
    @Order(15)
    void testBetweenQueries() {
        logger.info("=== 测试BETWEEN查询 ===");
        
        // 测试BETWEEN查询
        productDao.lambdaList(productDao.lambdaQuery()
                .between(Product::getPrice, new BigDecimal("100.00"), new BigDecimal("1000.00"))
                .eq(Product::getActive, true))
                .onSuccess(products -> {
                    assertTrue(products.size() >= 1);
                    products.forEach(product -> {
                        assertTrue(product.getPrice().compareTo(new BigDecimal("100.00")) >= 0);
                        assertTrue(product.getPrice().compareTo(new BigDecimal("1000.00")) <= 0);
                        assertTrue(product.getActive());
                    });
                    logger.info("✓ BETWEEN查询测试通过: 查询到 {} 个产品", products.size());
                })
                .onFailure(error -> fail("BETWEEN查询失败: " + error.getMessage()));
    }
}
