package cn.qaiu.vx.core.codegen;

/**
 * 字段信息
 * 封装字段的元数据信息
 * 
 * @author QAIU
 */
public class FieldInfo {
    
    private String fieldName;
    private String fieldType;
    private String columnName;
    private String columnType;
    private String description;
    private boolean primaryKey = false;
    private boolean nullable = true;
    private boolean unique = false;
    private int length = 0;
    private int precision = 0;
    private int scale = 0;
    private String defaultValue;
    private String comment;
    
    public FieldInfo() {
    }
    
    public FieldInfo(String fieldName, String fieldType) {
        this.fieldName = fieldName;
        this.fieldType = fieldType;
    }
    
    public FieldInfo(String fieldName, String fieldType, String columnName) {
        this.fieldName = fieldName;
        this.fieldType = fieldType;
        this.columnName = columnName;
    }
    
    public String getFieldName() {
        return fieldName;
    }
    
    public FieldInfo setFieldName(String fieldName) {
        this.fieldName = fieldName;
        return this;
    }
    
    public String getFieldType() {
        return fieldType;
    }
    
    public FieldInfo setFieldType(String fieldType) {
        this.fieldType = fieldType;
        return this;
    }
    
    public String getColumnName() {
        return columnName;
    }
    
    public FieldInfo setColumnName(String columnName) {
        this.columnName = columnName;
        return this;
    }
    
    public String getColumnType() {
        return columnType;
    }
    
    public FieldInfo setColumnType(String columnType) {
        this.columnType = columnType;
        return this;
    }
    
    public String getDescription() {
        return description;
    }
    
    public FieldInfo setDescription(String description) {
        this.description = description;
        return this;
    }
    
    public boolean isPrimaryKey() {
        return primaryKey;
    }
    
    public FieldInfo setPrimaryKey(boolean primaryKey) {
        this.primaryKey = primaryKey;
        return this;
    }
    
    public boolean isNullable() {
        return nullable;
    }
    
    public FieldInfo setNullable(boolean nullable) {
        this.nullable = nullable;
        return this;
    }
    
    public boolean isUnique() {
        return unique;
    }
    
    public FieldInfo setUnique(boolean unique) {
        this.unique = unique;
        return this;
    }
    
    public int getLength() {
        return length;
    }
    
    public FieldInfo setLength(int length) {
        this.length = length;
        return this;
    }
    
    public int getPrecision() {
        return precision;
    }
    
    public FieldInfo setPrecision(int precision) {
        this.precision = precision;
        return this;
    }
    
    public int getScale() {
        return scale;
    }
    
    public FieldInfo setScale(int scale) {
        this.scale = scale;
        return this;
    }
    
    public String getDefaultValue() {
        return defaultValue;
    }
    
    public FieldInfo setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }
    
    public String getComment() {
        return comment;
    }
    
    public FieldInfo setComment(String comment) {
        this.comment = comment;
        return this;
    }
    
    /**
     * 获取字段名的首字母大写形式
     * 
     * @return 首字母大写的字段名
     */
    public String getCapitalizedFieldName() {
        if (fieldName == null || fieldName.isEmpty()) {
            return fieldName;
        }
        return Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
    }
    
    /**
     * 获取Getter方法名
     * 
     * @return Getter方法名
     */
    public String getGetterName() {
        if (fieldName == null || fieldName.isEmpty()) {
            return fieldName;
        }
        
        String prefix = "boolean".equals(fieldType) ? "is" : "get";
        return prefix + getCapitalizedFieldName();
    }
    
    /**
     * 获取Setter方法名
     * 
     * @return Setter方法名
     */
    public String getSetterName() {
        return "set" + getCapitalizedFieldName();
    }
    
    /**
     * 设置Getter方法名
     * 
     * @param getterName Getter方法名
     * @return FieldInfo实例
     */
    public FieldInfo setGetterName(String getterName) {
        // 这里可以根据getterName反推fieldName，但为了简化，我们暂时不实现
        return this;
    }
    
    /**
     * 设置Setter方法名
     * 
     * @param setterName Setter方法名
     * @return FieldInfo实例
     */
    public FieldInfo setSetterName(String setterName) {
        // 这里可以根据setterName反推fieldName，但为了简化，我们暂时不实现
        return this;
    }
    
    /**
     * 获取简化的字段类型
     * 
     * @return 简化的字段类型
     */
    public String getSimpleFieldType() {
        if (fieldType == null) {
            return null;
        }
        
        // 移除包名，只保留类名
        int lastDot = fieldType.lastIndexOf('.');
        if (lastDot != -1) {
            return fieldType.substring(lastDot + 1);
        }
        
        return fieldType;
    }
    
    /**
     * 检查是否为基本类型
     * 
     * @return 是否为基本类型
     */
    public boolean isPrimitiveType() {
        if (fieldType == null) {
            return false;
        }
        
        String simpleType = getSimpleFieldType();
        return "int".equals(simpleType) ||
               "long".equals(simpleType) ||
               "double".equals(simpleType) ||
               "float".equals(simpleType) ||
               "boolean".equals(simpleType) ||
               "char".equals(simpleType) ||
               "byte".equals(simpleType) ||
               "short".equals(simpleType);
    }
    
    /**
     * 检查是否为包装类型
     * 
     * @return 是否为包装类型
     */
    public boolean isWrapperType() {
        if (fieldType == null) {
            return false;
        }
        
        String simpleType = getSimpleFieldType();
        return "Integer".equals(simpleType) ||
               "Long".equals(simpleType) ||
               "Double".equals(simpleType) ||
               "Float".equals(simpleType) ||
               "Boolean".equals(simpleType) ||
               "Character".equals(simpleType) ||
               "Byte".equals(simpleType) ||
               "Short".equals(simpleType);
    }
    
    /**
     * 检查是否为字符串类型
     * 
     * @return 是否为字符串类型
     */
    public boolean isStringType() {
        return "String".equals(getSimpleFieldType());
    }
    
    /**
     * 检查是否为日期时间类型
     * 
     * @return 是否为日期时间类型
     */
    public boolean isDateTimeType() {
        if (fieldType == null) {
            return false;
        }
        
        String simpleType = getSimpleFieldType();
        return "LocalDate".equals(simpleType) ||
               "LocalTime".equals(simpleType) ||
               "LocalDateTime".equals(simpleType) ||
               "Date".equals(simpleType) ||
               "Timestamp".equals(simpleType);
    }
    
    /**
     * 获取字段的默认值
     * 
     * @return 字段默认值
     */
    public String getFieldDefaultValue() {
        if (defaultValue != null) {
            return defaultValue;
        }
        
        if (isPrimitiveType()) {
            String simpleType = getSimpleFieldType();
            return switch (simpleType) {
                case "boolean" -> "false";
                case "int", "long", "double", "float", "char", "byte", "short" -> "0";
                default -> "null";
            };
        }
        
        return "null";
    }
    
    /**
     * 获取字段的注释
     * 
     * @return 字段注释
     */
    public String getFieldComment() {
        if (comment != null && !comment.trim().isEmpty()) {
            return comment;
        }
        if (description != null && !description.trim().isEmpty()) {
            return description;
        }
        return fieldName;
    }
    
    @Override
    public String toString() {
        return "FieldInfo{" +
                "fieldName='" + fieldName + '\'' +
                ", fieldType='" + fieldType + '\'' +
                ", columnName='" + columnName + '\'' +
                ", columnType='" + columnType + '\'' +
                ", description='" + description + '\'' +
                ", primaryKey=" + primaryKey +
                ", nullable=" + nullable +
                ", unique=" + unique +
                ", length=" + length +
                ", precision=" + precision +
                ", scale=" + scale +
                ", defaultValue='" + defaultValue + '\'' +
                ", comment='" + comment + '\'' +
                '}';
    }
}
