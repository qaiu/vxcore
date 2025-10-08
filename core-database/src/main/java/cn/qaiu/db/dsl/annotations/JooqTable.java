package cn.qaiu.db.dsl.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * jOOQ表注解
 * 用于标记实体类对应的数据库表信息
 * 
 * 注意：如果实体类已经使用了@DdlTable注解，将优先使用DDL注解的信息
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface JooqTable {
    
    /**
     * 表名
     */
    String name() default "";
    
    /**
     * 主键字段名
     */
    String primaryKey() default "id";
    
    /**
     * 架构名（可选）
     */
    String schema() default "";
}
