package cn.qaiu.db.dsl.lambda.example;

import cn.qaiu.db.ddl.DdlTable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Lambda测试用的User实体类
 * 
 * @author <a href="https://qaiu.top">QAIU</a>
 */
@DdlTable("users")
public class User {
    
    private Long id;
    private String username;
    private String email;
    private String password;
    private Integer age;
    private String status;
    private BigDecimal balance;
    private Boolean emailVerified;
    private String bio;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    
    // 构造函数
    public User() {}
    
    public User(String username, String email, String password, Integer age, String status, BigDecimal balance) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.age = age;
        this.status = status;
        this.balance = balance;
        this.emailVerified = false;
        this.createTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
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
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }
    
    public Boolean getEmailVerified() { return emailVerified; }
    public void setEmailVerified(Boolean emailVerified) { this.emailVerified = emailVerified; }
    
    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
    
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
    
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", age=" + age +
                ", status='" + status + '\'' +
                ", balance=" + balance +
                ", createTime=" + createTime +
                '}';
    }
}
