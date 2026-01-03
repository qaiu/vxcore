package cn.qaiu.db.dsl.lambda.example;

import cn.qaiu.db.dsl.lambda.LambdaQueryWrapper;
import cn.qaiu.db.dsl.lambda.LambdaUtils;
import java.util.Arrays;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 简单的Lambda查询测试 不依赖数据库连接，只测试Lambda表达式解析
 *
 * @author qaiu
 */
public class SimpleLambdaTest {

  private static final Logger logger = LoggerFactory.getLogger(SimpleLambdaTest.class);

  public static void main(String[] args) {
    logger.info("🚀 开始Lambda查询功能测试...");

    // 创建DSL上下文
    DSLContext dslContext = DSL.using(org.jooq.SQLDialect.H2);

    // 测试Lambda表达式解析
    testLambdaExpressionParsing();

    // 测试LambdaQueryWrapper构建
    testLambdaQueryWrapper(dslContext);

    logger.info("✅ Lambda查询功能测试完成！");
  }

  /** 测试Lambda表达式解析 */
  private static void testLambdaExpressionParsing() {
    logger.info("📝 测试Lambda表达式解析...");

    try {
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

      // 验证解析结果
      assert "id".equals(idField) : "id字段解析错误";
      assert "name".equals(nameField) : "name字段解析错误";
      assert "code".equals(codeField) : "code字段解析错误";
      assert "categoryId".equals(categoryIdField) : "categoryId字段解析错误";
      assert "price".equals(priceField) : "price字段解析错误";

      logger.info("✅ Lambda表达式解析测试通过！");

    } catch (Exception e) {
      logger.error("❌ Lambda表达式解析测试失败", e);
      throw new RuntimeException(e);
    }
  }

  /** 测试LambdaQueryWrapper构建 */
  private static void testLambdaQueryWrapper(DSLContext dslContext) {
    logger.info("📝 测试LambdaQueryWrapper构建...");

    try {
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

      logger.info("✅ LambdaQueryWrapper构建测试通过！");

    } catch (Exception e) {
      logger.error("❌ LambdaQueryWrapper构建测试失败", e);
      throw new RuntimeException(e);
    }
  }
}
