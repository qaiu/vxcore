package cn.qaiu.db.ddl;

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
 * MySQL表结构更新测试
 * 使用真实的MySQL数据库测试框架的自动表结构更新功能
 * 
 * @author <a href="https://qaiu.top">QAIU</a>
 */
@ExtendWith(VertxExtension.class)
@DisplayName("MySQL表结构更新测试")
public class MySQLTableUpdateTest {

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

        // 创建MySQL数据库连接
        PoolOptions poolOptions = new PoolOptions().setMaxSize(5);
        JDBCConnectOptions connectOptions = new JDBCConnectOptions()
                .setJdbcUrl("jdbc:mysql://localhost:3306/testdb?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true")
                .setUser("testuser")
                .setPassword("testpass");

        pool = JDBCPool.pool(vertx, connectOptions, poolOptions);
        
        // 测试连接
        pool.query("SELECT 1")
            .execute()
            .onSuccess(result -> {
                System.out.println("✅ MySQL连接成功！");
                testContext.completeNow();
            })
            .onFailure(e -> {
                System.out.println("❌ MySQL连接失败: " + e.getMessage());
                testContext.failNow(e);
            });
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
            System.out.println("🚀 第一步：使用框架在MySQL中创建初始表结构...");
            
            // 使用框架的自动同步功能创建表
            TableStructureSynchronizer.synchronizeTable(pool, MySQLUser.class, JDBCType.MySQL)
                .onSuccess(result -> {
                    System.out.println("✅ 框架在MySQL中自动创建表成功！");
                    
                    // 验证表是否创建成功
                    pool.query("SHOW TABLES LIKE 'mysql_user'")
                        .execute()
                        .onSuccess(tablesResult -> {
                            if (tablesResult.size() > 0) {
                                System.out.println("📋 MySQL数据库中的表:");
                                for (var row : tablesResult) {
                                    System.out.println("  - " + row.getString(0));
                                }
                                
                                // 查看表结构
                                pool.query("DESCRIBE mysql_user")
                                    .execute()
                                    .onSuccess(descResult -> {
                                        System.out.println("📊 MySQL表结构 (mysql_user):");
                                        for (var row : descResult) {
                                            String field = row.getString("Field");
                                            String type = row.getString("Type");
                                            String nullFlag = row.getString("Null");
                                            String key = row.getString("Key");
                                            String defaultVal = row.getString("Default");
                                            String extra = row.getString("Extra");
                                            
                                            System.out.println(String.format("  - %s: %s %s %s %s %s", 
                                                field, type, 
                                                "YES".equals(nullFlag) ? "NULL" : "NOT NULL",
                                                key.isEmpty() ? "" : key,
                                                defaultVal != null ? "DEFAULT " + defaultVal : "",
                                                extra.isEmpty() ? "" : extra
                                            ));
                                        }
                                        
                                        System.out.println("✅ 第一步完成：框架在MySQL中自动创建表结构成功！");
                                        testContext.completeNow();
                                    })
                                    .onFailure(testContext::failNow);
                            } else {
                                System.out.println("❌ 表mysql_user未创建");
                                testContext.failNow(new RuntimeException("表mysql_user未创建"));
                            }
                        })
                        .onFailure(testContext::failNow);
                })
                .onFailure(e -> {
                    System.out.println("❌ 框架在MySQL中自动创建表失败: " + e.getMessage());
                    testContext.failNow(e);
                });
                
        } catch (Exception e) {
            testContext.failNow(e);
        }
    }

    /**
     * 第二步：测试表结构自动更新 - 减少字段
     */
    @Test
    @DisplayName("第二步：测试表结构自动更新 - 减少字段")
    void testStep2_AutoUpdateTableStructure(VertxTestContext testContext) {
        try {
            System.out.println("🚀 第二步：使用框架在MySQL中自动更新表结构（减少字段）...");
            
            // 使用框架的自动同步功能更新表
            TableStructureSynchronizer.synchronizeTable(pool, SimplifiedMySQLUser.class, JDBCType.MySQL)
                .onSuccess(result -> {
                    System.out.println("✅ 框架在MySQL中自动更新表成功！");
                    
                    // 查看更新后的表结构
                    pool.query("DESCRIBE mysql_user")
                        .execute()
                        .onSuccess(descResult -> {
                            System.out.println("📊 MySQL表结构 (mysql_user - 更新后):");
                            int fieldCount = 0;
                            boolean hasBalance = false;
                            boolean hasUpdateTime = false;
                            boolean hasRemark = false;
                            
                            for (var row : descResult) {
                                String field = row.getString("Field");
                                String type = row.getString("Type");
                                String nullFlag = row.getString("Null");
                                String key = row.getString("Key");
                                String defaultVal = row.getString("Default");
                                String extra = row.getString("Extra");
                                
                                System.out.println(String.format("  - %s: %s %s %s %s %s", 
                                    field, type, 
                                    "YES".equals(nullFlag) ? "NULL" : "NOT NULL",
                                    key.isEmpty() ? "" : key,
                                    defaultVal != null ? "DEFAULT " + defaultVal : "",
                                    extra.isEmpty() ? "" : extra
                                ));
                                
                                fieldCount++;
                                if ("balance".equals(field)) hasBalance = true;
                                if ("update_time".equals(field)) hasUpdateTime = true;
                                if ("remark".equals(field)) hasRemark = true;
                            }
                            
                            // 验证减少的字段（这些字段不应该存在）
                            assertFalse(hasBalance, "表不应该包含balance字段");
                            assertFalse(hasUpdateTime, "表不应该包含update_time字段");
                            assertFalse(hasRemark, "表不应该包含remark字段");
                            
                            System.out.println("✅ 第二步完成：框架在MySQL中自动更新表结构成功！");
                            System.out.println("   - 总字段数: " + fieldCount);
                            System.out.println("   - 减少字段: balance, update_time, remark");
                            testContext.completeNow();
                        })
                        .onFailure(testContext::failNow);
                })
                .onFailure(e -> {
                    System.out.println("❌ 框架在MySQL中自动更新表失败: " + e.getMessage());
                    testContext.failNow(e);
                });
                
        } catch (Exception e) {
            testContext.failNow(e);
        }
    }

    /**
     * 第三步：测试框架的智能检测功能
     */
    @Test
    @DisplayName("第三步：测试框架的智能检测功能")
    void testStep3_SmartDetection(VertxTestContext testContext) {
        try {
            System.out.println("🚀 第三步：测试框架在MySQL中的智能检测功能...");
            
            // 先创建一个基础表
            TableStructureSynchronizer.synchronizeTable(pool, MySQLUser.class, JDBCType.MySQL)
                .compose(result -> {
                    System.out.println("✅ 基础表创建完成");
                    
                    // 检查是否需要同步（应该返回false，因为表已经同步）
                    return TableStructureSynchronizer.needsSynchronization(pool, MySQLUser.class, JDBCType.MySQL);
                })
                .onSuccess(needsSync -> {
                    System.out.println("🔍 框架检测结果：MySQLUser表需要同步 = " + needsSync);
                    
                    // 检查SimplifiedMySQLUser是否需要同步（应该返回true，因为字段不同）
                    TableStructureSynchronizer.needsSynchronization(pool, SimplifiedMySQLUser.class, JDBCType.MySQL)
                        .onSuccess(needsSyncSimplified -> {
                            System.out.println("🔍 框架检测结果：SimplifiedMySQLUser表需要同步 = " + needsSyncSimplified);
                            
                            if (needsSyncSimplified) {
                                System.out.println("✅ 第三步完成：框架在MySQL中智能检测功能正常");
                                System.out.println("   - 能够检测到表结构差异");
                                System.out.println("   - 能够判断是否需要同步");
                                testContext.completeNow();
                            } else {
                                System.out.println("❌ 框架检测异常：SimplifiedMySQLUser应该需要同步");
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

    /**
     * MySQL用户实体类 - 基础版本
     */
    @DdlTable(
        value = "mysql_user",             // 表名
        keyFields = "id",                  // 主键字段
        version = 1,                       // 版本号
        autoSync = true,                   // 启用自动同步
        comment = "MySQL用户表",            // 表注释
        charset = "utf8mb4",              // 字符集
        collate = "utf8mb4_unicode_ci",   // 排序规则
        engine = "InnoDB",                // 存储引擎
        dbtype = "mysql"                  // 数据库类型
    )
    public static class MySQLUser {

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
         * 用户名
         */
        @DdlColumn(
            type = "VARCHAR",             // SQL类型
            length = 50,                  // 长度
            nullable = false,             // 不允许NULL
            comment = "用户名"             // 字段注释
        )
        private String username;

        /**
         * 邮箱
         */
        @DdlColumn(
            type = "VARCHAR",             // SQL类型
            length = 100,                 // 长度
            nullable = false,             // 不允许NULL
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
         * 余额
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
    }

    /**
     * 简化的MySQL用户实体类 - 用于测试表结构更新
     * 在MySQLUser基础上减少几个字段
     */
    @DdlTable(
        value = "mysql_user",            // 使用相同的表名
        keyFields = "id",                  // 主键字段
        version = 2,                      // 版本号增加
        autoSync = true,                  // 启用自动同步
        comment = "简化MySQL用户表",        // 表注释
        charset = "utf8mb4",              // 字符集
        collate = "utf8mb4_unicode_ci",   // 排序规则
        engine = "InnoDB",                // 存储引擎
        dbtype = "mysql"                  // 数据库类型
    )
    public static class SimplifiedMySQLUser {

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
         * 用户名
         */
        @DdlColumn(
            type = "VARCHAR",             // SQL类型
            length = 50,                  // 长度
            nullable = false,             // 不允许NULL
            comment = "用户名"             // 字段注释
        )
        private String username;

        /**
         * 邮箱
         */
        @DdlColumn(
            type = "VARCHAR",             // SQL类型
            length = 100,                 // 长度
            nullable = false,             // 不允许NULL
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

        public Boolean getActive() { return active; }
        public void setActive(Boolean active) { this.active = active; }

        public java.time.LocalDateTime getCreateTime() { return createTime; }
        public void setCreateTime(java.time.LocalDateTime createTime) { this.createTime = createTime; }
    }
}
