package cn.qaiu.example.entity;

import cn.qaiu.db.ddl.DdlColumn;
import cn.qaiu.db.ddl.DdlTable;
import cn.qaiu.db.dsl.BaseEntity;
import io.vertx.core.json.JsonObject;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户实体类
 * 演示 VXCore 框架的实体映射和数据库操作
 * 
 * @author QAIU
 */
@DdlTable(
    value = "users",
    keyFields = "id",
    version = 1,
    autoSync = true,
    comment = "用户表",
    charset = "utf8mb4",
    collate = "utf8mb4_unicode_ci",
    engine = "InnoDB"
)
public class User extends BaseEntity {
    
    
    @DdlColumn(
        type = "VARCHAR",
        length = 50,
        nullable = false,
        uniqueKey = "username",
        comment = "用户名"
    )
    private String username;
    
    @DdlColumn(
        type = "VARCHAR",
        length = 100,
        nullable = false,
        uniqueKey = "email",
        indexName = "idx_email",
        comment = "邮箱地址"
    )
    private String email;
    
    @DdlColumn(
        type = "VARCHAR",
        length = 255,
        nullable = false,
        comment = "密码(加密)"
    )
    private String password;
    
    @DdlColumn(
        type = "INT",
        nullable = true,
        defaultValue = "0",
        comment = "年龄"
    )
    private Integer age;
    
    @DdlColumn(
        type = "TEXT",
        nullable = true,
        comment = "个人简介"
    )
    private String bio;
    
    @DdlColumn(
        type = "VARCHAR",
        length = 20,
        nullable = false,
        defaultValue = "'ACTIVE'",
        comment = "用户状态"
    )
    private UserStatus status;
    
    @DdlColumn(
        type = "DECIMAL",
        precision = 10,
        scale = 2,
        nullable = false,
        defaultValue = "0.00",
        comment = "账户余额"
    )
    private BigDecimal balance;
    
    @DdlColumn(
        type = "BOOLEAN",
        nullable = false,
        defaultValue = "false",
        comment = "邮箱是否验证"
    )
    private Boolean emailVerified;
    
    
    /**
     * 用户状态枚举
     */
    public enum UserStatus {
        ACTIVE("激活"), INACTIVE("未激活"), SUSPENDED("暂停"), DELETED("已删除");
        
        private final String description;
        
        UserStatus(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    // 构造函数
    public User() {
    }
    
    public User(JsonObject json) {
        super(json);
        this.username = json.getString("username");
        this.email = json.getString("email");
        this.password = json.getString("password");
        this.age = json.getInteger("age");
        this.bio = json.getString("bio");
        this.status = json.getString("status") != null ? UserStatus.valueOf(json.getString("status")) : null;
        this.balance = json.getValue("balance") != null ? new BigDecimal(json.getValue("balance").toString()) : null;
        this.emailVerified = json.getBoolean("email_verified");
    }
    
    // Getter 和 Setter 方法
    
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
    
    public String getBio() {
        return bio;
    }
    
    public void setBio(String bio) {
        this.bio = bio;
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

    
    public LocalDateTime getCreatedAt() {
        return getCreateTime();
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        setCreateTime(createdAt);
    }
    
    public LocalDateTime getUpdatedAt() {
        return getUpdateTime();
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        setUpdateTime(updatedAt);
    }
    
    // 业务方法
    public boolean verifyPassword(String password) {
        return this.password != null && this.password.equals(password);
    }
    
    public boolean isActive() {
        return status == UserStatus.ACTIVE;
    }
    
    public void suspend() {
        this.status = UserStatus.SUSPENDED;
    }
    
    public void activate() {
        this.status = UserStatus.ACTIVE;
    }
    
    
    /**
     * 转换为 JsonObject
     */
    @Override
    protected void fillJson(JsonObject json) {
        if (username != null) json.put("username", username);
        if (email != null) json.put("email", email);
        if (password != null) json.put("password", password);
        if (age != null) json.put("age", age);
        if (bio != null) json.put("bio", bio);
        if (status != null) json.put("status", status.name());
        if (balance != null) json.put("balance", balance);
        if (emailVerified != null) json.put("email_verified", emailVerified);
    }
    
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", age=" + age +
                ", status=" + status +
                ", balance=" + balance +
                '}';
    }
}
