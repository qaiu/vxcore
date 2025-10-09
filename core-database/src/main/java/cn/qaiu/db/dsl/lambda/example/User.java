package cn.qaiu.db.dsl.lambda.example;

import cn.qaiu.db.ddl.DdlColumn;
import cn.qaiu.db.ddl.DdlTable;
import cn.qaiu.db.dsl.BaseEntity;
import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;
import io.vertx.codegen.format.SnakeCase;
import io.vertx.sqlclient.templates.annotations.RowMapped;

import java.time.LocalDateTime;

/**
 * 用户实体类 - Lambda查询示例
 * 
 * @author qaiu
 */
@RowMapped(formatter = SnakeCase.class)
@DdlTable("users")
public class User extends BaseEntity {
    
    private static final long serialVersionUID = 1L;
    
    @DdlColumn("id")
    private Long id;
    
    @DdlColumn("username")
    private String username;
    
    @DdlColumn("email")
    private String email;
    
    @DdlColumn("password")
    private String password;
    
    @DdlColumn("age")
    private Integer age;
    
    @DdlColumn("status")
    private String status;
    
    @DdlColumn("balance")
    private Double balance;
    
    @DdlColumn("email_verified")
    private Boolean emailVerified;
    
    @DdlColumn("bio")
    private String bio;
    
    @DdlColumn("create_time")
    private LocalDateTime createTime;
    
    @DdlColumn("update_time")
    private LocalDateTime updateTime;
    
    public User() {
    }
    
    public User(JsonObject json) {
        // 简单的JSON转换实现
        this.id = json.getLong("id");
        this.username = json.getString("username");
        this.email = json.getString("email");
        this.password = json.getString("password");
        this.age = json.getInteger("age");
        this.status = json.getString("status");
        this.balance = json.getDouble("balance");
        this.emailVerified = json.getBoolean("emailVerified");
        this.bio = json.getString("bio");
        this.createTime = json.getString("createTime") != null ? LocalDateTime.parse(json.getString("createTime")) : null;
        this.updateTime = json.getString("updateTime") != null ? LocalDateTime.parse(json.getString("updateTime")) : null;
    }
    
    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.put("id", id);
        json.put("username", username);
        json.put("email", email);
        json.put("password", password);
        json.put("age", age);
        json.put("status", status);
        json.put("balance", balance);
        json.put("emailVerified", emailVerified);
        json.put("bio", bio);
        json.put("createTime", createTime != null ? createTime.toString() : null);
        json.put("updateTime", updateTime != null ? updateTime.toString() : null);
        return json;
    }
    
    // Getters and Setters
    
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
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public Double getBalance() {
        return balance;
    }
    
    public void setBalance(Double balance) {
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
    
    public LocalDateTime getCreateTime() {
        return createTime;
    }
    
    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
    
    public LocalDateTime getUpdateTime() {
        return updateTime;
    }
    
    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
    
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", age=" + age +
                ", status='" + status + '\'' +
                ", balance=" + balance +
                ", emailVerified=" + emailVerified +
                ", bio='" + bio + '\'' +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                '}';
    }
}
