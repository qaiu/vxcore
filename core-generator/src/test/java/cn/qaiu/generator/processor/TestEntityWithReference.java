package cn.qaiu.generator.processor;

import cn.qaiu.vx.core.annotations.GenerateServiceGen;
import io.vertx.core.json.JsonObject;

/**
 * 测试实体类 - 使用参照接口生成服务
 * 
 * 功能：
 * 1. 测试参照接口方法生成
 * 2. 测试泛型参数替换
 * 3. 验证 JooqDao 接口方法生成
 * 
 * @author vxcore
 * @version 1.0
 */
@GenerateServiceGen(
    idType = Long.class, 
    generateProxy = true,
    referenceInterface = TestReferenceInterface.class
)
public class TestEntityWithReference {

    private Long id;
    private String name;
    private String status;
    private String email;

    // 构造方法
    public TestEntityWithReference() {}

    public TestEntityWithReference(Long id, String name, String status, String email) {
        this.id = id;
        this.name = name;
        this.status = status;
        this.email = email;
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
        return json;
    }

    /**
     * 从 JsonObject 创建实例
     * @param json JsonObject
     * @return TestEntityWithReference 实例
     */
    public static TestEntityWithReference fromJson(JsonObject json) {
        TestEntityWithReference entity = new TestEntityWithReference();
        if (json.containsKey("id")) entity.setId(json.getLong("id"));
        if (json.containsKey("name")) entity.setName(json.getString("name"));
        if (json.containsKey("status")) entity.setStatus(json.getString("status"));
        if (json.containsKey("email")) entity.setEmail(json.getString("email"));
        return entity;
    }

    @Override
    public String toString() {
        return "TestEntityWithReference{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", status='" + status + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
