package cn.qaiu.example.model;

import cn.qaiu.db.ddl.DdlColumn;
import cn.qaiu.db.ddl.DdlTable;
import io.vertx.core.json.JsonObject;

import java.util.Objects;

/**
 * 用户实体类
 * 演示三层架构中的Model层
 * 
 * @author <a href="https://qaiu.top">QAIU</a>
 */
@DdlTable("users")
public class User {
    
    @DdlColumn("id")
    private Long id;
    
    @DdlColumn("name")
    private String name;
    
    @DdlColumn("email")
    private String email;
    
    @DdlColumn("age")
    private Integer age;
    
    @DdlColumn("phone")
    private String phone;
    
    @DdlColumn("address")
    private String address;
    
    public User() {
    }
    
    public User(String name, String email) {
        this.name = name;
        this.email = email;
    }
    
    public User(String name, String email, Integer age) {
        this.name = name;
        this.email = email;
        this.age = age;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public Integer getAge() {
        return age;
    }
    
    public void setAge(Integer age) {
        this.age = age;
    }
    
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
    
    /**
     * 转换为JsonObject
     */
    public JsonObject toJson() {
        return new JsonObject()
            .put("id", id)
            .put("name", name)
            .put("email", email)
            .put("age", age)
            .put("phone", phone)
            .put("address", address);
    }
    
    /**
     * 从JsonObject创建User
     */
    public static User fromJson(JsonObject json) {
        User user = new User();
        user.setId(json.getLong("id"));
        user.setName(json.getString("name"));
        user.setEmail(json.getString("email"));
        user.setAge(json.getInteger("age"));
        user.setPhone(json.getString("phone"));
        user.setAddress(json.getString("address"));
        return user;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id) &&
               Objects.equals(name, user.name) &&
               Objects.equals(email, user.email) &&
               Objects.equals(age, user.age) &&
               Objects.equals(phone, user.phone) &&
               Objects.equals(address, user.address);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id, name, email, age, phone, address);
    }
    
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", age=" + age +
                ", phone='" + phone + '\'' +
                ", address='" + address + '\'' +
                '}';
    }
}