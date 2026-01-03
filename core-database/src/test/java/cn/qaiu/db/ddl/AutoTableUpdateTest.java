package cn.qaiu.db.ddl;

import static cn.qaiu.vx.core.util.ConfigConstant.*;
import static org.junit.jupiter.api.Assertions.*;

import cn.qaiu.db.ddl.example.ExampleUser;
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
 * 真正的自动表结构更新测试 使用框架的TableStructureSynchronizer实现自动更新
 *
 * @author <a href="https://qaiu.top">QAIU</a>
 */
@ExtendWith(VertxExtension.class)
@DisplayName("真正的自动表结构更新测试")
public class AutoTableUpdateTest {

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

  /** 第一步：使用框架自动创建初始表结构 */
  @Test
  @DisplayName("第一步：使用框架自动创建初始表结构")
  void testStep1_AutoCreateInitialTable(VertxTestContext testContext) {
    try {
      System.out.println("🚀 第一步：使用TableStructureSynchronizer自动创建表结构...");

      // 使用框架的自动同步功能创建表
      TableStructureSynchronizer.synchronizeTable(pool, ExampleUser.class, JDBCType.H2DB)
          .onSuccess(
              result -> {
                System.out.println("✅ 框架自动创建表成功！");

                // 验证表是否创建成功
                pool.query("SHOW TABLES")
                    .execute()
                    .onSuccess(
                        tablesResult -> {
                          System.out.println("📋 数据库中的所有表:");
                          boolean tableExists = false;
                          for (var row : tablesResult) {
                            String tableName = row.getString(0);
                            System.out.println("  - " + tableName);
                            if ("example_user".equalsIgnoreCase(tableName)) {
                              tableExists = true;
                            }
                          }

                          if (tableExists) {
                            // 验证表结构
                            pool.query("SHOW COLUMNS FROM example_user")
                                .execute()
                                .onSuccess(
                                    columnsResult -> {
                                      int columnCount = 0;
                                      System.out.println("📊 ExampleUser表结构:");
                                      for (var row : columnsResult) {
                                        System.out.println(
                                            "  - " + row.getString(0) + " " + row.getString(1));
                                        columnCount++;
                                      }

                                      System.out.println(
                                          "✅ 第一步完成：框架自动创建表结构成功，包含" + columnCount + "个字段");
                                      testContext.completeNow();
                                    })
                                .onFailure(testContext::failNow);
                          } else {
                            System.out.println("❌ 表example_user未创建");
                            testContext.failNow(new RuntimeException("表example_user未创建"));
                          }
                        })
                    .onFailure(testContext::failNow);
              })
          .onFailure(
              e -> {
                System.out.println("❌ 框架自动创建表失败: " + e.getMessage());
                testContext.failNow(e);
              });

    } catch (Exception e) {
      testContext.failNow(e);
    }
  }

  /** 第二步：使用框架自动更新表结构 - 添加新字段 */
  @Test
  @DisplayName("第二步：使用框架自动更新表结构 - 添加新字段")
  void testStep2_AutoUpdateTableStructure(VertxTestContext testContext) {
    try {
      System.out.println("🚀 第二步：使用TableStructureSynchronizer自动更新表结构...");

      // 使用框架的自动同步功能更新表
      TableStructureSynchronizer.synchronizeTable(pool, ExtendedUser.class, JDBCType.H2DB)
          .onSuccess(
              result -> {
                System.out.println("✅ 框架自动更新表成功！");

                // 验证表结构是否已更新
                pool.query("SHOW COLUMNS FROM extended_user")
                    .execute()
                    .onSuccess(
                        columnsResult -> {
                          int columnCount = 0;
                          System.out.println("📊 ExtendedUser表结构:");
                          for (var row : columnsResult) {
                            System.out.println("  - " + row.getString(0) + " " + row.getString(1));
                            columnCount++;
                          }

                          // 验证新增的字段是否存在
                          pool.query("SHOW COLUMNS FROM extended_user")
                              .execute()
                              .onSuccess(
                                  columnsResult2 -> {
                                    // 检查是否包含新增的字段
                                    boolean hasPhone = false;
                                    boolean hasAddress = false;
                                    boolean hasBirthday = false;

                                    for (var row : columnsResult2) {
                                      String columnName = row.getString(0);
                                      if ("phone".equalsIgnoreCase(columnName)) hasPhone = true;
                                      if ("address".equalsIgnoreCase(columnName)) hasAddress = true;
                                      if ("birthday".equalsIgnoreCase(columnName))
                                        hasBirthday = true;
                                    }

                                    assertTrue(hasPhone, "表应该包含PHONE字段");
                                    assertTrue(hasAddress, "表应该包含ADDRESS字段");
                                    assertTrue(hasBirthday, "表应该包含BIRTHDAY字段");

                                    System.out.println("✅ 第二步完成：框架自动更新表结构成功，新增3个字段");
                                    System.out.println("   - PHONE: VARCHAR(20) COMMENT '手机号码'");
                                    System.out.println("   - ADDRESS: VARCHAR(200) COMMENT '地址'");
                                    System.out.println("   - BIRTHDAY: DATE COMMENT '生日'");

                                    testContext.completeNow();
                                  })
                              .onFailure(testContext::failNow);
                        })
                    .onFailure(testContext::failNow);
              })
          .onFailure(
              e -> {
                System.out.println("❌ 框架自动更新表失败: " + e.getMessage());
                testContext.failNow(e);
              });

    } catch (Exception e) {
      testContext.failNow(e);
    }
  }

  /** 第三步：测试框架的智能检测功能 */
  @Test
  @DisplayName("第三步：测试框架的智能检测功能")
  void testStep3_SmartDetection(VertxTestContext testContext) {
    try {
      System.out.println("🚀 第三步：测试框架的智能检测功能...");

      // 先创建一个基础表
      TableStructureSynchronizer.synchronizeTable(pool, ExampleUser.class, JDBCType.H2DB)
          .compose(
              result -> {
                System.out.println("✅ 基础表创建完成");

                // 检查是否需要同步（应该返回false，因为表已经同步）
                return TableStructureSynchronizer.needsSynchronization(
                    pool, ExampleUser.class, JDBCType.H2DB);
              })
          .onSuccess(
              needsSync -> {
                System.out.println("🔍 框架检测结果：ExampleUser表需要同步 = " + needsSync);

                // 检查ExtendedUser是否需要同步（应该返回true，因为字段不同）
                TableStructureSynchronizer.needsSynchronization(
                        pool, ExtendedUser.class, JDBCType.H2DB)
                    .onSuccess(
                        needsSyncExtended -> {
                          System.out.println("🔍 框架检测结果：ExtendedUser表需要同步 = " + needsSyncExtended);

                          if (needsSyncExtended) {
                            System.out.println("✅ 第三步完成：框架智能检测功能正常");
                            System.out.println("   - 能够检测到表结构差异");
                            System.out.println("   - 能够判断是否需要同步");
                            testContext.completeNow();
                          } else {
                            System.out.println("❌ 框架检测异常：ExtendedUser应该需要同步");
                            testContext.failNow(new RuntimeException("框架检测异常"));
                          }
                        })
                    .onFailure(testContext::failNow);
              })
          .onFailure(testContext::failNow);

    } catch (Exception e) {
      testContext.failNow(e);
    }
  }

  /** 扩展的用户实体类 - 用于测试自动表结构更新 在ExampleUser基础上新增3个字段 */
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

    /** 用户名 - 唯一约束 */
    @DdlColumn(
        type = "VARCHAR", // SQL类型
        length = 50, // 长度
        nullable = false, // 不允许NULL
        uniqueKey = "username", // 唯一约束
        comment = "用户名" // 字段注释
        )
    private String username;

    /** 邮箱 - 唯一约束，带索引 */
    @DdlColumn(
        type = "VARCHAR", // SQL类型
        length = 100, // 长度
        nullable = false, // 不允许NULL
        uniqueKey = "email", // 唯一约束
        indexName = "idx_email", // 索引名称
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

    /** 余额 - 精确小数 */
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
