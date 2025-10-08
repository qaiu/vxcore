package cn.qaiu.db.ddl;

import cn.qaiu.db.pool.JDBCType;
import io.vertx.codegen.format.LowerCamelCase;
import io.vertx.sqlclient.templates.annotations.Column;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * 列结构元数据
 * 用于存储列的结构信息
 *
 * @author <a href="https://qaiu.top">QAIU</a>
 */
public class ColumnMetadata {
    
    private String name;
    private String type;
    private int length;
    private int precision;
    private int scale;
    private boolean nullable;
    private String defaultValue;
    private boolean defaultValueIsFunction;
    private boolean autoIncrement;
    private String comment;
    private String uniqueKey;
    private String indexName;
    private int version;
    private boolean isPrimaryKey;

    // Java类型到SQL类型的映射
    private static final Map<Class<?>, String> JAVA_TO_SQL_TYPE_MAP = new HashMap<Class<?>, String>() {{
        put(Integer.class, "INT");
        put(Short.class, "SMALLINT");
        put(Byte.class, "TINYINT");
        put(Long.class, "BIGINT");
        put(java.math.BigDecimal.class, "DECIMAL");
        put(Double.class, "DOUBLE");
        put(Float.class, "REAL");
        put(Boolean.class, "BOOLEAN");
        put(String.class, "VARCHAR");
        put(java.util.Date.class, "TIMESTAMP");
        put(java.time.LocalDateTime.class, "TIMESTAMP");
        put(java.time.LocalDate.class, "DATE");
        put(java.time.LocalTime.class, "TIME");
        put(java.sql.Timestamp.class, "TIMESTAMP");
        put(java.sql.Date.class, "DATE");
        put(java.sql.Time.class, "TIME");
        put(int.class, "INT");
        put(short.class, "SMALLINT");
        put(byte.class, "TINYINT");
        put(long.class, "BIGINT");
        put(double.class, "DOUBLE");
        put(float.class, "REAL");
        put(boolean.class, "BOOLEAN");
    }};

    public ColumnMetadata() {}

    public ColumnMetadata(String name, String type, int length, int precision, int scale,
                         boolean nullable, String defaultValue, boolean defaultValueIsFunction,
                         boolean autoIncrement, String comment, String uniqueKey,
                         String indexName, int version, boolean isPrimaryKey) {
        this.name = name;
        this.type = type;
        this.length = length;
        this.precision = precision;
        this.scale = scale;
        this.nullable = nullable;
        this.defaultValue = defaultValue;
        this.defaultValueIsFunction = defaultValueIsFunction;
        this.autoIncrement = autoIncrement;
        this.comment = comment;
        this.uniqueKey = uniqueKey;
        this.indexName = indexName;
        this.version = version;
        this.isPrimaryKey = isPrimaryKey;
    }

    /**
     * 从Java字段创建列元数据
     */
    public static ColumnMetadata fromField(Field field, TableMetadata tableMetadata) {
        String columnName = LowerCamelCase.INSTANCE.to(tableMetadata.getCaseFormat(), field.getName());
        String sqlType = JAVA_TO_SQL_TYPE_MAP.get(field.getType());
        
        if (sqlType == null) {
            // 对于不支持的字段类型，跳过该字段
            return null;
        }

        int length = 0;
        int precision = 22;
        int scale = 2;
        boolean nullable = true;
        String defaultValue = "";
        boolean defaultValueIsFunction = false;
        boolean autoIncrement = false;
        String comment = "";
        String uniqueKey = "";
        String indexName = "";
        int version = 1;

        // 处理DdlColumn注解
        if (field.isAnnotationPresent(DdlColumn.class)) {
            DdlColumn ddlColumn = field.getAnnotation(DdlColumn.class);
            if (!ddlColumn.name().isEmpty()) {
                columnName = ddlColumn.name();
            }
            if (!ddlColumn.type().isEmpty()) {
                sqlType = ddlColumn.type();
            }
            length = ddlColumn.length();
            precision = ddlColumn.precision();
            scale = ddlColumn.scale();
            nullable = ddlColumn.nullable();
            defaultValue = ddlColumn.defaultValue();
            defaultValueIsFunction = ddlColumn.defaultValueIsFunction();
            autoIncrement = ddlColumn.autoIncrement();
            comment = ddlColumn.comment();
            uniqueKey = ddlColumn.uniqueKey();
            indexName = ddlColumn.indexName();
            version = ddlColumn.version();
        } else {
            // 兼容原有注解
            if (field.isAnnotationPresent(Column.class)) {
                Column column = field.getAnnotation(Column.class);
                if (!column.name().isEmpty()) {
                    columnName = column.name();
                }
            }

            if (field.isAnnotationPresent(Length.class)) {
                Length lengthAnnotation = field.getAnnotation(Length.class);
                int[] decimalSize = lengthAnnotation.decimalSize();
                precision = decimalSize[0];
                scale = decimalSize[1];
                length = lengthAnnotation.varcharSize();
            }

            if (field.isAnnotationPresent(Constraint.class)) {
                Constraint constraint = field.getAnnotation(Constraint.class);
                nullable = !constraint.notNull();
                defaultValue = constraint.defaultValue();
                defaultValueIsFunction = constraint.defaultValueIsFunction();
                autoIncrement = constraint.autoIncrement();
                uniqueKey = constraint.uniqueKey();
            }
        }

        // 设置默认长度
        if (length == 0) {
            if ("VARCHAR".equals(sqlType)) {
                length = 255;
            } else if ("DECIMAL".equals(sqlType)) {
                length = precision;
            }
        }

        boolean isPrimaryKey = field.getName().equals(tableMetadata.getPrimaryKey());

        return new ColumnMetadata(columnName, sqlType, length, precision, scale,
                nullable, defaultValue, defaultValueIsFunction, autoIncrement,
                comment, uniqueKey, indexName, version, isPrimaryKey);
    }

    /**
     * 生成列定义SQL
     */
    public String toColumnDefinition(JDBCType dbType) {
        return toColumnDefinition(dbType, true);
    }

    /**
     * 生成列定义SQL
     * @param dbType 数据库类型
     * @param includePrimaryKey 是否包含主键定义（ALTER TABLE时应该为false）
     */
    public String toColumnDefinition(JDBCType dbType, boolean includePrimaryKey) {
        StringBuilder sb = new StringBuilder();
        String quotationMarks = dbType == JDBCType.MySQL ? "`" : "\"";

        sb.append(quotationMarks).append(name).append(quotationMarks).append(" ");

        // 处理PostgreSQL的SERIAL类型
        if (autoIncrement && dbType == JDBCType.PostgreSQL) {
            if ("INT".equals(type)) {
                sb.append("SERIAL");
            } else if ("BIGINT".equals(type)) {
                sb.append("BIGSERIAL");
            } else {
                sb.append(type);
            }
        } else {
            sb.append(type);
        }

        // 添加长度和精度
        if ("VARCHAR".equals(type) && length > 0) {
            sb.append("(").append(length).append(")");
        } else if ("DECIMAL".equals(type)) {
            sb.append("(").append(precision).append(",").append(scale).append(")");
        }

        // 添加约束 - PostgreSQL的SERIAL类型已经包含NOT NULL，不需要再添加
        if (!nullable && !(autoIncrement && dbType == JDBCType.PostgreSQL)) {
            sb.append(" NOT NULL");
        }

        // 对于PostgreSQL的SERIAL类型，不需要添加DEFAULT值
        if (defaultValue != null && !defaultValue.isEmpty() && !(autoIncrement && dbType == JDBCType.PostgreSQL)) {
            String apostrophe = defaultValueIsFunction ? "" : "'";
            
            // 处理BOOLEAN类型的默认值
            if ("BOOLEAN".equals(type) && dbType == JDBCType.MySQL) {
                if ("true".equals(defaultValue)) {
                    sb.append(" DEFAULT 1");
                } else if ("false".equals(defaultValue)) {
                    sb.append(" DEFAULT 0");
                } else {
                    sb.append(" DEFAULT ").append(defaultValue);
                }
            } else {
                sb.append(" DEFAULT ").append(apostrophe).append(defaultValue).append(apostrophe);
            }
        }

        // 对于非PostgreSQL数据库，添加AUTO_INCREMENT
        if (autoIncrement && dbType != JDBCType.PostgreSQL) {
            sb.append(" AUTO_INCREMENT");
        }

        if (isPrimaryKey && includePrimaryKey) {
            sb.append(" PRIMARY KEY");
        }

        // 添加注释（PostgreSQL不支持在列定义中包含注释）
        if (dbType != JDBCType.PostgreSQL && comment != null && !comment.isEmpty()) {
            sb.append(" COMMENT '").append(comment).append("'");
        }

        return sb.toString();
    }

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public int getLength() { return length; }
    public void setLength(int length) { this.length = length; }

    public int getPrecision() { return precision; }
    public void setPrecision(int precision) { this.precision = precision; }

    public int getScale() { return scale; }
    public void setScale(int scale) { this.scale = scale; }

    public boolean isNullable() { return nullable; }
    public void setNullable(boolean nullable) { this.nullable = nullable; }

    public String getDefaultValue() { return defaultValue; }
    public void setDefaultValue(String defaultValue) { this.defaultValue = defaultValue; }

    public boolean isDefaultValueIsFunction() { return defaultValueIsFunction; }
    public void setDefaultValueIsFunction(boolean defaultValueIsFunction) { this.defaultValueIsFunction = defaultValueIsFunction; }

    public boolean isAutoIncrement() { return autoIncrement; }
    public void setAutoIncrement(boolean autoIncrement) { this.autoIncrement = autoIncrement; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public String getUniqueKey() { return uniqueKey; }
    public void setUniqueKey(String uniqueKey) { this.uniqueKey = uniqueKey; }

    public String getIndexName() { return indexName; }
    public void setIndexName(String indexName) { this.indexName = indexName; }

    public int getVersion() { return version; }
    public void setVersion(int version) { this.version = version; }

    public boolean isPrimaryKey() { return isPrimaryKey; }
    public void setPrimaryKey(boolean primaryKey) { isPrimaryKey = primaryKey; }
}
