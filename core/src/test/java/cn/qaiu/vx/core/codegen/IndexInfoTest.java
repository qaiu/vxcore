package cn.qaiu.vx.core.codegen;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * IndexInfo 单元测试
 *
 * @author test
 */
@DisplayName("IndexInfo 测试")
class IndexInfoTest {

  @Nested
  @DisplayName("构造函数测试")
  class ConstructorTest {

    @Test
    @DisplayName("无参构造函数")
    void testDefaultConstructor() {
      IndexInfo info = new IndexInfo();
      assertNull(info.getIndexName());
      assertNull(info.getTableName());
      assertFalse(info.isUnique());
      assertFalse(info.isPrimaryKey());
      assertNotNull(info.getColumnNames());
      assertTrue(info.getColumnNames().isEmpty());
    }

    @Test
    @DisplayName("单参数构造函数")
    void testSingleArgConstructor() {
      IndexInfo info = new IndexInfo("idx_user_name");
      assertEquals("idx_user_name", info.getIndexName());
      assertNull(info.getTableName());
    }

    @Test
    @DisplayName("双参数构造函数")
    void testTwoArgConstructor() {
      IndexInfo info = new IndexInfo("idx_user_name", "t_user");
      assertEquals("idx_user_name", info.getIndexName());
      assertEquals("t_user", info.getTableName());
    }
  }

  @Nested
  @DisplayName("链式调用测试")
  class FluentSetterTest {

    @Test
    @DisplayName("链式设置所有属性")
    void testFluentSetters() {
      IndexInfo info =
          new IndexInfo()
              .setIndexName("idx_user_email")
              .setTableName("t_user")
              .setUnique(true)
              .setPrimaryKey(false)
              .setComment("用户邮箱索引")
              .setDescription("唯一索引");

      assertEquals("idx_user_email", info.getIndexName());
      assertEquals("t_user", info.getTableName());
      assertTrue(info.isUnique());
      assertFalse(info.isPrimaryKey());
      assertEquals("用户邮箱索引", info.getComment());
      assertEquals("唯一索引", info.getDescription());
    }
  }

  @Nested
  @DisplayName("列名管理测试")
  class ColumnManagementTest {

    @Test
    @DisplayName("添加单个列名")
    void testAddColumnName() {
      IndexInfo info = new IndexInfo("idx_test").addColumnName("user_id").addColumnName("order_id");

      assertEquals(2, info.getColumnNames().size());
      assertTrue(info.getColumnNames().contains("user_id"));
      assertTrue(info.getColumnNames().contains("order_id"));
    }

    @Test
    @DisplayName("设置列名列表")
    void testSetColumnNames() {
      IndexInfo info =
          new IndexInfo("idx_composite").setColumnNames(Arrays.asList("col1", "col2", "col3"));

      assertEquals(3, info.getColumnCount());
    }
  }

  @Nested
  @DisplayName("列数检测测试")
  class ColumnCountTest {

    @Test
    @DisplayName("空索引")
    void testEmptyIndex() {
      IndexInfo info = new IndexInfo("idx_empty");
      assertEquals(0, info.getColumnCount());
      assertFalse(info.isSingleColumn());
      assertFalse(info.isMultiColumn());
    }

    @Test
    @DisplayName("单列索引")
    void testSingleColumnIndex() {
      IndexInfo info = new IndexInfo("idx_single").addColumnName("user_id");

      assertEquals(1, info.getColumnCount());
      assertTrue(info.isSingleColumn());
      assertFalse(info.isMultiColumn());
    }

    @Test
    @DisplayName("多列索引")
    void testMultiColumnIndex() {
      IndexInfo info =
          new IndexInfo("idx_multi").addColumnName("user_id").addColumnName("order_id");

      assertEquals(2, info.getColumnCount());
      assertFalse(info.isSingleColumn());
      assertTrue(info.isMultiColumn());
    }
  }

  @Nested
  @DisplayName("列名获取测试")
  class ColumnNameRetrievalTest {

    @Test
    @DisplayName("获取第一个列名")
    void testGetFirstColumnName() {
      IndexInfo info = new IndexInfo("idx_test").addColumnName("first").addColumnName("second");

      assertEquals("first", info.getFirstColumnName());
    }

    @Test
    @DisplayName("获取最后一个列名")
    void testGetLastColumnName() {
      IndexInfo info = new IndexInfo("idx_test").addColumnName("first").addColumnName("last");

      assertEquals("last", info.getLastColumnName());
    }

    @Test
    @DisplayName("空列表返回 null")
    void testEmptyListReturnsNull() {
      IndexInfo info = new IndexInfo("idx_empty");
      assertNull(info.getFirstColumnName());
      assertNull(info.getLastColumnName());
    }
  }

  @Nested
  @DisplayName("containsColumn 测试")
  class ContainsColumnTest {

    @Test
    @DisplayName("包含指定列")
    void testContainsColumn() {
      IndexInfo info = new IndexInfo("idx_test").addColumnName("user_id").addColumnName("order_id");

      assertTrue(info.containsColumn("user_id"));
      assertTrue(info.containsColumn("order_id"));
      assertFalse(info.containsColumn("product_id"));
    }
  }

  @Nested
  @DisplayName("getIndexComment 测试")
  class IndexCommentTest {

    @Test
    @DisplayName("优先返回 comment")
    void testCommentFirst() {
      IndexInfo info = new IndexInfo("idx_test").setComment("注释").setDescription("描述");

      assertEquals("注释", info.getIndexComment());
    }

    @Test
    @DisplayName("comment 为空时返回 description")
    void testDescriptionWhenCommentEmpty() {
      IndexInfo info = new IndexInfo("idx_test").setComment("").setDescription("描述");

      assertEquals("描述", info.getIndexComment());
    }

    @Test
    @DisplayName("都为空时返回 indexName")
    void testIndexNameWhenBothEmpty() {
      IndexInfo info = new IndexInfo("idx_user");
      assertEquals("idx_user", info.getIndexComment());
    }
  }

  @Nested
  @DisplayName("toString 测试")
  class ToStringTest {

    @Test
    @DisplayName("toString 包含所有字段")
    void testToString() {
      IndexInfo info =
          new IndexInfo("idx_pk", "t_user").setUnique(true).setPrimaryKey(true).addColumnName("id");

      String str = info.toString();

      assertTrue(str.contains("indexName='idx_pk'"));
      assertTrue(str.contains("tableName='t_user'"));
      assertTrue(str.contains("unique=true"));
      assertTrue(str.contains("primaryKey=true"));
    }
  }
}
