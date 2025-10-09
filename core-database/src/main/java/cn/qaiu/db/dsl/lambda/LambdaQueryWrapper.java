package cn.qaiu.db.dsl.lambda;

import org.jooq.*;
import org.jooq.impl.DSL;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

/**
 * Lambda查询包装器 - 仿照MyBatis-Plus实现
 * 支持实体类::字段格式的Lambda表达式查询
 * 
 * @param <T> 实体类型
 * @author qaiu
 */
public class LambdaQueryWrapper<T> {
    
    private final DSLContext dslContext;
    private final Table<?> table;
    private final Class<T> entityClass;
    private final List<Condition> conditions;
    private final List<SortField<?>> orderByFields;
    private final List<Field<?>> selectFields;
    private Long limitCount;
    private Long offsetCount;
    
    public LambdaQueryWrapper(DSLContext dslContext, Table<?> table, Class<T> entityClass) {
        this.dslContext = dslContext;
        this.table = table;
        this.entityClass = entityClass;
        this.conditions = new ArrayList<>();
        this.orderByFields = new ArrayList<>();
        this.selectFields = new ArrayList<>();
    }
    
    // =================== 条件构建方法 ===================
    
    /**
     * 等于条件
     */
    public <R> LambdaQueryWrapper<T> eq(SFunction<T, R> column, R value) {
        if (value != null) {
            String fieldName = getDatabaseFieldName(column);
            Object dbValue = convertValueForDatabase(value);
            conditions.add(DSL.field(fieldName).eq(dbValue));
        }
        return this;
    }
    
    /**
     * 不等于条件
     */
    public <R> LambdaQueryWrapper<T> ne(SFunction<T, R> column, R value) {
        if (value != null) {
            String fieldName = getDatabaseFieldName(column);
            Object dbValue = convertValueForDatabase(value);
            conditions.add(DSL.field(fieldName).ne(dbValue));
        }
        return this;
    }
    
    /**
     * 大于条件
     */
    public <R> LambdaQueryWrapper<T> gt(SFunction<T, R> column, R value) {
        if (value != null) {
            String fieldName = getDatabaseFieldName(column);
            Object dbValue = convertValueForDatabase(value);
            conditions.add(DSL.field(fieldName).gt(dbValue));
        }
        return this;
    }
    
    /**
     * 大于等于条件
     */
    public <R> LambdaQueryWrapper<T> ge(SFunction<T, R> column, R value) {
        if (value != null) {
            String fieldName = getDatabaseFieldName(column);
            Object dbValue = convertValueForDatabase(value);
            conditions.add(DSL.field(fieldName).ge(dbValue));
        }
        return this;
    }
    
    /**
     * 小于条件
     */
    public <R> LambdaQueryWrapper<T> lt(SFunction<T, R> column, R value) {
        if (value != null) {
            String fieldName = getDatabaseFieldName(column);
            Object dbValue = convertValueForDatabase(value);
            conditions.add(DSL.field(fieldName).lt(dbValue));
        }
        return this;
    }
    
    /**
     * 小于等于条件
     */
    public <R> LambdaQueryWrapper<T> le(SFunction<T, R> column, R value) {
        if (value != null) {
            String fieldName = getDatabaseFieldName(column);
            Object dbValue = convertValueForDatabase(value);
            conditions.add(DSL.field(fieldName).le(dbValue));
        }
        return this;
    }
    
    /**
     * LIKE条件
     */
    public <R> LambdaQueryWrapper<T> like(SFunction<T, R> column, R value) {
        if (value != null) {
            String fieldName = getDatabaseFieldName(column);
            Object dbValue = convertValueForDatabase(value);
            conditions.add(DSL.field(fieldName).like("%" + dbValue + "%"));
        }
        return this;
    }
    
    /**
     * 左LIKE条件
     */
    public <R> LambdaQueryWrapper<T> likeLeft(SFunction<T, R> column, R value) {
        if (value != null) {
            String fieldName = getDatabaseFieldName(column);
            Object dbValue = convertValueForDatabase(value);
            conditions.add(DSL.field(fieldName).like("%" + dbValue));
        }
        return this;
    }
    
    /**
     * 右LIKE条件
     */
    public <R> LambdaQueryWrapper<T> likeRight(SFunction<T, R> column, R value) {
        if (value != null) {
            String fieldName = getDatabaseFieldName(column);
            Object dbValue = convertValueForDatabase(value);
            conditions.add(DSL.field(fieldName).like(dbValue + "%"));
        }
        return this;
    }
    
    /**
     * 不包含条件
     */
    public <R> LambdaQueryWrapper<T> notLike(SFunction<T, R> column, R value) {
        if (value != null) {
            String fieldName = getDatabaseFieldName(column);
            Object dbValue = convertValueForDatabase(value);
            conditions.add(DSL.field(fieldName).notLike("%" + dbValue + "%"));
        }
        return this;
    }
    
    /**
     * IN条件
     */
    public <R> LambdaQueryWrapper<T> in(SFunction<T, R> column, Collection<R> values) {
        if (values != null && !values.isEmpty()) {
            String fieldName = getDatabaseFieldName(column);
            Object[] dbValues = values.stream().map(this::convertValueForDatabase).toArray();
            conditions.add(DSL.field(fieldName).in(dbValues));
        }
        return this;
    }
    
    /**
     * IN条件（数组）
     */
    @SafeVarargs
    public final <R> LambdaQueryWrapper<T> in(SFunction<T, R> column, R... values) {
        if (values != null && values.length > 0) {
            String fieldName = getDatabaseFieldName(column);
            Object[] dbValues = java.util.Arrays.stream(values).map(this::convertValueForDatabase).toArray();
            conditions.add(DSL.field(fieldName).in(dbValues));
        }
        return this;
    }
    
    /**
     * NOT IN条件
     */
    public <R> LambdaQueryWrapper<T> notIn(SFunction<T, R> column, Collection<R> values) {
        if (values != null && !values.isEmpty()) {
            String fieldName = getDatabaseFieldName(column);
            Object[] dbValues = values.stream().map(this::convertValueForDatabase).toArray();
            conditions.add(DSL.field(fieldName).notIn(dbValues));
        }
        return this;
    }
    
    /**
     * 为空条件
     */
    public <R> LambdaQueryWrapper<T> isNull(SFunction<T, R> column) {
        String fieldName = getDatabaseFieldName(column);
        conditions.add(DSL.field(fieldName).isNull());
        return this;
    }
    
    /**
     * 不为空条件
     */
    public <R> LambdaQueryWrapper<T> isNotNull(SFunction<T, R> column) {
        String fieldName = getDatabaseFieldName(column);
        conditions.add(DSL.field(fieldName).isNotNull());
        return this;
    }
    
    /**
     * BETWEEN条件
     */
    public <R> LambdaQueryWrapper<T> between(SFunction<T, R> column, R value1, R value2) {
        if (value1 != null && value2 != null) {
            String fieldName = getDatabaseFieldName(column);
            Object dbValue1 = convertValueForDatabase(value1);
            Object dbValue2 = convertValueForDatabase(value2);
            conditions.add(DSL.field(fieldName).between(dbValue1, dbValue2));
        }
        return this;
    }
    
    /**
     * NOT BETWEEN条件
     */
    public <R> LambdaQueryWrapper<T> notBetween(SFunction<T, R> column, R value1, R value2) {
        if (value1 != null && value2 != null) {
            String fieldName = getDatabaseFieldName(column);
            Object dbValue1 = convertValueForDatabase(value1);
            Object dbValue2 = convertValueForDatabase(value2);
            conditions.add(DSL.field(fieldName).notBetween(dbValue1, dbValue2));
        }
        return this;
    }
    
    /**
     * 嵌套条件 - AND
     */
    public LambdaQueryWrapper<T> and(Function<LambdaQueryWrapper<T>, LambdaQueryWrapper<T>> func) {
        LambdaQueryWrapper<T> nestedWrapper = new LambdaQueryWrapper<>(dslContext, table, entityClass);
        func.apply(nestedWrapper);
        if (!nestedWrapper.conditions.isEmpty()) {
            conditions.add(DSL.and(nestedWrapper.conditions));
        }
        return this;
    }
    
    /**
     * 嵌套条件 - OR
     */
    public LambdaQueryWrapper<T> or(Function<LambdaQueryWrapper<T>, LambdaQueryWrapper<T>> func) {
        LambdaQueryWrapper<T> nestedWrapper = new LambdaQueryWrapper<>(dslContext, table, entityClass);
        func.apply(nestedWrapper);
        if (!nestedWrapper.conditions.isEmpty()) {
            conditions.add(DSL.or(nestedWrapper.conditions));
        }
        return this;
    }
    
    // =================== 排序方法 ===================
    
    /**
     * 升序排序
     */
    public <R> LambdaQueryWrapper<T> orderByAsc(SFunction<T, R> column) {
        String fieldName = getDatabaseFieldName(column);
        orderByFields.add(DSL.field(fieldName).asc());
        return this;
    }
    
    /**
     * 降序排序
     */
    public <R> LambdaQueryWrapper<T> orderByDesc(SFunction<T, R> column) {
        String fieldName = getDatabaseFieldName(column);
        orderByFields.add(DSL.field(fieldName).desc());
        return this;
    }
    
    /**
     * 多字段排序
     */
    @SafeVarargs
    public final <R> LambdaQueryWrapper<T> orderByAsc(SFunction<T, R>... columns) {
        Arrays.stream(columns).forEach(this::orderByAsc);
        return this;
    }
    
    /**
     * 多字段排序
     */
    @SafeVarargs
    public final <R> LambdaQueryWrapper<T> orderByDesc(SFunction<T, R>... columns) {
        Arrays.stream(columns).forEach(this::orderByDesc);
        return this;
    }
    
    // =================== 字段选择方法 ===================
    
    /**
     * 选择指定字段
     */
    @SafeVarargs
    public final <R> LambdaQueryWrapper<T> select(SFunction<T, R>... columns) {
        Arrays.stream(columns).forEach(column -> {
            String fieldName = getDatabaseFieldName(column);
            selectFields.add(DSL.field(fieldName));
        });
        return this;
    }
    
    // =================== 分页方法 ===================
    
    /**
     * 限制查询数量
     */
    public LambdaQueryWrapper<T> limit(long count) {
        this.limitCount = count;
        return this;
    }
    
    /**
     * 偏移量
     */
    public LambdaQueryWrapper<T> offset(long count) {
        this.offsetCount = count;
        return this;
    }
    
    /**
     * 分页查询
     */
    public LambdaQueryWrapper<T> page(long current, long size) {
        this.offsetCount = (current - 1) * size;
        this.limitCount = size;
        return this;
    }
    
    // =================== 查询执行方法 ===================
    
    /**
     * 构建查询条件
     */
    public Condition buildCondition() {
        if (conditions.isEmpty()) {
            return DSL.noCondition();
        }
        return DSL.and(conditions);
    }
    
    /**
     * 构建查询
     */
    public Query buildSelect() {
        Query query;
        
        if (selectFields.isEmpty()) {
            query = dslContext.selectFrom(table).where(buildCondition());
        } else {
            query = dslContext.select(selectFields).from(table).where(buildCondition());
        }
        
        if (!orderByFields.isEmpty()) {
            if (query instanceof SelectConditionStep) {
                query = ((SelectConditionStep<?>) query).orderBy(orderByFields);
            }
        }
        
        if (limitCount != null) {
            if (query instanceof SelectOrderByStep) {
                query = ((SelectOrderByStep<?>) query).limit(limitCount);
            } else if (query instanceof SelectConditionStep) {
                query = ((SelectConditionStep<?>) query).limit(limitCount);
            }
        }
        
        if (offsetCount != null) {
            if (query instanceof SelectLimitStep) {
                query = ((SelectLimitStep<?>) query).offset(offsetCount);
            } else if (query instanceof SelectOrderByStep) {
                query = ((SelectOrderByStep<?>) query).limit(limitCount != null ? limitCount : Long.MAX_VALUE).offset(offsetCount);
            }
        }
        
        return query;
    }
    
    /**
     * 构建计数查询
     */
    public Query buildCount() {
        return dslContext.selectCount().from(table).where(buildCondition());
    }
    
    /**
     * 构建存在查询
     */
    public Query buildExists() {
        return dslContext.selectOne().from(table).where(buildCondition()).limit(1);
    }
    
    // =================== 工具方法 ===================
    
    /**
     * 获取数据库字段名（处理驼峰转下划线）
     * 默认使用下划线命名，符合数据库字段命名规范
     */
    private <R> String getDatabaseFieldName(SFunction<T, R> column) {
        String javaFieldName = LambdaUtils.getFieldName(column);
        
        // 通过反射获取实体类字段的@DdlColumn注解（包括父类字段）
        java.lang.reflect.Field field = getFieldFromClassHierarchy(entityClass, javaFieldName);
        if (field != null) {
            cn.qaiu.db.ddl.DdlColumn ddlColumn = field.getAnnotation(cn.qaiu.db.ddl.DdlColumn.class);
            if (ddlColumn != null) {
                // 优先使用value字段，如果为空则使用name字段
                if (!ddlColumn.value().isEmpty()) {
                    return ddlColumn.value();
                } else if (!ddlColumn.name().isEmpty()) {
                    return ddlColumn.name();
                }
            }
        }
        
        // 默认使用驼峰转下划线（符合数据库字段命名规范）
        return cn.qaiu.db.dsl.core.FieldNameConverter.toDatabaseFieldName(javaFieldName);
    }
    
    /**
     * 从类层次结构中获取字段（包括父类）
     */
    private java.lang.reflect.Field getFieldFromClassHierarchy(Class<?> clazz, String fieldName) {
        Class<?> currentClass = clazz;
        while (currentClass != null && currentClass != Object.class) {
            try {
                java.lang.reflect.Field field = currentClass.getDeclaredField(fieldName);
                return field;
            } catch (NoSuchFieldException e) {
                // 继续在父类中查找
            }
            currentClass = currentClass.getSuperclass();
        }
        return null;
    }
    
    /**
     * 清空所有条件
     */
    public LambdaQueryWrapper<T> clear() {
        conditions.clear();
        orderByFields.clear();
        selectFields.clear();
        limitCount = null;
        offsetCount = null;
        return this;
    }
    
    /**
     * 获取表名
     */
    public String getTableName() {
        return table.getName();
    }
    
    /**
     * 获取实体类
     */
    public Class<T> getEntityClass() {
        return entityClass;
    }
    
    /**
     * 将值转换为数据库兼容的值
     */
    private Object convertValueForDatabase(Object value) {
        if (value instanceof Enum) {
            return ((Enum<?>) value).name();
        }
        return value;
    }
}
