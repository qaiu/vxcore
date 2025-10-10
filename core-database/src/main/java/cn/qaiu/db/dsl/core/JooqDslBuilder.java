package cn.qaiu.db.dsl.core;

import cn.qaiu.db.ddl.DdlTable;
import io.vertx.core.json.JsonObject;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 简化的 jOOQ DSL SQL 构建器
 * 使用正确的 jOOQ API，避免复杂的类型推断问题
 */
public class JooqDslBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(JooqDslBuilder.class);

    private final DSLContext dslContext;

    public JooqDslBuilder(DSLContext dslContext) {
        this.dslContext = dslContext;
    }

    /**
     * 构建 INSERT 查询 - 使用简化的 jOOQ DSL
     */
    public Query buildInsert(String tableName, JsonObject data) {
        List<String> columns = new ArrayList<>();
        List<Object> values = new ArrayList<>();
        
        // 动态处理所有字段（跳过id主键）
        for (String key : data.fieldNames()) {
            if (!"id".equals(key)) {
                columns.add(key);
                values.add(convertJsonValue(data.getValue(key)));
            }
        }
        
        // 构建 SQL 字符串
        String sql = String.format("INSERT INTO %s (%s) VALUES (%s)", 
                tableName, 
                String.join(", ", columns),
                String.join(", ", columns.stream().map(c -> "?").toArray(String[]::new)));
        
        LOGGER.debug("Generated INSERT SQL: {}", sql);
        LOGGER.debug("Values: {}", values);
        
        return DSL.query(sql, values.toArray());
    }

    /**
     * 构建 UPDATE 查询 - 使用简化的 jOOQ DSL
     */
    public Query buildUpdate(String tableName, JsonObject data, Condition whereCondition) {
        List<String> setClauses = new ArrayList<>();
        List<Object> values = new ArrayList<>();
        
        // 添加数据字段
        for (String key : data.fieldNames()) {
            if (!"id".equals(key) && !"create_time".equals(key)) {
                Object value = convertJsonValue(data.getValue(key));
                if (value != null) {
                    String fieldName = cn.qaiu.vx.core.util.StringCase.toUnderlineCase(key);
                    setClauses.add(fieldName + " = ?");
                    values.add(value);
                }
            }
        }
        
        // 设置更新时间（如果还没有设置的话）
        if (!data.containsKey("update_time") && !data.containsKey("updateTime")) {
            setClauses.add("update_time = ?");
            values.add(LocalDateTime.now());
        }
        
        // 构建 WHERE 条件字符串
        String whereClause = buildConditionString(whereCondition);
        
        String sql = String.format("UPDATE %s SET %s WHERE %s", 
                tableName, 
                String.join(", ", setClauses),
                whereClause);
        
        return DSL.query(sql, values.toArray());
    }

    /**
     * 构建 SELECT 查询 - 使用简化的 jOOQ DSL
     */
    public Query buildSelect(String tableName, Condition condition) {
        if (condition != null) {
            // 使用jOOQ的DSL来构建查询，这样可以正确处理参数绑定
            return dslContext.selectFrom(DSL.table(tableName))
                    .where(condition);
        } else {
            return dslContext.selectFrom(DSL.table(tableName));
        }
    }

    /**
     * 构建 COUNT 查询 - 使用简化的 jOOQ DSL
     */
    public Query buildCount(String tableName, Condition condition) {
        if (condition != null) {
            // 使用jOOQ的DSL来构建查询，这样可以正确处理参数绑定
            return dslContext.selectCount()
                    .from(DSL.table(tableName))
                    .where(condition);
        } else {
            return dslContext.selectCount()
                    .from(DSL.table(tableName));
        }
    }

    /**
     * 构建 DELETE 查询 - 使用简化的 jOOQ DSL
     */
    public Query buildDelete(String tableName, Condition condition) {
        // 使用jOOQ的DSL来构建查询，这样可以正确处理参数绑定
        return dslContext.deleteFrom(DSL.table(tableName))
                .where(condition);
    }

    /**
     * 构建分页查询 - 使用简化的 jOOQ DSL
     */
    public Query buildSelectWithPagination(String tableName, Condition condition, 
                                                String orderBy, boolean ascending, 
                                                int offset, int limit) {
        StringBuilder sql = new StringBuilder("SELECT * FROM ").append(tableName);
        
        if (condition != null) {
            String whereClause = buildConditionString(condition);
            sql.append(" WHERE ").append(whereClause);
        }
        
        // 添加排序
        if (orderBy != null && !orderBy.isEmpty()) {
            String orderField = cn.qaiu.vx.core.util.StringCase.toUnderlineCase(orderBy);
            sql.append(" ORDER BY ").append(orderField);
            sql.append(ascending ? " ASC" : " DESC");
        }
        
        // 添加分页
        sql.append(" OFFSET ").append(offset).append(" LIMIT ").append(limit);
        
        return DSL.query(sql.toString());
    }

    /**
     * 构建批量 INSERT 查询 - 使用简化的 jOOQ DSL
     */
    public Query buildBatchInsert(String tableName, List<JsonObject> dataList) {
        if (dataList == null || dataList.isEmpty()) {
            throw new IllegalArgumentException("Data list cannot be empty");
        }
        
        // 从第一个记录获取字段结构
        JsonObject firstData = dataList.get(0);
        List<String> columns = new ArrayList<>();
        
        // 收集字段
        for (String key : firstData.fieldNames()) {
            columns.add(cn.qaiu.vx.core.util.StringCase.toUnderlineCase(key));
        }
        
        // 添加时间戳字段
        columns.add("create_time");
        columns.add("update_time");
        
        // 构建 VALUES 子句
        List<String> valueClauses = new ArrayList<>();
        List<Object> allValues = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        
        for (JsonObject data : dataList) {
            List<Object> values = new ArrayList<>();
            
            // 收集所有字段的值
            for (String key : firstData.fieldNames()) {
                Object value = convertJsonValue(data.getValue(key));
                values.add(value);
            }
            
            // 添加时间戳
            values.add(now); // create_time
            values.add(now); // update_time
            
            valueClauses.add("(" + String.join(", ", values.stream().map(v -> "?").toArray(String[]::new)) + ")");
            allValues.addAll(values);
        }
        
        // 构建批量 INSERT SQL
        String sql = String.format("INSERT INTO %s (%s) VALUES %s", 
                tableName, 
                String.join(", ", columns),
                String.join(", ", valueClauses));
        
        return DSL.query(sql, allValues.toArray());
    }

    /**
     * 构建 EXISTS 查询 - 使用简化的 jOOQ DSL
     */
    public Query buildExists(String tableName, Condition condition) {
        String whereClause = buildConditionString(condition);
        String sql = String.format("SELECT EXISTS(SELECT 1 FROM %s WHERE %s) as exists", 
                tableName, whereClause);
        return DSL.query(sql);
    }

    /**
     * 构建 IN 查询条件
     */
    public Condition buildInCondition(String fieldName, List<?> values) {
        Field<Object> field = DSL.field(cn.qaiu.vx.core.util.StringCase.toUnderlineCase(fieldName));
        return field.in(values.toArray());
    }

    /**
     * 构建 LIKE 查询条件
     */
    public Condition buildLikeCondition(String fieldName, String pattern) {
        Field<String> field = DSL.field(cn.qaiu.vx.core.util.StringCase.toUnderlineCase(fieldName), String.class);
        return field.like(pattern);
    }

    /**
     * 构建 BETWEEN 查询条件
     */
    public Condition buildBetweenCondition(String fieldName, Object minValue, Object maxValue) {
        Field<Object> field = DSL.field(cn.qaiu.vx.core.util.StringCase.toUnderlineCase(fieldName));
        return field.between(minValue, maxValue);
    }

    /**
     * 构建比较查询条件
     */
    public Condition buildComparisonCondition(String fieldName, String operator, Object value) {
        Field<Object> field = DSL.field(cn.qaiu.vx.core.util.StringCase.toUnderlineCase(fieldName));
        
        switch (operator.toUpperCase()) {
            case "EQ":
            case "=":
                return field.eq(value);
            case "NE":
            case "!=":
                return field.ne(value);
            case "GT":
            case ">":
                return field.gt(value);
            case "GE":
            case ">=":
                return field.ge(value);
            case "LT":
            case "<":
                return field.lt(value);
            case "LE":
            case "<=":
                return field.le(value);
            case "IS_NULL":
                return field.isNull();
            case "IS_NOT_NULL":
                return field.isNotNull();
            default:
                throw new IllegalArgumentException("Unsupported operator: " + operator);
        }
    }

    /**
     * 构建复合条件
     */
    public Condition buildCompoundCondition(List<Condition> conditions, boolean useAnd) {
        if (conditions == null || conditions.isEmpty()) {
            return DSL.noCondition();
        }
        
        Condition result = conditions.get(0);
        for (int i = 1; i < conditions.size(); i++) {
            if (useAnd) {
                result = result.and(conditions.get(i));
            } else {
                result = result.or(conditions.get(i));
            }
        }
        
        return result;
    }

    /**
     * 基于实体类注解获取表名
     */
    public String getTableName(Class<?> entityClass) {
        DdlTable ddlTable = entityClass.getAnnotation(DdlTable.class);
        if (ddlTable != null && !ddlTable.value().isEmpty()) {
            return ddlTable.value();
        }
        
        // 默认使用类名的驼峰转下划线（符合数据库表命名规范）
        return cn.qaiu.db.dsl.core.FieldNameConverter.toDatabaseTableName(entityClass.getSimpleName());
    }

    /**
     * 基于实体类注解获取主键字段名
     */
    public String getPrimaryKey(Class<?> entityClass) {
        DdlTable ddlTable = entityClass.getAnnotation(DdlTable.class);
        if (ddlTable != null && !ddlTable.keyFields().isEmpty()) {
            return ddlTable.keyFields();
        }
        
        return "id";
    }

    /**
     * 将 JsonObject 值转换为数据库兼容的值
     */
    private Object convertJsonValue(Object jsonValue) {
        return EnhancedTypeMapper.convertToDatabaseValue(jsonValue);
    }

    /**
     * 构建条件字符串 - 简化实现
     */
    private String buildConditionString(Condition condition) {
        // 对于简单条件，直接转换为字符串
        // 这里假设condition是一个简单的Field=Value条件
        return condition.toString();
    }


    /**
     * 获取 DSLContext
     */
    public DSLContext dsl() {
        return dslContext;
    }
}