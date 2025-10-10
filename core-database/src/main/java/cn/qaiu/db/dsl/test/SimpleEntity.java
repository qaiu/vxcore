package cn.qaiu.db.dsl.test;

import cn.qaiu.db.ddl.DdlColumn;
import cn.qaiu.db.ddl.DdlTable;
import cn.qaiu.db.dsl.BaseEntity;
import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

/**
 * 简单测试实体类
 * 
 * @author <a href="https://qaiu.top">QAIU</a>
 */
@DataObject(generateConverter = true)
@DdlTable(
    value = "test_simple",              // 表名
    keyFields = "id",                   // 主键字段
    version = 1,                        // 表结构版本
    autoSync = true,                    // 启用自动同步
    comment = "简单测试实体",           // 表注释
    charset = "utf8mb4",                // 字符集
    collate = "utf8mb4_unicode_ci",     // 排序规则
    engine = "InnoDB"                   // 存储引擎
)
public class SimpleEntity extends BaseEntity {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 名称
     */
    @DdlColumn(
        type = "VARCHAR",
        length = 50,
        nullable = false,
        comment = "名称"
    )
    private String name;
    
    /**
     * 描述
     */
    @DdlColumn(
        type = "TEXT",
        nullable = true,
        comment = "描述"
    )
    private String description;
    
    /**
     * 无参构造函数
     */
    public SimpleEntity() {
        super();
    }
    
    /**
     * JsonObject 构造函数
     */
    public SimpleEntity(JsonObject json) {
        super(json);
        this.name = json.getString("name");
        this.description = json.getString("description");
    }
    
    /**
     * 填充JSON字段
     */
    @Override
    protected void fillJson(JsonObject json) {
        json.put("name", name)
            .put("description", description);
    }
    
    // Getter/Setter
    public String getUsername() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    @Override
    public String toString() {
        return "SimpleEntity{" +
                "id=" + getId() +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", createTime=" + getCreateTime() +
                ", updateTime=" + getUpdateTime() +
                '}';
    }
}
