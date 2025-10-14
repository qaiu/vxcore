package cn.qaiu.db.dsl.core;

import cn.qaiu.db.dsl.common.PageRequest;
import cn.qaiu.db.dsl.common.PageResult;
import cn.qaiu.db.dsl.common.QueryCondition;
import cn.qaiu.db.dsl.interfaces.JooqDao;
import cn.qaiu.db.dsl.mapper.EntityMapper;
import cn.qaiu.db.dsl.mapper.DefaultMapper;
import cn.qaiu.vx.core.util.StringCase;
import cn.qaiu.vx.core.util.VertxHolder;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.impl.VertxHandler;

import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Query;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 增强的DAO基类
 * 
 * 在基础CRUD功能基础上，增加了以下高级查询功能：
 * - 分页查询：支持分页参数和结果封装
 * - 条件查询：支持复杂查询条件构建
 * - 排序查询：支持单字段和多字段排序
 * - 聚合查询：支持统计、分组等操作
 * - 批量操作：支持批量插入、更新、删除
 * 
 * 专为Vert.x异步服务设计，所有分页和查询对象都支持序列化
 * 
 * @param <T> 实体类型
 * @param <ID> 主键类型
 * @author <a href="https://qaiu.top">QAIU</a>
 */
public abstract class EnhancedDao<T, ID> implements JooqDao<T, ID> {

    private static final Logger LOGGER = LoggerFactory.getLogger(EnhancedDao.class);

    protected final Class<T> entityClass;
    protected final String tableName;
    protected final String primaryKeyField;
    protected final EntityMapper<T> entityMapper;
    
    // 执行器相关（支持延迟初始化）
    protected volatile JooqExecutor executor;
    protected volatile JooqDslBuilder dslBuilder;
    private volatile String dataSourceName;
    private final boolean autoExecutorMode;

    /**
     * 构造函数1：手动传入JooqExecutor（传统方式）
     */
    public EnhancedDao(JooqExecutor executor, Class<T> entityClass) {
        this.executor = executor;
        this.entityClass = entityClass;
        this.autoExecutorMode = false;
        
        // 立即初始化
        this.dslBuilder = new JooqDslBuilder(executor.dsl());
        this.tableName = dslBuilder.getTableName(entityClass);
        this.primaryKeyField = dslBuilder.getPrimaryKey(entityClass);
        this.entityMapper = new DefaultMapper<>(entityClass, tableName);
        
        LOGGER.debug("EnhancedDao initialized with manual JooqExecutor for entity: {}", 
                entityClass.getSimpleName());
    }

    /**
     * 构造函数2：自动获取JooqExecutor（推荐方式）
     */
    public EnhancedDao(Class<T> entityClass) {
        this.entityClass = entityClass;
        this.autoExecutorMode = true;
        
        // 初始化基本信息
        this.dataSourceName = getDataSourceNameFromAnnotation();
        this.tableName = getTableNameFromEntity(entityClass);
        this.primaryKeyField = getPrimaryKeyFromEntity(entityClass);
        this.entityMapper = new DefaultMapper<>(entityClass, tableName);
        
        LOGGER.debug("EnhancedDao initialized with auto-executor mode for entity: {} with datasource: {}", 
                entityClass.getSimpleName(), dataSourceName);
    }

    /**
     * 构造函数3：无参构造函数（最推荐方式）
     * 自动通过泛型获取实体类类型
     */
    @SuppressWarnings("unchecked")
    public EnhancedDao() {
        this.entityClass = (Class<T>) getGenericEntityClass();
        this.autoExecutorMode = true;
        
        // 初始化基本信息
        this.dataSourceName = getDataSourceNameFromAnnotation();
        this.tableName = getTableNameFromEntity(entityClass);
        this.primaryKeyField = getPrimaryKeyFromEntity(entityClass);
        this.entityMapper = new DefaultMapper<>(entityClass, tableName);
        
        LOGGER.debug("EnhancedDao initialized with auto-executor mode for entity: {} (from generics) with datasource: {}", 
                entityClass.getSimpleName(), dataSourceName);
    }

    // =================== 基础CRUD方法（继承自父类） ===================

    @Override
    public Future<Optional<T>> insert(T entity) {
        return performInsert(entity);
    }

    @Override
    public Future<Optional<T>> update(T entity) {
        return performUpdate(entity);
    }

    @Override
    public Future<Boolean> delete(ID id) {
        return performDelete(id);
    }

    @Override
    public Future<Optional<T>> findById(ID id) {
        return performFindById(id);
    }

    @Override
    public Future<List<T>> findAll() {
        return performFindByCondition(DSL.noCondition());
    }

    @Override
    public Future<List<T>> findByCondition(Condition condition) {
        return performFindByCondition(condition);
    }

    @Override
    public Future<Long> count() {
        return performCount(DSL.noCondition());
    }

    @Override
    public Future<Long> count(Condition condition) {
        return performCount(condition);
    }

    @Override
    public Future<Boolean> exists(ID id) {
        return count(DSL.field(primaryKeyField).eq(id))
                .map(count -> count > 0);
    }

    @Override
    public Future<Boolean> exists(Condition condition) {
        return count(condition)
                .map(count -> count > 0);
    }

    // =================== 分页查询方法 ===================

    /**
     * 分页查询所有数据
     * 
     * @param pageRequest 分页请求
     * @return 分页结果
     */
    public Future<PageResult> findPage(PageRequest pageRequest) {
        return findPage(pageRequest, DSL.noCondition());
    }

    /**
     * 根据条件分页查询
     * 
     * @param pageRequest 分页请求
     * @param condition 查询条件
     * @return 分页结果
     */
    public Future<PageResult> findPage(PageRequest pageRequest, Condition condition) {
        try {
            pageRequest.validate();

            // 构建查询SQL
            Query countQuery = getDslBuilder().buildCount(tableName, condition);
            Query selectQuery = buildPageableSelect(tableName, condition, pageRequest);

            // 并行执行计数和查询
            Future<Long> countFuture = getExecutor().executeQuery(countQuery)
                    .map(rowSet -> rowSet.size() > 0 ? rowSet.iterator().next().getLong(0) : 0L);

            Future<List<T>> dataFuture = getExecutor().executeQuery(selectQuery)
                    .map(entityMapper::fromMultiple);

            return countFuture
                    .compose(totalRecords -> {
                        return dataFuture.map(entities -> {
                            // 转换为JsonObject列表
                            List<JsonObject> jsonEntities = entities.stream()
                                    .map(entity -> {
                                        if (entity instanceof cn.qaiu.db.dsl.BaseEntity) {
                                            return ((cn.qaiu.db.dsl.BaseEntity) entity).toJson();
                                        } else {
                                            return entityMapper.toJsonObject(entity);
                                        }
                                    })
                                    .collect(Collectors.toList());

                            return PageResult.of(pageRequest, jsonEntities, totalRecords);
                        });
                    });

        } catch (Exception e) {
            LOGGER.error("Failed to find page for table {}", tableName, e);
            return Future.failedFuture(e);
        }
    }

    /**
     * 根据QueryCondition分页查询
     * 
     * @param pageRequest 分页请求
     * @param queryCondition 查询条件
     * @return 分页结果
     */
    public Future<PageResult> findPage(PageRequest pageRequest, QueryCondition queryCondition) {
        try {
            Condition condition = convertQueryConditionToJooq(queryCondition);
            return findPage(pageRequest, condition);
        } catch (Exception e) {
            LOGGER.error("Failed to find page with QueryCondition for table {}", tableName, e);
            return Future.failedFuture(e);
        }
    }

    /**
     * 根据多个QueryCondition分页查询（AND连接）
     * 
     * @param pageRequest 分页请求
     * @param conditions 查询条件列表
     * @return 分页结果
     */
    public Future<PageResult> findPage(PageRequest pageRequest, List<QueryCondition> conditions) {
        try {
            Condition condition = convertQueryConditionsToJooq(conditions, QueryCondition.ConditionType.AND);
            return findPage(pageRequest, condition);
        } catch (Exception e) {
            LOGGER.error("Failed to find page with conditions for table {}", tableName, e);
            return Future.failedFuture(e);
        }
    }

    // =================== 高级查询方法 ===================

    /**
     * 根据字段排序查询
     * 
     * @param sortField 排序字段
     * @param ascending 是否升序
     * @param limit 限制数量
     * @return 实体列表
     */
    public Future<List<T>> findByOrder(String sortField, boolean ascending, Integer limit) {
        try {
            Query query = buildOrderedSelect(tableName, DSL.noCondition(), sortField, ascending, limit);
            return getExecutor().executeQuery(query).map(entityMapper::fromMultiple);
        } catch (Exception e) {
            LOGGER.error("Failed to find by order for table {}", tableName, e);
            return Future.failedFuture(e);
        }
    }

    /**
     * 根据多个字段排序查询
     * 
     * @param sortFields 排序字段映射（字段名 -> 是否升序）
     * @param limit 限制数量
     * @return 实体列表
     */
    public Future<List<T>> findByOrder(Map<String, Boolean> sortFields, Integer limit) {
        try {
            Query query = buildMultiOrderedSelect(tableName, DSL.noCondition(), sortFields, limit);
            return getExecutor().executeQuery(query).map(entityMapper::fromMultiple);
        } catch (Exception e) {
            LOGGER.error("Failed to find by multi-order for table {}", tableName, e);
            return Future.failedFuture(e);
        }
    }

    /**
     * 聚合查询：统计指定字段的最大值
     */
    public Future<Optional<Object>> findMax(String field) {
        return findAggregate(field, "MAX");
    }

    /**
     * 聚合查询：统计指定字段的最小值
     */
    public Future<Optional<Object>> findMin(String field) {
        return findAggregate(field, "MIN");
    }

    /**
     * 聚合查询：统计指定字段的平均值
     */
    public Future<Optional<Object>> findAvg(String field) {
        return findAggregate(field, "AVG");
    }

    /**
     * 聚合查询：统计指定字段的和
     */
    public Future<Optional<Object>> findSum(String field) {
        return findAggregate(field, "SUM");
    }

    // =================== 批量操作方法 ===================

    /**
     * 批量插入
     * 
     * @param entities 实体列表
     * @return 插入成功的实体列表
     */
    public Future<List<T>> batchInsert(List<T> entities) {
        if (entities == null || entities.isEmpty()) {
            return Future.succeededFuture(new ArrayList<>());
        }

        try {
            List<Future<Optional<T>>> insertFutures = entities.stream()
                    .map(this::insert)
                    .collect(Collectors.toList());

            return Future.all(insertFutures)
                    .map(results -> {
                        List<T> successEntities = new ArrayList<>();
                        for (int i = 0; i < results.size(); i++) {
                            Optional<T> result = results.resultAt(i);
                            if (result.isPresent()) {
                                successEntities.add(result.get());
                            }
                        }
                        return successEntities;
                    });

        } catch (Exception e) {
            LOGGER.error("Failed to batch insert entities for table {}", tableName, e);
            return Future.failedFuture(e);
        }
    }

    /**
     * 批量更新
     * 
     * @param entities 实体列表
     * @return 更新成功的实体列表
     */
    public Future<List<T>> batchUpdate(List<T> entities) {
        if (entities == null || entities.isEmpty()) {
            return Future.succeededFuture(new ArrayList<>());
        }

        try {
            List<Future<Optional<T>>> updateFutures = entities.stream()
                    .map(this::update)
                    .collect(Collectors.toList());

            return Future.all(updateFutures)
                    .map(results -> {
                        List<T> successEntities = new ArrayList<>();
                        for (int i = 0; i < results.size(); i++) {
                            Optional<T> result = results.resultAt(i);
                            if (result.isPresent()) {
                                successEntities.add(result.get());
                            }
                        }
                        return successEntities;
                    });

        } catch (Exception e) {
            LOGGER.error("Failed to batch update entities for table {}", tableName, e);
            return Future.failedFuture(e);
        }
    }

    /**
     * 批量删除
     * 
     * @param ids ID列表
     * @return 删除成功的数量
     */
    public Future<Integer> batchDelete(List<ID> ids) {
        if (ids == null || ids.isEmpty()) {
            return Future.succeededFuture(0);
        }

        try {
            List<Future<Boolean>> deleteFutures = ids.stream()
                    .map(this::delete)
                    .collect(Collectors.toList());

            return Future.all(deleteFutures)
                    .map(results -> {
                        int successCount = 0;
                        for (int i = 0; i < results.size(); i++) {
                            Boolean result = results.resultAt(i);
                            if (Boolean.TRUE.equals(result)) {
                                successCount++;
                            }
                        }
                        return successCount;
                    });

        } catch (Exception e) {
            LOGGER.error("Failed to batch delete IDs for table {}", tableName, e);
            return Future.failedFuture(e);
        }
    }

    // =================== 内部辅助方法 ===================

    /**
     * 构建分页查询SQL
     */
    private Query buildPageableSelect(String tableName, Condition condition, PageRequest pageRequest) {
        String sql = String.format("SELECT * FROM %s", tableName);
        
        // 添加WHERE条件
        if (condition != null && !condition.equals(DSL.noCondition())) {
            String conditionSql = condition.toString();
            sql += " WHERE " + conditionSql;
        }
        
        // 添加排序
        if (pageRequest.hasSorting()) {
            sql += String.format(" ORDER BY %s %s", 
                pageRequest.getSortField(), 
                pageRequest.getSortDirection());
        } else {
            sql += String.format(" ORDER BY %s ASC", primaryKeyField);
        }
        
        // 添加LIMIT和OFFSET
        sql += String.format(" LIMIT %d OFFSET %d", 
            pageRequest.getLimit(), 
            pageRequest.getOffset());
        
        return DSL.query(sql);
    }

    /**
     * 构建排序查询SQL
     */
    private Query buildOrderedSelect(String tableName, Condition condition, String sortField, boolean ascending, Integer limit) {
        String sql = String.format("SELECT * FROM %s", tableName);
        
        if (condition != null && !condition.equals(DSL.noCondition())) {
            sql += " WHERE " + condition.toString();
        }
        
        sql += " ORDER BY " + sortField + " " + (ascending ? "ASC" : "DESC");
        
        if (limit != null && limit > 0) {
            sql += " LIMIT " + limit;
        }
        
        return DSL.query(sql);
    }

    /**
     * 构建多字段排序查询SQL
     */
    private Query buildMultiOrderedSelect(String tableName, Condition condition, Map<String, Boolean> sortFields, Integer limit) {
        String sql = String.format("SELECT * FROM %s", tableName);
        
        if (condition != null && !condition.equals(DSL.noCondition())) {
            sql += " WHERE " + condition.toString();
        }
        
        // 添加多字段排序
        if (sortFields != null && !sortFields.isEmpty()) {
            sql += " ORDER BY ";
            String orderClause = sortFields.entrySet().stream()
                    .map(entry -> entry.getKey() + " " + (entry.getValue() ? "ASC" : "DESC"))
                    .collect(Collectors.joining(", "));
            sql += orderClause;
        } else {
            sql += " ORDER BY " + primaryKeyField + " ASC";
        }
        
        if (limit != null && limit > 0) {
            sql += " LIMIT " + limit;
        }
        
        return DSL.query(sql);
    }

    /**
     * 执行聚合查询
     */
    private Future<Optional<Object>> findAggregate(String field, String aggregateFunction) {
        String sql = String.format("SELECT %s(%s) FROM %s", aggregateFunction, field, tableName);
        Query query = DSL.query(sql);
        
        return getExecutor().executeQuery(query)
                .map(rowSet -> {
                    if (rowSet.size() > 0) {
                        Object value = rowSet.iterator().next().getValue(0);
                        return Optional.ofNullable(value);
                    }
                    return Optional.empty();
                });
    }

    /**
     * 将QueryCondition转换为jOOQ Condition
     */
    protected Condition convertQueryConditionToJooq(QueryCondition queryCondition) {
        if (queryCondition == null) return DSL.noCondition();
        
        String field = queryCondition.getField();
        QueryCondition.ConditionType type = queryCondition.getType();
        Object value = queryCondition.getValue();
        
        Field<Object> tableField = DSL.field(field);
        
        switch (type) {
            case EQUAL:
                return tableField.eq(value);
            case NOT_EQUAL:
                return tableField.ne(value);
            case GREATER_THAN:
                return tableField.gt(value);
            case GREATER_THAN_EQ:
                return tableField.ge(value);
            case LESS_THAN:
                return tableField.lt(value);
            case LESS_THAN_EQ:
                return tableField.le(value);
            case LIKE:
                return tableField.like(String.valueOf(value));
            case NOT_LIKE:
                return tableField.notLike(String.valueOf(value));
            case IS_NULL:
                return tableField.isNull();
            case IS_NOT_NULL:
                return tableField.isNotNull();
            case IN:
                if (value.getClass().isArray()) {
                    return tableField.in((Object[]) value);
                } else if (value instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<Object> valueList = (List<Object>) value;
                    return tableField.in(valueList.toArray());
                }
                return DSL.noCondition();
            case NOT_IN:
                if (value.getClass().isArray()) {
                    return tableField.notIn((Object[]) value);
                } else if (value instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<Object> valueList = (List<Object>) value;
                    return tableField.notIn(valueList.toArray());
                }
                return DSL.noCondition();
            case BETWEEN:
                if (value.getClass().isArray() && ((Object[]) value).length == 2) {
                    Object[] range = (Object[]) value;
                    return tableField.between(range[0], range[1]);
                }
                return DSL.noCondition();
            default:
                return DSL.noCondition();
        }
    }

    /**
     * 将多个QueryCondition转换为jOOQ Condition（指定连接符）
     */
    protected Condition convertQueryConditionsToJooq(List<QueryCondition> conditions, QueryCondition.ConditionType connective) {
        if (conditions == null || conditions.isEmpty()) {
            return DSL.noCondition();
        }
        
        Condition result = convertQueryConditionToJooq(conditions.get(0));
        
        for (int i = 1; i < conditions.size(); i++) {
            Condition nextCondition = convertQueryConditionToJooq(conditions.get(i));
            if (connective == QueryCondition.ConditionType.AND) {
                result = result.and(nextCondition); // AND
            } else if (connective == QueryCondition.ConditionType.OR) {
                result = result.or(nextCondition); // OR
            }
        }
        
        return result;
    }

    // =================== 基础CRUD实现方法 ===================

    private Future<Optional<T>> performInsert(T entity) {
        try {
            if (entity instanceof cn.qaiu.db.dsl.BaseEntity) {
                ((cn.qaiu.db.dsl.BaseEntity) entity).onCreate();
            }

            JsonObject data = entityMapper.toJsonObject(entity);
            data.remove(cn.qaiu.db.dsl.core.FieldNameConverter.toDatabaseFieldName(primaryKeyField));

            Query insertQuery = getDslBuilder().buildInsert(tableName, data);

            return getExecutor().executeInsert(insertQuery)
                    .map(generatedId -> {
                        LOGGER.debug("EnhancedDao.performInsert: generatedId={}", generatedId);
                        setId(entity, generatedId);
                        LOGGER.debug("Inserted entity to table {} with ID: {}", tableName, generatedId);
                        if (generatedId != null && generatedId > 0) {
                            return Optional.of(entity);
                        } else {
                            LOGGER.warn("Insert returned invalid ID: {}, returning empty Optional", generatedId);
                            return Optional.empty();
                        }
                    });
        } catch (Exception e) {
            LOGGER.error("Failed to insert entity", e);
            return Future.failedFuture(e);
        }
    }

    private Future<Optional<T>> performUpdate(T entity) {
        try {
            ID entityId = getId(entity);
            if (entityId == null) {
                return Future.failedFuture(new IllegalArgumentException("Entity ID cannot be null for update"));
            }

            if (entity instanceof cn.qaiu.db.dsl.BaseEntity) {
                ((cn.qaiu.db.dsl.BaseEntity) entity).onUpdate();
            }

            JsonObject data = entityMapper.toJsonObject(entity);
            Condition whereCondition = DSL.field(primaryKeyField).eq(entityId);
            Query updateQuery = getDslBuilder().buildUpdate(tableName, data, whereCondition);

            return getExecutor().executeUpdate(updateQuery)
                    .map(rowCount -> {
                        if (rowCount > 0) {
                            LOGGER.debug("Updated entity in table {} with ID: {}", tableName, entityId);
                            return Optional.of(entity);
                        } else {
                            return Optional.<T>empty();
                        }
                    });
        } catch (Exception e) {
            LOGGER.error("Failed to update entity", e);
            return Future.failedFuture(e);
        }
    }

    private Future<Boolean> performDelete(ID id) {
        if (id == null) {
            return Future.failedFuture(new IllegalArgumentException("ID cannot be null"));
        }

        try {
            Condition whereCondition = DSL.field(primaryKeyField).eq(id);
            Query deleteQuery = getDslBuilder().buildDelete(tableName, whereCondition);

            return getExecutor().executeUpdate(deleteQuery)
                    .map(rowCount -> {
                        LOGGER.debug("Deleted {} rows from table {} where {} = {}", 
                                rowCount, tableName, primaryKeyField, id);
                        return rowCount > 0;
                    });
        } catch (Exception e) {
            LOGGER.error("Failed to delete entity with ID: {}", id, e);
            return Future.failedFuture(e);
        }
    }

    private Future<Optional<T>> performFindById(ID id) {
        if (id == null) {
            return Future.succeededFuture(Optional.empty());
        }

        try {
            Condition condition = DSL.field(primaryKeyField).eq(id);
            Query selectQuery = getDslBuilder().buildSelect(tableName, condition);

            return getExecutor().executeQuery(selectQuery)
                    .map(rowSet -> entityMapper.fromSingle(rowSet));
        } catch (Exception e) {
            LOGGER.error("Failed to find entity by ID: {}", id, e);
            return Future.failedFuture(e);
        }
    }

    private Future<List<T>> performFindByCondition(Condition condition) {
        try {
            Query selectQuery = getDslBuilder().buildSelect(tableName, condition);

            return getExecutor().executeQuery(selectQuery)
                    .map(entityMapper::fromMultiple);
        } catch (Exception e) {
            LOGGER.error("Failed to find entities by condition", e);
            return Future.failedFuture(e);
        }
    }

    private Future<Long> performCount(Condition condition) {
        try {
            Query countQuery = getDslBuilder().buildCount(tableName, condition);

            return getExecutor().executeQuery(countQuery)
                    .map(rowSet -> {
                        if (rowSet.size() > 0) {
                            return rowSet.iterator().next().getLong(0);
                        }
                        return 0L;
                    });
        } catch (Exception e) {
            LOGGER.error("Failed to count entities", e);
            return Future.failedFuture(e);
        }
    }

    /**
     * 获取实体的ID值
     */
    @SuppressWarnings("unchecked")
    protected ID getId(T entity) {
        try {
            Method getIdMethod = entityClass.getMethod("getId");
            return (ID) getIdMethod.invoke(entity);
        } catch (Exception e) {
            LOGGER.warn("Failed to get ID from entity", e);
            return null;
        }
    }

    /**
     * 设置实体的ID值
     */
    protected void setId(T entity, Long id) {
        try {
            Method setIdMethod = entityClass.getMethod("setId", Long.class);
            setIdMethod.invoke(entity, id);
        } catch (Exception e) {
            LOGGER.warn("Failed to set ID for entity", e);
        }
    }

    // Getter methods for subclasses
    protected String getTableName() {
        return tableName;
    }

    protected String getPrimaryKeyField() {
        return primaryKeyField;
    }

    protected DSLContext dsl() {
        return getExecutor().dsl();
    }

    // =================== 自动初始化辅助方法 ===================

    /**
     * 获取JooqExecutor实例（延迟初始化）
     */
    protected JooqExecutor getExecutor() {
        if (executor == null) {
            synchronized (this) {
                if (executor == null) {
                    if (autoExecutorMode) {
                        // 确保DataSourceManager已初始化
                        Vertx vertx = VertxHolder.getVertxInstance();
                        if (vertx == null) {
                            throw new IllegalStateException("Vertx not initialized");
                        }
                        executor = initializeExecutor();
                    } else {
                        throw new IllegalStateException("JooqExecutor not initialized in manual mode");
                    }
                }
            }
        }
        return executor;
    }

    /**
     * 获取JooqDslBuilder实例（延迟初始化）
     */
    protected JooqDslBuilder getDslBuilder() {
        if (dslBuilder == null) {
            synchronized (this) {
                if (dslBuilder == null) {
                    dslBuilder = new JooqDslBuilder(getExecutor().dsl());
                }
            }
        }
        return dslBuilder;
    }

    /**
     * 初始化JooqExecutor（自动模式）
     */
    private JooqExecutor initializeExecutor() {
        try {
            cn.qaiu.db.datasource.DataSourceManager manager = 
                    cn.qaiu.db.datasource.DataSourceManager.getInstance(VertxHolder.getVertxInstance());
            JooqExecutor executor = manager.getExecutor(dataSourceName);
            
            if (executor == null) {
                LOGGER.warn("No executor found for datasource: {}, using default", dataSourceName);
                executor = manager.getDefaultExecutor();
            }
            
            if (executor == null) {
                throw new IllegalStateException("No JooqExecutor available for datasource: " + dataSourceName);
            }
            
            LOGGER.info("JooqExecutor auto-initialized for datasource: {} in EnhancedDao: {}", 
                    dataSourceName, this.getClass().getSimpleName());
            
            return executor;
        } catch (Exception e) {
            LOGGER.error("Failed to auto-initialize JooqExecutor for datasource: {}", dataSourceName, e);
            throw new RuntimeException("Failed to auto-initialize JooqExecutor", e);
        }
    }

    /**
     * 从类注解中获取数据源名称
     */
    private String getDataSourceNameFromAnnotation() {
        // 检查@DataSource注解
        cn.qaiu.db.datasource.DataSource dataSourceAnnotation = 
                this.getClass().getAnnotation(cn.qaiu.db.datasource.DataSource.class);
        if (dataSourceAnnotation != null) {
            return dataSourceAnnotation.value();
        }
        
        // 检查@Dao注解（如果存在）
        try {
            @SuppressWarnings("unchecked")
            Class<? extends java.lang.annotation.Annotation> daoAnnotationClass =
                (Class<? extends java.lang.annotation.Annotation>) Class.forName("cn.qaiu.vx.core.annotaions.Dao");
            if (this.getClass().isAnnotationPresent(daoAnnotationClass)) {
                LOGGER.debug("Found @Dao annotation on class: {}", this.getClass().getName());
            }
        } catch (ClassNotFoundException e) {
            // Dao注解不存在，忽略
        }
        
        return "default";
    }

    /**
     * 从实体类获取表名
     */
    private String getTableNameFromEntity(Class<T> entityClass) {
        try {
            // 检查@Table注解（如果存在）
            try {
                @SuppressWarnings("unchecked")
                Class<? extends java.lang.annotation.Annotation> tableAnnotationClass =
                    (Class<? extends java.lang.annotation.Annotation>) Class.forName("cn.qaiu.db.dsl.Table");
                if (entityClass.isAnnotationPresent(tableAnnotationClass)) {
                    LOGGER.debug("Found @Table annotation on entity: {}", entityClass.getName());
                }
            } catch (ClassNotFoundException e) {
                // Table注解不存在，忽略
            }
            
            // 默认使用类名转下划线
            return convertToSnakeCase(entityClass.getSimpleName());
        } catch (Exception e) {
            LOGGER.warn("Failed to get table name from entity: {}", entityClass.getSimpleName(), e);
            return convertToSnakeCase(entityClass.getSimpleName());
        }
    }

    /**
     * 从实体类获取主键字段名
     */
    private String getPrimaryKeyFromEntity(Class<T> entityClass) {
        try {
            // 检查@Id注解的字段（如果存在）
            try {
                @SuppressWarnings("unchecked")
                Class<? extends java.lang.annotation.Annotation> idAnnotationClass =
                    (Class<? extends java.lang.annotation.Annotation>) Class.forName("cn.qaiu.db.dsl.Id");
                
                for (Method method : entityClass.getMethods()) {
                    if (method.isAnnotationPresent(idAnnotationClass)) {
                        String methodName = method.getName();
                        if (methodName.startsWith("get")) {
                            String fieldName = methodName.substring(3);
                            return convertToSnakeCase(fieldName);
                        }
                    }
                }
            } catch (ClassNotFoundException e) {
                // Id注解不存在，忽略
            }
            
            // 默认使用"id"
            return "id";
        } catch (Exception e) {
            LOGGER.warn("Failed to get primary key from entity: {}", entityClass.getSimpleName(), e);
            return "id";
        }
    }

    /**
     * 转换为下划线命名
     */
    private String convertToSnakeCase(String camelCase) {
        return StringCase.toUnderlineCase(camelCase);
    }

    /**
     * 通过反射获取泛型实体类类型
     * 支持多层继承的泛型类型获取
     */
    @SuppressWarnings("unchecked")
    private Class<?> getGenericEntityClass() {
        try {
            // 获取当前类的泛型信息
            Type genericSuperclass = this.getClass().getGenericSuperclass();
            
            // 如果是参数化类型（ParameterizedType），直接获取第一个泛型参数
            if (genericSuperclass instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) genericSuperclass;
                Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
                if (actualTypeArguments.length > 0) {
                    Type entityType = actualTypeArguments[0];
                    if (entityType instanceof Class) {
                        return (Class<?>) entityType;
                    }
                }
            }
            
            // 如果当前类没有泛型信息，向上查找父类
            Class<?> currentClass = this.getClass();
            while (currentClass != null && currentClass != EnhancedDao.class) {
                Type superclass = currentClass.getGenericSuperclass();
                if (superclass instanceof ParameterizedType) {
                    ParameterizedType parameterizedType = (ParameterizedType) superclass;
                    Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
                    if (actualTypeArguments.length > 0) {
                        Type entityType = actualTypeArguments[0];
                        if (entityType instanceof Class) {
                            return (Class<?>) entityType;
                        }
                    }
                }
                currentClass = currentClass.getSuperclass();
            }
            
            throw new IllegalStateException("无法从泛型中获取实体类类型，请使用带参数的构造函数");
            
        } catch (Exception e) {
            LOGGER.error("获取泛型实体类类型失败", e);
            throw new IllegalStateException("无法从泛型中获取实体类类型，请使用带参数的构造函数", e);
        }
    }

    /**
     * 获取实体类类型
     */
    public Class<T> getEntityClass() {
        return entityClass;
    }

    /**
     * 获取当前数据源名称
     */
    protected String getCurrentDataSource() {
        return dataSourceName;
    }

    /**
     * 切换到指定数据源执行操作
     */
    protected <R> Future<R> executeWithDataSource(String targetDataSource, 
                                                  java.util.function.Function<JooqExecutor, Future<R>> operation) {
        if (!autoExecutorMode) {
            return Future.failedFuture("DataSource switching only supported in auto-executor mode");
        }
        
        return Future.future(promise -> {
            String originalDataSource = dataSourceName;
            try {
                // 临时切换数据源
                dataSourceName = targetDataSource;
                executor = null; // 强制重新初始化
                
                JooqExecutor targetExecutor = getExecutor();
                operation.apply(targetExecutor)
                    .onSuccess(promise::complete)
                    .onFailure(promise::fail);
            } finally {
                // 恢复原始数据源
                dataSourceName = originalDataSource;
                executor = null; // 强制重新初始化
            }
        });
    }
}