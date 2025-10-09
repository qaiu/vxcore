package cn.qaiu.example;

import cn.qaiu.db.ddl.DdlColumn;
import cn.qaiu.db.ddl.DdlTable;
import cn.qaiu.db.dsl.BaseEntity;
import io.vertx.core.json.JsonObject;

import java.math.BigDecimal;

/**
 * 用户实体类示例
 * 
 * 结合 Vert.x CodeGen 和 DDL 注解系统
* 演示如何使用新的 DSL 框架
 * 
 * @author <a href="https://qaiu.top">QAIU</a>
 */
@DdlTable(
    value = "dsl_user",              // 表名
    keyFields = "id",                // 主键字段
    version = 1,                      // 表结构版本
    autoSync = true,                  // 启用自动同步
    comment = "DSL用户表示例",         // 表注释
    charset = "utf8mb4",              // 字符集
    collate = "utf8mb4_unicode_ci",   // 排序规则
    engine = "InnoDB"                 // 存储引擎
)
public class User extends BaseEntity {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 用户名
     */
    @DdlColumn(
        type = "VARCHAR",
        length = 50,
        nullable = false,
        uniqueKey = "username",
        comment = "用户名"
    )
    private String username;
    
    /**
     * 邮箱
     */
    @DdlColumn(
        type = "VARCHAR",
        length = 100,
        nullable = false,
        uniqueKey = "email",
        comment = "邮箱地址"
    )
    private String email;
    
    /**
     * 密码
     */
    @DdlColumn(
        type = "VARCHAR",
        length = 255,
        nullable = false,
        comment = "密码(加密)"
    )
    private String password;
    
    /**
     * 年龄
     */
    @DdlColumn(
        type = "INT",
        nullable = true,
        defaultValue = "0",
        comment = "年龄"
    )
    private Integer age;
    
    /**
     * 状态：ACTIVE, INACTIVE, SUSPENDED
     */
    @DdlColumn(
        type = "VARCHAR",
        length = 20,
        nullable = false,
        defaultValue = "ACTIVE",
        comment = "用户状态"
    )
    private UserStatus status;
    
    /**
     * 账户余额
     */
    @DdlColumn(
        type = "DECIMAL",
        precision = 10,
        scale = 2,
        nullable = false,
        defaultValue = "0.00",
        comment = "账户余额"
    )
    private BigDecimal balance;
    
    /**
     * 是否已激活邮箱验证
     */
    @DdlColumn(
        type = "BOOLEAN",
        nullable = false,
        defaultValue = "false",
        comment = "邮箱是否已验证"
    )
    private Boolean emailVerified;
    
    /**
     * 个人简介
     */
    @DdlColumn(
        type = "TEXT",
        nullable = true,
        comment = "个人简介"
    )
    private String bio;
    
    /**
     * 用户状态枚举
     */
    public enum UserStatus {
        ACTIVE("激活"),
        INACTIVE("未激活"),
        SUSPENDED("暂停");
        
        private final String description;
        
        UserStatus(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
        
        @Override
        public String toString() {
            return name();
        }
    }
    
    /**
     * 无参构造函数（Jackson/JSON 反序列化需要）
     */
    public User() {
        super();
        this.status = UserStatus.ACTIVE;
        this.emailVerified = false;
    }
    
    /**
     * BaseEntity 的 JsonObject 构造函数
     * 调用父类构造函数时会自动设置基础字段
     * 
     * @param json JSON 对象
     */
    public User(JsonObject json) {
        super(json);
        
        this.username = json.getString("username");
        this.email = json.getString("email");
        this.password = json.getString("password");
        this.age = json.getInteger("age");
        
        // 处理枚举类型
        String statusStr = json.getString("status");
        if (statusStr != null) {
            try {
                this.status = UserStatus.valueOf(statusStr);
            } catch (IllegalArgumentException e) {
                this.status = UserStatus.ACTIVE; // 默认值
            }
        } else {
            this.status = UserStatus.ACTIVE;
        }
        
        // 处理 BigDecimal
        String balanceStr = json.getString("balance");
        this.balance = balanceStr != null ? new BigDecimal(balanceStr) : BigDecimal.ZERO;
        
        this.emailVerified = json.getBoolean("emailVerified", false);
        this.bio = json.getString("bio");
    }
    
    /**
     * 转换为 JsonObject（重写父类方法，添加自定义字段）
     */
    @Override
    protected void fillJson(JsonObject json) {
        json.put("username", username)
            .put("email", email)
            .put("password", password)
            .put("age", age)
            .put("status", status != null ? status.name() : UserStatus.ACTIVE.name())
            .put("balance", balance != null ? balance.toString() : "0.00")
            .put("emailVerified", emailVerified)
            .put("bio", bio);
    }
    
    // =================== Getter/Setter 方法 ===================
    
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
    
    public UserStatus getStatus() {
        return status;
    }
    
    public void setStatus(UserStatus status) {
        this.status = status;
    }
    
    public BigDecimal getBalance() {
        return balance;
    }
    
    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }
    
    public Boolean getEmailVerified() {
        return emailVerified;
    }
    
    public void setEmailVerified(Boolean emailVerified) {
        this.emailVerified = emailVerified;
    }
    
    public String getBio() {
        return bio;
    }
    
    public void setBio(String bio) {
        this.bio = bio;
    }
    
    // =================== 业务方法 ===================
    
    /**
     * 验证用户密码
     * 
     * @param inputPassword 输入的密码
     * @return 是否验证通过
     */
    public boolean verifyPassword(String inputPassword) {
        // 这里应该使用加密算法验证，比如 BCrypt
        // 简化示例，实际应用中应该使用安全的密码验证
        return password != null && password.equals(inputPassword);
    }
    
    /**
     * 设置邮箱为已验证状态
     */
    public void verifyEmail() {
        this.emailVerified = true;
        onUpdate();
    }
    
    /**
     * 检查账户是否激活
     * 
     * @return 是否激活
     */
    public boolean isActive() {
        return status == UserStatus.ACTIVE;
    }
    
    /**
     * 冻结账户
     */
    public void suspend() {
        this.status = UserStatus.SUSPENDED;
        onUpdate();
    }
    
    /**
     * 激活账户
     */
    public void activate() {
        this.status = UserStatus.ACTIVE;
        onUpdate();
    }
    
    @Override
    public String toString() {
        return "User{" +
                "id=" + getId() +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", age=" + age +
                ", status=" + status +
                ", emailVerified=" + emailVerified +
                ", createTime=" + getCreateTime() +
                ", updateTime=" + getUpdateTime() +
                '}';
    }
}
