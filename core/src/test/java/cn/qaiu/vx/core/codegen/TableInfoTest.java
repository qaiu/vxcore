package cn.qaiu.vx.core.codegen;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * TableInfo 单元测试
 *
 * @author test
 */
@DisplayName("TableInfo 测试")
class TableInfoTest {

  @Nested
  @DisplayName("构造函数测试")
  class ConstructorTest {

    @Test
    @DisplayName("无参构造函数")
    void testDefaultConstructor() {
      TableInfo info = new TableInfo();
      assertNull(info.getTableName());
      assertNull(info.getSchema());
      assertNull(info.getCatalog());
      assertNotNull(info.getColumns());
      assertTrue(info.getColumns().isEmpty());
      assertNotNull(info.getIndexes());
      assertNotNull(info.getForeignKeys());
    }

    @Test
    @DisplayName("单参数构造函数")
    void testOneArgConstructor() {
      TableInfo info = new TableInfo("user");
      assertEquals("user", info.getTableName());
      assertNull(info.getSchema());
    }

    @Test
    @DisplayName("双参数构造函数")
    void testTwoArgConstructor() {
      TableInfo info = new TableInfo("user", "public");
      assertEquals("user", info.getTableName());
      assertEquals("public", info.getSchema());
    }
  }

  @Nested
  @DisplayName("链式调用测试")
  class FluentSetterTest {

    @Test
    @DisplayName("链式设置所有属性")
    void testFluentSetters() {
      TableInfo info =
          new TableInfo()
              .setTableName("t_user")
              .setSchema("public")
              .setCatalog("mydb")
              .setDescription("用户表")
              .setComment("存储用户信息");

      assertEquals("t_user", info.getTableName());
      assertEquals("public", info.getSchema());
      assertEquals("mydb", info.getCatalog());
      assertEquals("用户表", info.getDescription());
      assertEquals("存储用户信息", info.getComment());
    }
  }

  @Nested
  @DisplayName("列操作测试")
  class ColumnOperationTest {

    private TableInfo tableInfo;

    @BeforeEach
    void setUp() {
      tableInfo = new TableInfo("user");
    }

    @Test
    @DisplayName("添加列")
    void testAddColumn() {
      ColumnInfo col1 = new ColumnInfo("id", "bigint").setPrimaryKey(true);
      ColumnInfo col2 = new ColumnInfo("name", "varchar");

      tableInfo.addColumn(col1).addColumn(col2);

      assertEquals(2, tableInfo.getColumnCount());
      assertEquals(2, tableInfo.getColumns().size());
    }

    @Test
    @DisplayName("获取主键列")
    void testGetPrimaryKeyColumns() {
      tableInfo
          .addColumn(new ColumnInfo("id", "bigint").setPrimaryKey(true))
          .addColumn(new ColumnInfo("name", "varchar"))
          .addColumn(new ColumnInfo("email", "varchar"));

      List<ColumnInfo> pkColumns = tableInfo.getPrimaryKeyColumns();
      assertEquals(1, pkColumns.size());
      assertEquals("id", pkColumns.get(0).getColumnName());
    }

    @Test
    @DisplayName("获取非主键列")
    void testGetNonPrimaryKeyColumns() {
      tableInfo
          .addColumn(new ColumnInfo("id", "bigint").setPrimaryKey(true))
          .addColumn(new ColumnInfo("name", "varchar"))
          .addColumn(new ColumnInfo("email", "varchar"));

      List<ColumnInfo> nonPkColumns = tableInfo.getNonPrimaryKeyColumns();
      assertEquals(2, nonPkColumns.size());
    }

    @Test
    @DisplayName("检查是否有主键")
    void testHasPrimaryKey() {
      assertFalse(tableInfo.hasPrimaryKey());

      tableInfo.addColumn(new ColumnInfo("id", "bigint").setPrimaryKey(true));
      assertTrue(tableInfo.hasPrimaryKey());
    }

    @Test
    @DisplayName("根据列名查找列")
    void testGetColumnByName() {
      tableInfo
          .addColumn(new ColumnInfo("id", "bigint"))
          .addColumn(new ColumnInfo("name", "varchar"));

      ColumnInfo found = tableInfo.getColumnByName("name");
      assertNotNull(found);
      assertEquals("name", found.getColumnName());

      ColumnInfo notFound = tableInfo.getColumnByName("not_exist");
      assertNull(notFound);
    }

    @Test
    @DisplayName("根据列类型查找列")
    void testGetColumnsByType() {
      tableInfo
          .addColumn(new ColumnInfo("id", "bigint"))
          .addColumn(new ColumnInfo("name", "varchar"))
          .addColumn(new ColumnInfo("email", "varchar"));

      List<ColumnInfo> varcharColumns = tableInfo.getColumnsByType("varchar");
      assertEquals(2, varcharColumns.size());

      List<ColumnInfo> bigintColumns = tableInfo.getColumnsByType("bigint");
      assertEquals(1, bigintColumns.size());
    }
  }

  @Nested
  @DisplayName("索引操作测试")
  class IndexOperationTest {

    private TableInfo tableInfo;

    @BeforeEach
    void setUp() {
      tableInfo = new TableInfo("user");
    }

    @Test
    @DisplayName("添加索引")
    void testAddIndex() {
      IndexInfo index1 = new IndexInfo("idx_name").setUnique(false);
      IndexInfo index2 = new IndexInfo("uk_email").setUnique(true);

      tableInfo.addIndex(index1).addIndex(index2);

      assertEquals(2, tableInfo.getIndexes().size());
    }

    @Test
    @DisplayName("获取唯一索引")
    void testGetUniqueIndexes() {
      tableInfo
          .addIndex(new IndexInfo("idx_name").setUnique(false))
          .addIndex(new IndexInfo("uk_email").setUnique(true));

      List<IndexInfo> uniqueIndexes = tableInfo.getUniqueIndexes();
      assertEquals(1, uniqueIndexes.size());
      assertTrue(uniqueIndexes.get(0).isUnique());
    }

    @Test
    @DisplayName("获取普通索引")
    void testGetNormalIndexes() {
      tableInfo
          .addIndex(new IndexInfo("idx_name").setUnique(false))
          .addIndex(new IndexInfo("uk_email").setUnique(true));

      List<IndexInfo> normalIndexes = tableInfo.getNormalIndexes();
      assertEquals(1, normalIndexes.size());
      assertFalse(normalIndexes.get(0).isUnique());
    }
  }

  @Nested
  @DisplayName("外键操作测试")
  class ForeignKeyOperationTest {

    @Test
    @DisplayName("添加外键")
    void testAddForeignKey() {
      TableInfo tableInfo = new TableInfo("order");
      ForeignKeyInfo fk =
          new ForeignKeyInfo().setForeignKeyName("fk_user_id").setReferencedTable("user");

      tableInfo.addForeignKey(fk);

      assertEquals(1, tableInfo.getForeignKeys().size());
    }
  }

  @Nested
  @DisplayName("完整表名测试")
  class FullTableNameTest {

    @Test
    @DisplayName("只有表名")
    void testTableNameOnly() {
      TableInfo info = new TableInfo("user");
      assertEquals("user", info.getFullTableName());
    }

    @Test
    @DisplayName("有 schema")
    void testWithSchema() {
      TableInfo info = new TableInfo("user", "public");
      assertEquals("public.user", info.getFullTableName());
    }

    @Test
    @DisplayName("有 catalog 和 schema")
    void testWithCatalogAndSchema() {
      TableInfo info = new TableInfo().setTableName("user").setSchema("public").setCatalog("mydb");
      assertEquals("mydb.public.user", info.getFullTableName());
    }

    @Test
    @DisplayName("空白 schema 不添加")
    void testEmptySchemaNotAdded() {
      TableInfo info = new TableInfo().setTableName("user").setSchema("  ");
      assertEquals("user", info.getFullTableName());
    }
  }

  @Nested
  @DisplayName("表注释测试")
  class TableCommentTest {

    @Test
    @DisplayName("优先返回 comment")
    void testCommentFirst() {
      TableInfo info = new TableInfo("user").setComment("注释").setDescription("描述");
      assertEquals("注释", info.getTableComment());
    }

    @Test
    @DisplayName("comment 为空时返回 description")
    void testDescriptionWhenCommentEmpty() {
      TableInfo info = new TableInfo("user").setComment("").setDescription("描述");
      assertEquals("描述", info.getTableComment());
    }

    @Test
    @DisplayName("都为空时返回表名")
    void testTableNameWhenBothEmpty() {
      TableInfo info = new TableInfo("user");
      assertEquals("user", info.getTableComment());
    }
  }

  @Nested
  @DisplayName("toString 测试")
  class ToStringTest {

    @Test
    @DisplayName("toString 包含主要信息")
    void testToString() {
      TableInfo info =
          new TableInfo("user", "public")
              .addColumn(new ColumnInfo("id", "bigint"))
              .addColumn(new ColumnInfo("name", "varchar"));

      String str = info.toString();

      assertTrue(str.contains("tableName='user'"));
      assertTrue(str.contains("schema='public'"));
      assertTrue(str.contains("columns=2"));
    }
  }
}
