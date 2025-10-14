package cn.qaiu.db.dsl.core;

import cn.qaiu.db.dsl.interfaces.JooqDao;
import cn.qaiu.db.dsl.mapper.EntityMapper;
import cn.qaiu.db.dsl.mapper.DefaultMapper;
import cn.qaiu.vx.core.util.StringCase;
import cn.qaiu.vx.core.util.VertxHolder;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;

/**
 * 抽象DAO基类 - 基于真正的 jOOQ DSL
 * 充分利用 jOOQ 的类型安全和 DSL 功能
 * 
 * 支持两种初始化方式：
 * 1. 手动传入JooqExecutor（传统方式）
 * 2. 自动从DataSourceManager获取JooqExecutor（推荐方式）
 */
public abstract class AbstractDao<T, ID> implements JooqDao<T, ID> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractDao.class);

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
    public AbstractDao(JooqExecutor executor, Class<T> entityClass) {
        this.executor = executor;
        this.entityClass = entityClass;
        this.autoExecutorMode = false;
        
        // 立即初始化
        this.dslBuilder = new JooqDslBuilder(executor.dsl());
        this.tableName = dslBuilder.getTableName(entityClass);
        this.primaryKeyField = dslBuilder.getPrimaryKey(entityClass);
        this.entityMapper = new DefaultMapper<>(entityClass, tableName);
        
        LOGGER.debug("AbstractDao initialized with manual JooqExecutor for entity: {}", 
                entityClass.getSimpleName());
    }

    /**
     * 构造函数2：自动获取JooqExecutor（推荐方式）
     */
    public AbstractDao(Class<T> entityClass) {
        this.entityClass = entityClass;
        this.autoExecutorMode = true;
        
        // 初始化基本信息
        this.dataSourceName = getDataSourceNameFromAnnotation();
        this.tableName = getTableNameFromEntity(entityClass);
        this.primaryKeyField = getPrimaryKeyFromEntity(entityClass);
        this.entityMapper = new DefaultMapper<>(entityClass, tableName);
        
        LOGGER.debug("AbstractDao initialized with auto-executor mode for entity: {} with datasource: {}", 
                entityClass.getSimpleName(), dataSourceName);
    }

    /**
     * 构造函数3：无参构造函数（最推荐方式）
     * 自动通过泛型获取实体类类型
     */
    @SuppressWarnings("unchecked")
    public AbstractDao() {
        this.entityClass = (Class<T>) getGenericEntityClass();
        this.autoExecutorMode = true;
        
        // 初始化基本信息
        this.dataSourceName = getDataSourceNameFromAnnotation();
        this.tableName = getTableNameFromEntity(entityClass);
        this.primaryKeyField = getPrimaryKeyFromEntity(entityClass);
        this.entityMapper = new DefaultMapper<>(entityClass, tableName);
        
        LOGGER.debug("AbstractDao initialized with auto-executor mode for entity: {} (from generics) with datasource: {}", 
                entityClass.getSimpleName(), dataSourceName);
    }

    /**
     * 插入实体
     * 
     * @param entity 要插入的实体
     * @return 插入后的实体（包含生成的ID）
     */
    @Override
    public Future<Optional<T>> insert(T entity) {
        try {
            // 调用实体的onCreate方法
            if (entity instanceof cn.qaiu.db.dsl.BaseEntity) {
                ((cn.qaiu.db.dsl.BaseEntity) entity).onCreate();
            }

            JsonObject data = entityMapper.toJsonObject(entity);

            // 移除主键字段（插入时通常由数据库自动生成）
            data.remove(cn.qaiu.db.dsl.core.FieldNameConverter.toDatabaseFieldName(primaryKeyField));

            Query insertQuery = getDslBuilder().buildInsert(tableName, data);

            return getExecutor().executeInsert(insertQuery)
                    .map(generatedId -> {
                        // 设置生成的主键
                        setId(entity, generatedId);
                        LOGGER.debug("Inserted entity to table {} with ID: {}", tableName, generatedId);
                        return Optional.of(entity);
                    });
        } catch (Exception e) {
            LOGGER.error("Failed to insert entity", e);
            return Future.failedFuture(e);
        }
    }

    /**
     * 更新实体
     * 
     * @param entity 要更新的实体
     * @return 更新后的实体
     */
    @Override
    public Future<Optional<T>> update(T entity) {
        try {
            ID entityId = getId(entity);
            if (entityId == null) {
                LOGGER.warn("Entity ID is null, cannot update");
                return Future.failedFuture(new IllegalArgumentException("Entity ID cannot be null for update"));
            }

            LOGGER.debug("Updating entity: ID={}, class={}", entityId, entityClass.getSimpleName());

            // 调用实体的onUpdate方法
            if (entity instanceof cn.qaiu.db.dsl.BaseEntity) {
                ((cn.qaiu.db.dsl.BaseEntity) entity).onUpdate();
            }

            JsonObject data = entityMapper.toJsonObject(entity);
            LOGGER.debug("Entity data for update: {}", data.encode());

            Condition whereCondition = DSL.field(primaryKeyField).eq(entityId);
            Query updateQuery = getDslBuilder().buildUpdate(tableName, data, whereCondition);

            return getExecutor().executeUpdate(updateQuery)
                    .map(rowCount -> {
                        LOGGER.debug("Update query affected {} rows", rowCount);
                        if (rowCount > 0) {
                            LOGGER.debug("Updated entity in table {} with ID: {}", tableName, entityId);
                            return Optional.of(entity);
                        } else {
                            LOGGER.warn("No rows affected by update query for ID: {}", entityId);
                            return Optional.<T>empty();
                        }
                    });
        } catch (Exception e) {
            LOGGER.error("Failed to update entity", e);
            return Future.failedFuture(e);
        }
    }

    /**
     * 根据ID删除实体
     * 
     * @param id 实体ID
     * @return 是否删除成功
     */
    @Override
    public Future<Boolean> delete(ID id) {
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

    /**
     * 根据ID查找实体
     * 
     * @param id 实体ID
     * @return 找到的实体，如果不存在则返回空
     */
    @Override
    public Future<Optional<T>> findById(ID id) {
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

    /**
     * 查找所有实体
     * 
     * @return 所有实体的列表
     */
    @Override
    public Future<List<T>> findAll() {
        try {
            Query selectQuery = getDslBuilder().buildSelect(tableName, DSL.noCondition());

            return getExecutor().executeQuery(selectQuery)
                    .map(entityMapper::fromMultiple);
        } catch (Exception e) {
            LOGGER.error("Failed to find all entities", e);
            return Future.failedFuture(e);
        }
    }

    /**
     * 根据条件查找实体
     * 
     * @param condition 查询条件
     * @return 符合条件的实体列表
     */
    @Override
    public Future<List<T>> findByCondition(Condition condition) {
        try {
            Query selectQuery = getDslBuilder().buildSelect(tableName, condition);

            return getExecutor().executeQuery(selectQuery)
                    .map(entityMapper::fromMultiple);
        } catch (Exception e) {
            LOGGER.error("Failed to find entities by condition", e);
            return Future.failedFuture(e);
        }
    }

    @Override
    public Future<Long> count() {
        try {
            Query countQuery = getDslBuilder().buildCount(tableName, DSL.noCondition());

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

    @Override
    public Future<Long> count(Condition condition) {
        try {
            Query countQuery = dslBuilder.buildCount(tableName, condition);

            return executor.executeQuery(countQuery)
                    .map(rowSet -> {
                        if (rowSet.size() > 0) {
                            return rowSet.iterator().next().getLong(0);
                        }
                        return 0L;
                    });
        } catch (Exception e) {
            LOGGER.error("Failed to count entities by condition", e);
            return Future.failedFuture(e);
        }
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

    /**
     * 获取实体的ID值
     */
    @SuppressWarnings("unchecked")
    protected ID getId(T entity) {
        try {
            // 尝试使用getId方法
            Method getIdMethod = entityClass.getMethod("getId");
            ID id = (ID) getIdMethod.invoke(entity);
            LOGGER.debug("Got ID from entity {}: {}", entityClass.getSimpleName(), id);
            return id;
        } catch (Exception e) {
            LOGGER.warn("Failed to get ID from entity {}: {}", entityClass.getSimpleName(), e.getMessage(), e);
            return null;
        }
    }

    /**
     * 设置实体的ID值
     */
    protected void setId(T entity, Long id) {
        try {
            // 尝试使用setId方法
            Method setIdMethod = entityClass.getMethod("setId", Long.class);
            setIdMethod.invoke(entity, id);
        } catch (Exception e) {
            LOGGER.warn("Failed to set ID for entity", e);
        }
    }

    /**
     * 获取表名（供子类使用）
     */
    protected String getTableName() {
        return tableName;
    }

    /**
     * 获取主键字段名（供子类使用）
     */
    protected String getPrimaryKeyField() {
        return primaryKeyField;
    }

    /**
     * 获取jOOQ DSL上下文（供子类使用）
     */
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
                    cn.qaiu.db.datasource.DataSourceManager.getInstance(null);
            JooqExecutor executor = manager.getExecutor(dataSourceName);
            
            if (executor == null) {
                LOGGER.warn("No executor found for datasource: {}, using default", dataSourceName);
                executor = manager.getDefaultExecutor();
            }
            
            if (executor == null) {
                throw new IllegalStateException("No JooqExecutor available for datasource: " + dataSourceName);
            }
            
            LOGGER.info("JooqExecutor auto-initialized for datasource: {} in DAO: {}", 
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
            while (currentClass != null && currentClass != AbstractDao.class) {
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
    
    // =================== 批量操作方法 ===================
    
    /**
     * 批量插入实体
     * 
     * @param entities 要插入的实体列表
     * @return 插入成功的实体数量
     */
    public Future<Integer> batchInsert(List<T> entities) {
        if (entities == null || entities.isEmpty()) {
            return Future.succeededFuture(0);
        }
        
        try {
            LOGGER.debug("Batch inserting {} entities to table {}", entities.size(), tableName);
            
            // 调用实体的onCreate方法
            entities.forEach(entity -> {
                if (entity instanceof cn.qaiu.db.dsl.BaseEntity) {
                    ((cn.qaiu.db.dsl.BaseEntity) entity).onCreate();
                }
            });
            
            // 构建批量插入查询
            List<Query> insertQueries = entities.stream()
                .map(entity -> {
                    JsonObject data = entityMapper.toJsonObject(entity);
                    // 移除主键字段
                    data.remove(cn.qaiu.db.dsl.core.FieldNameConverter.toDatabaseFieldName(primaryKeyField));
                    return dslBuilder.buildInsert(tableName, data);
                })
                .collect(java.util.stream.Collectors.toList());
            
            // 执行批量插入
            return getExecutor().executeBatch(insertQueries)
                .map(results -> {
                    int successCount = 0;
                    for (int i = 0; i < results.length; i++) {
                        if (results[i] > 0) {
                            successCount++;
                            // 设置生成的主键（如果有）
                            if (i < entities.size()) {
                                setId(entities.get(i), (long) results[i]);
                            }
                        }
                    }
                    LOGGER.debug("Batch insert completed: {} entities inserted", successCount);
                    return successCount;
                });
        } catch (Exception e) {
            LOGGER.error("Failed to batch insert entities", e);
            return Future.failedFuture(e);
        }
    }
    
    /**
     * 批量更新实体
     * 
     * @param entities 要更新的实体列表
     * @return 更新成功的实体数量
     */
    public Future<Integer> batchUpdate(List<T> entities) {
        if (entities == null || entities.isEmpty()) {
            return Future.succeededFuture(0);
        }
        
        try {
            LOGGER.debug("Batch updating {} entities in table {}", entities.size(), tableName);
            
            // 调用实体的onUpdate方法
            entities.forEach(entity -> {
                if (entity instanceof cn.qaiu.db.dsl.BaseEntity) {
                    ((cn.qaiu.db.dsl.BaseEntity) entity).onUpdate();
                }
            });
            
            // 构建批量更新查询
            List<Query> updateQueries = entities.stream()
                .map(entity -> {
                    ID entityId = getId(entity);
                    if (entityId == null) {
                        throw new IllegalArgumentException("Entity ID cannot be null for batch update");
                    }
                    
                    JsonObject data = entityMapper.toJsonObject(entity);
                    Condition whereCondition = DSL.field(primaryKeyField).eq(entityId);
                    return dslBuilder.buildUpdate(tableName, data, whereCondition);
                })
                .collect(java.util.stream.Collectors.toList());
            
            // 执行批量更新
            return getExecutor().executeBatch(updateQueries)
                .map(results -> {
                    int successCount = 0;
                    for (int result : results) {
                        if (result > 0) {
                            successCount++;
                        }
                    }
                    LOGGER.debug("Batch update completed: {} entities updated", successCount);
                    return successCount;
                });
        } catch (Exception e) {
            LOGGER.error("Failed to batch update entities", e);
            return Future.failedFuture(e);
        }
    }
    
    /**
     * 批量删除实体
     * 
     * @param ids 要删除的ID列表
     * @return 删除成功的实体数量
     */
    public Future<Integer> batchDelete(List<ID> ids) {
        if (ids == null || ids.isEmpty()) {
            return Future.succeededFuture(0);
        }
        
        try {
            LOGGER.debug("Batch deleting {} entities from table {}", ids.size(), tableName);
            
            // 构建批量删除查询
            List<Query> deleteQueries = ids.stream()
                .map(id -> {
                    Condition whereCondition = DSL.field(primaryKeyField).eq(id);
                    return dslBuilder.buildDelete(tableName, whereCondition);
                })
                .collect(java.util.stream.Collectors.toList());
            
            // 执行批量删除
            return getExecutor().executeBatch(deleteQueries)
                .map(results -> {
                    int successCount = 0;
                    for (int result : results) {
                        if (result > 0) {
                            successCount++;
                        }
                    }
                    LOGGER.debug("Batch delete completed: {} entities deleted", successCount);
                    return successCount;
                });
        } catch (Exception e) {
            LOGGER.error("Failed to batch delete entities", e);
            return Future.failedFuture(e);
        }
    }
    
    /**
     * 批量删除实体（根据条件）
     * 
     * @param condition 删除条件
     * @return 删除成功的实体数量
     */
    public Future<Integer> batchDeleteByCondition(Condition condition) {
        if (condition == null) {
            return Future.failedFuture(new IllegalArgumentException("Condition cannot be null"));
        }
        
        try {
            LOGGER.debug("Batch deleting entities from table {} with condition", tableName);
            
            Query deleteQuery = dslBuilder.buildDelete(tableName, condition);
            
            return getExecutor().executeUpdate(deleteQuery)
                .map(rowCount -> {
                    LOGGER.debug("Batch delete by condition completed: {} entities deleted", rowCount);
                    return rowCount;
                });
        } catch (Exception e) {
            LOGGER.error("Failed to batch delete entities by condition", e);
            return Future.failedFuture(e);
        }
    }
    
    /**
     * 批量插入或更新实体（UPSERT）
     * 如果实体ID存在则更新，否则插入
     * 
     * @param entities 要插入或更新的实体列表
     * @return 操作成功的实体数量
     */
    public Future<Integer> batchUpsert(List<T> entities) {
        if (entities == null || entities.isEmpty()) {
            return Future.succeededFuture(0);
        }
        
        try {
            LOGGER.debug("Batch upserting {} entities in table {}", entities.size(), tableName);
            
            // 分离需要插入和更新的实体
            List<T> toInsert = new java.util.ArrayList<>();
            List<T> toUpdate = new java.util.ArrayList<>();
            
            for (T entity : entities) {
                ID entityId = getId(entity);
                if (entityId == null) {
                    toInsert.add(entity);
                } else {
                    // 检查实体是否存在
                    exists(entityId)
                        .onSuccess(exists -> {
                            if (exists) {
                                toUpdate.add(entity);
                            } else {
                                toInsert.add(entity);
                            }
                        });
                }
            }
            
            // 执行批量插入和更新
            Future<Integer> insertFuture = batchInsert(toInsert);
            Future<Integer> updateFuture = batchUpdate(toUpdate);
            
            return Future.all(insertFuture, updateFuture)
                .map(compositeFuture -> {
                    Integer insertCount = insertFuture.result();
                    Integer updateCount = updateFuture.result();
                    int totalCount = (insertCount != null ? insertCount : 0) + 
                                   (updateCount != null ? updateCount : 0);
                    LOGGER.debug("Batch upsert completed: {} inserted, {} updated, total: {}", 
                               insertCount, updateCount, totalCount);
                    return totalCount;
                });
        } catch (Exception e) {
            LOGGER.error("Failed to batch upsert entities", e);
            return Future.failedFuture(e);
        }
    }
}