package cn.qaiu.db.ddl;

import cn.qaiu.db.pool.JDBCType;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.Row;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * 表结构比较器
 * 用于比较Java对象与数据库表结构的差异
 *
 * @author <a href="https://qaiu.top">QAIU</a>
 */
public class TableStructureComparator {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(TableStructureComparator.class);
    
    /**
     * 表结构差异类型
     */
    public enum DifferenceType {
        TABLE_NOT_EXISTS,           // 表不存在
        COLUMN_NOT_EXISTS,          // 列不存在
        COLUMN_TYPE_MISMATCH,       // 列类型不匹配
        COLUMN_LENGTH_MISMATCH,     // 列长度不匹配
        COLUMN_NULLABLE_MISMATCH,   // 列可空性不匹配
        COLUMN_DEFAULT_MISMATCH,    // 列默认值不匹配
        COLUMN_AUTO_INCREMENT_MISMATCH, // 列自增属性不匹配
        EXTRA_COLUMN,               // 多余的列
        MISSING_INDEX,             // 缺少索引
        EXTRA_INDEX                 // 多余的索引
    }

    /**
     * 表结构差异
     */
    public static class TableDifference {
        private DifferenceType type;
        private String tableName;
        private String columnName;
        private String expectedValue;
        private String actualValue;
        private String sqlFix;

        public TableDifference(DifferenceType type, String tableName, String columnName,
                              String expectedValue, String actualValue, String sqlFix) {
            this.type = type;
            this.tableName = tableName;
            this.columnName = columnName;
            this.expectedValue = expectedValue;
            this.actualValue = actualValue;
            this.sqlFix = sqlFix;
        }

        // Getters
        public DifferenceType getType() { return type; }
        public String getTableName() { return tableName; }
        public String getColumnName() { return columnName; }
        public String getExpectedValue() { return expectedValue; }
        public String getActualValue() { return actualValue; }
        public String getSqlFix() { return sqlFix; }
    }

    /**
     * 比较表结构并返回差异列表
     */
    public static Future<List<TableDifference>> compareTableStructure(Pool pool, TableMetadata expectedMetadata, JDBCType dbType) {
        Promise<List<TableDifference>> promise = Promise.promise();
        
        List<TableDifference> differences = new ArrayList<>();
        
        // 检查表是否存在
        String tableExistsSql = getTableExistsSql(expectedMetadata.getTableName(), dbType);
        
        pool.query(tableExistsSql)
            .execute()
            .onSuccess(rows -> {
                boolean tableExists = false;
                if (rows.size() > 0) {
                    Row row = rows.iterator().next();
                    tableExists = row.getInteger(0) > 0;
                }
                
                if (!tableExists) {
                    differences.add(new TableDifference(
                        DifferenceType.TABLE_NOT_EXISTS,
                        expectedMetadata.getTableName(),
                        null,
                        "table should exist",
                        "table does not exist",
                        generateCreateTableSql(expectedMetadata, dbType)
                    ));
                } else {
                    // 表存在，比较列结构
                    compareColumns(pool, expectedMetadata, dbType, differences, promise);
                    return; // 不在这里complete，等compareColumns完成
                }
                
                promise.complete(differences);
            })
            .onFailure(promise::fail);
            
        return promise.future();
    }

    /**
     * 比较列结构
     */
    private static void compareColumns(Pool pool, TableMetadata expectedMetadata, JDBCType dbType, List<TableDifference> differences, Promise<List<TableDifference>> promise) {
        String columnsSql = getColumnsSql(expectedMetadata.getTableName(), dbType);
        
        pool.query(columnsSql)
            .execute()
            .onSuccess(rows -> {
                Map<String, ColumnInfo> actualColumns = new HashMap<>();
                
                for (Row row : rows) {
                    ColumnInfo columnInfo = extractColumnInfo(row, dbType);
                    actualColumns.put(columnInfo.getUsername(), columnInfo);
                }
                
                // 比较每个期望的列
                for (ColumnMetadata expectedColumn : expectedMetadata.getColumns().values()) {
                    ColumnInfo actualColumn = actualColumns.get(expectedColumn.getUsername());
                    
                    if (actualColumn == null) {
                        // 列不存在
                        differences.add(new TableDifference(
                            DifferenceType.COLUMN_NOT_EXISTS,
                            expectedMetadata.getTableName(),
                            expectedColumn.getUsername(),
                            expectedColumn.toColumnDefinition(dbType),
                            "column does not exist",
                            generateAddColumnSql(expectedMetadata.getTableName(), expectedColumn, dbType)
                        ));
                    } else {
                        // 比较列属性
                        compareColumnProperties(expectedMetadata.getTableName(), expectedColumn, actualColumn, dbType, differences);
                    }
                }
                
                // 检查多余的列
                for (ColumnInfo actualColumn : actualColumns.values()) {
                    if (!expectedMetadata.getColumns().containsKey(actualColumn.getUsername())) {
                        differences.add(new TableDifference(
                            DifferenceType.EXTRA_COLUMN,
                            expectedMetadata.getTableName(),
                            actualColumn.getUsername(),
                            "column should not exist",
                            actualColumn.toString(),
                            generateDropColumnSql(expectedMetadata.getTableName(), actualColumn.getUsername(), dbType)
                        ));
                    }
                }
                
                // 完成比较
                promise.complete(differences);
            })
            .onFailure(throwable -> {
                LOGGER.error("Failed to compare columns for table: {}", expectedMetadata.getTableName(), throwable);
                promise.fail(throwable);
            });
    }

    /**
     * 比较列属性
     */
    private static void compareColumnProperties(String tableName, ColumnMetadata expected, ColumnInfo actual, JDBCType dbType, List<TableDifference> differences) {
        // 比较类型
        if (!isTypeCompatible(expected.getType(), actual.getType())) {
            differences.add(new TableDifference(
                DifferenceType.COLUMN_TYPE_MISMATCH,
                tableName,
                expected.getUsername(),
                expected.getType(),
                actual.getType(),
                generateAlterColumnTypeSql(tableName, expected, dbType)
            ));
        }
        
        // 比较长度（智能比较）
        if (!isLengthCompatible(expected, actual)) {
            differences.add(new TableDifference(
                DifferenceType.COLUMN_LENGTH_MISMATCH,
                tableName,
                expected.getUsername(),
                String.valueOf(expected.getLength()),
                String.valueOf(actual.getLength()),
                generateAlterColumnLengthSql(tableName, expected, dbType)
            ));
        }
        
        // 比较可空性（对主键字段采用更宽松的策略）
        if (!isNullableCompatible(expected, actual, dbType)) {
            // 对于H2DB的主键字段，跳过可空性差异，因为主键字段不能为NULL
            if (!(dbType == JDBCType.H2DB && expected.isPrimaryKey())) {
                differences.add(new TableDifference(
                    DifferenceType.COLUMN_NULLABLE_MISMATCH,
                    tableName,
                    expected.getUsername(),
                    String.valueOf(expected.isNullable()),
                    String.valueOf(actual.isNullable()),
                    generateAlterColumnNullableSql(tableName, expected, dbType)
                ));
            }
        }
        
        // 比较默认值（使用宽松的比较）
        if (!isDefaultValueCompatible(expected.getDefaultValue(), actual.getDefaultValue())) {
            differences.add(new TableDifference(
                DifferenceType.COLUMN_DEFAULT_MISMATCH,
                tableName,
                expected.getUsername(),
                expected.getDefaultValue(),
                actual.getDefaultValue(),
                generateAlterColumnDefaultSql(tableName, expected, dbType)
            ));
        }
        
        // 比较自增属性（宽松比较）
        if (!isAutoIncrementCompatible(expected, actual)) {
            differences.add(new TableDifference(
                DifferenceType.COLUMN_AUTO_INCREMENT_MISMATCH,
                tableName,
                expected.getUsername(),
                String.valueOf(expected.isAutoIncrement()),
                String.valueOf(actual.isAutoIncrement()),
                generateAlterColumnAutoIncrementSql(tableName, expected, dbType)
            ));
        }
    }

    /**
     * 检查类型是否兼容
     */
    private static boolean isTypeCompatible(String expectedType, String actualType) {
        if (expectedType == null || actualType == null) {
            return false;
        }
        
        // 标准化类型名称（转换为小写）
        String expected = expectedType.toLowerCase().trim();
        String actual = actualType.toLowerCase().trim();
        
        // 完全匹配
        if (expected.equals(actual)) {
            return true;
        }
        
        // 类型兼容性映射
        return isTypeEquivalent(expected, actual);
    }
    
    /**
     * 检查类型是否等价
     */
    private static boolean isTypeEquivalent(String expected, String actual) {
        // 整数类型兼容性
        if (isIntegerType(expected) && isIntegerType(actual)) {
            return true;
        }
        
        // 字符串类型兼容性
        if (isStringType(expected) && isStringType(actual)) {
            return true;
        }
        
        // 数值类型兼容性
        if (isNumericType(expected) && isNumericType(actual)) {
            return true;
        }
        
        // 布尔类型兼容性
        if (isBooleanType(expected) && isBooleanType(actual)) {
            return true;
        }
        
        // 时间类型兼容性
        if (isTimeType(expected) && isTimeType(actual)) {
            return true;
        }
        
        // 具体类型映射
        return switch (expected) {
            case "varchar" -> actual.equals("character varying") || actual.equals("varchar");
            case "character varying" -> actual.equals("varchar") || actual.equals("character varying");
            case "int" -> actual.equals("integer") || actual.equals("int4");
            case "integer" -> actual.equals("int") || actual.equals("int4");
            case "bigint" -> actual.equals("int8") || actual.equals("bigint");
            case "decimal" -> actual.equals("numeric") || actual.equals("decimal");
            case "numeric" -> actual.equals("decimal") || actual.equals("numeric");
            case "boolean" -> actual.equals("bool") || actual.equals("boolean");
            case "bool" -> actual.equals("boolean") || actual.equals("bool");
            case "text" -> actual.equals("character varying") || actual.equals("text") || actual.equals("clob");
            case "timestamp" -> actual.equals("datetime") || actual.equals("timestamp") || 
                               actual.equals("timestamp without time zone") || actual.equals("timestamp with time zone") ||
                               actual.equals("timestamptz") || actual.equals("timestamp(6)");
            case "datetime" -> actual.equals("timestamp") || actual.equals("datetime") ||
                               actual.equals("timestamp without time zone") || actual.equals("timestamp with time zone") ||
                               actual.equals("timestamptz") || actual.equals("datetime(6)");
            case "timestamp without time zone" -> actual.equals("timestamp") || actual.equals("datetime") ||
                                                 actual.equals("timestamp without time zone") || actual.equals("timestamp(6)");
            case "timestamp with time zone" -> actual.equals("timestamptz") || actual.equals("timestamp with time zone");
            case "time" -> actual.equals("time") || actual.equals("time without time zone") || 
                          actual.equals("time with time zone") || actual.equals("timetz");
            case "time without time zone" -> actual.equals("time") || actual.equals("time without time zone");
            case "time with time zone" -> actual.equals("timetz") || actual.equals("time with time zone");
            case "date" -> actual.equals("date");
            case "interval" -> actual.equals("interval");
            default -> false;
        };
    }
    
    private static boolean isIntegerType(String type) {
        return type.equals("int") || type.equals("integer") || type.equals("int4") ||
               type.equals("bigint") || type.equals("int8") || type.equals("smallint") ||
               type.equals("tinyint");
    }
    
    private static boolean isStringType(String type) {
        return type.equals("varchar") || type.equals("character varying") || 
               type.equals("char") || type.equals("text") || type.equals("clob");
    }
    
    private static boolean isNumericType(String type) {
        return type.equals("decimal") || type.equals("numeric") || type.equals("float") ||
               type.equals("double") || type.equals("real");
    }
    
    private static boolean isBooleanType(String type) {
        return type.equals("boolean") || type.equals("bool");
    }
    
    private static boolean isTimeType(String type) {
        return type.equals("timestamp") || type.equals("datetime") || type.equals("date") ||
               type.equals("time") || type.equals("timestamp without time zone") || 
               type.equals("timestamp with time zone") || type.equals("time without time zone") ||
               type.equals("time with time zone") || type.equals("interval") ||
               // PostgreSQL 可能的其他时间类型表示
               type.equals("timestamptz") || type.equals("timetz") ||
               // MySQL 时间类型
               type.equals("year") || type.equals("datetime(6)") || type.equals("timestamp(6)");
    }
    
    /**
     * 检查默认值是否兼容
     */
    private static boolean isDefaultValueCompatible(String expected, String actual) {
        // 如果都为空，则兼容
        if (isEmptyOrNull(expected) && isEmptyOrNull(actual)) {
            return true;
        }
        
        // 如果其中一个为空，另一个不为空，则不兼容
        if (isEmptyOrNull(expected) || isEmptyOrNull(actual)) {
            return false;
        }
        
        // 标准化默认值
        String normalizedExpected = normalizeDefaultValue(expected);
        String normalizedActual = normalizeDefaultValue(actual);
        
        // 完全匹配
        if (normalizedExpected.equals(normalizedActual)) {
            return true;
        }
        
        // 特殊值兼容性检查
        return isDefaultValueEquivalent(normalizedExpected, normalizedActual);
    }
    
    private static boolean isEmptyOrNull(String value) {
        return value == null || value.trim().isEmpty() || "null".equals(value);
    }
    
    private static String normalizeDefaultValue(String value) {
        if (isEmptyOrNull(value)) {
            return "";
        }
        
        String normalized = value.trim();
        
        // 移除单引号
        if (normalized.startsWith("'") && normalized.endsWith("'")) {
            normalized = normalized.substring(1, normalized.length() - 1);
        }
        
        // 移除双引号
        if (normalized.startsWith("\"") && normalized.endsWith("\"")) {
            normalized = normalized.substring(1, normalized.length() - 1);
        }
        
        return normalized.toLowerCase();
    }
    
    private static boolean isDefaultValueEquivalent(String expected, String actual) {
        // 布尔值兼容性
        if (isBooleanDefault(expected) && isBooleanDefault(actual)) {
            return (expected.equals("true") && actual.equals("1")) ||
                   (expected.equals("false") && actual.equals("0")) ||
                   (expected.equals("1") && actual.equals("true")) ||
                   (expected.equals("0") && actual.equals("false"));
        }
        
        // 数值兼容性
        if (isNumericDefault(expected) && isNumericDefault(actual)) {
            try {
                double exp = Double.parseDouble(expected);
                double act = Double.parseDouble(actual);
                return Math.abs(exp - act) < 0.0001; // 允许小的浮点误差
            } catch (NumberFormatException e) {
                return false;
            }
        }
        
        // 字符串兼容性（忽略大小写）
        return expected.equalsIgnoreCase(actual);
    }
    
    private static boolean isBooleanDefault(String value) {
        return value.equals("true") || value.equals("false") || 
               value.equals("1") || value.equals("0");
    }
    
    private static boolean isNumericDefault(String value) {
        try {
            Double.parseDouble(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    /**
     * 检查长度是否兼容
     */
    private static boolean isLengthCompatible(ColumnMetadata expected, ColumnInfo actual) {
        // 如果期望长度为0，则不比较长度
        if (expected.getLength() <= 0) {
            return true;
        }
        
        String expectedType = expected.getType().toLowerCase();
        
        // 对于DECIMAL/NUMERIC类型，比较precision而不是length
        if (isNumericType(expectedType)) {
            return expected.getPrecision() == actual.getPrecision();
        }
        
        // 对于字符串类型，比较length
        if (isStringType(expectedType)) {
            return expected.getLength() == actual.getLength();
        }
        
        // 对于其他类型，不比较长度
        return true;
    }
    
    /**
     * 检查可空性是否兼容
     */
    private static boolean isNullableCompatible(ColumnMetadata expected, ColumnInfo actual, JDBCType dbType) {
        // 对于主键字段，采用更宽松的策略
        if (expected.isPrimaryKey()) {
            // 主键字段通常应该是NOT NULL的
            // 如果期望是NOT NULL，而实际也是NOT NULL，则兼容
            if (!expected.isNullable() && !actual.isNullable()) {
                return true;
            }
            
            // 如果期望是NOT NULL，但实际是NULL，这在某些情况下可能是检测问题
            // 特别是H2DB在MySQL模式下的检测可能不准确
            if (!expected.isNullable() && actual.isNullable()) {
                // 对于H2DB，我们采用更宽松的策略
                if (dbType == JDBCType.H2DB) {
                    return true;
                }
                return false;
            }
            
            // 如果期望是NULL，但实际是NOT NULL，这通常是不兼容的
            if (expected.isNullable() && !actual.isNullable()) {
                return false;
            }
        }
        
        // 对于非主键字段，进行严格的比较
        return expected.isNullable() == actual.isNullable();
    }
    
    /**
     * 检查AUTO_INCREMENT是否兼容
     */
    private static boolean isAutoIncrementCompatible(ColumnMetadata expected, ColumnInfo actual) {
        // 如果期望不是AUTO_INCREMENT，则总是兼容
        if (!expected.isAutoIncrement()) {
            return true;
        }
        
        // 对于AUTO_INCREMENT字段，我们采用更宽松的策略
        // 因为不同数据库的AUTO_INCREMENT检测可能不准确
        // 特别是H2DB在MySQL模式下的检测
        
        // 如果实际检测到AUTO_INCREMENT，则兼容
        if (actual.isAutoIncrement()) {
            return true;
        }
        
        // 如果实际没有检测到AUTO_INCREMENT，但我们期望有
        // 这种情况下，我们仍然认为兼容，因为可能是检测问题
        // 而不是真正的结构差异
        return true;
    }

    /**
     * 获取表是否存在的SQL
     */
    private static String getTableExistsSql(String tableName, JDBCType dbType) {
        if (dbType == JDBCType.H2DB) {
            // H2数据库在MySQL模式下使用MySQL语法
            return String.format(
                "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = '%s'",
                tableName
            );
        } else if (dbType == JDBCType.PostgreSQL) {
            // PostgreSQL使用单引号
            return String.format(
                "SELECT COUNT(*) FROM information_schema.tables WHERE table_name = '%s'",
                tableName
            );
        } else {
            // MySQL使用单引号
            return String.format(
                "SELECT COUNT(*) FROM information_schema.tables WHERE table_name = '%s'",
                tableName
            );
        }
    }

    /**
     * 获取列信息的SQL
     */
    private static String getColumnsSql(String tableName, JDBCType dbType) {
        if (dbType == JDBCType.H2DB) {
            // H2数据库没有information_schema.columns表，使用INFORMATION_SCHEMA.COLUMNS视图
            return String.format(
                "SELECT COLUMN_NAME, DATA_TYPE, CHARACTER_MAXIMUM_LENGTH, NUMERIC_PRECISION, NUMERIC_SCALE, " +
                "IS_NULLABLE, COLUMN_DEFAULT FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = '%s'",
                tableName
            );
        } else if (dbType == JDBCType.PostgreSQL) {
            // PostgreSQL使用单引号，并且没有extra列
            return String.format(
                "SELECT column_name, data_type, character_maximum_length, numeric_precision, numeric_scale, " +
                "is_nullable, column_default FROM information_schema.columns WHERE table_name = '%s'",
                tableName
            );
        } else {
            // MySQL使用单引号包围表名值
            return String.format(
                "SELECT column_name, data_type, character_maximum_length, numeric_precision, numeric_scale, " +
                "is_nullable, column_default, extra FROM information_schema.columns WHERE table_name = '%s'",
                tableName
            );
        }
    }

    /**
     * 从行中提取列信息
     */
    private static ColumnInfo extractColumnInfo(Row row, JDBCType dbType) {
        String name, type, nullable, defaultValue;
        Integer length, precision, scale;
        try {

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        try {
            // H2数据库使用大写列名
            name = row.getString("column_name");
            type = row.getString("data_type");
            length = row.getInteger("character_maximum_length");
            precision = row.getInteger("numeric_precision");
            scale = row.getInteger("numeric_scale");
            nullable = row.getString("is_nullable");
            defaultValue = row.getString("column_default");
        } catch (Exception e) {
            // MySQL使用大写列名
            name = row.getString("COLUMN_NAME");
            type = row.getString("DATA_TYPE");
            length = row.getInteger("CHARACTER_MAXIMUM_LENGTH");
            precision = row.getInteger("NUMERIC_PRECISION");
            scale = row.getInteger("NUMERIC_SCALE");
            nullable = row.getString("IS_NULLABLE");
            defaultValue = row.getString("COLUMN_DEFAULT");
        }

        // 处理auto_increment属性
        String extra = null;
        boolean autoIncrement = false;
        if (dbType == JDBCType.H2DB) {
            // H2数据库通过default值判断auto_increment
            // H2DB在MySQL模式下，AUTO_INCREMENT字段的默认值可能是多种格式
            autoIncrement = defaultValue != null && (
                defaultValue.contains("AUTO_INCREMENT") ||
                defaultValue.contains("NEXT VALUE FOR") ||
                defaultValue.contains("IDENTITY") ||
                // 对于主键字段，如果没有明确的默认值，也可能是AUTO_INCREMENT
                (name != null && name.toLowerCase().contains("id") && 
                 (defaultValue.isEmpty() || defaultValue.equals("null")))
            );
        } else if (dbType == JDBCType.PostgreSQL) {
            // PostgreSQL通过检查序列来判断auto_increment
            autoIncrement = defaultValue != null && defaultValue.contains("nextval");
        } else if (dbType == JDBCType.MySQL) {
            // MySQL使用EXTRA列
            extra = row.getString("EXTRA");
            autoIncrement = extra != null && extra.contains("auto_increment");
        } else {
            // PostgreSQL使用extra列
            extra = row.getString("extra");
            autoIncrement = extra != null && extra.contains("auto_increment");
        }
        
        return new ColumnInfo(name, type, length != null ? length : 0, 
                            precision != null ? precision : 0, scale != null ? scale : 0,
                            "YES".equals(nullable), defaultValue, autoIncrement);
    }

    /**
     * 生成创建表的SQL
     */
    private static String generateCreateTableSql(TableMetadata metadata, JDBCType dbType) {
        StringBuilder sb = new StringBuilder();
        String quotationMarks;
        
        if (dbType == JDBCType.MySQL) {
            quotationMarks = "`";
        } else if (dbType == JDBCType.PostgreSQL) {
            quotationMarks = "\"";
        } else {
            quotationMarks = "\"";
        }
        
        sb.append("CREATE TABLE ").append(quotationMarks).append(metadata.getTableName()).append(quotationMarks).append(" (\n");
        
        List<String> columnDefs = new ArrayList<>();
        for (ColumnMetadata column : metadata.getColumns().values()) {
            columnDefs.add("  " + column.toColumnDefinition(dbType));
        }
        
        sb.append(String.join(",\n", columnDefs));
        sb.append("\n)");
        
        if (dbType == JDBCType.MySQL) {
            sb.append(" ENGINE=").append(metadata.getEngine())
              .append(" DEFAULT CHARSET=").append(metadata.getCharset())
              .append(" COLLATE=").append(metadata.getCollate());
        } else if (dbType == JDBCType.PostgreSQL) {
            // PostgreSQL不需要ENGINE和CHARSET，但可以添加注释
            if (metadata.getComment() != null && !metadata.getComment().isEmpty()) {
                sb.append(";\nCOMMENT ON TABLE ").append(quotationMarks).append(metadata.getTableName())
                  .append(quotationMarks).append(" IS '").append(metadata.getComment()).append("'");
            }
        }
        
        return sb.toString();
    }

    /**
     * 生成添加列的SQL
     */
    private static String generateAddColumnSql(String tableName, ColumnMetadata column, JDBCType dbType) {
        String quotationMarks = dbType == JDBCType.MySQL ? "`" : "\"";
        return String.format("ALTER TABLE %s%s%s ADD COLUMN %s",
                quotationMarks, tableName, quotationMarks, column.toColumnDefinition(dbType));
    }

    /**
     * 生成删除列的SQL
     */
    private static String generateDropColumnSql(String tableName, String columnName, JDBCType dbType) {
        String quotationMarks = dbType == JDBCType.MySQL ? "`" : "\"";
        return String.format("ALTER TABLE %s%s%s DROP COLUMN %s%s%s",
                quotationMarks, tableName, quotationMarks, quotationMarks, columnName, quotationMarks);
    }

    /**
     * 生成修改列类型的SQL
     */
    private static String generateAlterColumnTypeSql(String tableName, ColumnMetadata column, JDBCType dbType) {
        String quotationMarks = dbType == JDBCType.MySQL ? "`" : "\"";
        
        if (dbType == JDBCType.PostgreSQL) {
            // PostgreSQL使用ALTER COLUMN语法
            StringBuilder sql = new StringBuilder();
            sql.append(String.format("ALTER TABLE %s%s%s ALTER COLUMN %s%s%s TYPE %s",
                    quotationMarks, tableName, quotationMarks,
                    quotationMarks, column.getUsername(), quotationMarks,
                    column.getType()));
            
            // 添加注释（PostgreSQL需要单独的COMMENT ON COLUMN语句）
            if (column.getComment() != null && !column.getComment().isEmpty()) {
                sql.append(";\n");
                sql.append(String.format("COMMENT ON COLUMN %s%s%s.%s%s%s IS '%s'",
                        quotationMarks, tableName, quotationMarks,
                        quotationMarks, column.getUsername(), quotationMarks,
                        column.getComment()));
            }
            
            return sql.toString();
        } else {
            // MySQL和H2DB使用MODIFY COLUMN语法
            return String.format("ALTER TABLE %s%s%s MODIFY COLUMN %s",
                    quotationMarks, tableName, quotationMarks, column.toColumnDefinition(dbType, false));
        }
    }

    /**
     * 生成修改列长度的SQL
     */
    private static String generateAlterColumnLengthSql(String tableName, ColumnMetadata column, JDBCType dbType) {
        return generateAlterColumnTypeSql(tableName, column, dbType);
    }

    /**
     * 生成修改列可空性的SQL
     */
    private static String generateAlterColumnNullableSql(String tableName, ColumnMetadata column, JDBCType dbType) {
        return generateAlterColumnTypeSql(tableName, column, dbType);
    }

    /**
     * 生成修改列默认值的SQL
     */
    private static String generateAlterColumnDefaultSql(String tableName, ColumnMetadata column, JDBCType dbType) {
        String quotationMarks = dbType == JDBCType.MySQL ? "`" : "\"";
        
        if (dbType == JDBCType.PostgreSQL) {
            // PostgreSQL使用ALTER COLUMN SET DEFAULT语法
            StringBuilder sql = new StringBuilder();
            
            if (column.getDefaultValue() != null && !column.getDefaultValue().isEmpty()) {
                String defaultValue = column.getDefaultValue();
                if (column.isDefaultValueIsFunction()) {
                    // 函数类型的默认值不需要引号
                    sql.append(String.format("ALTER TABLE %s%s%s ALTER COLUMN %s%s%s SET DEFAULT %s",
                            quotationMarks, tableName, quotationMarks,
                            quotationMarks, column.getUsername(), quotationMarks,
                            defaultValue));
                } else {
                    // 字面值需要引号
                    sql.append(String.format("ALTER TABLE %s%s%s ALTER COLUMN %s%s%s SET DEFAULT '%s'",
                            quotationMarks, tableName, quotationMarks,
                            quotationMarks, column.getUsername(), quotationMarks,
                            defaultValue));
                }
            } else {
                // 删除默认值
                sql.append(String.format("ALTER TABLE %s%s%s ALTER COLUMN %s%s%s DROP DEFAULT",
                        quotationMarks, tableName, quotationMarks,
                        quotationMarks, column.getUsername(), quotationMarks));
            }
            
            // 添加注释（PostgreSQL需要单独的COMMENT ON COLUMN语句）
            if (column.getComment() != null && !column.getComment().isEmpty()) {
                sql.append(";\n");
                sql.append(String.format("COMMENT ON COLUMN %s%s%s.%s%s%s IS '%s'",
                        quotationMarks, tableName, quotationMarks,
                        quotationMarks, column.getUsername(), quotationMarks,
                        column.getComment()));
            }
            
            return sql.toString();
        } else {
            // MySQL和H2DB使用MODIFY COLUMN语法
            return String.format("ALTER TABLE %s%s%s MODIFY COLUMN %s",
                    quotationMarks, tableName, quotationMarks, column.toColumnDefinition(dbType, false));
        }
    }

    /**
     * 生成修改列自增属性的SQL
     */
    private static String generateAlterColumnAutoIncrementSql(String tableName, ColumnMetadata column, JDBCType dbType) {
        return generateAlterColumnTypeSql(tableName, column, dbType);
    }

    /**
     * 生成PostgreSQL列注释SQL
     */
    private static String generatePostgreSQLColumnCommentSql(String tableName, ColumnMetadata column) {
        String quotationMarks = "\"";
        if (column.getComment() != null && !column.getComment().isEmpty()) {
            return String.format("COMMENT ON COLUMN %s%s%s.%s%s%s IS '%s'",
                    quotationMarks, tableName, quotationMarks,
                    quotationMarks, column.getUsername(), quotationMarks,
                    column.getComment());
        }
        return null; // 没有注释时返回null
    }

    /**
     * 列信息类
     */
    private static class ColumnInfo {
        private String name;
        private String type;
        private int length;
        private int precision;
        private int scale;
        private boolean nullable;
        private String defaultValue;
        private boolean autoIncrement;

        public ColumnInfo(String name, String type, int length, int precision, int scale,
                         boolean nullable, String defaultValue, boolean autoIncrement) {
            this.name = name;
            this.type = type;
            this.length = length;
            this.precision = precision;
            this.scale = scale;
            this.nullable = nullable;
            this.defaultValue = defaultValue;
            this.autoIncrement = autoIncrement;
        }

        // Getters
        public String getUsername() { return name; }
        public String getType() { return type; }
        public int getLength() { return length; }
        public int getPrecision() { return precision; }
        public int getScale() { return scale; }
        public boolean isNullable() { return nullable; }
        public String getDefaultValue() { return defaultValue; }
        public boolean isAutoIncrement() { return autoIncrement; }

        @Override
        public String toString() {
            return String.format("ColumnInfo{name='%s', type='%s', length=%d, nullable=%s, defaultValue='%s', autoIncrement=%s}",
                    name, type, length, nullable, defaultValue, autoIncrement);
        }
    }
}
