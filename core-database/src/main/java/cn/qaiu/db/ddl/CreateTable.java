package cn.qaiu.db.ddl;

import cn.qaiu.db.pool.JDBCType;
import cn.qaiu.vx.core.util.ReflectionUtil;
import io.vertx.codegen.format.CamelCase;
import io.vertx.codegen.format.Case;
import io.vertx.codegen.format.LowerCamelCase;
import io.vertx.codegen.format.SnakeCase;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.templates.annotations.Column;
import io.vertx.sqlclient.templates.annotations.RowMapped;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.*;

/**
 * 创建表
 *
 * @author <a href="https://qaiu.top">QAIU</a>
 */
public class CreateTable {
    public static Map<Class<?>, String> javaProperty2SqlColumnMap = new HashMap<>() {{
        // Java类型到SQL类型的映射
        put(Integer.class, "INT");
        put(Short.class, "SMALLINT");
        put(Byte.class, "TINYINT");
        put(Long.class, "BIGINT");
        put(java.math.BigDecimal.class, "DECIMAL");
        put(Double.class, "DOUBLE");
        put(Float.class, "REAL");
        put(Boolean.class, "BOOLEAN");
        put(String.class, "VARCHAR");
        put(Date.class, "TIMESTAMP");
        put(java.time.LocalDateTime.class, "TIMESTAMP");
        put(java.sql.Timestamp.class, "TIMESTAMP");
        put(java.sql.Date.class, "DATE");
        put(java.sql.Time.class, "TIME");

        // 基本数据类型
        put(int.class, "INT");
        put(short.class, "SMALLINT");
        put(byte.class, "TINYINT");
        put(long.class, "BIGINT");
        put(double.class, "DOUBLE");
        put(float.class, "REAL");
        put(boolean.class, "BOOLEAN");
    }};

    private static final Logger LOGGER = LoggerFactory.getLogger(CreateTable.class);
    public static String UNIQUE_PREFIX = "idx_";

    private static Case getCase(Class<?> clz) {
        return switch (clz.getUsername()) {
            case "io.vertx.codegen.format.CamelCase" -> CamelCase.INSTANCE;
            case "io.vertx.codegen.format.SnakeCase" -> SnakeCase.INSTANCE;
            case "io.vertx.codegen.format.LowerCamelCase" -> LowerCamelCase.INSTANCE;
            default -> throw new UnsupportedOperationException();
        };
    }

    public static List<String> getCreateTableSQL(Class<?> clz, JDBCType type) {
        // 获取表名和主键
        TableInfo tableInfo = extractTableInfo(clz, type);

        // 构建表的SQL语句
        List<String> sqlList = new ArrayList<>();
        StringBuilder sb = new StringBuilder(50);
        sb.append("CREATE TABLE IF NOT EXISTS ")
                .append(tableInfo.quotationMarks).append(tableInfo.tableName).append(tableInfo.quotationMarks)
                .append(" ( \r\n ");

        // 处理字段并生成列定义
        List<String> indexSQLs = new ArrayList<>();
        processFields(clz, tableInfo, sb, indexSQLs);

        // 去掉最后一个逗号并添加表尾部信息
        try {
            String tableSQL = sb.substring(0, sb.lastIndexOf(",")) + tableInfo.endStr;
            sqlList.add(tableSQL);

            // 添加索引SQL
            sqlList.addAll(indexSQLs);
            return sqlList;
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate CREATE TABLE SQL for class: "
                    + clz.getUsername() + ", error String: '" + sb + "'", e);
        }

    }


    // 修改extractTableInfo方法，处理没有Table注解时默认使用id字段作为主键
    private static TableInfo extractTableInfo(Class<?> clz, JDBCType type) {
        String quotationMarks;
        String endStr;
        if (type == JDBCType.MySQL) {
            quotationMarks = "`";
            endStr = ")ENGINE=InnoDB DEFAULT CHARSET=utf8;";
        } else {
            quotationMarks = "\"";
            endStr = ");";
        }

        String primaryKey = null;
        String tableName = null;
        Case caseFormat = SnakeCase.INSTANCE;

        // 判断类上是否有RowMapped注解
        if (clz.isAnnotationPresent(RowMapped.class)) {
            RowMapped annotation = clz.getAnnotation(RowMapped.class);
            caseFormat = getCase(annotation.formatter());
        }

        // 判断类上是否有Table注解
        if (clz.isAnnotationPresent(Table.class)) {
            Table annotation = clz.getAnnotation(Table.class);
            tableName = StringUtils.isNotEmpty(annotation.value())
                    ? annotation.value()
                    : LowerCamelCase.INSTANCE.to(caseFormat, clz.getSimpleName());
            primaryKey = annotation.keyFields();
        }
        
        // 判断类上是否有DdlTable注解
        if (clz.isAnnotationPresent(DdlTable.class)) {
            DdlTable annotation = clz.getAnnotation(DdlTable.class);
            tableName = StringUtils.isNotEmpty(annotation.value())
                    ? annotation.value()
                    : LowerCamelCase.INSTANCE.to(caseFormat, clz.getSimpleName());
            primaryKey = annotation.keyFields();
        }

        // 如果表名仍为null，使用类名转下划线命名作为表名
        if (StringUtils.isEmpty(tableName)) {
            tableName = LowerCamelCase.INSTANCE.to(SnakeCase.INSTANCE, clz.getSimpleName());
        }

        // 如果主键为空，默认使用id字段作为主键
        if (StringUtils.isEmpty(primaryKey)) {
            try {
                clz.getDeclaredField("id");
                primaryKey = "id";
            } catch (NoSuchFieldException e) {
                // 如果没有id字段，不设置主键
                primaryKey = null;
            }
        }

        return new TableInfo(tableName, quotationMarks, endStr, primaryKey, caseFormat, type);
    }

    // 修改processFields方法，处理索引
    private static void processFields(Class<?> clz, TableInfo tableInfo, StringBuilder sb, List<String> indexSQLs) {
        Field[] fields = getFieldsInDeclarationOrder(clz);
        boolean hasValidFields = false;
        
        for (Field field : fields) {
            // 跳过无效字段
            if (isIgnoredField(field)) {
                continue;
            }

            // 获取字段名和SQL类型
            String column = LowerCamelCase.INSTANCE.to(tableInfo.caseFormat, field.getUsername());
            String sqlType = javaProperty2SqlColumnMap.get(field.getType());

            // 处理字段注解
            column = processColumnAnnotation(field, column);
            int[] decimalSize = {22, 2};
            int varcharSize = 255;
            if (field.isAnnotationPresent(Length.class)) {
                Length length = field.getAnnotation(Length.class);
                decimalSize = length.decimalSize();
                varcharSize = length.varcharSize();
            }
            
            // 处理DdlColumn注解
            if (field.isAnnotationPresent(DdlColumn.class)) {
                DdlColumn ddlColumn = field.getAnnotation(DdlColumn.class);
                if (StringUtils.isNotBlank(ddlColumn.name())) {
                    column = ddlColumn.name();
                }
                if (StringUtils.isNotBlank(ddlColumn.type())) {
                    sqlType = ddlColumn.type();
                }
                if (ddlColumn.length() > 0) {
                    varcharSize = ddlColumn.length();
                }
                if (ddlColumn.precision() > 0) {
                    decimalSize[0] = ddlColumn.precision();
                }
                if (ddlColumn.scale() > 0) {
                    decimalSize[1] = ddlColumn.scale();
                }
            }

            // 构建列定义
            sb.append(tableInfo.quotationMarks).append(column).append(tableInfo.quotationMarks)
                    .append(" ").append(sqlType);
            appendTypeLength(sqlType, sb, decimalSize, varcharSize);
            appendConstraints(field, sb, tableInfo);
            appendPrimaryKey(tableInfo.primaryKey, column, sb);

            // 添加索引
            appendIndex(tableInfo, indexSQLs, field);

            sb.append(",\n ");
            hasValidFields = true;
        }
        
        // 如果没有有效字段，抛出异常
        if (!hasValidFields) {
            throw new RuntimeException("实体类 " + clz.getUsername() + " 没有有效的字段，无法创建表");
        }
    }

    // 获取按声明顺序排列的字段
    private static Field[] getFieldsInDeclarationOrder(Class<?> clz) {
        Field[] fields = clz.getDeclaredFields();
        // 按照字段名排序，这样可以保持相对一致的顺序
        // 注意：Java反射不保证字段的声明顺序，但我们可以通过排序来获得可预测的顺序
        Arrays.sort(fields, (f1, f2) -> {
            // 优先处理主键字段
            if ("id".equals(f1.getUsername()) && !"id".equals(f2.getUsername())) {
                return -1;
            }
            if (!"id".equals(f1.getUsername()) && "id".equals(f2.getUsername())) {
                return 1;
            }
            // 其他字段按名称排序
            return f1.getUsername().compareTo(f2.getUsername());
        });
        return fields;
    }

    // 判断是否忽略字段
    private static boolean isIgnoredField(Field field) {
        return field.getUsername().equals("serialVersionUID")
                || StringUtils.isEmpty(javaProperty2SqlColumnMap.get(field.getType()))
                || field.isAnnotationPresent(TableGenIgnore.class)
                || field.isAnnotationPresent(DdlIgnore.class);
    }

    // 处理Column注解
    private static String processColumnAnnotation(Field field, String column) {
        if (field.isAnnotationPresent(Column.class)) {
            Column columnAnnotation = field.getAnnotation(Column.class);
            if (StringUtils.isNotBlank(columnAnnotation.name())) {
                column = columnAnnotation.name();
            }
        }
        return column;
    }

    // 添加类型长度
    private static void appendTypeLength(String sqlType, StringBuilder sb, int[] decimalSize, int varcharSize) {
        if ("DECIMAL".equals(sqlType)) {
            sb.append("(").append(decimalSize[0]).append(",").append(decimalSize[1]).append(")");
        } else if ("VARCHAR".equals(sqlType)) {
            sb.append("(").append(varcharSize).append(")");
        }
    }

    // 添加约束
    private static void appendConstraints(Field field, StringBuilder sb, TableInfo tableInfo) {
        JDBCType type = tableInfo.dbType;

        if (field.isAnnotationPresent(Constraint.class)) {
            Constraint constraint = field.getAnnotation(Constraint.class);
            
            // 处理PostgreSQL的SERIAL类型
            if (constraint.autoIncrement() && type == JDBCType.PostgreSQL) {
                // 需要移除字段类型(最后一个单词)和NOT NULL
                if (field.getType().equals(Integer.class)) {
                    // 移除类型和NOT NULL
                    String currentSb = sb.toString();
                    if (currentSb.endsWith(" NOT NULL")) {
                        sb.delete(sb.lastIndexOf(" NOT NULL"), sb.length());
                    }
                    if (currentSb.endsWith(" INT")) {
                        sb.delete(sb.lastIndexOf(" INT"), sb.length());
                    }
                    sb.append(" SERIAL");
                } else if (field.getType().equals(Long.class)) {
                    // 移除类型和NOT NULL
                    String currentSb = sb.toString();
                    if (currentSb.endsWith(" NOT NULL")) {
                        sb.delete(sb.lastIndexOf(" NOT NULL"), sb.length());
                    }
                    if (currentSb.endsWith(" BIGINT")) {
                        sb.delete(sb.lastIndexOf(" BIGINT"), sb.length());
                    }
                    sb.append(" BIGSERIAL");
                }
            } else {
                // 非PostgreSQL或非自增字段的处理
                if (constraint.notNull()) {
                    sb.append(" NOT NULL");
                }
                String apostrophe = constraint.defaultValueIsFunction() ? "" : "'";
                if (StringUtils.isNotEmpty(constraint.defaultValue())) {
                    sb.append(" DEFAULT ").append(apostrophe).append(constraint.defaultValue()).append(apostrophe);
                }
                if (constraint.autoIncrement() && (field.getType().equals(Integer.class) || field.getType().equals(Long.class))) {
                    sb.append(" AUTO_INCREMENT");
                }
            }
        }
        
        // 处理DdlColumn注解
        if (field.isAnnotationPresent(DdlColumn.class)) {
            DdlColumn ddlColumn = field.getAnnotation(DdlColumn.class);
            
            // 处理PostgreSQL的SERIAL类型
            if (ddlColumn.autoIncrement() && type == JDBCType.PostgreSQL) {
                // 需要移除字段类型(最后一个单词)和NOT NULL
                if (field.getType().equals(Integer.class) || "INT".equals(ddlColumn.type())) {
                    // 移除类型和NOT NULL
                    String currentSb = sb.toString();
                    if (currentSb.endsWith(" NOT NULL")) {
                        sb.delete(sb.lastIndexOf(" NOT NULL"), sb.length());
                    }
                    if (currentSb.endsWith(" INT")) {
                        sb.delete(sb.lastIndexOf(" INT"), sb.length());
                    }
                    sb.append(" SERIAL");
                } else if (field.getType().equals(Long.class) || "BIGINT".equals(ddlColumn.type())) {
                    // 移除类型和NOT NULL
                    String currentSb = sb.toString();
                    if (currentSb.endsWith(" NOT NULL")) {
                        sb.delete(sb.lastIndexOf(" NOT NULL"), sb.length());
                    }
                    if (currentSb.endsWith(" BIGINT")) {
                        sb.delete(sb.lastIndexOf(" BIGINT"), sb.length());
                    }
                    sb.append(" BIGSERIAL");
                }
            } else {
                // 非PostgreSQL或非自增字段的处理
                if (!ddlColumn.nullable()) {
                    sb.append(" NOT NULL");
                }
                String apostrophe = ddlColumn.defaultValueIsFunction() ? "" : "'";
                if (StringUtils.isNotEmpty(ddlColumn.defaultValue())) {
                    sb.append(" DEFAULT ").append(apostrophe).append(ddlColumn.defaultValue()).append(apostrophe);
                }
                if (ddlColumn.autoIncrement() && type != JDBCType.PostgreSQL) {
                    sb.append(" AUTO_INCREMENT");
                }
            }
        }
    }

    // 添加主键
    private static void appendPrimaryKey(String primaryKey, String column, StringBuilder sb) {
        if (StringUtils.isEmpty(primaryKey)) {
            return;
        }
        if (primaryKey.equalsIgnoreCase(column)) {
            sb.append(" PRIMARY KEY");
        }
    }

    private static void appendIndex(TableInfo tableInfo, List<String> indexSQLs, Field field) {
        if (!field.isAnnotationPresent(Constraint.class)) {
            return;
        }

        Constraint constraint = field.getAnnotation(Constraint.class);
        if (StringUtils.isEmpty(constraint.uniqueKey())) {
            return;
        }

        // 将字段名转换为下划线命名法
        String columnName = LowerCamelCase.INSTANCE.to(SnakeCase.INSTANCE, field.getUsername());
        String indexName = UNIQUE_PREFIX + tableInfo.tableName + "_" + constraint.uniqueKey();

        // 检查是否已有相同索引名称的索引
        Optional<String> existingIndex = indexSQLs.stream()
                .filter(sql -> sql.contains(tableInfo.quotationMarks + indexName + tableInfo.quotationMarks))
                .findFirst();

        if (existingIndex.isPresent()) {
            // 如果存在相同索引名称，追加字段到索引定义中
            String updatedIndex = existingIndex.get().replaceFirst(
                    "\\(([^)]+)\\)", // 匹配索引字段列表
                    "($1, " + tableInfo.quotationMarks + columnName + tableInfo.quotationMarks + ")"
            );
            indexSQLs.remove(existingIndex.get());
            indexSQLs.add(updatedIndex);
        } else {
            // 如果不存在相同索引名称，创建新的索引
            String indexSQL = String.format(
                    "CREATE UNIQUE INDEX %s %s%s%s ON %s%s%s (%s%s%s);",
                    tableInfo.dbType == JDBCType.MySQL ? "" : "IF NOT EXISTS",
                    tableInfo.quotationMarks, indexName, tableInfo.quotationMarks,
                    tableInfo.quotationMarks, tableInfo.tableName, tableInfo.quotationMarks,
                    tableInfo.quotationMarks, columnName, tableInfo.quotationMarks
            );
            indexSQLs.add(indexSQL);
        }
    }

    // 表信息类
    private record TableInfo(
            String tableName,        // 表名
            String quotationMarks,   // 引号或反引号
            String endStr,           // 表尾部信息
            String primaryKey,       // 主键字段
            Case caseFormat,         // 命名格式
            JDBCType dbType          // 数据库类型
    ) {
    }

    public static Future<Void> createTable(Pool pool, JDBCType type) {
        Promise<Void> promise = Promise.promise();
        Set<Class<?>> tableClasses = ReflectionUtil.getReflections().getTypesAnnotatedWith(Table.class);
        Set<Class<?>> ddlTableClasses = ReflectionUtil.getReflections().getTypesAnnotatedWith(DdlTable.class);

        // 合并两个集合
        Set<Class<?>> allTableClasses = new HashSet<>();
        allTableClasses.addAll(tableClasses);
        allTableClasses.addAll(ddlTableClasses);

        if (allTableClasses.isEmpty()) {
            LOGGER.warn("Table model class not found");
            promise.complete();
            return promise.future();
        }

        List<Future<Object>> futures = new ArrayList<>();

        for (Class<?> clazz : allTableClasses) {
            try {
                List<String> sqlList = getCreateTableSQL(clazz, type);
                LOGGER.info("Class `{}` auto-generate table", clazz.getUsername());

                for (String sql : sqlList) {
                    try {
                        pool.query(sql).execute().result();
                        futures.add(Future.succeededFuture());
                        LOGGER.debug("Executed SQL:\n{}", sql);
                    } catch (Exception e) {
                        String message = e.getMessage();
                        if (message != null && message.contains("Duplicate key name")) {
                            LOGGER.warn("Ignoring duplicate key error: {}", message);
                            futures.add(Future.succeededFuture());
                        } else {
                            LOGGER.error("SQL Error: {}\nSQL: {}", message, sql);
                            futures.add(Future.failedFuture(e));
                            throw new RuntimeException(e); // Stop execution for other exceptions
                        }
                    }
                }
            } catch (RuntimeException e) {
                // 处理空实体类的情况
                if (e.getMessage() != null && e.getMessage().contains("没有有效的字段")) {
                    LOGGER.warn("跳过空实体类: {} - {}", clazz.getUsername(), e.getMessage());
                    futures.add(Future.succeededFuture()); // 继续处理其他实体类
                } else {
                    // 其他异常继续抛出
                    LOGGER.error("处理实体类 {} 时发生错误: {}", clazz.getUsername(), e.getMessage());
                    futures.add(Future.failedFuture(e));
                    throw e;
                }
            }
        }

        Future.all(futures).onSuccess(r -> promise.complete()).onFailure(promise::fail);
        return promise.future();
    }

}
