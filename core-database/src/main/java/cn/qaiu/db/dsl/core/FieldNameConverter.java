package cn.qaiu.db.dsl.core;

import cn.qaiu.db.ddl.DdlColumn;
import cn.qaiu.vx.core.util.StringCase;

import java.lang.reflect.Field;

/**
 * 字段名转换工具类
 * 统一处理Java字段名与数据库字段名的转换
 * 默认使用下划线命名，符合数据库字段命名规范
 * 
 * @author <a href="https://qaiu.top">QAIU</a>
 */
public class FieldNameConverter {
    
    /**
     * 将Java字段名转换为数据库字段名
     * 
     * @param field Java字段
     * @return 数据库字段名
     */
    public static String toDatabaseFieldName(Field field) {
        // 优先使用DDL注解
        DdlColumn ddlColumn = field.getAnnotation(DdlColumn.class);
        if (ddlColumn != null) {
            // 优先使用value字段，如果为空则使用name字段
            if (!ddlColumn.value().isEmpty()) {
                return ddlColumn.value();
            } else if (!ddlColumn.name().isEmpty()) {
                return ddlColumn.name();
            }
        }
        
        // 默认使用字段名的驼峰转下划线（符合数据库字段命名规范）
        return StringCase.toUnderlineCase(field.getName());
    }
    
    /**
     * 将Java字段名转换为数据库字段名
     * 
     * @param javaFieldName Java字段名
     * @return 数据库字段名
     */
    public static String toDatabaseFieldName(String javaFieldName) {
        // 默认使用字段名的驼峰转下划线（符合数据库字段命名规范）
        return StringCase.toUnderlineCase(javaFieldName);
    }
    
    /**
     * 将数据库字段名转换为Java字段名
     * 
     * @param databaseFieldName 数据库字段名
     * @return Java字段名
     */
    public static String toJavaFieldName(String databaseFieldName) {
        // 将下划线转换为驼峰命名
        return StringCase.toLittleCamelCase(databaseFieldName);
    }
    
    /**
     * 将Java类名转换为数据库表名
     * 
     * @param javaClassName Java类名
     * @return 数据库表名
     */
    public static String toDatabaseTableName(String javaClassName) {
        // 默认使用类名的驼峰转下划线（符合数据库表命名规范）
        return StringCase.toUnderlineCase(javaClassName);
    }
}
