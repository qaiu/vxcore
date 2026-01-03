package cn.qaiu.db.ddl;

import static cn.qaiu.vx.core.util.ConfigConstant.*;
import static org.junit.jupiter.api.Assertions.*;

import cn.qaiu.db.pool.JDBCType;
import cn.qaiu.vx.core.util.VertxHolder;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.LocalMap;
import io.vertx.core.shareddata.SharedData;
import io.vertx.jdbcclient.JDBCConnectOptions;
import io.vertx.jdbcclient.JDBCPool;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.PoolOptions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * 框架字段解析测试 验证TableMetadata.fromClass是否正确解析所有字段
 *
 * @author <a href="https://qaiu.top">QAIU</a>
 */
@ExtendWith(VertxExtension.class)
@DisplayName("框架字段解析测试")
public class FieldParsingTest {

  private Pool pool;
  private Vertx vertx;

  @BeforeEach
  void setUp(Vertx vertx, VertxTestContext testContext) {
    this.vertx = vertx;

    // 设置配置
    SharedData sharedData = vertx.sharedData();
    LocalMap<String, Object> localMap = sharedData.getLocalMap(LOCAL);
    localMap.put(GLOBAL_CONFIG, JsonObject.of("baseLocations", "cn.qaiu"));
    localMap.put(CUSTOM_CONFIG, JsonObject.of("baseLocations", "cn.qaiu"));

    VertxHolder.init(vertx);

    // 创建H2数据库连接 - 使用MySQL模式
    PoolOptions poolOptions = new PoolOptions().setMaxSize(5);
    JDBCConnectOptions connectOptions =
        new JDBCConnectOptions()
            .setJdbcUrl(
                "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_ON_EXIT=FALSE")
            .setUser("sa")
            .setPassword("");

    pool = JDBCPool.pool(vertx, connectOptions, poolOptions);

    testContext.completeNow();
  }

  @AfterEach
  void tearDown(VertxTestContext testContext) {
    if (pool != null) {
      pool.close();
    }
    if (vertx != null) {
      vertx.close(testContext.succeedingThenComplete());
    }
  }

  /** 测试字段解析 */
  @Test
  @DisplayName("测试字段解析")
  void testFieldParsing(VertxTestContext testContext) {
    try {
      System.out.println("🔍 测试TableMetadata字段解析...");

      // 解析ExtendedUser类的字段
      TableMetadata metadata = TableMetadata.fromClass(ExtendedUser.class, JDBCType.H2DB);

      System.out.println("📊 ExtendedUser表元数据:");
      System.out.println("  - 表名: " + metadata.getTableName());
      System.out.println("  - 版本: " + metadata.getVersion());
      System.out.println("  - 自动同步: " + metadata.isAutoSync());
      System.out.println("  - 字段数量: " + metadata.getColumns().size());

      System.out.println("📋 字段列表:");
      for (ColumnMetadata column : metadata.getColumns().values()) {
        System.out.println(
            "  - "
                + column.getName()
                + ": "
                + column.getType()
                + (column.getLength() > 0 ? "(" + column.getLength() + ")" : "")
                + (column.getComment() != null ? " COMMENT '" + column.getComment() + "'" : ""));
      }

      // 验证字段数量
      assertEquals(13, metadata.getColumns().size(), "ExtendedUser应该有13个字段");

      // 验证关键字段是否存在
      assertTrue(metadata.getColumns().containsKey("phone"), "应该包含phone字段");
      assertTrue(metadata.getColumns().containsKey("address"), "应该包含address字段");
      assertTrue(metadata.getColumns().containsKey("birthday"), "应该包含birthday字段");

      // 验证birthday字段的类型
      ColumnMetadata birthdayColumn = metadata.getColumns().get("birthday");
      assertNotNull(birthdayColumn, "birthday字段不应该为null");
      assertEquals("DATE", birthdayColumn.getType(), "birthday字段类型应该是DATE");

      System.out.println("✅ 字段解析测试通过！");
      testContext.completeNow();

    } catch (Exception e) {
      System.out.println("❌ 字段解析测试失败: " + e.getMessage());
      e.printStackTrace();
      testContext.failNow(e);
    }
  }

  /** 简化的扩展用户实体类 - 用于测试字段解析 */
  @DdlTable(
      value = "extended_user", // 表名
      keyFields = "id", // 主键字段
      version = 2, // 版本号增加
      autoSync = true, // 启用自动同步
      comment = "扩展用户表", // 表注释
      charset = "utf8mb4", // 字符集
      collate = "utf8mb4_unicode_ci", // 排序规则
      engine = "InnoDB", // 存储引擎
      dbtype = "mysql" // 数据库类型
      )
  public static class ExtendedUser {

    /** 用户ID - 主键，自增 */
    @DdlColumn(
        type = "BIGINT", // SQL类型
        autoIncrement = true, // 自增
        nullable = false, // 不允许NULL
        comment = "用户ID" // 字段注释
        )
    private Long id;

    /** 用户名 */
    @DdlColumn(
        type = "VARCHAR", // SQL类型
        length = 50, // 长度
        nullable = false, // 不允许NULL
        comment = "用户名" // 字段注释
        )
    private String username;

    /** 邮箱 */
    @DdlColumn(
        type = "VARCHAR", // SQL类型
        length = 100, // 长度
        nullable = false, // 不允许NULL
        comment = "邮箱地址" // 字段注释
        )
    private String email;

    /** 密码 */
    @DdlColumn(
        type = "VARCHAR", // SQL类型
        length = 255, // 长度
        nullable = false, // 不允许NULL
        comment = "密码(加密)" // 字段注释
        )
    private String password;

    /** 年龄 */
    @DdlColumn(
        type = "INT", // SQL类型
        nullable = true, // 允许NULL
        defaultValue = "0", // 默认值
        comment = "年龄" // 字段注释
        )
    private Integer age;

    /** 余额 */
    @DdlColumn(
        type = "DECIMAL", // SQL类型
        precision = 10, // 精度
        scale = 2, // 小数位数
        nullable = false, // 不允许NULL
        defaultValue = "0.00", // 默认值
        comment = "账户余额" // 字段注释
        )
    private java.math.BigDecimal balance;

    /** 是否激活 */
    @DdlColumn(
        type = "BOOLEAN", // SQL类型
        nullable = false, // 不允许NULL
        defaultValue = "true", // 默认值
        comment = "是否激活" // 字段注释
        )
    private Boolean active;

    /** 创建时间 */
    @DdlColumn(
        type = "TIMESTAMP", // SQL类型
        nullable = false, // 不允许NULL
        defaultValue = "CURRENT_TIMESTAMP", // 默认值
        defaultValueIsFunction = true, // 默认值是函数
        comment = "创建时间" // 字段注释
        )
    private java.time.LocalDateTime createTime;

    /** 更新时间 */
    @DdlColumn(
        type = "TIMESTAMP", // SQL类型
        nullable = true, // 允许NULL
        comment = "更新时间" // 字段注释
        )
    private java.time.LocalDateTime updateTime;

    /** 备注 */
    @DdlColumn(
        type = "TEXT", // SQL类型
        nullable = true, // 允许NULL
        comment = "备注信息" // 字段注释
        )
    private String remark;

    // ========== 新增字段 ==========

    /** 手机号码 - 新增字段 */
    @DdlColumn(
        type = "VARCHAR", // SQL类型
        length = 20, // 长度
        nullable = true, // 允许NULL
        comment = "手机号码" // 字段注释
        )
    private String phone;

    /** 地址 - 新增字段 */
    @DdlColumn(
        type = "VARCHAR", // SQL类型
        length = 200, // 长度
        nullable = true, // 允许NULL
        comment = "地址" // 字段注释
        )
    private String address;

    /** 生日 - 新增字段 */
    @DdlColumn(
        type = "DATE", // SQL类型
        nullable = true, // 允许NULL
        comment = "生日" // 字段注释
        )
    private java.time.LocalDate birthday;

    // Getter和Setter方法
    public Long getId() {
      return id;
    }

    public void setId(Long id) {
      this.id = id;
    }

    public String getUsername() {
      return username;
    }

    public void setUsername(String username) {
      this.username = username;
    }

    public String getEmail() {
      return email;
    }

    public void setEmail(String email) {
      this.email = email;
    }

    public String getPassword() {
      return password;
    }

    public void setPassword(String password) {
      this.password = password;
    }

    public Integer getAge() {
      return age;
    }

    public void setAge(Integer age) {
      this.age = age;
    }

    public java.math.BigDecimal getBalance() {
      return balance;
    }

    public void setBalance(java.math.BigDecimal balance) {
      this.balance = balance;
    }

    public Boolean getActive() {
      return active;
    }

    public void setActive(Boolean active) {
      this.active = active;
    }

    public java.time.LocalDateTime getCreateTime() {
      return createTime;
    }

    public void setCreateTime(java.time.LocalDateTime createTime) {
      this.createTime = createTime;
    }

    public java.time.LocalDateTime getUpdateTime() {
      return updateTime;
    }

    public void setUpdateTime(java.time.LocalDateTime updateTime) {
      this.updateTime = updateTime;
    }

    public String getRemark() {
      return remark;
    }

    public void setRemark(String remark) {
      this.remark = remark;
    }

    // 新增字段的Getter和Setter
    public String getPhone() {
      return phone;
    }

    public void setPhone(String phone) {
      this.phone = phone;
    }

    public String getAddress() {
      return address;
    }

    public void setAddress(String address) {
      this.address = address;
    }

    public java.time.LocalDate getBirthday() {
      return birthday;
    }

    public void setBirthday(java.time.LocalDate birthday) {
      this.birthday = birthday;
    }
  }
}
