package cn.qaiu.db.ddl;

import cn.qaiu.db.pool.JDBCType;
import io.vertx.codegen.format.Case;
import io.vertx.codegen.format.LowerCamelCase;
import io.vertx.codegen.format.SnakeCase;
import io.vertx.sqlclient.templates.annotations.RowMapped;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * 表结构元数据
 * 用于存储表的结构信息，便于比较和同步
 *
 * @author <a href="https://qaiu.top">QAIU</a>
 */
public class TableMetadata {
    
    private String tableName;
    private String primaryKey;
    private int version;
    private boolean autoSync;
    private String comment;
    private String charset;
    private String collate;
    private String engine;
    private Case caseFormat;
    private JDBCType dbType;
    private Map<String, ColumnMetadata> columns = new HashMap<>();

    public TableMetadata() {}

    public TableMetadata(String tableName, String primaryKey, int version, boolean autoSync,
                        String comment, String charset, String collate, String engine,
                        Case caseFormat, JDBCType dbType) {
        this.tableName = tableName;
        this.primaryKey = primaryKey;
        this.version = version;
        this.autoSync = autoSync;
        this.comment = comment;
        this.charset = charset;
        this.collate = collate;
        this.engine = engine;
        this.caseFormat = caseFormat;
        this.dbType = dbType;
    }

    /**
     * 从Java类创建表元数据（自动检测数据库类型）
     */
    public static TableMetadata fromClass(Class<?> clz) {
        // 默认使用MySQL数据库类型
        return fromClass(clz, JDBCType.MySQL);
    }
    
    /**
     * 从Java类创建表元数据
     */
    public static TableMetadata fromClass(Class<?> clz, JDBCType dbType) {
        String tableName = null;
        String primaryKey = "id";
        int version = 1;
        boolean autoSync = true;
        String comment = "";
        String charset = "utf8mb4";
        String collate = "utf8mb4_unicode_ci";
        String engine = "InnoDB";
        Case caseFormat = SnakeCase.INSTANCE;
        JDBCType detectedDbType = dbType; // 默认使用传入的dbType

        // 处理DdlTable注解
        if (clz.isAnnotationPresent(DdlTable.class)) {
            DdlTable ddlTable = clz.getAnnotation(DdlTable.class);
            tableName = ddlTable.value().isEmpty() ? 
                LowerCamelCase.INSTANCE.to(SnakeCase.INSTANCE, clz.getSimpleName()) : 
                ddlTable.value();
            primaryKey = ddlTable.keyFields();
            version = ddlTable.version();
            autoSync = ddlTable.autoSync();
            comment = ddlTable.comment();
            charset = ddlTable.charset();
            collate = ddlTable.collate();
            engine = ddlTable.engine();
            
            // 处理dbtype字段（已过时，保留用于向后兼容）
            String dbtypeStr = ddlTable.dbtype();
            if (!dbtypeStr.isEmpty()) {
                detectedDbType = parseDbType(dbtypeStr);
            }
        } else if (clz.isAnnotationPresent(Table.class)) {
            // 兼容原有的Table注解
            Table table = clz.getAnnotation(Table.class);
            tableName = table.value().isEmpty() ? 
                LowerCamelCase.INSTANCE.to(SnakeCase.INSTANCE, clz.getSimpleName()) : 
                table.value();
            primaryKey = table.keyFields();
        }

        // 处理RowMapped注解
        if (clz.isAnnotationPresent(RowMapped.class)) {
            RowMapped rowMapped = clz.getAnnotation(RowMapped.class);
            caseFormat = getCase(rowMapped.formatter());
        }

        // 如果表名仍为null，使用类名转下划线命名
        if (tableName == null) {
            tableName = LowerCamelCase.INSTANCE.to(SnakeCase.INSTANCE, clz.getSimpleName());
        }

        TableMetadata metadata = new TableMetadata(tableName, primaryKey, version, autoSync,
                comment, charset, collate, engine, caseFormat, detectedDbType);

        // 处理字段
        Field[] fields = getFieldsInDeclarationOrder(clz);
        for (Field field : fields) {
            if (isIgnoredField(field)) {
                continue;
            }
            ColumnMetadata columnMetadata = ColumnMetadata.fromField(field, metadata);
            if (columnMetadata != null) {
                metadata.addColumn(columnMetadata);
            }
        }

        return metadata;
    }

    private static Case getCase(Class<?> clz) {
        return switch (clz.getName()) {
            case "io.vertx.codegen.format.CamelCase" -> io.vertx.codegen.format.CamelCase.INSTANCE;
            case "io.vertx.codegen.format.SnakeCase" -> SnakeCase.INSTANCE;
            case "io.vertx.codegen.format.LowerCamelCase" -> LowerCamelCase.INSTANCE;
            default -> SnakeCase.INSTANCE;
        };
    }

    // 获取按声明顺序排列的字段
    private static Field[] getFieldsInDeclarationOrder(Class<?> clz) {
        Field[] fields = clz.getDeclaredFields();
        // 按照字段名排序，这样可以保持相对一致的顺序
        // 注意：Java反射不保证字段的声明顺序，但我们可以通过排序来获得可预测的顺序
        java.util.Arrays.sort(fields, (f1, f2) -> {
            // 优先处理主键字段
            if ("id".equals(f1.getName()) && !"id".equals(f2.getName())) {
                return -1;
            }
            if (!"id".equals(f1.getName()) && "id".equals(f2.getName())) {
                return 1;
            }
            // 其他字段按名称排序
            return f1.getName().compareTo(f2.getName());
        });
        return fields;
    }

    private static boolean isIgnoredField(Field field) {
        return field.getName().equals("serialVersionUID")
                || field.isAnnotationPresent(TableGenIgnore.class)
                || field.isAnnotationPresent(DdlIgnore.class);
    }

    public void addColumn(ColumnMetadata column) {
        columns.put(column.getName(), column);
    }

    public ColumnMetadata getColumn(String name) {
        return columns.get(name);
    }

    public Map<String, ColumnMetadata> getColumns() {
        return columns;
    }

    // Getters and Setters
    public String getTableName() { return tableName; }
    public void setTableName(String tableName) { this.tableName = tableName; }

    public String getPrimaryKey() { return primaryKey; }
    public void setPrimaryKey(String primaryKey) { this.primaryKey = primaryKey; }

    public int getVersion() { return version; }
    public void setVersion(int version) { this.version = version; }

    public boolean isAutoSync() { return autoSync; }
    public void setAutoSync(boolean autoSync) { this.autoSync = autoSync; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public String getCharset() { return charset; }
    public void setCharset(String charset) { this.charset = charset; }

    public String getCollate() { return collate; }
    public void setCollate(String collate) { this.collate = collate; }

    public String getEngine() { return engine; }
    public void setEngine(String engine) { this.engine = engine; }

    public Case getCaseFormat() { return caseFormat; }
    public void setCaseFormat(Case caseFormat) { this.caseFormat = caseFormat; }

    public JDBCType getDbType() { return dbType; }
    public void setDbType(JDBCType dbType) { this.dbType = dbType; }
    
    /**
     * 解析数据库类型字符串（已过时，保留用于向后兼容）
     * @deprecated 现在优先使用Pool的数据库类型自动检测
     * @param dbtypeStr 数据库类型字符串
     * @return JDBCType枚举值
     */
    @Deprecated(since = "0.1.9", forRemoval = true)
    private static JDBCType parseDbType(String dbtypeStr) {
        if (dbtypeStr == null || dbtypeStr.trim().isEmpty()) {
            return JDBCType.MySQL; // 默认MySQL
        }
        
        String type = dbtypeStr.trim().toLowerCase();
        switch (type) {
            case "mysql":
                return JDBCType.MySQL;
            case "postgresql":
            case "postgres":
                return JDBCType.PostgreSQL;
            case "h2":
                return JDBCType.H2DB;
            case "oracle":
                return JDBCType.MySQL; // 暂时使用MySQL，后续可以添加Oracle支持
            case "sqlserver":
            case "mssql":
                return JDBCType.MySQL; // 暂时使用MySQL，后续可以添加SQL Server支持
            default:
                // 如果无法识别，尝试通过字符串匹配
                if (type.contains("mysql")) {
                    return JDBCType.MySQL;
                } else if (type.contains("postgres")) {
                    return JDBCType.PostgreSQL;
                } else if (type.contains("h2")) {
                    return JDBCType.H2DB;
                } else if (type.contains("oracle")) {
                    return JDBCType.MySQL; // 暂时使用MySQL
                } else if (type.contains("sqlserver") || type.contains("mssql")) {
                    return JDBCType.MySQL; // 暂时使用MySQL
                }
                return JDBCType.MySQL; // 默认返回MySQL
        }
    }
}
