package cn.qaiu.example.util;

import cn.qaiu.example.config.DatabaseConfig;
import cn.qaiu.example.dao.UserDao;
import cn.qaiu.example.dao.ProductDao;
import cn.qaiu.example.entity.User;
import cn.qaiu.example.entity.Product;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * 数据库工具类
 * 用于初始化演示数据
 * 
 * @author QAIU
 */
public class DatabaseUtils {
    
    /**
     * 创建 H2 数据库连接池
     */
    public static Future<JDBCClient> createH2Pool(DatabaseConfig config) {
        JsonObject jdbcConfig = new JsonObject()
                .put("url", config.getUrl())
                .put("driver_class", config.getDriver())
                .put("user", config.getUsername())
                .put("password", config.getPassword())
                .put("max_pool_size", config.getMaxPoolSize())
                .put("min_pool_size", config.getMinPoolSize());
        
        // 这里需要 Vertx 实例，暂时返回成功
        // 实际实现需要从 VertxHolder 获取 Vertx 实例
        return Future.succeededFuture(null);
    }
    
    /**
     * 插入演示用户数据
     */
    public static Future<Void> insertDemoUsers(UserDao userDao) {
        List<User> demoUsers = Arrays.asList(
                createUser("张三", "zhangsan@example.com", "password123", 25, "软件工程师", User.UserStatus.ACTIVE, new BigDecimal("1000.00")),
                createUser("李四", "lisi@example.com", "password123", 30, "产品经理", User.UserStatus.ACTIVE, new BigDecimal("2000.00")),
                createUser("王五", "wangwu@example.com", "password123", 28, "设计师", User.UserStatus.INACTIVE, new BigDecimal("1500.00")),
                createUser("赵六", "zhaoliu@example.com", "password123", 35, "架构师", User.UserStatus.ACTIVE, new BigDecimal("3000.00")),
                createUser("钱七", "qianqi@example.com", "password123", 22, "实习生", User.UserStatus.SUSPENDED, new BigDecimal("500.00"))
        );
        
        List<Future<User>> futures = demoUsers.stream()
                .map(userDao::save)
                .collect(java.util.stream.Collectors.toList());
        
        return Future.all(futures)
                .mapEmpty();
    }
    
    /**
     * 插入演示产品数据
     */
    public static Future<Void> insertDemoProducts(ProductDao productDao) {
        List<Product> demoProducts = Arrays.asList(
                createProduct("iPhone 15", "Electronics", new BigDecimal("7999.00"), 50, Product.ProductStatus.ACTIVE, "最新款iPhone"),
                createProduct("MacBook Pro", "Electronics", new BigDecimal("12999.00"), 30, Product.ProductStatus.ACTIVE, "专业级笔记本电脑"),
                createProduct("Nike Air Max", "Clothing", new BigDecimal("899.00"), 100, Product.ProductStatus.ACTIVE, "经典运动鞋"),
                createProduct("Java编程思想", "Books", new BigDecimal("89.00"), 200, Product.ProductStatus.ACTIVE, "经典编程书籍"),
                createProduct("Spring Boot实战", "Books", new BigDecimal("79.00"), 150, Product.ProductStatus.ACTIVE, "Spring Boot开发指南"),
                createProduct("小米13", "Electronics", new BigDecimal("3999.00"), 0, Product.ProductStatus.OUT_OF_STOCK, "小米旗舰手机"),
                createProduct("Adidas T恤", "Clothing", new BigDecimal("199.00"), 80, Product.ProductStatus.ACTIVE, "舒适运动T恤"),
                createProduct("Python编程", "Books", new BigDecimal("69.00"), 120, Product.ProductStatus.ACTIVE, "Python入门教程")
        );
        
        List<Future<Product>> futures = demoProducts.stream()
                .map(productDao::save)
                .collect(java.util.stream.Collectors.toList());
        
        return Future.all(futures)
                .mapEmpty();
    }
    
    /**
     * 创建用户对象
     */
    private static User createUser(String username, String email, String password, Integer age, 
                                 String bio, User.UserStatus status, BigDecimal balance) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(password);
        user.setAge(age);
        user.setBio(bio);
        user.setStatus(status);
        user.setBalance(balance);
        user.setEmailVerified(false);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        return user;
    }
    
    /**
     * 创建产品对象
     */
    private static Product createProduct(String name, String category, BigDecimal price, 
                                       Integer stock, Product.ProductStatus status, String description) {
        Product product = new Product();
        product.setName(name);
        product.setCategory(category);
        product.setPrice(price);
        product.setStock(stock);
        product.setStatus(status);
        product.setDescription(description);
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());
        return product;
    }
}
