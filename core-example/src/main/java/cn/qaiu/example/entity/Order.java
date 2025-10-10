package cn.qaiu.example.entity;

import cn.qaiu.db.ddl.DdlColumn;
import cn.qaiu.db.ddl.DdlTable;
import io.vertx.core.json.JsonObject;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单实体类
 * 演示 VXCore 框架的实体映射和数据库操作
 * 
 * @author QAIU
 */
@DdlTable(
    value = "orders",
    keyFields = "id",
    version = 1,
    autoSync = true,
    comment = "订单表",
    charset = "utf8mb4",
    collate = "utf8mb4_unicode_ci",
    engine = "InnoDB"
)
public class Order {

    @DdlColumn(
        type = "BIGINT",
        autoIncrement = true,
        nullable = false,
        comment = "订单ID"
    )
    private Long id;

    @DdlColumn(
        type = "VARCHAR",
        length = 64,
        nullable = false,
        uniqueKey = "order_no",
        comment = "订单号"
    )
    private String orderNo;

    @DdlColumn(
        type = "BIGINT",
        nullable = false,
        indexName = "idx_user_id",
        comment = "用户ID"
    )
    private Long userId;

    @DdlColumn(
        type = "BIGINT",
        nullable = false,
        indexName = "idx_product_id",
        comment = "商品ID"
    )
    private Long productId;

    @DdlColumn(
        type = "INT",
        nullable = false,
        defaultValue = "1",
        comment = "数量"
    )
    private Integer quantity;

    @DdlColumn(
        type = "DECIMAL",
        precision = 10,
        scale = 2,
        nullable = false,
        comment = "单价"
    )
    private BigDecimal unitPrice;

    @DdlColumn(
        type = "DECIMAL",
        precision = 10,
        scale = 2,
        nullable = false,
        comment = "总金额"
    )
    private BigDecimal totalAmount;

    @DdlColumn(
        type = "VARCHAR",
        length = 20,
        nullable = false,
        defaultValue = "'PENDING'",
        indexName = "idx_status",
        comment = "订单状态"
    )
    private OrderStatus status;

    @DdlColumn(
        type = "VARCHAR",
        length = 50,
        nullable = true,
        comment = "支付方式"
    )
    private String paymentMethod;

    @DdlColumn(
        type = "TIMESTAMP",
        nullable = true,
        comment = "支付时间"
    )
    private LocalDateTime paymentTime;

    @DdlColumn(
        type = "TIMESTAMP",
        nullable = true,
        comment = "发货时间"
    )
    private LocalDateTime shipTime;

    @DdlColumn(
        type = "TIMESTAMP",
        nullable = true,
        comment = "收货时间"
    )
    private LocalDateTime deliveryTime;

    @DdlColumn(
        type = "TIMESTAMP",
        nullable = true,
        comment = "取消时间"
    )
    private LocalDateTime cancelTime;

    @DdlColumn(
        type = "TEXT",
        nullable = true,
        comment = "备注"
    )
    private String remark;

    @DdlColumn(
        type = "TIMESTAMP",
        nullable = false,
        defaultValue = "CURRENT_TIMESTAMP",
        defaultValueIsFunction = true,
        indexName = "idx_created_at",
        comment = "创建时间"
    )
    private LocalDateTime createdAt;

    @DdlColumn(
        type = "TIMESTAMP",
        nullable = true,
        comment = "更新时间"
    )
    private LocalDateTime updatedAt;

    /**
     * 订单状态枚举
     */
    public enum OrderStatus {
        PENDING("待支付"),
        PAID("已支付"),
        SHIPPED("已发货"),
        DELIVERED("已收货"),
        CANCELLED("已取消");

        private final String description;

        OrderStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // 默认构造函数
    public Order() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.status = OrderStatus.PENDING;
        this.quantity = 1;
    }

    // 从 JsonObject 构造
    public Order(JsonObject json) {
        this.id = json.getLong("id");
        this.orderNo = json.getString("order_no");
        this.userId = json.getLong("user_id");
        this.productId = json.getLong("product_id");
        this.quantity = json.getInteger("quantity");
        this.unitPrice = json.getValue("unit_price") != null ? new BigDecimal(json.getValue("unit_price").toString()) : null;
        this.totalAmount = json.getValue("total_amount") != null ? new BigDecimal(json.getValue("total_amount").toString()) : null;
        this.status = json.getString("status") != null ? OrderStatus.valueOf(json.getString("status")) : OrderStatus.PENDING;
        this.paymentMethod = json.getString("payment_method");
        this.paymentTime = json.getString("payment_time") != null ? LocalDateTime.parse(json.getString("payment_time")) : null;
        this.shipTime = json.getString("ship_time") != null ? LocalDateTime.parse(json.getString("ship_time")) : null;
        this.deliveryTime = json.getString("delivery_time") != null ? LocalDateTime.parse(json.getString("delivery_time")) : null;
        this.cancelTime = json.getString("cancel_time") != null ? LocalDateTime.parse(json.getString("cancel_time")) : null;
        this.remark = json.getString("remark");
        this.createdAt = json.getString("created_at") != null ? LocalDateTime.parse(json.getString("created_at")) : LocalDateTime.now();
        this.updatedAt = json.getString("updated_at") != null ? LocalDateTime.parse(json.getString("updated_at")) : LocalDateTime.now();
    }

    // 转换为 JsonObject
    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        if (id != null) json.put("id", id);
        if (orderNo != null) json.put("order_no", orderNo);
        if (userId != null) json.put("user_id", userId);
        if (productId != null) json.put("product_id", productId);
        if (quantity != null) json.put("quantity", quantity);
        if (unitPrice != null) json.put("unit_price", unitPrice);
        if (totalAmount != null) json.put("total_amount", totalAmount);
        if (status != null) json.put("status", status.name());
        if (paymentMethod != null) json.put("payment_method", paymentMethod);
        if (paymentTime != null) json.put("payment_time", paymentTime.toString());
        if (shipTime != null) json.put("ship_time", shipTime.toString());
        if (deliveryTime != null) json.put("delivery_time", deliveryTime.toString());
        if (cancelTime != null) json.put("cancel_time", cancelTime.toString());
        if (remark != null) json.put("remark", remark);
        if (createdAt != null) json.put("created_at", createdAt.toString());
        if (updatedAt != null) json.put("updated_at", updatedAt.toString());
        return json;
    }

    /**
     * 计算总金额
     */
    public void calculateTotalAmount() {
        if (this.unitPrice != null && this.quantity != null) {
            this.totalAmount = this.unitPrice.multiply(new BigDecimal(this.quantity));
        }
    }

    /**
     * 支付订单
     */
    public void pay(String paymentMethod) {
        this.status = OrderStatus.PAID;
        this.paymentMethod = paymentMethod;
        this.paymentTime = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 发货
     */
    public void ship() {
        this.status = OrderStatus.SHIPPED;
        this.shipTime = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 确认收货
     */
    public void confirmDelivery() {
        this.status = OrderStatus.DELIVERED;
        this.deliveryTime = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 取消订单
     */
    public void cancel() {
        this.status = OrderStatus.CANCELLED;
        this.cancelTime = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public LocalDateTime getPaymentTime() {
        return paymentTime;
    }

    public void setPaymentTime(LocalDateTime paymentTime) {
        this.paymentTime = paymentTime;
    }

    public LocalDateTime getShipTime() {
        return shipTime;
    }

    public void setShipTime(LocalDateTime shipTime) {
        this.shipTime = shipTime;
    }

    public LocalDateTime getDeliveryTime() {
        return deliveryTime;
    }

    public void setDeliveryTime(LocalDateTime deliveryTime) {
        this.deliveryTime = deliveryTime;
    }

    public LocalDateTime getCancelTime() {
        return cancelTime;
    }

    public void setCancelTime(LocalDateTime cancelTime) {
        this.cancelTime = cancelTime;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
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
        return "Order{" +
                "id=" + id +
                ", orderNo='" + orderNo + '\'' +
                ", userId=" + userId +
                ", productId=" + productId +
                ", quantity=" + quantity +
                ", unitPrice=" + unitPrice +
                ", totalAmount=" + totalAmount +
                ", status=" + status +
                ", paymentMethod='" + paymentMethod + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}