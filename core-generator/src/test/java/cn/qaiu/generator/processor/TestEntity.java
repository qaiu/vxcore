package cn.qaiu.generator.processor;

import cn.qaiu.vx.core.annotations.GenerateServiceGen;
import io.vertx.core.json.JsonObject;

/**
 * 测试实体类 - 用于验证重构后的注解处理器
 * 
 * 功能：
 * 1. 测试泛型参数解析
 * 2. 测试服务接口生成
 * 3. 测试服务实现类生成
 * 4. 验证 ProxyGen 注解处理
 * 
 * @author vxcore
 * @version 1.0
 */
@GenerateServiceGen(idType = Long.class, generateProxy = true, basePackage = "cn.qaiu.generator.processor")
public class TestEntity {

    private Long id;
    private String name;
    private String status;
    private String email;
    private User user;
    private Long timestamp;

    // 构造方法
    public TestEntity() {}

    public TestEntity(Long id, String name, String status, String email) {
        this.id = id;
        this.name = name;
        this.status = status;
        this.email = email;
        this.timestamp = System.currentTimeMillis();
    }

    // Getter 和 Setter 方法
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Long getTimestamp() { return timestamp; }
    public void setTimestamp(Long timestamp) { this.timestamp = timestamp; }

    /**
     * 转换为 JsonObject
     * @return JsonObject 表示
     */
    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        if (id != null) json.put("id", id);
        if (name != null) json.put("name", name);
        if (status != null) json.put("status", status);
        if (email != null) json.put("email", email);
        if (user != null) json.put("user", user.toJson());
        if (timestamp != null) json.put("timestamp", timestamp);
        return json;
    }

    /**
     * 从 JsonObject 创建实例
     * @param json JsonObject
     * @return TestEntity 实例
     */
    public static TestEntity fromJson(JsonObject json) {
        TestEntity entity = new TestEntity();
        if (json.containsKey("id")) entity.setId(json.getLong("id"));
        if (json.containsKey("name")) entity.setName(json.getString("name"));
        if (json.containsKey("status")) entity.setStatus(json.getString("status"));
        if (json.containsKey("email")) entity.setEmail(json.getString("email"));
        if (json.containsKey("timestamp")) entity.setTimestamp(json.getLong("timestamp"));
        return entity;
    }

    @Override
    public String toString() {
        return "TestEntity{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", status='" + status + '\'' +
                ", email='" + email + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}

/**
 * 泛型接口 - 用于测试泛型参数解析
 * 
 * @param <T> 第一个泛型参数
 * @param <U> 第二个泛型参数
 */
interface GenericInterface<T, U> {
    T getFirst();
    U getSecond();
    void setFirst(T first);
    void setSecond(U second);
}

/**
 * 用户类 - 用于测试泛型参数
 */
class User {
    private Long id;
    private String username;
    private String email;

    public User() {}

    public User(Long id, String username, String email) {
        this.id = id;
        this.username = username;
        this.email = email;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        if (id != null) json.put("id", id);
        if (username != null) json.put("username", username);
        if (email != null) json.put("email", email);
        return json;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}