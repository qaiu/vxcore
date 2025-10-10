# Lambda查询指南

## 概述

VXCore数据库模块提供了类似MyBatis-Plus的Lambda查询功能，支持类型安全的字段引用和查询构建。

## 核心特性

- **类型安全**: 使用Lambda表达式引用实体字段，编译时检查
- **字段映射**: 自动处理Java字段名与数据库字段名的转换
- **注解支持**: 支持`@DdlColumn`注解自定义字段映射
- **链式调用**: 支持链式构建复杂查询条件
- **性能优化**: 字段名缓存，避免重复解析

## 快速开始

### 1. 实体类定义

```java
@DdlTable("products")
public class Product extends BaseEntity {
    
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
    
    @DdlColumn("is_active")
    private Boolean active;
    
    // getter/setter方法...
}
```

### 2. DAO类定义

```java
public class ProductDao extends AbstractDao<Product> {
    
    public ProductDao(DSLContext dslContext) {
        super(dslContext, DSL.table("products"), Product.class);
    }
    
    // 继承的Lambda查询方法
    // - lambdaQuery(): 创建LambdaQueryWrapper
    // - lambdaList(): 执行Lambda查询并返回列表
    // - lambdaOne(): 执行Lambda查询并返回单个结果
    // - lambdaCount(): 执行Lambda查询并返回计数
}
```

### 3. 基础查询

```java
// 等值查询
List<Product> products = productDao.lambdaList(
    productDao.lambdaQuery()
        .eq(Product::getName, "iPhone 15 Pro")
);

// 范围查询
List<Product> products = productDao.lambdaList(
    productDao.lambdaQuery()
        .ge(Product::getPrice, new BigDecimal("100.00"))
        .le(Product::getPrice, new BigDecimal("500.00"))
);

// 模糊查询
List<Product> products = productDao.lambdaList(
    productDao.lambdaQuery()
        .like(Product::getName, "%手机%")
);

// IN查询
List<Product> products = productDao.lambdaList(
    productDao.lambdaQuery()
        .in(Product::getCategoryId, Arrays.asList(1L, 2L, 3L))
);
```

### 4. 复杂查询

```java
// 多条件组合查询
List<Product> products = productDao.lambdaList(
    productDao.lambdaQuery()
        .eq(Product::getActive, true)
        .ge(Product::getPrice, new BigDecimal("200.00"))
        .le(Product::getPrice, new BigDecimal("2000.00"))
        .in(Product::getCategoryId, Arrays.asList(1L, 2L, 3L))
        .orderByDesc(Product::getPrice)
        .orderByAsc(Product::getName)
        .limit(10)
);

// 计数查询
Long count = productDao.lambdaCount(
    productDao.lambdaQuery()
        .eq(Product::getActive, true)
        .ge(Product::getPrice, new BigDecimal("100.00"))
);

// 单个结果查询
Optional<Product> product = productDao.lambdaOne(
    productDao.lambdaQuery()
        .eq(Product::getCode, "IPHONE15PRO")
);
```

## 字段映射

### 默认映射规则

- Java字段名（驼峰）自动转换为数据库字段名（下划线）
- `productName` → `product_name`
- `categoryId` → `category_id`
- `isActive` → `is_active`

### 注解映射

```java
public class Product {
    
    // 使用value字段指定数据库字段名
    @DdlColumn("product_id")
    private Long id;
    
    // 使用name字段指定数据库字段名（value的别名）
    @DdlColumn(name = "product_name")
    private String name;
    
    // 默认映射：categoryId → category_id
    private Long categoryId;
}
```

### LambdaUtils工具类

```java
// 获取字段名
String fieldName = LambdaUtils.getFieldName(Product::getName);
// 返回: "product_name"

// 获取字段类型
Class<?> fieldType = LambdaUtils.getFieldType(Product::getName);
// 返回: String.class
```

## 查询条件

### 比较条件

```java
// 等于
.eq(Product::getName, "iPhone")

// 不等于
.ne(Product::getActive, false)

// 大于
.gt(Product::getPrice, new BigDecimal("100.00"))

// 大于等于
.ge(Product::getPrice, new BigDecimal("100.00"))

// 小于
.lt(Product::getStockQuantity, 100)

// 小于等于
.le(Product::getStockQuantity, 100)
```

### 范围条件

```java
// IN查询
.in(Product::getCategoryId, Arrays.asList(1L, 2L, 3L))

// NOT IN查询
.notIn(Product::getId, Arrays.asList(999L, 1000L))

// BETWEEN查询
.between(Product::getPrice, new BigDecimal("100.00"), new BigDecimal("500.00"))
```

### 模糊查询

```java
// LIKE查询
.like(Product::getName, "%手机%")

// NOT LIKE查询
.notLike(Product::getCode, "%test%")
```

### 空值查询

```java
// IS NULL
.isNull(Product::getDescription)

// IS NOT NULL
.isNotNull(Product::getName)
```

### 排序

```java
// 升序
.orderByAsc(Product::getName)

// 降序
.orderByDesc(Product::getPrice)

// 多字段排序
.orderByDesc(Product::getPrice)
.orderByAsc(Product::getName)
```

### 分页

```java
// 限制数量
.limit(10)

// 偏移量
.offset(20)

// 分页（limit + offset）
.limit(10).offset(20)
```

## 高级功能

### 子查询

```java
// EXISTS子查询
LambdaQueryWrapper<Product> subWrapper = productDao.lambdaQuery()
    .eq(Product::getCategoryId, 1L);

LambdaQueryWrapper<Product> wrapper = productDao.lambdaQuery()
    .exists(subWrapper);
```

### 聚合函数

```java
// 计数
LambdaQueryWrapper<Product> wrapper = productDao.lambdaQuery()
    .eq(Product::getActive, true);

Long count = productDao.lambdaCount(wrapper);

// 聚合函数（需要自定义实现）
// .sum(Product::getPrice)
// .avg(Product::getPrice)
// .max(Product::getPrice)
// .min(Product::getPrice)
```

### 连接查询

```java
// 左连接（需要自定义实现）
// .leftJoin(Category.class, Product::getCategoryId, Category::getId)
```

## 性能优化

### 字段名缓存

LambdaUtils自动缓存字段名解析结果，避免重复反射操作：

```java
// 第一次调用会进行反射解析
String fieldName1 = LambdaUtils.getFieldName(Product::getName);

// 后续调用直接返回缓存结果
String fieldName2 = LambdaUtils.getFieldName(Product::getName);
```

### 查询优化建议

1. **合理使用索引**: 确保查询条件涉及的字段有索引
2. **避免全表扫描**: 使用合适的WHERE条件
3. **分页查询**: 大数据量查询使用limit/offset
4. **字段选择**: 只查询需要的字段

```java
// 好的做法：使用索引字段查询
List<Product> products = productDao.lambdaList(
    productDao.lambdaQuery()
        .eq(Product::getCode, "IPHONE15PRO") // 假设code字段有索引
        .limit(10)
);

// 避免的做法：全表扫描
List<Product> products = productDao.lambdaList(
    productDao.lambdaQuery()
        .like(Product::getDescription, "%test%") // 模糊查询可能全表扫描
);
```

## 错误处理

### 常见错误

1. **字段不存在**: 确保Lambda表达式引用的字段在实体类中存在
2. **类型不匹配**: 确保查询条件的值与字段类型匹配
3. **注解错误**: 确保@DdlColumn注解的字段名正确

### 调试技巧

```java
// 打印生成的SQL
LambdaQueryWrapper<Product> wrapper = productDao.lambdaQuery()
    .eq(Product::getName, "iPhone")
    .orderByDesc(Product::getPrice);

String sql = wrapper.buildSelect().getSQL();
System.out.println("Generated SQL: " + sql);
```

## 最佳实践

1. **实体设计**: 合理使用@DdlColumn注解
2. **查询构建**: 使用链式调用构建复杂查询
3. **性能考虑**: 注意查询性能和索引使用
4. **类型安全**: 充分利用Lambda表达式的类型安全特性
5. **代码可读性**: 使用有意义的变量名和注释

## 示例项目

完整示例请参考：
- `core-database/src/test/java/cn/qaiu/db/dsl/lambda/SimpleLambdaTest.java`
- `core-database/src/test/java/cn/qaiu/db/dsl/lambda/LambdaQueryUnitTest.java`
- `core-database/src/main/java/cn/qaiu/db/dsl/lambda/example/Product.java`
- `core-database/src/main/java/cn/qaiu/db/dsl/lambda/example/ProductDao.java`
