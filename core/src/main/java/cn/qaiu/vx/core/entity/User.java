package cn.qaiu.vx.core.entity;

import cn.qaiu.vx.core.annotations.GenerateServiceGen;
import io.vertx.core.json.JsonObject;

@GenerateServiceGen(idType = Long.class, generateProxy = true)
public class User {
    
    private Long id;
    private String username;
    private String email;
    private String password;
    private Integer age;
    private String status = "ACTIVE";
    private Boolean emailVerified = false;
    private Double balance = 0.0;
    
    // Constructors
    public User() {}
    
    public User(Long id, String username, String email, String password, Integer age) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.age = age;
    }
    
    // Getters and Setters
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
    
    public Boolean getEmailVerified() { return emailVerified; }
    public void setEmailVerified(Boolean emailVerified) { this.emailVerified = emailVerified; }
    
    public Double getBalance() { return balance; }
    public void setBalance(Double balance) { this.balance = balance; }
    
    public JsonObject toJson() {
        return new JsonObject()
            .put("id", id)
            .put("username", username)
            .put("email", email)
            .put("age", age)
            .put("status", status)
            .put("emailVerified", emailVerified)
            .put("balance", balance);
    }
}
