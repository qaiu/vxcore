package cn.qaiu.db.dsl.lambda;

import cn.qaiu.db.dsl.core.JooqExecutor;
import cn.qaiu.db.dsl.core.EnhancedDao;
import cn.qaiu.db.dsl.mapper.EntityMapper;
import cn.qaiu.db.dsl.mapper.DefaultMapper;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

/**
 * Lambda DAO基类
 * 提供类似MyBatis-Plus的Lambda查询功能
 * 
 * @param <T> 实体类型
 * @param <ID> 主键类型
 * @author qaiu
 */
public abstract class LambdaDao<T, ID> extends EnhancedDao<T, ID> {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(LambdaDao.class);
    
    public LambdaDao(JooqExecutor executor, Class<T> entityClass) {
        super(executor, entityClass);
    }
    
    /**
     * 创建Lambda查询包装器
     */
    public LambdaQueryWrapper<T> lambdaQuery() {
        Table<?> table = DSL.table(tableName);
        return new LambdaQueryWrapper<>(executor.dsl(), table, entityClass);
    }
    
    /**
     * Lambda查询 - 根据条件查询列表
     */
    public Future<List<T>> lambdaList(LambdaQueryWrapper<T> wrapper) {
        Query select = wrapper.buildSelect();
        return executor.executeQuery(select)
                .map(rows -> {
                    // 手动映射行数据到实体对象
                    List<T> result = new java.util.ArrayList<>();
                    for (io.vertx.sqlclient.Row row : rows) {
                        T entity = entityMapper.fromRow(row);
                        result.add(entity);
                    }
                    return result;
                });
    }
    
    /**
     * Lambda查询 - 根据条件查询单个对象
     */
    public Future<Optional<T>> lambdaOne(LambdaQueryWrapper<T> wrapper) {
        return lambdaList(wrapper.limit(1))
                .map(list -> list.isEmpty() ? Optional.empty() : Optional.of(list.get(0)));
    }
    
    /**
     * Lambda查询 - 根据条件查询数量
     */
    public Future<Long> lambdaCount(LambdaQueryWrapper<T> wrapper) {
        Query countSelect = wrapper.buildCount();
        return executor.executeQuery(countSelect)
                .map(rows -> {
                    if (rows.size() > 0) {
                        return rows.iterator().next().getLong(0);
                    }
                    return 0L;
                });
    }
    
    /**
     * Lambda查询 - 检查是否存在
     */
    public Future<Boolean> lambdaExists(LambdaQueryWrapper<T> wrapper) {
        Query existsSelect = wrapper.buildExists();
        return executor.executeQuery(existsSelect)
                .map(rows -> rows.size() > 0);
    }
    
    /**
     * Lambda查询 - 分页查询
     */
    public Future<LambdaPageResult<T>> lambdaPage(LambdaQueryWrapper<T> wrapper, long current, long size) {
        // 先查询总数
        Future<Long> countFuture = lambdaCount(wrapper);
        
        // 再查询分页数据
        Future<List<T>> listFuture = lambdaList(wrapper.page(current, size));
        
        return Future.all(countFuture, listFuture)
                .map(compositeFuture -> {
                    Long total = compositeFuture.resultAt(0);
                    List<T> records = compositeFuture.resultAt(1);
                    
                    return new LambdaPageResult<>(records, total, current, size);
                });
    }
    
    /**
     * Lambda查询 - 根据条件删除
     */
    public Future<Integer> lambdaDelete(LambdaQueryWrapper<T> wrapper) {
        Condition condition = wrapper.buildCondition();
        if (condition == DSL.noCondition()) {
            // 如果没有条件，不允许删除所有数据
            return Future.failedFuture("Delete operation without condition is not allowed");
        }
        
        DeleteConditionStep<?> delete = executor.dsl()
                .deleteFrom(DSL.table(DSL.name(tableName)))
                .where(condition);
        
        return executor.executeUpdate(delete);
    }
    
    /**
     * Lambda查询 - 根据条件更新
     */
    public Future<Integer> lambdaUpdate(LambdaQueryWrapper<T> wrapper, T entity) {
        Condition condition = wrapper.buildCondition();
        if (condition == DSL.noCondition()) {
            // 如果没有条件，不允许更新所有数据
            return Future.failedFuture("Update operation without condition is not allowed");
        }
        
        // 将实体转换为更新字段
        EntityMapper<T> mapper = new DefaultMapper<>(entityClass, tableName);
        JsonObject updateData = mapper.toJsonObject(entity);
        
        // 移除主键字段，避免更新主键
        updateData.remove(primaryKeyField);
        
        // 移除时间字段，避免时间格式解析问题
        updateData.remove("createTime");
        updateData.remove("updateTime");
        
        UpdateSetFirstStep<?> update = executor.dsl()
                .update(DSL.table(DSL.name(tableName)));
        
        UpdateSetMoreStep<?> setStep = null;
        for (String field : updateData.fieldNames()) {
            Object value = updateData.getValue(field);
            if (value != null) {
                // 将Java字段名转换为数据库字段名
                String dbFieldName = cn.qaiu.db.dsl.core.FieldNameConverter.toDatabaseFieldName(field);
                if (setStep == null) {
                    setStep = update.set(DSL.field(DSL.name(dbFieldName)), value);
                } else {
                    setStep = setStep.set(DSL.field(DSL.name(dbFieldName)), value);
                }
            }
        }
        
        if (setStep == null) {
            return Future.succeededFuture(0);
        }
        
        UpdateConditionStep<?> whereStep = setStep.where(condition);
        
        return executor.executeUpdate(whereStep)
                .map(result -> result);
    }
    
    /**
     * 便捷方法 - 根据字段等值查询
     */
    public <R> Future<List<T>> lambdaList(SFunction<T, R> column, R value) {
        return lambdaList(lambdaQuery().eq(column, value));
    }
    
    /**
     * 便捷方法 - 根据字段等值查询单个
     */
    public <R> Future<Optional<T>> lambdaOne(SFunction<T, R> column, R value) {
        return lambdaOne(lambdaQuery().eq(column, value));
    }
    
    /**
     * 便捷方法 - 根据字段等值查询数量
     */
    public <R> Future<Long> lambdaCount(SFunction<T, R> column, R value) {
        return lambdaCount(lambdaQuery().eq(column, value));
    }
    
    /**
     * 便捷方法 - 根据字段等值检查存在
     */
    public <R> Future<Boolean> lambdaExists(SFunction<T, R> column, R value) {
        return lambdaExists(lambdaQuery().eq(column, value));
    }
    
    /**
     * 插入或更新（UPSERT）操作
     * 根据主键判断是插入还是更新
     * 
     * @param entity 实体对象
     * @return 操作结果，包含是否为新插入的记录
     */
    public Future<UpsertResult<T>> insertOrUpdate(T entity) {
        // 将实体转换为JSON对象
        EntityMapper<T> mapper = new DefaultMapper<>(entityClass, tableName);
        JsonObject entityData = mapper.toJsonObject(entity);
        
        // 获取主键值
        Object primaryKeyValue = entityData.getValue(primaryKeyField);
        
        if (primaryKeyValue == null || (primaryKeyValue instanceof Number && ((Number) primaryKeyValue).longValue() == 0)) {
            // 主键为空或为0，执行插入操作
            return insert(entity)
                    .map(optional -> {
                        if (optional.isPresent()) {
                            return new UpsertResult<>(optional.get(), true);
                        } else {
                            throw new RuntimeException("Insert operation failed");
                        }
                    });
        } else {
            // 主键有值，先检查是否存在
            SFunction<T, ?> primaryKeyFieldLambda = getPrimaryKeyFieldLambda();
            if (primaryKeyFieldLambda == null) {
                // 如果无法获取主键字段的Lambda表达式，直接尝试更新，失败则插入
                return update(entity)
                        .compose(optional -> {
                            if (optional.isPresent()) {
                                // 更新成功
                                return Future.succeededFuture(new UpsertResult<>(optional.get(), false));
                            } else {
                                // 更新失败，尝试插入
                                return insert(entity)
                                        .map(insertOptional -> {
                                            if (insertOptional.isPresent()) {
                                                return new UpsertResult<>(insertOptional.get(), true);
                                            } else {
                                                throw new RuntimeException("Both update and insert operations failed");
                                            }
                                        });
                            }
                        });
            } else {
                // 使用Lambda表达式构建条件检查是否存在
                @SuppressWarnings("unchecked")
                SFunction<T, Object> primaryKeyFieldLambdaObj = (SFunction<T, Object>) primaryKeyFieldLambda;
                return lambdaExists(lambdaQuery().eq(primaryKeyFieldLambdaObj, primaryKeyValue))
                        .compose(exists -> {
                            if (exists) {
                                // 存在则更新
                                return update(entity)
                                        .map(optional -> {
                                            if (optional.isPresent()) {
                                                return new UpsertResult<>(optional.get(), false);
                                            } else {
                                                throw new RuntimeException("Update operation failed");
                                            }
                                        });
                            } else {
                                // 不存在则插入
                                return insert(entity)
                                        .map(optional -> {
                                            if (optional.isPresent()) {
                                                return new UpsertResult<>(optional.get(), true);
                                            } else {
                                                throw new RuntimeException("Insert operation failed");
                                            }
                                        });
                            }
                        });
            }
        }
    }
    
    /**
     * 批量插入或更新（UPSERT）操作
     * 
     * @param entities 实体对象列表
     * @return 操作结果统计
     */
    public Future<BatchUpsertResult<T>> batchInsertOrUpdate(List<T> entities) {
        if (entities == null || entities.isEmpty()) {
            return Future.succeededFuture(new BatchUpsertResult<>(0, 0));
        }
        
        List<Future<UpsertResult<T>>> futures = entities.stream()
                .map(this::insertOrUpdate)
                .toList();
        
        return Future.all(futures)
                .map(compositeFuture -> {
                    int insertCount = 0;
                    int updateCount = 0;
                    List<T> results = new java.util.ArrayList<>();
                    
                    for (int i = 0; i < futures.size(); i++) {
                        UpsertResult<T> result = compositeFuture.resultAt(i);
                        results.add(result.getEntity());
                        if (result.isInserted()) {
                            insertCount++;
                        } else {
                            updateCount++;
                        }
                    }
                    
                    return new BatchUpsertResult<>(insertCount, updateCount, results);
                });
    }
    
    /**
     * 根据条件插入或更新（UPSERT）操作
     * 使用ON DUPLICATE KEY UPDATE语法（MySQL）或ON CONFLICT语法（PostgreSQL）
     * 
     * @param entity 实体对象
     * @param conflictColumns 冲突字段列表（用于ON CONFLICT判断）
     * @return 操作结果
     */
    public Future<UpsertResult<T>> insertOrUpdateOnConflict(T entity, List<String> conflictColumns) {
        // 将实体转换为JSON对象
        EntityMapper<T> mapper = new DefaultMapper<>(entityClass, tableName);
        JsonObject entityData = mapper.toJsonObject(entity);
        
        // 构建INSERT语句
        List<String> columns = new java.util.ArrayList<>();
        List<Object> values = new java.util.ArrayList<>();
        
        for (String key : entityData.fieldNames()) {
            if (!"id".equals(key)) {
                String dbFieldName = cn.qaiu.db.dsl.core.FieldNameConverter.toDatabaseFieldName(key);
                columns.add(dbFieldName);
                values.add(entityData.getValue(key));
            }
        }
        
        // 构建ON DUPLICATE KEY UPDATE子句
        List<String> updateClauses = new java.util.ArrayList<>();
        for (String column : columns) {
            if (!conflictColumns.contains(column)) {
                updateClauses.add(column + " = VALUES(" + column + ")");
            }
        }
        
        // 添加更新时间
        if (!updateClauses.contains("update_time = VALUES(update_time)")) {
            updateClauses.add("update_time = NOW()");
        }
        
        // 构建完整的UPSERT SQL
        String sql = String.format(
                "INSERT INTO %s (%s) VALUES (%s) ON DUPLICATE KEY UPDATE %s",
                tableName,
                String.join(", ", columns),
                String.join(", ", columns.stream().map(c -> "?").toArray(String[]::new)),
                String.join(", ", updateClauses)
        );
        
        LOGGER.debug("Generated UPSERT SQL: {}", sql);
        LOGGER.debug("Values: {}", values);
        
        Query query = DSL.query(sql, values.toArray());
        
        return executor.executeUpdate(query)
                .compose(updateCount -> {
                    // 重新查询获取最新数据
                    Object primaryKeyValue = entityData.getValue(primaryKeyField);
                    if (primaryKeyValue != null) {
                        SFunction<T, ?> primaryKeyFieldLambda = getPrimaryKeyFieldLambda();
                        if (primaryKeyFieldLambda != null) {
                            @SuppressWarnings("unchecked")
                            SFunction<T, Object> primaryKeyFieldLambdaObj = (SFunction<T, Object>) primaryKeyFieldLambda;
                            return lambdaOne(lambdaQuery().eq(primaryKeyFieldLambdaObj, primaryKeyValue))
                                    .map(optional -> {
                                        if (optional.isPresent()) {
                                            // 根据更新行数判断是插入还是更新
                                            boolean isInserted = updateCount == 0; // MySQL中INSERT返回0表示更新
                                            return new UpsertResult<>(optional.get(), isInserted);
                                        } else {
                                            throw new RuntimeException("Failed to retrieve entity after upsert");
                                        }
                                    });
                        } else {
                            // 无法使用Lambda表达式，直接返回结果
                            boolean isInserted = updateCount == 0;
                            return Future.succeededFuture(new UpsertResult<>(entity, isInserted));
                        }
                    } else {
                        throw new RuntimeException("Primary key is required for upsert operation");
                    }
                });
    }
    
    /**
     * 获取主键字段的Lambda表达式
     * 子类可以重写此方法来提供更精确的主键字段访问
     * 
     * 注意：此方法返回null，子类需要重写此方法来提供具体的Lambda表达式
     */
    protected <R> SFunction<T, R> getPrimaryKeyFieldLambda() {
        // 默认实现返回null，子类需要重写此方法
        // 例如：return User::getId;
        LOGGER.warn("getPrimaryKeyFieldLambda() not implemented in subclass, returning null");
        return null;
    }
    
    /**
     * UPSERT操作结果
     */
    public static class UpsertResult<T> {
        private final T entity;
        private final boolean inserted;
        
        public UpsertResult(T entity, boolean inserted) {
            this.entity = entity;
            this.inserted = inserted;
        }
        
        public T getEntity() {
            return entity;
        }
        
        public boolean isInserted() {
            return inserted;
        }
        
        public boolean isUpdated() {
            return !inserted;
        }
        
        @Override
        public String toString() {
            return "UpsertResult{" +
                    "inserted=" + inserted +
                    ", entity=" + entity +
                    '}';
        }
    }
    
    /**
     * 批量UPSERT操作结果
     */
    public static class BatchUpsertResult<T> {
        private final int insertCount;
        private final int updateCount;
        private final List<T> entities;
        
        public BatchUpsertResult(int insertCount, int updateCount) {
            this(insertCount, updateCount, new java.util.ArrayList<>());
        }
        
        public BatchUpsertResult(int insertCount, int updateCount, List<T> entities) {
            this.insertCount = insertCount;
            this.updateCount = updateCount;
            this.entities = entities;
        }
        
        public int getInsertCount() {
            return insertCount;
        }
        
        public int getUpdateCount() {
            return updateCount;
        }
        
        public List<T> getEntities() {
            return entities;
        }
        
        public int getTotalCount() {
            return insertCount + updateCount;
        }
        
        @Override
        public String toString() {
            return "BatchUpsertResult{" +
                    "insertCount=" + insertCount +
                    ", updateCount=" + updateCount +
                    ", totalCount=" + getTotalCount() +
                    '}';
        }
    }
}
