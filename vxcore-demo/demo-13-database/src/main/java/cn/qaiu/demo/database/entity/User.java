package cn.qaiu.demo.database.entity;

import cn.qaiu.db.ddl.DdlColumn;
import cn.qaiu.db.ddl.DdlTable;
import cn.qaiu.db.dsl.BaseEntity;
import io.vertx.core.json.JsonObject;

@DdlTable("users")
public class User extends BaseEntity {

    @DdlColumn(value = "user_name", type = "VARCHAR(100)", nullable = false, comment = "用户名")
    private String name;

    @DdlColumn(value = "email", type = "VARCHAR(200)", comment = "邮箱")
    private String email;

    @DdlColumn(value = "age", type = "INT", comment = "年龄")
    private Integer age;

    @DdlColumn(value = "status", type = "VARCHAR(20)", defaultValue = "ACTIVE", comment = "状态")
    private String status = "ACTIVE";

    public User() {}

    public User(JsonObject json) {
        super(json);
        this.name = json.getString("name");
        this.email = json.getString("email");
        this.age = json.getInteger("age");
        this.status = json.getString("status", "ACTIVE");
    }

    @Override
    protected void fillJson(JsonObject json) {
        json.put("name", name);
        json.put("email", email);
        json.put("age", age);
        json.put("status", status);
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    @Override
    public String toString() {
        return "User{id=" + getId() + ", name='" + name + "', email='" + email
                + "', age=" + age + ", status='" + status + "'}";
    }
}
