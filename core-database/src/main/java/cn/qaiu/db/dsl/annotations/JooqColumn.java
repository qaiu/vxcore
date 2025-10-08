package cn.qaiu.db.dsl.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * jOOQ字段注解
 * 用于标记实体字段对应的数据库列名
 * 
 * 注意：如果字段已有@DdlColumn注解，将优先使用DDL注解信息
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface JooqColumn {
    
    /**
     * 数据库栏位名
     */
    String value() default "";
    
    /**
     * 是否为主键
     */
    boolean primary() default false;
    
    /**
     * 是否可空
     */
    boolean nullable() default true;
}
