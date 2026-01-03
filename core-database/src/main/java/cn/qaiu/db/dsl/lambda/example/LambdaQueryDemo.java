package cn.qaiu.db.dsl.lambda.example;

import cn.qaiu.db.dsl.core.JooqExecutor;
import cn.qaiu.vx.core.util.VertxHolder;
// import cn.qaiu.db.dsl.lambda.LambdaPageResult; // 未使用
// import cn.qaiu.db.dsl.lambda.LambdaQueryWrapper; // 未使用
// import cn.qaiu.db.dsl.lambda.LambdaUtils; // 未使用
// import cn.qaiu.db.pool.JDBCPoolInit; // 未使用
import io.vertx.core.Vertx;
// import io.vertx.core.json.JsonObject; // 未使用
import io.vertx.jdbcclient.JDBCPool;
import io.vertx.sqlclient.PoolOptions;
import java.math.BigDecimal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// import java.util.Arrays; // 未使用
// import java.util.List; // 未使用

/**
 * Lambda查询功能演示 展示类似MyBatis-Plus的Lambda查询功能
 *
 * @author qaiu
 */
public class LambdaQueryDemo {

  private static final Logger logger = LoggerFactory.getLogger(LambdaQueryDemo.class);

  public static void main(String[] args) {
    // 初始化 Vertx 实例到 VertxHolder
    Vertx vertx = Vertx.vertx();
    VertxHolder.init(vertx);

    // 创建H2内存数据库连接池
    io.vertx.jdbcclient.JDBCConnectOptions connectOptions =
        new io.vertx.jdbcclient.JDBCConnectOptions()
            .setJdbcUrl("jdbc:h2:mem:demo;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE")
            .setUser("sa")
            .setPassword("");

    JDBCPool pool = JDBCPool.pool(vertx, connectOptions, new PoolOptions().setMaxSize(10));
    JooqExecutor executor = new JooqExecutor(pool);
    ProductDao productDao = new ProductDao(executor);

    // 创建演示表和数据
    createDemoTable(pool)
        .compose(v -> insertDemoData(pool))
        .compose(v -> runLambdaQueryDemo(productDao))
        .onComplete(
            result -> {
              if (result.succeeded()) {
                logger.info("✅ Lambda查询演示完成！");
              } else {
                logger.error("❌ Lambda查询演示失败", result.cause());
              }
              pool.close().onComplete(v -> vertx.close());
            });
  }

  /** 创建演示表 */
  private static io.vertx.core.Future<Void> createDemoTable(JDBCPool pool) {
    String createTableSql =
        """
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

    return pool.query(createTableSql)
        .execute()
        .map(
            v -> {
              logger.info("✅ 演示表创建成功");
              return null;
            });
  }

  /** 插入演示数据 */
  private static io.vertx.core.Future<Void> insertDemoData(JDBCPool pool) {
    String insertSql =
        """
            INSERT INTO products (product_name, product_code, category_id, price, stock_quantity, description, is_active) VALUES
            ('iPhone 15 Pro', 'IPHONE15PRO', 1, 999.99, 50, 'Latest iPhone with advanced features', true),
            ('Samsung Galaxy S24', 'SAMSUNG_S24', 1, 899.99, 30, 'Flagship Android smartphone', true),
            ('MacBook Pro M3', 'MACBOOK_M3', 2, 1999.99, 20, 'Professional laptop for developers', true),
            ('Dell XPS 13', 'DELL_XPS13', 2, 1299.99, 15, 'Ultrabook for business users', true),
            ('AirPods Pro', 'AIRPODS_PRO', 3, 249.99, 100, 'Wireless earbuds with noise cancellation', true),
            ('Sony WH-1000XM5', 'SONY_WH1000XM5', 3, 399.99, 25, 'Premium noise-cancelling headphones', true),
            ('Discontinued Product', 'DISCONTINUED', 1, 99.99, 0, 'This product is no longer available', false)
            """;

    return pool.query(insertSql)
        .execute()
        .map(
            v -> {
              logger.info("✅ 演示数据插入成功");
              return null;
            });
  }

  /** 运行Lambda查询演示 */
  private static io.vertx.core.Future<Void> runLambdaQueryDemo(ProductDao productDao) {
    return io.vertx.core.Future.succeededFuture()
        .compose(
            v -> {
              logger.info("🚀 开始Lambda查询演示...");
              return demoBasicQueries(productDao);
            })
        .compose(
            v -> {
              logger.info("📊 演示复杂查询...");
              return demoComplexQueries(productDao);
            })
        .compose(
            v -> {
              logger.info("📄 演示分页查询...");
              return demoPageQueries(productDao);
            })
        .compose(
            v -> {
              logger.info("🔍 演示统计查询...");
              return demoCountQueries(productDao);
            })
        .compose(
            v -> {
              logger.info("🎯 演示字段选择查询...");
              return demoFieldSelectionQueries(productDao);
            });
  }

  /** 演示基础查询 */
  private static io.vertx.core.Future<Void> demoBasicQueries(ProductDao productDao) {
    return productDao
        .findByCode("IPHONE15PRO")
        .compose(
            product -> {
              if (product.isPresent()) {
                logger.info("📱 根据产品代码查询: {}", product.get().getName());
              }
              return productDao.findByCategoryId(1L);
            })
        .compose(
            products -> {
              logger.info("📂 分类1的产品数量: {}", products.size());
              products.forEach(p -> logger.info("   - {}", p.getName()));
              return productDao.findActiveProducts();
            })
        .compose(
            products -> {
              logger.info("✅ 活跃产品数量: {}", products.size());
              return io.vertx.core.Future.succeededFuture();
            });
  }

  /** 演示复杂查询 */
  private static io.vertx.core.Future<Void> demoComplexQueries(ProductDao productDao) {
    return productDao
        .findByPriceRange(new BigDecimal("500.00"), new BigDecimal("1500.00"))
        .compose(
            products -> {
              logger.info("💰 价格在500-1500之间的产品: {}", products.size());
              products.forEach(p -> logger.info("   - {}: ${}", p.getName(), p.getPrice()));
              return productDao.findLowStockProducts(30);
            })
        .compose(
            products -> {
              logger.info("📦 库存不足30的产品: {}", products.size());
              products.forEach(
                  p -> logger.info("   - {}: 库存{}", p.getName(), p.getStockQuantity()));
              return productDao.findByNameLike("iPhone");
            })
        .compose(
            products -> {
              logger.info("🔍 名称包含'iPhone'的产品: {}", products.size());
              products.forEach(p -> logger.info("   - {}", p.getName()));
              return io.vertx.core.Future.succeededFuture();
            });
  }

  /** 演示分页查询 */
  private static io.vertx.core.Future<Void> demoPageQueries(ProductDao productDao) {
    return productDao
        .findProductsByPage(1, 3, 1L)
        .compose(
            pageResult -> {
              logger.info("📄 分页查询结果:");
              logger.info(
                  "   总数: {}, 当前页: {}, 页大小: {}",
                  pageResult.getTotal(),
                  pageResult.getCurrent(),
                  pageResult.getSize());
              pageResult
                  .getRecords()
                  .forEach(p -> logger.info("   - {} (分类: {})", p.getName(), p.getCategoryId()));
              return io.vertx.core.Future.succeededFuture();
            });
  }

  /** 演示统计查询 */
  private static io.vertx.core.Future<Void> demoCountQueries(ProductDao productDao) {
    return productDao
        .countActiveProducts()
        .compose(
            count -> {
              logger.info("📊 活跃产品总数: {}", count);
              return productDao.countProductsByCategory(1L);
            })
        .compose(
            count -> {
              logger.info("📂 分类1的产品总数: {}", count);
              return productDao.existsByCode("IPHONE15PRO");
            })
        .compose(
            exists -> {
              logger.info("🔍 产品代码'IPHONE15PRO'是否存在: {}", exists);
              return io.vertx.core.Future.succeededFuture();
            });
  }

  /** 演示字段选择查询 */
  private static io.vertx.core.Future<Void> demoFieldSelectionQueries(ProductDao productDao) {
    return productDao
        .findProductBasicInfo()
        .compose(
            products -> {
              logger.info("🎯 产品基本信息查询结果: {}", products.size());
              products.forEach(
                  p -> {
                    logger.info(
                        "   - ID: {}, 名称: {}, 代码: {}, 价格: ${}, 活跃: {}",
                        p.getId(),
                        p.getName(),
                        p.getCode(),
                        p.getPrice(),
                        p.getActive());
                  });
              return io.vertx.core.Future.succeededFuture();
            });
  }
}
