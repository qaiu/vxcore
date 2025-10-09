package cn.qaiu.db.dsl.lambda;

import io.vertx.core.Future;
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
            conditions.add(getField(column).eq(value));
        }
        return this;
    }
    
    /**
     * 不等于条件
     */
    public <R> LambdaQueryWrapper<T> ne(SFunction<T, R> column, R value) {
        if (value != null) {
            conditions.add(getField(column).ne(value));
        }
        return this;
    }
    
    /**
     * 大于条件
     */
    public <R> LambdaQueryWrapper<T> gt(SFunction<T, R> column, R value) {
        if (value != null) {
            conditions.add(getField(column).gt(value));
        }
        return this;
    }
    
    /**
     * 大于等于条件
     */
    public <R> LambdaQueryWrapper<T> ge(SFunction<T, R> column, R value) {
        if (value != null) {
            conditions.add(getField(column).ge(value));
        }
        return this;
    }
    
    /**
     * 小于条件
     */
    public <R> LambdaQueryWrapper<T> lt(SFunction<T, R> column, R value) {
        if (value != null) {
            conditions.add(getField(column).lt(value));
        }
        return this;
    }
    
    /**
     * 小于等于条件
     */
    public <R> LambdaQueryWrapper<T> le(SFunction<T, R> column, R value) {
        if (value != null) {
            conditions.add(getField(column).le(value));
        }
        return this;
    }
    
    /**
     * LIKE条件
     */
    public <R> LambdaQueryWrapper<T> like(SFunction<T, R> column, R value) {
        if (value != null) {
            conditions.add(getField(column).like("%" + value + "%"));
        }
        return this;
    }
    
    /**
     * 左LIKE条件
     */
    public <R> LambdaQueryWrapper<T> likeLeft(SFunction<T, R> column, R value) {
        if (value != null) {
            conditions.add(getField(column).like("%" + value));
        }
        return this;
    }
    
    /**
     * 右LIKE条件
     */
    public <R> LambdaQueryWrapper<T> likeRight(SFunction<T, R> column, R value) {
        if (value != null) {
            conditions.add(getField(column).like(value + "%"));
        }
        return this;
    }
    
    /**
     * 不包含条件
     */
    public <R> LambdaQueryWrapper<T> notLike(SFunction<T, R> column, R value) {
        if (value != null) {
            conditions.add(getField(column).notLike("%" + value + "%"));
        }
        return this;
    }
    
    /**
     * IN条件
     */
    public <R> LambdaQueryWrapper<T> in(SFunction<T, R> column, Collection<R> values) {
        if (values != null && !values.isEmpty()) {
            conditions.add(getField(column).in(values));
        }
        return this;
    }
    
    /**
     * IN条件（数组）
     */
    @SafeVarargs
    public final <R> LambdaQueryWrapper<T> in(SFunction<T, R> column, R... values) {
        if (values != null && values.length > 0) {
            conditions.add(getField(column).in(values));
        }
        return this;
    }
    
    /**
     * NOT IN条件
     */
    public <R> LambdaQueryWrapper<T> notIn(SFunction<T, R> column, Collection<R> values) {
        if (values != null && !values.isEmpty()) {
            conditions.add(getField(column).notIn(values));
        }
        return this;
    }
    
    /**
     * 为空条件
     */
    public <R> LambdaQueryWrapper<T> isNull(SFunction<T, R> column) {
        conditions.add(getField(column).isNull());
        return this;
    }
    
    /**
     * 不为空条件
     */
    public <R> LambdaQueryWrapper<T> isNotNull(SFunction<T, R> column) {
        conditions.add(getField(column).isNotNull());
        return this;
    }
    
    /**
     * BETWEEN条件
     */
    public <R> LambdaQueryWrapper<T> between(SFunction<T, R> column, R value1, R value2) {
        if (value1 != null && value2 != null) {
            conditions.add(getField(column).between(value1, value2));
        }
        return this;
    }
    
    /**
     * NOT BETWEEN条件
     */
    public <R> LambdaQueryWrapper<T> notBetween(SFunction<T, R> column, R value1, R value2) {
        if (value1 != null && value2 != null) {
            conditions.add(getField(column).notBetween(value1, value2));
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
        orderByFields.add(getField(column).asc());
        return this;
    }
    
    /**
     * 降序排序
     */
    public <R> LambdaQueryWrapper<T> orderByDesc(SFunction<T, R> column) {
        orderByFields.add(getField(column).desc());
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
        Arrays.stream(columns).forEach(column -> selectFields.add(getField(column)));
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
            query = ((SelectConditionStep<?>) query).orderBy(orderByFields);
        }
        
        if (limitCount != null) {
            query = ((SelectOrderByStep<?>) query).limit(limitCount);
        }
        
        if (offsetCount != null) {
            query = ((SelectLimitStep<?>) query).offset(offsetCount);
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
     * 从Lambda表达式获取字段名
     */
    private <R> Field<R> getField(SFunction<T, R> column) {
        String fieldName = LambdaUtils.getFieldName(column);
        return DSL.field(fieldName, LambdaUtils.getFieldType(column));
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
}
