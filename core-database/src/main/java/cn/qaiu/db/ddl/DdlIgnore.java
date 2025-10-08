package cn.qaiu.db.ddl;

import java.lang.annotation.*;

/**
 * DDL忽略注解
 * 用于标记不需要参与DDL映射的字段
 *
 * @author <a href="https://qaiu.top">QAIU</a>
 */
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface DdlIgnore {
}
