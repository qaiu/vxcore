package cn.qaiu.db.dsl.lambda.example;

import cn.qaiu.db.ddl.DdlColumn;
import cn.qaiu.db.ddl.DdlTable;
import cn.qaiu.db.dsl.BaseEntity;
import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;
import io.vertx.codegen.format.SnakeCase;
import io.vertx.sqlclient.templates.annotations.RowMapped;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 产品实体类 - 展示DdlColumn value字段的使用
 * 
 * @author qaiu
 */
@RowMapped(formatter = SnakeCase.class)
@DdlTable(value = "products", keyFields = "product_id")
public class Product extends BaseEntity {
    
    private static final long serialVersionUID = 1L;
    
    // 使用value字段的简洁写法
    @DdlColumn(value = "product_id", type = "BIGINT", autoIncrement = false)
    private Long id;
    
    // 使用value字段
    @DdlColumn("product_name")
    private String name;
    
    // 使用value字段
    @DdlColumn("product_code")
    private String code;
    
    // 使用value字段
    @DdlColumn("category_id")
    private Long categoryId;
    
    // 使用value字段
    @DdlColumn("price")
    private BigDecimal price;
    
    // 使用value字段
    @DdlColumn("stock_quantity")
    private Integer stockQuantity;
    
    // 使用value字段
    @DdlColumn("description")
    private String description;
    
    // 使用value字段
    @DdlColumn("is_active")
    private Boolean active;
    
    // 使用value字段
    @DdlColumn("created_at")
    private LocalDateTime createdAt;
    
    // 使用value字段
    @DdlColumn("updated_at")
    private LocalDateTime updatedAt;
    
    public Product() {
    }
    
    public Product(JsonObject json) {
        // 简单的JSON转换实现
        this.id = json.getLong("id");
        this.name = json.getString("name");
        this.code = json.getString("code");
        this.categoryId = json.getLong("categoryId");
        this.price = json.getValue("price") != null ? new BigDecimal(json.getString("price")) : null;
        this.stockQuantity = json.getInteger("stockQuantity");
        this.description = json.getString("description");
        this.active = json.getBoolean("active");
        this.createdAt = json.getString("createdAt") != null ? LocalDateTime.parse(json.getString("createdAt")) : null;
        this.updatedAt = json.getString("updatedAt") != null ? LocalDateTime.parse(json.getString("updatedAt")) : null;
    }
    
    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.put("id", id);
        json.put("name", name);
        json.put("code", code);
        json.put("categoryId", categoryId);
        json.put("price", price != null ? price.toString() : null);
        json.put("stockQuantity", stockQuantity);
        json.put("description", description);
        json.put("active", active);
        json.put("createdAt", createdAt != null ? createdAt.toString() : null);
        json.put("updatedAt", updatedAt != null ? updatedAt.toString() : null);
        return json;
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
    
    public String getCode() {
        return code;
    }
    
    public void setCode(String code) {
        this.code = code;
    }
    
    public Long getCategoryId() {
        return categoryId;
    }
    
    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }
    
    public BigDecimal getPrice() {
        return price;
    }
    
    public void setPrice(BigDecimal price) {
        this.price = price;
    }
    
    public Integer getStockQuantity() {
        return stockQuantity;
    }
    
    public void setStockQuantity(Integer stockQuantity) {
        this.stockQuantity = stockQuantity;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Boolean getActive() {
        return active;
    }
    
    public void setActive(Boolean active) {
        this.active = active;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", code='" + code + '\'' +
                ", categoryId=" + categoryId +
                ", price=" + price +
                ", stockQuantity=" + stockQuantity +
                ", description='" + description + '\'' +
                ", active=" + active +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
