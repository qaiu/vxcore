package cn.qaiu.vx.core.demo;

import cn.qaiu.vx.core.annotations.GenerateServiceGen;
import io.vertx.core.json.JsonObject;

@GenerateServiceGen(idType = Long.class, generateProxy = true)
public class Product {
    
    private Long id;
    private String name;
    private Double price;
    private String description;
    private String category;
    private Integer stock;
    private String status = "ACTIVE";
    
    // Constructors
    public Product() {}
    
    public Product(Long id, String name, Double price, String description, String category, Integer stock) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.description = description;
        this.category = category;
        this.stock = stock;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public JsonObject toJson() {
        return new JsonObject()
            .put("id", id)
            .put("name", name)
            .put("price", price)
            .put("description", description)
            .put("category", category)
            .put("stock", stock)
            .put("status", status);
    }
}
