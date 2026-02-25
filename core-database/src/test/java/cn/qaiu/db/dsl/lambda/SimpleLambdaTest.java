package cn.qaiu.db.dsl.lambda;

import static org.junit.jupiter.api.Assertions.*;

import cn.qaiu.db.dsl.core.JooqExecutor;
import cn.qaiu.db.dsl.lambda.example.Product;
import cn.qaiu.db.dsl.lambda.example.ProductDao;
import io.vertx.core.Vertx;
import io.vertx.jdbcclient.JDBCPool;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import java.math.BigDecimal;
import java.util.Arrays;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 简化的Lambda查询功能测试
 *
 * @author qaiu
 */
@ExtendWith(VertxExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SimpleLambdaTest {

  private static final Logger logger = LoggerFactory.getLogger(SimpleLambdaTest.class);

  private static Vertx vertx;
  private static JDBCPool pool;
  private static JooqExecutor executor;
  private static ProductDao productDao;

  @BeforeAll
  static void setUp() {
    vertx = Vertx.vertx();

    // 使用 H2TestConfig 创建数据库连接池（MODE=MySQL）
    pool = cn.qaiu.db.test.H2TestConfig.createH2Pool(vertx, "lambda_test_db");
    executor = new JooqExecutor(pool);
    productDao = new ProductDao(executor);

    // 同步创建测试表（等待完成）
    try {
      pool.query(cn.qaiu.db.test.H2TestConfig.TestTables.CREATE_PRODUCTS_TABLE)
          .execute()
          .toCompletionStage()
          .toCompletableFuture()
          .get(10, java.util.concurrent.TimeUnit.SECONDS);
      logger.info("Products test table created successfully");
    } catch (Exception e) {
      logger.error("Failed to create products test table", e);
    }
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
  void setUpEach(VertxTestContext testContext) {
    // 清空测试数据并插入
    pool.query("DELETE FROM products")
        .execute()
        .compose(
            v -> {
              String insertSql =
                  """
            INSERT INTO products (product_name, product_code, category_id, price, stock_quantity, description, is_active, created_at, updated_at) VALUES
            ('iPhone 15 Pro', 'IPHONE15PRO', 1, 999.99, 50, 'Latest iPhone with advanced features', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
            ('Samsung Galaxy S24', 'SAMSUNG_S24', 1, 899.99, 30, 'Flagship Android smartphone', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
            ('MacBook Pro M3', 'MACBOOK_M3', 2, 1999.99, 20, 'Professional laptop for developers', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
            ('Dell XPS 13', 'DELL_XPS13', 2, 1299.99, 15, 'Ultrabook for business users', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
            ('AirPods Pro', 'AIRPODS_PRO', 3, 249.99, 100, 'Wireless earbuds with noise cancellation', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
            """;
              return pool.query(insertSql).execute();
            })
        .onSuccess(
            result -> {
              logger.debug("Products test data inserted: {} rows", result.rowCount());
              testContext.completeNow();
            })
        .onFailure(testContext::failNow);
  }

  @Test
  @Order(1)
  void testDdlColumnValueMapping(VertxTestContext testContext) {
    logger.info("=== 测试DdlColumn value字段映射 ===");

    // 测试通过Lambda表达式查询，验证字段映射是否正确
    productDao
        .findByCode("IPHONE15PRO")
        .onSuccess(
            product -> {
              testContext.verify(
                  () -> {
                    assertTrue(product.isPresent());
                    assertEquals("iPhone 15 Pro", product.get().getName());
                    assertEquals("IPHONE15PRO", product.get().getCode());
                    assertEquals(new BigDecimal("999.99"), product.get().getPrice());
                    assertTrue(product.get().getActive());
                    logger.info("✓ DdlColumn value字段映射测试通过: {}", product.get().getName());
                  });
              testContext.completeNow();
            })
        .onFailure(testContext::failNow);
  }

  @Test
  @Order(2)
  void testBasicLambdaQueries(VertxTestContext testContext) {
    logger.info("=== 测试基础Lambda查询 ===");

    // 测试等值查询
    productDao
        .findByName("iPhone 15 Pro")
        .compose(
            product -> {
              testContext.verify(
                  () -> {
                    assertTrue(product.isPresent());
                    assertEquals("IPHONE15PRO", product.get().getCode());
                    logger.info("✓ 等值查询测试通过: {}", product.get().getName());
                  });
              // 测试分类查询
              return productDao.findByCategoryId(1L);
            })
        .onSuccess(
            products -> {
              testContext.verify(
                  () -> {
                    assertTrue(products.size() >= 2); // iPhone和Samsung
                    products.forEach(
                        product -> {
                          assertEquals(Long.valueOf(1L), product.getCategoryId());
                        });
                    logger.info("✓ 分类查询测试通过: 找到 {} 个产品", products.size());
                  });
              testContext.completeNow();
            })
        .onFailure(testContext::failNow);
  }

  @Test
  @Order(3)
  void testLambdaQueryWrapper(VertxTestContext testContext) {
    logger.info("=== 测试LambdaQueryWrapper ===");

    // 测试LambdaQueryWrapper的各种方法
    LambdaQueryWrapper<Product> wrapper =
        productDao
            .lambdaQuery()
            .eq(Product::getActive, true)
            .ge(Product::getPrice, new BigDecimal("200.00"))
            .le(Product::getPrice, new BigDecimal("2000.00"))
            .in(Product::getCategoryId, Arrays.asList(1L, 2L, 3L))
            .orderByDesc(Product::getPrice)
            .orderByAsc(Product::getName)
            .limit(5);

    productDao
        .lambdaList(wrapper)
        .onSuccess(
            products -> {
              testContext.verify(
                  () -> {
                    assertTrue(products.size() <= 5);
                    products.forEach(
                        product -> {
                          assertTrue(product.getActive());
                          assertTrue(product.getPrice().compareTo(new BigDecimal("200.00")) >= 0);
                          assertTrue(product.getPrice().compareTo(new BigDecimal("2000.00")) <= 0);
                          assertTrue(Arrays.asList(1L, 2L, 3L).contains(product.getCategoryId()));
                        });
                    logger.info("✓ LambdaQueryWrapper测试通过: 查询到 {} 个产品", products.size());
                  });
              testContext.completeNow();
            })
        .onFailure(testContext::failNow);
  }

  @Test
  @Order(4)
  void testLambdaUtils(VertxTestContext testContext) {
    logger.info("=== 测试LambdaUtils ===");

    testContext.verify(
        () -> {
          // 测试LambdaUtils的字段名提取功能
          String fieldName = LambdaUtils.getFieldName(Product::getName);
          assertEquals("product_name", fieldName);
          logger.info("✓ LambdaUtils字段名提取测试通过: {} -> {}", "Product::getName", fieldName);

          // 测试字段类型提取
          Class<?> fieldType = LambdaUtils.getFieldType(Product::getName);
          assertEquals(String.class, fieldType);
          logger.info(
              "✓ LambdaUtils字段类型提取测试通过: {} -> {}", "Product::getName", fieldType.getSimpleName());
        });
    testContext.completeNow();
  }

  @Test
  @Order(5)
  void testLambdaQueryBuilder(VertxTestContext testContext) {
    logger.info("=== 测试Lambda查询构建器 ===");

    testContext.verify(
        () -> {
          // 测试查询构建
          LambdaQueryWrapper<Product> wrapper =
              productDao.lambdaQuery().eq(Product::getActive, true).orderByDesc(Product::getPrice);

          // 构建查询条件
          org.jooq.Condition condition = wrapper.buildCondition();
          assertNotNull(condition);
          logger.info("✓ 查询条件构建测试通过");

          // 构建查询
          org.jooq.Query selectQuery = wrapper.buildSelect();
          assertNotNull(selectQuery);
          logger.info("✓ 查询构建测试通过");

          // 构建计数查询
          org.jooq.Query countQuery = wrapper.buildCount();
          assertNotNull(countQuery);
          logger.info("✓ 计数查询构建测试通过");
        });
    testContext.completeNow();
  }
}
