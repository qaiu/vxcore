package cn.qaiu.db.ddl;

import java.lang.annotation.*;

/**
 * DDL字段映射注解
 * 用于定义字段的详细映射信息
 *
 * @author <a href="https://qaiu.top">QAIU</a>
 */
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface DdlColumn {

    /**
     * 列名
     * @return 列名，为空时使用字段名转下划线
     */
    String name() default "";

    /**
     * SQL数据类型
     * @return SQL数据类型，为空时根据Java类型自动推断
     */
    String type() default "";

    /**
     * 字段长度
     * @return 字段长度，用于VARCHAR、DECIMAL等类型
     */
    int length() default 0;

    /**
     * 精度（用于DECIMAL类型）
     * @return 精度
     */
    int precision() default 0;

    /**
     * 小数位数（用于DECIMAL类型）
     * @return 小数位数
     */
    int scale() default 0;

    /**
     * 是否允许NULL
     * @return true允许NULL，false不允许NULL
     */
    boolean nullable() default true;

    /**
     * 默认值
     * @return 默认值
     */
    String defaultValue() default "";

    /**
     * 默认值是否为函数
     * @return true表示默认值是函数（如NOW()），false表示是字面值
     */
    boolean defaultValueIsFunction() default false;

    /**
     * 是否自增
     * @return true自增，false不自增
     */
    boolean autoIncrement() default false;

    /**
     * 字段注释
     * @return 字段注释
     */
    String comment() default "";

    /**
     * 唯一约束名称
     * @return 唯一约束名称，为空表示无唯一约束
     */
    String uniqueKey() default "";

    /**
     * 索引名称
     * @return 索引名称，为空表示无索引
     */
    String indexName() default "";

    /**
     * 字段版本号
     * 用于跟踪字段变更，每次修改字段时应该递增
     * @return 版本号
     */
    int version() default 1;
}
