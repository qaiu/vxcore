package cn.qaiu.vx.core.processor;

import cn.qaiu.vx.core.annotations.GenerateServiceGen;
import io.vertx.core.json.JsonObject;

@GenerateServiceGen(idType = Long.class, generateProxy = true)
public class TestEntity {
    
    private Long id;
    private String name;
    private String status;
    private String email;
    
    // Constructors, getters, setters
    public TestEntity() {}
    
    public TestEntity(Long id, String name, String status, String email) {
        this.id = id;
        this.name = name;
        this.status = status;
        this.email = email;
    }
    
    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public JsonObject toJson() {
        return new JsonObject()
            .put("id", id)
            .put("name", name)
            .put("status", status)
            .put("email", email);
    }
}
