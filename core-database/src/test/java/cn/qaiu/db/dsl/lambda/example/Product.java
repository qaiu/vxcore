package cn.qaiu.db.dsl.lambda.example;

import cn.qaiu.db.ddl.DdlColumn;
import cn.qaiu.db.ddl.DdlTable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Lambda测试用的Product实体类
 *
 * @author <a href="https://qaiu.top">QAIU</a>
 */
@DdlTable("products")
public class Product {

  @DdlColumn("product_id")
  private Long id;

  @DdlColumn("product_name")
  private String name;

  @DdlColumn("product_code")
  private String code;

  @DdlColumn("category_id")
  private Long categoryId;

  @DdlColumn("price")
  private BigDecimal price;

  @DdlColumn("stock_quantity")
  private Integer stockQuantity;

  @DdlColumn("description")
  private String description;

  @DdlColumn("is_active")
  private Boolean active;

  @DdlColumn("created_at")
  private LocalDateTime createdAt;

  @DdlColumn("updated_at")
  private LocalDateTime updatedAt;

  // 构造函数
  public Product() {}

  public Product(String name, String code, Long categoryId, BigDecimal price, Integer stockQuantity) {
    this.name = name;
    this.code = code;
    this.categoryId = categoryId;
    this.price = price;
    this.stockQuantity = stockQuantity;
    this.active = true;
    this.createdAt = LocalDateTime.now();
    this.updatedAt = LocalDateTime.now();
  }

  // Getter和Setter方法
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
    return "Product{"
        + "id="
        + id
        + ", name='"
        + name
        + '\''
        + ", code='"
        + code
        + '\''
        + ", categoryId="
        + categoryId
        + ", price="
        + price
        + ", stockQuantity="
        + stockQuantity
        + ", active="
        + active
        + '}';
  }
}
