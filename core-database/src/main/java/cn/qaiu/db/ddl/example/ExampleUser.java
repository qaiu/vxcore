package cn.qaiu.db.ddl.example;

import cn.qaiu.db.ddl.DdlColumn;
import cn.qaiu.db.ddl.DdlTable;
import io.vertx.codegen.annotations.DataObject;
import io.vertx.codegen.format.SnakeCase;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.templates.annotations.RowMapped;

import java.time.LocalDateTime;

/**
 * 示例实体类 - 演示严格的DDL映射
 * 
 * @author <a href="https://qaiu.top">QAIU</a>
 */
@DataObject
@RowMapped(formatter = SnakeCase.class)
@DdlTable(
    value = "example_user",           // 表名
    keyFields = "id",                 // 主键字段
    version = 1,                      // 表结构版本
    autoSync = true,                  // 启用自动同步
    comment = "示例用户表",             // 表注释
    charset = "utf8mb4",              // 字符集
    collate = "utf8mb4_unicode_ci",   // 排序规则
    engine = "InnoDB"                 // 存储引擎
    // dbtype 字段已移除，现在自动从Pool检测数据库类型
)
public class ExampleUser {

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
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @DdlColumn(
        type = "TIMESTAMP",          // SQL类型
        nullable = true,              // 允许NULL
        comment = "更新时间"          // 字段注释
    )
    private LocalDateTime updateTime;

    /**
     * 备注
     */
    @DdlColumn(
        type = "TEXT",               // SQL类型
        nullable = true,              // 允许NULL
        comment = "备注信息"          // 字段注释
    )
    private String remark;

    public ExampleUser() {
        // 无参构造函数
    }

    public ExampleUser(JsonObject json) {
        this.id = json.getLong("id");
        this.username = json.getString("username");
        this.email = json.getString("email");
        this.password = json.getString("password");
        this.age = json.getInteger("age");
        // 注意：JsonObject没有getBigDecimal方法，这里需要特殊处理
        String balanceStr = json.getString("balance");
        this.balance = balanceStr != null ? new java.math.BigDecimal(balanceStr) : null;
        this.active = json.getBoolean("active");
        // 注意：JsonObject没有getLocalDateTime方法，这里需要特殊处理
        String createTimeStr = json.getString("createTime");
        this.createTime = createTimeStr != null ? LocalDateTime.parse(createTimeStr) : null;
        String updateTimeStr = json.getString("updateTime");
        this.updateTime = updateTimeStr != null ? LocalDateTime.parse(updateTimeStr) : null;
        this.remark = json.getString("remark");
    }

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

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }

    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
