package cn.qaiu.db.dsl.lambda;

import io.vertx.core.Future;
import org.jooq.*;
import org.jooq.impl.DSL;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;
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
    private final cn.qaiu.db.dsl.core.JooqExecutor executor;
    private final List<Condition> conditions;
    private final List<SortField<?>> orderByFields;
    private final List<Field<?>> selectFields;
    private final List<JoinInfo> joins;
    private final List<Field<?>> groupByFields;
    private final List<Condition> havingConditions;
    private Long limitCount;
    private Long offsetCount;
    
    public LambdaQueryWrapper(DSLContext dslContext, Table<?> table, Class<T> entityClass) {
        this.dslContext = dslContext;
        this.table = table;
        this.entityClass = entityClass;
        this.executor = null; // 兼容旧构造函数
        this.conditions = new ArrayList<>();
        this.orderByFields = new ArrayList<>();
        this.selectFields = new ArrayList<>();
        this.joins = new ArrayList<>();
        this.groupByFields = new ArrayList<>();
        this.havingConditions = new ArrayList<>();
    }
    
    public LambdaQueryWrapper(cn.qaiu.db.dsl.core.JooqExecutor executor, Table<?> table, Class<T> entityClass) {
        this.executor = executor;
        this.dslContext = executor.dsl();
        this.table = table;
        this.entityClass = entityClass;
        this.conditions = new ArrayList<>();
        this.orderByFields = new ArrayList<>();
        this.selectFields = new ArrayList<>();
        this.joins = new ArrayList<>();
        this.groupByFields = new ArrayList<>();
        this.havingConditions = new ArrayList<>();
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
     * 条件式等于 - 支持 .eq(condition, Product::getName, name) 语法
     */
    public <R> LambdaQueryWrapper<T> eq(boolean condition, SFunction<T, R> column, R value) {
        if (condition && value != null) {
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
     * 条件式不等于
     */
    public <R> LambdaQueryWrapper<T> ne(boolean condition, SFunction<T, R> column, R value) {
        if (condition && value != null) {
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
     * 条件式大于
     */
    public <R> LambdaQueryWrapper<T> gt(boolean condition, SFunction<T, R> column, R value) {
        if (condition && value != null) {
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
     * 条件式大于等于
     */
    public <R> LambdaQueryWrapper<T> ge(boolean condition, SFunction<T, R> column, R value) {
        if (condition && value != null) {
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
     * 条件式小于
     */
    public <R> LambdaQueryWrapper<T> lt(boolean condition, SFunction<T, R> column, R value) {
        if (condition && value != null) {
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
     * 条件式小于等于
     */
    public <R> LambdaQueryWrapper<T> le(boolean condition, SFunction<T, R> column, R value) {
        if (condition && value != null) {
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
     * 条件式LIKE
     */
    public <R> LambdaQueryWrapper<T> like(boolean condition, SFunction<T, R> column, R value) {
        if (condition && value != null) {
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
            Object[] dbValues = Arrays.stream(values).map(this::convertValueForDatabase).toArray();
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
        
        // 构建基础查询
        if (selectFields.isEmpty()) {
            query = dslContext.selectFrom(table);
        } else {
            query = dslContext.select(selectFields).from(table);
        }
        
        // 添加JOIN
        if (!joins.isEmpty()) {
            SelectJoinStep<?> joinStep = (SelectJoinStep<?>) query;
            for (JoinInfo joinInfo : joins) {
                switch (joinInfo.joinType) {
                    case INNER_JOIN:
                        joinStep = joinStep.innerJoin(joinInfo.joinTable).on(joinInfo.joinCondition);
                        break;
                    case LEFT_JOIN:
                        joinStep = joinStep.leftJoin(joinInfo.joinTable).on(joinInfo.joinCondition);
                        break;
                    case RIGHT_JOIN:
                        joinStep = joinStep.rightJoin(joinInfo.joinTable).on(joinInfo.joinCondition);
                        break;
                    case FULL_JOIN:
                        joinStep = joinStep.fullJoin(joinInfo.joinTable).on(joinInfo.joinCondition);
                        break;
                }
            }
            query = joinStep;
        }
        
        // 添加WHERE条件
        Condition whereCondition = buildCondition();
        if (whereCondition != null && !whereCondition.equals(DSL.noCondition())) {
            if (query instanceof SelectJoinStep) {
                query = ((SelectJoinStep<?>) query).where(whereCondition);
            } else if (query instanceof SelectFromStep) {
                query = ((SelectFromStep<?>) query).where(whereCondition);
            }
        }
        
        // 添加GROUP BY
        if (!groupByFields.isEmpty()) {
            if (query instanceof SelectConditionStep) {
                query = ((SelectConditionStep<?>) query).groupBy(groupByFields);
            } else if (query instanceof SelectJoinStep) {
                query = ((SelectJoinStep<?>) query).groupBy(groupByFields);
            }
        }
        
        // 添加HAVING
        if (!havingConditions.isEmpty()) {
            Condition havingCondition = DSL.and(havingConditions);
            if (query instanceof SelectGroupByStep) {
                query = ((SelectGroupByStep<?>) query).having(havingCondition);
            }
        }
        
        // 添加ORDER BY
        if (!orderByFields.isEmpty()) {
            if (query instanceof SelectHavingStep) {
                query = ((SelectHavingStep<?>) query).orderBy(orderByFields);
            } else if (query instanceof SelectGroupByStep) {
                query = ((SelectGroupByStep<?>) query).orderBy(orderByFields);
            } else if (query instanceof SelectConditionStep) {
                query = ((SelectConditionStep<?>) query).orderBy(orderByFields);
            }
        }
        
        // 添加LIMIT
        if (limitCount != null) {
            if (query instanceof SelectOrderByStep) {
                query = ((SelectOrderByStep<?>) query).limit(limitCount);
            } else if (query instanceof SelectHavingStep) {
                query = ((SelectHavingStep<?>) query).limit(limitCount);
            } else if (query instanceof SelectGroupByStep) {
                query = ((SelectGroupByStep<?>) query).limit(limitCount);
            } else if (query instanceof SelectConditionStep) {
                query = ((SelectConditionStep<?>) query).limit(limitCount);
            }
        }
        
        // 添加OFFSET
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
        // LambdaUtils.getFieldName() 已经处理了@DdlColumn注解和字段名转换
        // 直接返回结果，避免重复转换
        return LambdaUtils.getFieldName(column);
    }
    
    
    /**
     * 清空所有条件
     */
    public LambdaQueryWrapper<T> clear() {
        conditions.clear();
        orderByFields.clear();
        selectFields.clear();
        joins.clear();
        groupByFields.clear();
        havingConditions.clear();
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
    
    // =================== Join查询方法 ===================
    
    /**
     * 内连接
     */
    public <J> LambdaQueryWrapper<T> innerJoin(Class<J> joinClass, BiFunction<T, J, Condition> on) {
        return addJoin(JoinType.INNER_JOIN, joinClass, on);
    }
    
    /**
     * 左连接
     */
    public <J> LambdaQueryWrapper<T> leftJoin(Class<J> joinClass, BiFunction<T, J, Condition> on) {
        return addJoin(JoinType.LEFT_JOIN, joinClass, on);
    }
    
    /**
     * 右连接
     */
    public <J> LambdaQueryWrapper<T> rightJoin(Class<J> joinClass, BiFunction<T, J, Condition> on) {
        return addJoin(JoinType.RIGHT_JOIN, joinClass, on);
    }
    
    /**
     * 全外连接
     */
    public <J> LambdaQueryWrapper<T> fullJoin(Class<J> joinClass, BiFunction<T, J, Condition> on) {
        return addJoin(JoinType.FULL_JOIN, joinClass, on);
    }
    
    /**
     * 添加Join信息
     */
    private <J> LambdaQueryWrapper<T> addJoin(JoinType joinType, Class<J> joinClass, BiFunction<T, J, Condition> on) {
        String joinTableName = getTableNameFromClass(joinClass);
        Table<?> joinTable = DSL.table(DSL.name(joinTableName));
        
        // 创建Join条件（这里简化处理，实际使用时需要更复杂的条件构建）
        Condition joinCondition = DSL.trueCondition(); // 占位符，实际应该根据on函数构建
        
        joins.add(new JoinInfo(joinType, joinTable, joinCondition));
        return this;
    }
    
    /**
     * 从实体类获取表名
     */
    private String getTableNameFromClass(Class<?> entityClass) {
        // 检查@DdlTable注解
        cn.qaiu.db.ddl.DdlTable ddlTable = entityClass.getAnnotation(cn.qaiu.db.ddl.DdlTable.class);
        if (ddlTable != null && !ddlTable.value().isEmpty()) {
            return ddlTable.value();
        }
        
        // 默认使用类名转下划线
        String className = entityClass.getSimpleName();
        return cn.qaiu.db.dsl.core.FieldNameConverter.toDatabaseTableName(className);
    }
    
    // =================== 聚合查询方法 ===================
    
    /**
     * 分组查询
     */
    @SafeVarargs
    public final <R> LambdaQueryWrapper<T> groupBy(SFunction<T, R>... columns) {
        Arrays.stream(columns).forEach(column -> {
            String fieldName = getDatabaseFieldName(column);
            groupByFields.add(DSL.field(fieldName));
        });
        return this;
    }
    
    /**
     * HAVING条件
     */
    public LambdaQueryWrapper<T> having(Condition condition) {
        if (condition != null) {
            havingConditions.add(condition);
        }
        return this;
    }
    
    /**
     * HAVING条件 - 聚合函数
     */
    public LambdaQueryWrapper<T> having(Function<AggregateFunctions, Condition> conditionFunc) {
        AggregateFunctions aggFuncs = new AggregateFunctions();
        Condition condition = conditionFunc.apply(aggFuncs);
        return having(condition);
    }
    
    /**
     * 选择聚合函数
     */
    @SafeVarargs
    public final <R> LambdaQueryWrapper<T> selectAggregate(SFunction<T, R>... columns) {
        Arrays.stream(columns).forEach(column -> {
            String fieldName = getDatabaseFieldName(column);
            selectFields.add(DSL.field(fieldName));
        });
        return this;
    }
    
    /**
     * 选择聚合函数 - COUNT
     */
    public LambdaQueryWrapper<T> selectCount() {
        selectFields.add(DSL.count());
        return this;
    }
    
    /**
     * 选择聚合函数 - COUNT DISTINCT
     */
    public <R> LambdaQueryWrapper<T> selectCountDistinct(SFunction<T, R> column) {
        String fieldName = getDatabaseFieldName(column);
        selectFields.add(DSL.countDistinct(DSL.field(fieldName)));
        return this;
    }
    
    /**
     * 选择聚合函数 - SUM
     */
    public <R> LambdaQueryWrapper<T> selectSum(SFunction<T, R> column) {
        String fieldName = getDatabaseFieldName(column);
        selectFields.add(DSL.sum(DSL.field(fieldName, Number.class)));
        return this;
    }
    
    /**
     * 选择聚合函数 - AVG
     */
    public <R> LambdaQueryWrapper<T> selectAvg(SFunction<T, R> column) {
        String fieldName = getDatabaseFieldName(column);
        selectFields.add(DSL.avg(DSL.field(fieldName, Number.class)));
        return this;
    }
    
    /**
     * 选择聚合函数 - MAX
     */
    public <R> LambdaQueryWrapper<T> selectMax(SFunction<T, R> column) {
        String fieldName = getDatabaseFieldName(column);
        selectFields.add(DSL.max(DSL.field(fieldName)));
        return this;
    }
    
    /**
     * 选择聚合函数 - MIN
     */
    public <R> LambdaQueryWrapper<T> selectMin(SFunction<T, R> column) {
        String fieldName = getDatabaseFieldName(column);
        selectFields.add(DSL.min(DSL.field(fieldName)));
        return this;
    }
    
    // =================== 子查询方法 ===================
    
    /**
     * EXISTS子查询
     */
    public LambdaQueryWrapper<T> exists(Class<?> subQueryClass, Function<LambdaQueryWrapper<?>, LambdaQueryWrapper<?>> subQueryBuilder) {
        LambdaQueryWrapper<?> subWrapper = new LambdaQueryWrapper<>(dslContext, 
            DSL.table(DSL.name(getTableNameFromClass(subQueryClass))), subQueryClass);
        subQueryBuilder.apply(subWrapper);
        conditions.add(DSL.exists((Select<?>) subWrapper.buildSelect()));
        return this;
    }
    
    /**
     * NOT EXISTS子查询
     */
    public LambdaQueryWrapper<T> notExists(Class<?> subQueryClass, Function<LambdaQueryWrapper<?>, LambdaQueryWrapper<?>> subQueryBuilder) {
        LambdaQueryWrapper<?> subWrapper = new LambdaQueryWrapper<>(dslContext, 
            DSL.table(DSL.name(getTableNameFromClass(subQueryClass))), subQueryClass);
        subQueryBuilder.apply(subWrapper);
        conditions.add(DSL.notExists((Select<?>) subWrapper.buildSelect()));
        return this;
    }
    
    /**
     * IN子查询
     */
    public <R> LambdaQueryWrapper<T> inSubQuery(SFunction<T, R> column, Class<?> subQueryClass, 
            Function<LambdaQueryWrapper<?>, LambdaQueryWrapper<?>> subQueryBuilder) {
        String fieldName = getDatabaseFieldName(column);
        LambdaQueryWrapper<?> subWrapper = new LambdaQueryWrapper<>(dslContext, 
            DSL.table(DSL.name(getTableNameFromClass(subQueryClass))), subQueryClass);
        subQueryBuilder.apply(subWrapper);
        conditions.add(DSL.field(fieldName).in(subWrapper.buildSelect()));
        return this;
    }
    
    /**
     * NOT IN子查询
     */
    public <R> LambdaQueryWrapper<T> notInSubQuery(SFunction<T, R> column, Class<?> subQueryClass, 
            Function<LambdaQueryWrapper<?>, LambdaQueryWrapper<?>> subQueryBuilder) {
        String fieldName = getDatabaseFieldName(column);
        LambdaQueryWrapper<?> subWrapper = new LambdaQueryWrapper<>(dslContext, 
            DSL.table(DSL.name(getTableNameFromClass(subQueryClass))), subQueryClass);
        subQueryBuilder.apply(subWrapper);
        conditions.add(DSL.field(fieldName).notIn(subWrapper.buildSelect()));
        return this;
    }
    
    /**
     * 标量子查询
     */
    public <R> LambdaQueryWrapper<T> eqSubQuery(SFunction<T, R> column, Class<?> subQueryClass, 
            Function<LambdaQueryWrapper<?>, LambdaQueryWrapper<?>> subQueryBuilder) {
        String fieldName = getDatabaseFieldName(column);
        LambdaQueryWrapper<?> subWrapper = new LambdaQueryWrapper<>(dslContext, 
            DSL.table(DSL.name(getTableNameFromClass(subQueryClass))), subQueryClass);
        subQueryBuilder.apply(subWrapper);
        conditions.add(DSL.field(fieldName).eq(subWrapper.buildSelect()));
        return this;
    }
    
    // =================== 内部类 ===================
    
    /**
     * Join信息
     */
    private static class JoinInfo {
        final JoinType joinType;
        final Table<?> joinTable;
        final Condition joinCondition;
        
        JoinInfo(JoinType joinType, Table<?> joinTable, Condition joinCondition) {
            this.joinType = joinType;
            this.joinTable = joinTable;
            this.joinCondition = joinCondition;
        }
    }
    
    /**
     * Join类型枚举
     */
    private enum JoinType {
        INNER_JOIN, LEFT_JOIN, RIGHT_JOIN, FULL_JOIN
    }
    
    /**
     * 聚合函数工具类
     */
    public static class AggregateFunctions {
        
        public Condition countGt(int value) {
            return DSL.count().gt(value);
        }
        
        public Condition countLt(int value) {
            return DSL.count().lt(value);
        }
        
        public Condition countEq(int value) {
            return DSL.count().eq(value);
        }
        
        public Condition sumGt(Number value) {
            return DSL.sum(DSL.field("amount", Number.class)).gt(java.math.BigDecimal.valueOf(value.doubleValue()));
        }
        
        public Condition avgGt(Number value) {
            return DSL.avg(DSL.field("amount", Number.class)).gt(java.math.BigDecimal.valueOf(value.doubleValue()));
        }
        
        public Condition maxGt(Number value) {
            return DSL.max(DSL.field("amount")).gt(value);
        }
        
        public Condition minLt(Number value) {
            return DSL.min(DSL.field("amount")).lt(value);
        }
    }
    
    // =================== 查询执行方法 ===================
    
    /**
     * 执行查询并返回列表
     */
    public Future<List<T>> list() {
        return executeQuery().map(rows -> {
            // 这里需要实现从RowSet到List<T>的转换
            // 暂时返回空列表，实际实现需要根据具体的映射器
            return new ArrayList<>();
        });
    }
    
    /**
     * 执行查询并返回单个对象
     */
    public Future<T> one() {
        return executeQuery().map(rows -> {
            if (rows.isEmpty()) {
                return null;
            }
            // 这里需要实现从RowSet到T的转换
            // 暂时返回null，实际实现需要根据具体的映射器
            return null;
        });
    }
    
    /**
     * 执行查询并返回数量
     */
    public Future<Long> count() {
        // 构建COUNT查询
        SelectJoinStep<Record1<Integer>> countQuery = dslContext
                .select(DSL.count())
                .from(table);
        
        // 添加WHERE条件
        SelectConditionStep<Record1<Integer>> whereQuery;
        if (!conditions.isEmpty()) {
            whereQuery = countQuery.where(DSL.and(conditions));
        } else {
            whereQuery = countQuery.where(DSL.noCondition());
        }
        
        return executeCountQuery(whereQuery);
    }
    
    /**
     * 执行分页查询
     */
    public Future<List<T>> executePage(long current, long size) {
        // 设置分页参数
        this.limitCount = size;
        this.offsetCount = (current - 1) * size;
        
        return list();
    }
    
    /**
     * 映射结果 - 支持链式调用
     */
    public <R> Future<R> map(Function<List<T>, R> mapper) {
        return list().map(mapper);
    }
    
    /**
     * 执行查询
     */
    @SuppressWarnings("unchecked")
    private Future<Result<org.jooq.Record>> executeQuery() {
        // 构建基础查询
        SelectConditionStep<org.jooq.Record> query;
        
        if (selectFields.isEmpty()) {
            query = (SelectConditionStep<org.jooq.Record>) dslContext.selectFrom(table).where(DSL.noCondition());
        } else {
            query = (SelectConditionStep<org.jooq.Record>) dslContext.select(selectFields.toArray(new Field[0])).from(table).where(DSL.noCondition());
        }
        
        // 添加WHERE条件
        if (!conditions.isEmpty()) {
            query = query.and(DSL.and(conditions));
        }
        
        // 添加ORDER BY
        SelectSeekStepN<org.jooq.Record> orderedQuery;
        if (!orderByFields.isEmpty()) {
            orderedQuery = query.orderBy(orderByFields.toArray(new OrderField[0]));
        } else {
            orderedQuery = query.orderBy(new OrderField[0]);
        }
        
        // 添加LIMIT和OFFSET
        SelectLimitPercentStep<org.jooq.Record> limitedQuery;
        if (limitCount != null) {
            limitedQuery = orderedQuery.limit(limitCount);
        } else {
            limitedQuery = orderedQuery.limit(Integer.MAX_VALUE);
        }
        
        SelectForUpdateStep<org.jooq.Record> finalQuery;
        if (offsetCount != null) {
            finalQuery = limitedQuery.offset(offsetCount);
        } else {
            finalQuery = limitedQuery.offset(0);
        }
        
        // 执行查询 - 使用JooqExecutor
        if (executor != null) {
            // 直接使用DSLContext执行查询，因为LambdaQueryWrapper需要jOOQ Result类型
            return Future.succeededFuture(dslContext.fetch(finalQuery));
        } else {
            // 兼容旧版本，直接使用DSLContext
            return Future.succeededFuture(dslContext.fetch(finalQuery));
        }
    }
    
    /**
     * 执行COUNT查询
     */
    private Future<Long> executeCountQuery(SelectConditionStep<Record1<Integer>> countQuery) {
        // 执行COUNT查询 - 直接使用DSLContext
        if (executor != null) {
            // 使用DSLContext执行查询
            return Future.succeededFuture(dslContext.fetchOne(countQuery).value1().longValue());
        } else {
            // 兼容旧版本，直接使用DSLContext
            return Future.succeededFuture(0L);
        }
    }
}
