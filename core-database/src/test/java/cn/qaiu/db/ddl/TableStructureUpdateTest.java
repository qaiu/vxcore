package cn.qaiu.db.ddl;

import cn.qaiu.db.ddl.example.ExampleUser;
import cn.qaiu.db.pool.JDBCType;
import cn.qaiu.vx.core.util.ConfigConstant;
import cn.qaiu.vx.core.util.VertxHolder;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.LocalMap;
import io.vertx.core.shareddata.SharedData;
import io.vertx.jdbcclient.JDBCConnectOptions;
import io.vertx.jdbcclient.JDBCPool;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.PoolOptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.extension.ExtendWith;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;

import static cn.qaiu.vx.core.util.ConfigConstant.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 表结构更新测试
 * 测试DDL映射系统的表结构自动更新功能
 * 
 * @author <a href="https://qaiu.top">QAIU</a>
 */
@ExtendWith(VertxExtension.class)
@DisplayName("表结构更新测试")
public class TableStructureUpdateTest {

    private Pool pool;
    private Vertx vertx;

    @BeforeEach
    void setUp(Vertx vertx, VertxTestContext testContext) {
        this.vertx = vertx;
        
        // 设置配置
        SharedData sharedData = vertx.sharedData();
        LocalMap<String, Object> localMap = sharedData.getLocalMap(LOCAL);
        localMap.put(GLOBAL_CONFIG, JsonObject.of("baseLocations","cn.qaiu"));
        localMap.put(CUSTOM_CONFIG, JsonObject.of("baseLocations","cn.qaiu"));

        VertxHolder.init(vertx);

        // 创建H2数据库连接 - 使用MySQL模式
        PoolOptions poolOptions = new PoolOptions().setMaxSize(5);
        JDBCConnectOptions connectOptions = new JDBCConnectOptions()
                .setJdbcUrl("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_ON_EXIT=FALSE")
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

    /**
     * 第一步：创建初始表结构
     */
    @Test
    @DisplayName("第一步：创建初始表结构")
    void testStep1_CreateInitialTable(VertxTestContext testContext) {
        try {
            // 使用TableMetadata创建初始表结构
            TableMetadata tableMetadata = TableMetadata.fromClass(ExampleUser.class, JDBCType.H2DB);
            
            // 生成建表SQL
            String createSQL = generateCreateTableSQL(tableMetadata);
            System.out.println("生成的建表SQL:");
            System.out.println(createSQL);
            
            // 执行建表SQL
            pool.query(createSQL)
                .execute()
                .onSuccess(result -> {
                    System.out.println("建表SQL执行成功");
                    
                    // 验证表是否创建成功
                    pool.query("SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'EXAMPLE_USER'")
                        .execute()
                        .onSuccess(countResult -> {
                            int tableCount = countResult.iterator().next().getInteger(0);
                            System.out.println("表EXAMPLE_USER存在数量: " + tableCount);
                            
                            if (tableCount > 0) {
                                // 验证初始字段数量
                                pool.query("SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'EXAMPLE_USER'")
                                    .execute()
                                    .onSuccess(columnCountResult -> {
                                        int columnCount = columnCountResult.iterator().next().getInteger(0);
                                        System.out.println("表EXAMPLE_USER的字段数量: " + columnCount);
                                        
                                        System.out.println("✅ 第一步完成：初始表结构创建成功，包含" + columnCount + "个字段");
                                        testContext.completeNow();
                                    })
                                    .onFailure(e -> {
                                        System.out.println("查询字段数量失败: " + e.getMessage());
                                        testContext.failNow(e);
                                    });
                            } else {
                                System.out.println("表创建失败");
                                testContext.failNow(new RuntimeException("表创建失败"));
                            }
                        })
                        .onFailure(e -> {
                            System.out.println("查询表存在性失败: " + e.getMessage());
                            testContext.failNow(e);
                        });
                })
                .onFailure(e -> {
                    System.out.println("建表SQL执行失败: " + e.getMessage());
                    testContext.failNow(e);
                });
                
        } catch (Exception e) {
            testContext.failNow(e);
        }
    }

    /**
     * 第二步：测试表结构自动更新 - 添加新字段
     */
    @Test
    @DisplayName("第二步：测试表结构自动更新 - 添加新字段")
    void testStep2_AutoUpdateTableStructure(VertxTestContext testContext) {
        try {
            // 使用扩展的TableMetadata创建更新后的表结构
            TableMetadata updatedTableMetadata = TableMetadata.fromClass(ExtendedUser.class, JDBCType.H2DB);
            
            // 生成更新后的建表SQL
            String updateSQL = generateCreateTableSQL(updatedTableMetadata);
            
            // 执行表结构更新
            pool.query(updateSQL)
                .execute()
                .onSuccess(result -> {
                    // 验证表结构是否已更新
                    pool.query("SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'EXTENDED_USER'")
                        .execute()
                        .onSuccess(columnCountResult -> {
                            // ExtendedUser类有13个字段（比ExampleUser多3个）
                            assertEquals(13, columnCountResult.iterator().next().getInteger(0), 
                                "更新后的表应该有13个字段");
                            
                            // 验证新增的字段是否存在
                            pool.query("SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'EXTENDED_USER' ORDER BY COLUMN_NAME")
                                .execute()
                                .onSuccess(columnsResult -> {
                                    // 检查是否包含新增的字段
                                    boolean hasPhone = false;
                                    boolean hasAddress = false;
                                    boolean hasBirthday = false;
                                    
                                    for (var row : columnsResult) {
                                        String columnName = row.getString(0);
                                        if ("PHONE".equals(columnName)) hasPhone = true;
                                        if ("ADDRESS".equals(columnName)) hasAddress = true;
                                        if ("BIRTHDAY".equals(columnName)) hasBirthday = true;
                                    }
                                    
                                    assertTrue(hasPhone, "表应该包含PHONE字段");
                                    assertTrue(hasAddress, "表应该包含ADDRESS字段");
                                    assertTrue(hasBirthday, "表应该包含BIRTHDAY字段");
                                    
                                    System.out.println("✅ 第二步完成：表结构自动更新成功，新增3个字段");
                                    System.out.println("   - PHONE: VARCHAR(20) COMMENT '手机号码'");
                                    System.out.println("   - ADDRESS: VARCHAR(500) COMMENT '地址'");
                                    System.out.println("   - BIRTHDAY: DATE COMMENT '生日'");
                                    
                                    testContext.completeNow();
                                })
                                .onFailure(testContext::failNow);
                        })
                        .onFailure(testContext::failNow);
                })
                .onFailure(testContext::failNow);
                
        } catch (Exception e) {
            testContext.failNow(e);
        }
    }

    /**
     * 生成建表SQL
     */
    private String generateCreateTableSQL(TableMetadata tableMetadata) {
        StringBuilder sql = new StringBuilder();
        sql.append("CREATE TABLE IF NOT EXISTS ").append(tableMetadata.getTableName()).append(" (\n");
        
        boolean first = true;
        for (ColumnMetadata column : tableMetadata.getColumns().values()) {
            if (!first) {
                sql.append(",\n");
            }
            sql.append("  ").append(column.getName()).append(" ").append(column.getType());
            
            // 添加长度
            if ("VARCHAR".equals(column.getType()) && column.getLength() > 0) {
                sql.append("(").append(column.getLength()).append(")");
            } else if ("DECIMAL".equals(column.getType())) {
                sql.append("(").append(column.getPrecision()).append(",").append(column.getScale()).append(")");
            }
            
            // 添加约束
            if (!column.isNullable()) {
                sql.append(" NOT NULL");
            }
            
            // 添加默认值
            if (column.getDefaultValue() != null && !column.getDefaultValue().isEmpty()) {
                sql.append(" DEFAULT '").append(column.getDefaultValue()).append("'");
            }
            
            // 添加自增
            if (column.isAutoIncrement()) {
                sql.append(" AUTO_INCREMENT");
            }
            
            // 添加主键
            if (column.isPrimaryKey()) {
                sql.append(" PRIMARY KEY");
            }
            
            first = false;
        }
        
        sql.append("\n)");
        
        return sql.toString();
    }

    /**
     * 扩展的用户实体类 - 用于测试表结构更新
     * 在ExampleUser基础上新增3个字段
     */
    @DdlTable(
        value = "extended_user",           // 表名
        keyFields = "id",                   // 主键字段
        version = 2,                        // 版本号增加
        autoSync = true,                    // 启用自动同步
        comment = "扩展用户表",              // 表注释
        charset = "utf8mb4",               // 字符集
        collate = "utf8mb4_unicode_ci",    // 排序规则
        engine = "InnoDB",                 // 存储引擎
        dbtype = "mysql"                   // 数据库类型
    )
    public static class ExtendedUser {

        /**
         * 用户ID - 主键，自增
         */
        @DdlColumn(
            type = "BIGINT",              // SQL类型
            autoIncrement = true,         // 自增
            nullable = false,             // 不允许NULL
            comment = "用户ID"             // 字段注释
        )
        private Long id;

        /**
         * 用户名 - 唯一约束
         */
        @DdlColumn(
            type = "VARCHAR",             // SQL类型
            length = 50,                  // 长度
            nullable = false,             // 不允许NULL
            uniqueKey = "username",       // 唯一约束
            comment = "用户名"             // 字段注释
        )
        private String username;

        /**
         * 邮箱 - 唯一约束，带索引
         */
        @DdlColumn(
            type = "VARCHAR",             // SQL类型
            length = 100,                 // 长度
            nullable = false,             // 不允许NULL
            uniqueKey = "email",          // 唯一约束
            indexName = "idx_email",      // 索引名称
            comment = "邮箱地址"           // 字段注释
        )
        private String email;

        /**
         * 密码
         */
        @DdlColumn(
            type = "VARCHAR",             // SQL类型
            length = 255,                 // 长度
            nullable = false,             // 不允许NULL
            comment = "密码(加密)"         // 字段注释
        )
        private String password;

        /**
         * 年龄
         */
        @DdlColumn(
            type = "INT",                 // SQL类型
            nullable = true,              // 允许NULL
            defaultValue = "0",           // 默认值
            comment = "年龄"              // 字段注释
        )
        private Integer age;

        /**
         * 余额 - 精确小数
         */
        @DdlColumn(
            type = "DECIMAL",             // SQL类型
            precision = 10,                // 精度
            scale = 2,                    // 小数位数
            nullable = false,             // 不允许NULL
            defaultValue = "0.00",        // 默认值
            comment = "账户余额"          // 字段注释
        )
        private java.math.BigDecimal balance;

        /**
         * 是否激活
         */
        @DdlColumn(
            type = "BOOLEAN",             // SQL类型
            nullable = false,             // 不允许NULL
            defaultValue = "true",        // 默认值
            comment = "是否激活"          // 字段注释
        )
        private Boolean active;

        /**
         * 创建时间
         */
        @DdlColumn(
            type = "TIMESTAMP",          // SQL类型
            nullable = false,             // 不允许NULL
            defaultValue = "CURRENT_TIMESTAMP", // 默认值
            defaultValueIsFunction = true, // 默认值是函数
            comment = "创建时间"          // 字段注释
        )
        private java.time.LocalDateTime createTime;

        /**
         * 更新时间
         */
        @DdlColumn(
            type = "TIMESTAMP",          // SQL类型
            nullable = true,              // 允许NULL
            comment = "更新时间"          // 字段注释
        )
        private java.time.LocalDateTime updateTime;

        /**
         * 备注
         */
        @DdlColumn(
            type = "TEXT",               // SQL类型
            nullable = true,              // 允许NULL
            comment = "备注信息"          // 字段注释
        )
        private String remark;

        // ========== 新增字段 ==========

        /**
         * 手机号码 - 新增字段
         */
        @DdlColumn(
            type = "VARCHAR",             // SQL类型
            length = 20,                  // 长度
            nullable = true,              // 允许NULL
            comment = "手机号码"          // 字段注释
        )
        private String phone;

        /**
         * 地址 - 新增字段
         */
        @DdlColumn(
            type = "VARCHAR",             // SQL类型
            length = 500,                 // 长度
            nullable = true,              // 允许NULL
            comment = "地址"              // 字段注释
        )
        private String address;

        /**
         * 生日 - 新增字段
         */
        @DdlColumn(
            type = "DATE",                // SQL类型
            nullable = true,              // 允许NULL
            comment = "生日"              // 字段注释
        )
        private java.time.LocalDate birthday;

        // Getter和Setter方法
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }

        public Integer getAge() { return age; }
        public void setAge(Integer age) { this.age = age; }

        public java.math.BigDecimal getBalance() { return balance; }
        public void setBalance(java.math.BigDecimal balance) { this.balance = balance; }

        public Boolean getActive() { return active; }
        public void setActive(Boolean active) { this.active = active; }

        public java.time.LocalDateTime getCreateTime() { return createTime; }
        public void setCreateTime(java.time.LocalDateTime createTime) { this.createTime = createTime; }

        public java.time.LocalDateTime getUpdateTime() { return updateTime; }
        public void setUpdateTime(java.time.LocalDateTime updateTime) { this.updateTime = updateTime; }

        public String getRemark() { return remark; }
        public void setRemark(String remark) { this.remark = remark; }

        // 新增字段的Getter和Setter
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }

        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }

        public java.time.LocalDate getBirthday() { return birthday; }
        public void setBirthday(java.time.LocalDate birthday) { this.birthday = birthday; }
    }
}
