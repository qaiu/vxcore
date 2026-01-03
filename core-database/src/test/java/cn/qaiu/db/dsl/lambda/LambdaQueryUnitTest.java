package cn.qaiu.db.dsl.lambda;

import static org.junit.jupiter.api.Assertions.*;

import cn.qaiu.db.dsl.lambda.example.Product;
import java.util.Arrays;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Lambda查询单元测试 不依赖数据库连接，只测试Lambda表达式解析和SQL构建
 *
 * @author qaiu
 */
public class LambdaQueryUnitTest {

  private static final Logger logger = LoggerFactory.getLogger(LambdaQueryUnitTest.class);
  private DSLContext dslContext;

  @BeforeEach
  void setUp() {
    dslContext = DSL.using(org.jooq.SQLDialect.H2);
  }

  @Test
  @DisplayName("测试Lambda表达式解析")
  void testLambdaExpressionParsing() {
    logger.info("📝 测试Lambda表达式解析...");

    // 测试Product实体的字段解析
    String idField = LambdaUtils.getFieldName(Product::getId);
    String nameField = LambdaUtils.getFieldName(Product::getName);
    String codeField = LambdaUtils.getFieldName(Product::getCode);
    String categoryIdField = LambdaUtils.getFieldName(Product::getCategoryId);
    String priceField = LambdaUtils.getFieldName(Product::getPrice);

    logger.info("✅ 字段解析结果:");
    logger.info("  - id: {}", idField);
    logger.info("  - name: {}", nameField);
    logger.info("  - code: {}", codeField);
    logger.info("  - categoryId: {}", categoryIdField);
    logger.info("  - price: {}", priceField);

    // 验证解析结果 - LambdaUtils返回数据库列名（从@DdlColumn注解）
    assertEquals("product_id", idField, "id字段解析错误");
    assertEquals("product_name", nameField, "name字段解析错误");
    assertEquals("product_code", codeField, "code字段解析错误");
    assertEquals("category_id", categoryIdField, "categoryId字段解析错误");
    assertEquals("price", priceField, "price字段解析错误");

    logger.info("✅ Lambda表达式解析测试通过！");
  }

  @Test
  @DisplayName("测试LambdaQueryWrapper构建")
  void testLambdaQueryWrapper() {
    logger.info("📝 测试LambdaQueryWrapper构建...");

    // 创建LambdaQueryWrapper
    LambdaQueryWrapper<Product> wrapper =
        new LambdaQueryWrapper<Product>(dslContext, DSL.table("products"), Product.class);

    // 测试各种查询条件
    wrapper
        .eq(Product::getId, 1L)
        .like(Product::getName, "手机")
        .in(Product::getCategoryId, Arrays.asList(1L, 2L, 3L))
        .gt(Product::getPrice, new java.math.BigDecimal("100.00"))
        .eq(Product::getActive, true)
        .orderByAsc(Product::getName)
        .orderByDesc(Product::getPrice)
        .limit(10)
        .offset(0);

    // 构建查询
    String sql = wrapper.buildSelect().getSQL();
    logger.info("✅ 生成的SQL: {}", sql);

    // 构建计数查询
    String countSql = wrapper.buildCount().getSQL();
    logger.info("✅ 生成的计数SQL: {}", countSql);

    // 构建存在查询
    String existsSql = wrapper.buildExists().getSQL();
    logger.info("✅ 生成的存在查询SQL: {}", existsSql);

    // 验证SQL包含预期的条件
    assertTrue(sql.contains("product_id = cast(? as bigint)"), "SQL应包含id条件");
    assertTrue(sql.contains("product_name like cast(? as varchar)"), "SQL应包含name条件");
    assertTrue(sql.contains("category_id in"), "SQL应包含category_id条件");
    assertTrue(sql.contains("price > cast(? as numeric"), "SQL应包含price条件");
    assertTrue(sql.contains("is_active = cast(? as boolean)"), "SQL应包含active条件");
    assertTrue(sql.contains("order by product_name asc, price desc"), "SQL应包含排序条件");
    assertTrue(sql.contains("offset ? rows fetch next ? rows only"), "SQL应包含分页条件");

    logger.info("✅ LambdaQueryWrapper构建测试通过！");
  }

  @Test
  @DisplayName("测试DdlColumn value字段")
  void testDdlColumnValueField() {
    logger.info("📝 测试DdlColumn value字段...");

    // 测试Product实体中使用@DdlColumn(value="category_id")的字段
    String categoryIdField = LambdaUtils.getFieldName(Product::getCategoryId);

    // 验证字段名映射是否正确 - LambdaUtils返回数据库列名
    assertEquals("category_id", categoryIdField, "categoryId字段解析错误");

    logger.info("✅ DdlColumn value字段测试通过！");
  }

  @Test
  @DisplayName("测试复杂查询条件")
  void testComplexQueryConditions() {
    logger.info("📝 测试复杂查询条件...");

    LambdaQueryWrapper<Product> wrapper =
        new LambdaQueryWrapper<Product>(dslContext, DSL.table("products"), Product.class);

    // 构建复杂查询条件
    wrapper
        .eq(Product::getId, 1L)
        .ne(Product::getActive, false)
        .like(Product::getName, "%手机%")
        .notLike(Product::getCode, "%test%")
        .in(Product::getCategoryId, Arrays.asList(1L, 2L, 3L))
        .notIn(Product::getId, Arrays.asList(999L, 1000L))
        .gt(Product::getPrice, new java.math.BigDecimal("100.00"))
        .ge(Product::getPrice, new java.math.BigDecimal("50.00"))
        .lt(Product::getStockQuantity, 100)
        .le(Product::getStockQuantity, 50)
        .isNull(Product::getDescription)
        .isNotNull(Product::getName)
        .between(
            Product::getPrice,
            new java.math.BigDecimal("10.00"),
            new java.math.BigDecimal("1000.00"));

    String sql = wrapper.buildSelect().getSQL();
    logger.info("✅ 复杂查询SQL: {}", sql);

    // 验证复杂条件
    assertTrue(sql.contains("product_id = cast(? as bigint)"), "应包含等于条件");
    assertTrue(sql.contains("is_active <> cast(? as boolean)"), "应包含不等于条件");
    assertTrue(sql.contains("product_name like cast(? as varchar)"), "应包含LIKE条件");
    assertTrue(sql.contains("product_code not like cast(? as varchar)"), "应包含NOT LIKE条件");
    assertTrue(sql.contains("category_id in"), "应包含IN条件");
    assertTrue(sql.contains("product_id not in"), "应包含NOT IN条件");
    assertTrue(sql.contains("price > cast(? as numeric"), "应包含大于条件");
    assertTrue(sql.contains("price >= cast(? as numeric"), "应包含大于等于条件");
    assertTrue(sql.contains("stock_quantity < cast(? as int)"), "应包含小于条件");
    assertTrue(sql.contains("stock_quantity <= cast(? as int)"), "应包含小于等于条件");
    assertTrue(sql.contains("description is null"), "应包含IS NULL条件");
    assertTrue(sql.contains("product_name is not null"), "应包含IS NOT NULL条件");
    assertTrue(
        sql.contains("price between cast(? as numeric") && sql.contains(") and cast(? as numeric"),
        "应包含BETWEEN条件");

    logger.info("✅ 复杂查询条件测试通过！");
  }
}
