package cn.qaiu.vx.core.codegen;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * ForeignKeyInfo 单元测试
 *
 * @author test
 */
@DisplayName("ForeignKeyInfo 测试")
class ForeignKeyInfoTest {

  @Nested
  @DisplayName("构造函数测试")
  class ConstructorTest {

    @Test
    @DisplayName("无参构造函数")
    void testDefaultConstructor() {
      ForeignKeyInfo info = new ForeignKeyInfo();
      assertNull(info.getForeignKeyName());
      assertNull(info.getTableName());
      assertNull(info.getReferencedTableName());
      assertNotNull(info.getColumnNames());
      assertNotNull(info.getReferencedColumnNames());
      assertTrue(info.getColumnNames().isEmpty());
      assertTrue(info.getReferencedColumnNames().isEmpty());
    }

    @Test
    @DisplayName("三参数构造函数")
    void testThreeArgConstructor() {
      ForeignKeyInfo info = new ForeignKeyInfo("fk_order_user", "t_order", "t_user");

      assertEquals("fk_order_user", info.getForeignKeyName());
      assertEquals("t_order", info.getTableName());
      assertEquals("t_user", info.getReferencedTableName());
    }
  }

  @Nested
  @DisplayName("链式调用测试")
  class FluentSetterTest {

    @Test
    @DisplayName("链式设置所有属性")
    void testFluentSetters() {
      ForeignKeyInfo info =
          new ForeignKeyInfo()
              .setForeignKeyName("fk_test")
              .setTableName("t_child")
              .setReferencedTableName("t_parent")
              .setComment("外键注释")
              .setDescription("外键描述");

      assertEquals("fk_test", info.getForeignKeyName());
      assertEquals("t_child", info.getTableName());
      assertEquals("t_parent", info.getReferencedTableName());
      assertEquals("外键注释", info.getComment());
      assertEquals("外键描述", info.getDescription());
    }

    @Test
    @DisplayName("setReferencedTable 是 setReferencedTableName 的别名")
    void testSetReferencedTableAlias() {
      ForeignKeyInfo info = new ForeignKeyInfo().setReferencedTable("t_ref");

      assertEquals("t_ref", info.getReferencedTableName());
    }
  }

  @Nested
  @DisplayName("列名管理测试")
  class ColumnManagementTest {

    @Test
    @DisplayName("添加外键列名")
    void testAddColumnName() {
      ForeignKeyInfo info = new ForeignKeyInfo().addColumnName("user_id").addColumnName("order_id");

      assertEquals(2, info.getColumnNames().size());
      assertTrue(info.getColumnNames().contains("user_id"));
    }

    @Test
    @DisplayName("添加引用列名")
    void testAddReferencedColumnName() {
      ForeignKeyInfo info =
          new ForeignKeyInfo().addReferencedColumnName("id").addReferencedColumnName("code");

      assertEquals(2, info.getReferencedColumnNames().size());
      assertTrue(info.getReferencedColumnNames().contains("id"));
    }

    @Test
    @DisplayName("设置列名列表")
    void testSetColumnNames() {
      ForeignKeyInfo info =
          new ForeignKeyInfo()
              .setColumnNames(Arrays.asList("col1", "col2"))
              .setReferencedColumnNames(Arrays.asList("ref1", "ref2"));

      assertEquals(2, info.getColumnCount());
    }
  }

  @Nested
  @DisplayName("PK/FK 别名方法测试")
  class PkFkAliasTest {

    @Test
    @DisplayName("setPkTableName 设置 referencedTableName")
    void testSetPkTableName() {
      ForeignKeyInfo info = new ForeignKeyInfo().setPkTableName("t_parent");

      assertEquals("t_parent", info.getReferencedTableName());
    }

    @Test
    @DisplayName("setFkTableName 设置 tableName")
    void testSetFkTableName() {
      ForeignKeyInfo info = new ForeignKeyInfo().setFkTableName("t_child");

      assertEquals("t_child", info.getTableName());
    }

    @Test
    @DisplayName("setPkColumnName 添加或更新引用列名")
    void testSetPkColumnName() {
      // 空列表时添加
      ForeignKeyInfo info1 = new ForeignKeyInfo().setPkColumnName("id");
      assertEquals("id", info1.getReferencedColumnNames().get(0));

      // 非空列表时更新第一个
      ForeignKeyInfo info2 =
          new ForeignKeyInfo().addReferencedColumnName("old_id").setPkColumnName("new_id");
      assertEquals("new_id", info2.getReferencedColumnNames().get(0));
    }

    @Test
    @DisplayName("setFkColumnName 添加或更新外键列名")
    void testSetFkColumnName() {
      // 空列表时添加
      ForeignKeyInfo info1 = new ForeignKeyInfo().setFkColumnName("user_id");
      assertEquals("user_id", info1.getColumnNames().get(0));

      // 非空列表时更新第一个
      ForeignKeyInfo info2 =
          new ForeignKeyInfo().addColumnName("old_user_id").setFkColumnName("new_user_id");
      assertEquals("new_user_id", info2.getColumnNames().get(0));
    }
  }

  @Nested
  @DisplayName("列数检测测试")
  class ColumnCountTest {

    @Test
    @DisplayName("空外键")
    void testEmptyForeignKey() {
      ForeignKeyInfo info = new ForeignKeyInfo();
      assertEquals(0, info.getColumnCount());
      assertFalse(info.isSingleColumn());
      assertFalse(info.isMultiColumn());
    }

    @Test
    @DisplayName("单列外键")
    void testSingleColumnForeignKey() {
      ForeignKeyInfo info = new ForeignKeyInfo().addColumnName("user_id");

      assertTrue(info.isSingleColumn());
      assertFalse(info.isMultiColumn());
    }

    @Test
    @DisplayName("多列外键")
    void testMultiColumnForeignKey() {
      ForeignKeyInfo info = new ForeignKeyInfo().addColumnName("user_id").addColumnName("order_id");

      assertFalse(info.isSingleColumn());
      assertTrue(info.isMultiColumn());
    }
  }

  @Nested
  @DisplayName("列名获取测试")
  class ColumnRetrievalTest {

    @Test
    @DisplayName("获取第一个列名")
    void testGetFirstColumnName() {
      ForeignKeyInfo info = new ForeignKeyInfo().addColumnName("first").addColumnName("second");

      assertEquals("first", info.getFirstColumnName());
    }

    @Test
    @DisplayName("获取第一个引用列名")
    void testGetFirstReferencedColumnName() {
      ForeignKeyInfo info =
          new ForeignKeyInfo()
              .addReferencedColumnName("ref_first")
              .addReferencedColumnName("ref_second");

      assertEquals("ref_first", info.getFirstReferencedColumnName());
    }

    @Test
    @DisplayName("空列表返回 null")
    void testEmptyListReturnsNull() {
      ForeignKeyInfo info = new ForeignKeyInfo();
      assertNull(info.getFirstColumnName());
      assertNull(info.getFirstReferencedColumnName());
    }
  }

  @Nested
  @DisplayName("列包含检查测试")
  class ColumnContainsTest {

    @Test
    @DisplayName("containsColumn")
    void testContainsColumn() {
      ForeignKeyInfo info = new ForeignKeyInfo().addColumnName("user_id");

      assertTrue(info.containsColumn("user_id"));
      assertFalse(info.containsColumn("order_id"));
    }

    @Test
    @DisplayName("referencesColumn")
    void testReferencesColumn() {
      ForeignKeyInfo info = new ForeignKeyInfo().addReferencedColumnName("id");

      assertTrue(info.referencesColumn("id"));
      assertFalse(info.referencesColumn("code"));
    }
  }

  @Nested
  @DisplayName("getColumnMapping 测试")
  class ColumnMappingTest {

    @Test
    @DisplayName("正常映射")
    void testNormalMapping() {
      ForeignKeyInfo info =
          new ForeignKeyInfo()
              .addColumnName("user_id")
              .addColumnName("order_id")
              .addReferencedColumnName("id")
              .addReferencedColumnName("id");

      String mapping = info.getColumnMapping();
      assertEquals("user_id -> id, order_id -> id", mapping);
    }

    @Test
    @DisplayName("单列映射")
    void testSingleColumnMapping() {
      ForeignKeyInfo info =
          new ForeignKeyInfo().addColumnName("user_id").addReferencedColumnName("id");

      assertEquals("user_id -> id", info.getColumnMapping());
    }

    @Test
    @DisplayName("列数不匹配返回 null")
    void testMismatchedColumnsReturnsNull() {
      ForeignKeyInfo info =
          new ForeignKeyInfo()
              .addColumnName("user_id")
              .addColumnName("order_id")
              .addReferencedColumnName("id"); // 只有一个引用列

      assertNull(info.getColumnMapping());
    }
  }

  @Nested
  @DisplayName("getForeignKeyComment 测试")
  class ForeignKeyCommentTest {

    @Test
    @DisplayName("优先返回 comment")
    void testCommentFirst() {
      ForeignKeyInfo info =
          new ForeignKeyInfo().setForeignKeyName("fk_test").setComment("外键注释").setDescription("描述");

      assertEquals("外键注释", info.getForeignKeyComment());
    }

    @Test
    @DisplayName("comment 为空时返回 description")
    void testDescriptionWhenCommentEmpty() {
      ForeignKeyInfo info =
          new ForeignKeyInfo().setForeignKeyName("fk_test").setComment("").setDescription("描述");

      assertEquals("描述", info.getForeignKeyComment());
    }

    @Test
    @DisplayName("都为空时返回 foreignKeyName")
    void testNameWhenBothEmpty() {
      ForeignKeyInfo info = new ForeignKeyInfo().setForeignKeyName("fk_order_user");

      assertEquals("fk_order_user", info.getForeignKeyComment());
    }
  }

  @Nested
  @DisplayName("toString 测试")
  class ToStringTest {

    @Test
    @DisplayName("toString 包含所有字段")
    void testToString() {
      ForeignKeyInfo info =
          new ForeignKeyInfo("fk_test", "t_order", "t_user")
              .addColumnName("user_id")
              .addReferencedColumnName("id");

      String str = info.toString();

      assertTrue(str.contains("foreignKeyName='fk_test'"));
      assertTrue(str.contains("tableName='t_order'"));
      assertTrue(str.contains("referencedTableName='t_user'"));
    }
  }
}
