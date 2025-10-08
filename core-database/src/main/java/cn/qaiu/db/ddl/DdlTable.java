package cn.qaiu.db.ddl;

import java.lang.annotation.*;

/**
 * 严格的DDL表映射注解
 * 用于实现Java对象与数据库表的严格映射和自动同步
 *
 * @author <a href="https://qaiu.top">QAIU</a>
 */
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface DdlTable {

    /**
     * 表名
     * @return 表名，为空时使用类名转下划线
     */
    String value() default "";

    /**
     * 主键字段名
     * @return 主键字段名，默认为"id"
     */
    String keyFields() default "id";

    /**
     * 表结构版本号
     * 用于跟踪表结构变更，每次修改表结构时应该递增
     * @return 版本号
     */
    int version() default 1;

    /**
     * 是否启用自动同步
     * @return true启用自动同步，false禁用
     */
    boolean autoSync() default true;

    /**
     * 表注释
     * @return 表注释
     */
    String comment() default "";

    /**
     * 字符集
     * @return 字符集，默认为utf8mb4
     */
    String charset() default "utf8mb4";

    /**
     * 排序规则
     * @return 排序规则，默认为utf8mb4_unicode_ci
     */
    String collate() default "utf8mb4_unicode_ci";

    /**
     * 存储引擎
     * @return 存储引擎，默认为InnoDB
     */
    String engine() default "InnoDB";

    /**
     * 数据库类型
     * 用于自动识别数据库类型，支持：mysql, postgresql, h2, oracle, sqlserver
     * 
     * @deprecated 此字段已过时，现在优先使用JDBC Pool的数据库类型自动检测。
     * 系统会从Pool连接中自动获取数据库类型，无需在注解中指定。
     * 优先级：Pool的JDBC类型 > 注解中的类型
     * 
     * @return 数据库类型，为空时自动检测
     */
    @Deprecated(since = "0.1.9", forRemoval = true)
    String dbtype() default "";
}
