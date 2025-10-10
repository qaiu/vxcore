package cn.qaiu.example.entity;

import cn.qaiu.db.ddl.DdlColumn;
import cn.qaiu.db.ddl.DdlTable;
import cn.qaiu.db.dsl.BaseEntity;
import io.vertx.core.json.JsonObject;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 产品实体类
 * 演示 VXCore 框架的实体映射和数据库操作
 * 
 * @author QAIU
 */
@DdlTable(
    value = "products",
    keyFields = "id",
    version = 1,
    autoSync = true,
    comment = "产品表",
    charset = "utf8mb4",
    collate = "utf8mb4_unicode_ci",
    engine = "InnoDB"
)
public class Product extends BaseEntity {
    
    
    @DdlColumn(
        type = "VARCHAR",
        length = 100,
        nullable = false,
        comment = "产品名称"
    )
    private String name;
    
    @DdlColumn(
        type = "VARCHAR",
        length = 50,
        nullable = false,
        indexName = "idx_category",
        comment = "产品分类"
    )
    private String category;
    
    @DdlColumn(
        type = "DECIMAL",
        precision = 10,
        scale = 2,
        nullable = false,
        comment = "产品价格"
    )
    private BigDecimal price;
    
    @DdlColumn(
        type = "INT",
        nullable = false,
        defaultValue = "0",
        comment = "库存数量"
    )
    private Integer stock;
    
    @DdlColumn(
        type = "VARCHAR",
        length = 20,
        nullable = false,
        defaultValue = "'ACTIVE'",
        comment = "产品状态"
    )
    private ProductStatus status;
    
    @DdlColumn(
        type = "TEXT",
        nullable = true,
        comment = "产品描述"
    )
    private String description;
    
    
    public enum ProductStatus {
        ACTIVE, INACTIVE, OUT_OF_STOCK, DISCONTINUED
    }
    
    public Product() {}
    
    public Product(JsonObject json) {
        super(json);
        this.name = json.getString("name");
        this.category = json.getString("category");
        this.price = json.getValue("price") != null ? new BigDecimal(json.getValue("price").toString()) : null;
        this.stock = json.getInteger("stock");
        this.status = json.getString("status") != null ? ProductStatus.valueOf(json.getString("status")) : null;
        this.description = json.getString("description");
    }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }
    public ProductStatus getStatus() { return status; }
    public void setStatus(ProductStatus status) { this.status = status; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    @Override
    protected void fillJson(JsonObject json) {
        if (name != null) json.put("name", name);
        if (category != null) json.put("category", category);
        if (price != null) json.put("price", price);
        if (stock != null) json.put("stock", stock);
        if (status != null) json.put("status", status.name());
        if (description != null) json.put("description", description);
    }
    
    @Override
    public String toString() {
        return "Product{id=" + id + ", name='" + name + "', category='" + category + "', price=" + price + ", stock=" + stock + ", status=" + status + "}";
    }
}
