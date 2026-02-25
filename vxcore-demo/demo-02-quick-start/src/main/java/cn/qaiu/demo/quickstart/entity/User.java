package cn.qaiu.demo.quickstart.entity;

/**
 * 用户实体类
 * 
 * 验证 02-quick-start.md 第三步中的实体定义
 * 
 * 注意: 文档使用了 @DdlTable, @DdlColumn, extends BaseEntity
 * 这里简化为普通 POJO，因为不配置真实数据库
 * 
 * ISSUE-005: LocalDateTime 序列化需要 jackson-datatype-jsr310 模块, 
 * 框架未自动注册该模块, 改用 String 类型避免序列化错误
 */
public class User {

    private Long id;
    private String name;
    private String email;
    private String status = "ACTIVE";
    private String createTime;
    private String updateTime;

    public User() {}

    public User(String name, String email) {
        this.name = name;
        this.email = email;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCreateTime() { return createTime; }
    public void setCreateTime(String createTime) { this.createTime = createTime; }

    public String getUpdateTime() { return updateTime; }
    public void setUpdateTime(String updateTime) { this.updateTime = updateTime; }

    @Override
    public String toString() {
        return "User{id=" + id + ", name='" + name + "', email='" + email + "', status='" + status + "'}";
    }
}
