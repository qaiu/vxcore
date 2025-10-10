package cn.qaiu.example.entity;

import cn.qaiu.db.ddl.DdlColumn;
import cn.qaiu.db.ddl.DdlTable;
import io.vertx.core.json.JsonObject;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单详情实体类
 * 演示 VXCore 框架的实体映射和数据库操作
 * 
 * @author QAIU
 */
@DdlTable(
    value = "order_details",
    keyFields = "id",
    version = 1,
    autoSync = true,
    comment = "订单详情表",
    charset = "utf8mb4",
    collate = "utf8mb4_unicode_ci",
    engine = "InnoDB"
)
public class OrderDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    @DdlColumn(
        type = "BIGINT",
        autoIncrement = true,
        nullable = false,
        comment = "主键ID"
    )
    private Long id;

    @DdlColumn(
        type = "BIGINT",
        nullable = false,
        comment = "订单ID"
    )
    private Long orderId;

    @DdlColumn(
        type = "BIGINT",
        nullable = false,
        comment = "商品ID"
    )
    private Long productId;

    @DdlColumn(
        type = "VARCHAR(100)",
        nullable = false,
        comment = "商品名称"
    )
    private String productName;

    @DdlColumn(
        type = "DECIMAL(10,2)",
        nullable = false,
        comment = "单价"
    )
    private BigDecimal unitPrice;

    @DdlColumn(
        type = "INT",
        nullable = false,
        defaultValue = "1",
        comment = "数量"
    )
    private Integer quantity;

    @DdlColumn(
        type = "DECIMAL(10,2)",
        nullable = false,
        comment = "小计"
    )
    private BigDecimal subtotal;

    @DdlColumn(
        type = "VARCHAR(50)",
        comment = "分类"
    )
    private String category;

    @DdlColumn(
        type = "TEXT",
        comment = "描述"
    )
    private String description;

    @DdlColumn(
        type = "DATETIME",
        nullable = false,
        defaultValue = "CURRENT_TIMESTAMP",
        comment = "创建时间"
    )
    private LocalDateTime createTime;

    @DdlColumn(
        type = "DATETIME",
        nullable = false,
        defaultValue = "CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP",
        comment = "更新时间"
    )
    private LocalDateTime updateTime;

    public OrderDetail() {
    }

    public OrderDetail(JsonObject json) {
        this.id = json.getLong("id");
        this.orderId = json.getLong("orderId");
        this.productId = json.getLong("productId");
        this.productName = json.getString("productName");
        this.unitPrice = new BigDecimal(json.getString("unitPrice", "0"));
        this.quantity = json.getInteger("quantity");
        this.subtotal = new BigDecimal(json.getString("subtotal", "0"));
        this.category = json.getString("category");
        this.description = json.getString("description");
        this.createTime = json.getInstant("createTime") != null ? 
            json.getInstant("createTime").atZone(java.time.ZoneId.systemDefault()).toLocalDateTime() : null;
        this.updateTime = json.getInstant("updateTime") != null ? 
            json.getInstant("updateTime").atZone(java.time.ZoneId.systemDefault()).toLocalDateTime() : null;
    }

    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.put("id", this.id);
        json.put("orderId", this.orderId);
        json.put("productId", this.productId);
        json.put("productName", this.productName);
        json.put("unitPrice", this.unitPrice);
        json.put("quantity", this.quantity);
        json.put("subtotal", this.subtotal);
        json.put("category", this.category);
        json.put("description", this.description);
        json.put("createTime", this.createTime);
        json.put("updateTime", this.updateTime);
        return json;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    @Override
    public String toString() {
        return "OrderDetail{" +
                "id=" + id +
                ", orderId=" + orderId +
                ", productId=" + productId +
                ", productName='" + productName + '\'' +
                ", unitPrice=" + unitPrice +
                ", quantity=" + quantity +
                ", subtotal=" + subtotal +
                ", category='" + category + '\'' +
                ", description='" + description + '\'' +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                '}';
    }
}
