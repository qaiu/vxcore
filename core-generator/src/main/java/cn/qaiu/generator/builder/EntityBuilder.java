package cn.qaiu.generator.builder;

import cn.qaiu.generator.config.FeatureConfig;
import cn.qaiu.generator.config.PackageConfig;
import cn.qaiu.vx.core.codegen.ColumnInfo;
import cn.qaiu.vx.core.codegen.EntityInfo;
import cn.qaiu.vx.core.codegen.FieldInfo;
import cn.qaiu.vx.core.codegen.TableInfo;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 实体类构建器
 * 基于 TableInfo 构建 EntityInfo
 * 
 * @author QAIU
 */
public class EntityBuilder {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(EntityBuilder.class);
    
    private final FeatureConfig featureConfig;
    private final PackageConfig packageConfig;
    
    public EntityBuilder(FeatureConfig featureConfig, PackageConfig packageConfig) {
        this.featureConfig = featureConfig;
        this.packageConfig = packageConfig;
    }
    
    /**
     * 构建实体信息
     * 
     * @param tableInfo 表信息
     * @return 实体信息
     */
    public EntityInfo buildEntity(TableInfo tableInfo) {
        LOGGER.info("Building entity for table: {}", tableInfo.getTableName());
        
        EntityInfo entityInfo = new EntityInfo();
        
        // 设置基本信息
        String className = toCamelCase(tableInfo.getTableName(), true);
        entityInfo.setClassName(className);
        entityInfo.setTableName(tableInfo.getTableName());
        entityInfo.setDescription(tableInfo.getTableComment());
        entityInfo.setPackageName(packageConfig.getEntityPackage());
        
        // 构建字段信息
        List<FieldInfo> fields = buildFields(tableInfo.getColumns());
        entityInfo.setFields(fields);
        
        // 构建导入语句
        Set<String> imports = buildImports(fields);
        entityInfo.setImports(new ArrayList<>(imports));
        
        LOGGER.info("Entity built successfully: {}", className);
        return entityInfo;
    }
    
    /**
     * 构建字段信息
     */
    private List<FieldInfo> buildFields(List<ColumnInfo> columns) {
        List<FieldInfo> fields = new ArrayList<>();
        
        for (ColumnInfo column : columns) {
            FieldInfo fieldInfo = new FieldInfo();
            
            // 字段名转换
            String fieldName = toCamelCase(column.getColumnName(), false);
            fieldInfo.setFieldName(fieldName);
            
            // Java 类型
            String javaType = mapColumnTypeToJavaType(column.getColumnType());
            fieldInfo.setFieldType(javaType);
            
            // 描述
            fieldInfo.setDescription(column.getComment());
            
            // 主键标识
            fieldInfo.setPrimaryKey(column.isPrimaryKey());
            
            // 可空标识
            fieldInfo.setNullable(column.isNullable());
            
            // 默认值
            fieldInfo.setDefaultValue(column.getDefaultValue());
            
            // Getter/Setter 方法名
        // FieldInfo 中没有 setGetterName 和 setSetterName 方法，这些信息在模板中处理
            
            fields.add(fieldInfo);
        }
        
        return fields;
    }
    
    /**
     * 构建导入语句
     */
    private Set<String> buildImports(List<FieldInfo> fields) {
        Set<String> imports = new HashSet<>();
        
        // 基础导入
        imports.add("java.time.LocalDateTime");
        imports.add("java.time.LocalDate");
        imports.add("java.time.LocalTime");
        imports.add("java.math.BigDecimal");
        imports.add("java.util.Objects");
        
        // 根据字段类型添加导入
        for (FieldInfo field : fields) {
            String fieldType = field.getFieldType();
            
            if ("BigDecimal".equals(fieldType)) {
                imports.add("java.math.BigDecimal");
            } else if ("LocalDateTime".equals(fieldType)) {
                imports.add("java.time.LocalDateTime");
            } else if ("LocalDate".equals(fieldType)) {
                imports.add("java.time.LocalDate");
            } else if ("LocalTime".equals(fieldType)) {
                imports.add("java.time.LocalTime");
            }
        }
        
        // 根据功能配置添加注解导入
        if (featureConfig.isUseLombok()) {
            imports.add("lombok.Data");
            imports.add("lombok.NoArgsConstructor");
            imports.add("lombok.AllArgsConstructor");
        }
        
        if (featureConfig.isUseJpaAnnotations()) {
            imports.add("jakarta.persistence.Entity");
            imports.add("jakarta.persistence.Table");
            imports.add("jakarta.persistence.Id");
            imports.add("jakarta.persistence.Column");
            imports.add("jakarta.persistence.GeneratedValue");
            imports.add("jakarta.persistence.GenerationType");
        }
        
        if (featureConfig.isUseVertxAnnotations()) {
            imports.add("io.vertx.sqlclient.templates.annotations.RowMapped");
            imports.add("io.vertx.sqlclient.templates.annotations.Column");
        }
        
        if (featureConfig.isGenerateValidation()) {
            imports.add("jakarta.validation.constraints.NotNull");
            imports.add("jakarta.validation.constraints.NotBlank");
            imports.add("jakarta.validation.constraints.Size");
        }
        
        return imports;
    }
    
    /**
     * 下划线转驼峰命名
     * 
     * @param name 原始名称
     * @param isClass 是否为类名（首字母大写）
     * @return 驼峰命名
     */
    private String toCamelCase(String name, boolean isClass) {
        if (name == null || name.trim().isEmpty()) {
            return name;
        }
        
        String[] parts = name.toLowerCase().split("_");
        StringBuilder result = new StringBuilder();
        
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            if (part.isEmpty()) {
                continue;
            }
            
            if (i == 0 && !isClass) {
                result.append(part);
            } else {
                result.append(StringUtils.capitalize(part));
            }
        }
        
        return result.toString();
    }
    
    /**
     * 映射数据库类型到 Java 类型
     */
    private String mapColumnTypeToJavaType(String columnType) {
        if (columnType == null) {
            return "String";
        }
        
        String type = columnType.toLowerCase();
        if (type.contains("varchar") || type.contains("char") || type.contains("text")) {
            return "String";
        } else if (type.contains("bigint")) {
            return "Long";
        } else if (type.contains("int")) {
            return "Integer";
        } else if (type.contains("decimal") || type.contains("numeric")) {
            return "BigDecimal";
        } else if (type.contains("float")) {
            return "Float";
        } else if (type.contains("double")) {
            return "Double";
        } else if (type.contains("boolean") || type.contains("bit")) {
            return "Boolean";
        } else if (type.contains("date")) {
            return "LocalDate";
        } else if (type.contains("time")) {
            return "LocalTime";
        } else if (type.contains("timestamp") || type.contains("datetime")) {
            return "LocalDateTime";
        } else {
            return "String";
        }
    }
}
