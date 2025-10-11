package cn.qaiu.generator.model;

/**
 * DAO 风格枚举
 * 定义支持的三种 DAO 实现风格
 * 
 * @author QAIU
 */
public enum DaoStyle {
    
    /**
     * Vert.x SQL 风格
     * 使用原生 SQL + 实体映射
     * 实体类使用 @DdlTable, @DdlColumn 注解
     */
    VERTX_SQL("vertx", "Vert.x SQL"),
    
    /**
     * jOOQ 风格
     * 使用 jOOQ DSL API
     * 类型安全的 SQL 构建
     */
    JOOQ("jooq", "jOOQ DSL"),
    
    /**
     * Lambda 风格
     * VXCore 风格的 Lambda 查询
     * 继承 LambdaDao，使用 Lambda 表达式
     */
    LAMBDA("lambda", "MP Lambda");
    
    private final String code;
    private final String description;
    
    DaoStyle(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * 根据代码获取 DAO 风格
     * 
     * @param code 风格代码
     * @return DAO 风格
     */
    public static DaoStyle fromCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return LAMBDA; // 默认使用 Lambda 风格
        }
        
        for (DaoStyle style : values()) {
            if (style.code.equalsIgnoreCase(code.trim())) {
                return style;
            }
        }
        
        throw new IllegalArgumentException("Unsupported DAO style: " + code);
    }
    
    /**
     * 检查是否为 Vert.x SQL 风格
     */
    public boolean isVertxSql() {
        return this == VERTX_SQL;
    }
    
    /**
     * 检查是否为 jOOQ 风格
     */
    public boolean isJooq() {
        return this == JOOQ;
    }
    
    /**
     * 检查是否为 Lambda 风格
     */
    public boolean isLambda() {
        return this == LAMBDA;
    }
    
    @Override
    public String toString() {
        return description;
    }
}
