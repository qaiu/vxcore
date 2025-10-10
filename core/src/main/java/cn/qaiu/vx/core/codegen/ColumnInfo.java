package cn.qaiu.vx.core.codegen;

/**
 * 列信息
 * 封装数据库列的元数据信息
 * 
 * @author QAIU
 */
public class ColumnInfo {
    
    private String columnName;
    private String columnType;
    private int dataType;
    private String typeName;
    private int columnSize;
    private int decimalDigits;
    private boolean nullable = true;
    private boolean primaryKey = false;
    private boolean unique = false;
    private boolean autoIncrement = false;
    private String defaultValue;
    private String comment;
    private String description;
    
    public ColumnInfo() {
    }
    
    public ColumnInfo(String columnName, String columnType) {
        this.columnName = columnName;
        this.columnType = columnType;
    }
    
    public String getColumnName() {
        return columnName;
    }
    
    public ColumnInfo setColumnName(String columnName) {
        this.columnName = columnName;
        return this;
    }
    
    public String getColumnType() {
        return columnType;
    }
    
    public ColumnInfo setColumnType(String columnType) {
        this.columnType = columnType;
        return this;
    }
    
    public int getDataType() {
        return dataType;
    }
    
    public ColumnInfo setDataType(int dataType) {
        this.dataType = dataType;
        return this;
    }
    
    public String getTypeName() {
        return typeName;
    }
    
    public ColumnInfo setTypeName(String typeName) {
        this.typeName = typeName;
        return this;
    }
    
    public int getColumnSize() {
        return columnSize;
    }
    
    public ColumnInfo setColumnSize(int columnSize) {
        this.columnSize = columnSize;
        return this;
    }
    
    public int getDecimalDigits() {
        return decimalDigits;
    }
    
    public ColumnInfo setDecimalDigits(int decimalDigits) {
        this.decimalDigits = decimalDigits;
        return this;
    }
    
    public boolean isNullable() {
        return nullable;
    }
    
    public ColumnInfo setNullable(boolean nullable) {
        this.nullable = nullable;
        return this;
    }
    
    public boolean isPrimaryKey() {
        return primaryKey;
    }
    
    public ColumnInfo setPrimaryKey(boolean primaryKey) {
        this.primaryKey = primaryKey;
        return this;
    }
    
    public boolean isUnique() {
        return unique;
    }
    
    public ColumnInfo setUnique(boolean unique) {
        this.unique = unique;
        return this;
    }
    
    public boolean isAutoIncrement() {
        return autoIncrement;
    }
    
    public ColumnInfo setAutoIncrement(boolean autoIncrement) {
        this.autoIncrement = autoIncrement;
        return this;
    }
    
    public String getDefaultValue() {
        return defaultValue;
    }
    
    public ColumnInfo setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }
    
    public String getComment() {
        return comment;
    }
    
    public ColumnInfo setComment(String comment) {
        this.comment = comment;
        return this;
    }
    
    public String getDescription() {
        return description;
    }
    
    public ColumnInfo setDescription(String description) {
        this.description = description;
        return this;
    }
    
    /**
     * 设置Java类型
     * 
     * @param javaType Java类型
     * @return ColumnInfo实例
     */
    public ColumnInfo setJavaType(String javaType) {
        // 这里可以根据javaType反推columnType，但为了简化，我们暂时不实现
        return this;
    }
    
    /**
     * 设置长度
     * 
     * @param length 长度
     * @return ColumnInfo实例
     */
    public ColumnInfo setLength(int length) {
        this.columnSize = length;
        return this;
    }
    
    /**
     * 获取Java字段名
     * 
     * @return Java字段名
     */
    public String getJavaFieldName() {
        if (columnName == null || columnName.isEmpty()) {
            return columnName;
        }
        
        // 将下划线命名转换为驼峰命名
        String[] parts = columnName.split("_");
        StringBuilder fieldName = new StringBuilder(parts[0].toLowerCase());
        
        for (int i = 1; i < parts.length; i++) {
            if (!parts[i].isEmpty()) {
                fieldName.append(Character.toUpperCase(parts[i].charAt(0)))
                        .append(parts[i].substring(1).toLowerCase());
            }
        }
        
        return fieldName.toString();
    }
    
    /**
     * 获取Java字段类型
     * 
     * @return Java字段类型
     */
    public String getJavaFieldType() {
        if (columnType == null) {
            return "String";
        }
        
        String lowerType = columnType.toLowerCase();
        
        return switch (lowerType) {
            case "int", "integer", "int4" -> "Integer";
            case "bigint", "int8" -> "Long";
            case "smallint", "int2" -> "Short";
            case "tinyint", "int1" -> "Byte";
            case "decimal", "numeric", "money" -> "BigDecimal";
            case "float", "real" -> "Float";
            case "double", "double precision" -> "Double";
            case "boolean", "bool", "bit" -> "Boolean";
            case "char", "varchar", "text", "string" -> "String";
            case "date" -> "LocalDate";
            case "time" -> "LocalTime";
            case "timestamp", "datetime" -> "LocalDateTime";
            case "blob", "binary", "varbinary" -> "byte[]";
            default -> "String";
        };
    }
    
    /**
     * 获取列的注释
     * 
     * @return 列注释
     */
    public String getColumnComment() {
        if (comment != null && !comment.trim().isEmpty()) {
            return comment;
        }
        if (description != null && !description.trim().isEmpty()) {
            return description;
        }
        return columnName;
    }
    
    /**
     * 检查是否为数值类型
     * 
     * @return 是否为数值类型
     */
    public boolean isNumericType() {
        if (columnType == null) {
            return false;
        }
        
        String lowerType = columnType.toLowerCase();
        return lowerType.contains("int") ||
               lowerType.contains("decimal") ||
               lowerType.contains("numeric") ||
               lowerType.contains("float") ||
               lowerType.contains("double") ||
               lowerType.contains("real") ||
               lowerType.contains("money");
    }
    
    /**
     * 检查是否为字符串类型
     * 
     * @return 是否为字符串类型
     */
    public boolean isStringType() {
        if (columnType == null) {
            return false;
        }
        
        String lowerType = columnType.toLowerCase();
        return lowerType.contains("char") ||
               lowerType.contains("varchar") ||
               lowerType.contains("text") ||
               lowerType.contains("string");
    }
    
    /**
     * 检查是否为日期时间类型
     * 
     * @return 是否为日期时间类型
     */
    public boolean isDateTimeType() {
        if (columnType == null) {
            return false;
        }
        
        String lowerType = columnType.toLowerCase();
        return lowerType.contains("date") ||
               lowerType.contains("time") ||
               lowerType.contains("timestamp");
    }
    
    /**
     * 检查是否为二进制类型
     * 
     * @return 是否为二进制类型
     */
    public boolean isBinaryType() {
        if (columnType == null) {
            return false;
        }
        
        String lowerType = columnType.toLowerCase();
        return lowerType.contains("blob") ||
               lowerType.contains("binary") ||
               lowerType.contains("varbinary");
    }
    
    @Override
    public String toString() {
        return "ColumnInfo{" +
                "columnName='" + columnName + '\'' +
                ", columnType='" + columnType + '\'' +
                ", dataType=" + dataType +
                ", typeName='" + typeName + '\'' +
                ", columnSize=" + columnSize +
                ", decimalDigits=" + decimalDigits +
                ", nullable=" + nullable +
                ", primaryKey=" + primaryKey +
                ", unique=" + unique +
                ", autoIncrement=" + autoIncrement +
                ", defaultValue='" + defaultValue + '\'' +
                ", comment='" + comment + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
